package com.quicktaxi;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        View.OnClickListener {

    final DecimalFormat FORMATTER = new DecimalFormat("$#,###.00");

    GoogleMap googleMap;

    BottomSheetBehavior bottomSheetBehavior;
    LinearLayout linearLayoutBSheet;

    Location currentLocation;
    LatLng whereTo;

    EditText etWhereTo;
    ImageView btnGo;

    TextView txtArriveTime;
    TextView txtTotalAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        init();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        etWhereTo.setText("Mexico");
    }

    void init() {
        linearLayoutBSheet = findViewById(R.id.bottomSheet);
        bottomSheetBehavior = BottomSheetBehavior.from(linearLayoutBSheet);
        btnGo = findViewById(R.id.btnGo);
        etWhereTo = findViewById(R.id.etWhereTo);
        txtArriveTime = findViewById(R.id.txtArriveTime);
        txtTotalAmount = findViewById(R.id.txtTotalAmount);

        btnGo.setOnClickListener(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        currentLocation = fetchLastLocation();
        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("I Am Here.");

        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5f));
        googleMap.addMarker(markerOptions);
    }

    void setMarker() {
        MarkerOptions markerOptions = new MarkerOptions().position(whereTo).title("I Am Going Here.");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

        googleMap.animateCamera(CameraUpdateFactory.newLatLng(whereTo));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(whereTo, 5f));
        googleMap.addMarker(markerOptions);

        //667 km - m
        double d = distance(currentLocation.getLatitude(), currentLocation.getLongitude(), whereTo.latitude, whereTo.longitude, "K");
        double km = d;
        d *= 1_000;

        // 90 km/h -> m/s
        int kmPh = 90 * 1000 / 3600;

        int secs = (int) (d / kmPh);

        txtArriveTime.setText(Utils.addSecondsToJavaUtilDate(secs));

        // Km * Litre
        int kmL = (int) (km / 12);

        // Price gas $18.00
        double totalAmount = kmL * 18;
        txtTotalAmount.setText(FORMATTER.format(totalAmount));
    }

    Location fetchLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            Location lastKnownLocationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocationGPS != null) {
                return lastKnownLocationGPS;
            } else {
                return locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            }
        } else {
            return null;
        }
    }

    //https://maps.googleapis.com/maps/api/directions/json?origin=28.64660502706282,-106.09835844201855&destination=19.4326296,-99.1331785&key=AIzaSyDUIHTcwBPAHOJ8aFkfT-qs8Ku1h5AYNdw
    double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        } else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            if (unit.equals("K")) {
                dist = dist * 1.609344;
            } else if (unit.equals("N")) {
                dist = dist * 0.8684;
            }
            return dist;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnGo:
                String address = etWhereTo.getText().toString();

                List<Address> addressList = null;
                if (!TextUtils.isEmpty(address)) {
                    Geocoder geocoder = new Geocoder(MapsActivity.this);
                    try {
                        addressList = geocoder.getFromLocationName(address, 6);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (addressList != null)
                        for (int i = 0; i < addressList.size(); i++) {
                            Address userAddress = addressList.get(i);
                            whereTo = new LatLng(userAddress.getLatitude(), userAddress.getLongitude());

                            setMarker();
                        }

                }

                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                break;
        }
    }
}