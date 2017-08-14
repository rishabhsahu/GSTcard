package in.gstcard.gstcard;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;


public class PrefManager {
    private SharedPreferences pref;

    private String defaultNewsURL = "http://www.gstindia.com/about/";

    // Shared preferences file name
    private static final String PREF_NAME = "GSTcard-welcome";

    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";
    private static final String GSTIN_KEY = "gstin_key";
    private static final String COMPANY_NAME_KEY = "company_name_key";
    private static final String PHONE_NO_KEY = "phone_number_key";
    private static final String USER_NAME_KEY = "user_name_key";
    private static final String NEWS_URL_KEY = "news_url";
    private static final String EXTRA_TAB_TEXT_KEY = "tab_text";

    public PrefManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setLogin(String GSTIN, String companyName) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        Map<String, Object> updateChild = new HashMap<>();
        updateChild.put("/registered/" + GSTIN + "/company_name", companyName);
        updateChild.put("/registered/" + GSTIN + "/gstin", GSTIN);
        ref.updateChildren(updateChild);

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
        String GSTIN = getGstin();
        if (GSTIN != null) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
            Map<String, Object> updateChild = new HashMap<>();
            updateChild.put("/registered/" + GSTIN + "/userName", userName);
            updateChild.put("/registered/" + GSTIN + "/phoneNumber", phoneNo);
            ref.updateChildren(updateChild);
        }
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(PHONE_NO_KEY, phoneNo);
        editor.putString(USER_NAME_KEY, userName);
        editor.apply();
    }

    public void setExtraTabText(String extraTabText) {
        pref.edit().putString(EXTRA_TAB_TEXT_KEY, extraTabText).apply();
    }

    public String getExtraTabText() {
        return pref.getString(EXTRA_TAB_TEXT_KEY, "GST News");
    }

    public void setNewsUrl(String url) {
        pref.edit().putString(NEWS_URL_KEY, url).apply();
    }

    public String getNewsUrl() {
        return pref.getString(NEWS_URL_KEY, defaultNewsURL);
    }

    public String getUserName()  {
        return pref.getString(USER_NAME_KEY, null);
    }

    public String getPhoneNo() {
        return pref.getString(PHONE_NO_KEY, null);
    }
}
