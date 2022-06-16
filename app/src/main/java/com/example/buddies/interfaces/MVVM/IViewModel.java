package com.example.buddies.interfaces.MVVM;

public interface IViewModel
{
    void registerForEvents(IView i_ViewToRegister);
    void registerForEventsAtIndex0(IView i_ViewToRegisterAtIndex0);
    void unregisterForEvents(IView i_ViewToUnregister);
}
