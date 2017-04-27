package com.ensipoly.project.utils;

public class LatLng  {
    public double latitude;
    public double longitude;

    public LatLng(com.google.android.gms.maps.model.LatLng latLng) {
        this.latitude = latLng.latitude;
        this.longitude = latLng.longitude;
    }

    public LatLng(){

    }

    public com.google.android.gms.maps.model.LatLng convert(){
        return new com.google.android.gms.maps.model.LatLng(latitude,longitude);
    }
}
