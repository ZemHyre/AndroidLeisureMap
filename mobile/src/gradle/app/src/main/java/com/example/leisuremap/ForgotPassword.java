package com.example.leisuremap;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ForgotPassword extends AppCompatActivity {

    private Button b_send;
    EditText email;
    TextView guideText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        guideText = findViewById(R.id.guide);
        b_send.setOnClickListener(view -> printInfo());
    }

    public void printInfo() {
        String em = email.getText().toString();
        if(em.matches(""))
            guideText.setText("Please enter email address");
        else if(!Patterns.EMAIL_ADDRESS.matcher(em).matches())
            guideText.setText("Incorrect email address, try again");
        else {
            guideText.setText("We have sent an email to change your password");
            System.out.println(em);
            //TODO send link to change password to the email address
            // check if the email is registered (look for the email in database)
        }
    }
}