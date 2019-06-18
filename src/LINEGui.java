import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import com.google.common.base.Joiner;
import line.LINEAuto;
import line_gui.LineLogin;


class SwingWorkerProcessor extends SwingWorker<Void, Integer> {
    private final LINEGui panel;
    private int iteration;
    private int intervalInSeconds;
    private boolean isBotmode;
    private String msgTemp;
    private String room;
    private String sender;
    private String keyword;
    private SimpleDateFormat dateFormat;

    public SwingWorkerProcessor(LINEGui panel, int iteration, int intervalInSeconds,
                                String msgTemp, String room, String sender, String keyword, boolean isBotmode) {
        this.panel = panel;
        this.iteration = iteration;
        this.isBotmode = isBotmode;
        this.msgTemp = msgTemp;
        this.room = room;
        this.sender = sender;
        this.keyword = keyword;

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
        LINEGui.LineHelper.chooseRoom(room);
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
            String[] replaceStr = {Integer.toString(counter), result[0], result[1], dateString};

            if(result[1]!=null && result[0]!=null) {
                boolean custom_trigger = (sender.equals("任何人") || sender.equals(result[0])) &&
                        (keyword.equals("(任何字)") || result[1].contains(keyword));

                if (custom_trigger) {
                    System.out.println("enter send scope");
                    sendRoomMessage(placeholder, replaceStr);
                    this.publish(counter);
                    if (Thread.interrupted()) {
                        System.out.println("Be Interrpted");
                        throw new InterruptedException();
                    }
                    counter++;
                }
            }
            if(this.isCancelled()) {
                System.out.println("Canceled broken.");
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
        String pattern = String.format("\\((%s)\\)", Joiner.on("|").join(placeholders));
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
//    private JTextField taMsg;
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
    private JTextArea taMsg;
    private JDialog loginDialog;
    public static LINEAuto LineHelper;
    private SwingWorkerProcessor processor;


    public LINEGui(LINEAuto LineHelper_) {
        LineHelper = LineHelper_;

        cbRoom.setModel(new DefaultComboBoxModel(LineHelper.readRooms()));
        spDelay.setValue(1);
        setButtonStatus(true);
        plTable.setVisible(false);

        final CardLayout cl = (CardLayout)(cards.getLayout());

        btStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LINEGui.this.startProcessing();
            }
        });
        btCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LINEGui.this.cancelProcessing();
            }
        });
        rbChatbot.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String[] members = LineHelper.readMembers((String) cbRoom.getSelectedItem());
                if (members != null) {
                    cbSetSender.setModel(new DefaultComboBoxModel(members));
                }

                cl.show(cards, "Card2");
                plTable.setVisible(false);
                LINEGui.this.setSize(550, 500);
            }
        });
        rbTimemode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cl.show(cards, "Card1");
                plTable.setVisible(false);
                LINEGui.this.setSize(550, 500);
            }
        });
        rbPeek.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                plTable.setVisible(true);
                LINEGui.this.setSize(800, 500);
            }
        });
//        cbRoom.addMouseListener(new MouseAdapter() {
//                                    @Override
//                                    public void mousePressed(MouseEvent e) {
//                                        cbRoom.setModel(new DefaultComboBoxModel(LineHelper.readRooms()));
//                                    }
//                                }
//        );
        cbRoom.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
            int num = cbRoom.getItemCount();
            String[] newRooms = LineHelper.readRooms();
            boolean isEqual = true;
            // Get items
            for (int i = 0; i < num&&isEqual; i++) {
                String item = (String) cbRoom.getItemAt(i);
                if(!newRooms[i].equals(item)) {
                    isEqual = false;
                    break;
                }
            }
            if(!isEqual) {
                cbRoom.setModel(new DefaultComboBoxModel(LineHelper.readRooms()));
            }
            }
        });
        cbRoom.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                String room = (String) e.getItem();
                LineHelper.checkRoom(room);
                if (rbChatbot.isSelected()) {
                    String[] members = LineHelper.readMembers(room);
                    if (members != null) {
                        cbSetSender.setModel(new DefaultComboBoxModel(members));
                    }
                }
            }
        });

        setTitle("LINE自動操作小幫手");
        setContentPane(plMain);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(550, 500);
        setLocationRelativeTo(null);
        toFront();
        setAlwaysOnTop(true);
        setVisible(true);
    }

    public void setButtonStatus(boolean canStart) {
        Component[] plCom = {btStart, cbRoom, taMsg, spDelay, spTimes, rbChatbot,
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
    }

    public void startProcessing() {
        setButtonStatus(false);
        processor = new SwingWorkerProcessor(this, (Integer) spTimes.getValue(),
                                            (Integer) spDelay.getValue(), taMsg.getText(),
                                            (String) cbRoom.getSelectedItem(), (String) cbSetSender.getSelectedItem(),
                tfKeyword.getText(), rbChatbot.isSelected());
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

        LineLogin dialog = new LineLogin();
        final LINEAuto LINEHelper =  new LINEAuto();
        LINEHelper.waitLogin();
        dialog.dispose();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                LINEGui demoGui = new LINEGui(LINEHelper);
            }
        });

    }
}
