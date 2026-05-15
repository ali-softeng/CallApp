package com.example.callapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.example.callapp.databinding.ActivityLoginBinding;

public class Login extends AppCompatActivity {

    ActivityLoginBinding binding;
    AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_login);
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        binding.setLifecycleOwner(this);

        if(viewModel.getUser() != null){
            Intent intent = new Intent(this,HomeScreen.class);
            startActivity(intent);
            finish();
        }

        binding.loginBtn.setOnClickListener(v -> {
            String email = binding.email.getText().toString();
            String password = binding.password.getText().toString();

            if(email.isEmpty()){
                binding.email.setError("Email is required");
                binding.email.requestFocus();
                return;

            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.email.setError("Please provide a valid email");
                binding.email.requestFocus();
                return;
            }

            if(password.isEmpty()){
                binding.password.setError("Password is required");
                binding.password.requestFocus();
                return;
            }

            viewModel.login(email,password, task -> {
                if(task.isSuccessful()){
                    startActivity(new Intent(this,HomeScreen.class));
                    finish();
                } else{
                    Toast.makeText(this,"Login Failed",Toast.LENGTH_SHORT).show();
                }
            });
        });

        binding.signupText.setOnClickListener(v ->{
            startActivity(new Intent(this, SignUp.class));
        });
    }
}