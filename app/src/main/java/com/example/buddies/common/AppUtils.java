package com.example.buddies.common;

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

public class AppUtils
{
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String[] getStringValueFromJsonObject(LatLng i_Coordinates)
    {
        String url = "http://api.positionstack.com/v1/reverse?access_key=d0ff480a5bda3210213193c176ac35c6&limit=1&query=" + i_Coordinates.latitude + "," + i_Coordinates.longitude;
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
                JSONObject items = jObj.getJSONArray("data").getJSONObject(0);

                String placeCountry = items.getString("country");

                String[] locationDetails = null;

                if (placeCountry.equals("Palestine") == true)
                {
                    placeCountry = "Israel";
                }

                if (placeCountry.equals("Israel") == true)
                {
                    String placeName    = items.getString("name");
                    String placeStreet  = items.getString("street");
                    String placeCity    = items.getString("county"); // Equals to 'City', not exact.

                    locationDetails = new String[]{ placeName, placeStreet, placeCity, placeCountry };

                    for (int i = 0; i < locationDetails.length; i++)
                    {
                        if (locationDetails[i].equals("null") == true) {
                            locationDetails[i] = "Unknown";
                        }
                    }
                }
                else {
                    throw new IllegalArgumentException("The selected location is not located in Israel, please select another location.");
                }

                return locationDetails;
            }
        }
        catch (Exception e) {
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