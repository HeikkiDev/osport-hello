package com.proyecto.enrique.osporthello;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import cz.msebera.android.httpclient.Header;

public class UserProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView imageView;
    private EditText etxUser;
    private TextInputLayout txtLayoutUser;
    private EditText etxLastname;
    AutoCompleteTextView txtAutoCity;
    private EditText etxSex;
    private EditText etxAge;
    private EditText etxWeight;
    private EditText etxHeight;
    Button btnSave;

    private static Bitmap bitmapImage = null;
    private static final int PICK_IMAGE = 1;
    private static final String[] CITY = new String[] {"Málaga", "Madrid", "Marbella", "Barcelona", "Sevilla"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //
        imageView = (ImageView)findViewById(R.id.image);
        etxUser = (EditText)findViewById(R.id.input_username);
        txtLayoutUser = (TextInputLayout)findViewById(R.id.input_layout_username);
        etxLastname = (EditText)findViewById(R.id.input_lastname);
        etxSex = (EditText)findViewById(R.id.input_sex);
        etxAge = (EditText)findViewById(R.id.input_age);
        etxWeight = (EditText)findViewById(R.id.input_weight);
        etxHeight = (EditText)findViewById(R.id.input_height);
        btnSave = (Button)findViewById(R.id.btnEditProfile);
        btnSave.setOnClickListener(this);
        // Autocompete EditText Cities
        txtAutoCity = (AutoCompleteTextView) findViewById(R.id.input_city);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, CITY);
        txtAutoCity.setAdapter(adapter);

        //

        // Set Collapsing Toolbar layout to the screen
        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);

        // To show back arrow
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // User data to edit
        showUserData();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.floatingButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                // Show only images, no videos or anything else
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                // Always show the chooser (if there are multiple options available)
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            }
        });
    }

    @Override
    public void onClick(View v) {
        saveChanges();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                imageView.setImageBitmap(bitmap);
                this.bitmapImage = bitmap;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

    private void showUserData(){
        User user = MainActivity.USER_ME;
        String username = user.getFirstname();
        String lastname = user.getLastname();
        String image = user.getImagepath();
        String sex = user.getSex();
        String age = user.getAge();
        String city = user.getCity();
        String weight = user.getWeight();
        String height = user.getHeight();

        if(username != null)
            etxUser.setText(username);
        if(lastname != null)
            etxLastname.setText(lastname);
        if(image != null){
            StorageImage storage = new StorageImage(this);
            storage.loadImageFromStorage(image, imageView);
        }
        if(sex != null)
            etxSex.setText(sex);
        if(age != null)
            etxAge.setText(age);
        if (city != null)
            txtAutoCity.setText(city);
        if(weight != null)
            etxWeight.setText(weight);
        if(height != null)
            etxHeight.setText(height);
    }

    private void saveChanges() {
        if(!validate()){
            txtLayoutUser.setError(getString(R.string.enter_username));
            Snackbar.make(getCurrentFocus(), R.string.specify_username, Snackbar.LENGTH_LONG).show();
            return;
        }

        User user = MainActivity.USER_ME;
        String username = etxUser.getText().toString();
        String lastname = etxLastname.getText().toString();
        String sex = etxSex.getText().toString();
        String age = etxAge.getText().toString();
        String city = txtAutoCity.getText().toString();
        String weight = etxWeight.getText().toString();
        String height = etxHeight.getText().toString();

        if(!username.isEmpty())
            user.setFirstname(username);
        if(lastname.isEmpty())
            user.setLastname(null);
        else
        user.setLastname(lastname);
        if(bitmapImage != null) { // Save the new user image
            StorageImage storage = new StorageImage(this);
            storage.saveToInternalStorage(bitmapImage, user.getEmail() + ".jpg");
            user.setImagepath(user.getEmail() + ".jpg");
            // Upload user image to database
            StorageImage storageImage = new StorageImage(this);
            storage.uploadStringImageToRemote(bitmapImage);
        }
        if(sex.isEmpty())
            user.setSex(null);
        else
            user.setSex(sex);
        if(age.isEmpty())
            user.setAge(null);
        else
            user.setAge(age);
        if(city.isEmpty())
            user.setCity(null);
        else
            user.setCity(city);
        if(weight.isEmpty())
            user.setWeight(null);
        else
            user.setWeight(weight);
        if(height.isEmpty())
            user.setHeight(null);
        else
            user.setHeight(height);

        uploadUserProfile();

        setResult(RESULT_OK);
        finish();
    }

    private void uploadUserProfile() {
        User user = MainActivity.USER_ME;
        String lastname = (user.getLastname() != null)?user.getLastname():"";
        String sex = (user.getSex() != null)?user.getSex():"";
        String age = (user.getAge() != null)?user.getAge():"";
        String city = (user.getCity() != null)?user.getCity():"";
        String weight = (user.getWeight() != null)? user.getWeight():"";
        String height = (user.getHeight() != null)?user.getHeight():"";
        RequestParams param = null;
        try {
            JSONObject json = new JSONObject();
            json.put("email", user.getEmail());
            json.put("firstname", user.getFirstname());
            json.put("lastname", lastname);
            json.put("sex", sex);
            json.put("age", age);
            json.put("city", city);
            json.put("weight", weight);
            json.put("height", height);
            param = new RequestParams();
            param.put("user", json.toString());
        } catch (JSONException e) {e.printStackTrace();}

        AsyncHttpClient client = new AsyncHttpClient(true, 80, 443);
        client.setTimeout(10000);
        client.put(MainActivity.HOST+"api/users/"+user.getApiKey(), param, new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                Log.e("PROFILE", "Error");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.e("PROFILE","Success");
            }
        });
    }

    private boolean validate() {
        String username = etxUser.getText().toString();
        return !username.isEmpty();
    }

}
