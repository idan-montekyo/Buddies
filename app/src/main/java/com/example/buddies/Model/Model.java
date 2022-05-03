package com.example.buddies.Model;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.buddies.common.AppUtils;
import com.example.buddies.common.UserProfile;
import com.example.buddies.enums.eDogGender;
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
    FirebaseAuth.AuthStateListener m_AuthStateListener;

    FirebaseDatabase m_DatabaseReference = null;
    DatabaseReference m_UsersTable = null;

    private Model()
    {
        this.m_FirebaseAuth = FirebaseAuth.getInstance();

        this.m_AuthStateListener = new FirebaseAuth.AuthStateListener()
        {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) { }
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

    @Override
    public void onRequestToLogin(String i_UserName, String i_Password)
    {
        // firebase code here
        try
        {
            Task<AuthResult> loginHandler = this.m_FirebaseAuth.signInWithEmailAndPassword(i_UserName + "@Buddies.com", i_Password);
            loginHandler.addOnCompleteListener(new OnCompleteListener<AuthResult>()
            {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task)
                {
                    if (task.isSuccessful() == true)
                    {
                        Model.this.onSuccessToLogin();
                    }
                    else
                    {
                        Model.this.onFailureToLogin(new Exception("An error occured while trying to login to the system.\nPlease retry or contact our support."));
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

    @Override
    public void onRequestToAnonymousLogin()
    {
        // firebase code here
        try
        {
            this.m_FirebaseAuth.signInAnonymously();
            this.onSuccessToAnonymousLogin();
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

    @Override
    public void onRequestToLogout()
    {
        try
        {
            this.m_FirebaseAuth.signOut();
            this.onSuccessToLogout();
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

    @Override
    public void onSuccessToAnonymousLogout()
    {
        ((ILogoutResponsesEventHandler)this.viewModel).onSuccessToAnonymousLogout();
    }

    @Override
    public void onFailureToAnonymousLogout(Exception i_Reason)
    {
        // if firebase anonymous logout failed
        ((ILogoutResponsesEventHandler)this.viewModel).onFailureToAnonymousLogout(i_Reason);
    }

    @Override
    public void onRequestToSignup(Context i_Context, String i_UserName, String i_Password, String i_FullName, String i_Age, eDogGender i_DogGender)
    {
        try
        {
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

                            // Notify that the signup process has been successfully accomplished.
                            Model.this.onSuccessToSignup();
                        }
                        catch (Exception err)
                        {

                        }
                    }
                    else
                    {
                        Exception reason = task.getException();
                        Model.this.onFailureToSignup(reason);
                    }
                }
            });

            /*
            TODO: add here the other data of the user, except the username and the password.
                  for example:
                      1. Full Name
                      2. Age
             */
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
}