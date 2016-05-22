package com.proyecto.enrique.osporthello.Models;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by enrique on 7/05/16.
 */
public class SportActivityInfo implements Serializable, Parcelable{

    private int _id;
    private String email;
    private String userName;
    private Bitmap userImage;
    private String name;
    private String date;
    private int sportType;
    private int distanceUnits;
    private int speedUnits;
    private double avgSpeed;
    private double distanceKms;
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Bitmap getUserImage() {
        return userImage;
    }

    public void setUserImage(Bitmap userImage) {
        this.userImage = userImage;
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

    public int getSportType() {
        return sportType;
    }

    public void setSportType(int sportType) {
        this.sportType = sportType;
    }

    public int getDistanceUnits() {
        return distanceUnits;
    }

    public void setDistanceUnits(int distanceUnits) {
        this.distanceUnits = distanceUnits;
    }

    public int getSpeedUnits() {
        return speedUnits;
    }

    public void setSpeedUnits(int speedUnits) {
        this.speedUnits = speedUnits;
    }

    public double getAvgSpeed() {
        return avgSpeed;
    }

    public void setAvgSpeed(double avgSpeed) {
        this.avgSpeed = avgSpeed;
    }

    public double getDistanceKms() {
        return distanceKms;
    }

    public void setDistanceKms(double distanceKms) {
        this.distanceKms = distanceKms;
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
            , int sportType, int distanceU, int speedU, String geoPoints){
        this._id = id;
        this.email = email;
        this.name = name;
        this.date = date;
        this.avgSpeed = avgSpeed;
        this.calories = calories;
        this.durationMillis = duration;
        this.distanceKms = distance;
        this.sportType = sportType;
        this.distanceUnits = distanceU;
        this.speedUnits = speedU;
        this.encodedPointsList = geoPoints;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        //
    }
}
