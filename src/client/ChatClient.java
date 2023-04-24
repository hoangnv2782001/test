/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import controller.client.ClientControl;
import controller.client.FileDownload;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import model.Room;
import model.User;

/**
 *
 * @author Administrator
 */
public class ChatClient extends javax.swing.JFrame {

    /**
     * Creates new form ChatClient
     */
    private User user;
    private HashMap<String, JTextPane> chatMaps = new HashMap<String, JTextPane>();
    private int action;
    private int sendEnmoji;
    private DefaultListModel<String> modelNoti;
//    thuộc tính chat 1-1 1-n
    private DefaultListModel<String> listModel;
    private String chatWindow;
    private List<User> friendList;
//    private HashMap<String, JTextPane> chatMaps = new HashMap<String, JTextPane>();

//    thuộc tính chat room
    private DefaultListModel<String> listModel1;
    private String chatWindow1;
    private List<Room> rooms;
    
    public ChatClient(User user, ActionListener al, WindowAdapter wa, ListSelectionListener lsl) {
        
        initComponents();
        this.user = user;
        
        nameLabel.setText(user.getUsername() + "");
        this.logout.addActionListener(al);
        this.addWindowListener(wa);
        jDialog1.setLocationRelativeTo(this);

//        khởi tạo thuộc tính chat 1-1 , 1- n
        friendList = new ArrayList<>();
        listModel = new DefaultListModel<>();
        listUser.setModel(listModel);
        this.sendText.addActionListener(al);
        this.btnEmoji.addActionListener(al);
        listUser.addListSelectionListener(lsl);
        file.addActionListener(al);
        
        JTextPane pane = new JTextPane();
        pane.setFont(new Font("Tahoma", Font.PLAIN, 14));
        pane.setEditable(false);
        
        chatMaps.put("No Message", pane);

        //        khởi tạo thuộc tính chat room
        listModel1 = new DefaultListModel<>();
        listUser1.setModel(listModel1);
        this.sendText1.addActionListener(al);
        this.btnEmoji1.addActionListener(al);
        listUser1.addListSelectionListener(lsl);
        file1.addActionListener(al);
        createRoom.addActionListener(al);
        join.addActionListener(al);
        btnRoom.addActionListener(al);
        
        JTextPane pane1 = new JTextPane();
        pane.setFont(new Font("Tahoma", Font.PLAIN, 14));
        pane.setEditable(false);
        
        chatMaps.put("No Message Room", pane1);
        
        modelNoti = new DefaultListModel<>();
        listNoti.setModel(modelNoti);
        btnNotification.addActionListener(al);
        notiForm.setLocationRelativeTo(this);
        
    }
    
    public void ShowMessage(String username, String room, String message, boolean isSending, String receiver) {
        StyledDocument doc = null;
        if (username.equals(this.user.getUsername())) {
            doc = chatMaps.get(receiver).getStyledDocument();
        } else {
            if (username.equals(room)) {
                doc = chatMaps.get(username).getStyledDocument();
            } else {
                doc = chatMaps.get(room).getStyledDocument();
            }
        }
        
        Style userStyle = doc.getStyle("User style");
        if (userStyle == null) {
            userStyle = doc.addStyle("User style", null);
            StyleConstants.setBold(userStyle, true);
        }
        
        if (isSending == true) {
            StyleConstants.setForeground(userStyle, Color.BLUE);
        } else {
            StyleConstants.setForeground(userStyle, Color.BLACK);
        }

        // hiển thị tên người gửi
        try {
            doc.insertString(doc.getLength(), username + ": ", userStyle);
        } catch (BadLocationException e) {
        }
        
        Style messageStyle = doc.getStyle("Message style");
        if (messageStyle == null) {
            messageStyle = doc.addStyle("Message style", null);
            StyleConstants.setForeground(messageStyle, Color.BLACK);
            StyleConstants.setBold(messageStyle, false);
        }

        // hiển thị nội dung
        try {
            doc.insertString(doc.getLength(), message + "\n", messageStyle);
        } catch (BadLocationException e) {
        }
    }
    
