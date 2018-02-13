/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aalsimulator.Event;

import jade.core.Agent;
import java.time.LocalTime;

/**
 *
 * @author andressolano
 */
public abstract class Event {
    protected int EVENT_IDENTIFICATOR;
    protected String  EVENT_NAME_IDENTIFICATOR;
    protected LocalTime BEGIN_TIME;
    protected LocalTime END_TIME;
    
    public int get_event_identificator() {
        return EVENT_IDENTIFICATOR;
    }
    
    public String get_event_name() {
        return EVENT_NAME_IDENTIFICATOR;
    }
    
    public LocalTime get_end_time() {
        return END_TIME;
    }
    
    abstract public void inform_agents(Agent agent);
    
    abstract public int get_minutes_to_last();
    
    abstract public float probabilty_of_fall();
}
