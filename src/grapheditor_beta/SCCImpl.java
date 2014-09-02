package grapheditor_beta;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.MapTransformer;
import org.apache.commons.collections15.map.LazyMap;

/**
 *
 * @author John Gasparis
 */
public class SCCImpl extends JFrame implements Printable {

    private final DirectedGraph<String, Number> graph;
    private final VisualizationViewer<String, Number> visual;
    private JMenuBar bar;
    private String[] array;
    private final TransformerImpl tVertex;

    /*
     * Implementation of Strongly Connected Component Viewer
     *
     */
    public SCCImpl(List<List<Number>> vec, Number[][] ar) {
        super("Strongly Connected Component Viewer");

        createMenu();

        graph = new DirectedSparseMultigraph<>();

        FRLayout<String, Number> layout = new FRLayout<>(graph);

        visual = new VisualizationViewer<>(layout, new Dimension(400, 400));
        visual.setBackground(Color.white);

        visual.getRenderContext().setVertexLabelTransformer(MapTransformer.<String, String>getInstance(LazyMap.<String, String>decorate(new HashMap<>(), new ToStringLabeller<>())));

        visual.getRenderContext().setEdgeLabelTransformer(MapTransformer.<Number, String>getInstance(
                LazyMap.<Number, String>decorate(new HashMap<>(), new ToStringLabeller<>())));

        visual.setVertexToolTipTransformer(visual.getRenderContext().getVertexLabelTransformer());

        GraphZoomScrollPane graphZoomPanel = new GraphZoomScrollPane(visual);
        graphZoomPanel.repaint();


        add(graphZoomPanel);

        setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("images/icon.gif")));


        makeView(vec, ar);

        tVertex = new TransformerImpl();
        tVertex.setArray(array);

        visual.getRenderContext().setVertexFillPaintTransformer(tVertex);
        visual.repaint();

    }

    /*
     * Creates Menu
     *
     */
    private void createMenu() {
        bar = new JMenuBar();
        setJMenuBar(bar);

        JMenu fileMenu = new JMenu("File");

        JMenuItem printItem = new JMenu("Print");
        printItem.setMnemonic('P');
        printItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                PrinterJob printerJob = PrinterJob.getPrinterJob();
                printerJob.setPrintable((Printable) SCCImpl.this);
                if (printerJob.printDialog()) {
                    try {
                        printerJob.print();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null,"Error while printing" , "Error" , JOptionPane.ERROR_MESSAGE , new ImageIcon("images/icon.gif"));
                    }
                }
            }
        });


        JMenuItem saveItem = new JMenu("Save as...");
        saveItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();

                int option = fileChooser.showSaveDialog(SCCImpl.this);

                if (option == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    writeImage(file);
                }

            }

            private void writeImage(File file) {

                int width = visual.getWidth();
                int height = visual.getHeight();

                BufferedImage bufferedImage = new BufferedImage(width, height,
                        BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics2D = bufferedImage.createGraphics();
                visual.paint(graphics2D);
                graphics2D.dispose();

                try {
                    ImageIO.write(bufferedImage, "jpeg", file);
                } catch (Exception e) {
                     JOptionPane.showMessageDialog(null,"Error while writing image" , "Error" , JOptionPane.ERROR_MESSAGE , new ImageIcon("images/icon.gif"));

                }
            }
        });

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setMnemonic('E');
        exitItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                SCCImpl.this.dispose();
            }
        });

        fileMenu.add(saveItem);
        fileMenu.add(printItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        bar.add(fileMenu);
    }

    /*
     * Creates the appropriate graph
     *
     */
    private void makeView(List<List<Number>> vec, Number[][] ar) {

        array = new String[vec.size()];

        int i = 0;
        Iterator<List<Number>> itF = vec.iterator();

        while (itF.hasNext()) {

            Iterator<Number> itS = itF.next().iterator();

            while (itS.hasNext()) {

                if (array[i] != null) {
                    array[i] += " " + itS.next();
                }
                else {
                    array[i] = itS.next().toString();
                }
            }

            i++;
        }

        for (i = 0; i < array.length; i++) {
            graph.addVertex(array[i]);
        }


        itF = vec.iterator();
        int pos = 0;
        int edgeN = 0;
        int realPos = 0;
        int nextPos;

        while (itF.hasNext()) {

            pos++;
            Iterator<Number> itS = itF.next().iterator();

            while (itS.hasNext()) {

                Number n = itS.next();
                Iterator<List<Number>> itFF = vec.iterator();

                for (int l = 0; l < pos; l++) {
                    itFF.next();
                }

                nextPos = pos;

                while (itFF.hasNext()) {

                    Iterator<Number> itSS = itFF.next().iterator();
                    
                    while (itSS.hasNext()) {

                        Number nn = itSS.next();

                        if (ar[(Integer) n][(Integer) nn] == (Number) 1 && ar[(Integer) nn][(Integer) n] == (Number) 1) {
                            continue;
                        }
                        else if (ar[(Integer) nn][(Integer) n] == (Number) 1) {

                            try {
                                if (graph.findEdge(array[nextPos], array[realPos]).intValue() >= 0) {
                                    continue;
                                }
                            } catch (NullPointerException nExc) { }

                            graph.addEdge(edgeN, array[nextPos], array[realPos]);
                            edgeN++;
                        }
                    }

                    nextPos++;
                }

            }

            realPos++;
        }
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int index) throws PrinterException {

        if (index > 0) {
            return Printable.NO_SUCH_PAGE;
        }
        else {
            Graphics2D graphics2D = (Graphics2D) graphics;
            visual.setDoubleBuffered(false);
            graphics2D.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

            visual.paint(graphics2D);
            visual.setDoubleBuffered(true);

            return Printable.PAGE_EXISTS;
        }
    }

    private class TransformerImpl implements Transformer<String, Paint> {

        private String[] ar;

        @Override
        public Paint transform(String arg0) {
           Random r = new Random();
           float f1, f2, f3;
           int level = 0;

           for ( int i = 0; i < ar.length; i++) {
               if ( arg0.equals(ar[i]))
                   level = i;
           }

           r.setSeed(level * 101);
           f1 = (Math.abs(r.nextFloat())) % 255 + 1;
           f2 = (Math.abs(r.nextFloat())) % 255 + 1;
           f3 = (Math.abs(r.nextFloat())) % 255 + 1;
           return Color.getHSBColor(f1, f2, f3);

        }

        public void setArray(String[] array) {
            ar = array;
        }

    }
}
