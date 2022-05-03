package com.example.buddies.interfaces.MVVM;

public interface IModel
{
    void registerForEvents(IViewModel i_ViewModelToRegister);
    void unregisterForEvents();
}
