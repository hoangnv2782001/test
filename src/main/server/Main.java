/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.server;

import controller.server.Server;
import server.ServerView;

/**
 *
 * @author Administrator
 */
public class Main {

    public static void main(String[] args) {
        ServerView serverView = new ServerView();
        Server server = new Server(serverView);

    }
}
