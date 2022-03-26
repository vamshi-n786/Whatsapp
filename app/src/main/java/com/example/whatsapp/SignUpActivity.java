package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.whatsapp.Models.Users;
import com.example.whatsapp.databinding.ActivitySignUpBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {

     ActivitySignUpBinding binding;
     private FirebaseAuth mAuth;
     FirebaseDatabase database;
     ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Objects.requireNonNull(getSupportActionBar()).hide();
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        progressDialog=new ProgressDialog(SignUpActivity.this);
        progressDialog.setTitle("Creating Account");
        progressDialog.setMessage("We're creating your account");

        binding.btnSignUp.setOnClickListener(this::onClick);
    }

    private void onClick(View view) {

        progressDialog.show();
        mAuth.createUserWithEmailAndPassword
                (binding.email.getText().toString(), binding.password.getText().toString()).
                addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            Users user = new Users(binding.textView.getText().toString(),
                                    binding.email.getText().toString(), binding.password.getText().toString());
                            String id = Objects.requireNonNull(Objects.requireNonNull(task.getResult()).getUser()).getUid();
                            database.getReference().child("Users").
                                    child(id).setValue(user);

                            Toast.makeText(SignUpActivity.this, "User created succesfully",
                                    Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(SignUpActivity.this,
                                    Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

       binding.tvAlreadyAccount.setOnClickListener(this::onClick2);

    }

    private void onClick2(View view) {
        Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
        startActivity(intent);
    }
}