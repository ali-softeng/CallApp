package com.example.callapp;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class AuthViewModel extends ViewModel {
    private AuthRepository authRepository;

    public AuthViewModel(){
        authRepository = new AuthRepository();
    }

    public void login(String email, String password, OnCompleteListener<AuthResult> listener){
        authRepository.login(email, password, listener);
    }

    public void register(String name, String email, String password,OnCompleteListener<AuthResult> listener){
        authRepository.register(name,email,password,listener);
    }

    public LiveData<List<User>> getUsers(){
        return authRepository.getUser();
    }
    public FirebaseUser getUser(){
        return authRepository.getCurrentUser();
    }
}
