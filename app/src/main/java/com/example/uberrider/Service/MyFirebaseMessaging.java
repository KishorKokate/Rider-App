package com.example.uberrider.Service;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.uberrider.Common.Common;
import com.example.uberrider.Utils.UserUtils;
import com.example.uberrider.model.EventBus.DeclineRequestAndRemoveTripFromDriver;
import com.example.uberrider.model.EventBus.DeclineRequestFromDriver;
import com.example.uberrider.model.EventBus.DriverAcceptTripEvent;
import com.example.uberrider.model.Token;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.util.Map;
import java.util.Random;

public class MyFirebaseMessaging extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Log.e("NEW_TOKEN", s);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            UserUtils.updateToken(this, s);
        }
        if (!s.isEmpty()) {
            //  updateTokenServer(s);

        }
    }


    /*   private void updateTokenServer(String s) {
           FirebaseDatabase db = FirebaseDatabase.getInstance();
           DatabaseReference tokens = db.getReference(Common.token_tbl);

           Token token = new Token(s);
           if (FirebaseAuth.getInstance().getCurrentUser() != null) //if already login ,must update token
               tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token);
       }
   */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Map<String, String> dataRecv = remoteMessage.getData();
        if (dataRecv != null) {
            if (dataRecv.get(Common.NOTI_TITLE) != null) {

                if (dataRecv.get(Common.NOTI_TITLE).equals(Common.REQUEST_DRIVER_DECLINE))
                {
                    EventBus.getDefault().postSticky(new DeclineRequestFromDriver());

                }else if (dataRecv.get(Common.NOTI_TITLE).equals(Common.REQUEST_DRIVER_DECLINE_AND_REMOVE_TRIP))
                {
                    EventBus.getDefault().postSticky(new DeclineRequestAndRemoveTripFromDriver());

                }
                else if (dataRecv.get(Common.NOTI_TITLE).equals(Common.REQUEST_DRIVER_ACCEPT)){
                    String tripKey=dataRecv.get(Common.TRIP_KEY);
                    EventBus.getDefault().postSticky(new
                            DriverAcceptTripEvent(tripKey));
                }
                else
                {
                    Common.showNtification(this, new Random().nextInt(),
                            dataRecv.get(Common.NOTI_TITLE),
                            dataRecv.get(Common.NOTI_CONTENT),
                            null);
                }

            }
        }


    }
}
