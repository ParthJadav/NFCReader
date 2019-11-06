package com.parthjadav.nfcreaderexample;

import android.content.Context;
import android.content.SharedPreferences;

import com.parthjadav.nfcreaderexample.utils.VariableBag;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by arati on cosmetic_img3/3/17.
 */

public class PreferenceManager {

    public static PreferenceManager preferenceManager;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";

    public static PreferenceManager getInstance() {
        return preferenceManager;
    }

    public PreferenceManager(Context context) {
        preferenceManager = this;
        mSharedPreferences = context.getSharedPreferences(VariableBag.PREF_NAME, Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
    }

    public void clearPreferences() {
        mEditor.clear();
        mEditor.commit();
    }

    public void removePref(Context context, String keyToRemove) {
        mSharedPreferences = context.getSharedPreferences(VariableBag.PREF_NAME, Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
        mEditor.remove(keyToRemove);
        mEditor.commit();
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        mEditor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        mEditor.commit();
    }

    public boolean isFirstTimeLaunch() {
        return mSharedPreferences.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }

    /*set preference for registration*/

    public String getRegisteredUserId() {
        String strUserId = mSharedPreferences.getString(VariableBag.USER_ID, "");
        return strUserId;
    }

    public void setRegisteredUserId(String strUserId) {
        mEditor.putString(VariableBag.USER_ID, strUserId).commit();
    }

    public String getFCMToken() {
        String strUserId = mSharedPreferences.getString(VariableBag.FCM_TOKEN, "");
        return strUserId;
    }

    public void setFCMToken(String fcmToken) {
        mEditor.putString(VariableBag.FCM_TOKEN, fcmToken).commit();
    }

    public void setLoginSession() {
        mEditor.putBoolean(VariableBag.LOGIN, true);
    }

    public boolean getLoginSession() {
        boolean login = mSharedPreferences.getBoolean(VariableBag.LOGIN, false);
        return login;
    }

    public void setKeyValueString(String key, String value) {
        mEditor.putString(key, value).commit();
    }

    public void setKeyValueInt(String key, int value) {
        mEditor.putInt(key, value).commit();
    }

    public boolean setKeyValueBoolean(String key, boolean value) {
        mEditor.putBoolean(key, value).commit();
        return value;
    }

    public String getKeyValueString(String key) {
        String getValue = mSharedPreferences.getString(key, "");
        return getValue;
    }

    public int getKeyValueInt(String key) {
        int getValue = mSharedPreferences.getInt(key, 0);
        return getValue;
    }

    public boolean getKeyValueBoolean(String key) {
        boolean getValue = mSharedPreferences.getBoolean(key, false);
        return getValue;
    }

    public void setJSONPref(String key, String json) {
        mSharedPreferences.edit().putString(key, json).commit();
    }

    public JSONObject getJSONObject(String key) {
        try {
            return new JSONObject(mSharedPreferences.getString(key, ""));
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public JSONArray getJSONArray(String key) {
        try {
            return new JSONArray(mSharedPreferences.getString(key, ""));
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getJSONKeyString(String objKey, String stringKey) {
        JSONObject obj = getJSONObject(objKey);
        if (objKey != null) {
            try {
                return obj.getString(stringKey);
            } catch (JSONException e) {
                e.printStackTrace();
                return "";
            }
        } else {
            return "";
        }
    }

    /*public void setObject(String key, Object object) {
        Gson gson = new Gson();
        String json = gson.toJson(object);
        mSharedPreferences.edit().putString(key, json).commit();
    }

    public <GenericClass> GenericClass getObject(String key, Class<GenericClass> object) {
        try {
            Gson gson = new Gson();
            String json = mSharedPreferences.getString(key,"");
            return gson.fromJson(json, object);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }*/
}


