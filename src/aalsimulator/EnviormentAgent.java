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
import java.util.Random;
/**
 *
 * @author Informática
 */
public class EnviormentAgent extends Agent {
    protected EnviormentAgentGUI _gui;
    protected AID _hourAgent;
    protected LocalTime _time;
    protected double _temperature;
    protected double _humidity;
    protected double _light;
    
    private boolean _kitchen;
    private int _kitchenTimeOn;
    private boolean _door;
    private boolean _windows;
    
    @Override
    public void setup() {
        _temperature = 22.0;
        _humidity = 0.0;
        _light = 0.0;
        _kitchen = false;
        _kitchenTimeOn = 0;
        _door = false;
        _windows = false;
        _gui = new EnviormentAgentGUI(this); 
        _gui.showGui();
        
        registerService();
        addBehaviour(new TickerBehaviour(this, 5000) {
            @Override
            protected void onTick() {
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("aal.simulation.time");
                template.addServices(sd);
                try {
                    DFAgentDescription[] result = DFService.search(myAgent, template); 
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
    
    public void registerService() {
        DFAgentDescription dfd = new DFAgentDescription();
	dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("aal.simulation.enviorment");
        sd.setName("aal.simulation.enviorment");
        dfd.addServices(sd);
        try {
                DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
                fe.printStackTrace();
        }
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
                case 3:
                    sendNotifications();
                default:
                    
                    break;
            }
        }

        @Override
        public boolean done() {
            if(_step > 3) return true;
            return false;
        }
        

        private void askForTime() {
            ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
            request.addReceiver(_hourAgent);
            request.setContent("aal.simulation.time.get");
            request.setConversationId("aal.simulation.time");
            request.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
            myAgent.send(request);
            System.out.println("[ENV AGENT] Sendig time request to " + _hourAgent.getLocalName() );
            // Prepare the template to get proposals
            mt = MessageTemplate.and(MessageTemplate.MatchConversationId("aal.simulation.time"),
                            MessageTemplate.MatchInReplyTo(request.getReplyWith()));
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
            System.out.println("Recieved time: " + _time.toString());
            //Reglas para manegar ambiente
            updateTemperature();
            //updateHumidity();
            //updateLight();
            _gui.setVariables(_temperature);
            _step = 3;
        }

        private void updateTemperature() {
            if(_time.isAfter(LocalTime.of(5, 0))) {
                if(_time.isBefore(LocalTime.of(10, 0))) {
                    // Grow slowly
                    _temperature += calculateValue(true, true);
                } else {
                    if(_time.isBefore(LocalTime.of(14,0))) {
                        // Grow rapidly
                        _temperature += calculateValue(true, false);
                    } else {
                        if(_time.isBefore(LocalTime.of(16,0))) {
                            // Grow Slowy
                            _temperature += calculateValue(true, true);
                        } else {
                            // Lower Slowly
                            _temperature += calculateValue(false, true);
                        }
                    }
                }
            } else {
                // Lower Rapidly
                _temperature += calculateValue(false, false);
            }
        }
        
        /**
         * 
         * @param grow : True = grow, False = lower
         * @param speed : True = slow, false = rapid
         * @return 
         */
        private double calculateValue(boolean grow, boolean speed) {
            double growth = 0.0;
            if(grow){
                if(speed) {
                    growth = calculateRange(6,3,1) * 2.5;
                } else {
                    growth = calculateRange(7,2,1) * 5;
                }
            } else {
                if(speed) {
                    growth =  - calculateRange(6,3,1) * 2.5;
                } else {
                    growth =  - calculateRange(7,2 ,1) * 5;
                }
            }
            return growth;
        }
        
        private double calculateRange(int grow, int stay, int lower) {
            Random rand = new Random();
            rand.setSeed(System.currentTimeMillis());
            int value = Math.abs(rand.nextInt() % 10) + 1;
            if(value <= grow) {
                return 0.01;
            }
            if(value > grow && value <= stay) {
                return 0.0;
            }
            return -0.01;
        }
        
        private void updateHumidity() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        private void updateLight() {
            if(_time.isAfter(LocalTime.of(5, 30))) {
                if(_time.isBefore(LocalTime.of(10, 0))) {
                    // Grow slowly
                } else {
                    if(_time.isBefore(LocalTime.of(14,0))) {
                        // Grow rapidly
                        
                    } else {
                        if(_time.isBefore(LocalTime.of(16,0))) {
                            // Grow Slowy
                        } else {
                            // Lower Slowly
                        }
                    }
                }
            } else {
                // Lowe Rapidly
            }
        }

        private void sendNotifications() {
            
            _step = 4;
        }
        
        
    
    } 
   
}
