package com.example.skindiseasedetector;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Adapterhistory extends RecyclerView.Adapter<Adapterhistory.ViewHolder> {

    Context context;
    ArrayList<History> list;

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
        holder.cause.setText(history.getCause());
        holder.symptoms.setText(history.getSymptoms());
        holder.treatment.setText(history.getTreatment());
        holder.date.setText(history.getDate());

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView diagnosis,cause,symptoms,treatment,date;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            diagnosis = itemView.findViewById(R.id.diagnosisTV);
            cause = itemView.findViewById(R.id.causeTV);
            symptoms = itemView.findViewById(R.id.symptomsTV);
            treatment = itemView.findViewById(R.id.treatmentTV);
            date = itemView.findViewById(R.id.dateTV);
        }
    }
}
