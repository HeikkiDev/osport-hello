package com.proyecto.enrique.osporthello;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.proyecto.enrique.osporthello.Activities.MainActivity;
import com.proyecto.enrique.osporthello.Models.Chat;
import com.proyecto.enrique.osporthello.Models.GeoSearch;
import com.proyecto.enrique.osporthello.Models.SportActivityInfo;
import com.proyecto.enrique.osporthello.Models.SportPercentage;
import com.proyecto.enrique.osporthello.Models.Statistic;
import com.proyecto.enrique.osporthello.Models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by enrique on 22/02/16.
 */
public class AnalyzeJSON {

    private static final String PREFERENCES_FILE = "osporthello_settings";

    public static User analyzeUserLogin(JSONObject jsonObject, Context context) throws JSONException {
        final String nombreObjeto = "data";
        String email = jsonObject.getJSONObject(nombreObjeto).getString("User_email");
        String firstname = jsonObject.getJSONObject(nombreObjeto).getString("User_firstname");
        String lastname = jsonObject.getJSONObject(nombreObjeto).getString("User_lastname");
        String image = jsonObject.getJSONObject(nombreObjeto).getString("User_image");
        String apiKey = jsonObject.getJSONObject(nombreObjeto).getString("User_apiKey");
        String age = jsonObject.getJSONObject(nombreObjeto).getString("User_age");
        String city = jsonObject.getJSONObject(nombreObjeto).getString("User_city");
        String weight = jsonObject.getJSONObject(nombreObjeto).getString("User_weight");
        String height = jsonObject.getJSONObject(nombreObjeto).getString("User_height");

        // Se ajusta la Configuración del usuario
        if(!jsonObject.isNull("data_aux") && !jsonObject.getString("data_aux").equals("false")) {
            int geosearch = jsonObject.getJSONObject("data_aux").getInt("Configuration_geosearch");
            float geoLatitude = 0;
            if(!jsonObject.getJSONObject("data_aux").isNull("Configuration_geoLat"))
                geoLatitude = (float) jsonObject.getJSONObject("data_aux").getDouble("Configuration_geoLat");
            float geoLongitude = 0;
            if(!jsonObject.getJSONObject("data_aux").isNull("Configuration_geoLon"))
                geoLongitude = (float) jsonObject.getJSONObject("data_aux").getDouble("Configuration_geoLon");
            int sportType = jsonObject.getJSONObject("data_aux").getInt("Configuration_sportType");
            int privacity = jsonObject.getJSONObject("data_aux").getInt("Configuration_privacity");
            float privLatitude = 0;
            if(!jsonObject.getJSONObject("data_aux").isNull("Configuration_privacityLat"))
                privLatitude = (float) jsonObject.getJSONObject("data_aux").getDouble("Configuration_privacityLat");
            float privLongitude = 0;
            if(!jsonObject.getJSONObject("data_aux").isNull("Configuration_privacityLon"))
                privLongitude = (float) jsonObject.getJSONObject("data_aux").getDouble("Configuration_privacityLon");
            int chatNotifications = jsonObject.getJSONObject("data_aux").getInt("Configuration_chatNotifications");
            int friendsNotifications = jsonObject.getJSONObject("data_aux").getInt("Configuration_friendsNotifications");

            SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt("geosearch", geosearch);
            editor.putInt("privacity", privacity);
            if (geoLatitude != 0 && geoLongitude != 0) {
                editor.putFloat("geolat", geoLatitude);
                editor.putFloat("geolon", geoLongitude);
            }
            editor.putInt("sporttype", sportType);
            if (privLatitude != 0 && privLongitude != 0) {
                editor.putFloat("privacitylat", privLatitude);
                editor.putFloat("privacitylon", privLongitude);
            }
            editor.putInt("chatnotifications", chatNotifications);
            editor.putInt("friendsnotification", friendsNotifications);
            editor.apply();
        }

        if(lastname.equals("null"))
            lastname = null;
        if(image.equals("null"))
            image = null;
        if(city.equals("null"))
            city = null;
        if(age.equals("null") || age.equals("0"))
            age = null;
        if(weight.equals("null") || weight.equals("0"))
            weight = null;
        if(height.equals("null") || height.equals("0"))
            height = null;

        // Se devuelven los datos del Usuario
        User user = new User(email,firstname,lastname, image ,apiKey,age,city,weight,height);
        return user;
    }

