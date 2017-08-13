package in.gstcard.gstcard;

import android.content.Context;
import android.content.SharedPreferences;


public class PrefManager {
    private SharedPreferences pref;

    // Shared preferences file name
    private static final String PREF_NAME = "GSTcard-welcome";

    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";
    private static final String GSTIN_KEY = "gstin_key";
    private static final String COMPANY_NAME_KEY = "company_name_key";
    private static final String PHONE_NO_KEY = "phone_number_key";
    private static final String USER_NAME_KEY = "user_name_key";

    public PrefManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setLogin(String GSTIN, String companyName) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(GSTIN_KEY, GSTIN);
        editor.putString(COMPANY_NAME_KEY, companyName);
        editor.apply();
    }

    public String getGstin() {
        return pref.getString(GSTIN_KEY, null);
    }

    public String getCompanyName() {
        return pref.getString(COMPANY_NAME_KEY, null);
    }

    public void setDetails(String phoneNo, String userName) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(PHONE_NO_KEY, phoneNo);
        editor.putString(USER_NAME_KEY, userName);
        editor.apply();
    }

    public String getUserName()  {
        return pref.getString(USER_NAME_KEY, null);
    }

    public String getPhoneNo() {
        return pref.getString(PHONE_NO_KEY, null);
    }
}
