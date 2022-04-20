package com.example.buddies.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.buddies.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;

public class CreatePostFragment extends Fragment {

    public final String HOME_FRAGMENT_TAG = "home_fragment";

    MapView m_mapView;

    interface IOnUploadListener {
        void onUpload();
    }

    public IOnUploadListener onUploadListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        Fragment homeFragment = getParentFragmentManager().findFragmentByTag(HOME_FRAGMENT_TAG);
        try {
            onUploadListener = (IOnUploadListener) homeFragment;
        } catch (ClassCastException ex) {
            throw new ClassCastException("Fragment must implement IOnUploadListener interface.");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_create_post, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton backBtn = view.findViewById(R.id.create_post_back_button);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().popBackStack();
            }
        });

        m_mapView = view.findViewById(R.id.create_post_map_view);
        m_mapView.onCreate(savedInstanceState);
        m_mapView.onResume();

        Button pickLocationBtn = view.findViewById(R.id.create_post_pick_location_button);
        pickLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: merge location picker.
                m_mapView.setVisibility(View.GONE);

            }
        });

        TextView cityEt = view.findViewById(R.id.create_post_city_output);
        TextView streetEt = view.findViewById(R.id.create_post_street_output);
        EditText timeEt = view.findViewById(R.id.create_post_time_input);
        EditText contentEt = view.findViewById(R.id.create_post_content_input);

        Button uploadBtn = view.findViewById(R.id.create_post_upload_button);
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String cityInput = cityEt.getText().toString();
                String streetInput = streetEt.getText().toString();
                String timeInput = timeEt.getText().toString();
                String contentInput = contentEt.getText().toString();

                if(cityInput.equals("") || streetInput.equals("") ||
                        timeInput.equals("") || contentInput.equals("")) {
                    Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else {

                    // TODO: save post to DataBase and notify to refresh adapter.

                    getParentFragmentManager().popBackStack();
                    onUploadListener.onUpload();
                }
            }
        });
    }
}
