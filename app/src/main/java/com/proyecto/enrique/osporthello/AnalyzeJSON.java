package com.proyecto.enrique.osporthello;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by enrique on 22/02/16.
 */
public class AnalyzeJSON {

    public static User analyzeUser(JSONObject jsonObject) throws JSONException {
        final String nombreObjeto = "data";
        String email = jsonObject.getJSONObject(nombreObjeto).getString("User_email");
        String firstname = jsonObject.getJSONObject(nombreObjeto).getString("User_firstname");
        String lastname = jsonObject.getJSONObject(nombreObjeto).getString("User_lastname");
        byte[] image = jsonObject.getJSONObject(nombreObjeto).getString("User_image").getBytes();
        String apiKey = jsonObject.getJSONObject(nombreObjeto).getString("User_apiKey");

        User user = new User(email,firstname,lastname,image,apiKey);
        return user;
    }

    public static ArrayList<User> analyzeAllUsers(JSONObject jsonObject) throws JSONException {
        ArrayList<User> usersList = new ArrayList<>();

        for (int i = 0; i < jsonObject.getJSONArray("data").length(); i++) {
            String email = jsonObject.getJSONArray("data").getJSONObject(i).getString("User_email");
            String firstname = jsonObject.getJSONArray("data").getJSONObject(i).getString("User_firstname");
            String lastname = jsonObject.getJSONArray("data").getJSONObject(i).getString("User_lastname");
            byte[] image = jsonObject.getJSONArray("data").getJSONObject(i).getString("User_image").getBytes();
            String apiKey = jsonObject.getJSONArray("data").getJSONObject(i).getString("User_apiKey");

            User user = new User(email,firstname,lastname,image,apiKey);
            usersList.add(user);
        }

        return usersList;
    }
}
