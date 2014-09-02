package grapheditor_beta;

import edu.uci.ics.jung.graph.util.Pair;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Formatter;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/**
 *
 * @author John Gasparis
 */
public class DFSImpl {

    private final boolean[] visited;
    private final int[] pre;
    private final int[] post;
    private final Number[][] arrayT;
    private int clock;
    private final Number[][] array;
    private Map<Integer, Integer> nMap;
    private final int[] temp;
    private Formatter out;
    private final GraphImpl graph;


    /*
     * Implements DFS
     *
     */
    public DFSImpl(GraphEditor cGraph , RandomGraphEditor rGraph ) {

        if ( rGraph != null)
            this.graph = rGraph;
        else
            this.graph = cGraph;

        Collection<Number> c = graph.getGraph().getEdges();
        Pair<Number> p;
        Number[] n = new Number[graph.getGraph().getEdgeCount()];
        array = new Number[graph.getGraph().getVertexCount()][graph.getGraph().getVertexCount()];
        for (Number[] array1 : array) {
            for (int j = 0; j < array.length; j++) {
                array1[j] = (Number) 0;
            }
        }
        c.toArray(n);
        for (Number n1 : n) {
            p = graph.getGraph().getEndpoints(n1);
            array[p.getFirst().intValue()][p.getSecond().intValue()] = (Number) 1;
        }

        arrayT = new Number[graph.getGraph().getVertexCount()][graph.getGraph().getVertexCount()];
        for (Number[] arrayT1 : arrayT) {
            for (int j = 0; j < arrayT.length; j++) {
                arrayT1[j] = (Number) 0;
            }
        }

        for (int i = 0; i < arrayT.length; i++) {
            for (int j = 0; j < arrayT.length; j++) {
                if (array[i][j] == (Number) 1) {
                    arrayT[j][i] = (Number) 1;
                }
            }
        }

        visited = new boolean[arrayT.length];
        pre = new int[arrayT.length];
        post = new int[arrayT.length];
        clock = 1;

        dfsT();
        
        temp = new int[findMax(post) + 1];

        for (int i = 0; i < temp.length; i++) {
            temp[i] = -1;
        }
        for (int i = 0; i < post.length; i++) {
            temp[post[i]] = i;
        }
        try {
            out = new Formatter(new File("strConComp.txt"));
            dfs();
            out.close();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Error while writing the file...", "Error",  JOptionPane.ERROR_MESSAGE , new ImageIcon("images/icon.gif"));
        }

        graph.setArray(array);
        graph.transformGraph(new File("strConComp.txt"), true);        
    }

    
    private void dfs() throws IOException {
        for (int i = 0; i < visited.length; i++) {
            visited[i] = false;
        }
        for (int i = temp.length - 1; i >= 0; i--) {
            if (temp[i] != -1) {
                if (!visited[temp[i]]) {
                    out.format("%s\n", "Strongly Connected Component");
                    explore(temp[i]);
                }
            }
        }

    }

    private void dfsT(){
        for (int i = 0; i < visited.length; i++) {
            visited[i] = false;
        }
        for (int i = 0; i < arrayT.length; i++) {
            if (!visited[i]) {
                exploreT(i);
            }
        }
    }

    private void explore(int v) throws IOException {
        previsit(v);
        for (int j = 0; j < array.length; j++) {
            if (array[v][j] == (Number) 1) {
                if (!visited[j]) {
                    explore(j);
                }
            }
        }
        postvisitT(v);
    }

    private void exploreT(int v) {
        previsitT(v);
        for (int j = 0; j < arrayT.length; j++) {
            if (arrayT[v][j] == (Number) 1) {
                if (!visited[j]) {
                    exploreT(j);
                }
            }
        }
        postvisitT(v);
    }

    private void previsit(int v) throws IOException {
        out.format("%s %d\n", "Vertex :", v);
        visited[v] = true;
        pre[v] = clock++;
    }

    private void previsitT(int v){
        visited[v] = true;
        pre[v] = clock++;
    }

    private void postvisitT(int v) {
        post[v] = clock++;
    }

    private int findMax(int[] a) {
        int max = 0;
        for (int i = 0; i < a.length; i++) {
            if (a[i] > max) {
                max = a[i];
            }
        }
        return max;
    }
}
