package com.example.callapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.example.callapp.databinding.ActivityHomeScreenBinding;
import com.google.firebase.auth.FirebaseAuth;

public class HomeScreen extends AppCompatActivity {

    ActivityHomeScreenBinding binding;

    AuthViewModel authVM;
    CallViewModel callVM;
    private boolean isIncomingCallVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_home_screen);

        authVM = new ViewModelProvider(this).get(AuthViewModel.class);
        callVM = new ViewModelProvider(this).get(CallViewModel.class);

        binding.setLifecycleOwner(this);


        // LOGOUT BUTTON
        binding.logoutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            goToLogin();
        });

        // USER LIST
        authVM.getUsers().observe(this, users -> {
            CallAdapter adapter = new CallAdapter(users, user -> {
                startCall(user);
            });
            binding.recyclerView.setAdapter(adapter);
        });

        // LISTEN INCOMING CALL
        String currentUid = FirebaseAuth.getInstance().getUid();
        if (currentUid != null) {
            callVM.listenIncoming(currentUid);
        }

        callVM.incomingCall.observe(this, doc -> {
            if (doc != null && !isIncomingCallVisible) {
                String callId = doc.getId();
                String callerId = doc.getString("callerId");
                String channel = doc.getString("channel");

                isIncomingCallVisible = true;
                Intent i = new Intent(this, IncomingCall.class);
                i.putExtra("callId", callId);
                i.putExtra("callerId", callerId);
                i.putExtra("channel", channel);

                startActivity(i);
            }
        });
    }

    private void goToLogin() {
        Intent intent = new Intent(this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    private void startCall(User user) {
        String callerId = FirebaseAuth.getInstance().getUid();
        String receiverId = user.getUid();

        if (callerId == null) return;

        String result = callVM.createCall(callerId, receiverId);

        String[] data = result.split("\\|");
        String callId = data[0];
        String channel = data[1];

        Intent i = new Intent(this, Calling.class);
        i.putExtra("callId", callId);
        i.putExtra("channel", channel);

        startActivity(i);
    }
}