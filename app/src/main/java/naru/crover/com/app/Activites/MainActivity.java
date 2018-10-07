package naru.crover.com.app.Activites;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.Arrays;
import java.util.List;

import naru.crover.com.app.Activites.Drivers.DriverMapActivity;
import naru.crover.com.app.Activites.Riders.RiderMapActivity;
import naru.crover.com.app.R;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout layout;
    private TextView textView;
    private Animation animation;
    private FirebaseAuth mAuth;
    private ProgressBar myProgressbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );
        setContentView(R.layout.activity_main);

        layout = findViewById(R.id.logoActivity);
        textView = findViewById(R.id.tvLogo);
        layout.setVisibility(View.GONE);
        textView.setVisibility(View.GONE);
        animation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        mAuth = FirebaseAuth.getInstance();
        myProgressbar = findViewById(R.id.progressBar);
        myProgressbar.setVisibility(View.GONE);

    }

    @Override
    protected void onStart() {
        super.onStart();

        myProgressbar.setVisibility(View.VISIBLE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                layout.setVisibility(View.VISIBLE);
                layout.startAnimation(animation);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        textView.setVisibility(View.VISIBLE);
                        textView.startAnimation(animation);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                //Find Current User and Redirecting To The Current User Activity
                                //Fire base Authentication and get current user
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null){
                                    //get user uid
                                    final String user_id = user.getUid();
                                    //get database reference
                                    final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                                    final Query query = reference.orderByChild(user_id);

                                   query.addChildEventListener(new ChildEventListener() {
                                        @Override
                                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                                            for (DataSnapshot child: dataSnapshot.getChildren())
                                            {
                                                String key = child.getKey();
                                                String value = child.getValue().toString();
                                                List<String> valueList = Arrays.asList(value.split("=|\\{|,|\\s"));

                                                if (valueList.contains(user_id)){

                                                    if (key.equals("Driver")){
                                                        Toast.makeText(MainActivity.this, "Log in Successful!!", Toast.LENGTH_SHORT).show();
                                                        new Handler().postDelayed(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                startActivity(new Intent(MainActivity.this, DriverMapActivity.class));
                                                                finish();
                                                            }
                                                        }, 500);
                                                    }else if (key.equals("Rider")){
                                                        new Handler().postDelayed(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                startActivity(new Intent(MainActivity.this, RiderMapActivity.class));
                                                                finish();
                                                            }
                                                        }, 500);
                                                    }
                                                }
                                            }
                                        }
                                        @Override
                                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                                        }
                                        @Override
                                        public void onChildRemoved(DataSnapshot dataSnapshot) {

                                        }
                                        @Override
                                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                                        }
                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                                }else {
                                    startActivity(new Intent(MainActivity.this, EmailLogInActivity.class));
                                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                }
                            }
                        }, 5000);
                    }
                }, 1000);
            }
        }, 500);
    }
}
