package com.proyecto.enrique.osporthello.Models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Autor: Enrique Ramos
 * Fecha última actualización: 12/06/2016
 * Descripción: Modelo de usuario que aparece en la lista de GeoBúsqueda.
 */

public class GeoSearch implements Serializable{

    @SerializedName("User_email")
    private String email;
    @SerializedName("User_firstname")
    private String firstname;
    @SerializedName("User_lastname")
    private String lastname;
    @SerializedName("User_city")
    private String city;
    @SerializedName("Distance")
    private double distance;
    private String image;

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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    // Constructor
    public GeoSearch(){
        //
    }
    public GeoSearch(String email, String first, String lastname, String city, double distance){
        this.email = email;
        this.firstname = first;
        this.lastname = lastname;
        this.city = city;
        this.distance = distance;
    }
}