    public static User analyzeUser(JSONObject jsonObject) throws JSONException {
        final String nombreObjeto = "data";
        String email = jsonObject.getJSONObject(nombreObjeto).getString("User_email");
        String firstname = jsonObject.getJSONObject(nombreObjeto).getString("User_firstname");
        String lastname = jsonObject.getJSONObject(nombreObjeto).getString("User_lastname");
        String image = jsonObject.getJSONObject(nombreObjeto).getString("User_image");
        String apiKey = jsonObject.getJSONObject(nombreObjeto).getString("User_apiKey");
        String age = jsonObject.getJSONObject(nombreObjeto).getString("User_age");
        String city = jsonObject.getJSONObject(nombreObjeto).getString("User_city");
        String weight = jsonObject.getJSONObject(nombreObjeto).getString("User_weight");
        String height = jsonObject.getJSONObject(nombreObjeto).getString("User_height");

        if(lastname.equals("null"))
            lastname = null;
        if(image.equals("null"))
            image = null;
        if(city.equals("null"))
            city = null;
        if(age.equals("null") || age.equals("0"))
            age = null;
        if(weight.equals("null") || weight.equals("0"))
            weight = null;
        if(height.equals("null") || height.equals("0"))
            height = null;

        User user = new User(email,firstname,lastname, image ,apiKey,age,city,weight,height);
        return user;
    }

    public static User analyzeUserNameImage(JSONObject jsonObject) throws JSONException {
        final String nombreObjeto = "data";
        String firstname = jsonObject.getJSONObject(nombreObjeto).getString("User_firstname");
        String lastname = jsonObject.getJSONObject(nombreObjeto).getString("User_lastname");
        String image = jsonObject.getJSONObject(nombreObjeto).getString("User_image");

        if(lastname.equals("null"))
            lastname = null;
        if(image.equals("null"))
            image = null;

        User user = new User();
        user.setFirstname(firstname);
        user.setLastname(lastname);
        user.setImage(image);
        return user;
    }

    public static ArrayList<User> analyzeAllUsers(JSONObject jsonObject) throws JSONException {
        ArrayList<User> usersList = new ArrayList<>();

        for (int i = 0; i < jsonObject.getJSONArray("data").length(); i++) {
            String email = jsonObject.getJSONArray("data").getJSONObject(i).getString("User_email");
            String firstname = jsonObject.getJSONArray("data").getJSONObject(i).getString("User_firstname");
            String lastname = jsonObject.getJSONArray("data").getJSONObject(i).getString("User_lastname");
            String city = jsonObject.getJSONArray("data").getJSONObject(i).getString("User_city");

            if(email.equals(MainActivity.USER_ME.getEmail()))
                continue;
            if(lastname.equals("null"))
                lastname = null;
            if(city.equals("null"))
                city = null;

            User user = new User();
            user.setEmail(email);
            user.setFirstname(firstname);
            user.setLastname(lastname);
            user.setCity(city);
            usersList.add(user);
        }

        return usersList;
    }

    public static ArrayList<GeoSearch> analyzeGeoSearchUsers(JSONObject jsonObject) throws JSONException {
        ArrayList<GeoSearch> usersList = new ArrayList<>();

        for (int i = 0; i < jsonObject.getJSONArray("data").length(); i++) {
            String email = jsonObject.getJSONArray("data").getJSONObject(i).getString("User_email");
            String firstname = jsonObject.getJSONArray("data").getJSONObject(i).getString("User_firstname");
            String lastname = jsonObject.getJSONArray("data").getJSONObject(i).getString("User_lastname");
            String city = jsonObject.getJSONArray("data").getJSONObject(i).getString("User_city");
            double distance = jsonObject.getJSONArray("data").getJSONObject(i).getDouble("Distance");

            if(email.equals(MainActivity.USER_ME.getEmail()))
                continue;
            if(lastname.equals("null"))
                lastname = null;
            if(city.equals("null"))
                city = null;

            GeoSearch user = new GeoSearch(email, firstname, lastname, city, distance);
            usersList.add(user);
        }

        return usersList;
    }

    public static ArrayList<User> analyzeMyFriends(JSONObject jsonObject) throws JSONException {
        ArrayList<User> usersList = new ArrayList<>();

        for (int i = 0; i < jsonObject.getJSONArray("data_aux").length(); i++) {
            String email = jsonObject.getJSONArray("data_aux").getJSONObject(i).getString("User_email");

            if(email.equals(MainActivity.USER_ME.getEmail()))
                continue;

            User user = new User();
            user.setEmail(email);
            usersList.add(user);
        }

        return usersList;
    }

