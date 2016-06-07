package com.proyecto.enrique.osporthello.Activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.firebase.client.Firebase;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.proyecto.enrique.osporthello.ApiClient;
import com.proyecto.enrique.osporthello.Fragments.ActivitiesFragment;
import com.proyecto.enrique.osporthello.Fragments.ChatFragment;
import com.proyecto.enrique.osporthello.Fragments.FriendsFragment;
import com.proyecto.enrique.osporthello.Fragments.GeoSearchFragment;
import com.proyecto.enrique.osporthello.Fragments.HomeFragment;
import com.proyecto.enrique.osporthello.Fragments.StatisticsFragment;
import com.proyecto.enrique.osporthello.ImageManager;
import com.proyecto.enrique.osporthello.Models.User;
import com.proyecto.enrique.osporthello.Services.ChatNotificationsService;
import com.proyecto.enrique.osporthello.R;
import com.proyecto.enrique.osporthello.Services.NotificationsService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import cz.msebera.android.httpclient.Header;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private boolean isFirstTime;
    private int currentSelected;
    private Fragment fragment;

    private Toolbar toolbar;
    public static DrawerLayout drawerLayout;
    private NavigationView navigationView;

    public static User USER_ME;
    public static Firebase FIREBASE;
    private static final int LOGIN_CODE = 1;
    private static final int EDIT_CODE = 2;
    private static final int CONFIGURATION = 3;
    private static final int CUSTOM_RESULT = 17;
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

        // Restart App if crash
        Intent restartIntent = getApplicationContext().getPackageManager().getLaunchIntentForPackage(getApplicationContext().getPackageName() );
        final PendingIntent restart = PendingIntent.getActivity(getApplicationContext(), 0, restartIntent, PendingIntent.FLAG_ONE_SHOT);
        restartIntent.putExtra("error_restart","ERROR");
        restartIntent.setAction("com.proyecto.enrique.osporthello.ErrorRestart");
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                Log.e("", "restarting app");
                AlarmManager manager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
                manager.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restart);
                finish();
                System.exit(2);
            }
        });

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
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
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
            if(userSesion == null)
                showFragment(-1, navigationView.getMenu().findItem(R.id.nav_home));
            else
                showFragment(currentSelected, navigationView.getMenu().findItem(R.id.nav_home));

            if(getIntent() != null && getIntent().getExtras() != null){
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle(getResources().getString(R.string.error_report));
                alertDialogBuilder.setMessage(getResources().getString(R.string.want_send_error));
                alertDialogBuilder
                        .setPositiveButton(getResources().getString(R.string.send), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SendLogcatMail();
                            }
                        })
                        .setNegativeButton(getResources().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try{
            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(SESSION_FILE, 0); // 0 private mode
            String userSesion = sharedPreferences.getString("email", null);
            if(userSesion != null){
                SharedPreferences sharedPref = getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
                if(sharedPref.getInt("chatnotifications", 0) != 0)
                    startService(new Intent(MainActivity.this, ChatNotificationsService.class));
                if(sharedPref.getInt("friendsnotification", 0) != 0)
                    startService(new Intent(MainActivity.this, NotificationsService.class));
            }
        }
        catch (Exception e){}
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
                showFragment(currentSelected, navigationView.getMenu().findItem(R.id.nav_home));
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
                    File file = getBaseContext().getFileStreamPath(user.getEmail()+".png");
                    if(!file.exists()){
                        ImageManager storage = new ImageManager(getApplicationContext());
                        storage.saveToInternalStorage(storage.stringToBitMap(user.getImage()), user.getEmail() + ".png");
                    }
                } catch (Exception e){}

                saveUserSession();
                retrieveUserData();

                SharedPreferences sharedPref = getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
                // Service for listen new chats and messages
                if(sharedPref.getInt("chatnotifications", 0) != 0)
                    startService(new Intent(MainActivity.this, ChatNotificationsService.class));
                // Service for listen new friends
                if(sharedPref.getInt("friendsnotification", 0) != 0)
                    startService(new Intent(MainActivity.this, NotificationsService.class));

                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            } else
                Snackbar.make(getCurrentFocus(), R.string.error_retrieving_userdata, Snackbar.LENGTH_SHORT).show();
        }
        else if(requestCode == EDIT_CODE){
            if(resultCode == RESULT_OK) {
                saveUserSession();
                retrieveUserData();
                Snackbar.make(getCurrentFocus(), R.string.data_updated, Snackbar.LENGTH_SHORT).show();
                View headerLayout = navigationView.getHeaderView(0);
                CircleImageView circleImage = (CircleImageView) headerLayout.findViewById(R.id.header_circle_image);
                try {
                    ImageManager storage = new ImageManager(getApplicationContext());
                    storage.loadImageFromStorage(this.USER_ME.getEmail() + ".png", circleImage);
                } catch (Exception e) {e.printStackTrace();}
            }
        }
        else if(requestCode == CONFIGURATION) {
            if (resultCode == CUSTOM_RESULT) {
                ApiClient.deleteUserAccount("api/users/" + USER_ME.getEmail(), new JsonHttpResponseHandler() {
                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                        Log.e("DELETE_ACCOUNT", "ERROR!!");
                        Toast.makeText(getApplicationContext(), R.string.connection_error,Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        super.onFailure(statusCode, headers, responseString, throwable);
                        Log.e("DELETE_ACCOUNT", "ERROR!!");
                        Toast.makeText(getApplicationContext(), R.string.connection_error,Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                        super.onFailure(statusCode, headers, throwable, errorResponse);
                        Log.e("DELETE_ACCOUNT", "ERROR!!");
                        Toast.makeText(getApplicationContext(), R.string.connection_error,Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        try {
                            if (response.getString("code").equals("true")) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.see_you), Toast.LENGTH_LONG).show();
                                closeSession();
                                finish();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("DELETE_ACCOUNT", "ERROR!!");
                            Toast.makeText(getApplicationContext(), R.string.connection_error,Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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
                itemMenu = navigationView.getMenu().findItem(R.id.nav_statistics);
                break;
            case 3:
                itemMenu = navigationView.getMenu().findItem(R.id.nav_friends);
                break;
            case 4:
                itemMenu = navigationView.getMenu().findItem(R.id.nav_chat);
                break;
            case 5:
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

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getResources().getString(R.string.exit));
        alertDialogBuilder.setMessage(getString(R.string.want_exit_osport))
                .setPositiveButton(getResources().getString(R.string.exit), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
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
            ImageManager storage = new ImageManager(getApplicationContext());
            storage.loadImageFromStorage(user.getEmail() + ".png", circleImage);
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
                if(drawerLayout.getDrawerLockMode(Gravity.LEFT) != DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
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
                            case R.id.nav_statistics:
                                if (currentSelected == 2) {
                                    drawerLayout.closeDrawers();
                                } else {
                                    currentSelected = 2;
                                    showFragment(currentSelected, menuItem);
                                }
                                break;
                            case R.id.nav_friends:
                                if (currentSelected == 3) {
                                    drawerLayout.closeDrawers();
                                } else {
                                    currentSelected = 3;
                                    showFragment(currentSelected, menuItem);
                                }
                                break;
                            case R.id.nav_chat:
                                if (currentSelected == 4) {
                                    drawerLayout.closeDrawers();
                                } else {
                                    currentSelected = 4;
                                    showFragment(currentSelected, menuItem);
                                }
                                break;
                            case R.id.nav_geosearch:
                                if (currentSelected == 5) {
                                    drawerLayout.closeDrawers();
                                } else {
                                    currentSelected = 5;
                                    showFragment(currentSelected, menuItem);
                                }
                                break;
                            case R.id.nav_settings:
                                Intent intent = new Intent(MainActivity.this, ConfigurationActivity.class);
                                startActivityForResult(intent, CONFIGURATION);
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
            case -1:
                fragment = new Fragment();
                break;
            case 0:
                fragment = new HomeFragment();
                break;
            case 1:
                fragment = new ActivitiesFragment();
                break;
            case 2:
                fragment = new StatisticsFragment();
                break;
            case 3:
                fragment = new FriendsFragment();
                break;
            case 4:
                fragment = new ChatFragment();
                break;
            case 5:
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

        // Stop services
        stopService(new Intent(MainActivity.this, ChatNotificationsService.class));
        stopService(new Intent(MainActivity.this, NotificationsService.class));

        // Delete saved user session
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(SESSION_FILE, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit(); // commit changes

        // Delete saved user configuration
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editorConfig = sharedPref.edit();
        editorConfig.clear();
        editorConfig.commit(); // commit changes

        // Launch Main Activity -> Login Activity
        Intent intent = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    /**
     * Send Logcat in Mail
     */
    private void SendLogcatMail(){
        //File outputFile = new File(Environment.getExternalStorageState(),"logcat.txt");
        StringBuilder builder = new StringBuilder();
        try{
            String processId = Integer.toString(android.os.Process.myPid());
            String[] command = new String[] { "logcat", "-d", "-v", "threadtime" };
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains(processId)) {
                    builder.append(line);
                }
            }
        }
        catch (IOException e){e.printStackTrace();}

        // Send email
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("vnd.android.cursor.dir/email");
        String to[] = {"osporthello@gmail.com"};
        emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
        emailIntent.putExtra(Intent.EXTRA_TEXT, builder.toString());
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.send_error_report));
        startActivity(Intent.createChooser(emailIntent, getString(R.string.send_error_report)));
    }
}
