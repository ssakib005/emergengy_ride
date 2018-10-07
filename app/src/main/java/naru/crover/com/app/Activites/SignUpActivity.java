package naru.crover.com.app.Activites;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import naru.crover.com.app.Activites.Drivers.DriverMapActivity;
import naru.crover.com.app.Activites.Drivers.DriverProfile;
import naru.crover.com.app.Activites.Riders.RiderMapActivity;
import naru.crover.com.app.Activites.Riders.RiderProfileActivity;
import naru.crover.com.app.R;

public class SignUpActivity extends AppCompatActivity {

    private EditText emailEt1, passwordEt2, confirmPass;
    private Button signUpBtn;
    private FirebaseAuth myAuth;
    private FirebaseAuth.AuthStateListener myAuthStateListener;
    private Switch aSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        emailEt1 = findViewById(R.id.emailTextBox);
        passwordEt2 = findViewById(R.id.passwordTextBox);
        confirmPass = findViewById(R.id.confirmPasswordTextBox);
        aSwitch = findViewById(R.id.switchRiderOrDriver);

        myAuth = FirebaseAuth.getInstance();

        signUpBtn = findViewById(R.id.btnSignUp);
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = "";
                String password = "";
                String conPass = "";
                if (emailEt1.getText().equals("")){
                    emailEt1.setError("Required");
                }else {
                    email = emailEt1.getText().toString();
                }

                if (passwordEt2.getText().equals("")){
                    passwordEt2.setError("Required");
                }else {
                    password = passwordEt2.getText().toString();
                }

                if (confirmPass.getText().equals("")){
                    confirmPass.setError("Required");
                }else {
                    conPass = confirmPass.getText().toString();
                }

                if (emailEt1.getText().equals("") || passwordEt2.getText().equals("")){

                    Toast.makeText(SignUpActivity.this, "Please Fill Email And Password", Toast.LENGTH_SHORT).show();

                }else {

                    if (!password.equals(conPass)){
                        Toast.makeText(SignUpActivity.this, "Invalid Password", Toast.LENGTH_SHORT).show();
                    }else {
                        myAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (!task.isSuccessful()) {
                                    Toast.makeText(SignUpActivity.this, "Sign Up Error !!", Toast.LENGTH_SHORT).show();
                                } else {

                                    String userType;

                                    if (aSwitch.isChecked()){
                                        userType = "Driver";
                                    }else {
                                        userType = "Rider";
                                    }

                                    if (userType.equals("Driver")) {
                                        String user_Id = myAuth.getCurrentUser().getUid();
                                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child("Activity").child(user_Id);
                                        reference.setValue(true);
                                        startActivity(new Intent(SignUpActivity.this, DriverProfile.class));
                                        finish();
                                    } else {
                                        String user_Id = myAuth.getCurrentUser().getUid();
                                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child("Rider").child("Activity").child(user_Id);
                                        reference.setValue(true);
                                        startActivity(new Intent(SignUpActivity.this, RiderProfileActivity.class));
                                        finish();
                                    }
                                }
                            }
                        });
                    }
                }
            }
        });
    }
}
