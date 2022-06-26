package com.example.buddies.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.icu.text.SimpleDateFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.RequestListener;
import com.example.buddies.Model.Model;
import com.example.buddies.enums.eDogGender;
import com.example.buddies.service.MyFirebaseMessagingService;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.example.buddies.R;
import com.google.firebase.database.DataSnapshot;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class AppUtils
{
    private static Gson gson;
    private Model m_Model = Model.getInstance();
    private static final String addressComponent_PlusCode_Indicator = "plus_code";
    private static final String addressComponent_Establishment_Indicator = "establishment";
    private static final String addressComponent_Route_Indicator = "route";
    private static final String addressComponent_Locality_Indicator = "locality";
    private static final String addressComponent_Country_Indicator = "country";
    private static final String countryName = "Israel";
    private static final String invalidCountryNameErrorMessage = "The selected location is not located in Israel, please select another location.";

    private static final String unknownStreetDefaultValue = "Unknown Street";
    private static final String unknownCityDefaultValue = "Unknown City";
    private static final String unknownPlaceDefaultValue = "Unknown Place";
    private static final String unknownPlusCOdeDefaultValue = "Unknown PlusCode";
    private static final String longNameJsonKey = "long_name";

    private static final String maleGenderString = "male";
    private static final String femaleGenderString = "female";

    /**
     * This method is responsible to retrieve a string value by its name
     * @param i_StringName - The name of the string it's text should be returned
     * @param i_Context    - The context which called to this method
     * @return             - The string value which belongs to the given string name
     */
    public static String GetResourceStringValueByStringName(String i_StringName, Context i_Context)
    {
        Resources res = i_Context.getResources();
        String packageName = i_Context.getPackageName();

        int stringId = res.getIdentifier(i_StringName, "string", packageName);

        return i_Context.getString(stringId);
    }

    /**
     * This method is responsible to remove all the desired fragments from container, by their given fragments tags
     * (Source: https://stackoverflow.com/a/39569243/2196301)
     * @param i_FragmentManager - The FragmentManager which will be used in order to close the desired fragments
     * @param i_TagsToRemoveBy  - A collection of strings which represents the tags which indicates what fragments should be closed
     */
    public static void closeFragmentsByFragmentsTags(FragmentManager i_FragmentManager, String... i_TagsToRemoveBy)
    {
        List<Fragment> allFragments = i_FragmentManager.getFragments();
        List<Fragment> fragmentsToRemove = new ArrayList<Fragment>();
        List<String> tagsToRemove = Arrays.asList(i_TagsToRemoveBy);

        for (Fragment fragment : allFragments)
        {
            // If this fragment should be removed
            if (tagsToRemove.contains(fragment.getTag()) == true)
            {
                fragmentsToRemove.add(fragment);
            }
        }

        for (Fragment fragment : fragmentsToRemove)
        {
            i_FragmentManager.beginTransaction().remove(fragment).commit();
        }
    }

    /**
     * This method is responsible to retrieve a reverse-geolocation data of the given coordinates.
     * You can read the docs (Link: https://positionstack.com/documentation) which indicating how to receive
     * the reversed-geolocation data from the api.
     * @param i_Coordinates - The coordinates which their reverse-geolocation data should be retrieved.
     * @return              - A strings array which contains all the information about the given coordinates.
     *                        The order of the information details is:
     *                        1. Establishment
     *                        2. Street
     *                        3. City
     *                        4. Country
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String[] getStringValueFromJsonObject(Context i_Context, LatLng i_Coordinates)
    {
        String url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + i_Coordinates.latitude + "," + i_Coordinates.longitude + "&key=" + AppUtils.GetResourceStringValueByStringName("google_maps_key", i_Context);
        // String url = "http://api.positionstack.com/v1/reverse?access_key=d0ff480a5bda3210213193c176ac35c6&limit=1&query=" + i_Coordinates.latitude + "," + i_Coordinates.longitude;
        final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31";

        try
        {
            final HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestProperty("User-Agent", USER_AGENT);

            if (connection.getResponseCode() == 200)
            {
                InputStreamReader jsonStreamReader = new InputStreamReader(connection.getInputStream());
                final BufferedReader reader = new BufferedReader(jsonStreamReader);

                String line = null;
                final StringBuffer buffer = new StringBuffer(4096);
                while ((line = reader.readLine()) != null)
                {
                    buffer.append(line);
                }
                reader.close();

                final JSONObject jObj = new JSONObject(buffer.toString());

                // Read the data from the google maps response (Source: https://stackoverflow.com/questions/29439754/parsing-json-from-the-google-maps-distancematrix-api-in-android)
                JSONArray items = jObj.getJSONArray("results");
                JSONObject firstAddress = items.getJSONObject(0);
                JSONArray addressComponentsAsJsonArray = firstAddress.getJSONArray("address_components");
                JSONObject[] addressComponents = new JSONObject[addressComponentsAsJsonArray.length()];

                for (int i = 0; i < addressComponents.length; i++)
                {
                    addressComponents[i] = addressComponentsAsJsonArray.getJSONObject(i);
                }

                JSONObject currentObject = null;
                String currentObjectInformationType = null;

                JSONObject plusCodeObject      = null;
                JSONObject establishmentObject = null;
                JSONObject streetObject        = null;
                JSONObject cityObject          = null;
                JSONObject countryObject       = null;

                String plusCode      = null;
                String establishment = null;
                String street        = null;
                String city          = null;
                String country       = null;

                for (int i = 0; i < addressComponents.length; i++)
                {
                    currentObject = addressComponents[i];
                    currentObjectInformationType = currentObject.getJSONArray("types").getString(0);

                    switch (currentObjectInformationType)
                    {
                        case addressComponent_PlusCode_Indicator:
                            plusCodeObject = currentObject;
                            break;
                        case addressComponent_Establishment_Indicator:
                            establishmentObject = currentObject;
                            break;
                        case addressComponent_Route_Indicator:
                            streetObject = currentObject;
                            break;
                        case addressComponent_Locality_Indicator:
                            cityObject = currentObject;
                            break;
                        case addressComponent_Country_Indicator:
                            countryObject = currentObject;
                            break;
                        default:
                            break;
                    }
                }

                country = (countryObject == null) ? countryName : countryObject.getString(longNameJsonKey);

                if (country.equals(countryName) == false)
                {
                    throw new IllegalArgumentException(invalidCountryNameErrorMessage);
                }

                street        = (streetObject == null)        ? unknownStreetDefaultValue   : streetObject.getString(longNameJsonKey);
                city          = (cityObject == null)          ? unknownCityDefaultValue     : cityObject.getString(longNameJsonKey);
                establishment = (establishmentObject == null) ? unknownPlaceDefaultValue    : establishmentObject.getString(longNameJsonKey);
                plusCode      = (plusCodeObject == null)      ? unknownPlusCOdeDefaultValue : plusCodeObject.getString(longNameJsonKey);

                String[] locationDetails = new String[]{establishment, street, city, country};

                return locationDetails;
            }
        }
        catch (Exception e)
        {
            String err = e.toString();
        }

        return null;
    }

    /**
     * This method is responsible to print a message to logcat
     * @param i_CallerClass  - The class which called to this method
     * @param i_CallerMethod - The method which called to this method
     * @param i_Message      - The message to print to logcat
     */
    public static void printDebugToLogcat(String i_CallerClass, String i_CallerMethod, String i_Message)
    {
        SimpleDateFormat sdf = null;
        String currentTime = "";
        String debuggerKey = "#AppDebugger#";

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)
        {
            sdf = new SimpleDateFormat("[dd.MM.yyyy][HH:mm:ss]", Locale.getDefault());
            currentTime = sdf.format(new Date());
        }

        Log.d(debuggerKey, currentTime + "[" + i_CallerClass + "][" + i_CallerMethod + "] " + i_Message);
    }

    //

    /**
     * This method is responsible to retrieve a selected RadioButton from a given RadioGroup
     * @param i_OwnerRadioGroup - The RadioGroup which contians all the RadioButton components
     * @param i_OwnerView       - The view which owns the given RadioGroup
     * @return                  - The selected RadioButton. If nothing is selected, return null.
     */
    public static RadioButton getSelectedRadioButtonFromRadioGroup(RadioGroup i_OwnerRadioGroup, View i_OwnerView)
    {
        int radioId = i_OwnerRadioGroup.getCheckedRadioButtonId();

        RadioButton selectedRadioButton = (RadioButton) i_OwnerView.findViewById(radioId);

        return selectedRadioButton;
    }

    /**
     * This method is responsible to resolve a given RadioButton text to a suitable DogGender
     * @param i_RadioButton - The RadioButton which it's text should be translated to DogGender
     * @param i_Context     - The context which called to this method
     * @return              - The suitable eDogGender of the given RadioButton.
     *                        If the given RadioButton doesn't represent a gender, eDogGender.UNINITIALIZED is returned.
     */
    public static eDogGender resolveDogGender(RadioButton i_RadioButton, Context i_Context)
    {
        eDogGender m_dogGender = eDogGender.UNINITIALIZED;
        String dogGenderLabel = i_RadioButton.getText().toString();

        if (dogGenderLabel.equals(AppUtils.GetResourceStringValueByStringName("male", i_Context)) == true)
        {
            dogGenderLabel = "MALE";
        }
        else if (dogGenderLabel.equals(AppUtils.GetResourceStringValueByStringName("female", i_Context)) == true)
        {
            dogGenderLabel = "FEMALE";
        }

        m_dogGender = eDogGender.valueOf(dogGenderLabel.toUpperCase());

        return m_dogGender;
    }

    /**
     * loadImageUsingGlide()
     *     Purpose: Load a given image to imageview, and apply the requested settings.
     * @param i_Context - The context which will be used in order to load the image.
     * @param i_AddressOfPhoto - A Uri object which holds the path of the image to load.
     * @param i_Width - An Integer object. If there's no need to resize, this value should be null.
     * @param i_Height - An Integer object. If there's no need to resize, this value should be null.
     * @param isCircleCropNeeded - A boolean which indicates if there's a need to circleCrop or not
     * @param listener - A listener which handles the load process of the image. This parameter is optional and can be null
     * @param i_TargetImageView - The ImageView which will finally contains the requested image.
     */
    @SuppressLint("CheckResult")
    public static void loadImageUsingGlide(Context i_Context, Uri i_AddressOfPhoto, Integer i_Width, Integer i_Height, boolean isCircleCropNeeded, RequestListener<Drawable> listener, ImageView i_TargetImageView)
    {
        RequestBuilder glideLoader = Glide.with(i_Context).load(i_AddressOfPhoto);

        // any placeholder to load at start
        glideLoader.placeholder(R.drawable.dog_default_profile_rounded);

        // any Photo in case of error
        glideLoader.error(R.drawable.dog_default_profile_rounded);

        // resizing
        if (i_Width != null && i_Height != null)
        {
            glideLoader.override(i_Width, i_Height);
        }

        if (isCircleCropNeeded == true)
        {
            glideLoader.circleCrop();
        }

        // Attach listener to the load process (Source: https://stackoverflow.com/a/53314724/2196301)
        if (listener != null)
        {
            glideLoader.listener(listener);
        }

        // imageview object
        glideLoader.into(i_TargetImageView);
    }


    /**
     * This method is responsible to retrieve a uri for the given drawable name.
     * @param i_DrawableName - The name of the drawable which it's uri should be returned.
     * @param i_Context      - The context which called to this method
     * @return               - The uri of the given drawable
     */
    public static Uri getUriOfDrawable(String i_DrawableName, Context i_Context)
    {
        return Uri.parse("android.resource://" + i_Context.getPackageName() + "/drawable/" + i_DrawableName);
    }

    /**
     * Check if there is an available network connection (Source: https://stackoverflow.com/a/4239019/2196301)
     * @param i_Context - The context which called to this method
     * @return          - True if a network connection is available, otherwise false.
     */
    public static boolean isNetworkAvailable(Context i_Context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) i_Context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        // If there is a connection available
        if ((activeNetworkInfo != null) && (activeNetworkInfo.isConnected()))
        {
            return true;
        }

        return false;
    }

    /**
     * This method is responsible to retrieve a Gson parser object
     * @return - The Gson parser object
     */
    public static Gson getGsonParser()
    {
        if(null == gson)
        {
            GsonBuilder builder = new GsonBuilder();
            gson = builder.create();
        }

        return gson;
    }

    public static void initializeDataMessageAndSendToServer(Context context, Post post, Comment comment,
                                                     UserProfile postCreatorProfile,
                                                     UserProfile commentCreatorProfile) {

        JSONObject rootObject = new JSONObject();
        try {

            String postAsJsonString = getGsonParser().toJson(post);

            rootObject.put("to", "/topics/" + post.getPostID());
            JSONObject object = new JSONObject();
            object.put("message", comment.getCommentContent());
            object.put(MyFirebaseMessagingService.POST_AS_JSON_STRING_KEY, postAsJsonString);
            object.put(MyFirebaseMessagingService.POST_CREATOR_USERNAME_KEY, postCreatorProfile.getFullName());
            object.put(MyFirebaseMessagingService.COMMENT_CREATOR_USERNAME_KEY, commentCreatorProfile.getFullName());
            object.put(MyFirebaseMessagingService.COMMENT_CONTENT_KEY, comment.getCommentContent());
            object.put(MyFirebaseMessagingService.COMMENT_CREATOR_IMAGE_KEY, commentCreatorProfile.getProfileImageUri());
            rootObject.put("data", object);

            String url = "https://fcm.googleapis.com/fcm/send";

            RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) { } // Irrelevant
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) { } // Irrelevant.
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", "key=" + MyFirebaseMessagingService.API_TOKEN_KEY);
                    return headers;
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    return rootObject.toString().getBytes();
                }
            };

            queue.add(request);
            queue.start();

            // By now, the request has been sent to the server.

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}