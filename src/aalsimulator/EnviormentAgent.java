/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package aalsimulator;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.time.LocalTime;
/**
 *
 * @author Informática
 */
public class EnviormentAgent extends Agent {
    protected AID _hourAgent;
    protected LocalTime _time;
    private double _temperature;
    private double _humidity;
    private double _light;
    
    private boolean _kitchen;
    private int _kitchenTimeOn;
    private boolean _door;
    private boolean _windows;
    
    public void setup() {
        _temperature = 0.0;
        _humidity = 0.0;
        _light = 0.0;
        _kitchen = false;
        _kitchenTimeOn = 0;
        _door = false;
        _windows = false;
        
        addBehaviour(new TickerBehaviour(this, 10000) {
            @Override
            protected void onTick() {
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("aal.simulation.time");
                template.addServices(sd);
                try {
                    DFAgentDescription[] result = DFService.search(myAgent, template); 
                    System.out.println("Found the following seller agents:");
                    if(result.length != 0) {
                      _hourAgent =  result[0].getName();
                    }
                    myAgent.addBehaviour(new UpdateEnviorment());
                }
                catch (FIPAException fe) {
                        fe.printStackTrace();
                }
            }
        });
    }
    
    public class UpdateEnviorment extends Behaviour {
        public int _step;
        private MessageTemplate mt;
        
        public UpdateEnviorment() {
            _step = 0;
        }

        @Override
        public void action() {
            switch(_step){
                case 0:
                    askForTime();
                    break;
                case 1:
                    setTime();
                    break;
                case 2:
                    updateVariables();
                    break; 
                default:
                    
                    break;
            }
        }

        @Override
        public boolean done() {
            if(_step > 1) return true;
            return false;
        }
        

        private void askForTime() {
            ACLMessage cfp = new ACLMessage(ACLMessage.REQUEST);
            cfp.addReceiver(_hourAgent);
            cfp.setContent("aal.simulation.time.get");
            cfp.setConversationId("aal.simulation.time");
            cfp.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
            myAgent.send(cfp);
            // Prepare the template to get proposals
            mt = MessageTemplate.and(MessageTemplate.MatchConversationId("aal.simulation.time"),
                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
            _step = 1;
        }
        
        private void setTime() {
            ACLMessage reply = myAgent.receive(mt);
            if (reply != null) {
                if (reply.getPerformative() == ACLMessage.INFORM) {
                        // This is an offer 
                    String timeFormat = reply.getContent();
                    _time = LocalTime.parse(timeFormat);
                    _step = 2;
                }
            }
            else {
                    block();
            }
        }
        
        private void updateVariables() {
            
        }
    
    } 
}
