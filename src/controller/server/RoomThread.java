/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller.server;

import java.util.ArrayList;
import java.util.List;
import model.Room;

/**
 *
 * @author Administrator
 */
public class RoomThread {
    private Room room;
    private List<ServerThread> listMembers;

    public RoomThread() {
    }

    public RoomThread(Room room) {
        this.room = room;
        listMembers = new ArrayList<>();
    }

    public RoomThread(Room room, List<ServerThread> listMembers) {
        this.room = room;
        this.listMembers = listMembers;
    }
    

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public List<ServerThread> getListMembers() {
        return listMembers;
    }

    public void setListMembers(List<ServerThread> listMembers) {
        this.listMembers = listMembers;
    }
    
    
}
