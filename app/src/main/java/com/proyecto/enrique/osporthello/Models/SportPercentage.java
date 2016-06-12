package com.proyecto.enrique.osporthello.Models;

import java.io.Serializable;

/**
 * Autor: Enrique Ramos
 * Fecha última actualización: 12/06/2016
 * Descripción: Modelo para el porcentaje que se realiza de cada actividad deportiva
 * que se muestra en el gráfico circular.
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
