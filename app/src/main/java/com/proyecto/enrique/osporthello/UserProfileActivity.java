package com.proyecto.enrique.osporthello;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

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
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showUserData(){
        User user = MainActivity.USER_ME;
        String username = user.getFirstname();
        String lastname = user.getLastname();
        String image = user.getImage();
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
            StorageData storage = new StorageData(this);
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
            Snackbar.make(getCurrentFocus(), "Debes rellenar el nombre de usuario", Snackbar.LENGTH_LONG).show();
            return;
        }

        final IndeterminateDialogTask progressDialog = new IndeterminateDialogTask(UserProfileActivity.this, "Saving data...");
        progressDialog.execute();

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
        if(!lastname.isEmpty())
            user.setLastname(lastname);
        if(bitmapImage != null) { // Save the new user image
            StorageData storage = new StorageData(this);
            storage.saveToInternalStorage(bitmapImage, user.getEmail()+".jpg");
            user.setImage(user.getEmail()+".jpg");
        }
        if(!sex.isEmpty())
            user.setSex(sex);
        if(!age.isEmpty())
            user.setAge(age);
        if(!city.isEmpty())
            user.setCity(city);
        if(!weight.isEmpty())
            user.setWeight(weight);
        if(!height.isEmpty())
            user.setHeight(height);

        progressDialog.cancel(true);
        setResult(RESULT_OK);
        finish();
    }

    private boolean validate() {
        String username = etxUser.getText().toString();
        return !username.isEmpty();
    }
}
