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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.example.buddies.R;
import com.example.buddies.ViewModel.ViewModel;
import com.example.buddies.common.AppUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

/*
This Fragment is used to allow the user to select a location from the map
*/
public class SelectLocationFragment extends Fragment implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener {

    GoogleMap m_Map;
    MapView m_MapHolder;
    Marker currMarker;
    LatLng currentLatLng = null;
    private ActivityResultLauncher<String> requestLocationPermissionLauncher;
    Context m_Context = null;
    LatLng startingCoordinates = new LatLng(31.881417, 34.709998);
    AlertDialog alertDialog;

    LocationListener m_LocationListener = null;

    public static final String SELECT_LOCATION_FRAGMENT_TAG = "select_location_fragment";
    private FusedLocationProviderClient fusedLocationClient;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onAttach(@NonNull Context context) {
        this.m_Context = context;
        this.initializeActivityResultLaunchers();
        super.onAttach(this.m_Context);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initializeActivityResultLaunchers() {
        // Request location permission with contracts (Source: https://stackoverflow.com/a/63546099/2196301)
        this.requestLocationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted ->
        {
            // Start load the map
            m_MapHolder.getMapAsync(SelectLocationFragment.this);

            // If there is permission to access the device's location
            if (isGranted) {
                // Do this dummy permission check because the code doesn't recognize that we are inside a code block that means the location
                // permission is actually granted (Source: https://www.gool.co.il/MyCourses/Chapter/86423#83994)
                int hasLocationPermission = this.m_Context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);

                if (hasLocationPermission == PackageManager.PERMISSION_GRANTED) {
                    LocationManager manager = (LocationManager) SelectLocationFragment.this.m_Context.getSystemService(LOCATION_SERVICE);

                    SelectLocationFragment.this.m_LocationListener = new LocationListener() {
                        @Override
                        public void onLocationChanged(@NonNull Location location) {
                            // After the first location has been granted, stop getting more location updates - we don't need them.
                            manager.removeUpdates(SelectLocationFragment.this.m_LocationListener);

                            SelectLocationFragment.this.startingCoordinates = new LatLng(location.getLatitude(), location.getLongitude());

                            AppUtils.printDebugToLogcat("SelectLocationFragment", "onLocationChanged()", "Current location is: " + location);

                            SelectLocationFragment.this.moveMapFocusToSpecificLocation(SelectLocationFragment.this.startingCoordinates);
                        }
                    };

                    // Check if the location service is off (Source: https://stackoverflow.com/a/54648795/2196301)
                    if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER) == false) {
                        SelectLocationFragment.this.askUserToTurnOnLocationService();
                    }

                    // Get the last known location
                    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.m_Context);
                    Task<Location> lastKnownLocationTask = fusedLocationClient.getLastLocation();
                    lastKnownLocationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            AppUtils.printDebugToLogcat("SelectLocationFragment", "onSuccess()", "Last known location is: " + location);

                            // If there is an information about the last location
                            if (location != null) {
                                SelectLocationFragment.this.startingCoordinates = new LatLng(location.getLatitude(), location.getLongitude());

                                SelectLocationFragment.this.moveMapFocusToSpecificLocation(SelectLocationFragment.this.startingCoordinates);
                            }
                        }
                    });

                    // Request the current user's location
                    // Note: Use both GPS_PROVIDER and NETWORK_PROVIDER in order to allow location gathering from various providers (Source: https://stackoverflow.com/a/55300972/2196301)
                    manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, SelectLocationFragment.this.m_LocationListener);
                    manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, SelectLocationFragment.this.m_LocationListener);
                }
            }
        });
    }

    private void askUserToTurnOnLocationService() {
        // Create a dialog for ordering the user to turn on the gps
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.m_Context);
        alertDialogBuilder.setTitle(AppUtils.GetResourceStringValueByStringName("alert_dialog_builder_title", m_Context));
        alertDialogBuilder.setMessage(AppUtils.GetResourceStringValueByStringName("alert_dialog_builder_message", m_Context));
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton(
                AppUtils.GetResourceStringValueByStringName("alert_dialog_builder_positive_button", m_Context)
                        + " !", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Open the GPS settings (Source: https://stackoverflow.com/a/23040461/2196301)
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });

        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final boolean isTheInflatedLayoutShouldBeTheRootLayout = false;
        View rootView = inflater.inflate(R.layout.fragment_select_location, container, isTheInflatedLayoutShouldBeTheRootLayout);

        AppUtils.printDebugToLogcat("SelectLocationFragment", "onCreateView", "Requesting permission ...");
        this.requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);

        m_MapHolder = (MapView) rootView.findViewById(R.id.select_location_map_view);
        m_MapHolder.onCreate(savedInstanceState);
        m_MapHolder.onResume();

        AppUtils.printDebugToLogcat("SelectLocationFragment", "onCreateView", "Loading the map");

        Button sendLocation = (Button) rootView.findViewById(R.id.select_location_pick_button);
        sendLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLatLng != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Close location selector?");
                    builder.setMessage("Are you sure you want to close the location selector?");

                    // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        AppUtils.printDebugToLogcat("SelectLocationFragment", "onMapReady()", "got here");
        m_Map = googleMap;
        m_Map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        m_Map.setOnMapClickListener(this);

        this.moveMapFocusToSpecificLocation(this.startingCoordinates);
    }

    private void setMarkerPosition(LatLng latLng) {
        currentLatLng = latLng;

        if (currMarker == null) {
            currMarker = m_Map.addMarker(new MarkerOptions().position(this.currentLatLng).title("The place I chose").snippet(""));
        } else {
            currMarker.setPosition(latLng);
        }
    }

    private void moveMapFocusToSpecificLocation(LatLng i_Location) {
        if (m_Map != null) {
            // Center the map according to the chosen coordinates (Source: https://stackoverflow.com/a/16342378/2196301)
            m_Map.moveCamera(CameraUpdateFactory.newLatLngZoom(i_Location, 14f));
        }
    }

    @Override
    public void onMapClick(LatLng latLng) { this.setMarkerPosition(latLng); }
}