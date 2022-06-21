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

    public CreationDate(int i_CreationYear, int i_CreationMonth, int i_CreationDay) {

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
    public String toString() {
        return "PostCreationDate{" +
                "postCreationYear=" + creationYear +
                ", postCreationMonth=" + creationMonth +
                ", postCreationDay=" + creationDay +
                '}';
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static CreationDate now()
    {
        ZoneId zoneId = ZoneId.of("Israel");
        int year = (Year.now(zoneId)).getValue();
        int month = (YearMonth.now(zoneId)).getMonthValue();
        int day = (MonthDay.now(zoneId)).getDayOfMonth();

        return new CreationDate(year, month, day);
    }
}
