package com.example.buddies.Model;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.buddies.common.AppUtils;
import com.example.buddies.common.UserProfile;
import com.example.buddies.enums.eDogGender;
import com.example.buddies.enums.eOnAuthStateChangedCaller;
import com.example.buddies.interfaces.LoginEvent.ILoginRequestEventHandler;
import com.example.buddies.interfaces.LoginEvent.ILoginResponsesEventHandler;
import com.example.buddies.interfaces.LogoutEvent.ILogoutRequestEventHandler;
import com.example.buddies.interfaces.LogoutEvent.ILogoutResponsesEventHandler;
import com.example.buddies.interfaces.MVVM.IModel;
import com.example.buddies.interfaces.SignupEvent.ISignupRequestEventHandler;
import com.example.buddies.interfaces.SignupEvent.ISignupResponsesEventHandler;
import com.example.buddies.interfaces.MVVM.IViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Model implements IModel,
                              ILoginRequestEventHandler,
                              ILoginResponsesEventHandler,
                              ILogoutRequestEventHandler,
                              ILogoutResponsesEventHandler,
                              ISignupRequestEventHandler,
                              ISignupResponsesEventHandler
{
    private static Model _instance = null;
    IViewModel viewModel = null;

    private FirebaseAuth m_FirebaseAuth;
    private FirebaseUser m_CurrentUser = null;
    FirebaseAuth.AuthStateListener m_AuthStateListener;

    FirebaseDatabase m_DatabaseReference = null;
    DatabaseReference m_UsersTable = null;

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
                Model.this.m_OnAuthStateChangedCaller = eOnAuthStateChangedCaller.UNINITIALIZED;
            }
        };

        this.m_FirebaseAuth.addAuthStateListener(this.m_AuthStateListener);

        this.m_DatabaseReference = FirebaseDatabase.getInstance();
        this.m_UsersTable = m_DatabaseReference.getReference("users");
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
                            String currentUserId = Model.this.m_FirebaseAuth.getCurrentUser().getUid();

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
            // anonymousLoginHasBeenRequested = null;
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

    public boolean isCurrentUserAnonymous()
    {
        return this.m_CurrentUser.isAnonymous();
    }

    public boolean isUserLoggedIn()
    {
        return m_FirebaseAuth.getCurrentUser() != null;
    }
}