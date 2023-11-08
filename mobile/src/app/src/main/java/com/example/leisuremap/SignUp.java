package com.example.leisuremap;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
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

public class SignUp extends AppCompatActivity {

    private Button b_sign_up;
    EditText username, password, password2;
    TextView guideText;
    boolean accCreated = false;
    private CheckBox passwordCheckbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        b_sign_up = findViewById(R.id.signUp);
        b_sign_up.setOnClickListener(view -> {
            printInfo();
        });

        guideText = findViewById(R.id.guide);
        username = findViewById(R.id.inputUsername);
        password = findViewById(R.id.inputPassword);
        password2 = findViewById(R.id.inputPassword2);
        passwordCheckbox = findViewById(R.id.passwordCheckbox);

        passwordCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    password2.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    password2.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });
    }

    public void printInfo() {
        String un = username.getText().toString();
        String pw = password.getText().toString();
        String pw2 = password2.getText().toString();
        if(un.matches(""))
            guideText.setText("Please enter username");
        else if(pw.matches(""))
            guideText.setText("Please enter password");
        else if(pw2.matches(""))
            guideText.setText("Please confirm password");
        else if(!pw.matches(pw2))
            guideText.setText("Passwords do not match");
        else {
            HashPassword hashPassword = new HashPassword(pw);
            signUp(un, hashPassword.hashing());
        }
    }

    public void signUp(String username, String password){

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://.../user/create?username="+username+"&pass="+password;

        Intent intent = new Intent(this, MainMenu.class);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                String message = "";
                try {
                    JSONObject status = response.getJSONObject("STATUS");
                    accCreated = (boolean) status.get("SIGNUP");
                    message = (String) status.get("message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(accCreated) {
                    guideText.setText("Account created");
                    intent.putExtra("UserStatus", accCreated);
                    intent.putExtra("Username", username);
                    startActivity(intent);
                    Login.fa.finish();
                    MainMenu.fa.finish();
                    finish();
                }
                else
                    guideText.setText(message);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                guideText.setText("Connection problem");
            }
        });
        queue.add(request);
    }
}