package grapheditor_beta;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.annotations.AnnotationControls;
import edu.uci.ics.jung.visualization.control.EditingModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.MapTransformer;
import org.apache.commons.collections15.map.LazyMap;

/**
 *
 * @author John Gasparis
 */
public final class GraphEditor implements Printable, GraphImpl {

    private final Graph<Number, Number> graph;
    private AbstractLayout<Number, Number> layout;
    private VisualizationViewer<Number, Number> visual;
    private GraphZoomScrollPane graphZoomPanel;
    private JPanel controls;
    private final VertexFactory vertexFactory;
    private final EdgeFactory edgeFactory;
    private final TransformerImpl tVertex;
    private Number[][] array;
    private static GraphEditor editor = null;
    
    public static void create(JFrame frame) {
        editor = new GraphEditor(frame);
    }

    /*
     *Class which implements Graph Editor
     *
     */
    private GraphEditor(JFrame frame) {

        graph = new DirectedSparseGraph<>();
        vertexFactory = new VertexFactory();
        edgeFactory = new EdgeFactory();
        tVertex = new TransformerImpl();

        createLayoutViewer();
        createZoomPanel();
        createGUI(frame);

    }

    /*
     *Creates the layout and the viewer
     *
     */
    @Override
    public void createLayoutViewer() {

        layout = new StaticLayout<>(graph, new Dimension(600, 600));

        visual = new VisualizationViewer<>(layout);

        visual.setBackground(Color.white);

        visual.getRenderContext().setVertexLabelTransformer(MapTransformer.<Number, String>getInstance(
                LazyMap.<Number, String>decorate(new HashMap<>(), new ToStringLabeller<>())));

        visual.getRenderContext().setEdgeLabelTransformer(MapTransformer.<Number, String>getInstance(
                LazyMap.<Number, String>decorate(new HashMap<>(), new ToStringLabeller<>())));

        visual.setVertexToolTipTransformer(visual.getRenderContext().getVertexLabelTransformer());


        final EditingModalGraphMouse<Number, Number> graphMouse =
                new EditingModalGraphMouse<>(visual.getRenderContext(), vertexFactory, edgeFactory);

        visual.setGraphMouse(graphMouse);
        visual.addKeyListener(graphMouse.getModeKeyListener());
        graphMouse.setMode(ModalGraphMouse.Mode.EDITING);

        AnnotationControls<Number, Number> annotationControls =
                new AnnotationControls<>(graphMouse.getAnnotatingPlugin());
        controls = new JPanel();

        JComboBox modeBox = graphMouse.getModeComboBox();
        controls.add(modeBox);
        controls.add(annotationControls.getAnnotationsToolBar());

    }

    /*
     *Creates the GraphZoomScrollPane
     *
     */
    @Override
    public void createZoomPanel() {
        graphZoomPanel = new GraphZoomScrollPane(visual);
        graphZoomPanel.repaint();
    }

    /*
     *Creates the appropriate gui
     *
     */
    private void createGUI(JFrame frame) {

        GraphGUI gui = new GraphGUI(frame, this);
        gui.setVisible(true);
        gui.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        gui.setResizable(false);
        gui.setSize(800, 600);

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screen.width / 2) - (gui.getWidth() / 2);
        int y = (screen.height / 2) - (gui.getHeight() / 2);
        gui.setLocation(x, y);
    }

    /*
     *Clears the zoomPanel
     *
     */
    @Override
    public void clearZoomPanel() {
        graphZoomPanel.removeAll();
        graphZoomPanel.repaint();
    }

     /*
     *Writes the graph into an image file(jpg)
     *
     */
    @Override
    public void writeImage(File file) {
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
            JOptionPane.showMessageDialog(null, "Error while saving image", "Error",  JOptionPane.ERROR_MESSAGE , new ImageIcon("images/icon.gif"));
        }
    }

     /*
     *Prints the graph
     *
     */
    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int index) throws PrinterException {

        if (index > 0) {
            return Printable.NO_SUCH_PAGE;
        } else {
            Graphics2D graphics2D = (Graphics2D) graphics;
            visual.setDoubleBuffered(false);
            graphics2D.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

            visual.paint(graphics2D);
            visual.setDoubleBuffered(true);

            return Printable.PAGE_EXISTS;
        }
    }

    @Override
    public void reset() {
        vertexFactory.reset();
        edgeFactory.reset();
    }

    /*
     *Makes nodes red
     *
     */
    @Override
    public void trasnformGraphDefault() {
        visual.getRenderContext().setVertexFillPaintTransformer(new Transformer<Number, Paint>() {

            @Override
            public Paint transform(Number arg0) {
                return Color.red;
            }
        });

        visual.repaint();
    }

    /*
     *Paints the node according to which strongly connected components belongs
     *
     */
    @Override
    public void transformGraph(File f, boolean type) {

        Scanner in;
        List<List<Number>> vec = new ArrayList<>();
        int safe;
        String temp;

        if (type) {
            try {

                in = new Scanner(f);
                safe = -1;
                while (in.hasNext()) {

                    temp = in.next();

                    if (temp.equals("Strongly")) {
                        vec.add(new ArrayList<>());
                        safe++;
                    }

                    try {
                        int k = Integer.parseInt(temp);
                        vec.get(safe).add((Number) k);
                    } 
                    catch (NumberFormatException nfe) { }
                }
            }

            catch (FileNotFoundException ex) {
                JOptionPane.showMessageDialog(null, "Error while opening the file...", "Error",  JOptionPane.ERROR_MESSAGE , new ImageIcon("images/icon.gif"));
            }

            tVertex.setNumbers(vec);
        }

        visual.getRenderContext().setVertexFillPaintTransformer(tVertex);
        visual.repaint();

        SCCImpl scc = new SCCImpl(vec, array);
        scc.setSize(800, 600);
        scc.setVisible(true);
        scc.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    @Override
    public GraphZoomScrollPane getGraphZoomScrollPane() {
        return graphZoomPanel;
    }

    @Override
    public JPanel getPanel() {
        return controls;
    }

    @Override
    public Graph<Number, Number> getGraph() {
        return graph;
    }

    public VisualizationViewer<Number, Number> getViewer() {
        return visual;
    }


    @Override
    public void setArray(Number[][] ar) {
        array = new Number[ar.length][ar.length];

        for (int i = 0; i < ar.length; i++) {
            System.arraycopy(ar[i], 0, array[i], 0, ar.length);
        }
    }
    /*
     *Transform implementation
     *
     */
    private class TransformerImpl implements Transformer<Number, Paint> {

        private List<List<Number>> vNumbers;

        @Override
        public Paint transform(Number arg0) {

            Random r = new Random();
            float f1, f2, f3;
            Iterator<List<Number>> itF = vNumbers.iterator();
            int level = -1;
            boolean found;

            while (itF.hasNext()) {
                found = false;
                level++;
                Iterator<Number> itS = itF.next().iterator();

                while (itS.hasNext()) {
                    if (itS.next() == arg0) {
                        found = true;
                    }
                }

                if (found) {
                    break;
                }
            }

            r.setSeed(level * 101);
            f1 = (Math.abs(r.nextFloat())) % 255 + 1;
            f2 = (Math.abs(r.nextFloat())) % 255 + 1;
            f3 = (Math.abs(r.nextFloat())) % 255 + 1;
            return Color.getHSBColor(f1, f2, f3);
        }

        public void setNumbers(List<List<Number>> v) {
            vNumbers = v;
        }
    }

}
