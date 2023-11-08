package com.example.leisuremap;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManagement {

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    public final String sharedPrefName= "shared_preferences";
    public final String usernameKey = "username_key";
    public final String passwordKey = "password_key";

    public SessionManagement(Context context) {
        //Context.MODE_PRIVATE - keeps the files private and secures the user’s data
        sharedPreferences = context.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    //save session of a user when he is logged in
    public void saveSession(String username, String password) {
        editor.putString(usernameKey, username).commit();
        editor.putString(passwordKey, password).commit();
        editor.apply();
    }

    //return a username and password of the user whose session is saved
    public String getSessionUsername() {
        return sharedPreferences.getString(usernameKey, null);
    }

    public String getSessionPassword() {
        return sharedPreferences.getString(passwordKey, null);
    }

    public void removeSession() {
        editor.putString(usernameKey, null).commit();
        editor.putString(passwordKey, null).commit();
    }

}
