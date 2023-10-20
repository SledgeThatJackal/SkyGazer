package com.echo.skygazer.ui.sky;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.echo.skygazer.R;
import com.echo.skygazer.databinding.FragmentSkyBinding;
import com.echo.skygazer.gfx.SkyDrawing;

public class SkyFragment extends Fragment {

    private FragmentSkyBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sky, container, false);
        LinearLayout rootLayout = rootView.findViewById(R.id.sky_view);

        SkyDrawing skyDrawing = new SkyDrawing(getActivity());
        rootLayout.addView(skyDrawing);
        skyDrawing.startDrawThread();

        return rootView;
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}