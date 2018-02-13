/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aalsimulator.Event;

import aalsimulator.ElderAgent;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import java.time.LocalTime;
import java.util.Random;

/**
 *
 * @author andressolano
 */
public class OverSleep extends Event  {

    public OverSleep (LocalTime start) {
       EVENT_IDENTIFICATOR = 1;
       EVENT_NAME_IDENTIFICATOR = "aal.simulation.event.sleep";
       BEGIN_TIME = start;
    }
    
    @Override
    public void inform_agents(Agent agent) {
        ACLMessage request = new ACLMessage(ACLMessage.PROPAGATE);
        request.addReceiver(((ElderAgent)agent).getHandlerAgent());
        request.setContent("aal.simulation.event.sleep.begin");
        request.setConversationId("aal.simulation.event");
        agent.send(request);
        System.out.println("[OVERSLEEP EVENT] Sendig event begin propagation to " + ((ElderAgent)agent).getHandlerAgent());
    }
    
    public void inform_agents_end(Agent agent) {
        ACLMessage request = new ACLMessage(ACLMessage.PROPAGATE);
        request.addReceiver(((ElderAgent)agent).getHandlerAgent());
        request.setContent("aal.simulation.event.sleep.end");
        request.setConversationId("aal.simulation.event");
        agent.send(request);
        System.out.println("[OVERSLEEP EVENT] Sendig event end propagation to " + ((ElderAgent)agent).getHandlerAgent());
    }
    
    @Override
    public int get_minutes_to_last() {
        Random rand = new Random();
        rand.setSeed(System.currentTimeMillis());
        int value = Math.abs(rand.nextInt() % 240) + 480;
        System.out.println("[EVENT OVER SLEEP] Time to end: " + value);
        END_TIME = BEGIN_TIME.plusMinutes(value);
        return value;
    }

    @Override
    public float probabilty_of_fall() {
        return 15/100;
    }
    
}
