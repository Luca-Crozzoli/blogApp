package com.example.pictureblog.Helpers;

import android.content.Context;
import android.widget.Toast;

public class ToastShort {

    private String message;
    private Context context;

    public ToastShort() {
    }

    public ToastShort(String message, Context context) {
        this.message = message;
        this.context = context;
    }


    public void showMessage() {
        Toast.makeText( context, message, Toast.LENGTH_LONG ).show();
    }
}
