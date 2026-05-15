package com.example.callapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.google.firebase.auth.FirebaseAuth;

import java.util.List;


public class CallAdapter extends RecyclerView.Adapter<CallAdapter.ViewHolder> {

    List<User> list;
    onUserClick listener;

    public CallAdapter(List<User> user, onUserClick listener){
        this.list = user;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.name.setText(list.get(position).getName());
        holder.email.setText(list.get(position).getEmail());
        holder.itemView.setOnClickListener(v -> {

            User user = list.get(position);
            String currentUid = FirebaseAuth.getInstance().getUid();

            if(user.getUid().equals(currentUid)){
                Toast.makeText(v.getContext(), "You can't call yourself", Toast.LENGTH_SHORT).show();
                return;
            }
            listener.onCallClick(user);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView name,email;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            email = itemView.findViewById(R.id.email);
        }

    }
}
