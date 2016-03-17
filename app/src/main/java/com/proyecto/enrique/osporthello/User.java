package com.proyecto.enrique.osporthello;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by enrique on 14/03/16.
 */
public class User implements Serializable {

    @SerializedName("User_email")
    private String email;
    @SerializedName("User_firstname")
    private String firstname;
    @SerializedName("User_lastname")
    private String lastname;
    @SerializedName("User_image")
    private byte[] image;
    @SerializedName("User_apiKey")
    private String apiKey;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public User(){
        //
    }

    public User(String email, String first, String last, byte[] image, String apiKey){
        this.email = email;
        this.firstname = first;
        this.lastname = last;
        this.image = image;
        this.apiKey = apiKey;
    }
}
