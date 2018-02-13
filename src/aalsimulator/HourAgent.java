/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package aalsimulator;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.time.*;

/**
 *
 * @author Informática
 */
public class HourAgent extends Agent {
    
    private HourAgentGUI _hour;
    protected LocalTime _time;
    protected ACLMessage _finishEventReply;
    protected LocalTime  _eventFinshTime;
    
    @Override
    protected void setup() {
        DFAgentDescription dfd = new DFAgentDescription();
	dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("aal.simulation.time");
        sd.setName("aal.simulation.time");
        dfd.addServices(sd);
        try {
                DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
                fe.printStackTrace();
        }
        _hour = new HourAgentGUI(this);
        _hour.showGui();
        _time = LocalTime.of(6, 0);
        addBehaviour(new RespondeTimeService());
        addBehaviour(new EventFinishTimeService());
        addBehaviour(new HourTickerBehaviour(this, 1000));
    }
    
        public class HourTickerBehaviour extends TickerBehaviour {

            public HourTickerBehaviour(Agent a, long period) {
                super(a, period);
            }

            @Override
            protected void onTick() {
                _time = _time.plusMinutes(1);
                _hour.setTime(_time.toString());
                if(null != _eventFinshTime && _time.equals(_eventFinshTime)) {
                    myAgent.addBehaviour(new EventFinishedInform());
                }
         }
    
    }
        
        private class RespondeTimeService extends CyclicBehaviour {
            public void action() {
                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
                ACLMessage msg = myAgent.receive(mt);
                if (msg != null) {
                    System.out.println("[HOUR AGENT] Recieved time request from " + msg.getSender().getLocalName());
                    // CFP Message received. Process it
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    if("aal.simulation.time.get".equals(msg.getContent())){
                        reply.setContent(_time.toString());
                        myAgent.send(reply);
                    } else {
                        reply.setContent("00:00:00");
                    }
                    
            } else {
                    block();
                }
            }
	}  // End of inner class OfferRequestsServer
        
        private class EventFinishTimeService extends CyclicBehaviour {
            public void action() {
                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                ACLMessage msg = myAgent.receive(mt);
                if (msg != null) {
                    System.out.println("[HOUR AGENT] Recieved event finishing time from " + msg.getSender().getLocalName());
                    // CFP Message received. Process it
                    if("aal.simulation.event.endtime".equals(msg.getConversationId())){
                        _finishEventReply = msg.createReply();
                        _eventFinshTime = LocalTime.parse(msg.getContent());
                        System.out.println("[HOUR AGENT] Time to finish event: " + msg.getContent());
                    }
                } else {
                    block();
                }
            }
	}  // End of inner class OfferRequestsServer
        
        private class EventFinishedInform extends OneShotBehaviour {

            @Override
            public void action() {
                System.out.println("[HOUR AGENT] Sending event end notice.");
                _finishEventReply.setPerformative(ACLMessage.INFORM);
                _finishEventReply.setContent("aal.simulation.event.end");
                myAgent.send(_finishEventReply);
            }
        
        }
}
