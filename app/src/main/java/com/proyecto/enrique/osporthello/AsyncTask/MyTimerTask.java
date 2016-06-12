package com.proyecto.enrique.osporthello.AsyncTask;

import android.os.SystemClock;
import android.widget.TextView;

import java.util.TimerTask;

import static com.google.android.gms.internal.zzir.runOnUiThread;

/**
 * Autor: Enrique Ramos
 * Fecha última actualización: 12/06/2016
 * Descripción: AsyncTask que inicia un cronómetro y actualiza un TextView a medida que avanza el cronómetro.
 */

public class MyTimerTask extends TimerTask {

    long result;
    long timeStart;
    TextView txtTime;

    public MyTimerTask(long time, TextView txt){
        this.timeStart = time;
        this.txtTime = txt;
    }

    @Override
    public void run() {
        runOnUiThread(new Runnable() {
            public void run() {
                result = SystemClock.uptimeMillis() - timeStart;
                int hours = (int)result / 3600000;
                int minutes = (int) (result % 3600000) / 60000;
                int seconds = (int) ((result % 3600000) % 60000) / 1000 ;
                txtTime.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
            }
        });
    }

    public long getFinalDuration(){
        return this.result;
    }
}
