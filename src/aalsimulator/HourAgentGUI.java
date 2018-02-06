/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aalsimulator;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author andressolano
 */
public class HourAgentGUI extends JFrame {
    private HourAgent _hourAgent;
    private JTextField _timeField;
    
    public HourAgentGUI(HourAgent agent){
        super(agent.getLocalName());
        _hourAgent = agent;
    }
    
    private void initGui() {
        JPanel p = new JPanel();
        p.setLayout(new GridLayout(2,1));
        p.add(new JLabel("Hora:"));
        _timeField = new JTextField(8);
        _timeField.setText("06:00");
        _timeField.setEnabled(false);
        p.add(_timeField);
        getContentPane().add(p, BorderLayout.CENTER);
    }
    
    public void showGui() {
        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int centerX = (int)screenSize.getWidth() / 2;
        int centerY = (int)screenSize.getHeight() / 2;
        setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
        super.setVisible(true);
    }
    
    public void setTime(String time) {
        _timeField.setText(time);
    }
}
