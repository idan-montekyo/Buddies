package com.example.buddies.common;

import androidx.annotation.NonNull;

public class MyCreationDate {

    int CreationYear;
    int CreationMonth;
    int CreationDay;

    public MyCreationDate(int i_CreationYear, int i_CreationMonth, int i_CreationDay) {

        this.CreationYear = i_CreationYear;
        this.CreationMonth = i_CreationMonth;
        this.CreationDay = i_CreationDay;
    }

    // Getters
    public int getCreationYear() { return CreationYear; }
    public int getCreationMonth() { return CreationMonth; }
    public int getCreationDay() { return CreationDay; }

    @NonNull
    @Override
    public String toString() {
        return "PostCreationDate{" +
                "postCreationYear=" + CreationYear +
                ", postCreationMonth=" + CreationMonth +
                ", postCreationDay=" + CreationDay +
                '}';
    }
}
