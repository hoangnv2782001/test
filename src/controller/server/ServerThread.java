/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller.server;

import controller.server.Server;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Message;
import model.Room;
import model.User;

/**
 *
 * @author Administrator
 */
public class ServerThread implements Runnable {
    private Object lock;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private User user;
    private boolean online;
    private Server server;

//	khởi tạo
    public ServerThread(Socket socket, User user, boolean online, Object lock, Server server, ObjectOutputStream oos, ObjectInputStream ois) throws IOException {
        this.socket = socket;
        this.user = user;
        this.dis = new DataInputStream(socket.getInputStream());
        this.dos = new DataOutputStream(socket.getOutputStream());
        this.online = online;
        this.lock = lock;
        this.server = server;
        this.oos = oos;
        this.ois = ois;

    }
//        khởi tạo không socket

    public ServerThread(User user, boolean online, Object lock, Server server) {
        this.user = user;
        this.online = online;
        this.lock = lock;
        this.server = server;
    }
// set login	

//	set socket và khởi tạo luồng

    public void setOnline(Socket socket, ObjectOutputStream oos, ObjectInputStream ois,boolean online) {
        this.socket = socket;
        try {
            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());

            System.out.println("thanh cong 1");
            this.oos = oos;

            System.out.println("thanh cong 2");
            this.ois = ois;
            this.online = online;

        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    public Socket getSocket() {
        return socket;
    }

//        get / set
    public boolean isOnline() {
        return online;
    }

    public boolean getOnline() {
        return this.online;
    }
    public DataOutputStream getDos() {
        return this.dos;
    }

    public DataInputStream getDis() {
        return dis;
    }

    public void setDis(DataInputStream dis) {
        this.dis = dis;
    }

    public ObjectInputStream getOis() {
        return ois;
    }

    public void setOis(ObjectInputStream ois) {
        this.ois = ois;
    }

    public ObjectOutputStream getOos() {
        return oos;
    }

    public void setOos(ObjectOutputStream oos) {
        this.oos = oos;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    @Override
    public void run() {

        while (true) {
            try {

                Message message = (Message) ois.readObject();
                System.out.println("thong dep-----------------------" + message.getType());

                // Yêu cầu đăng xuất từ user
                if (message.getType().equals("logout")) {
                    System.out.println(message);
                    // Thông báo cho user thoát thành công

                    Message message1 = new Message("thoat", "", "");
                    oos.writeObject(message1);

//                    gui thong bao out phong
                    for (RoomThread roomThread : server.getRooms()) {
                        RoomThread room_g = null;
                        System.out.println("grupp ");

                        for (ServerThread st : roomThread.getListMembers()) {
                            if (st.getUser().getUsername().equals(this.user.getUsername())) {
                                room_g = roomThread;
                                roomThread.getListMembers().remove(this);
                                roomThread.getRoom().setMembers(roomThread.getListMembers().size());
                                break;
                            }
                        }

                        if (room_g != null) {

                            for (ServerThread st : roomThread.getListMembers()) {
                                synchronized (lock) {
                                    if (!st.getUser().getUsername().equals(this.user.getUsername())) {
                                        Message message_g = new Message("text-group", this.user.getUsername() + "-" + roomThread.getRoom().getName(), "Đã thoát khỏi phòng");
                                        st.getOos().writeObject(message_g);
                                        st.getOos().flush();
                                        System.out.println(st.getUser().getUsername() + "   nguoi nhan  ");

                                    }
                                }
                            }

                        }

                    }
                    List<RoomThread> roomR = new ArrayList<>();
                    for (RoomThread roomThread : server.getRooms()) {
                        if (roomThread.getListMembers().size() == 0) {
                            roomR.add(roomThread);
                        }
                    }
                    server.getRooms().removeAll(roomR);
                    // Đóng socket và chuyển trạng thái thành offline
                    closeSocket();
                    this.online = false;

                    // Thông báo cho các user khác cập nhật danh sách người dùng trực tuyến
                    server.updateOnlineUsers();
                    server.setOnlineUser(server.getOnlineUser() - 1);
                    server.getServerView().showNumberUsers(server.getUsers().size(), server.getOnlineUser());
                    server.getServerView().showTableUser(server.getUsers());
                    server.getServerView().showNumberRoom(server.getRooms().size());
                    server.getServerView().showTableRoom(server.getRooms());

//                    thoat luong
                    break;
                } //-------------------------xử lí chat với bạn-----------------------------------------------------------
                // gửi text
                else if (message.getType().equals("text")) {
                    String nameReceiver = message.getNameReceiver();
                    String content = message.getContent();

                    for (ServerThread client : server.getUsers()) {
                        if (client.getUser().getUsername().equals(nameReceiver)) {
                            synchronized (lock) {
                                Message message1 = new Message("text", this.user.getUsername(), content);
                                client.getOos().writeObject(message1);
                                client.getOos().flush();
                                System.out.println(this.user.getUsername());
                                break;
                            }
                        }
                    }
                } // Yêu cầu gửi tin nhắn dạng Emoji
                else if (message.getType().equals("emoji")) {
                    System.out.println(message.getContent());

                    for (ServerThread client : server.getUsers()) {
                        if (client.getUser().getUsername().equals(message.getNameReceiver())) {
                            synchronized (lock) {
                                Message message1 = new Message("emoji", this.user.getUsername(), message.getContent());
                                client.getOos().writeObject(message1);
                                client.getOos().flush();
                                break;
                            }
                        }
                    }
                } // Yêu cầu gửi File
                else if (message.getType().equals("file")) {

                    // Đọc các thoong tin của tin nhắn gửi file
                    String receiver = message.getNameReceiver();
                    String[] content = message.getContent().split(",");
                    String filename = content[0];
                    int size = Integer.parseInt(content[1]);
                    System.out.println("server  file " + receiver + " " + content);
                    int bufferSize = 1024;
                    byte[] buffer = new byte[bufferSize];

                    for (ServerThread client : server.getUsers()) {
                        if (client.getUser().getUsername().equals(receiver)) {
                            synchronized (lock) {
                                Message message1 = new Message("file", this.getUser().getUsername(), filename + "," + String.valueOf(size));
                                client.getOos().writeObject(message1);
                                client.getOos().flush();
                                while (size > 0) {
                                    // Gửi lần lượt từng buffer cho người nhận cho đến khi hết file
                                    dis.read(buffer, 0, Math.min(size, bufferSize));
                                    client.getDos().write(buffer, 0, Math.min(size, bufferSize));
                                    size -= bufferSize;
                                }
                                client.getDos().flush();
                                break;
                            }
                        }
                    }
                } //               
                // ---------------------------------------------xử lí chat phòng----------------------------------------
                else if (message.getType().equals("createRoom")) {

                    String name = message.getContent();
                    boolean check = false;
                    for (RoomThread roomThread : server.getRooms()) {
                        if (roomThread.getRoom().getName().equals(name)) {
                            check = true;
                            break;
                        }
                    }
                    if (check) {
                        Message message1 = new Message("createRoom", "", "fail");
                        oos.writeObject(message1);
                        oos.flush();
                    } else {
                        Room room = new Room(name, 1, this.getUser());
                        RoomThread roomThread = new RoomThread(room);
                        roomThread.getListMembers().add(this);
                        server.getRooms().add(roomThread);
                        Message message1 = new Message("createRoom", "", "pass");
                        server.getServerView().showNumberRoom(server.getRooms().size());
                        server.getServerView().showTableRoom(server.getRooms());
                        oos.writeObject(message1);
                        oos.writeObject(room);
                        oos.flush();
                    }

                } else if (message.getType().equals("join")) {
                    String name = message.getContent();
                    System.out.println("sdfghjklsdfghjkldfghj" + name);
                    boolean check = true;
                    RoomThread room1 = null;
                    for (RoomThread roomThread : server.getRooms()) {
                        if (roomThread.getRoom().getName().equals(name)) {
                            synchronized (lock) {
                                roomThread.getListMembers().add(this);
                                Room room = roomThread.getRoom();
                                room.setMembers(room.getMembers() + 1);
                                roomThread.setRoom(room);
                                room1 = roomThread;
                                Message message1 = new Message("join", "", "pass");
                                oos.writeObject(message1);
                                oos.writeObject(room);
                                oos.flush();
                                check = false;
                            }
                            break;
                        }
                    }
                    if (check) {
                        Message message1 = new Message("join", "", "fail");
                        oos.writeObject(message1);
                        oos.flush();
                    } else {
//                        thong bao cos nguoi tham gia phong
                        if (room1 != null) {
                            for (ServerThread st : room1.getListMembers()) {
                                synchronized (lock) {
                                    if (!st.getUser().getUsername().equals(this.user.getUsername())) {
                                        Message message1 = new Message("text-group", this.user.getUsername() + "-" + room1.getRoom().getName(), "Đã tham vào phòng");
                                        st.getOos().writeObject(message1);
                                        st.getOos().flush();
                                        System.out.println(st.getUser().getUsername() + "   nguoi nhan  ");

                                    }
                                }
                            }
                            server.getServerView().showTableRoom(server.getRooms());
                        }
                    }
                } else if (message.getType().equals("text-group")) {
                    String room = message.getNameReceiver();
                    String content = message.getContent();
                    System.out.println("server " + user.getUsername() + " toi " + room + "     " + content);
                    for (RoomThread roomThread : server.getRooms()) {
                        if (roomThread.getRoom().getName().equals(room)) {
                            for (ServerThread st : roomThread.getListMembers()) {
                                synchronized (lock) {
                                    if (!st.getUser().getUsername().equals(this.user.getUsername())) {
                                        Message message1 = new Message("text-group", this.user.getUsername() + "-" + room, content);
                                        st.getOos().writeObject(message1);
                                        st.getOos().flush();
                                        System.out.println(st.getUser().getUsername() + "   nguoi nhan  ");

                                    }
                                }
                            }
                            break;
                        }
                    }

                } //                xử lí emiji room
                else if (message.getType().equals("emoji-group")) {
                    String room = message.getNameReceiver();
                    String content = message.getContent();
                    System.out.println("server " + user.getUsername() + " toi " + room + "     " + content);
                    for (RoomThread roomThread : server.getRooms()) {
                        if (roomThread.getRoom().getName().equals(room)) {
                            for (ServerThread st : roomThread.getListMembers()) {
                                synchronized (lock) {
                                    if (!st.getUser().getUsername().equals(this.user.getUsername())) {
                                        Message message1 = new Message("emoji-group", this.user.getUsername() + "-" + room, content);
                                        st.getOos().writeObject(message1);
                                        st.getOos().flush();
                                        System.out.println(st.getUser().getUsername() + "   nguoi nhan  ");

                                    }
                                }
                            }
                            break;
                        }
                    }

                } //                xử lí file group
                else if (message.getType().equals("file-group")) {

                    // Đọc các thoong tin của tin nhắn gửi file
                    String room = message.getNameReceiver();
                    String[] content = message.getContent().split(",");
                    String filename = content[0];
                    int size = Integer.parseInt(content[1]);
                    int bufferSize = 1024;
                    byte[] buffer = new byte[bufferSize];

//                    -------------------------------
                    for (RoomThread roomThread : server.getRooms()) {
//                        tìm phòng
                        if (roomThread.getRoom().getName().equals(room)) {
//                            gửi cho room
                            for (ServerThread st : roomThread.getListMembers()) {
                                synchronized (lock) {
                                    if (!st.getUser().getUsername().equals(this.user.getUsername())) {

                                        Message message1 = new Message("file-group", this.getUser().getUsername() + "-" + room, filename + "," + String.valueOf(size));
                                        st.getOos().writeObject(message1);
                                        st.getOos().flush();
                                        while (size > 0) {
                                            // Gửi lần lượt từng buffer cho người nhận cho đến khi hết file
                                            dis.read(buffer, 0, Math.min(size, bufferSize));
                                            st.getDos().write(buffer, 0, Math.min(size, bufferSize));
                                            size -= bufferSize;
                                        }
                                        st.getDos().flush();
                                    }

                                }
                            }
                            break;
                        }
                    }

                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

    }

    //ddongs kết nối
    public void closeSocket() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
