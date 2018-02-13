/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aalsimulator.Event;

import jade.core.Agent;
import java.time.LocalTime;
import java.util.Random;

/**
 *
 * @author andressolano
 */
public class Bathroom extends Event  {

    public Bathroom (LocalTime start) {
       EVENT_IDENTIFICATOR = 1;
       EVENT_NAME_IDENTIFICATOR = "aal.simulation.event.bathroom";
       BEGIN_TIME = start;
    }
    
    @Override
    public void inform_agents(Agent agent) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int get_minutes_to_last() {
        Random rand = new Random();
        rand.setSeed(System.currentTimeMillis());
        int value = Math.abs(rand.nextInt() % 25) + 1;
        System.out.println("[EVENT BATHROOM] Time to end: " + value);
        END_TIME = BEGIN_TIME.plusMinutes(value);
        return value;
    }

    @Override
    public float probabilty_of_fall() {
        return 40/100;
    }
    
}
