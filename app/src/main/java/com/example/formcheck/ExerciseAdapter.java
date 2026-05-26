package com.example.formcheck;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ViewHolder> {

    private final List<Exercise> list;
    private final Context context;

    public ExerciseAdapter(Context context, List<Exercise> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_exercise, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Exercise ex = list.get(position);
        holder.tvName.setText(ex.name);
        holder.tvDesc.setText(ex.description);
        holder.tvGood.setText(ex.whatIsGood);
        holder.tvBad.setText(ex.whatIsBad);
        holder.tvAngles.setText(String.format(
                "Целевой угол: до %d° (хорошо)  /  до %d° (допустимо)",
                ex.goodAngleMax, ex.lowAngleMax));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra("exercise", ex.name);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDesc, tvGood, tvBad, tvAngles;

        ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvName);
            tvDesc = v.findViewById(R.id.tvDesc);
            tvGood = v.findViewById(R.id.tvGood);
            tvBad = v.findViewById(R.id.tvBad);
            tvAngles = v.findViewById(R.id.tvAngles);
        }
    }
}
