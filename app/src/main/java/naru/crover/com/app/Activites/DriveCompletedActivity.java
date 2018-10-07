package naru.crover.com.app.Activites;



import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import naru.crover.com.app.Activites.Drivers.DriverMapActivity;
import naru.crover.com.app.R;

public class DriveCompletedActivity extends AppCompatActivity{

    private TextView textView;
    private Button btn;
    private FirebaseAuth auth;
    private String cost;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive_completed);

        auth = FirebaseAuth.getInstance();
        String user = auth.getUid();

        if (getIntent().getExtras() != null) {
            cost = getIntent().getExtras().getString("cost");
        }
        textView = findViewById(R.id.totalCost);
        btn = findViewById(R.id.btnDone);

        textView.setText(cost + " /-");
        
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatabaseReference driveCompleteRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Rider").child("Ride");
                HashMap dMap = new HashMap();
                dMap.put("Completed", "completed");
                driveCompleteRef.updateChildren(dMap);
                startActivity(new Intent(DriveCompletedActivity.this, DriverMapActivity.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

    }
}
