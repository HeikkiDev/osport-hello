package com.proyecto.enrique.osporthello;

/**
 * Created by enrique on 25/03/16.
 */
public class Chat {

    private int id;
    private String receiver_email;
    private String receiver_name;
    private String getReceiver_image;

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

    public String getGetReceiver_image() {
        return getReceiver_image;
    }

    public void setGetReceiver_image(String getReceiver_image) {
        this.getReceiver_image = getReceiver_image;
    }
}
