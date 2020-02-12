package com.gkouskos.unipitouristapp;

import android.content.Intent;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    EditText userText, passText;
    Button signinButton, signUpButton, signoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        userText = findViewById(R.id.user_edit);
        passText = findViewById(R.id.pass_edit);
        signinButton= findViewById(R.id.signin_button);
        signUpButton = findViewById(R.id.sign_up_button);
        signoutButton = findViewById(R.id.sign_out_button);

        signinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String userStr = userText.getText().toString().trim();
                String passStr = passText.getText().toString().trim();
                if(!TextUtils.isEmpty(userStr) && !TextUtils.isEmpty(passStr)) {

                    mAuth.signInWithEmailAndPassword(userStr, passStr)
                            .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        startActivity(new Intent (getApplicationContext(), MapsActivity.class));
                                        Toast.makeText(MainActivity.this, "Successfully signed in with: " + user.getEmail(), Toast.LENGTH_LONG).show();
                                    } else {

                                    }

                                    // ...
                                }
                            });

                } else {
                    Toast.makeText(MainActivity.this, "Please, enter email or password", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });


        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String userStr = userText.getText().toString().trim();
                String passStr = passText.getText().toString().trim();

                if(!TextUtils.isEmpty(userStr) && !TextUtils.isEmpty(passStr)) {

                    mAuth.createUserWithEmailAndPassword(userStr, passStr)
                            .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        startActivity(new Intent (getApplicationContext(), MapsActivity.class));
                                        Toast.makeText(MainActivity.this, "Registration completed with:" + user.getEmail(), Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(MainActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                } else {
                    Toast.makeText(MainActivity.this, "Please, enter email or password", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });


        signoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                userText.getText().clear();
                passText.getText().clear();
                Toast.makeText(MainActivity.this, "Signing out...", Toast.LENGTH_LONG).show();
            }
        });

    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        startActivity(new Intent (getApplicationContext(), MapsActivity.class));
        Toast.makeText(MainActivity.this, "Successfully signed in with: " + currentUser.getEmail(), Toast.LENGTH_SHORT).show();

    }
}
