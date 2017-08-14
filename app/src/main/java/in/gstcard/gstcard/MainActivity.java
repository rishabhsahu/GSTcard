package in.gstcard.gstcard;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import net.glxn.qrgen.android.QRCode;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements PermissionListener{
    private static String GST_NUMBER_KEY = "gst_number";
    private static String COMPANY_NAME_KEY = "company_name";
    private static String USER_NAME_KEY = "user_name";
    private static String PHONE_NUMBER_KEY = "phone_number";

    private PrefManager localDatabase;
    private TextView tvUserName;
    private TextView tvPhone;
    private TextView tvCompany;
    private TextView tvGstin;
    private ImageView ivQrCode;
    private View leftCard;
    private View cardView;

    private String phoneNo = null, userName = null;
    private JSONObject gstCard;
    private PermissionUtils permissionUtils;
    private DatabaseReference reference;
    private Typeface avenirFont;
    private ValueEventListener listener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot != null) {
                String newsURL = dataSnapshot.child("news_url").getValue(String.class);
                if (newsURL != null) {
                    localDatabase.setNewsUrl(newsURL);
                }
                String extraTabText = dataSnapshot.child("extra_tab_text").getValue(String.class);
                if (extraTabText != null) {
                    localDatabase.setExtraTabText(extraTabText);
                    ((TextView) findViewById(R.id.news_text))
                            .setText(extraTabText);
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        avenirFont = Typeface.createFromAsset(getAssets(), "fonts/avenir.ttc");

        localDatabase = new PrefManager(this);

        reference = FirebaseDatabase.getInstance().getReference()
                .child("settings");

        permissionUtils = PermissionUtils.Companion.newInstance(this, this);

        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("GST Card");
        }

        cardView = findViewById(R.id.card);
        leftCard = findViewById(R.id.left_card);

        tvCompany = findViewById(R.id.tv_company);
        tvGstin = findViewById(R.id.tv_gstin);
        tvUserName = findViewById(R.id.tv_user_name);
        tvPhone = findViewById(R.id.tv_phone_number);

        ivQrCode = findViewById(R.id.qr_code);


        tvCompany.setTypeface(avenirFont, Typeface.NORMAL);
        tvGstin.setTypeface(avenirFont, Typeface.NORMAL);
        tvPhone.setTypeface(avenirFont, Typeface.NORMAL);
        tvUserName.setTypeface(avenirFont, Typeface.NORMAL);

        ((TextView)findViewById(R.id.news_text))
                .setText(localDatabase.getExtraTabText());

        tvPhone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addDetailDialog();
                }
        });
        tvUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvPhone.callOnClick();
            }
        });

        onClickViews(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                permissionUtils.getWritePermission("App need permission to write to external storage");
            }
        }, R.id.share_icon, R.id.share_next_icon, R.id.share_text);

        onClickViews(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanQRCode();
            }
        }, R.id.scan_icon, R.id.scan_text, R.id.scan_next_icon);

        onClickViews(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Under Development", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, GstBookActivity.class));
            }
        }, R.id.view_icon, R.id.view_text, R.id.view_next_icon);

        onClickViews(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openNews();
            }
        }, R.id.news_icon, R.id.news_text);

        onClickViews(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callHelpline();
            }
        }, R.id.call_icon);

        updateCardData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        reference.addValueEventListener(listener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(listener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionUtils.onRequestPermissionsRequest(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                try {
                    JSONObject gstCard = new JSONObject(result.getContents());
                    if (gstCard.optString(GST_NUMBER_KEY, null) == null
                            || gstCard.optString(COMPANY_NAME_KEY, null) == null) {
                        throw new Exception();
                    } else {
                        onScanResult(gstCard);
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    void onScanResult(final JSONObject gstCard) {
        final String gsTin = gstCard.optString(GST_NUMBER_KEY);
        String companyName = gstCard.optString(COMPANY_NAME_KEY);
        String userName = gstCard.optString(USER_NAME_KEY, null);
        String phoneNo = gstCard.optString(PHONE_NUMBER_KEY, null);

        View root = getLayoutInflater().inflate(R.layout.dialog_scan, null);

        View leftCard = root.findViewById(R.id.left_card);

        TextView tvCompany = root.findViewById(R.id.tv_company);
        TextView tvGstin = root.findViewById(R.id.tv_gstin);
        TextView tvUserName = root.findViewById(R.id.tv_user_name);
        TextView tvPhone = root.findViewById(R.id.tv_phone_number);

        ImageView ivQrCode = root.findViewById(R.id.qr_code);

        tvCompany.setTypeface(avenirFont, Typeface.NORMAL);
        tvGstin.setTypeface(avenirFont, Typeface.NORMAL);
        tvPhone.setTypeface(avenirFont, Typeface.NORMAL);
        tvUserName.setTypeface(avenirFont, Typeface.NORMAL);

        tvCompany.setText(companyName);
        tvGstin.setText(gsTin);



        if (userName != null) {
            tvUserName.setText(userName);
        }
        if (phoneNo != null) {
            tvPhone.setText(phoneNo);
            tvPhone.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        } else {
            tvPhone.setTextColor(ContextCompat.getColor(this, R.color.blue));
        }

        ivQrCode.setImageBitmap(QRCode.from(gstCard.toString()).bitmap());
        leftCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bigQRCode(gstCard);
            }
        });

        scanedCardToDatabase(gsTin, companyName);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("QR code scan result").setView(root).create();
        dialog.show();
    }

    void scanedCardToDatabase(final String gsTin, String companyName) {
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        Map<String, Object> updateChild = new HashMap<>();
        final Map<String, Object> gstCardMap = new HashMap<>();
        gstCardMap.put(GST_NUMBER_KEY, gsTin);
        gstCardMap.put(COMPANY_NAME_KEY, companyName);
        gstCardMap.put(USER_NAME_KEY, userName);
        gstCardMap.put(PHONE_NUMBER_KEY, phoneNo);

        updateChild.put("/gst_company_book/" + localDatabase.getGstin() + "/" + gsTin, gstCardMap);
        updateChild.put("/gst_company_book/" + gsTin + "/" + localDatabase.getGstin(), getUserMap());

        ref.updateChildren(updateChild);

        ref.child("registered").child(gsTin).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Map<String, Object> updateChild = new HashMap<>();
                    updateChild.put("/registered/" + gsTin, gstCardMap);
                    ref.updateChildren(updateChild);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    void updateCardData() {
        String gsTin = localDatabase.getGstin();
        String companyName = localDatabase.getCompanyName();
        String userName = localDatabase.getUserName();
        String phoneNo = localDatabase.getPhoneNo();

        tvCompany.setText(companyName);
        tvGstin.setText(gsTin);

        if (userName != null) {
            tvUserName.setText(userName);
        }
        if (phoneNo != null) {
            tvPhone.setText(phoneNo);
            tvPhone.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        } else {
            tvPhone.setText(R.string.add_details);
            tvPhone.setTextColor(ContextCompat.getColor(this, R.color.blue));
        }

        gstCard = new JSONObject();
        try {
            gstCard.put(GST_NUMBER_KEY, gsTin);
            gstCard.put(COMPANY_NAME_KEY, companyName);
            if(userName != null) {
                gstCard.put(USER_NAME_KEY, userName);
            }
            if (phoneNo != null) {
                gstCard.put(PHONE_NUMBER_KEY, phoneNo);
            }
            ivQrCode.setImageBitmap(QRCode.from(gstCard.toString()).bitmap());
            leftCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bigQRCode(gstCard);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void addDetailDialog() {
        View root = getLayoutInflater().inflate(R.layout.dialog_detail, null);
        final EditText etUserName = root.findViewById(R.id.et_user_name);
        final EditText etPhoneNo = root.findViewById(R.id.et_phone_no);
        Button btSubmit = root.findViewById(R.id.btn_submit);
        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setView(root)
                .create();
        phoneNo = localDatabase.getPhoneNo();
        userName = localDatabase.getUserName();
        if (phoneNo != null) {
            etPhoneNo.setText(phoneNo);
        }
        if (userName != null) {
            etUserName.setText(userName);
        }

        btSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etUserName != null && etPhoneNo != null
                        && (!TextUtils.isEmpty(etUserName.getText())
                        || !TextUtils.isEmpty(etPhoneNo.getText()))) {
                    if (!TextUtils.isEmpty(etPhoneNo.getText())) {
                        phoneNo = etPhoneNo.getText().toString();
                    }
                    if (!TextUtils.isEmpty(etUserName.getText())){
                        userName = etUserName.getText().toString();
                    }
                    localDatabase.setDetails(phoneNo, userName);
                    updateCardData();
                    alertDialog.dismiss();
                } else {
                    Toast.makeText(MainActivity.this, "Add details to continue!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        alertDialog.show();
    }

    void shareCard() {
        Bitmap bitmap = getBitmapFromView(cardView);
        String path = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+"/ShareCard.png";
        OutputStream out;
        File file=new File(path);
        try {
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        path=file.getPath();

        Uri bmpUri = Uri.parse("file://"+path);

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                "GSTIN : " + gstCard.optString(GST_NUMBER_KEY) + "\n" +
                "Company Name : " + gstCard.optString(COMPANY_NAME_KEY) + "\n" +
                "Name : " + gstCard.optString(USER_NAME_KEY) + "\n" +
                "Phone Number : " + gstCard.optString(PHONE_NUMBER_KEY) + "\n" +
                "View this GST card at " + "https://www.gstcard.in");

        shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
        shareIntent.setType("*/*");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Share with"));
    }

    void openNews() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(localDatabase.getNewsUrl()));
        startActivity(intent);
    }

    void scanQRCode() {
        IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                integrator.setPrompt("Scan GST QR code");
                integrator.setBarcodeImageEnabled(false);
                integrator.setOrientationLocked(false);
                integrator.initiateScan();
    }

    void callHelpline() {
        Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + "18001039271"));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    void bigQRCode(JSONObject gstCard) {
        View root = getLayoutInflater().inflate(R.layout.big_qr_code, null);
        ((ImageView)root.findViewById(R.id.big_qr))
                .setImageBitmap(QRCode.from(gstCard.toString()).bitmap());

        new AlertDialog.Builder(this)
                .setView(root)
                .setTitle("Scan QR code for GST details")
                .create().show();
    }

    Map<String, Object> getUserMap() {
        String gsTin = localDatabase.getGstin();
        String companyName = localDatabase.getCompanyName();
        String userName = localDatabase.getUserName();
        String phoneNo = localDatabase.getPhoneNo();
        Map<String, Object> myCard = new HashMap<>();
        myCard.put(GST_NUMBER_KEY, gsTin);
        myCard.put(COMPANY_NAME_KEY, companyName);
        myCard.put(USER_NAME_KEY, userName);
        myCard.put(PHONE_NUMBER_KEY, phoneNo);
        return myCard;
    }


    void onClickViews(View.OnClickListener listener, int... layouts) {
        for (int layout: layouts) {
            findViewById(layout).setOnClickListener(listener);
        }
    }

    public static Bitmap getBitmapFromView(View view) {
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
                Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable != null) {
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        } else {
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        }
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }

    @Override
    public boolean onPermissionGrant(int permissionCode) {
        if (permissionCode == PermissionUtils.Companion.getWRITE_EXTERNAL_STORAGE_PEMISSION_CODE()) {
            shareCard();
            return true;
        }
        return false;
    }

    @Override
    public boolean onPermissionDeny(int permissionCode) {
        return false;
    }
}
