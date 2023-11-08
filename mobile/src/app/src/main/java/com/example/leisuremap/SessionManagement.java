package com.example.leisuremap;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManagement {

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    public final String sharedPrefName= "shared_preferences";
    public final String usernameKey = "username_key";
    public final String passwordKey = "password_key";
    public final String statusKey = "is_logged_in";

    public SessionManagement(Context context) {
        //Context.MODE_PRIVATE - keeps the files private and secures the user’s data
        sharedPreferences = context.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    //save string value in a preference editor - saves session of a user when he is logged in
    public void saveSession(String username, String password, boolean status) {
        editor.putString(usernameKey, username).commit();
        editor.putString(passwordKey, password).commit();
        editor.putBoolean(statusKey, status).commit();
        //editor.apply();
    }

    //return a username of the user whose session is saved, data can be retrieved from shared preferences by calling getString()
    public String getSessionUsername() {
        // returns stored preference value
        //If value is not present return second parameter value - null
        return sharedPreferences.getString(usernameKey, null);
    }

    public String getSessionPassword() {
        return sharedPreferences.getString(passwordKey, null);
    }

    public boolean getSessionStatus() {
        return sharedPreferences.getBoolean(statusKey, true);
    }

    public void removeSession() {
        editor.putString(usernameKey, null).commit();
        editor.putString(passwordKey, null).commit();
        editor.putBoolean(statusKey, false).commit();
    }
}
