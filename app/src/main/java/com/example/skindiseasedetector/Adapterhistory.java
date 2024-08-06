package com.example.skindiseasedetector;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Adapterhistory extends RecyclerView.Adapter<Adapterhistory.ViewHolder> {
     static Context context;
    static ArrayList<History> list;

    public Adapterhistory(Context context, ArrayList<History> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.items,parent,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        History history = list.get(position);
        holder.diagnosis.setText(history.getDiagnosis());
        holder.date.setText(history.getDate());


        Glide.with(holder.image.getContext())
               .load(history.getImageUrl())
                .placeholder(R.drawable.baseline_image)
                .error(R.drawable.baseline_broken_image)
               .into(holder.image);


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView diagnosis,cause,symptoms,treatment,date,url;
        ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            diagnosis = itemView.findViewById(R.id.diagnosisTV);
            date = itemView.findViewById(R.id.dateTV);
            image = itemView.findViewById(R.id.imageTV);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            Intent intent = new Intent(context, InfoActivity.class);
            intent.putExtra("diagnosis", list.get(position).getDiagnosis());
            intent.putExtra("cause",list.get(position).getCause());
            intent.putExtra("symptoms",list.get(position).getSymptoms());
            intent.putExtra("treatment",list.get(position).getTreatment());
            intent.putExtra("date",list.get(position).getDate());
            intent.putExtra("image",list.get(position).getImageUrl());
            intent.putExtra("timestamp",list.get(position).getTimestamp());
            context.startActivity(intent);

        }
    }
}
