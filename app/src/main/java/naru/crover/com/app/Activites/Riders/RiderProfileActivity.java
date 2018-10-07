package naru.crover.com.app.Activites.Riders;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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

import naru.crover.com.app.R;

public class RiderProfileActivity extends AppCompatActivity {

    private ImageView imageView;
    private EditText riderName, riderPhoneNumber;
    private Button saveBtn;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private String userID;
    private static final int GALLERY_INTENT = 2;
    private Uri resultUri;
    private String myProfileUrl;
    private Toolbar toolbar;
    private Spinner gender;
    private TextView date;
    private DatePickerDialog.OnDateSetListener dateSetListener;
    private String rName;
    private String rPhone;
    private String rGender;
    private String rDateOfBirth;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_profile);

        toolbar = findViewById(R.id.actionToolbar);
        toolbar.setTitleTextColor(0xFFFFFFFF);
        toolbar.setTitle("Profile");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RiderProfileActivity.this, RiderMapActivity.class));
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Rider").child("Profile").child(userID);

        imageView = findViewById(R.id.riderImage);
        riderName = findViewById(R.id.userNameTextBox);
        riderPhoneNumber = findViewById(R.id.userPhoneNumber);
        gender = findViewById(R.id.gender);
        date = findViewById(R.id.birth);


        saveBtn = findViewById(R.id.saveBtn);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveRiderProfile();
            }
        });
        getUserInfo();
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Intent.ACTION_PICK).setType("image/*"), GALLERY_INTENT);
            }
        });

        final ArrayAdapter<String> myAdapter = new ArrayAdapter<>(RiderProfileActivity.this,
                R.layout.support_simple_spinner_dropdown_item, getResources().getStringArray(R.array.genderList));
        myAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        gender.setAdapter(myAdapter);

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                final int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(

                        RiderProfileActivity.this,
                        R.style.Theme_AppCompat_Dialog_MinWidth,
                        dateSetListener, year, month, day
                );

                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
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

    }

    private void saveRiderProfile(){
        rName = riderName.getText().toString();
        rPhone = riderPhoneNumber.getText().toString();
        rGender = gender.getSelectedItem().toString();
        rDateOfBirth = date.getText().toString();

        if (resultUri != null){
            storageReference = FirebaseStorage.getInstance().getReference().child("Profile_Images").child(userID);
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
                    userInfo.put("rName", rName);
                    userInfo.put("rPhone", rPhone);
                    userInfo.put("rGender", rGender);
                    userInfo.put("rDateOfBirth", rDateOfBirth);
                    userInfo.put("profileImage", downloadUrl.toString());
                    databaseReference.updateChildren(userInfo);

                }
            });
        }else {

            Map userInfo = new HashMap();
            userInfo.put("rName", rName);
            userInfo.put("rPhone", rPhone);
            userInfo.put("rGender", rGender);
            userInfo.put("rDateOfBirth", rDateOfBirth);
            databaseReference.updateChildren(userInfo);

        }

//        clearTextBox();
        Toast.makeText(getApplicationContext(), "Profile Updated Successfully..", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                riderMapActivity();
            }
        }, 200);
    }

    private void riderMapActivity(){
        startActivity(new Intent(RiderProfileActivity.this, RiderMapActivity.class));
    }

    private void clearTextBox(){
        riderName.setText("");
        riderPhoneNumber.setText("");
    }

    private void getUserInfo(){

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0){

                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    if (map.get("rName") != null){
                        rName = map.get("rName").toString();
                        riderName.setText(rName);
                    }
                    if (map.get("rPhone") != null){
                        rPhone = map.get("rPhone").toString();
                        riderPhoneNumber.setText(rPhone);
                    }
                    if (map.get("rDateOfBirth") != null){
                        rDateOfBirth = map.get("rDateOfBirth").toString();
                        date.setText(rDateOfBirth);
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
}