    public void showEmojin(String username, String room, String emoji, boolean isSending, String receiver) {
        StyledDocument doc = null;
        if (username.equals(this.user.getUsername())) {
            doc = chatMaps.get(receiver).getStyledDocument();
        } else {
            if (username.equals(room)) {
                doc = chatMaps.get(username).getStyledDocument();
            } else {
                doc = chatMaps.get(room).getStyledDocument();
            }
        }
        Style userStyle = doc.getStyle("User style");
        if (userStyle == null) {
            userStyle = doc.addStyle("User style", null);
            StyleConstants.setBold(userStyle, true);
        }
        
        if (isSending == true) {
            StyleConstants.setForeground(userStyle, Color.BLUE);
        } else {
            StyleConstants.setForeground(userStyle, Color.BLACK);
        }

        // In ra màn hình tên người gửi
        try {
            doc.insertString(doc.getLength(), username + ": ", userStyle);
        } catch (BadLocationException e) {
        }
        
        Style iconStyle = doc.getStyle("Icon style");
        if (iconStyle == null) {
            iconStyle = doc.addStyle("Icon style", null);
        }
        
        StyleConstants.setIcon(iconStyle, new ImageIcon(emoji));

        // hiển thị emojin
        try {
            doc.insertString(doc.getLength(), "invisible text", iconStyle);
        } catch (BadLocationException e) {
        }

        // Xuống dòng
        try {
            doc.insertString(doc.getLength(), "\n", userStyle);
        } catch (BadLocationException e) {
        }
        
    }
    
    public void showFile(String username, String filename, byte[] file, boolean isSending, MouseAdapter ma) {
        
        StyledDocument doc;
        
        if (username.equals(this.user.getUsername())) {
            chatWindow = receiverMessage.getText();
        } else {
            chatWindow = username;
        }
        doc = chatMaps.get(chatWindow).getStyledDocument();
        
        Style userStyle = doc.getStyle("User style");
        if (userStyle == null) {
            userStyle = doc.addStyle("User style", null);
            StyleConstants.setBold(userStyle, true);
        }
        
        if (isSending == true) {
            StyleConstants.setForeground(userStyle, Color.BLUE);
        } else {
            StyleConstants.setForeground(userStyle, Color.BLACK);
        }
        
        try {
            doc.insertString(doc.getLength(), username + ": ", userStyle);
        } catch (BadLocationException e) {
        }
        
        Style linkStyle = doc.getStyle("Link style");
        if (linkStyle == null) {
            linkStyle = doc.addStyle("Link style", null);
            StyleConstants.setForeground(linkStyle, Color.BLACK);
            StyleConstants.setUnderline(linkStyle, true);
            StyleConstants.setBold(linkStyle, true);
            linkStyle.addAttribute("link", new FileDownload(filename, file));
        }
        
        if (chatMaps.get(chatWindow).getMouseListeners() != null) {
            // Tạo MouseListener cho các đường dẫn tải về file
            System.out.println("khong co mouse listener");
            chatMaps.get(chatWindow).addMouseListener(ma);
        }

        // In ra đường dẫn tải file
        try {
            doc.insertString(doc.getLength(), filename, linkStyle);
        } catch (BadLocationException e1) {
            e1.printStackTrace();
        }

        // Xuống dòng
        try {
            doc.insertString(doc.getLength(), "\n", userStyle);
        } catch (BadLocationException e1) {
            e1.printStackTrace();
        }
        
    }
    
