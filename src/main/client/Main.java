/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.client;

import client.Login;
import client.Signup;
import controller.client.ClientControl;

/**
 *
 * @author Administrator
 */
public class Main {

    public static void main(String[] args) {
        Login loginView = new Login();
        Signup signup = new Signup();
        ClientControl cc = new ClientControl(loginView, signup);
        loginView.setVisible(true);
        signup.setVisible(false);

    }
}
