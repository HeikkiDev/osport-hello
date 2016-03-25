package com.proyecto.enrique.osporthello;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.firebase.client.Firebase;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private boolean isFirstTime;
    private int currentSelected;
    private Fragment fragment;

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    public static User USER_ME;
    public static Firebase FIREBASE;
    private static final int LOGIN_CODE = 1;
    private static final int EDIT_CODE = 2;
    public static final String HOST = "https://enriqueramos.info/osporthello/";
    private static final String USER_FIRST_TIME = "first_time";
    private static final String PREFERENCES_FILE = "osporthello_settings";
    public static final String SESSION_FILE = "my_session";
    private static final String STATE_SELECTED_POSITION = "state_selected_position";
    private static final String STATE_FRAGMENT = "mFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        //
        Firebase.setAndroidContext(this);
        FIREBASE = new Firebase("https://osporthello.firebaseio.com/");
        //
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        // Is the user's first time?
        isFirstTime = Boolean.valueOf(readSharedSetting(this, USER_FIRST_TIME, "true"));
        if (isFirstTime) {
            // Show Drawer Menu and other information or help!!!
            drawerLayout.openDrawer(GravityCompat.START);
            saveSharedSetting(this, USER_FIRST_TIME, "false");
        }

        // Check user session
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(SESSION_FILE, 0); // 0 private mode
        String userSesion = sharedPreferences.getString("email", null);
        if(userSesion == null){
            // Show Login activity
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivityForResult(intent, LOGIN_CODE);
        }
        else {
            // Retrieve user session and information and show it
            retrieveUserData();
        }

        // Restore selected option of drawer menu
        if (savedInstanceState != null)
            currentSelected = savedInstanceState.getInt(STATE_SELECTED_POSITION);
        else
            currentSelected = 0;

        // Set Toolbar as ActionBar
        setToolbar();

        // Set items click on Drawer menu
        setNavigationDrawerItemsClick(navigationView);

        if (savedInstanceState == null) {
            // Default selection HOME, or currentSelected
            showFragment(currentSelected, navigationView.getMenu().findItem(R.id.nav_home));
        }
    }

    @Override
    public void onClick(View v) {
        // LANZAR ACTIVITY PARA VER/EDITAR PERFIL DE USUARIO E IMAGEN
        Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
        startActivityForResult(intent, EDIT_CODE);
    }

    /**
     * Result of Login Activity
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == LOGIN_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                Bundle bundle = data.getExtras();
                User user = (User) bundle.getSerializable("user");
                this.USER_ME = user;

                // Set user information
                View headerLayout = navigationView.getHeaderView(0); // 0-index heade
                TextView txtUsername = (TextView) headerLayout.findViewById(R.id.header_username);
                TextView txtEmail = (TextView) headerLayout.findViewById(R.id.header_email);
                CircleImageView circleImage = (CircleImageView) headerLayout.findViewById(R.id.header_circle_image);
                txtUsername.setText(user.getFirstname().toString());
                txtEmail.setText(user.getEmail().toString());
                try {
                    StorageImage storage = new StorageImage(getApplicationContext());
                    storage.saveToInternalStorage(stringToBitMap(user.getImage()), user.getEmail() + ".jpg");
                } catch (Exception e){}

                saveUserSession();
                retrieveUserData();
            } else
                Snackbar.make(getCurrentFocus(), "Error retrieving user data", Snackbar.LENGTH_SHORT).show();
        }
        else if(requestCode == EDIT_CODE){
            if(resultCode == RESULT_OK) {
                saveUserSession();
                retrieveUserData();
                Snackbar.make(getCurrentFocus(), "Datos actualizados", Snackbar.LENGTH_SHORT).show();
                View headerLayout = navigationView.getHeaderView(0);
                CircleImageView circleImage = (CircleImageView) headerLayout.findViewById(R.id.header_circle_image);
                try {
                    StorageImage storage = new StorageImage(getApplicationContext());
                    storage.loadImageFromStorage(this.USER_ME.getEmail() + ".jpg", circleImage);
                } catch (Exception e) {e.printStackTrace();}
            }
        }
    }

    /**
     * Save state of selection in sliding menu
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, currentSelected);
        //Save the fragment's instance
        getSupportFragmentManager().putFragment(outState, STATE_FRAGMENT, fragment);
    }

    /**
     * Restore selected option in sliding menu
     * @param savedInstanceState
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentSelected = savedInstanceState.getInt(STATE_SELECTED_POSITION, 0);
        //Restore the fragment's instance
        fragment = getSupportFragmentManager().getFragment(savedInstanceState, STATE_FRAGMENT);

        MenuItem itemMenu = null;
        switch (currentSelected){
            case 0:
                itemMenu = navigationView.getMenu().findItem(R.id.nav_home);
                break;
            case 1:
                itemMenu = navigationView.getMenu().findItem(R.id.nav_activities);
                break;
            case 2:
                itemMenu = navigationView.getMenu().findItem(R.id.nav_friends);
                break;
            case 3:
                itemMenu = navigationView.getMenu().findItem(R.id.nav_chat);
                break;
            case 4:
                itemMenu = navigationView.getMenu().findItem(R.id.nav_geosearch);
                break;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager
                .beginTransaction()
                .replace(R.id.main_content, fragment)
                .commit();

        itemMenu.setChecked(true);
        // Set fragment title
        setTitle(itemMenu.getTitle().toString());
    }

    /**
     * Save user session data
     */
    private void saveUserSession(){
        User user = this.USER_ME;
        // Save the user session
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(SESSION_FILE, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email", user.getEmail());
        editor.putString("firstname", user.getFirstname());
        if (user.getLastname() != null)
            editor.putString("lastname", user.getLastname());
        editor.putString("apikey", user.getApiKey());
        if(user.getSex() != null)
            editor.putString("sex", user.getSex());
        if(user.getAge() != null)
            editor.putString("age", user.getAge());
        if(user.getCity() != null)
            editor.putString("city", user.getCity());
        if(user.getWeight() != null)
            editor.putString("weight", user.getWeight());
        if(user.getHeight() != null)
            editor.putString("height", user.getHeight());
        editor.commit(); // commit changes
    }

    /**
     * Retrieve user data from sharedpreferences and show it in GUI
     */
    private void retrieveUserData() {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(SESSION_FILE, 0);
        User user = new User(sharedPreferences.getString("email", null),
                sharedPreferences.getString("firstname", null),
                sharedPreferences.getString("lastname", null), null,
                sharedPreferences.getString("apikey", null),
                sharedPreferences.getString("sex", null),
                sharedPreferences.getString("age", null),
                sharedPreferences.getString("city", null),
                sharedPreferences.getString("weight", null),
                sharedPreferences.getString("height", null));
        this.USER_ME = user;
        View headerLayout = navigationView.getHeaderView(0); // 0-index heade
        TextView txtUsername = (TextView)headerLayout.findViewById(R.id.header_username);
        TextView txtEmail = (TextView)headerLayout.findViewById(R.id.header_email);
        CircleImageView circleImage = (CircleImageView)headerLayout.findViewById(R.id.header_circle_image);
        txtUsername.setText(sharedPreferences.getString("firstname", null) +" "+ sharedPreferences.getString("lastname", ""));
        txtEmail.setText(sharedPreferences.getString("email", null));
        try {
            StorageImage storage = new StorageImage(getApplicationContext());
            storage.loadImageFromStorage(user.getEmail() + ".jpg", circleImage);
        } catch (Exception e){}
    }

    /**
     * Set Toolbar as App ActionBar with Hamburger icon
     */
    private void setToolbar() {
        // Set Toolbar as ActionBar
        setSupportActionBar(toolbar);

        // Set Hamburger Icon and handle click
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_menu);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

    }

    /**
     * Handle clicks in sliding menu items (not header)
     * @param navigationView Contains navigation menu items
     */
    private void setNavigationDrawerItemsClick(NavigationView navigationView) {
        // Click slide menu options
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {

                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.nav_home:
                                if (currentSelected == 0) {
                                    drawerLayout.closeDrawers();
                                } else {
                                    currentSelected = 0;
                                    showFragment(currentSelected, menuItem);
                                }
                                break;
                            case R.id.nav_activities:
                                if (currentSelected == 1) {
                                    drawerLayout.closeDrawers();
                                } else {
                                    currentSelected = 1;
                                    showFragment(currentSelected, menuItem);
                                }
                                break;
                            case R.id.nav_friends:
                                if (currentSelected == 2) {
                                    drawerLayout.closeDrawers();
                                } else {
                                    currentSelected = 2;
                                    showFragment(currentSelected, menuItem);
                                }
                                break;
                            case R.id.nav_chat:
                                if (currentSelected == 3) {
                                    drawerLayout.closeDrawers();
                                } else {
                                    currentSelected = 3;
                                    showFragment(currentSelected, menuItem);
                                }
                                break;
                            case R.id.nav_geosearch:
                                if (currentSelected == 4) {
                                    drawerLayout.closeDrawers();
                                } else {
                                    currentSelected = 4;
                                    showFragment(currentSelected, menuItem);
                                }
                                break;
                            case R.id.nav_settings:
                                Snackbar.make(getCurrentFocus(), "Item Settings", Snackbar.LENGTH_SHORT).show();
                                //
                                //TODO: LANZAR ACTIVITY DE SETTINGS CON FLECHITA ARRIBA PARA VOLVER ATRÁS AQUÍ
                                // Un fragment aquí no tiene mucho sentido
                                drawerLayout.closeDrawers();
                                break;
                            case R.id.nav_log_out:
                                closeSession(); // Log Out
                                break;
                            default:
                                break;
                        }

                        return true;
                    }
                }
        );
    }

    /**
     * Show fragment as application main content
     * @param position sliding menu item position
     */
    private void showFragment(int position, MenuItem item) {

        switch (position){
            case 0:
                fragment = new HomeFragment();
                break;
            case 1:
                fragment = new ActivitiesFragment();
                break;
            case 2:
                fragment = new FriendsFragment();
                break;
            case 3:
                fragment = new ChatFragment();
                break;
            case 4:
                fragment = new GeoSearchFragment();
                break;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager
                .beginTransaction()
                .replace(R.id.main_content, fragment)
                .commit();

        item.setChecked(true);
        // Set fragment title
        setTitle(item.getTitle().toString());
        // Close drawer
        drawerLayout.closeDrawers();
    }

    /**
     * String 64 base enconded to Bitmap
     * @param encodedString
     * @return bitmap (from given string)
     */
    public Bitmap stringToBitMap(String encodedString){
        try{
            byte [] encodeByte= Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap= BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        }catch(Exception e){
            e.getMessage();
            return null;
        }
    }

    /**
     * Save a persistent setting using SharedPreferences
     * @param context application context
     * @param settingName key name
     * @param settingValue value
     */
    public static void saveSharedSetting(Context context, String settingName, String settingValue) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(settingName, settingValue);
        editor.apply();
    }

    /**
     * Retrieve a setting from SharedPreferences
     * @param context application context
     * @param settingName key name
     * @param defaultValue default value if setting doesn't exists
     * @return
     */
    public static String readSharedSetting(Context context, String settingName, String defaultValue) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        return sharedPref.getString(settingName, defaultValue);
    }

    /**
     * Close user session deleting preferences in session file
     */
    private void closeSession(){
        // Check if facebook session is open
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if(accessToken != null){
            // Facebook log out programmatically
            LoginManager.getInstance().logOut();
        }

        // Delete saved user session
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(SESSION_FILE, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit(); // commit changes

        // Launch Main Activity -> Login Activity
        Intent intent = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
