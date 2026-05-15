package com.example.callapp;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.DocumentSnapshot;

public class CallViewModel extends ViewModel {

    CallRepository repo = new CallRepository();

    public MutableLiveData<DocumentSnapshot> incomingCall = new MutableLiveData<>();

    public String createCall(String callerId, String receiverId) {
        return repo.createCall(callerId, receiverId);
    }

    public void update(String callId, String status) {
        repo.updateStatus(callId, status);
    }

    public void listenIncoming(String uid) {
        repo.listenIncoming(uid, incomingCall);
    }
}