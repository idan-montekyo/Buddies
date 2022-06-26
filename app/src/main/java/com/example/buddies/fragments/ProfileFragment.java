package com.example.buddies.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.buddies.BuildConfig;
import com.example.buddies.R;
import com.example.buddies.ViewModel.ViewModel;
import com.example.buddies.common.AppUtils;
import com.example.buddies.common.UserProfile;
import com.example.buddies.enums.eDogGender;
import com.example.buddies.interfaces.LoadUserProfileEvent.ILoadUserProfileRequestEventHandler;
import com.example.buddies.interfaces.LoadUserProfileEvent.ILoadUserProfileResponseEventHandler;
import com.example.buddies.interfaces.MVVM.IView;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;

public class ProfileFragment extends Fragment implements IView,
                                                         ILoadUserProfileRequestEventHandler,
                                                         ILoadUserProfileResponseEventHandler
{
    public  static final String PROFILE_FRAGMENT_TAG = "profile_fragment";

    ActivityResultLauncher<Intent> takePictureActivityResultLauncher;
    ActivityResultLauncher<String> pickFromGalleryActivityResultLauncher;
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;
    private ActivityResultLauncher<String> requestGalleryPermissionLauncher;

    private String imgUri = "";
    private String currentPhotoPath;

    EditText m_FullNameEt, m_AgeEt;
    ImageButton cameraBtn, galleryBtn, backBtn;
    Button resetImgBtn, saveBtn;
    ImageView imageView;

    // need to add current image.
    RadioGroup m_radioGroup;
    RadioButton m_radioButton;
    eDogGender m_dogGender = eDogGender.UNINITIALIZED;

    ViewModel m_ViewModel = ViewModel.getInstance();
    Context m_Context = null;
    TextWatcher m_EditTextComponentsTextWatcher = null;
    UserProfile m_CurrentUserProfile = null;

    boolean m_IsLoadingProcessEnded = false;
    boolean m_IsPermissionToReadExternalStorageHasBeenRequestedByCamera = false;

    Uri photoURI = null;

    // Initialize both ActivityResultLaunchers.
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onAttach(@NonNull Context context)
    {
        this.m_Context = context;
        this.initializeActivityResultLaunchers();
        super.onAttach(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initializeActivityResultLaunchers()
    {
        // Request camera permission in with contracts (Source: https://stackoverflow.com/a/63546099/2196301)
        this.requestCameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted ->
        {
            if (isGranted)
            {
                ProfileFragment.this.selectPhotoFromCamera();
            }
            else
            {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
            }
        });

        // Request gallery permission in with contracts (Source: https://stackoverflow.com/a/63546099/2196301)
        this.requestGalleryPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted ->
        {
            if (isGranted)
            {
                if (m_IsPermissionToReadExternalStorageHasBeenRequestedByCamera == false)
                {
                    ProfileFragment.this.selectPhotoFromGallery();
                }
                else
                {
                    m_IsPermissionToReadExternalStorageHasBeenRequestedByCamera = false;

                    // Request for camera permission.
                    // If permission will be granted, the method "onRequestPermissionsResult()" will open the camera
                    if (ContextCompat.checkSelfPermission(Objects.requireNonNull(ProfileFragment.this.m_Context), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                    {
                        ProfileFragment.this.requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
                    }
                    else
                    {
                        ProfileFragment.this.selectPhotoFromCamera();
                    }
                }
            }
            else
            {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
            }
        });

        // Source - Capture image with camera - https://www.youtube.com/watch?v=RaOyw84625w
        // After taking a picture - load into ImageView, and save URI to imgUri.
        takePictureActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>()
                {
                    @Override
                    public void onActivityResult(ActivityResult result)
                    {
                        // Check condition. In case camera opened and closed.
                        if (result.getResultCode() == Activity.RESULT_OK)
                        {
                            AppUtils.printDebugToLogcat("ProfileFragment", "onActivityResult()", "currentPhotoPath is: " + currentPhotoPath);
                            AppUtils.printDebugToLogcat("ProfileFragment", "onActivityResult()", "photoURI is: " + photoURI);

                            AppUtils.loadImageUsingGlide(
                                    ProfileFragment.this.m_Context,
                                    photoURI,
                                    null,
                                    null,
                                    true,
                                    null,
                                    imageView);

                            /*
                            Glide.with(Objects.requireNonNull(ProfileFragment.this.m_Context)).
                                    load(currentPhotoPath).circleCrop().into(imageView);
                            */
                            imgUri = currentPhotoPath;

                            ProfileFragment.this.saveBtn.setEnabled(true);
                        }
                    }
                }
        );

        // Source - Open file manager and pick photo (get Uri) - https://www.youtube.com/watch?v=cXyeozbLqq0
        // After picking from gallery - Load into ImageView, and save URI as String to imgUri.
        pickFromGalleryActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>()
                {
                    @Override
                    public void onActivityResult(Uri result)
                    {
                        if (result != null)
                        {
                            AppUtils.loadImageUsingGlide(
                                    ProfileFragment.this.m_Context,
                                    result,
                                    null,
                                    null,
                                    true,
                                    null,
                                    imageView);

                            /*
                            Glide.with(Objects.requireNonNull(ProfileFragment.this.m_Context)).
                                    load(result).circleCrop().into(imageView);
                            */

                            if (result.toString().equals(imgUri) == false)
                            {
                                ProfileFragment.this.saveBtn.setEnabled(true);
                            }

                            imgUri = result.toString();
                        }
                    }
                }
        );
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        m_ViewModel.registerForEvents((IView)this);
        super.onCreate(savedInstanceState);
    }

    // Inflate fragment_profile.
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        return view;
    }

    // Handle profile editing.
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        this.m_EditTextComponentsTextWatcher = new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                compareCurrentDetailsToSavedUserProfileDetails();
            }

            @Override
            public void afterTextChanged(Editable s)
            {
            }
        };

        backBtn = view.findViewById(R.id.profile_back_button);
        backBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getParentFragmentManager().popBackStack();
            }
        });

        imageView = view.findViewById(R.id.profile_image_view);

        cameraBtn = view.findViewById(R.id.profile_camera_image_button);
        cameraBtn.setOnClickListener(new View.OnClickListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v)
            {
                if (ContextCompat.checkSelfPermission(ProfileFragment.this.m_Context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        /*
                        ActivityCompat.requestPermissions(requireActivity(),
                                new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
                        */
                    m_IsPermissionToReadExternalStorageHasBeenRequestedByCamera = true;
                    ProfileFragment.this.requestGalleryPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                }
                else
                {
                    // Request for camera permission.
                    // If permission will be granted, the method "onRequestPermissionsResult()" will open the camera
                    if (ContextCompat.checkSelfPermission(Objects.requireNonNull(ProfileFragment.this.m_Context), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                    {
                        /*
                        ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()),
                        new String[] { Manifest.permission.CAMERA }, 100);
                        */
                        ProfileFragment.this.requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
                    }
                    else
                    {
                        ProfileFragment.this.selectPhotoFromCamera();
                    }
                }
            }
        });

        galleryBtn = view.findViewById(R.id.profile_gallery_image_button);
        galleryBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Request for gallery permission.
                if (ContextCompat.checkSelfPermission(ProfileFragment.this.m_Context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                {
                    /*
                    ActivityCompat.requestPermissions(requireActivity(),
                            new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
                    */

                    ProfileFragment.this.requestGalleryPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                }
                else
                {
                    // Open gallery
                    pickFromGalleryActivityResultLauncher.launch("image/*");
                }
            }
        });

        resetImgBtn = view.findViewById(R.id.profile_reset_button);
        resetImgBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // initialize DB.image to null.
                imageView.setImageResource(R.drawable.dog_default_profile_rounded);

                if (!imgUri.toString().equals(AppUtils.getUriOfDrawable("dog_default_profile_rounded", ProfileFragment.this.m_Context).toString()))
                {
                    ProfileFragment.this.saveBtn.setEnabled(true);
                }

                imgUri = "";
            }
        });

        m_FullNameEt = view.findViewById(R.id.profile_full_name_edit_text);
        m_FullNameEt.addTextChangedListener(this.m_EditTextComponentsTextWatcher);

        m_AgeEt = view.findViewById(R.id.profile_age_edit_text);
        m_AgeEt.addTextChangedListener(this.m_EditTextComponentsTextWatcher);

        m_radioGroup = view.findViewById(R.id.profile_radio_group);
        m_radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                m_radioButton = AppUtils.getSelectedRadioButtonFromRadioGroup(m_radioGroup, view);

                // If RadioButton selected
                if(m_radioButton != null)
                {
                    /*
                    String dogGenderLabel = m_radioButton.getText().toString();

                    if (dogGenderLabel.equals("זכר"))
                    {
                        dogGenderLabel = "MALE";
                    }
                    else if (dogGenderLabel.equals("נקבה"))
                    {
                        dogGenderLabel = "FEMALE";
                    }

                    m_dogGender = eDogGender.valueOf(dogGenderLabel.toUpperCase());
                    */

                    m_dogGender = AppUtils.resolveDogGender(m_radioButton, ProfileFragment.this.m_Context);
                }

                ProfileFragment.this.compareCurrentDetailsToSavedUserProfileDetails();
            }
        });

        onRequestToLoadProfile();

        saveBtn = view.findViewById(R.id.profile_save_button);
        saveBtn.setEnabled(false);
        saveBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // If the details of the profile hasn't been changed - no action should be taken.
                if (saveBtn.isEnabled() == false)
                {
                    return;
                }

                String fullNameInput = m_FullNameEt.getText().toString();
                String ageInput = m_AgeEt.getText().toString();

                if(fullNameInput.equals("") || ageInput.equals(""))
                {
                    Toast.makeText(ProfileFragment.this.m_Context, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    m_radioButton = AppUtils.getSelectedRadioButtonFromRadioGroup(m_radioGroup, view);

                    // If RadioButton selected
                    if(m_radioButton != null)
                    {
                        m_dogGender = AppUtils.resolveDogGender(m_radioButton, ProfileFragment.this.m_Context);
                    }

                    getParentFragmentManager().popBackStack();

                    ProfileFragment.this.m_ViewModel.onRequestToUpdateProfile(ProfileFragment.this.m_Context, fullNameInput, ageInput, m_dogGender, imgUri);
                }
            }
        });

        this.m_IsLoadingProcessEnded = true;
    }

    @Override
    public void onDestroy()
    {
        m_ViewModel.unregisterForEvents((IView)this);
        super.onDestroy();
    }

    @Override
    public void onRequestToLoadProfile()
    {
        m_ViewModel.onRequestToLoadProfile();
    }

    @Override
    public void onSuccessToLoadProfile(UserProfile i_UserProfile)
    {
        this.m_CurrentUserProfile = i_UserProfile;
        Uri uriToLoad;

        if  (!this.m_CurrentUserProfile.getProfileImageUri().equals(""))
        {
            uriToLoad = Uri.parse(i_UserProfile.getProfileImageUri());
        }
        else
        {
            uriToLoad = AppUtils.getUriOfDrawable("dog_default_profile_rounded", this.m_Context);
            //  uriToLoad = Uri.parse("android.resource://" + this.m_Context.getPackageName() + "/drawable/dog_default_profile_rounded");
        }

        RequestListener<Drawable> glideListener = new RequestListener<Drawable>()
        {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource)
            {
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource)
            {
                m_FullNameEt.setText(i_UserProfile.getFullName());
                m_AgeEt.setText(String.valueOf(i_UserProfile.getAge()));

                if(i_UserProfile.getDogGender() == eDogGender.MALE)
                {
                    m_radioGroup.check(R.id.profile_radio_button_male);
                }
                else
                {
                    m_radioGroup.check(R.id.profile_radio_button_female);
                }

                return false;
            }
        };

        AppUtils.loadImageUsingGlide(
                ProfileFragment.this.m_Context,
                uriToLoad,
                null,
                null,
                true,
                glideListener,
                imageView);

        /*
        Glide.with(Objects.requireNonNull(ProfileFragment.this.m_Context)).
              load(uriToLoad).
              listener(glideListener).circleCrop().into(this.imageView);
        */
            // this.imageView.setImageURI(Uri.parse(this.m_CurrentUserProfile.getProfileImageUri()));
    }

    @Override
    public void onFailureToLoadProfile(Exception i_Reason)
    {
        Toast.makeText(m_Context, i_Reason.getMessage(), Toast.LENGTH_SHORT).show();
    }

    // Source - https://developer.android.com/training/camera/photobasics
    @RequiresApi(api = Build.VERSION_CODES.N)
    private File createImageFile() throws IOException
    {
        // Create an image file name
        @SuppressLint("SimpleDateFormat")
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Objects.requireNonNull(getActivity()).getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,/* prefix */
                ".jpg",  /* suffix */
                storageDir    /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void compareCurrentDetailsToSavedUserProfileDetails()
    {
        // NOTE: If this compare mechanism causes the app to be laggy, consider not create a temp user profile and then compare it with the current user profile,
        //       and compare the values one by one instead in the following way:
        //
        /*
        boolean areUserProfileDetailsStayedTheSame = true;

        areUserProfileDetailsStayedTheSame &= (m_FullNameEt.getText().equals(this.m_CurrentUserProfile.getFullName()) == true);
        areUserProfileDetailsStayedTheSame &= (m_AgeEt.getText().equals(String.valueOf(this.m_CurrentUserProfile.getAge())) == true);
        */

        try
        {
            if (this.m_IsLoadingProcessEnded == true)
            {
                UserProfile tempUserProfile = new UserProfile(m_FullNameEt.getText().toString(), Integer.valueOf(m_AgeEt.getText().toString()), this.m_dogGender, null);

                if (this.m_CurrentUserProfile.equals(tempUserProfile) == false)
                {
                    this.saveBtn.setEnabled(true);
                }
                else
                {
                    this.saveBtn.setEnabled(false);
                }
            }
        }
        catch (Exception err)
        {
            AppUtils.printDebugToLogcat("ProfileFragment.java", "compareCurrentDetailsToSavedUserProfileDetails()", err.toString());
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void selectPhotoFromCamera()
    {
        // Open camera
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (intent.resolveActivity(Objects.requireNonNull(getActivity()).getPackageManager()) != null)
        {
            // Create the File where the photo should go
            File photoFile = null;
            try
            {
                photoFile = createImageFile();
            }
            catch (IOException ignored) {} // Error occurred while creating the File

            // Continue only if the File was successfully created
            if (photoFile != null)
            {
                photoURI = FileProvider.getUriForFile(
                        Objects.requireNonNull(ProfileFragment.this.m_Context),
                        "com.example.android.fileprovider" + BuildConfig.APPLICATION_ID,
                        photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                takePictureActivityResultLauncher.launch(intent);
            }
        }
    }

    public void selectPhotoFromGallery()
    {
        pickFromGalleryActivityResultLauncher.launch("image/*");
    }
}