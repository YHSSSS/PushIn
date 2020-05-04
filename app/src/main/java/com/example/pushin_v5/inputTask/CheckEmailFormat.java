package com.example.pushin_v5.inputTask;
/***********************************************************************
 This file is used to check if the input text is an email address in
 right format.
 ***********************************************************************/

import android.text.TextUtils;

public class CheckEmailFormat {
    public boolean isEmail(String strEmail) {
        String strPattern = "^[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]@[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]$";
        if (TextUtils.isEmpty(strPattern)) {
            return false;
        } else {
            return strEmail.matches(strPattern);
        }
    }
}
