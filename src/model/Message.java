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
public class Message implements Serializable{
    private String type;
    private String nameReceiver;
    private String content;

    public Message() {
    }

    public Message(String type, String nameReceiver, String content) {
        this.type = type;
        this.nameReceiver = nameReceiver;
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNameReceiver() {
        return nameReceiver;
    }

    public void setNameReceiver(String nameReceiver) {
        this.nameReceiver = nameReceiver;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

   
    
    
}
