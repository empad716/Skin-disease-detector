package com.example.skindiseasedetector;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;


public class HistoryFragment extends Fragment {
    RecyclerView recyclerView;
    DatabaseReference databaseReference;
    Adapterhistory adapterhistory;
    ArrayList<History> list;
    FirebaseAuth auth;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        auth = FirebaseAuth.getInstance();

        recyclerView = view.findViewById(R.id.historyList);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(auth.getCurrentUser().getUid()).child("history");
        Query sortedQuery = databaseReference.orderByChild("timestamp");
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        list= new ArrayList<>();
        adapterhistory = new Adapterhistory(getActivity(),list);
        recyclerView.setAdapter(adapterhistory);
        sortedQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    History history = dataSnapshot.getValue(History.class);
                    list.add(history);

                }
                adapterhistory.notifyDataSetChanged();
                Collections.reverse(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Failed to Retrieve Data: "+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}