package com.example.callapp;

import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import com.example.callapp.databinding.ActivityIncomingCallBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class IncomingCall extends AppCompatActivity {

    ActivityIncomingCallBinding binding;
    CallViewModel callVM;

    String callId;
    String callerId;
    String channel;
    private ListenerRegistration statusListener;
    private Ringtone ringtone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_incoming_call);
        callVM = new ViewModelProvider(this).get(CallViewModel.class);

        callId = getIntent().getStringExtra("callId");
        callerId = getIntent().getStringExtra("callerId");
        channel = getIntent().getStringExtra("channel");

        // Start Ringtone
        playRingtone();

        // ringing status update
        callVM.update(callId, "ringing");
        
        // Listen if caller cancels the call
        listenForCancel();

        binding.acceptBtn.setOnClickListener(v -> {
            stopRingtone();
            callVM.update(callId, "accepted");
            Intent i = new Intent(this, Calling.class);
            i.putExtra("callId", callId);
            i.putExtra("channel", channel);
            startActivity(i);
            finish();
        });

        binding.rejectBtn.setOnClickListener(v -> {
            stopRingtone();
            callVM.update(callId, "rejected");
            finish();
        });
    }

    private void playRingtone() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
            if (ringtone != null) {
                ringtone.play();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopRingtone() {
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
    }

    private void listenForCancel() {
        statusListener = FirebaseFirestore.getInstance().collection("calls")
                .document(callId)
                .addSnapshotListener((value, error) -> {
                    if (value != null && value.exists()) {
                        String status = value.getString("status");
                        if ("ended".equals(status) || "rejected".equals(status)) {
                            stopRingtone();
                            finish();
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRingtone();
        if (statusListener != null) {
            statusListener.remove();
        }
    }
}