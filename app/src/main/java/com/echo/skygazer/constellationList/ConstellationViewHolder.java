package com.echo.skygazer.constellationList;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.echo.skygazer.R;

public class ConstellationViewHolder extends RecyclerView.ViewHolder {

    TextView constellationName;

    public ConstellationViewHolder(@NonNull View itemView) {
        super(itemView);
        constellationName.findViewById(R.id.constellation_name);
    }
}
