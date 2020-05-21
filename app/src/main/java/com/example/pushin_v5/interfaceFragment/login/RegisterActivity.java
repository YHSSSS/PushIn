package com.example.pushin_v5.interfaceFragment.login;
/***********************************************************************
 This file is a register page for users to register an account to login
 ***********************************************************************/

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.pushin_v5.R;
import com.example.pushin_v5.inputTask.CheckEmailFormat;
import com.example.pushin_v5.jsonTask.HttpConnection;
import com.example.pushin_v5.jsonTask.ParseJson;

import java.util.concurrent.ExecutionException;

public class RegisterActivity extends Activity {
    HttpConnection httpConnection = new HttpConnection();
    String jsonstring = "";
    ParseJson pj = new ParseJson();
    EditText userEmail, userPassword, userCPassword;
    Button b_confirm, b_cancel;
    CheckEmailFormat ci = new CheckEmailFormat();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userEmail = findViewById(R.id.i_registerEmail);
        userPassword = findViewById(R.id.i_registerPassword);
        userCPassword = findViewById(R.id.i_registerCPassword);
        b_confirm = findViewById(R.id.b_registerConfirm);
        b_cancel = findViewById(R.id.b_registerCancel);

        b_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if the input fields are empty
                if (TextUtils.isEmpty(userEmail.getText().toString()) || TextUtils.isEmpty(userPassword.getText().toString())
                        || TextUtils.isEmpty(userCPassword.getText().toString())) {
                    Toast.makeText(RegisterActivity.this, "Empty field not allowed!",
                            Toast.LENGTH_SHORT).show();
                } else if (!userCPassword.getText().toString().equals(userPassword.getText().toString())) {
                    Toast.makeText(RegisterActivity.this, "Please enter same password!",
                            Toast.LENGTH_SHORT).show();
                } else if (!ci.isEmail(userEmail.getText().toString())) {
                    Toast.makeText(RegisterActivity.this, "Please enter valid email!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    //send a request the server to insert a new user
                    String tempUrl = "https://zeno.computing.dundee.ac.uk/2019-projects/jiaxinhuang/userRegisteration.php?email=" +
                            userEmail.getText().toString() + "&password=" + userPassword.getText().toString();
                    try {
                        jsonstring = httpConnection.execute(tempUrl).get();
                        if (jsonstring != null && pj.identification(jsonstring)) {
                            //successfully inserted and jump to login page
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            Bundle b = new Bundle();
                            startActivity(intent);
                        } else {
                            Toast.makeText(RegisterActivity.this, "Fail to register!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        //go back the login page
        b_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                Bundle b = new Bundle();
                startActivity(intent);
            }
        });
    }

}
