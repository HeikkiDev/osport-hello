package com.proyecto.enrique.osporthello.Activities;

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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.proyecto.enrique.osporthello.ImageManager;
import com.proyecto.enrique.osporthello.IndeterminateDialogTask;
import com.proyecto.enrique.osporthello.Models.User;
import com.proyecto.enrique.osporthello.R;

import org.json.JSONException;
import org.json.JSONObject;

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
    private static final int WIDTH = 350;
    private static final int HEIGHT = 300;
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
                intent.putExtra("crop", "true");
                intent.putExtra("aspectX", WIDTH);
                intent.putExtra("aspectY", HEIGHT);
                intent.putExtra("outputX", WIDTH);
                intent.putExtra("outputY", HEIGHT);
                intent.putExtra("scale", true);
                intent.putExtra("scaleUpIfNeeded", true);
                intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
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
        String sex = user.getSex();
        String age = user.getAge();
        String city = user.getCity();
        String weight = user.getWeight();
        String height = user.getHeight();

        if(username != null)
            etxUser.setText(username);
        if(lastname != null)
            etxLastname.setText(lastname);
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
        try {
            ImageManager storage = new ImageManager(getApplicationContext());
            storage.loadImageFromStorage(user.getEmail() + ".jpg", imageView);
        } catch (Exception e){}
    }

    private void saveChanges() {
        if(!validate()){
            txtLayoutUser.setError(getString(R.string.enter_username));
            Snackbar.make(getCurrentFocus(), R.string.specify_username, Snackbar.LENGTH_LONG).show();
            return;
        }

        final IndeterminateDialogTask progressDialog = new IndeterminateDialogTask(UserProfileActivity.this, "Saving changes...");
        progressDialog.execute();

        User user = MainActivity.USER_ME;
        String username = etxUser.getText().toString();
        String lastname = etxLastname.getText().toString();
        String sex = etxSex.getText().toString();
        String age = etxAge.getText().toString();
        String city = txtAutoCity.getText().toString();
        String weight = etxWeight.getText().toString();
        String height = etxHeight.getText().toString();

        if(bitmapImage != null) { // Save the new user image
            ImageManager storage = new ImageManager(this);
            storage.saveToInternalStorage(bitmapImage, user.getEmail() + ".jpg");
        }
        if(!username.isEmpty())
            user.setFirstname(username);
        if(lastname.isEmpty())
            user.setLastname(null);
        else
            user.setLastname(lastname);
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

        progressDialog.cancel(true);
        setResult(RESULT_OK);
        finish();
    }

    private void uploadUserProfile() {
        User user = MainActivity.USER_ME;
        UploadProfileTask uploadProfileTask = new UploadProfileTask();
        uploadProfileTask.execute(user);
    }

    private boolean validate() {
        String username = etxUser.getText().toString();
        return !username.isEmpty();
    }

    private class UploadProfileTask extends AsyncTask <User, Void, Void>{

        @Override
        protected Void doInBackground(User... params) {
            User user = params[0];

            String lastname = (user.getLastname() != null)?user.getLastname():"";
            String sex = (user.getSex() != null)?user.getSex():"";
            String age = (user.getAge() != null)?user.getAge():"";
            String city = (user.getCity() != null)?user.getCity():"";
            String weight = (user.getWeight() != null)? user.getWeight():"";
            String height = (user.getHeight() != null)?user.getHeight():"";

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
                RequestParams param = new RequestParams();
                param.put("user", json.toString());

                SyncHttpClient client = new SyncHttpClient(true, 80, 443);
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
            } catch (JSONException e) {e.printStackTrace();}

            return null;
        }
    }

}