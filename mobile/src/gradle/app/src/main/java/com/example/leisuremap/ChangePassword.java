package com.example.leisuremap;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
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

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class ChangePassword extends AppCompatActivity {

    private Button b_send;
    EditText username_input, current_pass, new_pass, new_pass2;
    TextView guideText;
    boolean passChanged = false;

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
        b_send.setOnClickListener(view -> {
            try {
                printInfo();
            } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
                e.printStackTrace();
            }
        });
    }

    public void printInfo() throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
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
            EncryptPass encryptCrPass = new EncryptPass(crpass);
            EncryptPass encryptNewPass = new EncryptPass(newpass);
            changePass(username, encryptCrPass.encryption(), encryptNewPass.encryption());
            System.out.println("Change pass: " + "username: " + username + " password: " + encryptCrPass.encryption() + " newpass " + encryptNewPass.encryption());
        }
    }

    public void changePass(String username, String crPassword, String newPassword) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://.../user/update?username=" + username + "&oldpass=" + crPassword + "&newpass=" + newPassword;

        Intent intent = new Intent(this, MainMenu.class);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject status = response.getJSONObject("STATUS");
                    passChanged = (boolean) status.get("UPDATE");
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
                    guideText.setText("Could not change password");
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