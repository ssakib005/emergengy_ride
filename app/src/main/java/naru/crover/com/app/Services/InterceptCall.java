package naru.crover.com.app.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;

import naru.crover.com.app.Activites.Riders.RiderMapActivity;

public class InterceptCall extends BroadcastReceiver {

    private String number;
    private String state;

    @Override
    public void onReceive(Context context, Intent intent) {

        state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

        try{
            if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)){
                number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                FirebaseAuth auth = FirebaseAuth.getInstance();
//                Toast.makeText(context, "Service Started", Toast.LENGTH_SHORT).show();
                String userId =  auth.getUid();
                DatabaseReference database =  FirebaseDatabase.getInstance().getReference().child("callStatus").child(userId);
                HashMap map = new HashMap();
                map.put("number", number);
                database.updateChildren(map);
            }
            else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)){
                FirebaseAuth auth = FirebaseAuth.getInstance();
//                Toast.makeText(context, "Service Received", Toast.LENGTH_SHORT).show();
                String userId =  auth.getUid();
                DatabaseReference database =  FirebaseDatabase.getInstance().getReference().child("callStatus").child(userId);
                HashMap map = new HashMap();
                map.put("received", 1);
                database.updateChildren(map);
            }
            else if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_IDLE)) {

                FirebaseAuth auth = FirebaseAuth.getInstance();
//                Toast.makeText(context, "Service End", Toast.LENGTH_SHORT).show();
                String userId =  auth.getUid();
                DatabaseReference database =  FirebaseDatabase.getInstance().getReference().child("callStatus").child(userId);
                HashMap map = new HashMap();
                map.put("end", 1);
                database.updateChildren(map);

            }
        }catch (Exception e){
            Toast.makeText(context, "Exception" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
