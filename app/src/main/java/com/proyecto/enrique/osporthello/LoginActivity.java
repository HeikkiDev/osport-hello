package com.proyecto.enrique.osporthello;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.protocol.HTTP;

/**
 * Created by enrique on 14/03/16.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText etxUser;
    private TextInputLayout txtLayoutUser;
    private EditText etxPassword;
    private TextInputLayout txtLayoutPassword;
    private Button btnLogin;
    private Button btnCreateAccount;
    private Toolbar mToolbar;
    private CoordinatorLayout coordinatorLayout;

    static AsyncHttpClient client;

    private static final int CREATE_ACCOUNT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //
        etxUser = (EditText)findViewById(R.id.input_email);
        txtLayoutUser = (TextInputLayout)findViewById(R.id.input_layout_email);
        etxPassword = (EditText)findViewById(R.id.input_password);
        txtLayoutPassword = (TextInputLayout)findViewById(R.id.input_layout_password);
        btnLogin = (Button)findViewById(R.id.btnLogin);
        btnCreateAccount = (Button)findViewById(R.id.btnCreateAccount);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Login();
            }
        });
        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activityCreateAccount();
            }
        });
    }
    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == CREATE_ACCOUNT){
                Snackbar.make(coordinatorLayout, R.string.confirm_registration, Snackbar.LENGTH_LONG)
                        .setAction(R.string.go_to_email, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(Intent.ACTION_MAIN);
                                intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                                startActivity(Intent.createChooser(intent, getString(R.string.choose_email_app)));
                            }
                        })
                        .show();
            }
        }
    }

    private void activityCreateAccount() {
        Intent intent = new Intent(LoginActivity.this, CreateAccountActivity.class);
        startActivityForResult(intent, CREATE_ACCOUNT);
    }

    private void Login() {
        if (!validate()) {
            onLoginFailed();
            return;
        }

        btnLogin.setEnabled(false);

        final IndeterminateDialogTask progressDialog = new IndeterminateDialogTask(LoginActivity.this, "Log in...");
        progressDialog.execute();

        final String email = etxUser.getText().toString();
        String password = etxPassword.getText().toString();

        RequestParams params = new RequestParams();
        params.put("email", email);
        params.put("password", password);
        client = new AsyncHttpClient(true, 80, 443);
        client.setTimeout(10000);
        client.get(MainActivity.HOST + "api/users/login", params, new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                progressDialog.cancel(true);
                onLoginFailed();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if(response.getString("code").equals("true") && response.getString("message").equals("Login completed")){
                        User user = AnalyzeJSON.analyzeUser(response);
                        onLoginSuccess(user);
                    }
                    else
                        onLoginFailed();
                    progressDialog.cancel(true);
                } catch (JSONException e) {
                    e.printStackTrace();
                    progressDialog.cancel(true);
                }
            }
        });
    }

    public boolean validate() {
        boolean valid = true;

        String email = etxUser.getText().toString();
        String password = etxPassword.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            txtLayoutUser.setError(getString(R.string.enter_valid_email));
            valid = false;
        } else {
            txtLayoutUser.setError(null);
        }

        if (password.isEmpty() || password.length() < 8) {
            txtLayoutPassword.setError(getString(R.string.enter_valid_password));
            valid = false;
        } else {
            txtLayoutPassword.setError(null);
        }

        return valid;
    }

    public void onLoginSuccess(User user) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("user", user);
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onLoginFailed() {
        Snackbar snackbar = Snackbar.make(coordinatorLayout, R.string.login_failed, Snackbar.LENGTH_LONG);
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.parseColor("#D1C4E9"));
        snackbar.show();
        btnLogin.setEnabled(true);
    }
}