package com.example.callapp;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CallRepository {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration incomingCallListener;

    // CREATE CALL
    public String createCall(String callerId, String receiverId) {

        String callId = db.collection("calls").document().getId();
        String channel = "call_" + UUID.randomUUID();

        Map<String, Object> call = new HashMap<>();
        call.put("callerId", callerId);
        call.put("receiverId", receiverId);
        call.put("status", "calling");
        call.put("channel", channel);
        call.put("timestamp", System.currentTimeMillis());

        db.collection("calls").document(callId).set(call);

        return callId + "|" + channel;
    }

    // UPDATE STATUS
    public void updateStatus(String callId, String status) {
        db.collection("calls").document(callId).update("status", status);
    }

    // LISTEN INCOMING CALL
    public void listenIncoming(String uid, MutableLiveData<DocumentSnapshot> liveData) {
        if (incomingCallListener != null) {
            incomingCallListener.remove();
        }

        incomingCallListener = db.collection("calls")
                .whereEqualTo("receiverId", uid)
                .whereEqualTo("status", "calling")
                .addSnapshotListener((value, error) -> {
                    if (value != null && !value.isEmpty()) {
                        liveData.postValue(value.getDocuments().get(0));
                    }
                });
    }

    public void stopListening() {
        if (incomingCallListener != null) {
            incomingCallListener.remove();
            incomingCallListener = null;
        }
    }
}