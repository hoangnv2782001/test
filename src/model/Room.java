/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.Serializable;

/**
 *
 * @author Administrator
 */
public class Room implements Serializable {

    private String name;
    private int members;
    private User creater;

    public Room() {
    }

    public Room(String name, int members) {
        this.name = name;
        this.members = members;
    }

    public Room(String name, int members, User creater) {
        this.name = name;
        this.members = members;
        this.creater = creater;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMembers() {
        return members;
    }

    public void setMembers(int members) {
        this.members = members;
    }

    public User getCreater() {
        return creater;
    }

    public void setCreater(User creater) {
        this.creater = creater;
    }

}
