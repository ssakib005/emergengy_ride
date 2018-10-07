package naru.crover.com.app.Activites.Drivers;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.Resource;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import io.grpc.internal.SharedResourceHolder;
import naru.crover.com.app.Activites.Riders.RiderMapActivity;
import naru.crover.com.app.Activites.Riders.RiderProfileActivity;
import naru.crover.com.app.R;

public class DriverProfile extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageView imageView;
    private EditText driverName, driverPhoneNumber, driverPresentAddress, driverPermanentAddress, driverCarModel, driverCarNumber;
    private TextView date;
    private Spinner gender, carType;
    private Button saveBtn;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private String userID;
    private static final int GALLERY_INTENT = 2;
    private Uri resultUri;
    private String myProfileUrl;
    private DatePickerDialog.OnDateSetListener dateSetListener;
    private String dName;
    private String dPhone;
    private String dGender;
    private String dDateOfBirth;
    private String dPresentAddress;
    private String dPermanentAddress;
    private String dCarModel;
    private String dCarNumber;
    private String service;
    private ImageView btnAmbulance, btnCng;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_profile);

        toolbar = findViewById(R.id.actionToolbar);
        toolbar.setTitleTextColor(0xFFFFFFFF);
        toolbar.setTitle("Profile");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DriverProfile.this, DriverMapActivity.class));
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child("Profile").child(userID);

        imageView = findViewById(R.id.riderImage);
        driverName = findViewById(R.id.userNameTextBox);
        driverPhoneNumber = findViewById(R.id.userPhoneNumber);
        driverPresentAddress = findViewById(R.id.userPresentAddress);
        driverPermanentAddress = findViewById(R.id.userPermanentAddress);
        driverCarModel = findViewById(R.id.carModel);
        driverCarNumber = findViewById(R.id.carNumber);
        gender = findViewById(R.id.gender);
        date = findViewById(R.id.birth);
        btnAmbulance = findViewById(R.id.btnAmbulance);
        btnCng = findViewById(R.id.btnCng);
        saveBtn = findViewById(R.id.saveBtn);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDriverProfile();
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Intent.ACTION_PICK).setType("image/*"), GALLERY_INTENT);
            }
        });

        final ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(DriverProfile.this,
                R.layout.support_simple_spinner_dropdown_item, getResources().getStringArray(R.array.genderList));
        genderAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        gender.setAdapter(genderAdapter);

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                final int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(

                        DriverProfile.this,
                        R.style.Theme_AppCompat_Dialog_MinWidth,
                        dateSetListener, year, month, day
                );

                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
                dialog.show();
            }
        });

        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month+1;
                String datePicker = dayOfMonth + "-" + month + "-" + year;
                date.setText(datePicker);
            }
        };

        getUserInfo();

        btnCng.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                service = "cng";
                btnCng.setBackground(getResources().getDrawable(R.drawable.round_button_service));
                btnAmbulance.setBackground(getResources().getDrawable(R.drawable.round_button));
            }
        });

        btnAmbulance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                service = "ambulance";
                btnCng.setBackground(getResources().getDrawable(R.drawable.round_button));
                btnAmbulance.setBackground(getResources().getDrawable(R.drawable.round_button_service));
            }
        });
    }
    private void saveDriverProfile(){
        dName = driverName.getText().toString();
        dPhone = driverPhoneNumber.getText().toString();
        dGender = gender.getSelectedItem().toString();
        dDateOfBirth = date.getText().toString();

        dPresentAddress = driverPresentAddress.getText().toString();
        dPermanentAddress = driverPermanentAddress.getText().toString();
        dCarModel = driverCarModel.getText().toString();
        dCarNumber = driverCarNumber.getText().toString();
//        dCarType = carType.getSelectedItem().toString();

        if (resultUri != null){
            storageReference = FirebaseStorage.getInstance().getReference().child("Driver_Profile_Images").child(userID);
            Bitmap bitmap = null;

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);

            byte[] data = baos.toByteArray();

            UploadTask uploadTask = storageReference.putBytes(data);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                    if (!task.isSuccessful()){
                        Toast.makeText(getApplication(), "Upload Unsuccessful...", Toast.LENGTH_SHORT).show();
                    }

                    return storageReference.getDownloadUrl();

                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {

                    Uri downloadUrl = task.getResult();

                    Map userInfo = new HashMap();
                    userInfo.put("dName", dName);
                    userInfo.put("dPhone", dPhone);
                    userInfo.put("dGender", dGender);
                    userInfo.put("dDateOfBirth", dDateOfBirth);
                    userInfo.put("dPresentAddress", dPresentAddress);
                    userInfo.put("dPermanentAddress", dPermanentAddress);
                    userInfo.put("dVehicleName", dCarModel);
                    userInfo.put("dVehicleModel", dCarNumber);
                    userInfo.put("dService", service);
                    userInfo.put("profileImage", downloadUrl.toString());

                    databaseReference.updateChildren(userInfo);
                }
            });
        }else {

            Map userInfo = new HashMap();
            userInfo.put("dName", dName);
            userInfo.put("dPhone", dPhone);
            userInfo.put("dGender", dGender);
            userInfo.put("dDateOfBirth", dDateOfBirth);
            userInfo.put("dPresentAddress", dPresentAddress);
            userInfo.put("dPermanentAddress", dPermanentAddress);
            userInfo.put("dVehicleName", dCarModel);
            userInfo.put("dVehicleModel", dCarNumber);
            userInfo.put("dService", service);
            databaseReference.updateChildren(userInfo);
        }

