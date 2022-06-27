package com.example.buddies.common;

import androidx.annotation.Nullable;

import com.example.buddies.enums.eDogGender;

public class UserProfile {

    private String m_FullName;
    private int m_Age;
    private eDogGender m_DogGender;
    private String m_ProfileImageUri = null;

    public UserProfile() {}

    public UserProfile(String i_FullName, int i_Age, eDogGender i_DogGender, String i_ProfileImageUri) {

        this.m_FullName = i_FullName;
        this.m_Age = i_Age;
        this.m_DogGender = i_DogGender;
        this.m_ProfileImageUri = i_ProfileImageUri;
    }

    public String getFullName() { return this.m_FullName; }
    public int getAge() { return this.m_Age; }
    public eDogGender getDogGender() { return this.m_DogGender; }
    public String getProfileImageUri() { return this.m_ProfileImageUri; }

    public void setFullName(String i_NewFullName) { this.m_FullName = i_NewFullName; }
    public void setAge(int i_NewAge) { this.m_Age = i_NewAge; }
    public void setDogGender(eDogGender i_NewDogGender) { this.m_DogGender = i_NewDogGender; }
    public void setProfileImageUri(String i_NewProfileImageUri) { this.m_ProfileImageUri = i_NewProfileImageUri; }

    @Override
    public boolean equals(@Nullable Object obj) {
        boolean isEqual = true;

        if ((obj instanceof UserProfile) == false) {
            return false;
        }

        isEqual &= ((UserProfile)obj).m_Age == this.m_Age;
        isEqual &= ((UserProfile)obj).m_DogGender == this.m_DogGender;
        isEqual &= ((UserProfile)obj).m_FullName.equals(this.m_FullName);

        return isEqual;
    }
}