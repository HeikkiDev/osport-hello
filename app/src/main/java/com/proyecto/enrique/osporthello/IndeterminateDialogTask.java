package com.proyecto.enrique.osporthello;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

/**
 * Created by enrique on 17/03/16.
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
        return null;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        progressDialog.dismiss();
    }

    @Override
    protected void onCancelled(Void aVoid) {
        super.onCancelled(aVoid);
        progressDialog.dismiss();
    }
}
