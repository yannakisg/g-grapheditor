package grapheditor_beta;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 *
 * @author John Gasparis
 */
public class GraphGUI extends JFrame {

    private GraphImpl graph = null;
    private JFrame frame;
    private JMenuBar bar;
    private JMenu fileMenu;

    /*
     *Creates the gui according to which class is passed in the constructor
     *
     */
    public GraphGUI(final JFrame frame, GraphImpl graph) {
        super();

        this.frame = frame;

        if (graph instanceof GraphEditor) {
            this.setTitle("Graph Editor");
            this.graph = (GraphEditor) graph;
        } else {
            this.setTitle("Random Graph Editor");
            this.graph = (RandomGraphEditor) graph;
        }

        createMenu();

        add(graph.getGraphZoomScrollPane());
        add(graph.getPanel(), BorderLayout.SOUTH);

        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent winEvt) {
                frame.setVisible(true);

            }
        });

         setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("images/icon.gif")));
         
    }
     /*
     *Creates components which are related with menu
     *
     */
    private void createMenu() {
        bar = new JMenuBar();
        setJMenuBar(bar);

        fileMenu = new JMenu("File");

        final JMenuItem findItem = new JMenuItem("Find");

        JMenuItem newItem = new JMenuItem("New");
        newItem.setMnemonic('N');
        newItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                Collection<Number> cEdges = graph.getGraph().getEdges();
                Collection<Number> cVertices = graph.getGraph().getVertices();

                Iterator<Number> iEdges = cEdges.iterator();
                Iterator<Number> iVertices = cVertices.iterator();

                Number[] num = new Number[cEdges.size()];

                int i = 0;

                while (iEdges.hasNext()) {
                    num[i] = iEdges.next();
                    i++;
                }

                for (int j = 0; j < num.length; j++) {
                    graph.getGraph().removeEdge(num[j]);
                }

                num = new Number[cVertices.size()];
                i = 0;

                while (iVertices.hasNext()) {
                    num[i] = iVertices.next();
                    i++;
                }

                for (Number num1 : num) {
                    graph.getGraph().removeVertex(num1);
                }

                graph.reset();
                graph.getGraphZoomScrollPane().repaint();
                graph.trasnformGraphDefault();

                if (graph instanceof RandomGraphEditor) {
                    GraphGUI.this.setVisible(false);
                    RandomGraphEditor.create(frame, true, null, null, null);
                    GraphGUI.this.dispose();
                }
            }
        });

        fileMenu.add(newItem);

        findItem.setMnemonic('F');

        findItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                SwingUtilities.invokeLater(new Runnable() {

                    DFSImpl dfs;

                    @Override
                    public void run() {
                        if (graph instanceof GraphEditor) {
                            dfs = new DFSImpl((GraphEditor) graph, null);
                        } else {
                            dfs = new DFSImpl(null, (RandomGraphEditor) graph);
                        }
                    }
                });
            }
        });

        fileMenu.add(findItem);
        fileMenu.addSeparator();

        JMenuItem saveItem = new JMenuItem("Save As...");
        saveItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                JFileChooser fileChooser = new JFileChooser();

                int option = fileChooser.showSaveDialog(graph.getPanel());

                if (option == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    graph.writeImage(file);
                }
            }
        });


        fileMenu.add(saveItem);
        fileMenu.addSeparator();

        JMenuItem printItem = new JMenuItem("Print...");
        printItem.setMnemonic('P');
        printItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                PrinterJob printerJob = PrinterJob.getPrinterJob();
                printerJob.setPrintable((Printable) graph);

                if (printerJob.printDialog()) {
                    try {
                        printerJob.print();
                    } catch (Exception ex) {
                       JOptionPane.showMessageDialog(null, "Error while printing" , "Error" , JOptionPane.ERROR_MESSAGE , new ImageIcon("images/icon.gif"));
                    }
                }
            }
        });

        fileMenu.add(printItem);
        fileMenu.addSeparator();

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setMnemonic('E');
        exitItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                GraphGUI.this.dispose();
                frame.setVisible(true);
            }
        });

        fileMenu.add(exitItem);

        bar.add(fileMenu);

    }

    public JMenu getFileMenu() {
        return fileMenu;
    }
}
