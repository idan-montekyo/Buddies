package com.example.buddies.Model;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.example.buddies.adapters.PostAdapter;
import com.example.buddies.common.AppUtils;
import com.example.buddies.common.Post;
import com.example.buddies.common.ProgressNotification;
import com.example.buddies.common.UserProfile;
import com.example.buddies.enums.eDogGender;
import com.example.buddies.enums.eOnAuthStateChangedCaller;
import com.example.buddies.enums.ePostType;
import com.example.buddies.interfaces.LoadPostCardEvent.ILoadPostCardRequestEventHandler;
import com.example.buddies.interfaces.LoadPostCardEvent.ILoadPostCardResponseEventHandler;
import com.example.buddies.interfaces.LoadPostsEvent.ILoadPostsRequestEventHandler;
import com.example.buddies.interfaces.LoadPostsEvent.ILoadPostsResponseEventHandler;
import com.example.buddies.interfaces.LoadUserProfileEvent.ILoadUserProfileRequestEventHandler;
import com.example.buddies.interfaces.LoadUserProfileEvent.ILoadUserProfileResponseEventHandler;
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
import com.example.buddies.interfaces.UpdateProfileEvent.IUpdateProfileRequestEventHandler;
import com.example.buddies.interfaces.UpdateProfileEvent.IUpdateProfileResponsesEventHandler;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
                              IUpdateCitiesAutocompleteListResponsesEventHandler,
                              ILoadUserProfileRequestEventHandler,
                              ILoadUserProfileResponseEventHandler,
                              // IUploadImageRequestEventHandler,
                              // IUploadImageResponsesEventHandler,
                              IUpdateProfileRequestEventHandler,
                              IUpdateProfileResponsesEventHandler,
                              ILoadPostsRequestEventHandler,
                              ILoadPostsResponseEventHandler,
                              ILoadPostCardRequestEventHandler,
                              ILoadPostCardResponseEventHandler
{
    private static Model _instance = null;
    IViewModel viewModel = null;

    private FirebaseAuth m_FirebaseAuth;
    private FirebaseUser m_CurrentUser = null;
    FirebaseAuth.AuthStateListener m_AuthStateListener;

    boolean m_IsFirstLoad = true;
    boolean m_IsOccurringAProfileDetailsUpdate = false;
    FirebaseDatabase m_DatabaseReference = null;

    // Create a storage reference from our app
    StorageReference m_StorageReference = null;
    DatabaseReference m_UsersTable = null;
    DatabaseReference m_PostsTable = null;
    DatabaseReference m_CitiesTable = null;
    DataSnapshot m_CurrentSnapshotOfCitiesTable = null;
    DataSnapshot m_CurrentSnapshotOfUsersTable = null;
    DataSnapshot m_CurrentSnapshotOfPostsTable = null;
    ArrayList<String> m_ListOfCities = null;

    UserProfile m_UserProfile = null;
    List<Post> m_PostsList = null;

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

                AppUtils.printDebugToLogcat("Model.java", "onAuthStateChanged()", "current user is: " + m_CurrentUser);
                AppUtils.printDebugToLogcat("Model.java", "onAuthStateChanged()", "traceback: " + Log.getStackTraceString(new Exception()));
                Model.this.m_OnAuthStateChangedCaller = eOnAuthStateChangedCaller.UNINITIALIZED;
            }
        };

        this.m_FirebaseAuth.addAuthStateListener(this.m_AuthStateListener);

        // Load the storage reference from the storage url.
        // The load has to be made with specifying the storage url in order to avoid the error StorageException
        // with Code: -13000 and HttpResult: 400 (Source: https://stackoverflow.com/a/40189271/2196301)
        this.m_StorageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://leafy-firmament-349212.appspot.com");

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

        this.m_UsersTable.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    m_CurrentSnapshotOfUsersTable = snapshot;
                } catch (Exception exception) {
                    ((ILoadUserProfileResponseEventHandler)viewModel).onFailureToLoadProfile(exception);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                ((ILoadUserProfileResponseEventHandler)viewModel).onFailureToLoadProfile(error.toException());
            }
        });

        this.m_PostsTable.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    m_CurrentSnapshotOfPostsTable = snapshot;
                } catch (Exception exception) {
                    ((ILoadPostsResponseEventHandler)viewModel).onFailureToLoadPosts(exception);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                ((ILoadPostsResponseEventHandler)viewModel).onFailureToLoadPosts(error.toException());
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
            if (this.areRegisterDetailsValid(i_UserName, i_Password, i_FullName, i_Age, i_DogGender) == false) {
                throw new Exception("Fill in all required details");
            } else if (AppUtils.isNetworkAvailable(i_Context) == false) {
                throw new Exception("No internet access");
            }
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
                            Model.this.onRequestToUpdateProfile(i_Context, i_FullName, i_Age, i_DogGender, "");

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
    public void onRequestToLogin(Context i_Context, String i_UserName, String i_Password)
    {
        // firebase code here
        try
        {
            if (AppUtils.isNetworkAvailable(i_Context) == false) {
                throw new Exception("No internet access");
            }
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
    public void onRequestToAnonymousLogin(Context i_Context)
    {
        // firebase code here
        try
        {
            if (AppUtils.isNetworkAvailable(i_Context) == false)
            {
                throw new Exception("No internet access");
            }
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
    public void onRequestToLogout(Context i_Context)
    {
        try
        {
            if (AppUtils.isNetworkAvailable(i_Context) == false) {
                throw new Exception("No internet access");
            }
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
            // TODO: Check if this solves the bug of sometime exception in load of the app.
            if (this.m_CurrentSnapshotOfCitiesTable != null)
            {
                for (DataSnapshot currentRecord : this.m_CurrentSnapshotOfCitiesTable.getChildren()) {
                    currentCity = currentRecord.getKey(); // currentRecord.getValue(String.class);
                    m_ListOfCities.add(currentCity);
                }
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
                                         TASK: Upload image
    ****************************************************************************************************
    */

    /*
    @Override
    public void onRequestToUploadImage(Uri i_PathOfFileInFilesystem)
    {
        // Create the file metadata
        StorageMetadata.Builder metadata = new StorageMetadata.Builder();
        metadata.setContentType("image/jpeg");

        // Get the filename from the path
        String filename = i_PathOfFileInFilesystem.getLastPathSegment();

        // Upload file and metadata to the path 'images/<filename>.jpeg'
        StorageReference referenceToFile = storageRef.child("images/" + filename);
        UploadTask uploadTask = referenceToFile.putFile(i_PathOfFileInFilesystem, metadata.build());

        // Listen for state changes, errors, and completion of the upload.
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>()
        {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot)
            {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                AppUtils.printDebugToLogcat("Model.java", "onRequestToUploadImage()", "Upload is " + progress + "% done");
            }
        });

        uploadTask.addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>()
        {
            @Override
            public void onPaused(UploadTask.TaskSnapshot taskSnapshot)
            {
                AppUtils.printDebugToLogcat("Model.java", "onRequestToUploadImage()", "Upload is paused");
            }
        });

        uploadTask.addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception exception)
            {
                Model.this.onFailureToUploadImage(exception);
            }
        });

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
        {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
            {
                try
                {
                    StorageReference referenceToFileInCloud = taskSnapshot.getStorage();
                    Task<Uri> fileUriOwner = referenceToFileInCloud.getDownloadUrl();

                    fileUriOwner.addOnCompleteListener(new OnCompleteListener<Uri>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task)
                        {
                            Uri uriOfFileInCloud = task.getResult();
                            Model.this.onSuccessToUploadImage(uriOfFileInCloud);
                        }
                    });
                }
                catch (Exception exception)
                {
                    Model.this.onFailureToUploadImage(exception);
                }
            }
        });
    }

    @Override
    public void onSuccessToUploadImage(Uri i_PathOfFileInCloud)
    {
        if (m_IsOccurringAProfileDetailsUpdate == true)
        {
            m_IsOccurringAProfileDetailsUpdate = false;
            this.onSuccessToUpdateProfile();
        }
        else
        {
            ((IUploadImageResponsesEventHandler)this.viewModel).onSuccessToUploadImage(i_PathOfFileInCloud);
        }
    }

    @Override
    public void onFailureToUploadImage(Exception i_Reason)
    {
        if (m_IsOccurringAProfileDetailsUpdate == true)
        {
            m_IsOccurringAProfileDetailsUpdate = false;
            this.onFailureToUpdateProfile(i_Reason);
        }
        else
        {
            ((IUploadImageResponsesEventHandler) this.viewModel).onFailureToUploadImage(i_Reason);
        }
    }
    */

    @Override
    public void onRequestToUpdateProfile(Context i_Context, String i_FullName, String i_Age, eDogGender i_DogGender, String i_ProfileImage)
    {
        m_IsOccurringAProfileDetailsUpdate = true;

        try
        {
            UserProfile currentUserProfile = new UserProfile(i_FullName, Integer.parseInt(i_Age), i_DogGender, "");

            String currentUserId = getCurrentUserUID();

            if (i_ProfileImage.equals("") == false)
            {
                Uri profileImageUri = Uri.parse(i_ProfileImage);

                // Create the file metadata
                StorageMetadata.Builder metadata = new StorageMetadata.Builder();
                metadata.setContentType("image/jpeg");

                // Get the filename from the path
                String filename = profileImageUri.getLastPathSegment();

                // Upload file and metadata to the path 'images/<filename>.jpeg'
                StorageReference referenceToFile = this.m_StorageReference.child("images/ProfilePictureOfUser_" + currentUserId + ".jpg");

                UploadTask uploadTask = null;
                final long fileSize;

                // If the file has been se;ected from the gallery
                if (i_ProfileImage.contains("content://") == true)
                {
                    AssetFileDescriptor fileDescriptor = i_Context.getContentResolver().openAssetFileDescriptor(profileImageUri , "r");
                    fileSize = fileDescriptor.getLength();

                    uploadTask = referenceToFile.putFile(profileImageUri, metadata.build());
                }
                // If the file has been selected from the camera
                else
                {
                    File f = new File(i_ProfileImage);
                    fileSize = f.length();
                    InputStream stream = new FileInputStream(f);

                    uploadTask = referenceToFile.putStream(stream);
                }

                // Listen for state changes, errors, and completion of the upload.
                uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>()
                {
                    boolean isFirstCall = true;
                    ProgressNotification progressNotification;
                    final long totalBytesCount = fileSize;
                    long bytesLoadedSoFar = 0;

                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot)
                    {
                        if (isFirstCall == true)
                        {
                            isFirstCall = false;

                            // totalBytesCount = taskSnapshot.getTotalByteCount();

                            progressNotification = new ProgressNotification(i_Context,
                                    ProgressNotification.eSecondaryNotificationTypes.NOTIFICATIONS_OF_UPLOADS_PROGRESSES___UPLOAD_IMAGE_PROGRESS,
                                    "Profile Picture Upload Progress",
                                    "Upload in progress ... (0%)");

                            progressNotification.launchNotification();
                        }

                        bytesLoadedSoFar = taskSnapshot.getBytesTransferred();

                        double progress = (100.0 * bytesLoadedSoFar) / totalBytesCount;

                        progressNotification.updateNotification("Profile Picture Upload Progress", "Upload in progress ... (" + progress + "%)", progress);
                        AppUtils.printDebugToLogcat("Model.java", "onRequestToUploadImage()", "Upload is " + progress + "% done");
                    }
                });

                uploadTask.addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>()
                {
                    @Override
                    public void onPaused(UploadTask.TaskSnapshot taskSnapshot)
                    {
                        AppUtils.printDebugToLogcat("Model.java", "onRequestToUploadImage()", "Upload is paused");
                    }
                });

                uploadTask.addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception exception)
                    {
                        Model.this.onFailureToUpdateProfile(exception);
                    }
                });

                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
                {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                    {
                        StorageReference referenceToFileInCloud = taskSnapshot.getStorage();
                        Task<Uri> fileUriOwner = referenceToFileInCloud.getDownloadUrl();

                        fileUriOwner.addOnCompleteListener(new OnCompleteListener<Uri>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task)
                            {
                                Uri uriOfFileInCloud = task.getResult();

                                currentUserProfile.setProfileImageUri(uriOfFileInCloud.toString());

                                // Save the extra details of the user in the database too.
                                Model.this.m_UsersTable.child(currentUserId).setValue(currentUserProfile);

                                Model.this.onSuccessToUpdateProfile();
                            }
                        });
                    }
                });
            }
            else
            {
                // Save the extra details of the user in the database too.
                Model.this.m_UsersTable.child(currentUserId).setValue(currentUserProfile);
                Model.this.onSuccessToUpdateProfile();
            }
        }
        catch (Exception exception)
        {
            Model.this.onFailureToUpdateProfile(exception);
        }
    }

    @Override
    public void onSuccessToUpdateProfile()
    {
        ((IUpdateProfileResponsesEventHandler)this.viewModel).onSuccessToUpdateProfile();
    }

    @Override
    public void onFailureToUpdateProfile(Exception i_Reason)
    {
        ((IUpdateProfileResponsesEventHandler)this.viewModel).onFailureToUpdateProfile(i_Reason);
    }

    /*
    ****************************************************************************************************
                                         TASK: Load Profile
    ****************************************************************************************************
    */

    @Override
    public void onLoadProfile() {

        m_UserProfile = m_CurrentSnapshotOfUsersTable.child(getCurrentUserUID())
                .getValue(UserProfile.class);
        if (m_UserProfile != null) {
            onSuccessToLoadProfile(m_UserProfile);
        } else {
            onFailureToLoadProfile(new Exception("Model.m_UserProfile is null"));
        }
    }

    @Override
    public void onSuccessToLoadProfile(UserProfile i_UserProfile) {
        ((ILoadUserProfileResponseEventHandler)viewModel).onSuccessToLoadProfile(i_UserProfile);
    }

    @Override
    public void onFailureToLoadProfile(Exception i_Reason) {
        ((ILoadUserProfileResponseEventHandler)viewModel).onFailureToLoadProfile(i_Reason);
    }

    /*
    ****************************************************************************************************
                                         TASK: Load Posts
    ****************************************************************************************************
    */

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onLoadPosts(ePostType type) {

        try {
            m_PostsTable = m_CurrentSnapshotOfPostsTable.getRef();
            m_PostsList = new ArrayList<>();

            switch (type) {
                case ALL:
                    for (DataSnapshot UserUIDs : m_CurrentSnapshotOfPostsTable.getChildren()) {
                        for (DataSnapshot post : UserUIDs.getChildren()) {
                            LatLng latLng = new LatLng((double) post.child("meetingLocation").child("latitude").getValue(),
                                    (double) post.child("meetingLocation").child("longitude").getValue());

                            Long localHour = (long)post.child("postCreationTime").child("hour").getValue();
                            Long localMinute = (long)post.child("postCreationTime").child("minute").getValue();
                            Long localSecond = (long)post.child("postCreationTime").child("second").getValue();
                            Long localNano = (long)post.child("postCreationTime").child("nano").getValue();

                            LocalTime localTime = LocalTime.of(localHour.intValue(), localMinute.intValue(),
                                    localSecond.intValue(), localNano.intValue());

                            Long creationYear = (long) post.child("postCreationDate").child("postCreationYear").getValue();
                            Long creationMonth = (long) post.child("postCreationDate").child("postCreationMonth").getValue();
                            Long creationDay = (long) post.child("postCreationDate").child("postCreationDay").getValue();
                            Long creationDateTimeAsLong = (long) post.child("postCreationDateTimeAsLong").getValue();

                            Post newPost = new Post((String) post.child("creatorUserUID").getValue(),
                                    (String) post.child("meetingCity").getValue(),
                                    (String) post.child("meetingStreet").getValue(),
                                    (String) post.child("meetingTime").getValue(),
                                    latLng,
                                    (String) post.child("postContent").getValue(),
                                    localTime, creationDateTimeAsLong,
                                    creationYear.intValue(), creationMonth.intValue(), creationDay.intValue());
                            m_PostsList.add(newPost);
                        }
                    }
                    break;
                case MY_POSTS:
                case POSTS_I_COMMENTED_ON:
                    break;
            }

            Collections.sort(m_PostsList);
            Model.this.onSuccessToLoadPosts(m_PostsList);

        } catch (Exception exception) {
            Model.this.onFailureToLoadPosts(exception);
        }
    }

    @Override
    public void onSuccessToLoadPosts(List<Post> i_PostsList) {
        ((ILoadPostsResponseEventHandler)viewModel).onSuccessToLoadPosts(i_PostsList);
    }

    @Override
    public void onFailureToLoadPosts(Exception i_Reason) {
        ((ILoadPostsResponseEventHandler)viewModel).onFailureToLoadPosts(i_Reason);
    }

    /*
    ****************************************************************************************************
                                         TASK: Load Post Cards
    ****************************************************************************************************
    */

    @Override
    public void onLoadPostCard(String i_CreatorUserUID, PostAdapter i_PostAdapterToUpdate) {

        try {

            m_UserProfile = m_CurrentSnapshotOfUsersTable.child(i_CreatorUserUID).getValue(UserProfile.class);
            if (m_UserProfile != null) {
                onSuccessToLoadPostCard(m_UserProfile, i_PostAdapterToUpdate);
            } else {
                onFailureToLoadPostCard(new Exception("Model.m_UserProfile is null"), i_PostAdapterToUpdate);
            }

        } catch (Exception exception) {
            onFailureToLoadPostCard(exception, i_PostAdapterToUpdate);
        }
    }

    @Override
    public void onSuccessToLoadPostCard(UserProfile i_UserProfile, PostAdapter i_PostAdapterToUpdate) {
        ((ILoadPostCardResponseEventHandler)viewModel).onSuccessToLoadPostCard(i_UserProfile, i_PostAdapterToUpdate);
    }

    @Override
    public void onFailureToLoadPostCard(Exception i_Reason, PostAdapter i_PostAdapterToUpdate) {
        ((ILoadPostCardResponseEventHandler)viewModel).onFailureToLoadPostCard(i_Reason, i_PostAdapterToUpdate);
    }

    /*
    ****************************************************************************************************
                                             Common Functions
    ****************************************************************************************************
    */

    public boolean isUserLoggedIn()
    {
        return this.m_CurrentUser != null;
    }

    public boolean isCurrentUserAnonymous()
    {
        return this.m_CurrentUser.isAnonymous();
    }

    public String getCurrentUserUID()
    {
        return Objects.requireNonNull(this.m_FirebaseAuth.getCurrentUser()).getUid();
    }

    public boolean areRegisterDetailsValid(String i_UserName, String i_Password,
                                           String i_FullName, String i_age, eDogGender i_DogGender) {
        if (i_UserName.equals("") || i_Password.equals("") || i_FullName.equals("") || i_age.equals("")
                || i_DogGender == eDogGender.UNINITIALIZED) {
            return false;
        }
        return true;
    }
}