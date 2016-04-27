package com.proyecto.enrique.osporthello.Models;

import android.graphics.drawable.Drawable;

/**
 * Created by enrique on 27/04/16.
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
