package grapheditor_beta;

import org.apache.commons.collections15.Factory;

/**
 *
 * @author John Gasparis
 */
public class EdgeFactory implements Factory<Number>{
    private int i = 0;
    
    @Override
    public Number create() {
        return i++;
    }
    
    public void reset() {
        i = 0;
    }
}
