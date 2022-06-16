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

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.RequestListener;
import com.example.buddies.Model.Model;
import com.example.buddies.enums.eDogGender;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
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
import java.util.List;
import java.util.Locale;

import com.example.buddies.R;
import com.google.firebase.database.DataSnapshot;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class AppUtils
{
    Model m_Model = Model.getInstance();
    private static Gson gson;

    public static String GetResourceStringValueByStringName(String i_StringName, Context i_Context)
    {
        Resources res = i_Context.getResources();
        String packageName = i_Context.getPackageName();

        int stringId = res.getIdentifier(i_StringName, "string", packageName);

        return i_Context.getString(stringId);
    }

    public double convertExponentialNumberToDecimalNumber()
    {
        String str = "6.151536E-8";
        BigDecimal bd = new BigDecimal(str);
        bd = bd.round(new MathContext(2, RoundingMode.HALF_UP));
        return bd.doubleValue();
    }

    /*
    Remove all the desired fragments from container, by their given fragments tags
    (Source: https://stackoverflow.com/a/39569243/2196301)
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
     * You can read the docs (Link: https://positionstack.com/documentation) which indicating how to receive
     * the reversed-geolocation data from the api.
     * @param i_Coordinates
     * @return
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
                        case "plus_code":
                            plusCodeObject = currentObject;
                            break;
                        case "establishment":
                            establishmentObject = currentObject;
                            break;
                        case "route":
                            streetObject = currentObject;
                            break;
                        case "locality":
                            cityObject = currentObject;
                            break;
                        case "country":
                            countryObject = currentObject;
                            break;
                        default:
                            break;
                    }
                }

                country = (countryObject == null) ? "Israel" : countryObject.getString("long_name");

                if (country.equals("Israel") == false)
                {
                    throw new IllegalArgumentException("The selected location is not located in Israel, please select another location.");
                }

                street        = (streetObject == null)        ? "Unknown Street"   : streetObject.getString("long_name");
                city          = (cityObject == null)          ? "Unknown City"     : cityObject.getString("long_name");
                establishment = (establishmentObject == null) ? "Unknown Place"    : establishmentObject.getString("long_name");
                plusCode      = (plusCodeObject == null)      ? "Unknown PlusCode" : plusCodeObject.getString("long_name");

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

    // Get selected RadioButton. If nothing is selected, return null.
    public static RadioButton getSelectedRadioButtonFromRadioGroup(RadioGroup i_OwnerRadioGroup, View i_OwnerView)
    {
        // TODO: what if no button selected?
        int radioId = i_OwnerRadioGroup.getCheckedRadioButtonId();

        RadioButton selectedRadioButton = (RadioButton) i_OwnerView.findViewById(radioId);

        return selectedRadioButton;
    }

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

        /*
                Glide.with(i_Context)
                .load(i_AddressOfPhoto)                     // Photo url
                .placeholder(R.drawable.dog_default_profile_rounded) // any placeholder to load at start
                .error(R.drawable.dog_default_profile_rounded)         // any Photo in case of error
                // .override(i_Width, i_Height)                // resizing
                .into(i_TargetImageView);                   // imageview object
        */
    }

    public static Uri getUriOfMipmap(String i_MipmapName, Context i_Context)
    {
        return Uri.parse("android.resource://" + i_Context.getPackageName() + "/mipmap/" + i_MipmapName);
    }

    public static Uri getUriOfDrawable(String i_DrawableName, Context i_Context)
    {
        return Uri.parse("android.resource://" + i_Context.getPackageName() + "/drawable/" + i_DrawableName);
    }

    /**
     * Check if there is an available network connection (Source: https://stackoverflow.com/a/4239019/2196301)
     * @param i_Context
     * @return
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

    public static Gson getGsonParser()
    {
        if(null == gson)
        {
            GsonBuilder builder = new GsonBuilder();
            gson = builder.create();
        }

        return gson;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static Post ConvertDataSnapshotToPost(DataSnapshot post) {

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

        return newPost;
    }
}