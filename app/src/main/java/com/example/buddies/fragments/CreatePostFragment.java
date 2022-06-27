package com.example.buddies.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.buddies.Model.Model;
import com.example.buddies.R;
import com.example.buddies.ViewModel.ViewModel;
import com.example.buddies.common.AppUtils;
import com.example.buddies.common.CreationDate;
import com.example.buddies.common.Post;
import com.example.buddies.interfaces.LocationSelectionEvent.ILocationSelect_EventHandler;
import com.example.buddies.interfaces.MVVM.IView;
import com.example.buddies.interfaces.PostCreationEvent.IPostCreationResponseEventHandler;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AddressComponent;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class CreatePostFragment extends Fragment implements IView,
                                                            ILocationSelect_EventHandler,
                                                            OnMapReadyCallback,
                                                            TimePickerDialog.OnTimeSetListener,
                                                            IPostCreationResponseEventHandler
{
    public static final String CREATE_POST_FRAGMENT_TAG = "create_post_fragment";

    ViewModel m_ViewModel = ViewModel.getInstance();

    private final String invalidCityNameErrorMessage = "Illegal choose! Choose in your city only.";
    private final String invalidLocationErrorMessage = "Cannot selected this location, place select other.";
    private final String interfaceNotImplementedErrorMessage = "Fragment must implement IOnUploadListener interface.";
    private final String notAllFieldsAreFullErrorMEssage = "Please fill in all fields";

    private final String israelCountryCode = "IL";
    private static final String unknownStreetDefaultValue = "Unknown Street";

    String[] m_LocationDetails;

    LatLng m_SelectedLocation;
    Handler m_MainActivityHandlerFromRemoteThreads = new Handler(Looper.getMainLooper());

    MapView m_mapView;

    TextView m_cityTv;
    EditText m_streetTv;
    TextView m_dateTv;
    TextView m_timeTv;

    GoogleMap m_GoogleMap;
    Marker m_CurrMarker;
    Context m_Context = null;

    interface IOnUploadListener {
        void onUpload();
    }

    private ActivityResultLauncher<Intent> autocompleteLocationLauncher;

    public IOnUploadListener onUploadListener;

    @Override
    public void onAttach(@NonNull Context context) {
        this.m_Context = context;
        super.onAttach(context);

        Fragment homeFragment = getParentFragmentManager().findFragmentByTag(HomeFragment.HOME_FRAGMENT_TAG);
        try {
            onUploadListener = (IOnUploadListener) homeFragment;

            autocompleteLocationLauncher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            if (result.getResultCode() == Activity.RESULT_OK) {
                                Intent data = result.getData();

                                Place place = Autocomplete.getPlaceFromIntent(data);

                                // If there is information available for the selected street
                                if (place.getAddressComponents() != null) {
                                    List<AddressComponent> components = place.getAddressComponents().asList();
                                    String streetName = components.get(0).getName();
                                    String cityName = components.get(1).getName();
                                    AppUtils.printDebugToLogcat("CreatePostFragment", "onActivityResult()", "Address components are: " + place.getAddressComponents().toString());

                                    // If the user selected some street but not in the city that he selected earlier - show error message
                                    if (cityName.equals(CreatePostFragment.this.m_LocationDetails[2]) == false) {
                                        Toast.makeText(CreatePostFragment.this.m_Context, CreatePostFragment.this.invalidCityNameErrorMessage, Toast.LENGTH_LONG).show();
                                    } else {
                                        CreatePostFragment.this.m_streetTv.setText(streetName);
                                        CreatePostFragment.this.m_streetTv.setEnabled(false);
                                    }
                                } else {
                                    Toast.makeText(CreatePostFragment.this.m_Context, CreatePostFragment.this.invalidLocationErrorMessage, Toast.LENGTH_LONG).show();
                                }

                                AppUtils.printDebugToLogcat("CreatePostFragment", "onActivityResult()", place.toString());

                            }
                        }
                    });
        } catch (ClassCastException ex) {
            throw new ClassCastException(this.interfaceNotImplementedErrorMessage);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        this.m_ViewModel.registerForEvents((IView) this);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_post, container, false);
        return view;
    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton backBtn = view.findViewById(R.id.create_post_back_image_button);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { getParentFragmentManager().popBackStack(); }
        });

        m_mapView = (MapView) view.findViewById(R.id.create_post_map_view);
        m_mapView.onCreate(savedInstanceState);
        m_mapView.onResume();

        Button pickLocationBtn = view.findViewById(R.id.create_post_pick_location_button);
        pickLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // FragmentsCenter.m_Fragment_AddNewSong = new Fragment_AddNewSong();
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();

                // Start the transaction process
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setCustomAnimations(
                        R.anim.slide_in,  // enter
                        R.anim.fade_out,  // exit
                        R.anim.fade_in,   // popEnter
                        R.anim.slide_out); // popExit

                // Move to this fragment in the view
                transaction.hide(getParentFragmentManager().findFragmentByTag(CREATE_POST_FRAGMENT_TAG));
                transaction.add(R.id.root_main_activity, new SelectLocationFragment(), SelectLocationFragment.SELECT_LOCATION_FRAGMENT_TAG);

                // Insert the current fragment to the default BackStack in order to remove the fragment
                // from the view when the back button is pressed from inside the fragment.
                transaction.addToBackStack(null);

                transaction.commit();
            }
        });

        Button pickDateBtn = view.findViewById(R.id.create_post_pick_date_button);
        pickDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreationDate currentDate = CreationDate.now();

                DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // Set the month value to be the correct one.
                        monthOfYear++;
                        String selectedDate = dayOfMonth + "." + monthOfYear + "." + year;
                        CreatePostFragment.this.m_dateTv.setText(selectedDate);
                    }
                };

                DatePickerDialog datePickerDialog = new DatePickerDialog(CreatePostFragment.this.m_Context, mDateSetListener, currentDate.getCreationYear(), currentDate.getCreationMonth() - 1, currentDate.getCreationDay());
                datePickerDialog.show();
            }
        });

        Button pickTimeBtn = (Button) view.findViewById(R.id.create_post_pick_time_button);
        pickTimeBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                Calendar c = Calendar.getInstance();
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(), android.R.style.Theme_Holo_Light_Dialog_NoActionBar, CreatePostFragment.this, hour, minute, true);
                timePickerDialog.setTitle(R.string.pick_time);
                timePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                timePickerDialog.show();
            }
        });



        m_cityTv = view.findViewById(R.id.create_post_city_output);
        m_streetTv = (EditText) view.findViewById(R.id.create_post_street_output);
        m_streetTv.setFocusable(false);
        m_streetTv.setEnabled(false);

        m_streetTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If necessary - initialize the Places class
                if (Places.isInitialized() == false) {
                    Places.initialize(CreatePostFragment.this.m_Context, AppUtils.GetResourceStringValueByStringName("google_maps_key", CreatePostFragment.this.m_Context), Locale.US);
                }

                // Set the fields to specify which types of place data to
                // return after the user has made a selection.
                List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS_COMPONENTS);

                // Start the autocomplete intent.
                Autocomplete.IntentBuilder builder = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields);

                // Suggest to the user results which are only in Israel
                builder.setCountry(CreatePostFragment.this.israelCountryCode);

                // Suggest to the user addresses only (Source: https://developers.google.com/maps/documentation/places/android-sdk/reference/com/google/android/libraries/places/api/model/TypeFilter#enum-values)
                builder.setTypeFilter(TypeFilter.ADDRESS);

                Intent intent = builder.build(CreatePostFragment.this.m_Context);
                autocompleteLocationLauncher.launch(intent);
            }
        });

        m_dateTv = (TextView) view.findViewById(R.id.create_post_date_output);

        m_timeTv = (TextView) view.findViewById(R.id.create_post_time_output);
        EditText contentEt = view.findViewById(R.id.create_post_content_input);

        Button uploadBtn = view.findViewById(R.id.create_post_upload_button);
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                String userUID = Model.getInstance().getCurrentUserUID();
                String cityInput = m_cityTv.getText().toString();
                String streetInput = m_streetTv.getText().toString();
                String dateInput = m_dateTv.getText().toString();
                String timeInput = m_timeTv.getText().toString();
                String contentInput = contentEt.getText().toString();

                if (cityInput.equals("") || streetInput.equals("") || dateInput.equals("") || timeInput.equals("") || contentInput.equals("")) {
                    Toast.makeText(CreatePostFragment.this.m_Context, CreatePostFragment.this.notAllFieldsAreFullErrorMEssage, Toast.LENGTH_SHORT).show();
                } else {
                    // Save the new post to FireBase
                    m_ViewModel.onRequestToCreatePost(CreatePostFragment.this.m_Context, userUID, cityInput, streetInput, dateInput, timeInput, m_SelectedLocation, contentInput);
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        this.m_ViewModel.unregisterForEvents((IView) this);
        super.onDestroy();
    }

    @Override
    public void onLocationSelected(LatLng i_SelectedLocation) {
        m_SelectedLocation = i_SelectedLocation;

        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                m_LocationDetails = AppUtils.getStringValueFromJsonObject(CreatePostFragment.this.m_Context, i_SelectedLocation);

                // Update the Activity's TextView
                m_MainActivityHandlerFromRemoteThreads.post(new Runnable() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {
                        assert m_LocationDetails != null;

                        m_cityTv.setText(m_LocationDetails[2]);

                        if (m_LocationDetails[1].equals(unknownStreetDefaultValue)) {
                            m_streetTv.setText("");
                            m_streetTv.setEnabled(true);
                        } else {
                            m_streetTv.setText(m_LocationDetails[1]);
                            m_streetTv.setEnabled(false);
                        }

                        m_mapView.getMapAsync(CreatePostFragment.this);
                    }
                });
            }
        }).start();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        m_GoogleMap = googleMap;

        // If we want to disable moving the map, uncomment this. (Source: https://stackoverflow.com/a/28452115/2196301)
        // m_Map.getUiSettings().setAllGesturesEnabled(false);

        m_GoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if (m_CurrMarker == null) {
            m_CurrMarker = m_GoogleMap.addMarker(new MarkerOptions().position(m_SelectedLocation)
                    .title(m_LocationDetails[0]).snippet(m_SelectedLocation.toString()));
        } else {
            m_CurrMarker.setPosition(m_SelectedLocation);
        }

        // Center the map according to the chosen coordinates (Source: https://stackoverflow.com/a/16342378/2196301)
        m_GoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(m_SelectedLocation, 14f));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if (minute < 10) {
            m_timeTv.setText(String.valueOf(hourOfDay) + ":0" + String.valueOf(minute));
        } else {
            m_timeTv.setText(String.valueOf(hourOfDay) + ":" + String.valueOf(minute));
        }
    }

    @Override
    public void onSuccessToCreatePost(Post i_Post) {
        getParentFragmentManager().popBackStack();
        onUploadListener.onUpload();
    }

    @Override
    public void onFailureToCreatePost(Exception i_Reason) {
        AppUtils.printDebugToLogcat("CreatePostFragment", "onFailureToCreatePost", i_Reason.toString());
        Toast.makeText(requireContext(), i_Reason.getMessage(), Toast.LENGTH_LONG).show();
    }
}