/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aalsimulator.Event;

import aalsimulator.ElderAgent;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.time.LocalTime;
import java.util.Random;

/**
 *
 * @author andressolano
 */
public class Fall extends Event {

    public Fall (LocalTime start) {
       EVENT_IDENTIFICATOR = 1;
       EVENT_NAME_IDENTIFICATOR = "aal.simulation.event.fall";
       BEGIN_TIME = start;
    }
    
    @Override
    public void inform_agents(Agent agent) {
        ACLMessage request = new ACLMessage(ACLMessage.PROPAGATE);
        request.addReceiver(((ElderAgent)agent).getHandlerAgent());
        request.setContent("aal.simulation.event.fall");
        request.setConversationId("aal.simulation.event");
        agent.send(request);
        System.out.println("[FALL EVENT] Sendig event propagation to " + ((ElderAgent)agent).getHandlerAgent());
    }

    @Override
    public int get_minutes_to_last() {
        Random rand = new Random();
        rand.setSeed(System.currentTimeMillis());
        int value = Math.abs(rand.nextInt() % 155) + 5;
        System.out.println("[EVENT FALL] Time to end: " + value);
        END_TIME = BEGIN_TIME.plusMinutes(value);
        return value;
    }

    @Override
    public float probabilty_of_fall() {
        return 0/100;
    }
    
}
