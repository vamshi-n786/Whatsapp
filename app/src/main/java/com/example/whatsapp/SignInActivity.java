package com.example.whatsapp;



import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


import com.example.whatsapp.Models.Users;
import com.example.whatsapp.databinding.ActivitySignInBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class SignInActivity extends AppCompatActivity {
    ActivitySignInBinding binding;

    private static int RC_SIGN_IN =65;
    ProgressDialog progressDailog;
    FirebaseAuth mAuth;
    GoogleSignInClient mGoogleSignInClient;
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Objects.requireNonNull(getSupportActionBar()).hide();

        mAuth=FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        progressDailog =  new ProgressDialog(SignInActivity.this);
        progressDailog.setTitle("Login");
        progressDailog.setMessage("Signing into account");


        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("574512452959-4d7j57ti22pkthm42i997kj1lied9cps.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleSignInClient= GoogleSignIn.getClient(this,gso);


        binding.btnSignIn.setOnClickListener(view -> {
        progressDailog.show();
        mAuth.signInWithEmailAndPassword(binding.email.getText().toString(),
                binding.editTextTextPassword.getText().toString()).
                addOnCompleteListener(task -> {
                    progressDailog.dismiss();
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                        SignInActivity.this.startActivity(intent);
                    } else {

                        Toast.makeText(SignInActivity.this,
                                Objects.requireNonNull(task.getException()).
                                        getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        });
        binding.tvClickSignUp.setOnClickListener(view -> {
            Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
            SignInActivity.this.startActivity(intent);
        });

        binding.btnGoogle.setOnClickListener(view -> signIn());
        if(mAuth.getCurrentUser()!=null)
        {
            Intent intent = new Intent(SignInActivity.this,MainActivity.class);
            startActivity(intent);
        }

    }


    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);//on click google sign in can be done by google mail id

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("TAG", "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("TAG", "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("TAG", "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        Users users = new Users();
                        assert user != null;
                        users.setUserId(user.getUid());
                        users.setUserName(user.getDisplayName());
                        users.setProfilepic(Objects.requireNonNull(user.getPhotoUrl()).toString());
                        database.getReference().child("Users").child(user.getUid()).setValue(users);


                        Intent intent = new Intent(SignInActivity.this,MainActivity.class);
                        startActivity(intent);
                        Toast.makeText(SignInActivity.this, "Sign in with Google", Toast.LENGTH_SHORT).show();
                       //updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("TAG", "signInWithCredential:failure", task.getException());
                        Toast.makeText(SignInActivity.this, Objects.requireNonNull(task.getException()).getMessage(),  Toast.LENGTH_SHORT).show();
                       // Snackbar.make(binding.getRoot(),"Authentication failed",Snackbar.LENGTH_SHORT).show();
                        //  updateUI(null);
                    }
                });
    }

}