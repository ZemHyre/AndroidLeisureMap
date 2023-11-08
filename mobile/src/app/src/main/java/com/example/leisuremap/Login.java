package com.example.leisuremap;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Login extends AppCompatActivity {

    private Button b_login, b_sign_up, b_change_pass;
    EditText username, password;
    TextView guideText;
    boolean isLoggedIn = false;
    boolean status = true;
    public static Activity fa;
    private CheckBox passwordCheckbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        fa = this;

        b_login = findViewById(R.id.signIn);
        b_login.setOnClickListener(view -> {
            printInfo();
        });

        b_change_pass = findViewById(R.id.changePass);
        b_change_pass.setOnClickListener(view -> openForgotPass());

        b_sign_up = findViewById(R.id.signUp);
        b_sign_up.setOnClickListener(view -> openSignUp());

        guideText = findViewById(R.id.guide);
        username = findViewById(R.id.inputUsername);
        password = findViewById(R.id.inputPassword);
        passwordCheckbox = findViewById(R.id.passwordCheckbox);
        passwordCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

    }


    protected void onStart() {
        super.onStart();
        SessionManagement sessionManagement = new SessionManagement(Login.this);
        String un = sessionManagement.getSessionUsername();
        String pw = sessionManagement.getSessionPassword();
        boolean st = sessionManagement.getSessionStatus();

        if (un != null && pw != null && st == true ) {
            Login login = new Login();
            login.moveToMainMenu();
        }
    }

    public void openForgotPass() {
        Intent intent = new Intent(this, ChangePassword.class);
        startActivity(intent);
    }

    public void openSignUp() {
        Intent intent = new Intent(this, SignUp.class);
        startActivity(intent);
    }

    public void printInfo() {
        String un = username.getText().toString();
        String pw = password.getText().toString();
        if(un.matches(""))
            guideText.setText("Please enter username");
        else if(pw.matches(""))
            guideText.setText("Please enter password");
        else {
            logIn(un, pw);
        }
    }

    public void logIn(String username, String password) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://.../user/login?username=" + username + "&pass=" + password;

        Intent intent = new Intent(this, MainMenu.class);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    JSONObject status = response.getJSONObject("STATUS");
                    isLoggedIn = (boolean) status.get("LOGIN");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(isLoggedIn) {
                    guideText.setText("Logged in");
                    intent.putExtra("UserStatus", isLoggedIn);
                    intent.putExtra("Username", username);
                    SimpleDateFormat formatter= new SimpleDateFormat("HH:mm");
                    Date startTime = new Date(System.currentTimeMillis());
                    String time = formatter.format(startTime);
                    intent.putExtra("startTime", time);
                    startActivity(intent);
                    MainMenu.fa.finish();
                    finish();
                }
                else
                    guideText.setText("Could not log in");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                guideText.setText("Connection problem");
            }
        });
        queue.add(request);
    }

    public void saveLoggedInSession(View view){
        SessionManagement sessionManagement = new SessionManagement(Login.this);
        sessionManagement.saveSession(username.getText().toString(), password.getText().toString(), status);
        moveToMainMenu();
    }

    public void moveToMainMenu(){
        Intent intent = new Intent(Login.this, MainMenu.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}