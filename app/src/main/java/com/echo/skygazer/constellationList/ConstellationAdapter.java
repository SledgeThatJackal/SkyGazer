package com.echo.skygazer.constellationList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.echo.skygazer.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ConstellationAdapter extends RecyclerView.Adapter<ConstellationViewHolder> {

    Context context;
    String[] constellations;

    public ConstellationAdapter(Context context, String[] constellations) {
        this.context = context;
        this.constellations = constellations;
    }

    @NonNull
    @Override
    public ConstellationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.constellation_item_view, parent, false);

        return new ConstellationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConstellationViewHolder holder, int position) {
        holder.constellationName.setText(constellations[position]);
    }

    @Override
    public int getItemCount() {
        return constellations.length;
    }
}
