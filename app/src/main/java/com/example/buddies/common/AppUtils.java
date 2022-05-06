package com.example.buddies.common;

import android.content.Context;
import android.content.res.Resources;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.example.buddies.R;

public class AppUtils
{
    public static String GetResourceStringValueByStringName(String i_StringName, Context i_Context)
    {
        Resources res = i_Context.getResources();
        String packageName = i_Context.getPackageName();

        int stringId = res.getIdentifier(i_StringName, "string", packageName);

        return i_Context.getString(stringId);
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
}