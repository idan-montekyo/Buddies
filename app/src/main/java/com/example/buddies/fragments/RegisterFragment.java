package com.example.buddies.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.transition.TransitionInflater;

import com.example.buddies.R;

public class RegisterFragment extends Fragment {

    public final String LOGIN_FRAGMENT_TAG = "login_fragment";

    public interface IOnRegisteredListener {
        void onRegistered();
    }

    public IOnRegisteredListener onRegisteredListener;

    // Initialize onRegisteredListener.
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

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
            }
        });
    }

}
