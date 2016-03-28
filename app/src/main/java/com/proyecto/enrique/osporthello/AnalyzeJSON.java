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
        String image = jsonObject.getJSONObject(nombreObjeto).getString("User_image");
        String apiKey = jsonObject.getJSONObject(nombreObjeto).getString("User_apiKey");
        String sex = jsonObject.getJSONObject(nombreObjeto).getString("User_sex");
        String age = jsonObject.getJSONObject(nombreObjeto).getString("User_age");
        String city = jsonObject.getJSONObject(nombreObjeto).getString("User_city");
        String weight = jsonObject.getJSONObject(nombreObjeto).getString("User_weight");
        String height = jsonObject.getJSONObject(nombreObjeto).getString("User_height");

        if(lastname.equals("null"))
            lastname = null;
        if(image.equals("null"))
            image = null;
        if(sex.equals("null"))
            sex = null;
        if(city.equals("null"))
            city = null;
        if(age.equals("null") || age.equals("0"))
            age = null;
        if(weight.equals("null") || weight.equals("0"))
            weight = null;
        if(height.equals("null") || height.equals("0"))
            height = null;

        User user = new User(email,firstname,lastname, image ,apiKey,sex,age,city,weight,height);
        return user;
    }

    public static ArrayList<User> analyzeAllUsers(JSONObject jsonObject) throws JSONException {
        ArrayList<User> usersList = new ArrayList<>();

        for (int i = 0; i < jsonObject.getJSONArray("data").length(); i++) {
            String email = jsonObject.getJSONArray("data").getJSONObject(i).getString("User_email");
            String firstname = jsonObject.getJSONArray("data").getJSONObject(i).getString("User_firstname");
            String lastname = jsonObject.getJSONArray("data").getJSONObject(i).getString("User_lastname");
            String image = jsonObject.getJSONArray("data").getJSONObject(i).getString("User_image");
            String city = jsonObject.getJSONArray("data").getJSONObject(i).getString("User_city");

            if(email.equals(MainActivity.USER_ME.getEmail()))
                break;
            if(lastname.equals("null"))
                lastname = null;
            if(image.equals("null"))
                image = null;
            if(city.equals("null"))
                city = null;

            User user = new User(email,firstname,lastname,image,city);
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
}
