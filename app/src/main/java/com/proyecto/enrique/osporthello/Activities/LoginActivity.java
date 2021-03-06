package com.proyecto.enrique.osporthello.Activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.proyecto.enrique.osporthello.AnalyzeJSON;
import com.proyecto.enrique.osporthello.ApiClient;
import com.proyecto.enrique.osporthello.ImageManager;
import com.proyecto.enrique.osporthello.AsyncTask.IndeterminateDialogTask;
import com.proyecto.enrique.osporthello.Models.User;
import com.proyecto.enrique.osporthello.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

import cz.msebera.android.httpclient.Header;

/**
 * Autor: Enrique Ramos
 * Fecha última actualización: 12/06/2016
 * Descripción: Activity donde el usuario puede hacer Login, ir a la Activity de crear cuenta,
 * o recuperar la contraseña.
 */

public class LoginActivity extends AppCompatActivity {

    private EditText etxUser;
    private TextInputLayout txtLayoutUser;
    private EditText etxPassword;
    private TextInputLayout txtLayoutPassword;
    private Button btnLogin;
    private TextView txtForgotPassword;
    private LoginButton btnFacebookLogin;
    private Button btnCreateAccount;
    private Toolbar mToolbar;
    private CoordinatorLayout coordinatorLayout;

    Context context;
    CallbackManager callbackManager;

