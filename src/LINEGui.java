import javax.swing.*;
import java.awt.event.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import line.LINEAuto;

public class LINEGui {
    private JComboBox cbRoom;
    private JSpinner spDelay;
    private JTextField tfMsg;
    private JButton btStart;
    private JButton btStop;
    private JTable tbMsgs;
    private JPanel plMain;
    private JPanel plControl;
    private JPanel plTable;
    private JPanel plRun;
    private JPanel plOption;
    private JDialog loginDialog;
    private LINEAuto LineHelper;

    public LINEGui() {

        spDelay.setValue(1);
        btStart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loopMessage();
            }
        });
    }

    private void loopMessage() {
        LineHelper.chooseRoom((String)cbRoom.getSelectedItem());
        LineHelper.sendMsg(tfMsg.getText());

        SwingUtilities.invokeLater(() ->  {
            MySwingApp  app = new MySwingApp("A Swing App");
            app.pack();
            app.setVisible(true);
        });

    }

    public static void main(String[] args) {

        LINEGui demoGui = new LINEGui();

        final JFrame frame = new JFrame("LINE自動操作小幫手");
//        demoGui.loginDialog.addWindowListener(new WindowAdapter() {
//            public void windowClosing(WindowEvent e) {
//                frame.dispose();
//            }
//        });
        frame.setContentPane(demoGui.plMain);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.pack();
        frame.setSize(800, 500);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

    }

    private void createUIComponents() {
        // TODO: place custom component creation code here

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException e) {
            // handle exception
        } catch (ClassNotFoundException e) {
            // handle exception
        } catch (InstantiationException e) {
            // handle exception
        } catch (IllegalAccessException e) {
            // handle exception
        }

        LineHelper = new LINEAuto();
        LineHelper.login("george0228489372@yahoo.com.tw", "wuorsut");
//        loginDialog = new LineLogin(LineHelper);
//        loginDialog.setVisible(true);

        cbRoom = new JComboBox(LineHelper.readRooms());
    }
}
