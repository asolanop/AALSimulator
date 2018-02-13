/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package aalsimulator;

import aalsimulator.Event.*;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Random;

 /*enum EVENTS {
        COOK(1), TV(2), HANG(3), BATH(4), LEAVE(5), 
        FULLSLEEP(6), SHORTSLEEP(6), OVERSLEEP(6), FALL(6);
        
        private int event_id;
        EVENTS(int id){ event_id = id;}
        public int event(){ return event_id; }
 }*/

enum Events {
        COOK, TV, HANG, BATH, LEAVE, 
        FULLSLEEP, SHORTSLEEP, OVERSLEEP, FALL;
}

/**
 *
 * @author Informática
 */
public class ElderAgent extends Agent {
    protected ArrayList<Event> _eventQueue;
    protected ArrayList<Event> _recentEvent;
    protected Event _currentEvent;
    protected LocalTime _time;
    protected String _movement;
    protected int _currentMovementTime; //Amount of minute from the same time.
    protected AID _hourAgent;
    protected AID _envAgent;
    protected AID _eventHandler;
    
    @Override
    public void setup() {
        _eventQueue = new ArrayList();
        getEnviormentalAgents();
        addBehaviour(new GetNextEvent());
    }
    
    public AID getHandlerAgent() {
        return _eventHandler;
    }
    
    private void getEnviormentalAgents() {
        DFAgentDescription timeTemplate = new DFAgentDescription();
        DFAgentDescription envTemplate = new DFAgentDescription();
        DFAgentDescription eventHandlerTemplate = new DFAgentDescription();
        ServiceDescription sdTime = new ServiceDescription();
        ServiceDescription sdEnv = new ServiceDescription();
        ServiceDescription sdHandler = new ServiceDescription();
        sdTime.setType("aal.simulation.time");
        sdEnv.setType("aal.simulation.enviorment");
        sdHandler.setType("aal.simulation.handler");
        timeTemplate.addServices(sdTime);
        envTemplate.addServices(sdEnv);
        eventHandlerTemplate.addServices(sdHandler);
        try {
            DFAgentDescription[] result = DFService.search(this, timeTemplate); 
            if(result.length != 0) {
              _hourAgent =  result[0].getName();
            }
            result = DFService.search(this, envTemplate); 
            if(result.length != 0) {
              System.out.println("[ELDER AGENT]: Found enviorment agent: " + result[0].getName().getLocalName());
              _envAgent =  result[0].getName();
            }
            result = DFService.search(this, eventHandlerTemplate); 
            if(result.length != 0) {
              System.out.println("[ELDER AGENT]: Found event handler agent: " + result[0].getName().getLocalName());
              _eventHandler =  result[0].getName();
            }
        }
        catch (FIPAException fe) {
                fe.printStackTrace();
        }
    }
    