    private static final int CREATE_ACCOUNT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize Facebook SDK before setContentView
        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_login);
        //
        etxUser = (EditText)findViewById(R.id.input_email);
        txtLayoutUser = (TextInputLayout)findViewById(R.id.input_layout_email);
        etxPassword = (EditText)findViewById(R.id.input_password);
        txtLayoutPassword = (TextInputLayout)findViewById(R.id.input_layout_password);
        btnLogin = (Button)findViewById(R.id.btnLogin);
        txtForgotPassword = (TextView)findViewById(R.id.txtForgotPassword);
        btnFacebookLogin = (LoginButton)findViewById(R.id.login_facebook);
        btnCreateAccount = (Button)findViewById(R.id.btnCreateAccount);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        context = this;

        txtForgotPassword.setClickable(true);
        txtForgotPassword.setMovementMethod(LinkMovementMethod.getInstance());
        txtForgotPassword.setText(Html.fromHtml(getString(R.string.forgot_your_password)));

        try {
            FacebookLogin();
        }
        catch (Exception e){onLoginFailed();}

        txtForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restorePassword();
            }
        });
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
        callbackManager.onActivityResult(requestCode, resultCode, data);

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

    private void restorePassword(){
        String email = etxUser.getText().toString();

        if(email.equals("")){
            Toast.makeText(this, getString(R.string.enter_valid_email), Toast.LENGTH_SHORT).show();
            return;
        }
        else{
            final IndeterminateDialogTask progressDialog = new IndeterminateDialogTask(LoginActivity.this, getString(R.string.wait_a_moment));
            progressDialog.execute();

            ApiClient.getRestorePassword("api/restore-password/"+email,
                    new JsonHttpResponseHandler() {

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            progressDialog.cancel(true);
                            Snackbar snackbar = Snackbar.make(coordinatorLayout, R.string.restore_password_failed, Snackbar.LENGTH_LONG);
                            View sbView = snackbar.getView();
                            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                            textView.setTextColor(Color.parseColor("#F44336"));
                            snackbar.show();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            super.onFailure(statusCode, headers, throwable, errorResponse);
                            progressDialog.cancel(true);
                            Snackbar snackbar = Snackbar.make(coordinatorLayout, R.string.restore_password_failed, Snackbar.LENGTH_LONG);
                            View sbView = snackbar.getView();
                            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                            textView.setTextColor(Color.parseColor("#F44336"));
                            snackbar.show();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                            super.onFailure(statusCode, headers, throwable, errorResponse);
                            progressDialog.cancel(true);
                            Snackbar snackbar = Snackbar.make(coordinatorLayout, R.string.restore_password_failed, Snackbar.LENGTH_LONG);
                            View sbView = snackbar.getView();
                            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                            textView.setTextColor(Color.parseColor("#F44336"));
                            snackbar.show();
                        }

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            try {
                                if (response.getString("code").equals("true")) {
                                    Snackbar.make(coordinatorLayout, R.string.restore_password_email, Snackbar.LENGTH_LONG)
                                            .setAction(R.string.go_to_email, new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Intent intent = new Intent(Intent.ACTION_MAIN);
                                                    intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                                                    startActivity(Intent.createChooser(intent, getString(R.string.choose_email_app)));
                                                }
                                            }).show();
                                }
                                else{
                                    Snackbar snackbar = Snackbar.make(coordinatorLayout, R.string.restore_password_failed, Snackbar.LENGTH_LONG);
                                    View sbView = snackbar.getView();
                                    TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                                    textView.setTextColor(Color.parseColor("#F44336"));
                                    snackbar.show();
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

    private void FacebookLogin(){
        // FACEBOOK LOGIN
        btnFacebookLogin.setReadPermissions(new String[]{"public_profile", "user_friends", "email"});

        callbackManager = CallbackManager.Factory.create();
        // Callback registration
        btnFacebookLogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                btnFacebookLogin.setVisibility(View.INVISIBLE);
                // Info about Facebook user profile
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject me, GraphResponse response) {
                        if (response.getError() != null) {
                            // handle error
                        } else {
                            String id = me.optString("id");
                            final String firstname = me.optString("first_name");
                            final String lastname = me.optString("last_name");
                            final String email = me.optString("email");
                            if (email.equals("")) {
                                onLoginFailed();
                                return;
                            }
                            final String genre = me.optString("genre");
                            String location = null;
                            try {
                                String urlImage = me.getJSONObject("picture").getJSONObject("data").getString("url");
                                try {
                                    location = me.getJSONObject("location").getString("name").split(",")[0];
                                } catch (Exception e) {
                                }
                                final String finalLocation = location;
                                // Download profile image
                                AsyncHttpClient client = new AsyncHttpClient(true, 80, 443);
                                client.get(urlImage, new TextHttpResponseHandler() {
                                    @Override
                                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                        checkFacebookUser(email, firstname, lastname, genre, finalLocation, null);
                                    }

                                    @Override
                                    public void onSuccess(int statusCode, Header[] headers, String responseString) {
                                        Bitmap bitmap = ImageManager.stringToBitMap(responseString);
                                        checkFacebookUser(email, firstname, lastname, genre, finalLocation, bitmap);
                                    }
                                });
                            } catch (JSONException e) {
                                e.printStackTrace();
                                checkFacebookUser(email, firstname, lastname, genre, location, null);
                            }
                        }
                    }
                });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,first_name,last_name,email, picture.type(large),gender,location");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                onLoginFailed();
            }

            @Override
            public void onError(FacebookException exception) {
                onLoginFailed();
            }
        });
    }

    private void checkFacebookUser(String email, String firstname, String lastname, String genre, String location, Bitmap bitmap){
        // Facebook login success. Now check user in my server.
        final IndeterminateDialogTask progressDialog = new IndeterminateDialogTask(LoginActivity.this, getString(R.string.wait_a_moment));
        progressDialog.execute();

        String stringImage = "";
        if(bitmap != null){
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, baos);
            byte[] imageBytes = baos.toByteArray();
            stringImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        }
        if(location == null)
            location = "null";

        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("email", email);
            jsonParams.put("first", firstname);
            jsonParams.put("last", lastname);
            jsonParams.put("sex", genre);
            jsonParams.put("city", location);
            jsonParams.put("image", stringImage);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestParams params = new RequestParams("user", jsonParams.toString());

        ApiClient.postFacebookLogin("api/users/facebook", params, new JsonHttpResponseHandler() {

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                progressDialog.cancel(true);
                onLoginFailed();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                progressDialog.cancel(true);
                onLoginFailed();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                progressDialog.cancel(true);
                onLoginFailed();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.getString("code").equals("true") && response.getString("message").equals("Login completed")) {
                        User user = AnalyzeJSON.analyzeUserLogin(response, context);
                        onLoginSuccess(user);
                    } else
                        onLoginFailed();
                    progressDialog.cancel(true);
                } catch (JSONException e) {
                    e.printStackTrace();
                    progressDialog.cancel(true);
                    onLoginFailed();
                }
            }
        });
    }

    private void Login() {
        if (!validate()) {
            onLoginFailed();
            return;
        }

        btnLogin.setEnabled(false);

        final IndeterminateDialogTask progressDialog = new IndeterminateDialogTask(LoginActivity.this, getString(R.string.logging_in));
        progressDialog.execute();

        final String email = etxUser.getText().toString();
        String password = etxPassword.getText().toString();

        RequestParams params = new RequestParams();
        params.put("email", email);
        params.put("password", password);

        ApiClient.getUserLogin("api/users/login", params, new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                progressDialog.cancel(true);
                onLoginFailed();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                progressDialog.cancel(true);
                onLoginFailed();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                progressDialog.cancel(true);
                onLoginFailed();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.getString("code").equals("true") && response.getString("message").equals("Login completed")) {
                        User user = AnalyzeJSON.analyzeUserLogin(response,context);
                        onLoginSuccess(user);
                    } else
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
        try{
            btnFacebookLogin.setVisibility(View.VISIBLE);
            // Facebook log out programmatically
            LoginManager.getInstance().logOut();
        } catch (Exception e){}
        Snackbar snackbar = Snackbar.make(coordinatorLayout, R.string.login_failed, Snackbar.LENGTH_LONG);
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.parseColor("#F44336"));
        snackbar.show();
        btnLogin.setEnabled(true);
    }
}