    public static ArrayList<Chat> analyzeChats(JSONObject jsonObject) throws JSONException{
        ArrayList<Chat> chatsList = new ArrayList<>();

        for (int i = 0; i < jsonObject.getJSONArray("data").length(); i++) {
            int id = jsonObject.getJSONArray("data").getJSONObject(i).getInt("Chat_id");
            String email = jsonObject.getJSONArray("data").getJSONObject(i).getString("Chat_receiver");
            String username = jsonObject.getJSONArray("data").getJSONObject(i).getString("Username");
            String image = jsonObject.getJSONArray("data").getJSONObject(i).getString("User_image");

            if(image.equals("null"))
                image = null;

            Chat chat = new Chat(id, email, username, image);
            chatsList.add(chat);
        }

        return chatsList;
    }

    public static ArrayList<Chat> analyzeCheckChats(JSONObject jsonObject) throws JSONException{
        ArrayList<Chat> chatsList = new ArrayList<>();

        for (int i = 0; i < jsonObject.getJSONArray("data").length(); i++) {
            int id = jsonObject.getJSONArray("data").getJSONObject(i).getInt("Chat_id");
            String email = jsonObject.getJSONArray("data").getJSONObject(i).getString("Chat_receiver");
            String username = jsonObject.getJSONArray("data").getJSONObject(i).getString("Username");

            Chat chat = new Chat(id, email, username, null);
            chatsList.add(chat);
        }

        return chatsList;
    }

    public static ArrayList<SportActivityInfo> analyzeListActivities(JSONObject jsonObject) throws JSONException {
        ArrayList<SportActivityInfo> activitiesList = new ArrayList<>();

        for (int i = 0; i < jsonObject.getJSONArray("data").length(); i++) {
            int id = jsonObject.getJSONArray("data").getJSONObject(i).getInt("Activity_id");
            String email = jsonObject.getJSONArray("data").getJSONObject(i).getString("Activity_userEmail");
            String name = jsonObject.getJSONArray("data").getJSONObject(i).getString("Activity_name");
            String date = jsonObject.getJSONArray("data").getJSONObject(i).getString("Activity_date");
            double avgSpeed = jsonObject.getJSONArray("data").getJSONObject(i).getDouble("Activity_avSpeed");
            int calories = jsonObject.getJSONArray("data").getJSONObject(i).getInt("Activity_calories");
            long duration = jsonObject.getJSONArray("data").getJSONObject(i).getLong("Activity_duration");
            double distance = jsonObject.getJSONArray("data").getJSONObject(i).getDouble("Activity_distance");
            int sportType = jsonObject.getJSONArray("data").getJSONObject(i).getInt("Activity_sportType");
            int distanceUnits = jsonObject.getJSONArray("data").getJSONObject(i).getInt("Activity_distanceUnits");
            int speedUnits = jsonObject.getJSONArray("data").getJSONObject(i).getInt("Activity_speedUnits");
            String geoPoints = jsonObject.getJSONArray("data").getJSONObject(i).getString("Activity_geoPoints");

            SportActivityInfo activity =
                    new SportActivityInfo(id,email,name,date,avgSpeed,calories,duration,distance,sportType,distanceUnits,speedUnits,geoPoints);
            activitiesList.add(activity);
        }

        return activitiesList;
    }

    public static ArrayList<Statistic> analyzeStatistics(JSONObject jsonObject) throws JSONException {
        ArrayList<Statistic> statisticsList = new ArrayList<>();

        for (int i = 0; i < jsonObject.getJSONArray("data").length(); i++) {
            int dayOrMonth = jsonObject.getJSONArray("data").getJSONObject(i).getInt("Date");
            double kms = jsonObject.getJSONArray("data").getJSONObject(i).getDouble("DistanceKms");
            double miles = jsonObject.getJSONArray("data").getJSONObject(i).getDouble("DistanceMiles");

            statisticsList.add(new Statistic(dayOrMonth, kms, miles));
        }

        return statisticsList;
    }

    public static ArrayList<SportPercentage> analyzeSportsPercentage(JSONObject jsonObject) throws JSONException {
        ArrayList<SportPercentage> percentageList = new ArrayList<>();

        for (int i = 0; i < jsonObject.getJSONArray("data").length(); i++) {
            int type = jsonObject.getJSONArray("data").getJSONObject(i).getInt("SportType");
            double percentage = jsonObject.getJSONArray("data").getJSONObject(i).getDouble("Percentage");

            percentageList.add(new SportPercentage(type, percentage));
        }

        return percentageList;
    }
}
