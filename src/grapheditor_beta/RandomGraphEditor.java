package grapheditor_beta;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout2;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.algorithms.layout.util.Relaxer;
import edu.uci.ics.jung.algorithms.layout.util.VisRunner;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.ObservableGraph;
import edu.uci.ics.jung.graph.util.Graphs;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.EditingModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.layout.LayoutTransition;
import edu.uci.ics.jung.visualization.util.Animator;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
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
public final class RandomGraphEditor implements Printable, GraphImpl {

    private Graph<Number, Number> graph = null;
    private VisualizationViewer<Number, Number> visual = null;
    private AbstractLayout<Number, Number> layout = null;
    private GraphZoomScrollPane graphZoomPanel;
    private Timer timer;
    private boolean done;
    private boolean isDone;
    private VertexFactory vertexFactory;
    private EdgeFactory edgeFactory;
    private TransformerImpl tVertex;
    private JPanel controls;
    private Double prob;
    private Integer n;
    private GraphGUI gui;
    private Number[][] array;
    private  JFrame frame;
    private final boolean withGUI;
    private static RandomGraphEditor editor = null;

    public static void create(JFrame frame, boolean wGUI, DialogImpl d, Double p, Integer N) {
        editor = new RandomGraphEditor(frame, wGUI, d, p, N);
    }

    /*
     * Implements Random Graph Editor
     *
     */
    private RandomGraphEditor(JFrame frame, boolean wGUI, DialogImpl d, Double p, Integer N) {

        withGUI = wGUI;
        isDone = false;

        if (withGUI) {
            DialogImpl dialog = new DialogImpl(frame, true, true, true);
            if (!dialog.getAnswer()) {
                frame.setVisible(true);
                return;
            }

            prob = dialog.getProbability();
            n = dialog.getNumber();
        } else {
            if (d.getActivateN() && (!d.getActivateProb())) {
                n = d.getNumber();
                prob = p;
            }

            if (d.getActivateProb() && (!d.getActivateN())) {
                prob = d.getProbability();
                n = N;
            }
        }

        this.frame = frame;

        Graph<Number, Number> ig = Graphs.<Number, Number>synchronizedDirectedGraph(new DirectedSparseGraph<Number, Number>());

        ObservableGraph<Number, Number> og = new ObservableGraph<Number, Number>(ig);

        this.graph = og;

        vertexFactory = new VertexFactory();
        edgeFactory = new EdgeFactory();
        tVertex = new TransformerImpl();

        createLayoutViewer();
        createZoomPanel();

        controls = new JPanel();
        controls.setLayout(new BorderLayout());
        controls.setBackground(java.awt.Color.lightGray);
        controls.setFont(new Font("Serif", Font.PLAIN, 12));

        createGUI(frame, withGUI);

        timer = new Timer();

        start();
    }

    /*
     * Creates layout and viewer
     *
     */
    @Override
    public void createLayoutViewer() {
        //create a graphdraw
        layout = new FRLayout2<>(graph);
        Relaxer relaxer = new VisRunner((IterativeContext) layout);
        relaxer.stop();
        relaxer.prerelax();

        Layout<Number, Number> staticLayout = new StaticLayout<>(graph, layout);
        visual = new VisualizationViewer<>(staticLayout, new Dimension(600, 600));

        //visual.getModel().getRelaxer().setSleepTime(500);
        final EditingModalGraphMouse<Number, Number> graphMouse
                = new EditingModalGraphMouse<>(visual.getRenderContext(), vertexFactory, edgeFactory);
        visual.setGraphMouse(graphMouse);
        visual.addKeyListener(graphMouse.getModeKeyListener());
        graphMouse.setMode(ModalGraphMouse.Mode.PICKING);

        visual.getRenderContext().setVertexLabelTransformer(MapTransformer.<Number, String>getInstance(
                LazyMap.<Number, String>decorate(new HashMap<>(), new ToStringLabeller<>())));
        visual.getRenderContext().setEdgeLabelTransformer(MapTransformer.<Number, String>getInstance(
                LazyMap.<Number, String>decorate(new HashMap<>(), new ToStringLabeller<>())));
        visual.setVertexToolTipTransformer(visual.getRenderContext().getVertexLabelTransformer());

        visual.addComponentListener(new ComponentAdapter() {

            /**
             * @see
             * java.awt.event.ComponentAdapter#componentResized(java.awt.event.ComponentEvent)
             */
            @Override
            public void componentResized(ComponentEvent arg0) {
                super.componentResized(arg0);
                layout.setSize(arg0.getComponent().getSize());
            }
        });

    }

