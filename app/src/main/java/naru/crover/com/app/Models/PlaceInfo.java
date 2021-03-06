package naru.crover.com.app.Models;

import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;

public class PlaceInfo {
    private String name;
    private String address;
    private String phoneNumber;
    private String id;
    private Uri webSiteUri;
    private LatLng latLng;
    private float rating;
    private String attribution;

    public PlaceInfo() {

    }

    public PlaceInfo(String name, String address, String phoneNumber, String id, Uri webSiteUri, LatLng latLng, float rating, String attribution) {
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.id = id;
        this.webSiteUri = webSiteUri;
        this.latLng = latLng;
        this.rating = rating;
        this.attribution = attribution;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Uri getWebSiteUri() {
        return webSiteUri;
    }

    public void setWebSiteUri(Uri webSiteUri) {
        this.webSiteUri = webSiteUri;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getAttribution() {
        return attribution;
    }

    public void setAttribution(String attribution) {
        this.attribution = attribution;
    }
}
