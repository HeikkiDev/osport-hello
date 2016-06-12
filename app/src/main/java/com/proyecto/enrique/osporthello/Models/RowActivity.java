package com.proyecto.enrique.osporthello.Models;

import android.graphics.drawable.Drawable;

/**
 * Autor: Enrique Ramos
 * Fecha última actualización: 12/06/2016
 * Descripción: Modelo de fila en el diálogo de elección de actividad deportiva.
 */

public class RowActivity {
    private Drawable image;
    private String name;

    public RowActivity(Drawable imageResource, String nameActivity){
        this.image = imageResource;
        this.name = nameActivity;
    }

    public Drawable getImage(){
        return this.image;
    }
    public String getName(){
        return this.name;
    }
}
