package com.proyecto.enrique.osporthello;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class CreateAccountActivity extends AppCompatActivity {

    private EditText etxUser;
    private TextInputLayout txtLayoutUser;
    private EditText etxEmail;
    private TextInputLayout txtLayoutEmail;
    private EditText etxPassword;
    private TextInputLayout txtLayoutPassword;
    Button btnCreateAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //
        etxUser = (EditText)findViewById(R.id.input_username);
        txtLayoutUser = (TextInputLayout)findViewById(R.id.input_layout_username);
        etxEmail = (EditText)findViewById(R.id.input_email);
        txtLayoutEmail = (TextInputLayout)findViewById(R.id.input_layout_email);
        etxPassword = (EditText)findViewById(R.id.input_password);
        txtLayoutPassword = (TextInputLayout)findViewById(R.id.input_layout_password);
        btnCreateAccount = (Button)findViewById(R.id.btnCreateAccount);

        // To show back arrow
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    createNewAccount();
                }
                catch (JSONException e) {e.printStackTrace();}
            }
        });
    }

    private void createNewAccount() throws JSONException {
        if(validate()){
            btnCreateAccount.setEnabled(false);

            final ProgressDialog progressDialog = new ProgressDialog(CreateAccountActivity.this);
            progressDialog.setMessage("Creating account...");
            progressDialog.setIndeterminate(true);
            progressDialog.show();

            String username = etxUser.getText().toString();
            String email = etxEmail.getText().toString();
            String password = etxPassword.getText().toString();

            JSONObject jsonParams = new JSONObject();
            jsonParams.put("email", email);
            jsonParams.put("password", password);
            jsonParams.put("firstname", username);
            RequestParams params = new RequestParams("user", jsonParams.toString());

            AsyncHttpClient client = new AsyncHttpClient(true, 80, 443);
            client.setTimeout(6000);
            client.post(MainActivity.HOST + "api/users/register", params, new JsonHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                    progressDialog.dismiss();
                    btnCreateAccount.setEnabled(true);
                    Snackbar.make(getCurrentFocus(), "Error creating account", Snackbar.LENGTH_LONG).show();
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        if(response.getString("code").equals("true")){
                            // Back to Login Activity
                            setResult(RESULT_OK);
                            finish();
                        }
                        else{
                            btnCreateAccount.setEnabled(true);
                            Snackbar.make(getCurrentFocus(), "Error creating account", Snackbar.LENGTH_LONG).show();
                        }
                        progressDialog.dismiss();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        progressDialog.dismiss();
                    }
                }
            });
        }
    }

    public boolean validate() {
        boolean valid = true;

        String username = etxUser.getText().toString();
        String email = etxEmail.getText().toString();
        String password = etxPassword.getText().toString();

        if(username.isEmpty()){
            txtLayoutUser.setError(getString(R.string.enter_username));
        }
        else
            txtLayoutUser.setError(null);

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            txtLayoutEmail.setError(getString(R.string.enter_valid_email));
            valid = false;
        }
        else
            txtLayoutEmail.setError(null);

        if (password.isEmpty() || password.length() < 8) {
            txtLayoutPassword.setError(getString(R.string.enter_valid_password));
            valid = false;
        }
        else
            txtLayoutPassword.setError(null);

        return valid;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
