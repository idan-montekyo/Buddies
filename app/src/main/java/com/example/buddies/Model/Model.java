package com.example.buddies.Model;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.buddies.common.AppUtils;
import com.example.buddies.common.Post;
import com.example.buddies.common.UserProfile;
import com.example.buddies.enums.eDogGender;
import com.example.buddies.enums.eOnAuthStateChangedCaller;
import com.example.buddies.interfaces.LoginEvent.ILoginRequestEventHandler;
import com.example.buddies.interfaces.LoginEvent.ILoginResponsesEventHandler;
import com.example.buddies.interfaces.LogoutEvent.ILogoutRequestEventHandler;
import com.example.buddies.interfaces.LogoutEvent.ILogoutResponsesEventHandler;
import com.example.buddies.interfaces.MVVM.IModel;
import com.example.buddies.interfaces.PostCreationEvent.IPostCreationRequestEventHandler;
import com.example.buddies.interfaces.PostCreationEvent.IPostCreationResponseEventHandler;
import com.example.buddies.interfaces.SignupEvent.ISignupRequestEventHandler;
import com.example.buddies.interfaces.SignupEvent.ISignupResponsesEventHandler;
import com.example.buddies.interfaces.MVVM.IViewModel;
import com.example.buddies.interfaces.UpdateCitiesAutocompleteListEvent.IUpdateCitiesAutocompleteListRequestEventHandler;
import com.example.buddies.interfaces.UpdateCitiesAutocompleteListEvent.IUpdateCitiesAutocompleteListResponsesEventHandler;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class Model implements IModel,
                              ILoginRequestEventHandler,
                              ILoginResponsesEventHandler,
                              ILogoutRequestEventHandler,
                              ILogoutResponsesEventHandler,
                              ISignupRequestEventHandler,
                              ISignupResponsesEventHandler,
                              IPostCreationRequestEventHandler,
                              IPostCreationResponseEventHandler,
                              IUpdateCitiesAutocompleteListRequestEventHandler,
                              IUpdateCitiesAutocompleteListResponsesEventHandler
{
    private static Model _instance = null;
    IViewModel viewModel = null;

    private FirebaseAuth m_FirebaseAuth;
    private FirebaseUser m_CurrentUser = null;
    FirebaseAuth.AuthStateListener m_AuthStateListener;

    boolean m_IsFirstLoad = true;
    FirebaseDatabase m_DatabaseReference = null;
    DatabaseReference m_UsersTable = null;
    DatabaseReference m_PostsTable = null;
    DatabaseReference m_CitiesTable = null;
    DataSnapshot m_CurrentSnapshotOfCitiesTable = null;
    ArrayList<String> m_ListOfCities = null;

    eOnAuthStateChangedCaller m_OnAuthStateChangedCaller = eOnAuthStateChangedCaller.UNINITIALIZED;

    private Model()
    {
        this.m_FirebaseAuth = FirebaseAuth.getInstance();

        this.m_AuthStateListener = new FirebaseAuth.AuthStateListener()
        {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
            {
                Model.this.m_CurrentUser = Model.this.m_FirebaseAuth.getCurrentUser();

                // If this is the first load of the application
                if (m_IsFirstLoad == true)
                {
                    AppUtils.printDebugToLogcat("Model.java", "onAuthStateChanged()", "This is the first load of the \"Model\" component.\nAt this point, the current user is \"" + Model.this.m_CurrentUser + "\"");

                    // Do not notify anyone (whether a user is signed in or not) and just return
                    m_IsFirstLoad = !m_IsFirstLoad;
                    return;
                }

                switch (Model.this.m_OnAuthStateChangedCaller)
                {
                    case SIGN_UP:
                        Model.this.m_CurrentUser = null;
                        break;
                    case LOG_IN:
                        Model.this.onSuccessToLogin();
                        break;
                    case LOG_IN_ANONYMOUSLY:
                        Model.this.onSuccessToAnonymousLogin();
                        break;
                    case LOG_OUT:
                        Model.this.onSuccessToLogout();
                        break;
                    case UNINITIALIZED:
                    default:
                        break;
                }

                // TODO: Think if we need the user name...
                // String currentUserName = Model.this.m_CurrentUser.getDisplayName();
                AppUtils.printDebugToLogcat("Model.java", "onAuthStateChanged()", "current user is: " + m_CurrentUser);
                AppUtils.printDebugToLogcat("Model.java", "onAuthStateChanged()", "traceback: " + Log.getStackTraceString(new Exception()));
                Model.this.m_OnAuthStateChangedCaller = eOnAuthStateChangedCaller.UNINITIALIZED;
            }
        };

        this.m_FirebaseAuth.addAuthStateListener(this.m_AuthStateListener);

        this.m_DatabaseReference = FirebaseDatabase.getInstance();
        this.m_UsersTable = m_DatabaseReference.getReference("users");
        this.m_PostsTable = m_DatabaseReference.getReference("posts");
        this.m_CitiesTable = m_DatabaseReference.getReference("cities");

        // Load all cities in m_CitiesTable to m_ListOfCities. (Source: https://www.youtube.com/watch?v=XactTKR0Wfc)
        this.m_CitiesTable.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {

                Model.this.m_CurrentSnapshotOfCitiesTable = dataSnapshot;
                Model.this.onRequestToUpdateListOfCities();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                ((IUpdateCitiesAutocompleteListResponsesEventHandler)Model.this.viewModel).onFailureToUpdateListOfCities(databaseError.toException());
            }
        });
    }

    public static Model getInstance()
    {
        if (_instance == null)
        {
            _instance = new Model();
        }
        return _instance;
    }

    @Override
    public void registerForEvents(IViewModel i_ViewModelToRegister)
    {
        this.viewModel = i_ViewModelToRegister;
    }

    @Override
    public void unregisterForEvents()
    {
        this.viewModel = null;
    }

    /*
    ****************************************************************************************************
                                            TASK: Signup
    ****************************************************************************************************
    */

    @Override
    public void onRequestToSignup(Context i_Context, String i_UserName, String i_Password, String i_FullName, String i_Age, eDogGender i_DogGender)
    {
        try
        {
            this.m_OnAuthStateChangedCaller = eOnAuthStateChangedCaller.SIGN_UP;
            AppUtils.printDebugToLogcat("Model", "onRequestToSignup", "trying to sign up the desired user ...");

            Task<AuthResult> signupHandler = this.m_FirebaseAuth.createUserWithEmailAndPassword(i_UserName + "@Buddies.com", i_Password);
            signupHandler.addOnCompleteListener(new OnCompleteListener<AuthResult>()
            {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task)
                {
                    AppUtils.printDebugToLogcat("Model", "onRequestToSignup.onComplete", "task.isSuccessful() == " + task.isSuccessful());
                    if (task.isSuccessful() == true)
                    {
                        try
                        {
                            UserProfile currentUserProfile = new UserProfile(i_FullName, Integer.parseInt(i_Age), i_DogGender);
                            String currentUserId = getCurrentUserUID();

                            // Save the extra details of the user in the database too.
                            Model.this.m_UsersTable.child(currentUserId).setValue(currentUserProfile);

                            // Logout in order to force the user to re-enter it's credentials in order to sign in
                            Model.this.m_FirebaseAuth.signOut();

                            // Notify that the signup process has been successfully accomplished.
                            Model.this.onSuccessToSignup();
                        }
                        catch (Exception err)
                        {
                            Model.this.onFailureToSignup(err);
                        }
                    }
                    else
                    {
                        Exception reason = task.getException();
                        Model.this.onFailureToSignup(reason);
                    }
                }
            });
        }
        catch (Exception err)
        {
            this.onFailureToSignup(err);
        }
    }

    @Override
    public void onSuccessToSignup()
    {
        ((ISignupResponsesEventHandler)this.viewModel).onSuccessToSignup();
    }

    @Override
    public void onFailureToSignup(Exception i_Reason)
    {
        ((ISignupResponsesEventHandler)this.viewModel).onFailureToSignup(i_Reason);
    }

    /*
    ****************************************************************************************************
                                            TASK: Login
    ****************************************************************************************************
    */

    @Override
    public void onRequestToLogin(String i_UserName, String i_Password)
    {
        // firebase code here
        try
        {
            this.m_OnAuthStateChangedCaller = eOnAuthStateChangedCaller.LOG_IN;
            Task<AuthResult> loginHandler = this.m_FirebaseAuth.signInWithEmailAndPassword(i_UserName + "@Buddies.com", i_Password);
            loginHandler.addOnCompleteListener(new OnCompleteListener<AuthResult>()
            {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task)
                {
                    if (task.isSuccessful() == false)
                    {
                        Model.this.onFailureToLogin(task.getException());
                    }
                }
            });
        }
        catch (Exception err)
        {
            this.onFailureToLogin(err);
        }
    }

    @Override
    public void onSuccessToLogin()
    {
        // if firebase login succeeded
        ((ILoginResponsesEventHandler)this.viewModel).onSuccessToLogin();
    }

    @Override
    public void onFailureToLogin(Exception i_Reason)
    {
        // if firebase login failed
        ((ILoginResponsesEventHandler)this.viewModel).onFailureToLogin(i_Reason);
    }

    /*
    ****************************************************************************************************
                                        TASK: Anonymous Login
    ****************************************************************************************************
    */

    @Override
    public void onRequestToAnonymousLogin()
    {
        // firebase code here
        try
        {
            this.m_OnAuthStateChangedCaller = eOnAuthStateChangedCaller.LOG_IN_ANONYMOUSLY;
            this.m_FirebaseAuth.signInAnonymously();
        }
        catch (Exception err)
        {
            this.onFailureToAnonymousLogin(err);
        }
    }

    @Override
    public void onSuccessToAnonymousLogin()
    {
        // if firebase anonymous login succeeded
        ((ILoginResponsesEventHandler)this.viewModel).onSuccessToAnonymousLogin();
    }

    @Override
    public void onFailureToAnonymousLogin(Exception i_Reason)
    {
        // if firebase anonymous login failed
        ((ILoginResponsesEventHandler)this.viewModel).onFailureToAnonymousLogin(i_Reason);
    }

    /*
    ****************************************************************************************************
                                            TASK: Logout
    ****************************************************************************************************
    */

    @Override
    public void onRequestToLogout()
    {
        // TODO: Decide later what to do with the use case of logging out when the WiFi and either the Mobile Data
        //       both turned off.
        try
        {
            this.m_OnAuthStateChangedCaller = eOnAuthStateChangedCaller.LOG_OUT;
            this.m_FirebaseAuth.signOut();
        }
        catch (Exception err)
        {
            this.onFailureToLogout(err);
        }
    }

    @Override
    public void onSuccessToLogout()
    {
        ((ILogoutResponsesEventHandler)this.viewModel).onSuccessToLogout();
    }

    @Override
    public void onFailureToLogout(Exception i_Reason)
    {
        // if firebase logout failed
        ((ILogoutResponsesEventHandler)this.viewModel).onFailureToLogout(i_Reason);
    }

    /*
    ****************************************************************************************************
                                         TASK: Create Post
    ****************************************************************************************************
    */

    @Override
    public void onRequestToCreatePost(Context i_Context, Post i_Post)
    {
        AppUtils.printDebugToLogcat("Model", "onRequestToCreatePost", "trying to save post details to FireBase...");

        try
        {
            // Save post under the current user at FireBase -> posts.
            String newPostKey = m_PostsTable.child(getCurrentUserUID()).push().getKey();
            m_PostsTable.child(getCurrentUserUID()).child(newPostKey).setValue(i_Post);
            // Save city at FireBase -> cities (if the city does not exist already).
            String desiredCity = i_Post.getMeetingCity();
            if (!this.m_ListOfCities.contains(desiredCity)) {
                m_CitiesTable.child(desiredCity).setValue(true);
            }
            onSuccessToCreatePost();
        }
        catch (Exception exception)
        {
            onFailureToCreatePost(exception);
        }
    }

    @Override
    public void onSuccessToCreatePost()
    {
        ((IPostCreationResponseEventHandler)viewModel).onSuccessToCreatePost();
    }

    @Override
    public void onFailureToCreatePost(Exception i_Reason)
    {
        ((IPostCreationResponseEventHandler)viewModel).onFailureToCreatePost(i_Reason);
    }

    /*
    ****************************************************************************************************
                                     TASK: Update list of cities
    ****************************************************************************************************
    */

    @Override
    public void onRequestToUpdateListOfCities()
    {
        String currentCity = null;
        m_ListOfCities = new ArrayList<String>();

        try
        {
            for (DataSnapshot currentRecord : this.m_CurrentSnapshotOfCitiesTable.getChildren())
            {
                currentCity = currentRecord.getKey(); // currentRecord.getValue(String.class);
                m_ListOfCities.add(currentCity);
            }

            this.onSuccessToUpdateListOfCities(m_ListOfCities);
        }
        catch (Exception err)
        {
            this.onFailureToUpdateListOfCities(err);
        }
    }

    @Override
    public void onSuccessToUpdateListOfCities(ArrayList<String> i_UpdatedListOfCities)
    {
        ((IUpdateCitiesAutocompleteListResponsesEventHandler)Model.this.viewModel).onSuccessToUpdateListOfCities(i_UpdatedListOfCities);
    }

    @Override
    public void onFailureToUpdateListOfCities(Exception i_Reason)
    {
        ((IUpdateCitiesAutocompleteListResponsesEventHandler)Model.this.viewModel).onFailureToUpdateListOfCities(i_Reason);
    }

    /*
    ****************************************************************************************************
                                             Common Functions
    ****************************************************************************************************
    */

    public boolean isUserLoggedIn() {
        return this.m_CurrentUser != null;
    }

    public boolean isCurrentUserAnonymous()
    {
        return this.m_CurrentUser.isAnonymous();
    }

    public String getCurrentUserUID() {
        return Objects.requireNonNull(this.m_FirebaseAuth.getCurrentUser()).getUid();
    }
}