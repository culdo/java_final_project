package line_gui;

import line.LINEAuto;

import javax.swing.*;

public class LineLogin extends JDialog {
    private JPanel contentPane;

    public LineLogin() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                LineLogin.this.createUIComponents();
            }
        });
    }

    public void createUIComponents() {
        setTitle("登入賴");

        setContentPane(contentPane);
        setModal(true);
        setLocationRelativeTo(null);
        setSize(300, 230);
        toFront();
        setAlwaysOnTop(true);
        setVisible(true);
    }

    public static void main(String[] args) {
        LineLogin dialog = new LineLogin();
        LINEAuto testLINE =  new LINEAuto();
        testLINE.waitLogin();
        dialog.dispose();
        System.exit(0);
    }

}
