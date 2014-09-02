package grapheditor_beta;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import java.awt.Component;
import java.io.File;

/**
 *
 * @author John Gasparis
 */
public interface GraphImpl {

    public GraphZoomScrollPane getGraphZoomScrollPane();

    public Graph<Number, Number> getGraph();

    public void createLayoutViewer();

    public void createZoomPanel();

    public void clearZoomPanel();

    public Component getPanel();

    public void setArray(Number[][] ar);

    public void reset();

    public void trasnformGraphDefault();

    public void writeImage(File file);

    public void transformGraph(File f, boolean type);
}