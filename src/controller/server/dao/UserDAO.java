/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller.server.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.User;

/**
 *
 * @author Administrator
 */
public class UserDAO {

    public UserDAO() {
        getConnection();
    }

    protected Connection getConnection() {
        Connection connection = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/ltm?useSSL=false", "root", "1234");
            System.out.println("Thanh cong");
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            System.out.println("Khoong thanh cong");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return connection;
    }

    private boolean checkUser(User user) throws Exception {
        String query = "Select * FROM users WHERE username = ? AND password = ?";
        try {
            Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getPass());
            ResultSet rs = preparedStatement.executeQuery(query);
            if (rs.next()) {
                return true;
            }
        } catch (Exception e) {
            throw e;

        }
        return false;
    }

    public List< User> selectAllUsers() {

       
        List< User> users = new ArrayList<>();
      
        try (
                Connection connection = getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement("select * from users");) {
            System.out.println(preparedStatement);
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
              
                String name = rs.getString("username");
                String pass = rs.getString("password");
               
                users.add(new User(name, pass));
            }
        } catch (SQLException e) {
         
        }
        return users;
    }
      public void insertUser(User user) throws SQLException {
      
        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO users VALUES (?,?) ")) {
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getPass());
           
            System.out.println(preparedStatement);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("e");
        }
    }

}
