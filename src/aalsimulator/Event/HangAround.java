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
public class HangAround extends Event  {

    public HangAround (LocalTime start) {
       EVENT_IDENTIFICATOR = 1;
       EVENT_NAME_IDENTIFICATOR = "aal.simulation.event.hang";
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
        int value = Math.abs(rand.nextInt() % 65) + 15;
        System.out.println("[EVENT HANG] Time to end: " + value);
        END_TIME = BEGIN_TIME.plusMinutes(value);
        return value;
    }

    @Override
    public float probabilty_of_fall() {
        return 5/100;
    }
    
}
