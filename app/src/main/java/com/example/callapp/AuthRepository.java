package com.example.callapp;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuthRepository {

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    public AuthRepository() {
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    public void login(String email, String password, OnCompleteListener<AuthResult> listener) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(listener);
    }

    public void register(String name, String email, String password, OnCompleteListener<AuthResult> listener) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (auth.getCurrentUser() != null) {
                            String uid = auth.getCurrentUser().getUid();

                            Map<String, Object> user = new HashMap<>();
                            user.put("uid", uid);
                            user.put("name", name);
                            user.put("email", email.toLowerCase().trim());

                            firestore.collection("users").document(uid).set(user)
                                    .addOnSuccessListener(s -> listener.onComplete(task))
                                    .addOnFailureListener(f -> listener.onComplete(task));
                        }
                    } else {
                        listener.onComplete(task);
                    }
                });
    }

    public LiveData<List<User>> getUser() {
        MutableLiveData<List<User>> liveData = new MutableLiveData<>();
        
        firestore.collection("users").addSnapshotListener((value, error) -> {
            if (error != null) return;

            FirebaseUser currentUser = auth.getCurrentUser();
            String currentUid = (currentUser != null) ? currentUser.getUid() : "";

            List<User> list = new ArrayList<>();
            if (value != null) {
                for (DocumentSnapshot doc : value.getDocuments()) {
                    User user = doc.toObject(User.class);
                    
                    if (user != null && user.getUid() != null) {
                        if (!user.getUid().equals(currentUid)) {
                            list.add(user);
                        }
                    }
                }
            }
            liveData.setValue(list);
        });

        return liveData;
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }
}
