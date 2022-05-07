package com.example.buddies.common;

public class PostCreationDate {

    int postCreationYear;
    int postCreationMonth;
    int postCreationDay;

    public PostCreationDate(int i_PostCreationYear, int i_PostCreationMonth, int i_PostCreationDay) {

        this.postCreationYear = i_PostCreationYear;
        this.postCreationMonth = i_PostCreationMonth;
        this.postCreationDay = i_PostCreationDay;
    }

    // Getters
    public int getPostCreationYear() { return postCreationYear; }
    public int getPostCreationMonth() { return postCreationMonth; }
    public int getPostCreationDay() { return postCreationDay; }
}