    public void showFile(String username, String room, String filename, byte[] file, boolean isSending, MouseAdapter ma, String receiver) {
        
        StyledDocument doc;
        
        if (username.equals(this.user.getUsername())) {
            chatWindow = receiver;
        } else {
            if (username.equals(room)) {
                chatWindow = username;
                
            } else {
                chatWindow = room;
            }
        }
        doc = chatMaps.get(chatWindow).getStyledDocument();
        
        Style userStyle = doc.getStyle("User style");
        if (userStyle == null) {
            userStyle = doc.addStyle("User style", null);
            StyleConstants.setBold(userStyle, true);
        }
        
        if (isSending == true) {
            StyleConstants.setForeground(userStyle, Color.red);
        } else {
            StyleConstants.setForeground(userStyle, Color.BLUE);
        }
        
        try {
            doc.insertString(doc.getLength(), username + ": ", userStyle);
        } catch (BadLocationException e) {
        }
        
        Style linkStyle = doc.getStyle("Link style");
        if (linkStyle == null) {
            linkStyle = doc.addStyle("Link style", null);
            StyleConstants.setForeground(linkStyle, Color.BLACK);
            StyleConstants.setUnderline(linkStyle, true);
            StyleConstants.setBold(linkStyle, true);
            linkStyle.addAttribute("link", new FileDownload(filename, file));
        }
        
        if (chatMaps.get(chatWindow).getMouseListeners() != null) {
            // Tạo MouseListener cho các đường dẫn tải về file
            System.out.println("khong co mouse listener");
            chatMaps.get(chatWindow).addMouseListener(ma);
        }

        // In ra đường dẫn tải file
        try {
            doc.insertString(doc.getLength(), filename, linkStyle);
        } catch (BadLocationException e1) {
            e1.printStackTrace();
        }

        // Xuống dòng
        try {
            doc.insertString(doc.getLength(), "\n", userStyle);
        } catch (BadLocationException e1) {
            e1.printStackTrace();
        }
        
    }

//    public User getFriend() {
//
//        return friendList.get(listUser.getSelectedIndex());
//    }
//    ---------------------------thao tac voi phong----------------------------------------------
    public void updateListRoom(Room room) {
        listModel1.addElement(room.getName());
    }

//    ------------------------------------------------------------------------------------------
    public void showMess(String mess) {
        JOptionPane.showMessageDialog(this, mess);
    }
    
    public void updateListUsers(List<User> list) {
        listModel.clear();
        for (User l : list) {
            listModel.addElement(l.getUsername());
        }
    }
//    theem thong bao

    public void addNotification(String user) {
        btnNotification.setText("Có Thông Báo");
        modelNoti.addElement("Có tin nhắn mới từ " + user);
    }
    
    public void addEmojinAction(MouseListener ml) {
        e1.addMouseListener(ml);
        e2.addMouseListener(ml);
        e3.addMouseListener(ml);
        e4.addMouseListener(ml);
        e5.addMouseListener(ml);
        e6.addMouseListener(ml);
        e7.addMouseListener(ml);
        e8.addMouseListener(ml);
        e9.addMouseListener(ml);
        e10.addMouseListener(ml);
        e11.addMouseListener(ml);
        e12.addMouseListener(ml);
        e13.addMouseListener(ml);
        e14.addMouseListener(ml);
        e15.addMouseListener(ml);
        e16.addMouseListener(ml);
        e17.addMouseListener(ml);
        e18.addMouseListener(ml);
        
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public List<User> getFriendList() {
        return friendList;
    }
    
    public void setFriendList(List<User> friendList) {
        this.friendList = friendList;
    }
    
    public DefaultListModel<String> getListModel() {
        return listModel;
    }
    
    public void setListModel(DefaultListModel<String> listModel) {
        this.listModel = listModel;
    }
    
    public HashMap<String, JTextPane> getChatMaps() {
        return chatMaps;
    }
    
    public void setChatMaps(HashMap<String, JTextPane> chatMaps) {
        this.chatMaps = chatMaps;
    }
    
    public JButton getLogout() {
        return logout;
    }
    
    public DefaultListModel<String> getModelNoti() {
        return modelNoti;
    }
    
    public void setModelNoti(DefaultListModel<String> modelNoti) {
        this.modelNoti = modelNoti;
    }
    
    public JList<String> getListNoti() {
        return listNoti;
    }
    
    public void setListNoti(JList<String> listNoti) {
        this.listNoti = listNoti;
    }
    
    public void setLogout(JButton logout) {
        this.logout = logout;
    }
    
    public JLabel getNameLabel() {
        return nameLabel;
    }
    
    public void setNameLabel(JLabel nameLabel) {
        this.nameLabel = nameLabel;
    }
    
    public JTextPane getChatDefault() {
        return chatDefault;
    }
    
    public void setChatDefault(JTextPane chatDefault) {
        this.chatDefault = chatDefault;
    }
    
    public JButton getFile() {
        return file;
    }
    
    public void setFile(JButton file) {
        this.file = file;
    }
    
    public JList<String> getListUser() {
        return listUser;
    }
    
    public void setListUser(JList<String> listUser) {
        this.listUser = listUser;
    }
    
    public JLabel getReceiverMessage() {
        return receiverMessage;
    }
    
    public void setReceiverMessage(JLabel receiverMessage) {
        this.receiverMessage = receiverMessage;
    }
    
    public JScrollPane getScrollChat() {
        return scrollChat;
    }
    
    public int getSendEnmoji() {
        return sendEnmoji;
    }
    
    public void setSendEnmoji(int sendEnmoji) {
        this.sendEnmoji = sendEnmoji;
    }
    
    public List<Room> getRooms() {
        return rooms;
    }
    
    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }
    
