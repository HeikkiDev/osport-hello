package com.proyecto.enrique.osporthello;

import java.io.Serializable;

/**
 * Created by enrique on 25/03/16.
 */
public class Chat implements Serializable{

    private int id;
    private String receiver_email;
    private String receiver_name;
    private String receiver_image;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getReceiver_email() {
        return receiver_email;
    }

    public void setReceiver_email(String receiver_email) {
        this.receiver_email = receiver_email;
    }

    public String getReceiver_name() {
        return receiver_name;
    }

    public void setReceiver_name(String receiver_name) {
        this.receiver_name = receiver_name;
    }

    public String getReceiver_image() {
        return receiver_image;
    }

    public void setReceiver_image(String receiver_image) {
        this.receiver_image = receiver_image;
    }

    public Chat(){

    }
    public Chat(int id, String receiver, String username, String image){
        this.id = id;
        this.receiver_email = receiver;
        this.receiver_name = username;
        this.receiver_image = image;
    }
}
