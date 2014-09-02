package grapheditor_beta;

import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

/**
 *
 * @author John Gasparis
 */
public class MainFrame extends JFrame {

    private final JButton graphCreator;
    private final JButton randomGraph;

    /*
     * Creates the main frame of application
     *
     */
    public MainFrame() {
        super("Graph Creator - Beta Edition");
        GridLayout grid = new GridLayout(4, 1);
        setLayout(grid);

        JMenuBar bar = new JMenuBar();
        setJMenuBar(bar);

        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");

        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        fileMenu.add(exitItem);

        bar.add(fileMenu);

        graphCreator = new JButton("Graph Editor");
        graphCreator.setCursor(new Cursor(Cursor.HAND_CURSOR));

        graphCreator.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                MainFrame.this.setVisible(false);
                GraphEditor.create(MainFrame.this);
            }
        });

        add(graphCreator);
        randomGraph = new JButton("Random Graph Editor");
        randomGraph.setCursor(new Cursor(Cursor.HAND_CURSOR));

        randomGraph.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                MainFrame.this.setVisible(false);
                RandomGraphEditor.create(MainFrame.this , true , null , null , null );
                
            }
        });

        add(randomGraph);


        setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("images/icon.gif")));

    }
}