    public void setScrollChat(JScrollPane scrollChat) {
        this.scrollChat = scrollChat;
    }
    
    public JScrollPane getScrollListUser() {
        return scrollListUser;
    }
    
    public void setScrollListUser(JScrollPane scrollListUser) {
        this.scrollListUser = scrollListUser;
    }
    
    public String getChatWindow() {
        return chatWindow;
    }
    
    public JButton getBtnNotification() {
        return btnNotification;
    }
    
    public void setBtnNotification(JButton btnNotification) {
        this.btnNotification = btnNotification;
    }
    
    public JDialog getNotiForm() {
        return notiForm;
    }
    
    public void setNotiForm(JDialog notiForm) {
        this.notiForm = notiForm;
    }
    
    public void setChatWindow(String chatWindow) {
        this.chatWindow = chatWindow;
    }
    
    public JButton getSendText() {
        return sendText;
    }
    
    public void setSendText(JButton sendText) {
        this.sendText = sendText;
    }
    
    public JTextField getTxtMessage() {
        return txtMessage;
    }
    
    public void setTxtMessage(JTextField txtMessage) {
        this.txtMessage = txtMessage;
    }
    
    public JButton getBtnEmoji() {
        return btnEmoji;
    }
    
    public void setBtnEmoji(JButton btnEmoji) {
        this.btnEmoji = btnEmoji;
    }
    
    public JLabel getE1() {
        return e1;
    }
    
    public void setE1(JLabel e1) {
        this.e1 = e1;
    }
    
    public JLabel getE10() {
        return e10;
    }
    
    public void setE10(JLabel e10) {
        this.e10 = e10;
    }
    
    public JLabel getE11() {
        return e11;
    }
    
    public void setE11(JLabel e11) {
        this.e11 = e11;
    }
    
    public JLabel getE12() {
        return e12;
    }
    
    public void setE12(JLabel e12) {
        this.e12 = e12;
    }
    
    public JLabel getE13() {
        return e13;
    }
    
    public void setE13(JLabel e13) {
        this.e13 = e13;
    }
    
    public JLabel getE14() {
        return e14;
    }
    
    public void setE14(JLabel e14) {
        this.e14 = e14;
    }
    
    public JLabel getE15() {
        return e15;
    }
    
    public void setE15(JLabel e15) {
        this.e15 = e15;
    }
    
    public JLabel getE16() {
        return e16;
    }
    
    public void setE16(JLabel e16) {
        this.e16 = e16;
    }
    
    public JLabel getE17() {
        return e17;
    }
    
    public void setE17(JLabel e17) {
        this.e17 = e17;
    }
    
    public JLabel getE18() {
        return e18;
    }
    
    public void setE18(JLabel e18) {
        this.e18 = e18;
    }
    
    public JLabel getE3() {
        return e3;
    }
    
    public void setE3(JLabel e3) {
        this.e3 = e3;
    }
    
    public JLabel getE4() {
        return e4;
    }
    
    public void setE4(JLabel e4) {
        this.e4 = e4;
    }
    
    public JLabel getE5() {
        return e5;
    }
    
    public void setE5(JLabel e5) {
        this.e5 = e5;
    }
    
    public JLabel getE6() {
        return e6;
    }
    
    public void setE6(JLabel e6) {
        this.e6 = e6;
    }
    
    public JLabel getE7() {
        return e7;
    }
    
    public void setE7(JLabel e7) {
        this.e7 = e7;
    }
    
    public JLabel getE8() {
        return e8;
    }
    
    public void setE8(JLabel e8) {
        this.e8 = e8;
    }
    
    public JLabel getE9() {
        return e9;
    }
    
    public void setE9(JLabel e9) {
        this.e9 = e9;
    }
    
    public JLabel getE2() {
        return e2;
    }
    
    public void setE2(JLabel e2) {
        this.e2 = e2;
    }
    
