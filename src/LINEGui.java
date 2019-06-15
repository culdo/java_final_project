import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import line.LINEAuto;


class SwingWorkerProcessor extends SwingWorker<Void, Integer> {
    private final LINEGui panel;
    private int iteration;
    private int intervalInSeconds;
    private boolean isBotmode;
    private String msgTemp;
    private String room;

    public SwingWorkerProcessor(LINEGui panel, int iteration, int intervalInSeconds,
                                String msgTemp, String room,  boolean isBotmode) {
        this.panel = panel;
        this.iteration = iteration;
        this.isBotmode = isBotmode;
        this.msgTemp = msgTemp;
        this.room = room;

        if (this.iteration < 0) {
            this.iteration = 0;
        }
        this.intervalInSeconds = intervalInSeconds;
        if (this.intervalInSeconds < 0) {
            this.intervalInSeconds = 0;
        }
    }

    @Override
    protected Void doInBackground() throws Exception{
        if(!isBotmode){
            timeModetask();
        }else{
            botModetask();
        }
        return null;
    }

    private void botModetask() throws Exception {
        String[] placeholder = {"訊息數", "誰", "訊息"};
        int counter = 1;
        while(true) {
            String[] result = LINEGui.LineHelper.checkNewMsg(room,"Other");
            String[] repStr = {Integer.toString(counter), result[0], result[1]};
            if(result[1]!=null) {
                sendRoomMessage(placeholder, repStr);
                this.publish(counter);
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
                counter++;
            }
            if(this.isCancelled()) {
                break;
            }
        }
    }

    private void timeModetask() throws Exception {
        String[] placeholder = {"訊息數"};
        for (int counter = 1; counter <= iteration || iteration == 0; counter++) {
            String[] repStr = {Integer.toString(counter)};
            sendRoomMessage(placeholder, repStr);
            this.publish(counter);
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            if (this.isCancelled()) {
                break;
            }
            TimeUnit.SECONDS.sleep(intervalInSeconds);
        }
    }


    public void sendRoomMessage(String[] placeholder, String[] repStr) {
        String msg = msgFormatter(msgTemp, placeholder, repStr);
        LINEGui.LineHelper.sendMsg(room, msg);
    }

    private static String msgFormatter(String msg, String[] placeholders, String[] replaceStrings) {
        String pattern = String.format("\\((%s)\\)", String.join("|", placeholders));
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(msg);
        StringBuffer sb = new StringBuffer();

        while (m.find()) {
            String newString="";
            for (int i = 0; i < placeholders.length; i++) {
                if (m.group(1).equals(placeholders[i])) {
                    newString = replaceStrings[i];
                }
            }
            m.appendReplacement(sb, newString);
        }

        m.appendTail(sb);
        System.out.println(sb.toString());

        return sb.toString();
    }


    @Override
    protected void process(List<Integer> data) {
        for (int counter : data) {
            panel.updateStatus(counter, iteration);
        }
    }

    @Override
    public void done() {
        try {
            panel.doneProcessing();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

public class LINEGui {
    private JComboBox cbRoom;
    private JSpinner spDelay;
    private JTextField tfMsg;
    private JButton btStart;
    private JButton btCancel;
    private JTable tbMsgs;
    private JPanel plMain;
    private JPanel plControl;
    private JPanel plTable;
    private JPanel plRun;
    private JPanel plOption;
    private JLabel lbStatus;
    private JSpinner spTimes;
    private JRadioButton rbTimemode;
    private JRadioButton rbChatbot;
    private JDialog loginDialog;
    public static LINEAuto LineHelper;
    private SwingWorkerProcessor processor;


    public LINEGui() {
        spDelay.setValue(1);
        setButtonStatus(true);
        rbTimemode.setSelected(true);

        btStart.addActionListener(e -> startProcessing());
        btCancel.addActionListener(e -> cancelProcessing());
        rbChatbot.addActionListener(e -> spDelay.setEnabled(false));
        rbTimemode.addActionListener(e -> spDelay.setEnabled(true));
    }

    public void setButtonStatus(boolean canStart) {
        Component[] plCom = {btStart, btCancel, cbRoom, tfMsg,
                spDelay, spTimes, rbChatbot, rbTimemode};
        for (Component com : plCom
        ) {
            if (com == btCancel) {
                if (canStart) {
                    com.setEnabled(false);
                } else {
                    com.setEnabled(true);
                }
            } else {
                if (canStart) {
                    com.setEnabled(true);
                } else {
                    com.setEnabled(false);
                }
            }
        }
    }

    public void startProcessing() {
        setButtonStatus(false);
        processor = new SwingWorkerProcessor(this, (Integer) spTimes.getValue(),
                                            (Integer) spDelay.getValue(), tfMsg.getText(),
                                            (String) cbRoom.getSelectedItem(), rbChatbot.isSelected());
        processor.execute();
    }

    public void cancelProcessing() {
        processor.cancel(true);
        setButtonStatus(true);
    }

    public void updateStatus(int counter, int total) {
        String msg = "狀態：目前發送 " + counter+"  則，";
        if(total==0) {
            msg+="無限則發送...";
        }else{
            msg+="總共  " + total + "  則...";
        }
        lbStatus.setText(msg);
    }

    public void doneProcessing() throws Exception {
        if (processor.isCancelled()) {
            lbStatus.setText("狀態：自動發訊息已取消。");
        } else {
            setButtonStatus(true);
        }
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
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
        });


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
