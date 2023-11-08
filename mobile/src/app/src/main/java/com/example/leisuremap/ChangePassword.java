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

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class ChangePassword extends AppCompatActivity {

    private Button b_send;
    EditText username_input, current_pass, new_pass, new_pass2;
    TextView guideText;
    boolean passChanged = false;
    private CheckBox passwordCheckbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        guideText = findViewById(R.id.guide);
        username_input = findViewById(R.id.inputUsername);
        current_pass = findViewById(R.id.inputCurrentPass);
        new_pass = findViewById(R.id.inputNewPass);
        new_pass2 = findViewById(R.id.inputNewPass2);
        b_send = findViewById(R.id.changePass);
        passwordCheckbox = findViewById(R.id.passwordCheckbox);

        passwordCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    current_pass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    new_pass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    new_pass2.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    current_pass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    new_pass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    new_pass2.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        b_send.setOnClickListener(view -> {
            printInfo();
        });
    }

    public void printInfo() {
        String username = username_input.getText().toString();
        String crpass = current_pass.getText().toString();
        String newpass = new_pass.getText().toString();
        String newpass2 = new_pass2.getText().toString();
        if(username.matches(""))
            guideText.setText("Please enter username");
        else if (crpass.matches(""))
            guideText.setText("Please enter current password");
        else if (newpass.matches(""))
            guideText.setText("Please enter new password");
        else if (newpass2.matches(""))
            guideText.setText("Please confirm new password");
        else if (!newpass.matches(newpass2))
            guideText.setText("Entered passwords do not match");
        else {
            HashPassword hashNewPass = new HashPassword(newpass);
            changePass(username, crpass, hashNewPass.hashing());
        }
    }

    public void changePass(String username, String crPassword, String newPassword) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://.../user/update?username=" + username + "&oldpass=" + crPassword + "&newpass=" + newPassword;

        Intent intent = new Intent(this, MainMenu.class);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                String message = "";
                try {
                    JSONObject status = response.getJSONObject("STATUS");
                    passChanged = (boolean) status.get("UPDATE");
                    message = (String) status.get("message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(passChanged) {
                    guideText.setText("Password changed");
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