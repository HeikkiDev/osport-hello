package com.proyecto.enrique.osporthello;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.proyecto.enrique.osporthello.Activities.MainActivity;

/**
 * Created by enrique on 9/04/16.
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

    public static void postFacebookLogin(String url, RequestParams params, JsonHttpResponseHandler responseHandler){
        client.setTimeout(6000);
        client.post(HOST + url, params, responseHandler);
    }

    public static void getUsersByName(String url, JsonHttpResponseHandler responseHandler){
        client.setTimeout(10000);
        client.get(HOST + url + "/" + MainActivity.USER_ME.getApiKey(), responseHandler);
    }

    /**
     * CHATS
     */

    public static void getUserChats(String url, JsonHttpResponseHandler responseHandler){
        client.setTimeout(10000);
        client.get(HOST + url + "/" + MainActivity.USER_ME.getApiKey(), responseHandler);
    }

    public static void postNewChat(String url, JsonHttpResponseHandler responseHandler){
        client.setTimeout(6000);
        client.post(HOST + url + "/" + MainActivity.USER_ME.getApiKey(), responseHandler);
    }

    public static void postNewMessage(String url, JsonHttpResponseHandler responseHandler){
        client.setTimeout(10000);
        client.post(HOST + url + "/" + MainActivity.USER_ME.getApiKey(), responseHandler);
    }

    public static void deleteChat(String url, JsonHttpResponseHandler responseHandler){
        client.setTimeout(6000);
        client.delete(HOST + url + "/" + MainActivity.USER_ME.getApiKey(), responseHandler);
    }

    public static void deletePairChat(String url, JsonHttpResponseHandler responseHandler){
        client.setTimeout(6000);
        client.delete(HOST + url + "/" + MainActivity.USER_ME.getApiKey(), responseHandler);
    }

    /**
     * FRIENDS
     */

    public static void postNewFriend(String url, JsonHttpResponseHandler responseHandler){
        client.setTimeout(6000);
        client.post(HOST + url + "/" + MainActivity.USER_ME.getApiKey(), responseHandler);
    }

    public static void deleteFriend(String url, JsonHttpResponseHandler responseHandler){
        client.setTimeout(6000);
        client.delete(HOST + url + "/" + MainActivity.USER_ME.getApiKey(), responseHandler);
    }
}
