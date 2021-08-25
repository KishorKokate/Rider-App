package com.example.uberrider;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.uberrider.Common.Common;
import com.example.uberrider.Remote.IGoogleAPI;
import com.example.uberrider.Remote.RetrofitClient;
import com.example.uberrider.Utils.UserUtils;
import com.example.uberrider.model.DriverGeoModel;
import com.example.uberrider.model.EventBus.DeclineRequestAndRemoveTripFromDriver;
import com.example.uberrider.model.EventBus.DeclineRequestFromDriver;
import com.example.uberrider.model.EventBus.DriverAcceptTripEvent;
import com.example.uberrider.model.EventBus.SelectPlaceEvent;
import com.example.uberrider.model.TripPlanModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.uberrider.databinding.ActivityRequestdriverBinding;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.ui.IconGenerator;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class requestdriverActivity extends FragmentActivity implements OnMapReadyCallback {

    //slowly camera spinning
    private ValueAnimator animator;
    private static final int DESIRED_NUM_OF_SPIN = 5;
    private static final int DESTRED_SECOND_PRE_ONE_FULL_360_SPIN = 40;
    //effect
    private Circle lastUserCircle;
    private long duration = 1000;
    private ValueAnimator lastPulseAnimator;
    private DriverGeoModel lastDriver;
    private String driverOldPosition = "";

    private Handler handler;
    private float v;
    private double lat, lng;
    private int index, next;
    private LatLng start, end;


    //view

    @BindView(R.id.confirm_uber_layout)
    CardView confirm_uber_layout;
    @BindView(R.id.finding_your_ride_layout)
    CardView finding_your_ride_layout;

    @BindView(R.id.btn_confirm_uber)
    Button btn_confirm_uber;

    @BindView(R.id.confirm_pickup_layout)
    CardView confirm_pickup_layout;
    @BindView(R.id.btn_confirm_pickup)
    Button btn_confirm_pickup;

    @BindView(R.id.txt_address_pickup)
    TextView txt_address_pickup;

    @BindView(R.id.fill_maps)
    View fill_maps;
    @BindView(R.id.main_layout)
    RelativeLayout main_layout;

    @BindView(R.id.driver_info_layout)
    CardView driver_info_layout;
    @BindView(R.id.txt_driver_name)
    TextView txt_driver_name;
    @BindView(R.id.img_driver)
    ImageView img_driver;


    @OnClick(R.id.btn_confirm_uber)
    void onConfirmUber() {
        confirm_pickup_layout.setVisibility(View.VISIBLE);
        confirm_uber_layout.setVisibility(View.GONE);

        setDataPickup();
    }

    @OnClick(R.id.btn_confirm_pickup)
    void onComfirmPickup() {
        if (mMap == null) return;
        if (selectPlaceEvent == null) return;

        //clear map
        mMap.clear();
        //Tilt
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(selectPlaceEvent.getOrigin())
                .tilt(45f)
                .zoom(16f)
                .build();

        //start animation
        addMarkerWithPulseanimation();
    }

    private void addMarkerWithPulseanimation() {
        confirm_pickup_layout.setVisibility(View.GONE);
        fill_maps.setVisibility(View.VISIBLE);
        finding_your_ride_layout.setVisibility(View.VISIBLE);

        originMarker = mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.defaultMarker())
                .position(selectPlaceEvent.getOrigin()));

        addPulsatingEffect(selectPlaceEvent);
    }


    private void addPulsatingEffect(SelectPlaceEvent selectPlaceEvent) {
        if (lastPulseAnimator != null) lastPulseAnimator.cancel();
        if (lastUserCircle != null) lastUserCircle.setCenter(selectPlaceEvent.getOrigin());

        lastPulseAnimator = Common.valueAnimate(duration, animation -> {
            if (lastUserCircle != null)
                lastUserCircle.setRadius((Float) animation.getAnimatedValue());
            else {
                lastUserCircle = mMap.addCircle(new CircleOptions()
                        .center(selectPlaceEvent.getOrigin())
                        .radius((Float) animation.getAnimatedValue())
                        .strokeColor(Color.WHITE)
                        .fillColor(Color.parseColor("#333333333"))
                );
            }
        });

        startMapCameraSpinningAnimation(selectPlaceEvent);
    }

    private void startMapCameraSpinningAnimation(SelectPlaceEvent selectPlaceEvent) {
        if (animator != null) animator.cancel();
        animator = ValueAnimator.ofFloat(0, DESIRED_NUM_OF_SPIN * 360);
        animator.setDuration(DESIRED_NUM_OF_SPIN * DESIRED_NUM_OF_SPIN * 1000);
        animator.setInterpolator(new LinearInterpolator());
        animator.setStartDelay(100);
        animator.addUpdateListener(valueAnimator -> {
            Float newBearingValue = (Float) valueAnimator.getAnimatedValue();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                    .target(selectPlaceEvent.getOrigin())
                    .zoom(16f)
                    .tilt(45)
                    .bearing(newBearingValue)
                    .build()));
        });
        animator.start();

        //find driver
        findNearByDriver(selectPlaceEvent);

    }

    private void findNearByDriver(SelectPlaceEvent selectPlaceEvent) {
        if (Common.driversFound.size() > 0) {

            float min_distance = 0; //default min distance 0
            DriverGeoModel foundDriver = null;
            Location currentRiderLocation = new Location("");
            currentRiderLocation.setLatitude(selectPlaceEvent.getOrigin().latitude);
            currentRiderLocation.setLongitude(selectPlaceEvent.getOrigin().longitude);
            for (String key : Common.driversFound.keySet()) {
                Location driverLocation = new Location("");
                driverLocation.setLatitude(Common.driversFound.get(key).getGeoLocation().latitude);
                driverLocation.setLongitude(Common.driversFound.get(key).getGeoLocation().longitude);


                //compare 2 location
                if (min_distance == 0) {
                    min_distance = driverLocation.distanceTo(currentRiderLocation); //first min distance

                    if (!Common.driversFound.get(key).isDecline()) //if not decline before
                    {
                        foundDriver = Common.driversFound.get(key);
                        break; //Exit loop because we found driver
                    } else continue; //if already decline before , just skip and continue


                } else if (driverLocation.distanceTo(currentRiderLocation) < min_distance) {
                    //if have any driver smaller min_distance, just get it
                    min_distance = driverLocation.distanceTo(currentRiderLocation); //first min distance
                    if (!Common.driversFound.get(key).isDecline()) //if not decline before
                    {
                        foundDriver = Common.driversFound.get(key);
                        break; //Exit loop because we found driver
                    } else continue; //if already decline before , just skip and continue

                }
                //       Snackbar.make(main_layout, new StringBuilder("Found driver")
                //             .append(foundDriver.getDriverInfoModel().getPhoneNumber()), Snackbar.LENGTH_LONG).show();

            }

            //after loop
            if (foundDriver != null) {
                UserUtils.sendRequestToDriver(this, main_layout, foundDriver, selectPlaceEvent);

                lastDriver = foundDriver;
            } else {
                Toast.makeText(this, getString(R.string.no_driver_accept_request), Toast.LENGTH_SHORT).show();
                lastDriver = null;
                finish();
            }
        } else {
            //Not found

            Snackbar.make(main_layout, getString(R.string.driver_not_found), Snackbar.LENGTH_SHORT).show();

            lastDriver = null;
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        if (animator != null) animator.end();
        super.onDestroy();
    }

    private void setDataPickup() {
        txt_address_pickup.setText(txt_origin != null ? txt_origin.getText() : "None");
        mMap.clear(); //clear all map
        //Add pickupmarker
        addPickupMarker();
    }

    private void addPickupMarker() {
        View view = getLayoutInflater().inflate(R.layout.pickup_info_window, null);
    }


    TextView txt_origin;

    private GoogleMap mMap;
    private ActivityRequestdriverBinding binding;

    private SelectPlaceEvent selectPlaceEvent;

    //Routes
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private IGoogleAPI iGoogleAPI;
    private Polyline blackPolyline, greyPolyline;
    private PolylineOptions blackPolylineOptions, polylineOptions;
    private List<LatLng> polilineList;

    private Marker originMarker, destinationMarker;


    @Override
    protected void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        compositeDisposable.clear();
        super.onStop();

        if (EventBus.getDefault().hasSubscriberForEvent(SelectPlaceEvent.class))
            EventBus.getDefault().removeStickyEvent(SelectPlaceEvent.class);
        if (EventBus.getDefault().hasSubscriberForEvent(DeclineRequestFromDriver.class))
            EventBus.getDefault().removeStickyEvent(DeclineRequestFromDriver.class);
        if (EventBus.getDefault().hasSubscriberForEvent(DriverAcceptTripEvent.class))
            EventBus.getDefault().removeStickyEvent(DriverAcceptTripEvent.class);
        if (EventBus.getDefault().hasSubscriberForEvent(DeclineRequestAndRemoveTripFromDriver.class))
            EventBus.getDefault().removeStickyEvent(DeclineRequestAndRemoveTripFromDriver.class);


        EventBus.getDefault().unregister(this);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onDriverAcceptEvent(DriverAcceptTripEvent event) {

        //get trip information
        FirebaseDatabase.getInstance().getReference(Common.TRIP)
                .child(event.getTripIp())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            TripPlanModel tripPlanModel = snapshot.getValue(TripPlanModel.class);
                            mMap.clear();
                            fill_maps.setVisibility(View.GONE);
                            if (animator != null) animator.end();
                            CameraPosition cameraPosition = new CameraPosition.Builder()
                                    .tilt(0f)
                                    .target(mMap.getCameraPosition().target)
                                    .zoom(mMap.getCameraPosition().zoom)
                                    .build();
                            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                            //get routes
                            String driverLocation = new StringBuilder()
                                    .append(tripPlanModel.getCurrentLat())
                                    .append(",")
                                    .append(tripPlanModel.getCurrentLng())
                                    .toString();

                            //Request API
                            compositeDisposable.add(iGoogleAPI.getDirection("driving",
                                    "less_driving",
                                    tripPlanModel.getOrigin(), driverLocation,
                                    getString(R.string.google_maps_key))
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(returnResult -> {
                                        PolylineOptions blackPolylineOptions = null;
                                        List<LatLng> polylineList = null;
                                        Polyline blackPolyline = null;

                                        try {
                                            //parse json
                                            JSONObject jsonObject = new JSONObject(returnResult);
                                            JSONArray jsonArray = jsonObject.getJSONArray("routes");
                                            for (int i = 0; i < jsonArray.length(); i++) {
                                                JSONObject route = jsonArray.getJSONObject(i);
                                                JSONObject poly = route.getJSONObject("overview_polyline");
                                                String polyline = poly.getString("points");
                                                polilineList = Common.decodePoly(polyline);

                                            }


                                            blackPolylineOptions = new PolylineOptions();
                                            blackPolylineOptions.color(Color.BLACK);
                                            blackPolylineOptions.width(12);
                                            blackPolylineOptions.startCap(new SquareCap());
                                            blackPolylineOptions.jointType(JointType.ROUND);
                                            blackPolylineOptions.addAll(polilineList);
                                            blackPolyline = mMap.addPolyline(blackPolylineOptions);


                                            JSONObject object = jsonArray.getJSONObject(0);
                                            JSONArray legs = object.getJSONArray("legs");
                                            JSONObject legObject = legs.getJSONObject(0);

                                            JSONObject time = legObject.getJSONObject("duration");
                                            String duration = time.getString("text");

                                            JSONObject distanceEstimate = legObject.getJSONObject("distance");
                                            String distance = distanceEstimate.getString("text");

                                            LatLng origin = new LatLng(
                                                    Double.parseDouble(tripPlanModel.getOrigin().split(",")[0]),
                                                    Double.parseDouble(tripPlanModel.getOrigin().split(",")[1])
                                            );
                                            LatLng destination = new LatLng(tripPlanModel.getCurrentLat(), tripPlanModel.getCurrentLng());


                                            LatLngBounds latLngBounds = new LatLngBounds.Builder()
                                                    .include(origin)
                                                    .include(destination)
                                                    .build();

                                            addPickupmarkerWithDuration(duration, origin);
                                            addDriverMarker(destination);

                                            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 160));
                                            mMap.moveCamera(CameraUpdateFactory.zoomTo(mMap.getCameraPosition().zoom - 1));

                                            initDriverForMoving(event.getTripIp(), tripPlanModel);

                                            //load driver avatar
                                            Glide.with(requestdriverActivity.this)
                                                    .load(tripPlanModel.getDriverInfoModel().getAvatar())
                                                    .into(img_driver);
                                            txt_driver_name.setText(tripPlanModel.getDriverInfoModel().getName());


                                            confirm_pickup_layout.setVisibility(View.GONE);
                                            confirm_uber_layout.setVisibility(View.GONE);
                                            driver_info_layout.setVisibility(View.VISIBLE);


                                        } catch (Exception e) {
                                            //  Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                            Toast.makeText(requestdriverActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    })
                            );


                        } else
                            Snackbar.make(main_layout, getString(R.string.trip_not_found), Snackbar.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        Snackbar.make(main_layout, error.getMessage(), Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    private void initDriverForMoving(String tripIp, TripPlanModel tripPlanModel) {
        driverOldPosition = new StringBuilder()
                .append(tripPlanModel.getCurrentLat())
                .append(",")
                .append(tripPlanModel.getCurrentLng())
                .toString();

        FirebaseDatabase.getInstance()
                .getReference(Common.TRIP)
                .child(tripIp)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        TripPlanModel newData = snapshot.getValue(TripPlanModel.class);
                        String driverNewLocation = new StringBuilder()
                                .append(newData.getCurrentLat())
                                .append(",")
                                .append(newData.getCurrentLng())
                                .toString();

                        if (!driverOldPosition.equals(driverNewLocation)) //if not equal
                            moveMarkerAnimation(destinationMarker, driverOldPosition, driverNewLocation);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        Snackbar.make(main_layout, error.getMessage(), Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    private void moveMarkerAnimation(Marker marker, String from, String to) {

        //Request API
        compositeDisposable.add(iGoogleAPI.getDirection("driving",
                "less_driving",
                from, to,
                getString(R.string.google_maps_key))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(returnResult -> {
                    Log.d("API_RETURN", returnResult);

                    try {
                        //parse json
                        JSONObject jsonObject = new JSONObject(returnResult);
                        JSONArray jsonArray = jsonObject.getJSONArray("routes");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject route = jsonArray.getJSONObject(i);
                            JSONObject poly = route.getJSONObject("overview_polyline");
                            String polyline = poly.getString("points");
                            polilineList = Common.decodePoly(polyline);

                        }

                        blackPolylineOptions = new PolylineOptions();
                        blackPolylineOptions.color(Color.BLACK);
                        blackPolylineOptions.width(12);
                        blackPolylineOptions.startCap(new SquareCap());
                        blackPolylineOptions.jointType(JointType.ROUND);
                        blackPolylineOptions.addAll(polilineList);
                        blackPolyline = mMap.addPolyline(blackPolylineOptions);


                        JSONObject object = jsonArray.getJSONObject(0);
                        JSONArray legs = object.getJSONArray("legs");
                        JSONObject legObject = legs.getJSONObject(0);

                        JSONObject time = legObject.getJSONObject("duration");
                        String duration = time.getString("text");

                        JSONObject distanceEstimate = legObject.getJSONObject("distance");
                        String distance = distanceEstimate.getString("text");

                        Bitmap bitmap = Common.createiconWithDuration(requestdriverActivity.this, duration);
                        originMarker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));

                        //Moving
                        handler = new Handler();
                        index = -1;
                        next = 1;
                        handler.postDelayed(() -> {

                            if (index < polilineList.size() - 2) {
                                index++;
                                next = index;
                                start = polilineList.get(index);
                                end = polilineList.get(next);
                            }
                            ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 1);
                            valueAnimator.setDuration(1500);
                            valueAnimator.setInterpolator(new LinearInterpolator());
                            valueAnimator.addUpdateListener(valueAnimator1 -> {
                                v = valueAnimator1.getAnimatedFraction();
                                lng = v * end.longitude + (1 - v) * start.longitude;
                                lat = v * end.latitude + (1 - v) * start.latitude;
                                LatLng newPos = new LatLng(lat, lng);
                                marker.setPosition(newPos);
                                marker.setAnchor(0.5f, 0.5f);

                                marker.setRotation(Common.getBearing(start, newPos));
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(newPos));

                            });

                            valueAnimator.start();
                            if (index < polilineList.size() - 2)
                                handler.postDelayed((Runnable) this, 1500);
                            else if (index < polilineList.size() - 1) {

                            }

                        }, 1500);

                        driverOldPosition = to; //set new driver position


                    } catch (Exception e) {
                        Snackbar.make(main_layout, e.getMessage(), Snackbar.LENGTH_SHORT).show();
                    }
                }, throwable -> {
                    if (throwable != null) {
                        Snackbar.make(main_layout, throwable.getMessage(), Snackbar.LENGTH_SHORT).show();
                    }
                })
        );

    }

    private void addDriverMarker(LatLng destination) {
        destinationMarker = mMap.addMarker(new MarkerOptions().position(destination).flat(true)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
    }

    private void addPickupmarkerWithDuration(String duration, LatLng origin) {
        Bitmap icon = Common.createiconWithDuration(this, duration);
        originMarker = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(icon)).position(origin));

    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSelectPlaceEvent(SelectPlaceEvent event) {
        selectPlaceEvent = event;
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onDeclineRequestEvent(DeclineRequestFromDriver event) {

        if (lastDriver != null) {
            Common.driversFound.get(lastDriver.getKey());
            //driver has been decline request ,just find new driver
            findNearByDriver(selectPlaceEvent);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onDeclineRequestAndRemoveTripEvent(DeclineRequestAndRemoveTripFromDriver event) {

        if (lastDriver != null) {
            Common.driversFound.get(lastDriver.getKey());
            //driver has been decline request ,just finish this activity
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRequestdriverBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void init() {
        ButterKnife.bind(this);
        iGoogleAPI = RetrofitClient.getInstance().create(IGoogleAPI.class);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        drawPath(selectPlaceEvent);


        try {
            boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.uber_maps_style));
            if (!success) {
                Toast.makeText(this, "Load map style failed", Toast.LENGTH_SHORT).show();
            }
        } catch (Resources.NotFoundException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void drawPath(SelectPlaceEvent selectPlaceEvent) {
        //Request API
        compositeDisposable.add(iGoogleAPI.getDirection("driving",
                "less_driving",
                selectPlaceEvent.getOriginString(), selectPlaceEvent.getDestinationString(),
                getString(R.string.google_maps_key))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(returnResult -> {
                    Log.d("API_RETURN", returnResult);

                    try {
                        //parse json
                        JSONObject jsonObject = new JSONObject(returnResult);
                        JSONArray jsonArray = jsonObject.getJSONArray("routes");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject route = jsonArray.getJSONObject(i);
                            JSONObject poly = route.getJSONObject("overview_polyline");
                            String polyline = poly.getString("points");
                            polilineList = Common.decodePoly(polyline);

                        }

                        polylineOptions = new PolylineOptions();
                        polylineOptions.color(Color.GRAY);
                        polylineOptions.width(12);
                        polylineOptions.startCap(new SquareCap());
                        polylineOptions.jointType(JointType.ROUND);
                        polylineOptions.addAll(polilineList);
                        greyPolyline = mMap.addPolyline(polylineOptions);

                        blackPolylineOptions = new PolylineOptions();
                        blackPolylineOptions.color(Color.BLACK);
                        blackPolylineOptions.width(12);
                        blackPolylineOptions.startCap(new SquareCap());
                        blackPolylineOptions.jointType(JointType.ROUND);
                        blackPolylineOptions.addAll(polilineList);
                        blackPolyline = mMap.addPolyline(blackPolylineOptions);


                        // Animator

                        ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 1);
                        valueAnimator.setDuration(3000);
                        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
                        valueAnimator.setInterpolator(new LinearInterpolator());
                        valueAnimator.addUpdateListener(valueAnimator1 -> {

                            List<LatLng> points = greyPolyline.getPoints();
                            int percentValue = (int) valueAnimator.getAnimatedValue();
                            int size = points.size();
                            int newPoints = (int) (size * (percentValue / 100.0f));
                            List<LatLng> p = points.subList(0, newPoints);
                            blackPolyline.setPoints(p);

                        });

                        valueAnimator.start();

                        LatLngBounds latLngBounds = new LatLngBounds.Builder()
                                .include(selectPlaceEvent.getOrigin())
                                .include(selectPlaceEvent.getDestination())
                                .build();

                        //add car for origin
                        JSONObject object = jsonArray.getJSONObject(0);
                        JSONArray legs = object.getJSONArray("legs");
                        JSONObject legObject = legs.getJSONObject(0);

                        JSONObject time = legObject.getJSONObject("duration");
                        String duration = time.getString("text");

                        String start_address = legObject.getString("start_address");
                        String end_address = legObject.getString("end_address");

                        addOriginMarker(duration, start_address);

                        addDestination(end_address);

                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 160));
                        mMap.moveCamera(CameraUpdateFactory.zoomTo(mMap.getCameraPosition().zoom - 1));


                    } catch (Exception e) {
                        //  Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_SHORT).show();
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
        );
    }

    private void addDestination(String end_address) {
        View view = getLayoutInflater().inflate(R.layout.destination_info_window, null);
        TextView txt_destination = view.findViewById(R.id.txt_destination);

        txt_destination.setText(Common.formatAddress(end_address));

        //create marker for icon
        IconGenerator generator = new IconGenerator(this);
        generator.setContentView(view);
        generator.setBackground(new ColorDrawable(Color.TRANSPARENT));
        Bitmap icon = generator.makeIcon();

        originMarker = mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(icon))
                .position(selectPlaceEvent.getOrigin()));

    }

    private void addOriginMarker(String duration, String start_address) {
        View view = getLayoutInflater().inflate(R.layout.origin_info_window, null);

        TextView txt_time = view.findViewById(R.id.txt_time);
        txt_origin = view.findViewById(R.id.txt_origin);

        txt_time.setText(Common.formateDuration(duration));
        txt_origin.setText(Common.formatAddress(start_address));

        //create marker for icon
        IconGenerator generator = new IconGenerator(this);
        generator.setContentView(view);
        generator.setBackground(new ColorDrawable(Color.TRANSPARENT));
        Bitmap icon = generator.makeIcon();

        originMarker = mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(icon))
                .position(selectPlaceEvent.getOrigin()));
    }
}