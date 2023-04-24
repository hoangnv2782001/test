package controller.server;

import controller.server.dao.UserDAO;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Message;
import model.User;
import server.ServerView;

/**
 *
 * @Created by DELL - StudentID: 18120652
 * @Date Jul 8, 2020 - 4:13:42 PM
 * @Description ...
 */
public class Server {

    private Object lock;
    private ServerSocket s;
    private Socket socket;
    private List<ServerThread> users = new ArrayList<ServerThread>();
    private UserDAO userDao;
    private int onlineUser;
    private ServerView serverView;

//    room
    private List<RoomThread> rooms;

    /**
     * Tải lên danh sách tài khoản từ file
     */
    public Server(ServerView serverView) {

        this.serverView = serverView;
        this.serverView.setVisible(true);
        onlineUser = 0;
        rooms = new ArrayList<>();
        try {
            listening();
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void getAllUsers() {
        List<User> userList= userDao.selectAllUsers();

        for (User u : userList) {
            users.add(new ServerThread(u, false, lock, this));
        }
    }

    public void listening() throws IOException {
        try {

            userDao = new UserDAO();
            // dung de dong bo hoa cac phuong thuc
            lock = new Object();
            // Đọc danh sách tài khoản có trong hệ thống
            this.getAllUsers();
//            hiển thị các thông tin phòng ,user onlines
            serverView.showNumberUsers(users.size(), onlineUser);
            serverView.showTableUser(users);
            serverView.showNumberRoom(0);

            // Socket dùng để xử lý các yêu cầu đăng nhập/đăng ký từ user
            s = new ServerSocket(9911);

            while (true) {
                // Đợi request đăng nhập/đăng xuất từ client

                System.out.println("chờ kết nối");

                socket = s.accept();

                DataInputStream dis = new DataInputStream(socket.getInputStream());
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                System.out.println("xong tream");
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                System.out.println("xong object stream");

                // Đọc yêu cầu đăng nhập/đăng xuất
                String request = dis.readUTF();
                System.out.println(request);
                if (request.equals("Log in")) {
                    User user = (User) ois.readObject();

                    // Kiểm tra tên đăng nhập có tồn tại hay không
                    if (isExisted(user) == true) {
                        for (ServerThread usThread : users) {
                            if (usThread.getUser().getUsername().equals(user.getUsername())) {
                                // Kiểm tra mật khẩu có trùng khớp không
                                if (user.getPass().equals(usThread.getUser().getPass())) {

                                    // Thông báo đăng nhập thành công cho người dùng
                                    dos.writeUTF("Log in successful");
                                    dos.flush();

                                    // Tạo ServerThread mới để giải quyết các request từ user này
                                    ServerThread uServerThread = usThread;
                                    uServerThread.setOnline(socket, oos, ois,true);
                                    onlineUser++;
//                        cập nhật số user
                                    serverView.showNumberUsers(users.size(), onlineUser);

//                        hiển thị danh sách
                                    serverView.showTableUser(users);

                                    // Tạo một Thread để giao tiếp với user này
                                    Thread t = new Thread(uServerThread);
                                    t.start();

                                    // Gửi thông báo cho các client đang online cập nhật danh sách người dùng trực tuyến
                                    updateOnlineUsers();
                                    System.out.println("ok");
                                } else {
                                    dos.writeUTF("Password is not correct");
                                    dos.flush();
                                }
                                break;
                            }
                        }

                    } else {
                        dos.writeUTF("This username is not exist");
                        dos.flush();
                    }
                } else if (request.equals("Sign up")) {

                    User user = (User) ois.readObject();

                    // Kiểm tra tên đăng nhập đã tồn tại hay chưa
                    if (isExisted(user) == false) {

                        // Tạo một ServerThread để giải quyết các request từ user này
                        ServerThread serverThread = new ServerThread(socket, user, true, lock, this,oos,ois);
                        users.add(serverThread);

                        // Lưu danh sách tài khoản xuống file và gửi thông báo đăng nhập thành công cho user
                        userDao.insertUser(user);
                        dos.writeUTF("pass");
                        dos.flush();
                        onlineUser++;
//                        cập nhật số user

                        serverView.showNumberUsers(users.size(), onlineUser);

//                        hiển thị danh sách
                        serverView.showTableUser(users);
                        // Tạo một Thread để giao tiếp với user này
                        Thread t = new Thread(serverThread);
                        t.start();

                        // Gửi thông báo cho các client đang online cập nhật danh sách người dùng trực tuyến
                        updateOnlineUsers();
                    } else {

                        // Thông báo đăng kí thất bại
                        dos.writeUTF("fail");
                        dos.flush();
                    }
                }
            }

        } catch (Exception ex) {
            System.err.println(ex);
        } finally {
            if (s != null) {
                s.close();
            }
        }
    }
 
    
//    kiểm tra usename tồn tại hay chưa
    public boolean isExisted(User user) {
        for (ServerThread client : users) {
            if (client.getUser().getUsername().equals(user.getUsername())) {
                return true;
            }
        }
        return false;
    }

    /*
	 * Gửi yêu cầu các user đang online cập nhật lại danh sách người dùng trực tuyến
	 * Được gọi mỗi khi có 1 user online hoặc offline
     */
    public  void updateOnlineUsers() {
//        String message = " ";
        System.out.println("hoang sep djj");
        List<User> us = new ArrayList<>();
        for (ServerThread client : users) {
            if (client.isOnline()== true) {
                us.add(client.getUser());
            }
        }
        for (ServerThread client : users) {
            if (client.isOnline()== true) {
                try {
                    Message message = new Message("Update users", "", "");

                    client.getOos().writeObject(message);
                    client.getOos().writeObject(us);
                    System.out.println("dfghjksdfghjkdfghjkdfg");
                    client.getOos().flush();
                    client.getDos().flush();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    System.out.println("khong cos dos");

                }
            }
        }
    }

    public int getOnlineUser() {
        return onlineUser;
    }

    public void setOnlineUser(int onlineUser) {
        this.onlineUser = onlineUser;
    }

    public ServerView getServerView() {
        return serverView;
    }

    public void setServerView(ServerView serverView) {
        this.serverView = serverView;
    }

    public List<ServerThread> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<ServerThread> users) {
        this.users = users;
    }


    public List<RoomThread> getRooms() {
        return rooms;
    }

    public void setRooms(List<RoomThread> rooms) {
        this.rooms = rooms;
    }


}

/**
 * Luồng riêng dùng để giao tiếp với mỗi user
 */
