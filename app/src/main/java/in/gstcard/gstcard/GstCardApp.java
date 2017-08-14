package in.gstcard.gstcard;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by kshivang on 15/08/17.
 */

public class GstCardApp extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
