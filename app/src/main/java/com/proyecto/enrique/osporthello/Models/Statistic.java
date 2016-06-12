package com.proyecto.enrique.osporthello.Models;

import java.io.Serializable;

/**
 * Autor: Enrique Ramos
 * Fecha última actualización: 12/06/2016
 * Descripción: Modelo de datos de estadísticas que se muestran en el gráfico de barras.
 */

public class Statistic implements Serializable{

    private int date;
    private double distance_kms;
    private double distance_miles;

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public double getDistance_kms() {
        return distance_kms;
    }

    public void setDistance_kms(double distance_kms) {
        this.distance_kms = distance_kms;
    }

    public double getDistance_miles() {
        return distance_miles;
    }

    public void setDistance_miles(double distance_miles) {
        this.distance_miles = distance_miles;
    }

    public Statistic(){
        //
    }
    public Statistic(int dayOrMonth, double kms, double miles){
        this.date = dayOrMonth;
        this.distance_kms = kms;
        this.distance_miles = miles;
    }
}