    /*
     * Creates gui
     *
     */
    private void createGUI(JFrame frame, boolean withGUI) {

        gui = new GraphGUI(frame, this);
        gui.setVisible(withGUI);
        gui.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        gui.setResizable(false);
        gui.setSize(800, 600);
        gui.getFileMenu().setEnabled(false);

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screen.width / 2) - (gui.getWidth() / 2);
        int y = (screen.height / 2) - (gui.getHeight() / 2);
        gui.setLocation(x, y);

    }

    private void start() {
        timer.schedule(new RemindTask(), 1000, 1000); //subsequent rate
        visual.repaint();
    }

    /*
     * Adding and connecting vertices with each other
     *
     */
    private void process() {

        Integer edge;
        visual.getRenderContext().getPickedVertexState().clear();
        visual.getRenderContext().getPickedEdgeState().clear();

        try {
            if (graph.getVertexCount() < n) {

                Integer v1 = graph.getVertexCount();

                graph.addVertex(v1);
                visual.getRenderContext().getPickedVertexState().pick(v1, true);

                Random r = new Random();

                for (int i = 0; i <= v1; i++) {
                    r.setSeed(i);

                    for (int j = 0; j <= i; j++) {
                        if (Math.abs(r.nextInt()) % 1000 < Math.floor(prob * 1000)) {

                            edge = graph.getEdgeCount();
                            visual.getRenderContext().getPickedEdgeState().pick(edge, true);
                            graph.addEdge(graph.getEdgeCount(), i, j);

                        }
                    }

                    edge = graph.getEdgeCount();
                    visual.getRenderContext().getPickedEdgeState().pick(edge, true);

                    for (int k = 0; k <= i; k++) {
                        if (Math.abs(r.nextInt()) % 1000 < Math.floor(prob * 1000)) {

                            edge = graph.getEdgeCount();
                            visual.getRenderContext().getPickedEdgeState().pick(edge, true);

                            graph.addEdge(graph.getEdgeCount(), k, i);
                        }
                    }

                    edge = graph.getEdgeCount();
                    visual.getRenderContext().getPickedEdgeState().pick(edge, true);
                }

                layout.initialize();

                Relaxer relaxer = new VisRunner((IterativeContext) layout);
                relaxer.stop();
                relaxer.prerelax();

                StaticLayout<Number, Number> staticLayout
                        = new StaticLayout<>(graph, layout);

                LayoutTransition<Number, Number> lt;
                lt = new LayoutTransition<>(visual, visual.getGraphLayout(),
                        staticLayout);

                Animator animator = new Animator(lt);
                animator.start();

                visual.repaint();

            } else {
                done = true;
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error while creating the random graph", "Error", JOptionPane.ERROR_MESSAGE, new ImageIcon("images/icon.gif"));
        }
    }

    /*
     * Prints graph
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
    /*
     *Creates the GraphZoomScrollPane
     *
     */

    @Override
    public void createZoomPanel() {
        graphZoomPanel = new GraphZoomScrollPane(visual);
        visual.repaint();
    }

    @Override
    public void clearZoomPanel() {
        graphZoomPanel.removeAll();
        graphZoomPanel.repaint();
    }

    /*
     * Writes graph into a file(jpg)
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
            JOptionPane.showMessageDialog(null, "Error while writing image", "Error", JOptionPane.ERROR_MESSAGE, new ImageIcon("images/icon.gif"));
        }
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
                    } catch (NumberFormatException nfe) {
                    }

                }

            } catch (FileNotFoundException ex) {
                JOptionPane.showMessageDialog(null, "Error while opening the file...", "Error", JOptionPane.ERROR_MESSAGE, new ImageIcon("images/icon.gif"));
            }

            tVertex.setNumbers(vec);
        }

        if (withGUI) {
            visual.getRenderContext().setVertexFillPaintTransformer(tVertex);
            visual.repaint();
            SCCImpl scc = new SCCImpl(vec, array);
            scc.setSize(800, 600);
            scc.setVisible(true);
            scc.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        }
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

    @Override
    public Component getPanel() {
        return controls;
    }

    @Override
    public void reset() {
        vertexFactory.reset();
        edgeFactory.reset();
    }

    @Override
    public void setArray(Number[][] ar) {
        array = new Number[ar.length][ar.length];
        array = ar;
    }

    @Override
    public GraphZoomScrollPane getGraphZoomScrollPane() {
        return graphZoomPanel;
    }

    @Override
    public Graph<Number, Number> getGraph() {
        return graph;
    }

    public int getSCC() {
        return tVertex.getSize();
    }

    public boolean isDone() {
        return isDone;
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
                    Number n = itS.next();
                    if (n.intValue() == arg0.intValue()) {
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

        public int getSize() {
            return vNumbers.size();
        }
    }

    class RemindTask extends TimerTask {

        @Override
        public void run() {
            process();
            if (done) {
                cancel();

                if (withGUI) {
                    JOptionPane.showMessageDialog(frame, "The graph has been created", "Success", JOptionPane.INFORMATION_MESSAGE, new ImageIcon("images/icon.gif"));
                    gui.getFileMenu().setEnabled(true);
                } else {
                    DFSImpl dfs = new DFSImpl(null, RandomGraphEditor.this);
                    isDone = true;
                    gui.dispose();
                }
            }
        }
    }

}
