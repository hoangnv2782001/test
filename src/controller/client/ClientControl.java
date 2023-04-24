/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller.client;

import client.ChatClient;

import client.Login;
import client.Signup;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import model.Message;
import model.Room;
import model.User;

/**
 *
 * @author Administrator
 */
public class ClientControl extends Thread {

    private String host = "localhost";
    private int port = 9911;
    private Socket socket;

    private DataInputStream dis;
    private DataOutputStream dos;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    private User user;

    private Login loginView;
    private Signup signupView;
    private ChatClient chatClient;

    private Thread t;

//    khoiw taoj
    public ClientControl(Login loginView, Signup signupView) {
        this.loginView = loginView;
        this.signupView = signupView;
        this.loginView.addAction(new AccessListener());
        this.signupView.addAction(new AccessListener());
    }
//connect server

    public void Connect() {
        try {
            if (socket != null) {
                socket.close();
            }
            socket = new Socket(host, port);
            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());
            this.ois = new ObjectInputStream(socket.getInputStream());
            this.oos = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
//đăng nhập

    public String Login(User user) {
        try {
            Connect();

            dos.writeUTF("Log in");
            oos.writeObject(user);
            dos.flush();
            oos.flush();

            String response = dis.readUTF();
            return response;

        } catch (IOException e) {
            e.printStackTrace();
            return "Network error: Log in fail";
        }
    }
//đăng kí

    public String Signup(User user) {
        try {
            Connect();

            dos.writeUTF("Sign up");
            oos.writeObject(user);
            oos.flush();
            dos.flush();
            String response = dis.readUTF();
            return response;

        } catch (IOException e) {
            e.printStackTrace();
            return "Network error: Sign up fail";
        }
    }

//xử lí dữ liệu nhận
    public void receiveData() {
        t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        // Chờ tin nhắn từ server
//                        String method = dis.readUTF();
                        Message message = (Message) ois.readObject();
                        System.out.println(message.getType());
                        String method = message.getType();
//---------------------------------xử lí chat 1-1 1-n----------------------------------------------
                        if (method.equals("text")) {
                            // Nhận một tin nhắn văn bản
                            String sender = message.getNameReceiver();//dis.readUTF();
                            String content = message.getContent();//dis.readUTF();

                            // In tin nhắn lên màn hình chat với người gửi
                            chatClient.ShowMessage(sender, sender, content, false, chatClient.getReceiverMessage().getText());
//                            notification(sender);
                            System.out.println(sender + " toi " + user.getUsername() + ":" + content);
                            if (!sender.equals(chatClient.getReceiverMessage().getText())) {
                                chatClient.addNotification(sender);
                            }
                        } else if (method.equals("emoji")) {
                            // Nhận một emoji
                            String sender = message.getNameReceiver();//dis.readUTF();
                            String emoji = message.getContent();//dis.readUTF();

                            // In tin nhắn lên màn hình chat với người gửi
                            chatClient.showEmojin(sender, sender, emoji, false, chatClient.getReceiverMessage().getText());
//                            notification(sender);
                            if (!sender.equals(chatClient.getReceiverMessage().getText())) {
                                chatClient.addNotification(sender);
                            }
                        } else if (method.equals("file")) {
                            // Nhận một file
                            String sender = message.getNameReceiver();//dis.readUTF();
                            String[] arr = message.getContent().split(",");
                            String fileName = arr[0];//dis.readUTF();
                            int size = Integer.parseInt(arr[1]);
                            int bufferSize = 1024;
                            byte[] buffer = new byte[bufferSize];
                            ByteArrayOutputStream file = new ByteArrayOutputStream();

                            while (size > 0) {
                                dis.read(buffer, 0, Math.min(bufferSize, size));
                                file.write(buffer, 0, Math.min(bufferSize, size));
                                size -= bufferSize;
                            }
//                            notification(sender);

                            // hiển thị file
                            chatClient.showFile(sender, fileName, file.toByteArray(), false, new FileListener());
                            if (!sender.equals(chatClient.getReceiverMessage().getText())) {
                                chatClient.addNotification(sender);
                            }
                        } else if (method.equals("Update users")) {

                            // Danh sách người dùng trực tuyến
                            List<User> us = (List<User>) ois.readObject();
                            System.out.println(us.size());

                            String chater = chatClient.getReceiverMessage().getText();
                            boolean isOnline = false;

                            if (us.size() != 0) {
                                List<User> userRemove = new ArrayList<>();
                                for (User u : us) {
                                    if (u.getUsername().equals(user.getUsername())) {

                                        //  thêm vao danh sach remmove
                                        userRemove.add(u);

                                    } else {
                                        if (chatClient.getChatMaps().get(u) == null) {
                                            JTextPane pane = new JTextPane();
                                            pane.setFont(new Font("Tahoma", Font.PLAIN, 14));
                                            pane.setEditable(false);
                                            chatClient.getChatMaps().put(u.getUsername(), pane);
                                        }
                                        if (chater.equals(u.getUsername())) {
                                            isOnline = true;
                                        }
                                    }
                                    System.out.println("chatting " + chater);

                                }
                                System.out.println("gia tri online " + isOnline);
                                System.out.println(chatClient.getChatMaps());
                                us.removeAll(userRemove);

                                chatClient.updateListUsers(us);
                                chatClient.setFriendList(us);
                            }

                            if (isOnline == false && !chater.equals("No Message")) {
                                // Nếu người đang chat không online thì chuyển hướng về màn hình mặc định và thông báo cho người dùng
                                chatClient.getReceiverMessage().setText("No Message");

//                                JOptionPane.showMessageDialog(null, chater + " Đã Offline");
                            }
//
//                    onlineUsers.validate();
                        } //  ---------------------------------xử lí chat phong---------------------------------------------------------
                        //    xử kiện tạo phòng
                        else if (method.equals("createRoom")) {
                            String content = message.getContent();
                            System.out.println("tham gia-----------------------------");
                            if (content.equals("fail")) {
                                chatClient.showMess("Tên phòng đã tồn tại");
                            } else {
                                Room room = (Room) ois.readObject();
                                chatClient.showMess("Tạo phòng thành công");
                                chatClient.updateListRoom(room);
                                JTextPane pane = new JTextPane();
                                pane.setFont(new Font("Tahoma", Font.PLAIN, 14));
                                pane.setEditable(false);
                                chatClient.getChatMaps().put(room.getName(), pane);
                            }
                        } //     xự kiến tham gia nhóm
                        else if (method.equals("join")) {
                            String content = message.getContent();
                            if (content.equals("pass")) {
                                Room room = (Room) ois.readObject();
                                chatClient.showMess("Tham gia thành công");
                                chatClient.updateListRoom(room);
                                JTextPane pane = new JTextPane();
                                pane.setFont(new Font("Tahoma", Font.PLAIN, 14));
                                pane.setEditable(false);
                                chatClient.getChatMaps().put(room.getName(), pane);
                            } else {
                                chatClient.showMess("Phòng không tồn tại");
                            }
                        } //      xự kiện gửi text
                        else if (method.equals("text-group")) {
                            // Nhận một tin nhắn văn bản
                            String[] sender = message.getNameReceiver().split("-");//dis.readUTF();
                            String content = message.getContent();//dis.readUTF();
                            System.out.println(sender + "            " + content);

                            // In tin nhắn lên màn hình chat với người gửi
                            chatClient.ShowMessage(sender[0], sender[1], content, false, chatClient.getReceiverMessage1().getText());
//                            notification(sender);
                            System.out.println(sender + " toi " + user.getUsername() + ":" + content);
                            if (!sender[1].equals(chatClient.getReceiverMessage1().getText())) {
                                chatClient.addNotification(sender[1]);
                            }
                        } //                        xự kiện gửi emoji
                        else if (method.equals("emoji-group")) {
                            // Nhận một emoji
                            String[] sender = message.getNameReceiver().split("-");//dis.readUTF();
                            String content = message.getContent();//dis.readUTF();
                            System.out.println(sender + "            " + content);

                            // In tin nhắn lên màn hình chat với người gửi
                            chatClient.showEmojin(sender[0], sender[1], content, false, chatClient.getReceiverMessage1().getText());
//                            notification(sender);
                            System.out.println(sender + " toi " + user.getUsername() + ":" + content);
                            if (!sender[1].equals(chatClient.getReceiverMessage1().getText())) {
                                chatClient.addNotification(sender[1]);
                            }
                        } else if (method.equals("file-group")) {
                            // Nhận một file
                            String[] sender = message.getNameReceiver().split("-");//dis.readUTF();
                            String[] arr = message.getContent().split(",");
                            String fileName = arr[0];//dis.readUTF();
                            int size = Integer.parseInt(arr[1]);
                            int bufferSize = 1024;
                            byte[] buffer = new byte[bufferSize];
                            ByteArrayOutputStream file = new ByteArrayOutputStream();

                            while (size > 0) {
                                dis.read(buffer, 0, Math.min(bufferSize, size));
                                file.write(buffer, 0, Math.min(bufferSize, size));
                                size -= bufferSize;
                            }
//                            notification(sender);

                            // hiển thị file
                            chatClient.showFile(sender[0], sender[1], fileName, file.toByteArray(), false, new FileListener(), chatClient.getReceiverMessage1().getText());
                            if (!sender[1].equals(chatClient.getReceiverMessage1().getText())) {
                                chatClient.addNotification(sender[1]);
                            }
                        } //  -----------------------------------------------------------------------------------------------------------
                        else if (method.equals("thoat")) {
                            // Thông báo có thể thoát
                            System.out.println(method + "thoat");
                            chatClient.dispose();
                            loginView = new Login();
                            signupView = new Signup();
                            loginView.addAction(new AccessListener());
                            signupView.addAction(new AccessListener());
                            loginView.setVisible(true);
                            signupView.setVisible(false);
                            if (dos != null) {
                                dos.close();
                            }
                            if (dis != null) {
                                dis.close();
                            }
                            break;
                        }

                    }

                } catch (IOException ex) {
                    System.err.println(ex);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(ClientControl.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    try {
                        if (dis != null) {
                            dis.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
        );

        t.start();
    }

    public Login getLoginView() {
        return loginView;
    }

    public void setLoginView(Login loginView) {
        this.loginView = loginView;
    }

    public Signup getSignupView() {
        return signupView;
    }

    public void setSignupView(Signup signupView) {
        this.signupView = signupView;
    }

    public ChatClient getChatClient() {
        return chatClient;
    }

    public void setChatClient(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

//class xử lí sự kiện login sign up
    class AccessListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == loginView.getLogin()) {
                String username = loginView.getuName().getText();
                String pass = String.copyValueOf(loginView.getPass().getPassword());

                if (username.isEmpty() || pass.isEmpty()) {
                    loginView.showMessage("Vui lòng nhập đủ tài khoản và mật khẩu");
                    return;
                }
                user = new User(username, pass);
                String response = Login(user);
                System.out.println(response);
                // đăng nhập thành công thì server sẽ trả về  chuỗi "Log in successful"
                if (response.equals("Log in successful")) {

                    chatClient = new ChatClient(user, new ChatListener(), new WindowListener(), new ListActioner());
                    chatClient.addEmojinAction(new EmojinListener());
                    chatClient.setVisible(true);
                    receiveData();
                    loginView.dispose();
                } else {
                    loginView.showMessage("Tài khoản hoặc mật khẩu không chính xác");
                }
            } else if (e.getSource() == loginView.getSignup()) {
                loginView.setVisible(false);
//               
                signupView.setVisible(true);
            } else {
                String username = signupView.getUname2().getText();
                String pass = String.copyValueOf(signupView.getPassSignup().getPassword());
                String pass1 = String.copyValueOf(signupView.getPassSignup1().getPassword());
                if (username.isEmpty() || pass.isEmpty() || pass1.isEmpty()) {
                    signupView.showMessage("Vui lòng nhập đủ thông tin");
                    return;
                }
                System.out.println(pass + "   " + pass1);
                if (!pass.equals(pass1)) {
                    signupView.getPassSignup1().setText("");
                    signupView.showMessage("Mật khẩu nhập lại không chính xác");

                    return;
                }
                user = new User(username, pass);
                String response = Signup(user);
                if (response.equals("pass")) {
                    System.out.println("thanh cong");
                    System.out.println(pass);

                    chatClient = new ChatClient(user, new ChatListener(), new WindowListener(), new ListActioner());
                    chatClient.addEmojinAction(new EmojinListener());
                    chatClient.setVisible(true);
                    receiveData();
                    loginView.dispose();
                    signupView.dispose();
                } else {
                    signupView.showMessage("Tài khoản này đã được sử dụng");
                }

            }

        }

    }

//class xử lí các xự kiện khi chat
    class ChatListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
//            click nut logouut
            if (e.getSource() == chatClient.getLogout()) {
                try {

                    oos.writeObject(new Message("logout", "", ""));
                    oos.flush();
//                    
                } catch (IOException ex) {
                    Logger.getLogger(ClientControl.class.getName()).log(Level.SEVERE, null, ex);
                }
// send text chat
            } else if (e.getSource() == chatClient.getSendText()) {
                if (!chatClient.getListUser().isSelectionEmpty()) {
                    Message message = new Message("text", chatClient.getReceiverMessage().getText(), chatClient.getTxtMessage().getText());

                    try {
                        oos.writeObject(message);
                        oos.flush();
                        //            System.out.println(user.getUsername() + " toi " + message.getNameReceiver() + ":" + message.getContent());
                    } catch (IOException e1) {
                        e1.printStackTrace();
//                    newMessage("ERROR", "Network error!", true);
                    }

                    // In ra tin nhắn lên màn hình chat với người nhận
                    chatClient.ShowMessage(user.getUsername(), chatClient.getReceiverMessage().getText(), message.getContent(), true, chatClient.getReceiverMessage().getText());
                    chatClient.getTxtMessage().setText("");
                }
//                send emoji
            } else if (e.getSource() == chatClient.getBtnEmoji()) {

                if (!chatClient.getListUser().isSelectionEmpty()) {
                    chatClient.getjDialog1().setVisible(true);
                    chatClient.setSendEnmoji(0);
                }
            } else if (e.getSource() == chatClient.getFile()) {
                if (!chatClient.getListUser().isSelectionEmpty()) {

                    // Hiển thị cửa sổ để chọn file
                    JFileChooser fileChooser = new JFileChooser();
                    int k = fileChooser.showOpenDialog(null);
                    if (k == JFileChooser.APPROVE_OPTION) {
                        byte[] file = new byte[(int) fileChooser.getSelectedFile().length()];
                        BufferedInputStream bis;
                        try {
                            bis = new BufferedInputStream(new FileInputStream(fileChooser.getSelectedFile()));
                            // Đọc file vào mảng file[]
                            bis.read(file, 0, file.length);
                            String receiver = chatClient.getReceiverMessage().getText();
                            String content = fileChooser.getSelectedFile().getName() + "," + String.valueOf(file.length);
                            int size = file.length;
                            int bufferSize = 1024;
                            int offset = 0;
//                            gửi thông tin
                            Message message = new Message("file", receiver, content);
                            oos.writeObject(message);

                            System.out.println("gui file " + receiver + " " + content);

                            // Lần lượt gửi từng luồng dữ liệu cho server tới khi hết
                            while (size > 0) {
                                dos.write(file, offset, Math.min(size, bufferSize));
                                offset += Math.min(size, bufferSize);
                                size -= bufferSize;
                            }
                            oos.flush();

                            dos.flush();

                            bis.close();

                            // In ra màn hình file
                            chatClient.showFile(user.getUsername(), fileChooser.getSelectedFile().getName(), file, true, new FileListener());
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }

//     --------------------------------------- xử lí sự kiện room----------------------------------------------------------------
//           xự kiện tạo nhóm vaf tham gia nhom
            } else if (e.getSource() == chatClient.getCreateRoom()) {
                chatClient.getReadRoom().setVisible(true);
                chatClient.setAction(0);
            } else if (e.getSource() == chatClient.getJoin()) {
                chatClient.getReadRoom().setVisible(true);
                chatClient.setAction(1);
            } else if (e.getSource() == chatClient.getBtnRoom()) {
                String name = chatClient.getTxtRoom().getText();
                chatClient.getTxtRoom().setText("");
                if (name.isEmpty()) {
                    chatClient.showMess("Nhập đầy đủ tên phòng");
                } else {
                    chatClient.getReadRoom().setVisible(false);
                    Message message;
                    if (chatClient.getAction() == 1) {

                        message = new Message("join", "", name);
                    } else {
                        System.out.println("2345678905678");
                        message = new Message("createRoom", "", name);

                    }
                    try {

                        oos.writeObject(message);
                        oos.flush();

                    } catch (IOException ex) {
                        Logger.getLogger(ClientControl.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            } //            -----------------------------------------xự kiện nhắn tin----------------------------------
            else if (e.getSource() == chatClient.getSendText1()) {
//                gửi text room
                if (!chatClient.getListUser1().isSelectionEmpty()) {
                    Message message = new Message("text-group", chatClient.getReceiverMessage1().getText(), chatClient.getTxtMessage1().getText());
                    try {
                        oos.writeObject(message);
                        oos.flush();
                        System.out.println(user.getUsername() + " toi " + message.getNameReceiver() + ":" + message.getContent());
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }

                    // In ra tin nhắn lên màn hình chat với người nhận
//                    chu thich
                    chatClient.ShowMessage(user.getUsername(), chatClient.getReceiverMessage1().getText(), message.getContent(), true, chatClient.getReceiverMessage1().getText());
                    chatClient.getTxtMessage1().setText("");
                }
            } //emoji room
            else if (e.getSource() == chatClient.getBtnEmoji1()) {

                if (!chatClient.getListUser1().isSelectionEmpty()) {
                    chatClient.getjDialog1().setVisible(true);
                    chatClient.setSendEnmoji(1);
                }
            } else if (e.getSource() == chatClient.getFile1()) {
                if (!chatClient.getListUser1().isSelectionEmpty()) {

                    // Hiển thị cửa sổ để chọn file
                    JFileChooser fileChooser = new JFileChooser();
                    int k = fileChooser.showOpenDialog(null);
                    if (k == JFileChooser.APPROVE_OPTION) {
                        byte[] file = new byte[(int) fileChooser.getSelectedFile().length()];
                        BufferedInputStream bis;
                        try {
                            bis = new BufferedInputStream(new FileInputStream(fileChooser.getSelectedFile()));
                            // Đọc file vào mảng file[]
                            bis.read(file, 0, file.length);
                            String receiver = chatClient.getReceiverMessage1().getText();
                            String content = fileChooser.getSelectedFile().getName() + "," + String.valueOf(file.length);
                            int size = file.length;
                            int bufferSize = 1024;
                            int offset = 0;
//                            gửi thông tin
                            Message message = new Message("file-group", receiver, content);
                            oos.writeObject(message);

                            System.out.println("gui file " + receiver + " " + content);

                            // Lần lượt gửi từng luồng dữ liệu cho server tới khi hết
                            while (size > 0) {
                                dos.write(file, offset, Math.min(size, bufferSize));
                                offset += Math.min(size, bufferSize);
                                size -= bufferSize;
                            }
                            oos.flush();

                            dos.flush();

                            bis.close();

                            // In ra màn hình file
                            chatClient.showFile(user.getUsername(), user.getUsername(), fileChooser.getSelectedFile().getName(), file, true, new FileListener(), chatClient.getReceiverMessage1().getText());
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            } else if (e.getSource() == chatClient.getBtnNotification()) {
                chatClient.getNotiForm().setVisible(true);
                chatClient.getBtnNotification().setText("Thông báo");
            }

        }

//
    }

//    xử lí sự kiện click list
    class ListActioner implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (e.getSource() == chatClient.getListUser()) {
                if (chatClient.getListUser().getSelectedValue() != null) {
                    String nameReceiver = chatClient.getListUser().getSelectedValue();

                    chatClient.getReceiverMessage().setText(nameReceiver);

                } else {
                    chatClient.getReceiverMessage().setText("No Message");
                }

                chatClient.setChatDefault(chatClient.getChatMaps().get(chatClient.getReceiverMessage().getText()));
                chatClient.getScrollChat().setViewportView(chatClient.getChatDefault());
            } else {
//                if (chatClient.getListUser1().getSelectedValue() != null) {
//                    String[] l = chatClient.getListUser().getSelectedValue().split("-");
//                    String nameReceiver = l[0];
//                    if (l.length > 1) {
//                        chatClient.getListModel().setElementAt(nameReceiver, chatClient.getListUser().getSelectedIndex());
//                    }
//                    chatClient.getReceiverMessage().setText(nameReceiver);
//
//                } else {
//                     chatClient.getReceiverMessage().setText(nameReceiver);
//                }
//------------------------------------------xử lí room--------------------------------------------------------------
                System.out.println("clicl0000000000000000000000000");
                System.out.println(chatClient.getChatMaps().size());
                String room = chatClient.getListUser1().getSelectedValue();
                System.out.println("rôm " + room + "  " + chatClient.getListModel1().getSize());
                chatClient.getReceiverMessage1().setText(room);
                chatClient.setChatDefault1(chatClient.getChatMaps().get(chatClient.getReceiverMessage1().getText()));
                chatClient.getScrollChat1().setViewportView(chatClient.getChatDefault1());
            }
        }

    }
// xử lí sự kiện đóng cửa sổ

    class WindowListener extends WindowAdapter {

        @Override
        public void windowClosing(WindowEvent e) {

            try {
//                dos.writeUTF("Log out");
//                dos.flush();
                oos.writeObject(new Message("logout", "", ""));
                oos.flush();

            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }
    }

//    xử lí gửi emojin
    class EmojinListener implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent e) {
            chatClient.getjDialog1().setVisible(false);

            JLabel label = (JLabel) e.getSource();
            String[] emojins = label.getIcon().toString().split("/");
            int l = emojins.length;

            String emojin = "src/" + emojins[l - 2] + "/" + emojins[l - 1];
            System.out.println(emojin);

            if (chatClient.getSendEnmoji() == 0) {
                if (!chatClient.getListUser().isSelectionEmpty()) {

                    try {
                        Message message = new Message("emoji", chatClient.getReceiverMessage().getText(), emojin);
                        oos.writeObject(message);
                        oos.flush();
                    } catch (IOException e1) {
                        e1.printStackTrace();

                    }

                    // In Emoji lên màn hình chat với người nhận
                    chatClient.showEmojin(user.getUsername(), user.getUsername(), emojin, true, chatClient.getReceiverMessage().getText());

                }
            } //            emoji room
            else {
                if (!chatClient.getListUser1().isSelectionEmpty()) {

                    try {
                        Message message = new Message("emoji-group", chatClient.getReceiverMessage1().getText(), emojin);
                        oos.writeObject(message);
                        oos.flush();
                    } catch (IOException e1) {
                        e1.printStackTrace();

                    }

                    // In Emoji lên màn hình chat với người nhận
                    chatClient.showEmojin(user.getUsername(), chatClient.getReceiverMessage1().getText(), emojin, true, chatClient.getReceiverMessage1().getText());

                }
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }
    }

    class FileListener extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {
            System.out.println("------------------------------------------------------");
            StyledDocument doc = chatClient.getChatMaps().get(chatClient.getChatWindow()).getStyledDocument();
            Element ele = doc.getCharacterElement(chatClient.getChatDefault().viewToModel(e.getPoint()));
            AttributeSet as = ele.getAttributes();
            FileDownload listener = (FileDownload) as.getAttribute("link");
            if (listener != null) {
                listener.execute();
            }
        }
    }

}
