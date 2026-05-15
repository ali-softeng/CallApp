package com.example.callapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;

import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.video.VideoCanvas;

public class Calling extends AppCompatActivity {

    CallViewModel callVM;
    String callId, channel;
    RtcEngine rtcEngine;
    final String APP_ID = "686d18f806d142c48f494ebc352e57f9";

    private boolean isMuted = false;
    private ToneGenerator toneGenerator;

    private static final int PERMISSION_REQ_ID = 22;
    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
    };

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onUserJoined(int uid, int elapsed) {
            runOnUiThread(() -> {
                stopDialingSound();
                setupRemoteVideo(uid);
            });
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            runOnUiThread(() -> {
                Toast.makeText(Calling.this, "User went offline", Toast.LENGTH_SHORT).show();
                endCall();
            });
        }

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            runOnUiThread(() -> Toast.makeText(Calling.this, "Connected", Toast.LENGTH_SHORT).show());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);

        callVM = new ViewModelProvider(this).get(CallViewModel.class);
        callId = getIntent().getStringExtra("callId");
        channel = getIntent().getStringExtra("channel");

        // Start Dialing Sound
        startDialingSound();

        if (checkSelfPermission()) {
            initCalling();
        } else {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID);
        }

        findViewById(R.id.endCallBtn).setOnClickListener(v -> endCall());

        FloatingActionButton muteBtn = findViewById(R.id.muteBtn);
        muteBtn.setOnClickListener(v -> {
            if (rtcEngine != null) {
                isMuted = !isMuted;
                rtcEngine.muteLocalAudioStream(isMuted);
                muteBtn.setImageResource(isMuted ? android.R.drawable.ic_lock_silent_mode : android.R.drawable.ic_btn_speak_now);
            }
        });

        findViewById(R.id.switchBtn).setOnClickListener(v -> {
            if (rtcEngine != null) {
                rtcEngine.switchCamera();
            }
        });
    }

    private void startDialingSound() {
        try {
            toneGenerator = new ToneGenerator(AudioManager.STREAM_VOICE_CALL, 100);
            toneGenerator.startTone(ToneGenerator.TONE_SUP_RINGTONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopDialingSound() {
        if (toneGenerator != null) {
            toneGenerator.stopTone();
            toneGenerator.release();
            toneGenerator = null;
        }
    }

    private void initCalling() {
        callVM.update(callId, "in_call");
        initAgora();
        callStatus(callId);
    }

    private boolean checkSelfPermission() {
        return ContextCompat.checkSelfPermission(this, REQUESTED_PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, REQUESTED_PERMISSIONS[1]) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQ_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initCalling();
            } else {
                stopDialingSound();
                Toast.makeText(this, "Permissions are required", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void initAgora() {
        try {
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = getBaseContext();
            config.mAppId = APP_ID;
            config.mEventHandler = mRtcEventHandler;
            rtcEngine = RtcEngine.create(config);

            rtcEngine.enableVideo();
            rtcEngine.startPreview();

            setupLocalVideo();

            ChannelMediaOptions options = new ChannelMediaOptions();
            options.autoSubscribeAudio = true;
            options.autoSubscribeVideo = true;
            options.publishCameraTrack = true;
            options.publishMicrophoneTrack = true;
            options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
            options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION;

            rtcEngine.joinChannel(null, channel, 0, options);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupLocalVideo() {
        FrameLayout container = findViewById(R.id.localVideo);
        SurfaceView surfaceView = new SurfaceView(getBaseContext());
        surfaceView.setZOrderMediaOverlay(true);
        container.addView(surfaceView);
        rtcEngine.setupLocalVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0));
    }

    private void setupRemoteVideo(int uid) {
        FrameLayout container = findViewById(R.id.remoteVideo);
        container.removeAllViews();

        SurfaceView surfaceView = new SurfaceView(getBaseContext());
        container.addView(surfaceView);
        rtcEngine.setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid));
    }

    private void callStatus(String callId) {
        FirebaseFirestore.getInstance().collection("calls")
                .document(callId)
                .addSnapshotListener((value, error) -> {
                    if (value != null && value.exists()) {
                        String status = value.getString("status");
                        if ("ended".equals(status) || "rejected".equals(status)) {
                            stopDialingSound();
                            finish();
                        }
                    }
                });
    }

    private void endCall() {
        stopDialingSound();
        if (callId != null) {
            callVM.update(callId, "ended");
        }
        leaveChannel();
        finish();
    }

    private void leaveChannel() {
        if (rtcEngine != null) {
            rtcEngine.leaveChannel();
            RtcEngine.destroy();
            rtcEngine = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopDialingSound();
        leaveChannel();
    }
}