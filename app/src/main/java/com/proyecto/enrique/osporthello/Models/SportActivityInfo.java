package com.proyecto.enrique.osporthello.Models;

import java.io.Serializable;

/**
 * Created by enrique on 7/05/16.
 */
public class SportActivityInfo implements Serializable{

    private int _id;
    private String email;
    private String name;
    private String date;
    private String sportType;
    private String distanceUnits;
    private String speedUnits;
    private double avgSpeed;
    private double distanceMetres;
    private long durationMillis;
    private int calories;
    private String encodedPointsList;

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSportType() {
        return sportType;
    }

    public void setSportType(String sportType) {
        this.sportType = sportType;
    }

    public String getDistanceUnits() {
        return distanceUnits;
    }

    public void setDistanceUnits(String distanceUnits) {
        this.distanceUnits = distanceUnits;
    }

    public String getSpeedUnits() {
        return speedUnits;
    }

    public void setSpeedUnits(String speedUnits) {
        this.speedUnits = speedUnits;
    }

    public double getAvgSpeed() {
        return avgSpeed;
    }

    public void setAvgSpeed(double avgSpeed) {
        this.avgSpeed = avgSpeed;
    }

    public double getDistanceMetres() {
        return distanceMetres;
    }

    public void setDistanceMetres(double distanceMetres) {
        this.distanceMetres = distanceMetres;
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    public void setDurationMillis(long durationMillis) {
        this.durationMillis = durationMillis;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public String getEncodedPointsList() {
        return encodedPointsList;
    }

    public void setEncodedPointsList(String encodedPointsList) {
        this.encodedPointsList = encodedPointsList;
    }

    // Constructors
    public SportActivityInfo(){
        //
    }
    public SportActivityInfo(int id, String email, String name, String date, double avgSpeed, int calories, long duration, double distance
            , String sportType, String distanceU, String speedU, String geoPoints){
        this._id = id;
        this.email = email;
        this.name = name;
        this.date = date;
        this.avgSpeed = avgSpeed;
        this.calories = calories;
        this.durationMillis = duration;
        this.distanceMetres = distance;
        this.sportType = sportType;
        this.distanceUnits = distanceU;
        this.speedUnits = speedU;
        this.encodedPointsList = geoPoints;
    }
}
