package com.example.callapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.example.callapp.databinding.ActivitySignUpBinding;

public class SignUp extends AppCompatActivity {

    ActivitySignUpBinding binding;
    AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up);
        binding.setLifecycleOwner(this);
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        binding.signupBtn.setOnClickListener(v -> {
            String name = binding.name.getText().toString();
            String email = binding.email.getText().toString();
            String password = binding.password.getText().toString();

            if(name.isEmpty()){
                binding.name.setError("Name is required");
                binding.name.requestFocus();
                return;
            }

            if(email.isEmpty()){
                binding.email.setError("Email is required");
                binding.email.requestFocus();
                return;

            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.email.setError("Please provide a valid email");
                binding.email.requestFocus();
                return;
            }

            if (password.isEmpty()){
                binding.password.setError("Password is required");
                binding.password.requestFocus();
                return;
            }

            viewModel.register(name,email,password,task ->{
                if(task.isSuccessful()){
                    Toast.makeText(this, "SignUp Success!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, HomeScreen.class));
                    finish();
                } else{
                    Toast.makeText(this, "SignUp Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        });
    }
}