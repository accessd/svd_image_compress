import javax.swing.JFrame;

/**
 * Application for image compression by svd decomposition
 * Author: accessd
 */

public class Main {
    public static void main(String args[]) {
        gui gi = new gui();
        gi.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gi.setVisible(true);
    }
}
