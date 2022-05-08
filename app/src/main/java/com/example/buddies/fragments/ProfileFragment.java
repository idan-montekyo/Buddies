package com.example.buddies.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.buddies.BuildConfig;
import com.example.buddies.R;
import com.example.buddies.common.AppUtils;
import com.example.buddies.enums.eDogGender;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;

public class ProfileFragment extends Fragment {

    public  static final String PROFILE_FRAGMENT_TAG = "profile_fragment";

    ActivityResultLauncher<Intent> takePictureActivityResultLauncher;
    ActivityResultLauncher<String> pickFromGalleryActivityResultLauncher;

    private String imgUri = "";
    private String currentPhotoPath;

    ImageButton cameraBtn, galleryBtn;
    Button resetImgBtn;
    ImageView imageView;

    // need to add current image.
    RadioGroup m_radioGroup;
    RadioButton m_radioButton;
    eDogGender m_dogGender = eDogGender.UNINITIALIZED;

    interface IOnSaveListener {
        void onSave();
    }

    IOnSaveListener onSaveListener;

    // Initialize both ActivityResultLaunchers.
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        Fragment homeFragment = getParentFragmentManager().findFragmentByTag(HomeFragment.HOME_FRAGMENT_TAG);
        try {
            onSaveListener = (IOnSaveListener) homeFragment;
        } catch (ClassCastException ex) {
            throw new ClassCastException("Fragment must implement IOnSaveListener interface.");
        }

        // Source - Capture image with camera - https://www.youtube.com/watch?v=RaOyw84625w
        // After taking a picture - load into ImageView, and save URI to imgUri.
        takePictureActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        // Check condition. In case camera opened and closed.
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Glide.with(Objects.requireNonNull(getContext())).
                                    load(currentPhotoPath).circleCrop().into(imageView);
                            imgUri = currentPhotoPath;
                        }
                    }
                }
        );

        // Source - Open file manager and pick photo (get Uri) - https://www.youtube.com/watch?v=cXyeozbLqq0
        // After picking from gallery - Load into ImageView, and save URI as String to imgUri.
        pickFromGalleryActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        if (result != null) {
                            Glide.with(Objects.requireNonNull(getContext())).
                                    load(result).circleCrop().into(imageView);
                            imgUri = result.toString();
                        }
                    }
                }
        );
    }

    // Inflate fragment_profile.
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        return view;
    }

    // Handle profile editing.
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton backBtn = view.findViewById(R.id.profile_back_button);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().popBackStack();
            }
        });

        imageView = view.findViewById(R.id.profile_image_view);

        // Request for camera permission.
        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()), Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()),
                    new String[] { Manifest.permission.CAMERA }, 100);
        }
        // Request for gallery permission.
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        }

        cameraBtn = view.findViewById(R.id.profile_camera_image_button);
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                // Open camera
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                // Ensure that there's a camera activity to handle the intent
                if (intent.resolveActivity(Objects.requireNonNull(getActivity()).getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ignored) {} // Error occurred while creating the File

                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        Uri photoURI = FileProvider.getUriForFile(Objects.requireNonNull(getContext()),
                                "com.example.android.fileprovider" + BuildConfig.APPLICATION_ID,
                                photoFile);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        takePictureActivityResultLauncher.launch(intent);
                    }
                }
            }
        });

        galleryBtn = view.findViewById(R.id.profile_gallery_image_button);
        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open gallery
                pickFromGalleryActivityResultLauncher.launch("image/*");
            }
        });

        resetImgBtn = view.findViewById(R.id.profile_reset_button);
        resetImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: delete image from DataBase and set dog_default_profile_rounded.png
                // initialize DB.image to null.
                imageView.setImageResource(R.drawable.dog_default_profile_rounded);
            }
        });

        EditText fullNameEt = view.findViewById(R.id.profile_full_name_input);
        EditText ageEt = view.findViewById(R.id.profile_age_input);
        m_radioGroup = view.findViewById(R.id.profile_radio_group);

        Button saveBtn = view.findViewById(R.id.profile_save_button);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String fullNameInput = fullNameEt.getText().toString();
                String tempAgeInput = ageEt.getText().toString();

                if(fullNameInput.equals("") || tempAgeInput.equals("")) {
                    Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                }
                // TODO: add option to change image & select gender. Also show image using Glide (circle).
                else {

                    int ageInput = Integer.parseInt(tempAgeInput);
                    m_radioButton = AppUtils.getSelectedRadioButtonFromRadioGroup(m_radioGroup, view);
                    if(m_radioButton != null) // If RadioButton selected
                        m_dogGender = eDogGender.valueOf(m_radioButton.getText().toString());

                    // TODO: save all info in DataBase.

                    getParentFragmentManager().popBackStack();
                    onSaveListener.onSave();
                }
            }
        });

    }

    // Source - https://developer.android.com/training/camera/photobasics
    @RequiresApi(api = Build.VERSION_CODES.N)
    private File createImageFile() throws IOException {
        // Create an image file name
        @SuppressLint("SimpleDateFormat")
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Objects.requireNonNull(getActivity()).getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
}
