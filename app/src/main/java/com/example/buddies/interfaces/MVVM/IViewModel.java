package com.example.buddies.interfaces.MVVM;

public interface IViewModel
{
    void registerForEvents(IView i_ViewToRegister);
    void unregisterForEvents(IView i_ViewToUnregister);
}
