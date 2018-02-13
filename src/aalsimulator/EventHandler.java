/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package aalsimulator;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.time.LocalTime;
import javax.swing.JTextField;

/**
 *
 * @author Informática
 */
public class EventHandler extends Agent {
    final protected String _host = "localhost";
    protected AID _hourAgent;
    protected AID _envAgent;
    protected LocalTime _time;
    protected JTextField _eventSend;
    
    @Override
    public void setup() {
        
        registerService();
        getAgents();
        addBehaviour(new EventListener());
    }
    
    private void registerService() {
        DFAgentDescription dfd = new DFAgentDescription();
	dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("aal.simulation.handler");
        sd.setName("aal.simulation.handler");
        dfd.addServices(sd);
        try {
                DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
                fe.printStackTrace();
        }
    }
    
    private void getAgents() {
        DFAgentDescription timeTemplate = new DFAgentDescription();
        DFAgentDescription envTemplate = new DFAgentDescription();
        ServiceDescription sdTime = new ServiceDescription();
        ServiceDescription sdEnv = new ServiceDescription();
        sdTime.setType("aal.simulation.time");
        sdEnv.setType("aal.simulation.enviorment");
        timeTemplate.addServices(sdTime);
        envTemplate.addServices(sdEnv);
        try {
            DFAgentDescription[] result = DFService.search(this, timeTemplate); 
            if(result.length != 0) {
              _hourAgent =  result[0].getName();
            }
            result = DFService.search(this, envTemplate); 
            if(result.length != 0) {
              _envAgent =  result[0].getName();
            }
        }
        catch (FIPAException fe) {
                fe.printStackTrace();
        }
    }    
    
    public class EventListener extends CyclicBehaviour {

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.PROPAGATE);
                ACLMessage msg = myAgent.receive(mt);
                if (msg != null) {
                    System.out.println("[HANDLER AGENT] Recieved time event info from " + msg.getSender().getLocalName());
                    // CFP Message received. Process it
                    if("aal.simulation.event".equals(msg.getConversationId())) {
                        myAgent.addBehaviour(new SendEventService(msg.getContent()));
                    }
                    
            } else {
                    block();
                }
        }
    
    }
    
    public class SendEventService extends Behaviour {
        final private String _eventNameID;
        private String _notificationToSend;
        private int _step;
        private int _stepsToRun;
        private MessageTemplate mt;
        
        
        public SendEventService(String eventName) {
            _eventNameID = eventName;
            _step = 0;
        }
        
        @Override
        public void action() {
            switch(_step){
                case 0:
                    setSendingParams();
                    break;
                case 1:
                    askForTime();
                    break;
                case 2:
                    getTime();
                    break;
                case 3:
                    askForEnviormentInfo();
                    break;
                case 4:
                    getEnviormentInfo();
                    break;
                case 5:
                    sendNotifications();
                    break;
                default:
                    break;
            }
        }
        
        private void setSendingParams() {
            switch(_eventNameID) {
                case "aal.simulation.event.fall":
                    _stepsToRun = 0;
                    _step = 5;
                    break;
                case "aal.simulation.event.sleep.end":
                    _stepsToRun = 1;
                    _step = 1;
                    break;
                case "aal.simulation.event.kitchen":
                case "aal.simulation.event.goout":
                case "aal.simulation.event.sleep.begin":
                    _stepsToRun = 1;
                    _step = 3;
                    break;
                default:
                    _step = 6;
                    break;
            }
        }
        
        private void askForTime() {
            ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
            request.addReceiver(_hourAgent);
            request.setContent("aal.simulation.time.get");
            request.setConversationId("aal.simulation.time");
            request.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
            myAgent.send(request);
            System.out.println("[HANDLER AGENT] Sendig time request to " + _hourAgent.getLocalName() );
            // Prepare the template to get proposals
            mt = MessageTemplate.and(MessageTemplate.MatchConversationId("aal.simulation.time"),
                            MessageTemplate.MatchInReplyTo(request.getReplyWith()));
            _step = 2;
        }
        
        private void getTime() {
            ACLMessage reply = myAgent.receive(mt);
            if (reply != null) {
                if (reply.getPerformative() == ACLMessage.INFORM) {
                        // This is an offer 
                    String timeFormat = reply.getContent();
                    _time = LocalTime.parse(timeFormat);
                    
                    if(_stepsToRun > 1) {
                        _step = 3;
                        _stepsToRun--;
                    } else {
                        _step = 5;
                    }
                    
                }
            }
            else {
                    block();
            }
        }
        
        private void askForEnviormentInfo() {
            ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
            request.addReceiver(_envAgent);
            request.setContent("aal.simulation.enviorment.get");
            request.setConversationId("aal.simulation.enviorment");
            request.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
            myAgent.send(request);
            System.out.println("[HANDLER AGENT] Sendig env request to " + _envAgent.getLocalName() );
            // Prepare the template to get proposals
            mt = MessageTemplate.and(MessageTemplate.MatchConversationId("aal.simulation.enviorment"),
                            MessageTemplate.MatchInReplyTo(request.getReplyWith()));
            _step = 4;
        }
        
        private void getEnviormentInfo() {
            ACLMessage reply = myAgent.receive(mt);
            if (reply != null) {
                if (reply.getPerformative() == ACLMessage.INFORM) {
                        // This is an offer 
                    System.out.println(reply.getContent());
                    
                    if(_stepsToRun > 1) {
                        _step = 1;
                        _stepsToRun--;
                    } else {
                        _step = 5;
                    }
                    
                }
            }
            else {
                    block();
            }
        }
        
        private void sendNotifications() {
            if(shouldSendNotification()) {
                System.out.println("Sendinf notifications to " + _host + ": " + _notificationToSend);
            }
            _step = 6;
        }
        
        private String getNotificationContent() {
            String notification = "";
            switch(_eventNameID) {
                case "aal.simulation.event.fall":
                    notification = "event=fall";
                    break;
                case "aal.simulation.event.sleep.end":
                    if(_time.isAfter(LocalTime.of(6, 0)) && _time.isBefore(LocalTime.of(12, 0))) {
                        notification = "event=wake";
                    }
                    break;
                case "aal.simulation.event.kitchen":
                    notification = "event=kitchen";
                    break;
                case "aal.simulation.event.goout":
                    notification = "event=leave&options=doorOpen";
                    break;
                case "aal.simulation.event.sleep.begin":
                    notification = "event=sleep&options=kitchenOn";
                    break;
                case "aal.simulation.event.reminder":
                    notification = "event=reminder&options=inmediate";
                    break;
                default:
                    break;
            }
            return notification;
        }
        
        private boolean shouldSendNotification() {
            _notificationToSend = getNotificationContent();
            return !_notificationToSend.isEmpty();
        }

        @Override
        public boolean done() {
            return _step > 5;
        }
    
    }

}
