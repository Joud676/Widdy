package com.example.widdy;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class PickLocationActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "PickLocation";
    private static final int LOCATION_PERMISSION_REQUEST = 100;

    private GoogleMap mMap;
    private Marker selectedMarker;
    private Button confirmBtn;
    private ImageView searchBtn, backButton;
    private EditText searchEt;
    private TextView selectedLocationText;
    private FloatingActionButton myLocationBtn;
    private FusedLocationProviderClient fusedLocationClient;
    private Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_location);

        initViews();
        setupListeners();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        geocoder = new Geocoder(this, new Locale("ar", "SA"));

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void initViews() {
        confirmBtn = findViewById(R.id.confirmBtn);
        searchBtn = findViewById(R.id.searchBtn);
        searchEt = findViewById(R.id.searchLocationEt);
        selectedLocationText = findViewById(R.id.selectedLocationText);
        myLocationBtn = findViewById(R.id.myLocationBtn);
        backButton = findViewById(R.id.backButton);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());

        confirmBtn.setOnClickListener(v -> {
            if (selectedMarker != null) {
                LatLng latLng = selectedMarker.getPosition();
                String locationName = getAddressFromLatLng(latLng);

                Intent intent = new Intent();
                intent.putExtra("lat", latLng.latitude);
                intent.putExtra("lng", latLng.longitude);
                intent.putExtra("address", locationName);
                setResult(RESULT_OK, intent);
                finish();
            } else {
                Toast.makeText(this, "اختر موقعاً على الخريطة أولاً", Toast.LENGTH_SHORT).show();
            }
        });

        searchBtn.setOnClickListener(v -> searchLocation());

        searchEt.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                searchLocation();
                return true;
            }
            return false;
        });

        myLocationBtn.setOnClickListener(v -> goToMyLocation());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        LatLng riyadh = new LatLng(24.7136, 46.6753);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(riyadh, 12));

        mMap.setOnMapClickListener(latLng -> selectLocation(latLng));

        checkLocationPermission();
    }

    private void searchLocation() {
        String locationName = searchEt.getText().toString().trim();

        if (locationName.isEmpty()) {
            Toast.makeText(this, "أدخل اسم موقع للبحث", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "جاري البحث...", Toast.LENGTH_SHORT).show();

        try {
            List<Address> addresses = geocoder.getFromLocationName(locationName, 5);

            if (addresses != null && !addresses.isEmpty()) {
                Address bestMatch = null;

                for (Address address : addresses) {
                    if (address.getCountryCode() != null &&
                            address.getCountryCode().equalsIgnoreCase("SA")) {
                        bestMatch = address;
                        break;
                    }
                }

                if (bestMatch == null) {
                    bestMatch = addresses.get(0);
                }

                LatLng latLng = new LatLng(bestMatch.getLatitude(), bestMatch.getLongitude());
                selectLocation(latLng);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

                String addressLine = bestMatch.getAddressLine(0);
                if (addressLine != null) {
                    selectedLocationText.setText(addressLine);
                } else {
                    selectedLocationText.setText(locationName);
                }

                Toast.makeText(this, "تم العثور على الموقع", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this, "لم يتم العثور على نتائج. جرب البحث بطريقة أخرى", Toast.LENGTH_LONG).show();
                Log.d(TAG, "No results for: " + locationName);
            }

        } catch (IOException e) {
            Log.e(TAG, "Geocoder error: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "خطأ في البحث. تأكد من اتصال الإنترنت", Toast.LENGTH_LONG).show();
        }
    }

    private void selectLocation(LatLng latLng) {
        if (selectedMarker != null) {
            selectedMarker.remove();
        }

        selectedMarker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("الموقع المختار"));

        String address = getAddressFromLatLng(latLng);
        selectedLocationText.setText(address);

        Toast.makeText(this, "تم اختيار الموقع", Toast.LENGTH_SHORT).show();
    }

    private String getAddressFromLatLng(LatLng latLng) {
        try {
            List<Address> addresses = geocoder.getFromLocation(
                    latLng.latitude,
                    latLng.longitude,
                    1
            );

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String addressLine = address.getAddressLine(0);
                return addressLine != null ? addressLine : "الموقع المختار";
            }
        } catch (IOException e) {
            Log.e(TAG, "Error getting address: " + e.getMessage());
        }

        return String.format(Locale.getDefault(),
                "%.4f, %.4f", latLng.latitude, latLng.longitude);
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        }
    }

    private void goToMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "يجب السماح بالوصول للموقع", Toast.LENGTH_SHORT).show();
            checkLocationPermission();
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        selectLocation(myLocation);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15));
                    } else {
                        Toast.makeText(this, "تعذر تحديد موقعك الحالي", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationPermission();
            }
        }
    }
}