    public JDialog getjDialog1() {
        return jDialog1;
    }
    
    public void setjDialog1(JDialog jDialog1) {
        this.jDialog1 = jDialog1;
    }
    
    public DefaultListModel<String> getListModel1() {
        return listModel1;
    }
    
    public void setListModel1(DefaultListModel<String> listModel1) {
        this.listModel1 = listModel1;
    }
    
    public String getChatWindow1() {
        return chatWindow1;
    }
    
    public void setChatWindow1(String chatWindow1) {
        this.chatWindow1 = chatWindow1;
    }
    
    public JButton getBtnEmoji1() {
        return btnEmoji1;
    }
    
    public void setBtnEmoji1(JButton btnEmoji1) {
        this.btnEmoji1 = btnEmoji1;
    }
    
    public JTextPane getChatDefault1() {
        return chatDefault1;
    }
    
    public void setChatDefault1(JTextPane chatDefault1) {
        this.chatDefault1 = chatDefault1;
    }
    
    public JButton getFile1() {
        return file1;
    }
    
    public void setFile1(JButton file1) {
        this.file1 = file1;
    }
    
    public JButton getCreateRoom() {
        return createRoom;
    }
    
    public void setCreateRoom(JButton createRoom) {
        this.createRoom = createRoom;
    }
    
    public JButton getJoin() {
        return join;
    }
    
    public void setJoin(JButton join) {
        this.join = join;
    }
    
    public JList<String> getListUser1() {
        return listUser1;
    }
    
    public void setListUser1(JList<String> listUser1) {
        this.listUser1 = listUser1;
    }
    
    public JLabel getReceiverMessage1() {
        return receiverMessage1;
    }
    
    public void setReceiverMessage1(JLabel receiverMessage1) {
        this.receiverMessage1 = receiverMessage1;
    }
    
    public JScrollPane getScrollChat1() {
        return scrollChat1;
    }
    
    public void setScrollChat1(JScrollPane scrollChat1) {
        this.scrollChat1 = scrollChat1;
    }
    
    public JScrollPane getScrollListUser1() {
        return scrollListUser1;
    }
    
    public void setScrollListUser1(JScrollPane scrollListUser1) {
        this.scrollListUser1 = scrollListUser1;
    }
    
    public JButton getSendText1() {
        return sendText1;
    }
    
    public void setSendText1(JButton sendText1) {
        this.sendText1 = sendText1;
    }
    
    public JTextField getTxtMessage1() {
        return txtMessage1;
    }
    
    public void setTxtMessage1(JTextField txtMessage1) {
        this.txtMessage1 = txtMessage1;
    }
    
    public JButton getBtnRoom() {
        return btnRoom;
    }
    
    public void setBtnRoom(JButton btnRoom) {
        this.btnRoom = btnRoom;
    }
    
    public JDialog getReadRoom() {
        return readRoom;
    }
    
    public void setReadRoom(JDialog readRoom) {
        this.readRoom = readRoom;
    }
    
    public JTextField getTxtRoom() {
        return txtRoom;
    }
    
    public void setTxtRoom(JTextField txtRoom) {
        this.txtRoom = txtRoom;
    }
    
    public int getAction() {
        return action;
    }
    
