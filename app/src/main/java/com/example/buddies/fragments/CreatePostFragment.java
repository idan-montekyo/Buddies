package com.example.buddies.fragments;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.Context;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

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

import java.time.MonthDay;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;

public class CreatePostFragment extends Fragment implements IView,
                                                            ILocationSelect_EventHandler,
                                                            OnMapReadyCallback,
                                                            TimePickerDialog.OnTimeSetListener,
                                                            IPostCreationResponseEventHandler
{

    public static final String CREATE_POST_FRAGMENT_TAG = "create_post_fragment";

    ViewModel m_ViewModel = ViewModel.getInstance();

    String[] m_LocationDetails;

    LatLng m_SelectedLocation;
    Handler m_MainActivityHandlerFromRemoteThreads = new Handler(Looper.getMainLooper());

    MapView m_mapView;

    TextView m_cityTv;
    TextView m_streetTv;
    TextView m_timeTv;

    GoogleMap m_GoogleMap;
    Marker m_CurrMarker;

    interface IOnUploadListener {
        void onUpload();
    }

    public IOnUploadListener onUploadListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        Fragment homeFragment = getParentFragmentManager().findFragmentByTag(HomeFragment.HOME_FRAGMENT_TAG);
        try {
            onUploadListener = (IOnUploadListener) homeFragment;
        } catch (ClassCastException ex) {
            throw new ClassCastException("Fragment must implement IOnUploadListener interface.");
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton backBtn = view.findViewById(R.id.create_post_back_button);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().popBackStack();
            }
        });

        m_mapView = (MapView) view.findViewById(R.id.create_post_map_view);
        m_mapView.onCreate(savedInstanceState);
        m_mapView.onResume();

        Button pickLocationBtn = view.findViewById(R.id.create_post_pick_location_button);
        pickLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If a location has not been already selected
                if (m_CurrMarker == null) {
                    m_mapView.setVisibility(View.GONE);
                }

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

        Button pickTimeBtn = (Button) view.findViewById(R.id.create_post_pick_time_button);
        pickTimeBtn.setOnClickListener(new View.OnClickListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v)
            {
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
        m_streetTv = view.findViewById(R.id.create_post_street_output);
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
                String timeInput = m_timeTv.getText().toString();
                String contentInput = contentEt.getText().toString();

                if(cityInput.equals("") || streetInput.equals("") ||
                        timeInput.equals("") || contentInput.equals("")) {
                    Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else {

                    // TODO: save post to DataBase and notify to refresh adapter.

                    ZoneId zoneId = ZoneId.of("Israel");
                    int year = (Year.now(zoneId)).getValue();
                    int month = (YearMonth.now(zoneId)).getMonthValue();
                    int day = (MonthDay.now(zoneId)).getDayOfMonth();

                    Post newPost = new Post(userUID, cityInput, streetInput, timeInput,
                                            m_SelectedLocation, contentInput, year, month, day);
                    m_ViewModel.onRequestToCreatePost(requireContext(), newPost);
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
    public void onLocationSelected(LatLng i_SelectedLocation)
    {
        m_SelectedLocation = i_SelectedLocation;

        new Thread(new Runnable() {

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {

                // TODO: sometimes crashes! with exception:
                //  " java.lang.IllegalStateException: Fragment CreatePostFragment{ce75c49}
                //  (63531ab2-d50d-41b3-a78e-b38cc032b083) not attached to a context. "
                m_LocationDetails = AppUtils.getStringValueFromJsonObject(requireContext(), i_SelectedLocation);

                // Update the Activity's TextView
                m_MainActivityHandlerFromRemoteThreads.post(new Runnable() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {

                        assert m_LocationDetails != null;
                        m_cityTv.setText(m_LocationDetails[2]);
                        m_streetTv.setText(m_LocationDetails[1]);

                        m_mapView.setVisibility(View.VISIBLE);
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

        if (m_CurrMarker == null)
        {
            m_CurrMarker = m_GoogleMap.addMarker(new MarkerOptions().position(m_SelectedLocation)
                    .title(m_LocationDetails[0]).snippet(m_SelectedLocation.toString()));
        }
        else
        {
            m_CurrMarker.setPosition(m_SelectedLocation);
        }

        // Center the map according to the chosen coordinates (Source: https://stackoverflow.com/a/16342378/2196301)
        m_GoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(m_SelectedLocation, 14f));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute)
    {
        if (minute < 10) {
            m_timeTv.setText(String.valueOf(hourOfDay) + ":0" + String.valueOf(minute));
        } else {
            m_timeTv.setText(String.valueOf(hourOfDay) + ":" + String.valueOf(minute));
        }
    }

    @Override
    public void onSuccessToCreatePost() {
        getParentFragmentManager().popBackStack();
        onUploadListener.onUpload();
    }

    @Override
    public void onFailureToCreatePost(Exception i_Reason) {
        AppUtils.printDebugToLogcat("CreatePostFragment", "onFailureToCreatePost", i_Reason.toString());
        Toast.makeText(requireContext(), "Failed - " + i_Reason.getMessage(), Toast.LENGTH_LONG).show();
    }
}