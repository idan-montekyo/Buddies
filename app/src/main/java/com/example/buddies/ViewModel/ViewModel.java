package com.example.buddies.ViewModel;

import com.example.buddies.interfaces.ILocationSelect_EventHandler;
import com.example.buddies.interfaces.IView;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class ViewModel implements ILocationSelect_EventHandler
{
    private static ViewModel _instance = null;
    private LatLng location;

    List<IView> views = new ArrayList<IView>();

    private ViewModel() { }

    public static ViewModel getInstance()
    {
        if (_instance == null) {
            _instance = new ViewModel();
        }

        return _instance;
    }

    public LatLng getLocation() { return this.location; }

    public void registerForEvents(IView i_NewVIew) { this.views.add(i_NewVIew); }

    public void setLocation(LatLng i_NewLocation) { this.location = i_NewLocation; }

    @Override
    public void onLocationSelected(LatLng i_SelectedLocation)
    {
        for (IView view : views)
        {
            if (view instanceof ILocationSelect_EventHandler)
            {
                ((ILocationSelect_EventHandler)view).onLocationSelected(i_SelectedLocation);
            }
        }
    }
}