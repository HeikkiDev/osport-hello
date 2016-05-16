package com.proyecto.enrique.osporthello.Activities;

import android.os.Bundle;
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
import com.proyecto.enrique.osporthello.IndeterminateDialogTask;
import com.proyecto.enrique.osporthello.R;

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
    private EditText etxRepeatPassword;
    private TextInputLayout txtLayoutRepeatPassword;
    private EditText etxAlternativeEmail;
    private TextInputLayout txtLayoutAlternativeEmail;
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
        etxRepeatPassword = (EditText)findViewById(R.id.input_repeatPassword);
        txtLayoutRepeatPassword = (TextInputLayout)findViewById(R.id.input_layout_repeatPassword);
        etxAlternativeEmail = (EditText)findViewById(R.id.input_emergencyEmail);
        txtLayoutAlternativeEmail = (TextInputLayout)findViewById(R.id.input_layout_emergencyEmail);
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

            final IndeterminateDialogTask progressDialog = new IndeterminateDialogTask(CreateAccountActivity.this, getString(R.string.creating_account));
            progressDialog.execute();

            String username = etxUser.getText().toString();
            String email = etxEmail.getText().toString();
            String password = etxPassword.getText().toString();
            String alternativeEmail = etxAlternativeEmail.getText().toString();

            JSONObject jsonParams = new JSONObject();
            jsonParams.put("email", email);
            jsonParams.put("password", password);
            jsonParams.put("firstname", username);
            jsonParams.put("alternative_email", alternativeEmail);
            RequestParams params = new RequestParams("user", jsonParams.toString());

            AsyncHttpClient client = new AsyncHttpClient(true, 80, 443);
            client.setTimeout(6000);
            client.post(MainActivity.HOST + "api/registration", params, new JsonHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                    progressDialog.cancel(true);
                    btnCreateAccount.setEnabled(true);
                    Snackbar.make(getCurrentFocus(), R.string.error_creating_account, Snackbar.LENGTH_LONG).show();
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
                            if(response.getString("message").equals("User already exists")) {
                                Snackbar.make(getCurrentFocus(), R.string.user_already_exists, Snackbar.LENGTH_LONG).show();
                                txtLayoutEmail.setError("Use another email");
                                etxEmail.requestFocus();
                            }
                            else
                                Snackbar.make(getCurrentFocus(), R.string.error_creating_account, Snackbar.LENGTH_LONG).show();
                        }
                        progressDialog.cancel(true);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        progressDialog.cancel(true);
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
        String repeatPassword = etxRepeatPassword.getText().toString();
        String alternativeEmail = etxAlternativeEmail.getText().toString();

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

        if(!repeatPassword.equals(password)){
            txtLayoutRepeatPassword.setError(getString(R.string.passwords_dont_match));
            valid = false;
        }
        else
        txtLayoutRepeatPassword.setError(null);

        if (alternativeEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(alternativeEmail).matches()) {
            txtLayoutAlternativeEmail.setError(getString(R.string.enter_valid_email));
            valid = false;
        }
        else
            txtLayoutAlternativeEmail.setError(null);

        return valid;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
