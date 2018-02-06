/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package aalsimulator;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import java.time.*;

/**
 *
 * @author Informática
 */
public class HourAgent extends Agent {
    
    private HourAgentGUI _hour;
    private LocalTime _time;
    
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
        addBehaviour(new HourTickerBehaviour(this, 6000));
    }
    
        public class HourTickerBehaviour extends TickerBehaviour {

        public HourTickerBehaviour(Agent a, long period) {
            super(a, period);
        }

        @Override
        protected void onTick() {
            _time = _time.plusMinutes(1);
            _hour.setTime(_time.toString());
        }
    
    }
    

}
