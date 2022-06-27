package com.example.buddies.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.buddies.BuildConfig;
import com.example.buddies.R;

public class SettingsFragment extends DialogFragment {

    public static final String SETTINGS_FRAGMENT_TAG = "settings_fragment";

    private Context m_Context = null;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch m_Switch = null;

    public static final String SP_NOTIFICATIONS_KEY = "notifications";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        m_Context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences sp = m_Context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);

        m_Switch = view.findViewById(R.id.settings_switch);
        m_Switch.setChecked(sp.getBoolean(SP_NOTIFICATIONS_KEY, true));

        m_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sp.edit().putBoolean(SP_NOTIFICATIONS_KEY, isChecked).apply();
            }
        });
    }
}