    public class GetNextEvent extends Behaviour {
        private int _step = 0;
        private MessageTemplate mt;
        
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
                    getEvent();
                    break;
                case 3:
                    informAgents();
                    break;
                case 4:
                    waitForEnd();
                    break;
            }
        }

        @Override
        public boolean done() {
            return _step > 4;
        }
        
        private void askForTime() {
            ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
            request.addReceiver(_hourAgent);
            request.setContent("aal.simulation.time.get");
            request.setConversationId("aal.simulation.time");
            request.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
            myAgent.send(request);
            System.out.println("[ELDER AGENT] Sendig time request to " + _hourAgent.getLocalName() );
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
        
        private void getEvent() {
            Events event = getNextEventID();
            if(null != event) {
                switch(event) {
                    case COOK:
                        _currentEvent = new Cooking(_time);
                        break;
                    case TV:
                        _currentEvent = new WatchTV(_time);
                        break;
                    case HANG:
                        _currentEvent = new HangAround(_time);
                        break;
                    case BATH:
                        _currentEvent = new Bathroom(_time);
                        break;
                    case LEAVE:
                        _currentEvent = new GoOut(_time);
                        break;
                    case FULLSLEEP:
                        _currentEvent = new FullSleep(_time);
                        break;
                    case SHORTSLEEP:
                        _currentEvent = new ShortSleep(_time);
                        break;
                    case OVERSLEEP:
                        _currentEvent = new OverSleep(_time);
                        break;
                    case FALL:
                        _currentEvent = new Fall(_time);
                        break;
                    default:
                        break;
                }
            } else {
                _currentEvent = _eventQueue.get(0);
            }
            System.out.println("[ELDER AGENT] Event: " + _currentEvent.get_event_name());
            _currentEvent.get_minutes_to_last();
            _step = 3;
        }
        
        private Events getNextEventID() {
            if (_eventQueue.isEmpty()) {
                Events[] possibleEvents = getPossibleEvents();
                Random rand = new Random();
                rand.setSeed(System.currentTimeMillis());
                int value = Math.abs(rand.nextInt() % 26) + 1;
                if(value <= 10) {
                    return possibleEvents[0];
                } else {
                   if (value > 10 && value <= 17) {
                    return possibleEvents[1];
                    } else {
                        if (value > 18 && value <= 22) {
                            return possibleEvents[2];
                        } else {
                            if (value > 23 && value <= 25) {
                                return possibleEvents[3];
                            } else {
                                return possibleEvents[4];
                            }
                        }
                    }
                }
            } else {
               return null; 
            }
        }
        
        private Events[] getPossibleEvents () {
            Events[] possibleEvents = new Events[5];
            if(_time.isBefore(LocalTime.of(12, 0))){
                if(_time.isBefore(LocalTime.of(6, 0))) {
                    possibleEvents[0] = Events.SHORTSLEEP;
                    possibleEvents[1] = Events.BATH;
                    possibleEvents[2] = Events.FULLSLEEP;
                    possibleEvents[3] = Events.FALL;
                    possibleEvents[4] = Events.HANG;
                } else {
                    if(_time.isBefore(LocalTime.of(10, 0))) {
                        possibleEvents[0] = Events.COOK;
                        possibleEvents[1] = Events.HANG;
                        possibleEvents[2] = Events.BATH ;
                        possibleEvents[3] = Events.TV;
                        possibleEvents[4] = Events.FALL;
                    } else {
                        possibleEvents[0] = Events.TV;
                        possibleEvents[1] = Events.LEAVE;
                        possibleEvents[2] = Events.COOK;
                        possibleEvents[3] = Events.BATH;
                        possibleEvents[4] = Events.FALL;
                    }
                }
            } else {
                if(_time.isBefore(LocalTime.of(14, 0))) {
                    possibleEvents[0] = Events.TV;
                    possibleEvents[1] = Events.LEAVE;
                    possibleEvents[2] = Events.COOK;
                    possibleEvents[3] = Events.BATH;
                    possibleEvents[4] = Events.FALL;
                } else {
                    if(_time.isBefore(LocalTime.of(16, 0))) {
                        possibleEvents[0] = Events.COOK;
                        possibleEvents[1] = Events.TV;
                        possibleEvents[2] = Events.BATH;
                        possibleEvents[3] = Events.LEAVE;
                        possibleEvents[4] = Events.HANG;
                    } else {
                        if(_time.isBefore(LocalTime.of(22, 0))) {
                            possibleEvents[0] = Events.FULLSLEEP;
                            possibleEvents[1] = Events.SHORTSLEEP;
                            possibleEvents[2] = Events.BATH;
                            possibleEvents[3] = Events.OVERSLEEP;
                            possibleEvents[4] = Events.FALL;
                        } else {
                            possibleEvents[0] = Events.SHORTSLEEP;
                            possibleEvents[1] = Events.BATH;
                            possibleEvents[2] = Events.FULLSLEEP;
                            possibleEvents[3] = Events.FALL;
                            possibleEvents[4] = Events.HANG;
                        }
                    }
                }
            }
            return possibleEvents;
        }
        
        private void informAgents() {
            _step = 4;
            System.out.println("[ELDER AGENT] Informing other agents of event");
            ACLMessage request = new ACLMessage(ACLMessage.INFORM);
            request.addReceiver(_hourAgent);
            request.setContent(_currentEvent.get_end_time().toString());
            request.setConversationId("aal.simulation.event.endtime");
            request.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
            myAgent.send(request);
            System.out.println("[ELDER AGENT] Sendig time to finish to " + _hourAgent.getLocalName() );
            
        }
        
        private void waitForEnd () {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                ACLMessage msg = myAgent.receive(mt);
                if (msg != null) {
                    System.out.println("[ELDER AGENT] Recieved end event notices from " + msg.getSender().getLocalName());
                    // CFP Message received. Process it
                    if("aal.simulation.event.end".equals(msg.getContent())){
                        myAgent.addBehaviour(new EndEvent());
                        _step = 5;
                    }
                } else {
                    System.out.println("[ELDER AGENT] Waiting to recieve end event notice for " + _currentEvent.get_event_name());
                    block();
                }
        }
    
    }
    
    public class EndEvent extends OneShotBehaviour {

        @Override
        public void action() {
            //Pop queue, add to recent events the last event.
            _currentEvent = null;
            myAgent.addBehaviour(new GetNextEvent());
        }

    }

}
