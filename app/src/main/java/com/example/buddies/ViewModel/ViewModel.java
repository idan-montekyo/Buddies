package com.example.buddies.ViewModel;

import android.content.Context;

import com.example.buddies.Model.Model;
import com.example.buddies.common.AppUtils;
import com.example.buddies.common.Post;
import com.example.buddies.common.UserProfile;
import com.example.buddies.enums.eDogGender;
import com.example.buddies.interfaces.LoadUserProfileEvent.ILoadUserProfileRequestEventHandler;
import com.example.buddies.interfaces.LoadUserProfileEvent.ILoadUserProfileResponseEventHandler;
import com.example.buddies.interfaces.LocationSelectionEvent.ILocationSelect_EventHandler;
import com.example.buddies.interfaces.LoginEvent.ILoginRequestEventHandler;
import com.example.buddies.interfaces.LoginEvent.ILoginResponsesEventHandler;
import com.example.buddies.interfaces.LogoutEvent.ILogoutRequestEventHandler;
import com.example.buddies.interfaces.LogoutEvent.ILogoutResponsesEventHandler;
import com.example.buddies.interfaces.PostCreationEvent.IPostCreationRequestEventHandler;
import com.example.buddies.interfaces.PostCreationEvent.IPostCreationResponseEventHandler;
import com.example.buddies.interfaces.SignupEvent.ISignupRequestEventHandler;
import com.example.buddies.interfaces.SignupEvent.ISignupResponsesEventHandler;
import com.example.buddies.interfaces.MVVM.IView;
import com.example.buddies.interfaces.MVVM.IViewModel;
import com.example.buddies.interfaces.UpdateCitiesAutocompleteListEvent.IUpdateCitiesAutocompleteListRequestEventHandler;
import com.example.buddies.interfaces.UpdateCitiesAutocompleteListEvent.IUpdateCitiesAutocompleteListResponsesEventHandler;
import com.example.buddies.interfaces.UpdateProfileEvent.IUpdateProfileRequestEventHandler;
import com.example.buddies.interfaces.UpdateProfileEvent.IUpdateProfileResponsesEventHandler;
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
                                  ISignupResponsesEventHandler,
                                  IPostCreationRequestEventHandler,
                                  IPostCreationResponseEventHandler,
                                  IUpdateCitiesAutocompleteListRequestEventHandler,
                                  IUpdateCitiesAutocompleteListResponsesEventHandler,
                                  // IUploadImageRequestEventHandler,
                                  // IUploadImageResponsesEventHandler,
                                  IUpdateProfileRequestEventHandler,
                                  IUpdateProfileResponsesEventHandler,
                                  ILoadUserProfileRequestEventHandler,
                                  ILoadUserProfileResponseEventHandler
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

    /*
    ****************************************************************************************************
                                            TASK: Login
    ****************************************************************************************************
    */

    @Override
    public void onRequestToLogin(Context i_Context, String i_UserName, String i_Password)
    {
        this.m_Model.onRequestToLogin(i_Context, i_UserName, i_Password);
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

    /*
    ****************************************************************************************************
                                        TASK: Anonymous Login
    ****************************************************************************************************
    */

    @Override
    public void onRequestToAnonymousLogin(Context i_Context) {
        this.m_Model.onRequestToAnonymousLogin(i_Context);
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

    /*
    ****************************************************************************************************
                                            TASK: Logout
    ****************************************************************************************************
    */

    @Override
    public void onRequestToLogout(Context i_Context)
    {
        this.m_Model.onRequestToLogout(i_Context);
    }

    @Override
    public void onSuccessToLogout()
    {
        for (IView view : views)
        {
            if (view instanceof ILogoutResponsesEventHandler)
            {
                ((ILogoutResponsesEventHandler)view).onSuccessToLogout();
            }
        }
    }

    @Override
    public void onFailureToLogout(Exception i_Reason)
    {
        for (IView view : views)
        {
            if (view instanceof ILogoutResponsesEventHandler)
            {
                ((ILogoutResponsesEventHandler)view).onFailureToLogout(i_Reason);
            }
        }
    }

    /*
    ****************************************************************************************************
                                            TASK: Signup
    ****************************************************************************************************
    */

    @Override
    public void onRequestToSignup(Context i_Context, String i_UserName, String i_Password, String i_FullName, String i_Age, eDogGender i_DogGender)
    {
        AppUtils.printDebugToLogcat("ViewModel", "onRequestToSignup", "calling onRequestToSignup()");
        this.m_Model.onRequestToSignup(i_Context, i_UserName, i_Password, i_FullName, i_Age, i_DogGender);
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

    /*
    ****************************************************************************************************
                                         TASK: Create Post
    ****************************************************************************************************
    */

    @Override
    public void onRequestToCreatePost(Context i_Context, Post i_Post) {

        AppUtils.printDebugToLogcat("ViewModel", "onRequestToCreatePost", "calling onRequestToCreatePost()");
        this.m_Model.onRequestToCreatePost(i_Context, i_Post);
        AppUtils.printDebugToLogcat("ViewModel", "onRequestToCreatePost", "returned from onRequestToCreatePost()");

    }

    @Override
    public void onSuccessToCreatePost() {
        for (IView view : views) {
            if (view instanceof IPostCreationResponseEventHandler) {
                ((IPostCreationResponseEventHandler)view).onSuccessToCreatePost();
            }
        }
    }

    @Override
    public void onFailureToCreatePost(Exception i_Reason)
    {
        for (IView view : views) {
            if (view instanceof IPostCreationResponseEventHandler)
            {
                ((IPostCreationResponseEventHandler)view).onFailureToCreatePost(i_Reason);
            }
        }
    }

    @Override
    public void onRequestToUpdateListOfCities()
    {
        ((IUpdateCitiesAutocompleteListRequestEventHandler)this.m_Model).onRequestToUpdateListOfCities();
    }

    @Override
    public void onSuccessToUpdateListOfCities(ArrayList<String> i_UpdatedListOfCities)
    {
        for (IView view : views)
        {
            if (view instanceof IUpdateCitiesAutocompleteListResponsesEventHandler)
            {
                ((IUpdateCitiesAutocompleteListResponsesEventHandler)view).onSuccessToUpdateListOfCities(i_UpdatedListOfCities);
            }
        }
    }

    @Override
    public void onFailureToUpdateListOfCities(Exception i_Reason)
    {
        for (IView view : views)
        {
            if (view instanceof IUpdateCitiesAutocompleteListResponsesEventHandler)
            {
                ((IUpdateCitiesAutocompleteListResponsesEventHandler)view).onFailureToUpdateListOfCities(i_Reason);
            }
        }
    }

    /*
    ****************************************************************************************************
                                         TASK: Load Profile
    ****************************************************************************************************
    */

    @Override
    public void onLoadProfile() {
        m_Model.onLoadProfile();
    }

    @Override
    public void onSuccessToLoadProfile(UserProfile i_UserProfile) {
        for (IView view : views) {
            if (view instanceof ILoadUserProfileResponseEventHandler) {
                ((ILoadUserProfileResponseEventHandler) view).onSuccessToLoadProfile(i_UserProfile);
            }
        }
    }

    @Override
    public void onFailureToLoadProfile(Exception i_Reason) {
        for (IView view : views) {
            if (view instanceof ILoadUserProfileResponseEventHandler) {
                ((ILoadUserProfileResponseEventHandler) view).onFailureToLoadProfile(i_Reason);
            }
        }
    }

    /*
    @Override
    public void onRequestToUploadImage(Uri i_PathOfFileInFilesystem)
    {
        this.m_Model.onRequestToUploadImage(i_PathOfFileInFilesystem);
    }
    */

    /*
    TODO: NOTE: At any moment there could be only one view which handles the success or failure of uploading image.
                In addition, the 2 fragments "HomeFragment" and "ProfileFragment" should implement the interface "IUploadImageResponsesEventHandler",
                because we don't know which one of them will handle the success or failure of upload image request.
    */

    /*
    @Override
    public void onSuccessToUploadImage(Uri i_PathOfFileInCloud)
    {
        for (IView view : views)
        {
            if (view instanceof IUploadImageResponsesEventHandler)
            {
                ((IUploadImageResponsesEventHandler)view).onSuccessToUploadImage(i_PathOfFileInCloud);
            }
        }
    }
    */

    /*
    @Override
    public void onFailureToUploadImage(Exception i_Reason)
    {
        for (IView view : views)
        {
            if (view instanceof IUploadImageResponsesEventHandler)
            {
                ((IUploadImageResponsesEventHandler)view).onFailureToUploadImage(i_Reason);
            }
        }
    }
    */

    @Override
    public void onRequestToUpdateProfile(Context i_Context, String i_FullName, String i_Age, eDogGender i_DogGender, String i_ProfileImage)
    {
        this.m_Model.onRequestToUpdateProfile(i_Context, i_FullName, i_Age, i_DogGender, i_ProfileImage);
    }

    @Override
    public void onSuccessToUpdateProfile()
    {
        for (IView view : views)
        {
            if (view instanceof IUpdateProfileResponsesEventHandler)
            {
                ((IUpdateProfileResponsesEventHandler)view).onSuccessToUpdateProfile();
            }
        }
    }

    @Override
    public void onFailureToUpdateProfile(Exception i_Reason)
    {
        for (IView view : views)
        {
            if (view instanceof IUpdateProfileResponsesEventHandler)
            {
                ((IUpdateProfileResponsesEventHandler)view).onFailureToUpdateProfile(i_Reason);
            }
        }
    }
}