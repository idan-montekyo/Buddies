package com.example.buddies.common;
import com.example.buddies.enums.eDogGender;

public class UserProfile
{
    String m_FullName;
    int m_Age;
    eDogGender m_DogGender;

    public UserProfile(String i_FullName, int i_Age, eDogGender i_DogGender)
    {
        this.m_FullName = i_FullName;
        this.m_Age = i_Age;
        this.m_DogGender = i_DogGender;
    }

    public String getFullName() { return this.m_FullName; }
    public int getAge() { return this.m_Age; }
    public eDogGender getDogGender() { return this.m_DogGender; }

    public void setFullName(String i_NewFullName) { this.m_FullName = i_NewFullName; }
    public void setAge(int i_NewAge) { this.m_Age = i_NewAge; }
    public void setDogGender(eDogGender i_NewDogGender) { this.m_DogGender = i_NewDogGender; }
}