package com.proyecto.enrique.osporthello.AsyncTask;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

/**
 * Autor: Enrique Ramos
 * Fecha última actualización: 12/06/2016
 * Descripción: AsyncTask que muestra un diálogo con texto personalizable.
 */

public class IndeterminateDialogTask extends AsyncTask<Void, Void, Void> {

    String message;
    Context context;
    ProgressDialog progressDialog;

    public IndeterminateDialogTask(Context context, String message){
        this.message = message;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(message);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    @Override
    protected Void doInBackground(Void... params) {
        while (true) {
            if (isCancelled())
                break;
        }
        progressDialog.dismiss();
        return null;
    }
}
