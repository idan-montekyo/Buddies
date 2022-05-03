package com.example.buddies.interfaces.LocationSelectionEvent;

import com.google.android.gms.maps.model.LatLng;

public interface ILocationSelect_EventHandler
{
    void onLocationSelected(LatLng i_SelectedLocation);
}
