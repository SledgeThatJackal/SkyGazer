package com.echo.skygazer.constellationList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.echo.skygazer.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ConstellationAdapter extends RecyclerView.Adapter<ConstellationViewHolder> {

    private final LayoutInflater inflater;
    List<String> constellations;

    public ConstellationAdapter(Context context, List<String> constellations) {
        inflater = LayoutInflater.from(context);
        this.constellations = constellations;
    }

    @NonNull
    @Override
    public ConstellationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.constellation_item_view, parent, false);

        return new ConstellationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConstellationViewHolder holder, int position) {
        Log.d("OnBindViewHolder", constellations.get(position));
        holder.constellationName.setText(constellations.get(position));
    }

    @Override
    public int getItemCount() {
        Log.d("getItemCount", "List Length: " + constellations.size());
        return constellations.size();
    }
}
