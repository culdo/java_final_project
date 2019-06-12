package line_gui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import line.LINEAuto;

public class LineLogin extends JDialog {
    private JPanel contentPane;
    private JButton btLogin;
    private JTextField tfAccount;
    private JPasswordField pfPasswd;

    public LineLogin(final LINEAuto browser) {
        setTitle("登入賴");

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(btLogin);
        setLocationRelativeTo(null);
        setSize(300, 230);
//        setVisible(true);

        btLogin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Onlogin(browser);
            }
        });
        pfPasswd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Onlogin(browser);
            }
        });
    }

    private void Onlogin(LINEAuto browser) {
//        btLogin.setText("登入中...");
//        btLogin.setEnabled(false);
        browser.login(tfAccount.getText(), pfPasswd.getText());
        setVisible(false);
    }

    public static void main(String[] args) {
        LineLogin dialog = new LineLogin(new LINEAuto());
//        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

}
