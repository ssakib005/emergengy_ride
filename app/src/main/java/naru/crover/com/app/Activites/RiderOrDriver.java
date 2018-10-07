package naru.crover.com.app.Activites;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import com.hbb20.CountryCodePicker;

import naru.crover.com.app.Activites.Drivers.DriverMapActivity;
import naru.crover.com.app.R;

public class RiderOrDriver extends AppCompatActivity {

    private ScrollView scrollView;
    private RelativeLayout layout;
    private EditText userPhoneNumber;
    private ImageView imageView;
    private CountryCodePicker ccp;
    private TextView textView;
    private Switch aSwitch;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_or_driver);

        scrollView = findViewById(R.id.riderOrDriverScrollView);
        layout = findViewById(R.id.mainLayout);
        textView = findViewById(R.id.signIn);
        aSwitch = findViewById(R.id.switchRiderOrDriver);


        userPhoneNumber = findViewById(R.id.etUserPhoneNumber);
        ccp = findViewById(R.id.ccpBtn);

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                View view = RiderOrDriver.this.getCurrentFocus();

                if (view != null){

                    InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    assert manager != null;
                    manager.hideSoftInputFromWindow(view.getWindowToken(), 0);

                }

            }
        });

        userPhoneNumber.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                final Handler handler;
                handler = new Handler();
                final Runnable r = new Runnable() {
                    public void run() {
                        scrollView.smoothScrollTo(0, 300);
                        handler.postDelayed(this, 200);
                    }
                };
                handler.postDelayed(r, 200);
            }
        });

        imageView = findViewById(R.id.logInBtn);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RiderOrDriver.this, DriverMapActivity.class));
            }
        });

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String userType;

                if (aSwitch.isChecked()){
                    userType = "Driver";
                }else {
                    userType = "Rider";
                }

                startActivity(new Intent(RiderOrDriver.this, EmailLogInActivity.class).putExtra("switch",userType));
            }
        });

    }

    private void attemptLogin(){

        userPhoneNumber.setError(null);

        ccp.registerCarrierNumberEditText(userPhoneNumber);
        Log.i("PhoneNumber", ccp.getFullNumber());

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}
