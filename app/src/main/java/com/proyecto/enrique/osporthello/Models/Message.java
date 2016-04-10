package com.proyecto.enrique.osporthello.Models;

import java.io.Serializable;

/**
 * Created by enrique on 10/03/16.
 */
public class Message implements Serializable{

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    private String author;
    private String date;
    private String hour;
    private String text;

    public Message(){

    }

    public Message(String author, String date, String hour, String text){
        this.author = author;
        this.date = date;
        this.hour = hour;
        this.text = text;
    }
}
