package com.example.buddies.ViewModel;

import android.content.Context;

import com.example.buddies.Model.Model;
import com.example.buddies.common.AppUtils;
import com.example.buddies.enums.eDogGender;
import com.example.buddies.interfaces.LocationSelectionEvent.ILocationSelect_EventHandler;
import com.example.buddies.interfaces.LoginEvent.ILoginRequestEventHandler;
import com.example.buddies.interfaces.LoginEvent.ILoginResponsesEventHandler;
import com.example.buddies.interfaces.LogoutEvent.ILogoutRequestEventHandler;
import com.example.buddies.interfaces.LogoutEvent.ILogoutResponsesEventHandler;
import com.example.buddies.interfaces.SignupEvent.ISignupRequestEventHandler;
import com.example.buddies.interfaces.SignupEvent.ISignupResponsesEventHandler;
import com.example.buddies.interfaces.MVVM.IView;
import com.example.buddies.interfaces.MVVM.IViewModel;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class ViewModel implements IViewModel,
                                  ILocationSelect_EventHandler,
                                  ILoginRequestEventHandler,
                                  ILoginResponsesEventHandler,
                                  ILogoutRequestEventHandler,
                                  ILogoutResponsesEventHandler,
                                  ISignupRequestEventHandler,
                                  ISignupResponsesEventHandler
{
    private static ViewModel _instance = null;
    // private LatLng location;
    private Model m_Model = null;

    List<IView> views = new ArrayList<IView>();

    private ViewModel()
    {
        this.m_Model = Model.getInstance();
        this.m_Model.registerForEvents(((IViewModel)this));
    }

    public static ViewModel getInstance()
    {
        if (_instance == null)
        {
            _instance = new ViewModel();
        }

        return _instance;
    }

    // public LatLng getLocation() { return this.location; }

    @Override
    public void registerForEvents(IView i_NewVIew) { this.views.add(i_NewVIew); }

    @Override
    public void unregisterForEvents(IView i_ViewToUnregister)
    {
        this.views.remove(i_ViewToUnregister);
    }

    // public void setLocation(LatLng i_NewLocation) { this.location = i_NewLocation; }

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

    @Override
    public void onRequestToLogin(String i_UserName, String i_Password)
    {
        Model.getInstance().onRequestToLogin(i_UserName, i_Password);
    }

    @Override
    public void onSuccessToLogin()
    {
        for (IView view : views)
        {
            if (view instanceof ILoginResponsesEventHandler)
            {
                ((ILoginResponsesEventHandler)view).onSuccessToLogin();
            }
        }
    }

    @Override
    public void onFailureToLogin(Exception i_Reason)
    {
        for (IView view : views)
        {
            if (view instanceof ILoginResponsesEventHandler)
            {
                ((ILoginResponsesEventHandler)view).onFailureToLogin(i_Reason);
            }
        }
    }

    @Override
    public void onRequestToAnonymousLogin()
    {
        Model.getInstance().onRequestToAnonymousLogin();
    }

    @Override
    public void onSuccessToAnonymousLogin()
    {
        for (IView view : views)
        {
            if (view instanceof ILoginResponsesEventHandler)
            {
                ((ILoginResponsesEventHandler)view).onSuccessToAnonymousLogin();
            }
        }
    }

    @Override
    public void onFailureToAnonymousLogin(Exception i_Reason)
    {
        for (IView view : views)
        {
            if (view instanceof ILoginResponsesEventHandler)
            {
                ((ILoginResponsesEventHandler)view).onFailureToAnonymousLogin(i_Reason);
            }
        }
    }

    @Override
    public void onRequestToLogout()
    {

    }

    @Override
    public void onSuccessToLogout()
    {

    }

    @Override
    public void onFailureToLogout(Exception i_Reason)
    {

    }

    @Override
    public void onSuccessToAnonymousLogout()
    {

    }

    @Override
    public void onFailureToAnonymousLogout(Exception i_Reason)
    {

    }

    @Override
    public void onRequestToSignup(Context i_Context, String i_UserName, String i_Password, String i_FullName, String i_Age, eDogGender i_DogGender)
    {
        AppUtils.printDebugToLogcat("ViewModel", "onRequestToSignup", "calling onRequestToSignup()");
        Model.getInstance().onRequestToSignup(i_Context, i_UserName, i_Password, i_FullName, i_Age, i_DogGender);
        AppUtils.printDebugToLogcat("ViewModel", "onRequestToSignup", "returned from onRequestToSignup()");
    }

    @Override
    public void onSuccessToSignup()
    {
        for (IView view : views)
        {
            if (view instanceof ISignupResponsesEventHandler)
            {
                ((ISignupResponsesEventHandler)view).onSuccessToSignup();
            }
        }
    }

    @Override
    public void onFailureToSignup(Exception i_Reason)
    {
        for (IView view : views)
        {
            if (view instanceof ISignupResponsesEventHandler)
            {
                ((ISignupResponsesEventHandler)view).onFailureToSignup(i_Reason);
            }
        }
    }
}