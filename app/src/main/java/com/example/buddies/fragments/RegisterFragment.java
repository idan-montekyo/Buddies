package com.example.buddies.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.buddies.R;
import com.example.buddies.ViewModel.ViewModel;
import com.example.buddies.common.AppUtils;
import com.example.buddies.enums.eDogGender;
import com.example.buddies.interfaces.MVVM.IView;
import com.example.buddies.interfaces.SignupEvent.ISignupResponsesEventHandler;

public class RegisterFragment extends Fragment implements IView,
                                                          ISignupResponsesEventHandler
{
    public final String LOGIN_FRAGMENT_TAG = "login_fragment";
    private ViewModel m_ViewModel = ViewModel.getInstance();

    RadioGroup m_radioGroup;
    RadioButton m_radioButton;

    public interface IOnRegisteredListener {
        void onRegistered();
    }

    public IOnRegisteredListener onRegisteredListener;

    // Initialize onRegisteredListener.
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        this.m_ViewModel.registerForEvents((IView) this);

        Fragment loginFragment = getParentFragmentManager().findFragmentByTag(LOGIN_FRAGMENT_TAG);
        try {
            onRegisteredListener = (IOnRegisteredListener)loginFragment;
        } catch (ClassCastException ex) {
            throw new ClassCastException("Fragment must implement IOnRegisteredListener interface.");
        }
    }

    // Inflate fragment_register.
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_register, container, false);
        return view;
    }

    // Confirm all fields are filled, Confirm username not taken, Add new user to DataBase.
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText usernameEt = view.findViewById(R.id.register_username_input);
        EditText passwordEt = view.findViewById(R.id.register_password_input);
        EditText fullNameEt = view.findViewById(R.id.register_full_name_input);
        EditText ageEt = view.findViewById(R.id.register_age_input);
        this.m_radioGroup = view.findViewById(R.id.register_radio_group);

        ImageButton backBtn = view.findViewById(R.id.register_back_button);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().popBackStack();
            }
        });

        Button registerBtn = view.findViewById(R.id.register_register_button);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String usernameInput = usernameEt.getText().toString();
                String passwordInput = passwordEt.getText().toString();
                String fullNameInput = fullNameEt.getText().toString();
                String tempAgeInput = ageEt.getText().toString();
                eDogGender dogGenderInput = eDogGender.UNINITIALIZED;

                m_radioButton = AppUtils.getSelectedRadioButtonFromRadioGroup(m_radioGroup, view);

                String dogGenderLabel = m_radioButton.getText().toString();

                // If RadioButton selected
                if(m_radioButton != null)
                {
                    if (dogGenderLabel.equals("זכר"))
                    {
                        dogGenderLabel = "MALE";
                    }
                    else if (dogGenderLabel.equals("נקבה"))
                    {
                        dogGenderLabel = "FEMALE";
                    }

                    dogGenderInput = eDogGender.valueOf(dogGenderLabel.toUpperCase());
                }

                /*

                // TODO: The code below should be changed / moved from here to viewmodel / model / other locations.

                if(usernameInput.equals("") || passwordInput.equals("")
                        || fullNameInput.equals("") || tempAgeInput.equals("")) {

                    Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                }
                // TODO: confirm 'username' field is not already taken.
                // else if(usernameInput *ALREADY IN DB*) {
                //     Toast.makeText(requireContext(), "Username already taken", Toast.LENGTH_LONG).show();
                // }
                else {

                    int ageInput = Integer.parseInt(tempAgeInput);
                    // TODO: add username, password, full-name, age info to DataBase.

                    getParentFragmentManager().popBackStack();
                    onRegisteredListener.onRegistered();
                }
                */

                AppUtils.printDebugToLogcat("RegisterFragment", "onRequestToSignup", "calling onRequestToSignup()");
                ViewModel.getInstance().onRequestToSignup(requireContext(), usernameInput, passwordInput, fullNameInput, tempAgeInput, dogGenderInput);
                AppUtils.printDebugToLogcat("RegisterFragment", "onRequestToSignup", "returned from onRequestToSignup()");
            }
        });
    }

    @Override
    public void onSuccessToSignup()
    {
        Toast.makeText(requireContext(), "The user has been successfully registered !", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFailureToSignup(Exception i_Reason)
    {
        AppUtils.printDebugToLogcat("RegisterFragment", "onFailureToSignup", i_Reason.toString());
        Toast.makeText(requireContext(), i_Reason.getMessage(), Toast.LENGTH_LONG).show();
    }
}