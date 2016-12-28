package com.gellert.sunshine;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by gellert on 2016. 08. 18..
 */
public class LocationEditTextPreference extends EditTextPreference {
    private static final int DEFAULT_MIN_LOCATION_LENGTH = 3;
    private int mMinLength;


    public LocationEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,R.styleable.LocationEditTextPreference,0,0);
        try{
            mMinLength = a.getInt(R.styleable.LocationEditTextPreference_minLength,
                    DEFAULT_MIN_LOCATION_LENGTH);
        } finally {
            a.recycle();
        }
    }


    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);

        EditText et = getEditText();
        et.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        Dialog d = getDialog();
                        if(d instanceof AlertDialog) {
                            AlertDialog alertDialog = (AlertDialog) d;
                            Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                            if(s.length() < mMinLength){
                                positiveButton.setEnabled(false);
                            } else{
                                positiveButton.setEnabled(true);
                            }


                        }
                    }
                }
        );
    }
}
