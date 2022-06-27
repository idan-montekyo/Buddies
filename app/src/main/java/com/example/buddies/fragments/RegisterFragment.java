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

public class RegisterFragment extends Fragment
                              implements IView,
                                         ISignupResponsesEventHandler {

    public static final String REGISTER_FRAGMENT_TAG = "register_fragment";
    private final ViewModel m_ViewModel = ViewModel.getInstance();

    RadioGroup m_radioGroup;
    RadioButton m_radioButton;

    public interface IOnRegisteredListener {
        void onRegistered();
    }

    public IOnRegisteredListener onRegisteredListener;
    private Context m_Context = null;

    // Initialize onRegisteredListener.
    @Override
    public void onAttach(@NonNull Context context) {
        this.m_Context = context;
        super.onAttach(context);

        Fragment loginFragment = getParentFragmentManager().findFragmentByTag(LoginFragment.LOGIN_FRAGMENT_TAG);
        try {
            onRegisteredListener = (IOnRegisteredListener) loginFragment;
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        this.m_ViewModel.registerForEvents((IView) this);
        super.onCreate(savedInstanceState);
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

                // If RadioButton selected
                if (m_radioButton != null) {
                    dogGenderInput = AppUtils.resolveDogGender(m_radioButton, RegisterFragment.this.m_Context);
                }

                AppUtils.printDebugToLogcat("RegisterFragment", "onRequestToSignup", "calling onRequestToSignup()");
                m_ViewModel.onRequestToSignup(RegisterFragment.this.m_Context, usernameInput, passwordInput, fullNameInput, tempAgeInput, dogGenderInput);
                AppUtils.printDebugToLogcat("RegisterFragment", "onRequestToSignup", "returned from onRequestToSignup()");
            }
        });
    }

    @Override
    public void onDestroy() {
        this.m_ViewModel.unregisterForEvents((IView) this);
        super.onDestroy();
    }

    @Override
    public void onSuccessToSignup() {
        getParentFragmentManager().popBackStack();
        onRegisteredListener.onRegistered();
    }

    @Override
    public void onFailureToSignup(Exception i_Reason) {
        AppUtils.printDebugToLogcat("RegisterFragment", "onFailureToSignup", i_Reason.toString());
        Toast.makeText(this.m_Context, i_Reason.getMessage(), Toast.LENGTH_LONG).show();
    }
}