/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aalsimulator.Event;

import aalsimulator.ElderAgent;
import jade.core.Agent;
import java.time.LocalTime;
import java.util.Random;

/**
 *
 * @author andressolano
 */
public class Cooking extends Event {

    public Cooking (LocalTime start) {
       EVENT_IDENTIFICATOR = 1;
       EVENT_NAME_IDENTIFICATOR = "aal.simulation.event.cooking";
       BEGIN_TIME = start;
    }
    
    @Override
    public void inform_agents(Agent agent) {
        
    }

    @Override
    public int get_minutes_to_last() {
        Random rand = new Random();
        rand.setSeed(System.currentTimeMillis());
        int value = Math.abs(rand.nextInt() % 60) + 15;
        System.out.println("[EVENT COOKING] Time to end: " + value);
        END_TIME = BEGIN_TIME.plusMinutes(value);
        return value;
    }
    
    @Override
    public float probabilty_of_fall() {
        return 12/100;
    }
}
