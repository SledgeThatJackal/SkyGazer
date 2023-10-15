package com.echo.skygazer.ui.sky;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SkyViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public SkyViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is the sky view fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}