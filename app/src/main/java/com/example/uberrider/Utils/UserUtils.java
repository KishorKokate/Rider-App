package com.example.uberrider.Utils;

import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.uberrider.Common.Common;
import com.example.uberrider.R;
import com.example.uberrider.Remote.IFCService;
import com.example.uberrider.Remote.RetrofitFCMClient;
import com.example.uberrider.model.DriverGeoModel;
import com.example.uberrider.model.EventBus.SelectPlaceEvent;
import com.example.uberrider.model.FCMSendData;
import com.example.uberrider.model.Token;
import com.example.uberrider.requestdriverActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class UserUtils {
    public static void updateUser(View view, Map<String, Object> updateData) {

        FirebaseDatabase.getInstance()
                .getReference(Common.user_rider_tbl)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .updateChildren(updateData)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(view, e.getMessage(), Snackbar.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Snackbar.make(view, "update information successfully!", Snackbar.LENGTH_SHORT).show();


            }
        });
    }

    public static void updateToken(Context context, String token) {

        Token tokenModel = new Token(token);

        FirebaseDatabase.getInstance()
                .getReference(Common.token_tbl)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(tokenModel)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

            }
        });

    }


    public static void sendRequestToDriver(Context context, RelativeLayout main_layout, DriverGeoModel foundDriver, SelectPlaceEvent selectPlaceEvent) {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        IFCService ifcService = RetrofitFCMClient.getInstance().create(IFCService.class);

        //get token
        FirebaseDatabase
                .getInstance()
                .getReference(Common.token_tbl)
                .child(foundDriver.getKey())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {
                            Token tokenModel = snapshot.getValue(Token.class);

                            Map<String, String> notificationData = new HashMap<>();
                            notificationData.put(Common.NOTI_TITLE, Common.REQUEST_DRIVER_TITLE);
                            notificationData.put(Common.NOTI_CONTENT, "This message represent for request driver action");
                            notificationData.put(Common.RIDER_KEY,FirebaseAuth.getInstance().getCurrentUser().getUid());

                            notificationData.put(Common.RIDER_PICKUP_LOCATION_STRING, selectPlaceEvent.getOriginString());
                            notificationData.put(Common.RIDER_PICKUP_LOCATION, new StringBuilder("")
                                    .append(selectPlaceEvent.getOrigin().latitude)
                                    .append(",")
                                    .append(selectPlaceEvent.getOrigin().longitude)
                                    .toString());

                            notificationData.put(Common.RIDER_DESTINATION_STRING, selectPlaceEvent.getAddress());
                            notificationData.put(Common.RIDER_DESTINATION, new StringBuilder("")
                                    .append(selectPlaceEvent.getDestination().latitude)
                                    .append(",")
                                    .append(selectPlaceEvent.getDestination().longitude)
                                    .toString());




                            FCMSendData fcmSendData = new FCMSendData(tokenModel.getToken(), notificationData);

                            compositeDisposable.add(ifcService.sendNotification(fcmSendData)
                                    .subscribeOn(Schedulers.newThread())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(fcmResponse -> {
                                        if (fcmResponse.getSuccess() == 0) {
                                            compositeDisposable.clear();
                                            Snackbar.make(main_layout, context.getString(R.string.request_driver_found), Snackbar.LENGTH_LONG).show();
                                        }
                                    }, throwable -> {

                                        compositeDisposable.clear();
                                        Snackbar.make(main_layout, throwable.getMessage(), Snackbar.LENGTH_LONG).show();
                                    }));

                        } else {
                            Snackbar.make(main_layout, context.getString(R.string.token_not_found), Snackbar.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        Snackbar.make(main_layout, error.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                });
    }
}