//        clearTextBox();
        Toast.makeText(getApplicationContext(), "Profile Updated Successfully..", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                driverMapActivity();
            }
        }, 200);
    }

    private void driverMapActivity(){
        startActivity(new Intent(DriverProfile.this, DriverMapActivity.class));
    }

    private void clearTextBox(){
        driverName.setText("");
        driverPhoneNumber.setText("");
    }

    private void getUserInfo(){

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0){

                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    if (map.get("dName") != null){
                        dName = map.get("dName").toString();
                        driverName.setText(dName);
                    }
                    if (map.get("dPhone") != null){
                        dPhone = map.get("dPhone").toString();
                        driverPhoneNumber.setText(dPhone);
                    }
                    if (map.get("dDateOfBirth") != null){
                        dDateOfBirth = map.get("dDateOfBirth").toString();
                        date.setText(dDateOfBirth);
                    }
                    if (map.get("dPresentAddress") != null){
                        dPresentAddress = map.get("dPresentAddress").toString();
                        driverPresentAddress.setText(dPresentAddress);
                    }
                    if (map.get("dPermanentAddress") != null){
                        dPermanentAddress = map.get("dPermanentAddress").toString();
                        driverPermanentAddress.setText(dPermanentAddress);
                    }
                    if (map.get("dVehicleName") != null){
                        dCarModel = map.get("dVehicleName").toString();
                        driverCarModel.setText(dCarModel);
                    }
                    if (map.get("dVehicleModel") != null){
                        dCarNumber = map.get("dVehicleModel").toString();
                        driverCarNumber.setText(dCarNumber);
                    }
                    if (map.get("dService") != null){
                        service = map.get("dService").toString();
                        displayCarService(service);
                    }
                    if (map.get("profileImage") != null){
                        myProfileUrl = map.get("profileImage").toString();
                        Glide.with(getApplication()).load(myProfileUrl).into(imageView);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_INTENT && resultCode == Activity.RESULT_OK){

            final Uri imageUri = data.getData();
            resultUri = imageUri;
            imageView.setImageURI(resultUri);
        }
    }

    private void displayCarService(String ser){
        switch (ser){
            case "cng":
                btnCng.setBackground(getResources().getDrawable(R.drawable.round_button_service));
                break;
            case "ambulance":
                btnAmbulance.setBackground(getResources().getDrawable(R.drawable.round_button_service));
                break;
        }

    }
}
