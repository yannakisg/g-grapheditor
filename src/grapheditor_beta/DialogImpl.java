package grapheditor_beta;

import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

/**
 *
 * @author John Gasparis
 */
public class DialogImpl extends JDialog {

    private final JPanel panel;
    private final JSpinner spinnerP;
    private final JSpinner spinnerN;
    private final SpinnerModel spinnerModelP;
    private final SpinnerModel spinnerModelN;
    private final JLabel lprob;
    private final JLabel lnum;

    private final JButton bOK;
    private final JButton bCancel;
    private boolean answer;
    private final boolean activateN;
    private final boolean activateProb;

   /*
    *Class which implements two necessary dialogs for RandomGraphEditor class
    *
    */

    public DialogImpl(final JFrame frame, boolean modal, boolean activateN, boolean activateProb) {
        super(frame, modal);
        setTitle("Please choose...");

        this.activateN = activateN;
        this.activateProb = activateProb;

        panel = new JPanel();
        getContentPane().add(panel);
        panel.setLayout(new GridLayout(3, 2));

        lprob = new JLabel("Probability: ");

        spinnerModelP = new SpinnerNumberModel(0.5, (double) 0, 1.0, 0.001);
        spinnerP = new JSpinner(spinnerModelP);

        panel.add(lprob);
        panel.add(spinnerP);

        lnum = new JLabel("Number: ");

        spinnerModelN = new SpinnerNumberModel(10, 0, 1000, 1);
        spinnerN = new JSpinner(spinnerModelN);

        panel.add(lnum);
        panel.add(spinnerN);

        bOK = new JButton("OK");
        bOK.setCursor(new Cursor(Cursor.HAND_CURSOR));

        bOK.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                answer = true;
                dispose();
            }
        });

        bCancel = new JButton("Cancel");
        bCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        bCancel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                answer = false;
                dispose();
            }
        });

        panel.add(bOK);
        panel.add(bCancel);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowStateListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent winEvt) {
                answer = false;
            }
        });


        if (activateN && (!activateProb)) {
            spinnerP.setEnabled(false);
        }
        if (activateProb && (!activateN)) {
            spinnerN.setEnabled(false);
        }

        setSize(300, 100);
        setResizable(false);
        setLocationRelativeTo(frame);
        setVisible(true);

        setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("images/icon.gif")));

    }

    public boolean getAnswer() {
        return answer;
    }

    public Integer getNumber() {
        return (Integer) spinnerN.getValue();
    }

    public Double getProbability() {
        return (Double) spinnerP.getValue();
    }

    public boolean getActivateN() {
        return activateN;
    }

    public boolean getActivateProb() {
        return activateProb;
    }
}
