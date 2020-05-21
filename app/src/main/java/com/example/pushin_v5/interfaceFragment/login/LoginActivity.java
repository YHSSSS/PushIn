package com.example.pushin_v5.interfaceFragment.login;
/***********************************************************************
 This file is a login page for users to enter the account email and
 password. Users are able to login if the identification is success.
 ***********************************************************************/

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.example.pushin_v5.MainActivity;
import com.example.pushin_v5.R;
import com.example.pushin_v5.inputTask.CheckEmailFormat;
import com.example.pushin_v5.inputTask.MD5;
import com.example.pushin_v5.jsonTask.HttpConnection;
import com.example.pushin_v5.jsonTask.ParseJson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class LoginActivity extends FragmentActivity {
    HttpConnection httpConnection;
    String jsonstring = "";
    int userId = 0;
    ParseJson pj = new ParseJson();
    CheckEmailFormat ci = new CheckEmailFormat();
    Button b_login, b_register;
    EditText email, pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        b_login = findViewById(R.id.b_login);
        b_register = findViewById(R.id.b_register);
        email = findViewById(R.id.email);
        pass = findViewById(R.id.pass);

        b_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(email.getText().toString()) || TextUtils.isEmpty(pass.getText().toString())) {
                    Toast.makeText(LoginActivity.this, "Empty field not allowed!",
                            Toast.LENGTH_SHORT).show();
                } else if (!ci.isEmail(email.getText().toString())) {
                    Toast.makeText(LoginActivity.this, "Please enter valid email!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "Proceed..",
                            Toast.LENGTH_SHORT).show();

                    //using MD5 to encrypt the password
                    MD5 encrypyt = new MD5();
                    String password = encrypyt.MD5(pass.getText().toString());
                    //get json string
                    String tempUrl = "https://zeno.computing.dundee.ac.uk/2019-projects/jiaxinhuang/userIdentification.php?email=" +
                            email.getText().toString() + "&password=" + password;
                    try {
                        httpConnection = new HttpConnection();
                        jsonstring = httpConnection.execute(tempUrl).get();
                        if (jsonstring != null) {
                            if (pj.identification(jsonstring)) {
                                //get user id
                                JSONObject user = new JSONObject(jsonstring).getJSONArray("user").getJSONObject(0);
                                userId = user.getInt("userId");
                                Bundle b = new Bundle();
                                b.putInt("userId", userId);
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.putExtras(b);
                                startActivity(intent);
                            } else {
                                Toast.makeText(LoginActivity.this, "Email or password is incorrect!",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        b_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                Bundle b = new Bundle();
                startActivity(intent);
            }
        });
    }

}
