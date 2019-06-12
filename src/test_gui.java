import javax.swing.*;
import java.awt.*;

public class test_gui {
    private JButton button1;
    private JButton button2;
    private JPanel pn;

    public static void main(String[] args) {
        JFrame frame = new JFrame("test_gui");
        frame.setContentPane(new test_gui().pn);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

}
