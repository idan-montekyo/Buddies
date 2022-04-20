package com.example.buddies.fragments;

import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.buddies.R;
import com.example.buddies.ViewModel.ViewModel;
import com.example.buddies.common.AppUtils;
import com.example.buddies.interfaces.ILocationSelect_EventHandler;
import com.example.buddies.interfaces.IView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/*
This Fragment is used to allow the user to select a location from the map
*/

public class SelectLocationFragment extends Fragment implements OnMapReadyCallback,
                                                                GoogleMap.OnMapClickListener {

    GoogleMap m_Map;
    MapView m_MapHolder;
    Marker currMarker;
    LatLng currentLatLng = null;

    public static final String SELECT_LOCATION_FRAGMENT_TAG = "Fragment_SelectLocation";

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
//        FragmentsCenter.isSelectLocationFragmentAlive = true;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        boolean isTheInflatedLayoutShouldBeTheRootLayout = false;
        View rootView = inflater.inflate(R.layout.fragment_select_location, container, isTheInflatedLayoutShouldBeTheRootLayout);

        m_MapHolder = (MapView) rootView.findViewById(R.id.select_location_map_view);
        m_MapHolder.onCreate(savedInstanceState);
        m_MapHolder.onResume();

        m_MapHolder.getMapAsync(this);

        Button sendLocation = (Button) rootView.findViewById(R.id.select_location_pick_button);
        sendLocation.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (currentLatLng != null)
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Close location selector?");
                    builder.setMessage("Are you sure you want to close the location selector?");

                    // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which)
                        {
                            ViewModel.getInstance().setLocation(currentLatLng);
                            AppUtils.closeFragmentsByFragmentsTags(getActivity().getSupportFragmentManager(), SELECT_LOCATION_FRAGMENT_TAG);
//                            FragmentsCenter.isSelectLocationFragmentAlive = false;
                            ViewModel.getInstance().onLocationSelected(currentLatLng);
                            getParentFragmentManager().popBackStack();
                        }
                    });

                    // A null listener allows the button to dismiss the dialog and take no further action.
                    builder.setNegativeButton(android.R.string.no, null);
                    builder.setIcon(android.R.drawable.ic_dialog_alert);

                    final AlertDialog show = builder.show();
                }
            }
        });

        return rootView;
    }

    @Override
    public void onDestroy()
    {
//        FragmentsCenter.isSelectLocationFragmentAlive = false;
        super.onDestroy();
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        m_Map = googleMap;
        m_Map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        LatLng coordinates = new LatLng(31.881417, 34.709998);
        currMarker = m_Map.addMarker(new MarkerOptions().position(coordinates).title("Title").snippet("Description"));
        m_Map.setOnMapClickListener(this);

        // Center the map according to the chosen coordinates (Source: https://stackoverflow.com/a/16342378/2196301)
        m_Map.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 14f));
    }

    private void setMarkerPosition(LatLng latLng)
    {
        currentLatLng = latLng;
        currMarker.setPosition(latLng);
        Toast.makeText(getContext(), latLng.toString(), Toast.LENGTH_SHORT).show();

        /*
        if (m_IsMapGesturesDisabled == false)
        {
            currMarker.setPosition(latLng);
            Toast.makeText(getContext(), latLng.toString(), Toast.LENGTH_SHORT).show();
        }
        */
    }

    @Override
    public void onMapClick(LatLng latLng)
    {
        /*
        new GoogleMap.OnMapClickListener()
        {
            @Override
            public void onMapClick(LatLng latLng)
            {
                Fragment_SelectLocation.this.setMarkerPosition(latLng);
            }
        });
        */

        this.setMarkerPosition(latLng);
    }
}