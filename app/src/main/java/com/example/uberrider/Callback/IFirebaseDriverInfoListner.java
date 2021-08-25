package com.example.uberrider.Callback;

import com.example.uberrider.model.DriverGeoModel;
import com.example.uberrider.model.DriverInfoModel;

public interface IFirebaseDriverInfoListner {
    void onDriverInfoLoadSuccess(DriverGeoModel driverGeoModel);
}
