package com.example.buddies.common;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.time.MonthDay;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;

public class CreationDate
{
    private int creationYear;
    private int creationMonth;
    private int creationDay;

    public CreationDate(int i_CreationYear, int i_CreationMonth, int i_CreationDay)
    {
        this.creationYear = i_CreationYear;
        this.creationMonth = i_CreationMonth;
        this.creationDay = i_CreationDay;
    }

    // Getters
    public int getCreationYear() { return creationYear; }
    public int getCreationMonth() { return creationMonth; }
    public int getCreationDay() { return creationDay; }

    @NonNull
    @Override
    public String toString() { return creationDay + "." + creationMonth + "." + creationYear; }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static CreationDate now() {

        ZoneId zoneId = ZoneId.of("Israel");
        int year = (Year.now(zoneId)).getValue();
        int month = (YearMonth.now(zoneId)).getMonthValue();
        int day = (MonthDay.now(zoneId)).getDayOfMonth();

        return new CreationDate(year, month, day);
    }

    public static CreationDate parse(String i_StringOfDate) {

        int day;
        int month;
        int year;

        try {
            // Use escaping in order to split by dot (Source: https://stackoverflow.com/a/14833048/2196301)
            String[] dateParts = i_StringOfDate.split("\\.");

            if (dateParts.length == 3) {
                day   = Integer.parseInt(dateParts[0]);
                month = Integer.parseInt(dateParts[1]);
                year  = Integer.parseInt(dateParts[2]);

                return new CreationDate(year, month, day);
            } else {
                throw new Exception();
            }
        } catch (Exception error) {
            return null;
        }
    }
}