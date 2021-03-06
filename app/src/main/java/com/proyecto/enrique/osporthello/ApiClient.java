package com.proyecto.enrique.osporthello;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.proyecto.enrique.osporthello.Activities.MainActivity;

/**
 * Autor: Enrique Ramos
 * Fecha última actualización: 12/06/2016
 * Descripción: Clase apoyo que facilita la búsqueda y organización de todas las peticiones
 * que se le hacen a la Api.
 */

public class ApiClient {

    private static final String HOST = "https://enriqueramos.info/osporthello/";

    private static AsyncHttpClient client = new AsyncHttpClient(true, 80, 443);


    /**
     * USERS
     */

    public static void getUserLogin(String url, RequestParams params, JsonHttpResponseHandler responseHandler){
        client.setTimeout(6000);
        client.get(HOST + url, params, responseHandler);
    }

    public static void getRestorePassword(String url, JsonHttpResponseHandler responseHandler){
        client.setTimeout(6000);
        client.get(HOST + url, responseHandler);
    }

    public static void postFacebookLogin(String url, RequestParams params, JsonHttpResponseHandler responseHandler){
        client.setTimeout(6000);
        client.post(HOST + url, params, responseHandler);
    }

    public static void getUserName(String url, String apiKey, JsonHttpResponseHandler responseHandler){
        client.setTimeout(6000);
        client.get(HOST + url + "/" + apiKey, responseHandler);
    }

    public static void getUsersByName(String url, JsonHttpResponseHandler responseHandler){
        client.setTimeout(10000);
        client.get(HOST + url + "/" + MainActivity.USER_ME.getApiKey(), responseHandler);
    }

    public static void getGeoSearch(String url, JsonHttpResponseHandler responseHandler){
        client.setTimeout(10000);
        client.get(HOST + url + "/" + MainActivity.USER_ME.getApiKey(), responseHandler);
    }

    public static void deleteUserAccount(String url, JsonHttpResponseHandler responseHandler){
        client.setTimeout(6000);
        client.delete(HOST + url + "/" + MainActivity.USER_ME.getApiKey(), responseHandler);
    }

    /**
     * CHATS
     */

    public static void getUserChats(String url, JsonHttpResponseHandler responseHandler){
        client.setTimeout(10000);
        client.get(HOST + url, responseHandler);
    }

    public static void postNewChat(String url, JsonHttpResponseHandler responseHandler){
        client.setTimeout(6000);
        client.post(HOST + url, responseHandler);
    }

    public static void postNewMessage(String url, JsonHttpResponseHandler responseHandler){
        client.setTimeout(10000);
        client.post(HOST + url, responseHandler);
    }

    public static void deleteChat(String url, JsonHttpResponseHandler responseHandler){
        client.setTimeout(6000);
        client.delete(HOST + url, responseHandler);
    }

    public static void deletePairChat(String url, JsonHttpResponseHandler responseHandler){
        client.setTimeout(6000);
        client.delete(HOST + url, responseHandler);
    }

    /**
     * FRIENDS
     */

    public static void getMyFriends(String url, JsonHttpResponseHandler responseHandler){
        client.setTimeout(6000);
        client.get(HOST + url + "/" + MainActivity.USER_ME.getApiKey(), responseHandler);
    }

    public static void postNewFriend(String url, JsonHttpResponseHandler responseHandler){
        client.setTimeout(6000);
        client.post(HOST + url + "/" + MainActivity.USER_ME.getApiKey(), responseHandler);
    }

    public static void deleteFriend(String url, JsonHttpResponseHandler responseHandler){
        client.setTimeout(6000);
        client.delete(HOST + url + "/" + MainActivity.USER_ME.getApiKey(), responseHandler);
    }

    /**
     * ACTIVITIES
     */
    public static void getMyActivities(String url, JsonHttpResponseHandler responseHandler){
        client.setTimeout(10000);
        client.get(HOST + url + "/" + MainActivity.USER_ME.getApiKey(), responseHandler);
    }

    public static void getFriendsActivities(String url, JsonHttpResponseHandler responseHandler){
        client.setTimeout(10000);
        client.get(HOST + url + "/" + MainActivity.USER_ME.getApiKey(), responseHandler);
    }

    public static void deleteMapActivity(String url, JsonHttpResponseHandler responseHandler){
        client.setTimeout(6000);
        client.delete(HOST + url + "/" + MainActivity.USER_ME.getApiKey(), responseHandler);
    }

    /**
     * CONFIGURATION
     */
    public static void postMyConfiguration(String url, RequestParams params, JsonHttpResponseHandler responseHandler){
        client.setTimeout(10000);
        client.post(HOST + url + "/" + MainActivity.USER_ME.getApiKey(), params, responseHandler);
    }

    /**
     * STATISTICS
     */
    public static void getMyStatistics(String url, JsonHttpResponseHandler responseHandler){
        client.setTimeout(10000);
        client.get(HOST + url + "/" + MainActivity.USER_ME.getApiKey(), responseHandler);
    }

    public static void getSportsPercentage(String url, JsonHttpResponseHandler responseHandler){
        client.setTimeout(10000);
        client.get(HOST + url + "/" + MainActivity.USER_ME.getApiKey(), responseHandler);
    }

    /**
     * ERROR MAIL
     */
    public static void postErrorMail(String url, RequestParams params, JsonHttpResponseHandler responseHandler){
        client.setTimeout(6000);
        client.post(HOST + url + "/" + MainActivity.USER_ME.getApiKey(), params, responseHandler);
    }
}
