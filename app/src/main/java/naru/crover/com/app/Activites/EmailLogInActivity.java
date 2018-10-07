package naru.crover.com.app.Activites;

import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
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

public class EmailLogInActivity extends AppCompatActivity {

    private EditText emailEt1, passwordEt2;
    private Button signInBtn;
    private FirebaseAuth myAuth;
    private TextView signUpBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_log_in);

        emailEt1 = findViewById(R.id.emailTextBox);
        passwordEt2 = findViewById(R.id.passwordTextBox);
        myAuth = FirebaseAuth.getInstance();

        signInBtn = findViewById(R.id.btnSignIn);
        signUpBtn = findViewById(R.id.btnSignUp);


        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EmailLogInActivity.this, SignUpActivity.class));
            }
        });


        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = "";
                String password = "";
                if (TextUtils.isEmpty(emailEt1.getText())){
                    emailEt1.setError("Required");
                }else {
                    email = emailEt1.getText().toString();
                }

                if (TextUtils.isEmpty(passwordEt2.getText())){
                    passwordEt2.setError("Required");
                }else {
                    password = passwordEt2.getText().toString();
                }

                if (emailEt1.getText().equals("") || passwordEt2.getText().equals("")){

                    Toast.makeText(EmailLogInActivity.this, "Please Fill Email And Password", Toast.LENGTH_SHORT).show();

                }else
                {
                    myAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(EmailLogInActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()){

                                final FirebaseUser database = FirebaseAuth.getInstance().getCurrentUser();
                                assert database != null;
                                final String user_id = database.getUid();

                                final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();


                                final Query userQuery = reference.orderByChild(user_id);

                                userQuery.addChildEventListener(new ChildEventListener() {
                                    @Override
                                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                                        for (DataSnapshot child: dataSnapshot.getChildren())
                                        {
                                            String key = child.getKey();
                                            String value = child.getValue().toString();
                                            List<String> valueList = Arrays.asList(value.split("=|\\{|,|\\s"));

                                            if (valueList.contains(user_id)){

                                                if (key.equals("Driver")){
                                                    Toast.makeText(EmailLogInActivity.this, "Driver Log in Successful!!", Toast.LENGTH_SHORT).show();
                                                    new Handler().postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            startActivity(new Intent(EmailLogInActivity.this, DriverMapActivity.class));
                                                            finish();
                                                        }
                                                    }, 500);
                                                }else if (key.equals("Rider")){
                                                    Toast.makeText(EmailLogInActivity.this, "Rider Log in Successful!!", Toast.LENGTH_SHORT).show();
                                                    new Handler().postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            startActivity(new Intent(EmailLogInActivity.this, RiderMapActivity.class));
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
                                Toast.makeText(EmailLogInActivity.this, "Log In Failed! Invalid Email/Password", Toast.LENGTH_SHORT).show();
                                emailEt1.setText("");
                                passwordEt2.setText("");
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}
