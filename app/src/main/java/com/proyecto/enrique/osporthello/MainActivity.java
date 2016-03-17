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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private boolean isFirstTime;
    private int currentSelected;

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private static User USER_ME;
    private final int LOGIN_CODE = 1;
    public static final String HOST = "http://10.0.2.2/osporthello/";
    private static final String USER_FIRST_TIME = "first_time";
    private static final String PREFERENCES_FILE = "osporthello_settings";
    private static final String SESSION_FILE = "my_session";
    private static final String STATE_SELECTED_POSITION = "state_selected_position";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
            // Retrieve user session and information
            View headerLayout = navigationView.getHeaderView(0); // 0-index heade
            TextView txtUsername = (TextView)headerLayout.findViewById(R.id.header_username);
            TextView txtEmail = (TextView)headerLayout.findViewById(R.id.header_email);
            //CircleImageView circleImage = (CircleImageView)headerLayout.findViewById(R.id.header_circle_image);
            txtUsername.setText(sharedPreferences.getString("firstname", null));
            txtEmail.setText(sharedPreferences.getString("email", null));
            //circleImage.setImageBitmap( BitmapFactory.decodeByteArray(user.getImage(), 0, user.getImage().length));
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

        // Default selection HOME, or currentSelected
        showFragment(currentSelected);
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
        Bundle bundle = data.getExtras();
        User user = (User)bundle.getSerializable("user");
        this.USER_ME = user;

        // Set user information
        View headerLayout = navigationView.getHeaderView(0); // 0-index heade
        TextView txtUsername = (TextView)headerLayout.findViewById(R.id.header_username);
        TextView txtEmail = (TextView)headerLayout.findViewById(R.id.header_email);
        //CircleImageView circleImage = (CircleImageView)headerLayout.findViewById(R.id.header_circle_image);
        txtUsername.setText(user.getFirstname().toString());
        txtEmail.setText(user.getEmail().toString());
        //circleImage.setImageBitmap( BitmapFactory.decodeByteArray(user.getImage(), 0, user.getImage().length));

        // Save the user session
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(SESSION_FILE, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email", user.getEmail());
        editor.putString("image", user.getImage().toString());
        editor.putString("firstname", user.getFirstname());
        editor.putString("lastname", user.getLastname());
        editor.putString("apikey", user.getApiKey());
        editor.commit(); // commit changes
    }

    /**
     * Save state of selection in sliding menu
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, currentSelected);
    }

    /**
     * Restore selected option in sliding menu
     * @param savedInstanceState
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentSelected = savedInstanceState.getInt(STATE_SELECTED_POSITION, 0);
        Menu menu = navigationView.getMenu();
        menu.getItem(currentSelected).setChecked(true);
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
                                    menuItem.setChecked(true);
                                    currentSelected = 0;
                                    showFragment(currentSelected);
                                }
                                break;
                            case R.id.nav_activities:
                                if (currentSelected == 1) {
                                    drawerLayout.closeDrawers();
                                } else {
                                    menuItem.setChecked(true);
                                    currentSelected = 1;
                                    showFragment(currentSelected);
                                }
                                break;
                            case R.id.nav_friends:
                                if (currentSelected == 2) {
                                    drawerLayout.closeDrawers();
                                } else {
                                    menuItem.setChecked(true);
                                    currentSelected = 2;
                                    showFragment(currentSelected);
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
    private void showFragment(int position) {
        Fragment fragment = null;

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
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager
                .beginTransaction()
                .replace(R.id.main_content, fragment)
                .commit();

        // Set fragment title
        setTitle(navigationView.getMenu().getItem(position).getTitle().toString());
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
