package agent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class specialistGui extends JFrame {
    //Farming agent class
    private specialistAgent myAgent;

    //Creating setter and getter for passing parameters.

    //GUI design preferences
    private JTextArea log;

    specialistGui(specialistAgent a) {
        super(a.getLocalName() + " Monitoring");
        myAgent = a;

        //log area create
        log = new JTextArea(10,50);
        log.setEditable(false);
        getContentPane().add(log, BorderLayout.CENTER);
        log.setMargin(new Insets(5,5,100,100));
        JScrollPane logScrollPane = new JScrollPane(log);
        getContentPane().add(logScrollPane, BorderLayout.CENTER);

        // Make the agent terminate when the user closes
        // the GUI using the button on the upper right corner
        addWindowListener(new	WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                myAgent.doDelete();
            }
        } );
        setResizable(false);
    }

    public void show() {
        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int centerX = (int)screenSize.getWidth() / 2;
        int centerY = (int)screenSize.getHeight() / 2;
        setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
        super.show();
    }

    public void displayUI(String displayUI) {
        log.append(displayUI);
        log.setCaretPosition(log.getDocument().getLength());
    }
}
