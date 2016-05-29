package com.proyecto.enrique.osporthello.Models;

import java.io.Serializable;

/**
 * Created by enrique on 28/05/16.
 */
public class StatisticsTotal implements Serializable{

    private double kms_total;
    private double miles_total;
    private int duration_total; //millis
    private int calories_total;

    public double getKms_total() {
        return kms_total;
    }

    public void setKms_total(double kms_total) {
        this.kms_total = kms_total;
    }

    public double getMiles_total() {
        return miles_total;
    }

    public void setMiles_total(double miles_total) {
        this.miles_total = miles_total;
    }

    public int getDuration_total() {
        return duration_total;
    }

    public void setDuration_total(int duration_total) {
        this.duration_total = duration_total;
    }

    public int getCalories_total() {
        return calories_total;
    }

    public void setCalories_total(int calories_total) {
        this.calories_total = calories_total;
    }

    public StatisticsTotal(){
        //
    }
    public StatisticsTotal(double kms, double miles, int duration, int calories){
        this.kms_total = kms;
        this.miles_total = miles;
        this.duration_total = duration;
        this.calories_total = calories;
    }
}
