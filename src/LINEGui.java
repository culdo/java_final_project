import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
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
    private SimpleDateFormat dateFormat;

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
        dateFormat = new SimpleDateFormat("HH時mm分ss.SSS秒");

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
        String[] placeholder = {"訊息數", "誰", "訊息", "時間"};
        int counter = 1;
        System.out.println("i got in botmode");
        while(true) {
            String dateString = dateFormat.format(new Date());
            String[] result = LINEGui.LineHelper.checkNewMsg(room,"Other");
            String[] repStr = {Integer.toString(counter), result[0], result[1], dateString};

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
        String[] placeholder = {"訊息數", "時間"};
        System.out.println("i got in timemode");
        for (int counter = 1; counter <= iteration || iteration == 0; counter++) {
            String dateString = dateFormat.format(new Date());
            System.out.println(dateString);
            String[] repStr = {Integer.toString(counter), dateString};

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
                    break;
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

public class LINEGui extends JFrame {
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
    private JComboBox cbSetSender;
    private JTextField tfKeyword;
    private JPanel card1;
    private JPanel card2;
    private JPanel cards;
    private JPanel plBase;
    private JPanel plMode;
    private JRadioButton rbPeek;
    private JDialog loginDialog;
    public static LINEAuto LineHelper;
    private SwingWorkerProcessor processor;


    public LINEGui() {
        spDelay.setValue(1);
        setButtonStatus(true);
        plTable.setVisible(false);

        CardLayout cl = (CardLayout)(cards.getLayout());

        btStart.addActionListener(e -> startProcessing());
        btCancel.addActionListener(e -> cancelProcessing());
        rbChatbot.addActionListener(e -> {
            tfMsg.setText("機器人已發送(訊息數)則，訊息由(誰)說了(訊息)");
            cl.show(cards, "Card2");
            plTable.setVisible(false);
            setSize(550, 500);
        });
        rbTimemode.addActionListener(e -> {
            tfMsg.setText("機器人已發送(訊息數)則");
            cl.show(cards, "Card1");
            plTable.setVisible(false);
            setSize(550, 500);
        });
        rbPeek.addActionListener(e -> {
            plTable.setVisible(true);
            setSize(800, 500);
        });

        setTitle("LINE自動操作小幫手");
        setContentPane(plMain);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(550, 500);
        setLocationRelativeTo(null);
        setVisible(true);
        toFront();
        setAlwaysOnTop(true);
    }

    public void setButtonStatus(boolean canStart) {
        Component[] plCom = {btStart, cbRoom, tfMsg, spDelay, spTimes, rbChatbot,
                                rbTimemode, rbPeek, tfKeyword, cbSetSender};
        for (Component com : plCom
        ) {
            if (canStart) {
                com.setEnabled(true);
            } else {
                com.setEnabled(false);
            }
        }
        if (canStart) {
            btCancel.setEnabled(false);
        } else {
            btCancel.setEnabled(true);
        }
//        plOption.setEnabled(canStart);
//        plMode.setEnabled(canStart);
//        if (canStart) {
//            btCancel.setEnabled(false);
//            btStart.setEnabled(true);
//        } else {
//            btCancel.setEnabled(true);
//            btStart.setEnabled(false);
//        }
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
        LineHelper.waitLogin();
        cbRoom = new JComboBox(LineHelper.readRooms());
    }
}