    public void setAction(int action) {
        this.action = action;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jDialog1 = new javax.swing.JDialog();
        e1 = new javax.swing.JLabel();
        e2 = new javax.swing.JLabel();
        e3 = new javax.swing.JLabel();
        e4 = new javax.swing.JLabel();
        e5 = new javax.swing.JLabel();
        e6 = new javax.swing.JLabel();
        e7 = new javax.swing.JLabel();
        e8 = new javax.swing.JLabel();
        e9 = new javax.swing.JLabel();
        e10 = new javax.swing.JLabel();
        e11 = new javax.swing.JLabel();
        e12 = new javax.swing.JLabel();
        e13 = new javax.swing.JLabel();
        e14 = new javax.swing.JLabel();
        e15 = new javax.swing.JLabel();
        e16 = new javax.swing.JLabel();
        e17 = new javax.swing.JLabel();
        e18 = new javax.swing.JLabel();
        readRoom = new javax.swing.JDialog();
        jLabel2 = new javax.swing.JLabel();
        txtRoom = new javax.swing.JTextField();
        btnRoom = new javax.swing.JButton();
        notiForm = new javax.swing.JDialog();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        listNoti = new javax.swing.JList<>();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        nameLabel = new javax.swing.JLabel();
        logout = new javax.swing.JButton();
        btnNotification = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        scrollChat = new javax.swing.JScrollPane();
        chatDefault = new javax.swing.JTextPane();
        scrollListUser = new javax.swing.JScrollPane();
        listUser = new javax.swing.JList<>();
        txtMessage = new javax.swing.JTextField();
        file = new javax.swing.JButton();
        sendText = new javax.swing.JButton();
        receiverMessage = new javax.swing.JLabel();
        btnEmoji = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        scrollChat1 = new javax.swing.JScrollPane();
        chatDefault1 = new javax.swing.JTextPane();
        scrollListUser1 = new javax.swing.JScrollPane();
        listUser1 = new javax.swing.JList<>();
        txtMessage1 = new javax.swing.JTextField();
        file1 = new javax.swing.JButton();
        sendText1 = new javax.swing.JButton();
        receiverMessage1 = new javax.swing.JLabel();
        btnEmoji1 = new javax.swing.JButton();
        createRoom = new javax.swing.JButton();
        join = new javax.swing.JButton();

        jDialog1.setTitle("Emojin");
        jDialog1.setResizable(false);
        jDialog1.setSize(new java.awt.Dimension(407, 207));

        e1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ioc/angry.png"))); // NOI18N

        e2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ioc/cake.png"))); // NOI18N

        e3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ioc/sad.png"))); // NOI18N

        e4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ioc/apple.png"))); // NOI18N

        e5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ioc/ninja.png"))); // NOI18N

        e6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ioc/orange.png"))); // NOI18N

        e7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ioc/egypt.png"))); // NOI18N

        e8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ioc/big-smile.png"))); // NOI18N

        e9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ioc/happy.png"))); // NOI18N

        e10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ioc/mad.png"))); // NOI18N

        e11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ioc/love.png"))); // NOI18N

        e12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ioc/smile.png"))); // NOI18N

        e13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ioc/vietnam.png"))); // NOI18N

        e14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ioc/australia.png"))); // NOI18N

        e15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ioc/italy.png"))); // NOI18N

        e16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ioc/uk.png"))); // NOI18N

        e17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ioc/sweden.png"))); // NOI18N

        e18.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ioc/spain.png"))); // NOI18N

        javax.swing.GroupLayout jDialog1Layout = new javax.swing.GroupLayout(jDialog1.getContentPane());
        jDialog1.getContentPane().setLayout(jDialog1Layout);
        jDialog1Layout.setHorizontalGroup(
            jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialog1Layout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addGroup(jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(e1)
                    .addComponent(e7)
                    .addComponent(e13))
                .addGap(37, 37, 37)
                .addGroup(jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jDialog1Layout.createSequentialGroup()
                        .addComponent(e14)
                        .addGap(39, 39, 39)
                        .addComponent(e15))
                    .addGroup(jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(jDialog1Layout.createSequentialGroup()
                            .addComponent(e8)
                            .addGap(39, 39, 39)
                            .addComponent(e9))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jDialog1Layout.createSequentialGroup()
                            .addComponent(e2)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(e3))))
                .addGap(59, 59, 59)
                .addGroup(jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(e4)
                    .addComponent(e16)
                    .addComponent(e10))
                .addGap(39, 39, 39)
                .addGroup(jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(e11)
                    .addComponent(e17)
                    .addComponent(e5))
                .addGroup(jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jDialog1Layout.createSequentialGroup()
                        .addGap(56, 56, 56)
                        .addComponent(e6)
                        .addGap(0, 49, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jDialog1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(e18)
                            .addComponent(e12))
                        .addGap(48, 48, 48))))
        );
        jDialog1Layout.setVerticalGroup(
            jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialog1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(e2)
                    .addComponent(e3)
                    .addComponent(e4, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(e5, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(e6)
                    .addComponent(e1))
                .addGap(31, 31, 31)
                .addGroup(jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(e12, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(e7)
                        .addComponent(e8)
                        .addComponent(e9)
                        .addComponent(e10)
                        .addComponent(e11)))
                .addGap(36, 36, 36)
                .addGroup(jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(e17)
                    .addComponent(e18)
                    .addComponent(e16)
                    .addComponent(e15)
                    .addComponent(e14)
                    .addComponent(e13))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        readRoom.setResizable(false);
        readRoom.setSize(new java.awt.Dimension(480, 200));

        jLabel2.setText("Nhập Tên Phòng");

        btnRoom.setText("Ok");

        javax.swing.GroupLayout readRoomLayout = new javax.swing.GroupLayout(readRoom.getContentPane());
        readRoom.getContentPane().setLayout(readRoomLayout);
        readRoomLayout.setHorizontalGroup(
            readRoomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(readRoomLayout.createSequentialGroup()
                .addGroup(readRoomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(readRoomLayout.createSequentialGroup()
                        .addGap(196, 196, 196)
                        .addComponent(jLabel2))
                    .addGroup(readRoomLayout.createSequentialGroup()
                        .addGap(144, 144, 144)
                        .addComponent(txtRoom, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(readRoomLayout.createSequentialGroup()
                        .addGap(214, 214, 214)
                        .addComponent(btnRoom)))
                .addContainerGap(164, Short.MAX_VALUE))
        );
        readRoomLayout.setVerticalGroup(
            readRoomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(readRoomLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addGap(27, 27, 27)
                .addComponent(txtRoom, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 27, Short.MAX_VALUE)
                .addComponent(btnRoom)
                .addContainerGap())
        );

        notiForm.setResizable(false);
        notiForm.setSize(new java.awt.Dimension(500, 400));

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel3.setText("Thông Báo");

        listNoti.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        jScrollPane1.setViewportView(listNoti);

        javax.swing.GroupLayout notiFormLayout = new javax.swing.GroupLayout(notiForm.getContentPane());
        notiForm.getContentPane().setLayout(notiFormLayout);
        notiFormLayout.setHorizontalGroup(
            notiFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, notiFormLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(141, 141, 141))
            .addGroup(notiFormLayout.createSequentialGroup()
                .addGap(91, 91, 91)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 302, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(85, Short.MAX_VALUE))
        );
        notiFormLayout.setVerticalGroup(
            notiFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(notiFormLayout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 232, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(57, Short.MAX_VALUE))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(204, 204, 204));

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ioc/profile.png"))); // NOI18N

        nameLabel.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        nameLabel.setText("Name");

        logout.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        logout.setText("Đăng Xuất");
        logout.setBorder(null);
        logout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logoutActionPerformed(evt);
            }
        });

        btnNotification.setText("Thông Báo");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(36, 36, 36)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(nameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnNotification, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(69, 69, 69)
                .addComponent(logout, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(40, 40, 40)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(nameLabel)
                            .addComponent(logout, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnNotification, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(30, Short.MAX_VALUE))
        );

        chatDefault.setEditable(false);
        scrollChat.setViewportView(chatDefault);

        listUser.setBorder(javax.swing.BorderFactory.createTitledBorder("Online"));
        listUser.setAlignmentX(5);
        scrollListUser.setViewportView(listUser);

        file.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ioc/attach.png"))); // NOI18N

        sendText.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ioc/send.png"))); // NOI18N

        receiverMessage.setBackground(new java.awt.Color(153, 153, 153));
        receiverMessage.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        receiverMessage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        receiverMessage.setText("No Message");
        receiverMessage.setOpaque(true);

        btnEmoji.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ioc/big-smile.png"))); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollListUser, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrollChat)
                    .addComponent(receiverMessage, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(txtMessage, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(file, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnEmoji, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 16, Short.MAX_VALUE)
                        .addComponent(sendText, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(receiverMessage, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(scrollChat, javax.swing.GroupLayout.PREFERRED_SIZE, 327, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(scrollListUser, javax.swing.GroupLayout.PREFERRED_SIZE, 356, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 25, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(txtMessage)
                            .addComponent(file, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE))
                        .addComponent(sendText, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnEmoji, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20))
        );

        jTabbedPane1.addTab("Chat", jPanel2);

        chatDefault1.setEditable(false);
        scrollChat1.setViewportView(chatDefault1);

        listUser1.setBorder(javax.swing.BorderFactory.createTitledBorder("Online"));
        listUser1.setAlignmentX(5);
        scrollListUser1.setViewportView(listUser1);

        file1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ioc/attach.png"))); // NOI18N

        sendText1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ioc/send.png"))); // NOI18N

        receiverMessage1.setBackground(new java.awt.Color(153, 153, 153));
        receiverMessage1.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        receiverMessage1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        receiverMessage1.setText("No Message Room");
        receiverMessage1.setOpaque(true);

        btnEmoji1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ioc/big-smile.png"))); // NOI18N

        createRoom.setText("Tạo Phòng");
        createRoom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createRoomActionPerformed(evt);
            }
        });

        join.setText("Tham Gia Phòng");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(scrollListUser1, javax.swing.GroupLayout.DEFAULT_SIZE, 192, Short.MAX_VALUE)
                    .addComponent(createRoom, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(join, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrollChat1)
                    .addComponent(receiverMessage1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(txtMessage1, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(file1, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnEmoji1, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 16, Short.MAX_VALUE)
                        .addComponent(sendText1, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(receiverMessage1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addComponent(scrollChat1, javax.swing.GroupLayout.PREFERRED_SIZE, 327, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(scrollListUser1, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(55, 55, 55)
                        .addComponent(createRoom, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(46, 46, 46)
                        .addComponent(join, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 25, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(txtMessage1)
                            .addComponent(file1, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE))
                        .addComponent(sendText1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnEmoji1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20))
        );

        jTabbedPane1.addTab("Room", jPanel4);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jTabbedPane1)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void logoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logoutActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_logoutActionPerformed

    private void createRoomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createRoomActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_createRoomActionPerformed

    /**
     * @param args the command line arguments
     */
//    public static void main(String args[]) {
//        /* Set the Nimbus look and feel */
//        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
//        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
//         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
//         */
//        try {
//            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
//        } catch (ClassNotFoundException ex) {
//            java.util.logging.Logger.getLogger(ChatClient.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (InstantiationException ex) {
//            java.util.logging.Logger.getLogger(ChatClient.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (IllegalAccessException ex) {
//            java.util.logging.Logger.getLogger(ChatClient.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
//            java.util.logging.Logger.getLogger(ChatClient.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        }
//        //</editor-fold>
//
//        /* Create and display the form */
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                new ChatClient().setVisible(true);
//            }
//        });
//    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnEmoji;
    private javax.swing.JButton btnEmoji1;
    private javax.swing.JButton btnNotification;
    private javax.swing.JButton btnRoom;
    private javax.swing.JTextPane chatDefault;
    private javax.swing.JTextPane chatDefault1;
    private javax.swing.JButton createRoom;
    private javax.swing.JLabel e1;
    private javax.swing.JLabel e10;
    private javax.swing.JLabel e11;
    private javax.swing.JLabel e12;
    private javax.swing.JLabel e13;
    private javax.swing.JLabel e14;
    private javax.swing.JLabel e15;
    private javax.swing.JLabel e16;
    private javax.swing.JLabel e17;
    private javax.swing.JLabel e18;
    private javax.swing.JLabel e2;
    private javax.swing.JLabel e3;
    private javax.swing.JLabel e4;
    private javax.swing.JLabel e5;
    private javax.swing.JLabel e6;
    private javax.swing.JLabel e7;
    private javax.swing.JLabel e8;
    private javax.swing.JLabel e9;
    private javax.swing.JButton file;
    private javax.swing.JButton file1;
    private javax.swing.JDialog jDialog1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JButton join;
    private javax.swing.JList<String> listNoti;
    private javax.swing.JList<String> listUser;
    private javax.swing.JList<String> listUser1;
    private javax.swing.JButton logout;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JDialog notiForm;
    private javax.swing.JDialog readRoom;
    private javax.swing.JLabel receiverMessage;
    private javax.swing.JLabel receiverMessage1;
    private javax.swing.JScrollPane scrollChat;
    private javax.swing.JScrollPane scrollChat1;
    private javax.swing.JScrollPane scrollListUser;
    private javax.swing.JScrollPane scrollListUser1;
    private javax.swing.JButton sendText;
    private javax.swing.JButton sendText1;
    private javax.swing.JTextField txtMessage;
    private javax.swing.JTextField txtMessage1;
    private javax.swing.JTextField txtRoom;
    // End of variables declaration//GEN-END:variables
}
