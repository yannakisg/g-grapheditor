package grapheditor_beta;

import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JFrame;

/**
 *
 * @author John Gasparis
 */
public class Main {

    
    public static void main(String[] args) {
        
        MainFrame frame = new MainFrame();
        frame.setSize(250, 200);
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screen.width / 2) - (frame.getWidth() / 2);
        int y = (screen.height / 2) - (frame.getHeight() / 2);
        frame.setLocation(x, y);
    }
}
