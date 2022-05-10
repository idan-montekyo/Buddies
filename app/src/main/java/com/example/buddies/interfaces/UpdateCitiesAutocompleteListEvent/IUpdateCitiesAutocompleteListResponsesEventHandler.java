package com.example.buddies.interfaces.UpdateCitiesAutocompleteListEvent;

import java.util.ArrayList;

public interface IUpdateCitiesAutocompleteListResponsesEventHandler
{
    void onSuccessToUpdateListOfCities(ArrayList<String> i_UpdatedListOfCities);
    void onFailureToUpdateListOfCities(Exception i_Reason);
}