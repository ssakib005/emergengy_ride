package naru.crover.com.app.Activites;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import naru.crover.com.app.Activites.Drivers.DriverMapActivity;
import naru.crover.com.app.Activites.Riders.RiderMapActivity;
import naru.crover.com.app.Activites.Riders.RiderProfileActivity;
import naru.crover.com.app.Adapter.HistoryAdapter;
import naru.crover.com.app.Models.History;
import naru.crover.com.app.R;

public class HistoryActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference reference;
    private FirebaseUser user;
    private RecyclerView recyclerView;
    private List<History> histories = new ArrayList<>();
    private List<History> historyList = new ArrayList<>();
    private List<Object> mas;
    private String riderOrDriver = "";
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        if (getIntent().getExtras()!= null){
            riderOrDriver = getIntent().getExtras().getString("riderOrDriver");
        }

        toolbar = findViewById(R.id.toolbarID);
        toolbar.setTitleTextColor(0xFFFFFFFF);
        toolbar.setTitle("History");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (riderOrDriver.equals("driver")){
                    startActivity(new Intent(HistoryActivity.this, DriverMapActivity.class));
                }else {
                    startActivity(new Intent(HistoryActivity.this, RiderMapActivity.class));
                }

            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.recyclerID);
        mas = new ArrayList<>();



        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        getHistory();

        if (historyList != null){
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(new HistoryAdapter(this, historyList));
        }

    }

    private void getHistory(){
        if (user != null){
            if (riderOrDriver.equals("driver")){
                reference = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child("History").child(user.getUid());
            }else {
                reference = FirebaseDatabase.getInstance().getReference().child("Users").child("Rider").child("History").child(user.getUid());
            }

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()){
                        for (DataSnapshot history: dataSnapshot.getChildren()){
                            getDataFromKey(history.getKey());
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void getDataFromKey(String key) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("History").child(key);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    History history = new History();
                    history.setCost(map.get("cost").toString());
                    history.setRiderId(map.get("riderId").toString());
                    history.setDestination(map.get("destination").toString());
                    history.setDriverRating(map.get("driverRating").toString());
                    history.setDriverId(map.get("driverId").toString());
                    historyList.add(history);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
