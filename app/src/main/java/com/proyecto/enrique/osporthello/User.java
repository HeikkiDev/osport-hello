package com.proyecto.enrique.osporthello;

import android.graphics.Bitmap;

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
    private String image;
    @SerializedName("User_apiKey")
    private String apiKey;
    private String sex;
    private String age;
    private String city;
    private String weight;
    private String height;

    public String getEmail() {return email;}

    public void setEmail(String email) {this.email = email;}

    public String getFirstname() {return firstname;}

    public void setFirstname(String firstname) {this.firstname = firstname;}

    public String getLastname() {return lastname;}

    public void setLastname(String lastname) {this.lastname = lastname;}

    public String getImage() {return image;}

    public void setImage(String image) {this.image = image;}

    public String getApiKey() {return apiKey;}

    public void setApiKey(String apiKey) {this.apiKey = apiKey;}

    public String getSex() {return sex;}

    public void setSex(String sex) {this.sex = sex;}

    public String getAge() {return age;}

    public void setAge(String age) {this.age = age;}

    public String getCity() {return city;}

    public void setCity(String city) {this.city = city;}

    public String getWeight() {return weight;}

    public void setWeight(String weight) {this.weight = weight;}

    public String getHeight() {return height;}

    public void setHeight(String height) {this.height = height;}

    public User(){
        //
    }

    public User(String email, String first, String last, String image, String apiKey){
        this.email = email;
        this.firstname = first;
        this.lastname = last;
        this.image = image;
        this.apiKey = apiKey;
    }
}
