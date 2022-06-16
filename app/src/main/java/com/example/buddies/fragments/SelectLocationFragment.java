package com.example.buddies.fragments;

import static android.content.Context.LOCATION_SERVICE;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.example.buddies.R;
import com.example.buddies.ViewModel.ViewModel;
import com.example.buddies.common.AppUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

/*
This Fragment is used to allow the user to select a location from the map
*/

public class SelectLocationFragment extends Fragment implements OnMapReadyCallback,
                                                                GoogleMap.OnMapClickListener
{
    GoogleMap m_Map;
    MapView m_MapHolder;
    Marker currMarker;
    LatLng currentLatLng = null;
    private ActivityResultLauncher<String> requestLocationPermissionLauncher;
    Context m_Context = null;
    LatLng startingCoordinates = null;
    AlertDialog alertDialog;

    LocationListener m_LocationListener = null;

    int LOCATION_REFRESH_TIME = 15000; // 15 seconds to update
    int LOCATION_REFRESH_DISTANCE = 500; // 500 meters to update

    public static final String SELECT_LOCATION_FRAGMENT_TAG = "select_location_fragment";

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onAttach(@NonNull Context context)
    {
        this.m_Context = context;
        this.initializeActivityResultLaunchers();
        super.onAttach(this.m_Context);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initializeActivityResultLaunchers()
    {
        // Request location permission with contracts (Source: https://stackoverflow.com/a/63546099/2196301)
        this.requestLocationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted ->
        {
            if (isGranted)
            {
                Intent intent=new Intent("android.location.GPS_ENABLED_CHANGE");
                intent.putExtra("enabled", true);
                SelectLocationFragment.this.m_Context.sendBroadcast(intent);

                // Do this dummy permission check because the code doesn't recognize that we are inside a code block that means the location
                // permission is actually granted (Source: https://www.gool.co.il/MyCourses/Chapter/86423#83994)
                int hasLocationPermission = this.m_Context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);

                if (hasLocationPermission == PackageManager.PERMISSION_GRANTED)
                {
                    LocationManager manager = (LocationManager) SelectLocationFragment.this.m_Context.getSystemService(LOCATION_SERVICE);

                    SelectLocationFragment.this.m_LocationListener = new LocationListener()
                    {
                        @Override
                        public void onLocationChanged(@NonNull Location location)
                        {
                            SelectLocationFragment.this.startingCoordinates = new LatLng(location.getLatitude(), location.getLongitude());

                            // After the first location has been granted, stop getting more location updates - we don't need them.
                            manager.removeUpdates(SelectLocationFragment.this.m_LocationListener);

                            // Start load the map
                            m_MapHolder.getMapAsync(SelectLocationFragment.this);
                        }
                    };

                    // Check if the location service is enabled (Source: https://stackoverflow.com/a/54648795/2196301)
                    if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER) == false)
                    {
                        // Create a dialog for ordering the user to turn on the gps
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.m_Context);
                        alertDialogBuilder.setTitle("Location Access Alert");
                        alertDialogBuilder.setMessage("You will be now redirected to the settings page in order to turn on gps location.\nAfter the location access confirmation, please wait a few seconds in order the app will recognize your location.");
                        alertDialogBuilder.setCancelable(false);
                        alertDialogBuilder.setPositiveButton("Take me there !", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                // Open the GPS settings (Source: https://stackoverflow.com/a/23040461/2196301)
                                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            }
                        });

                        alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }

                    manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, SelectLocationFragment.this.m_LocationListener);
                }
            }
            else
            {
                SelectLocationFragment.this.startingCoordinates = new LatLng(31.881417, 34.709998);

                // Start load the map
                m_MapHolder.getMapAsync(this);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        final boolean isTheInflatedLayoutShouldBeTheRootLayout = false;
        View rootView = inflater.inflate(R.layout.fragment_select_location, container, isTheInflatedLayoutShouldBeTheRootLayout);

        AppUtils.printDebugToLogcat("SelectLocationFragment", "onCreateView", "Requesting permission ...");
        this.requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);

        m_MapHolder = (MapView) rootView.findViewById(R.id.select_location_map_view);
        m_MapHolder.onCreate(savedInstanceState);
        m_MapHolder.onResume();

        AppUtils.printDebugToLogcat("SelectLocationFragment", "onCreateView", "Loading the map");
        // Start load the map
        // m_MapHolder.getMapAsync(this);

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
                            AppUtils.closeFragmentsByFragmentsTags(getActivity().getSupportFragmentManager(), SELECT_LOCATION_FRAGMENT_TAG);
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroy()
    {
        // FragmentsCenter.isSelectLocationFragmentAlive = false;
        super.onDestroy();
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        m_Map = googleMap;
        m_Map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // currMarker = m_Map.addMarker(new MarkerOptions().position(this.startingCoordinates).title("Title").snippet("Description"));
        m_Map.setOnMapClickListener(this);

        // Center the map according to the chosen coordinates (Source: https://stackoverflow.com/a/16342378/2196301)
        m_Map.moveCamera(CameraUpdateFactory.newLatLngZoom(this.startingCoordinates, 14f));
    }

    private void setMarkerPosition(LatLng latLng)
    {
        currentLatLng = latLng;

        if (currMarker == null)
        {
            currMarker = m_Map.addMarker(new MarkerOptions().position(this.currentLatLng).title("The place I chose").snippet(""));
        }
        else
        {
            currMarker.setPosition(latLng);
        }
    }

    @Override
    public void onMapClick(LatLng latLng)
    {
        this.setMarkerPosition(latLng);
    }
}