package in.gstcard.gstcard;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.glxn.qrgen.android.QRCode;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by kshivang on 14/08/17.
 *
 */

public class GstBookActivity extends AppCompatActivity{

    private static String GST_NUMBER_KEY = "gst_number";
    private static String COMPANY_NAME_KEY = "company_name";
    private static String USER_NAME_KEY = "user_name";
    private static String PHONE_NUMBER_KEY = "phone_number";

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private DatabaseReference ref;
    private PrefManager localDatabase;

    private ValueEventListener listener = new ValueEventListener() {
        @Override
        public void onDataChange(final DataSnapshot dataSnapshot) {
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            final HashMap child = (HashMap)dataSnapshot.getValue();
            if (child != null) {
                final List<Object> values = new ArrayList<Object>(child.values());

                RecyclerView.Adapter<GstBookHolder> adapter = new RecyclerView.Adapter<GstBookHolder>() {
                    @Override
                    public GstBookHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                        View itemView = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.card_other, parent, false);
                        return new GstBookHolder(itemView);
                    }

                    @Override
                    public void onBindViewHolder(GstBookHolder holder, int position) {
                        final HashMap<String, String> gstCardMap = (HashMap<String, String>) values.get(position);
                        String gsTin = gstCardMap.get(GST_NUMBER_KEY);
                        String companyName = gstCardMap.get(COMPANY_NAME_KEY);
                        String userName = gstCardMap.get(USER_NAME_KEY);
                        String phoneNo = gstCardMap.get(PHONE_NUMBER_KEY);

                        View root = holder.itemView;
                        View leftCard = root.findViewById(R.id.left_card);

                        TextView tvCompany = root.findViewById(R.id.tv_company);
                        TextView tvGstin = root.findViewById(R.id.tv_gstin);
                        TextView tvUserName = root.findViewById(R.id.tv_user_name);
                        TextView tvPhone = root.findViewById(R.id.tv_phone_number);

                        ImageView ivQrCode = root.findViewById(R.id.qr_code);

                        Typeface avenirFont = Typeface.createFromAsset(getAssets(), "fonts/avenir.ttc");
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
                            tvPhone.setTextColor(ContextCompat.getColor(GstBookActivity.this, android.R.color.black));
                        } else {
                            tvPhone.setTextColor(ContextCompat.getColor(GstBookActivity.this, R.color.blue));
                        }
                        final JSONObject gstCard = new JSONObject();
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

                    @Override
                    public int getItemCount() {
                        return values.size();
                    }
                };
                recyclerView.setAdapter(adapter);
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        localDatabase = new PrefManager(this);
        setContentView(R.layout.activity_gst_book);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("GST Book");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        progressBar = findViewById(R.id.progress_bar);

        ref = FirebaseDatabase.getInstance().getReference()
                .child("/gst_company_book/" + localDatabase.getGstin());
        ref.addValueEventListener(listener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ref.removeEventListener(listener);
    }

    private class GstBookHolder extends RecyclerView.ViewHolder {
        GstBookHolder(View itemView) {
            super(itemView);
        }
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
}
