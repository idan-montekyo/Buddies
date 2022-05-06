package com.example.buddies.fragments;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.buddies.R;
import com.example.buddies.ViewModel.ViewModel;
import com.example.buddies.common.AppUtils;
import com.example.buddies.interfaces.LocationSelectionEvent.ILocationSelect_EventHandler;
import com.example.buddies.interfaces.MVVM.IView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class CreatePostFragment extends Fragment implements IView,
                                                            ILocationSelect_EventHandler,
                                                            OnMapReadyCallback {

    public final String HOME_FRAGMENT_TAG = "home_fragment";
    public final String CREATE_POST_FRAGMENT_TAG = "create_post_fragment";

    String[] m_LocationDetails;

    LatLng m_SelectedLocation;
    Handler m_MainActivityHandlerFromRemoteThreads = new Handler(Looper.getMainLooper());

    MapView m_mapView;

    TextView m_cityTv;
    TextView m_streetTv;

    GoogleMap m_GoogleMap;
    Marker m_CurrMarker;

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
        ViewModel.getInstance().registerForEvents((IView) this);
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

        m_mapView = (MapView) view.findViewById(R.id.create_post_map_view);
        m_mapView.onCreate(savedInstanceState);
        m_mapView.onResume();

        Button pickLocationBtn = view.findViewById(R.id.create_post_pick_location_button);
        pickLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If a location has not been already selected
                if (m_CurrMarker == null) {
                    m_mapView.setVisibility(View.GONE);
                }

                // FragmentsCenter.m_Fragment_AddNewSong = new Fragment_AddNewSong();
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();

                // Start the transaction process
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setCustomAnimations(
                        R.anim.slide_in,  // enter
                        R.anim.fade_out,  // exit
                        R.anim.fade_in,   // popEnter
                        R.anim.slide_out); // popExit

                // Move to this fragment in the view
                transaction.hide(getParentFragmentManager().findFragmentByTag(CREATE_POST_FRAGMENT_TAG));
                transaction.add(R.id.root_main_activity, new SelectLocationFragment(), SelectLocationFragment.SELECT_LOCATION_FRAGMENT_TAG);

                // Insert the current fragment to the default BackStack in order to remove the fragment
                // from the view when the back button is pressed from inside the fragment.
                transaction.addToBackStack(null);

                transaction.commit();


            }
        });

        m_cityTv = view.findViewById(R.id.create_post_city_output);
        m_streetTv = view.findViewById(R.id.create_post_street_output);
        EditText timeEt = view.findViewById(R.id.create_post_time_input);
        EditText contentEt = view.findViewById(R.id.create_post_content_input);

        Button uploadBtn = view.findViewById(R.id.create_post_upload_button);
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String cityInput = m_cityTv.getText().toString();
                String streetInput = m_streetTv.getText().toString();
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

    @Override
    public void onLocationSelected(LatLng i_SelectedLocation) {

        m_SelectedLocation = i_SelectedLocation;

        new Thread(new Runnable() {

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {

                m_LocationDetails = AppUtils.getStringValueFromJsonObject(requireContext(), i_SelectedLocation);

                // Update the Activity's TextView
                m_MainActivityHandlerFromRemoteThreads.post(new Runnable() {

                    @Override
                    public void run() {

                        assert m_LocationDetails != null;
                        m_cityTv.setText(m_LocationDetails[0] + " (" + m_LocationDetails[2] + ")");
                        m_streetTv.setText(m_LocationDetails[1]);

                        m_mapView.setVisibility(View.VISIBLE);
                        m_mapView.getMapAsync(CreatePostFragment.this);
                    }
                });
            }
        }).start();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        m_GoogleMap = googleMap;

        // If we want to disable moving the map, uncomment this. (Source: https://stackoverflow.com/a/28452115/2196301)
        // m_Map.getUiSettings().setAllGesturesEnabled(false);

        m_GoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if (m_CurrMarker == null)
        {
            m_CurrMarker = m_GoogleMap.addMarker(new MarkerOptions().position(m_SelectedLocation)
                    .title(m_LocationDetails[0]).snippet(m_SelectedLocation.toString()));
        }
        else
        {
            m_CurrMarker.setPosition(m_SelectedLocation);
        }

        // Center the map according to the chosen coordinates (Source: https://stackoverflow.com/a/16342378/2196301)
        m_GoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(m_SelectedLocation, 14f));
    }
}