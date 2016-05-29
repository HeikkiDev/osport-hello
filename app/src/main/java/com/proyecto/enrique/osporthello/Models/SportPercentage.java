package com.proyecto.enrique.osporthello.Models;

import java.io.Serializable;

/**
 * Created by enrique on 28/05/16.
 */
public class SportPercentage implements Serializable{
    private int sportType;
    private double sportPercentage;

    public int getSportType() {
        return sportType;
    }

    public void setSportType(int sportType) {
        this.sportType = sportType;
    }

    public double getSportPercentage() {
        return sportPercentage;
    }

    public void setSportPercentage(double sportPercentage) {
        this.sportPercentage = sportPercentage;
    }

    public SportPercentage(){
        //
    }
    public SportPercentage(int type, double percentage){
        this.sportType = type;
        this.sportPercentage = percentage;
    }
}
