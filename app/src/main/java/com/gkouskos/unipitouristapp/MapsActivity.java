 /*
    Logic:
        The app will inform us if we are near a POI of our interest.

    More specifically:
        *  If the app in open, it will get the location of the mobile via GPS.
        *  When needed run-time permissions will be activated.
        *  It will be a List, on-line on Firebase, with POIs where every point will have the below data:
            1. Title of point
            2. Concise description
            3. Category (e.g. entertainment, food, nightlife)
            4. Coordinates
        *  The user will define the distance in meters in which he would like to get informed when he is close
            to a POI or POIs.
        *  Optionally, if the user will be located inside a range of a POI:
            1. In a new activity all the details of the POI will be shown, in addition, the user could
                hear the info via TextToSpeech. Finally, he should use a graphic (e.g. photo) of the POI, which will
                be stored locally in the app.
            2. The event will be stored in a DB locally or not (timestamp, POI, location)
        *  There will be an option of the statistics to be shown, from the data that the app will have collected
            (Those could be, how many times he has gone to a POI, or what POIs he prefer)
 */

package com.gkouskos.unipitouristapp;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static android.widget.Toast.makeText;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, LocationListener, NavigationView.OnNavigationItemSelectedListener {
    private GoogleMap mMap;
    boolean check = false;
    boolean setAlarm = false;
    NumberPicker numberPicker;
    int pickedValue;
    Snackbar snackbar;

    // Location stuff
    final static int REQUESTCODE = 325;
    LocationManager locationManager;
    Location lastLocation, poiLocation;
    Double longitude, latitude;
    static double distance;
    Marker m;

    // Firebase stuff
    FirebaseDatabase fbdatabase;
    DatabaseReference myref;
    Toolbar myToolbar;

    // Material stuff
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        fbdatabase = FirebaseDatabase.getInstance();
        myref = fbdatabase.getReference("pois");
        myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                myToolbar,
                R.string.nav_drawer_open,
                R.string.nav_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        myref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                double aLat, aLong;
                LatLng coordinates;
                String aTitle, aDesc;
                MarkerOptions markerOptions;

                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    aLat = (double) snapshot.child("latitude").getValue();
                    aLong = (double) snapshot.child("longitude").getValue();
                    coordinates = new LatLng(aLat, aLong);
                    aTitle = (String) snapshot.child("title").getValue();
                    aDesc = (String) snapshot.child("description").getValue();
                    markerOptions = new MarkerOptions().position(coordinates).title(aTitle).snippet(aDesc);
                    mMap.addMarker(markerOptions);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Failed to read value
                Log.w("Poi", "Failed to read value", databaseError.toException());
            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUESTCODE);
        else
        {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0 ,0, this);
        }
        lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        poiLocation = new Location(LocationManager.GPS_PROVIDER);
        latitude = lastLocation.getLatitude();
        longitude = lastLocation.getLongitude();
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        LatLng myPosition = new LatLng(latitude, longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myPosition));
        if (mMap != null) {
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        // Set the position of the place equal to the marker the the user clicked and get the title also
        poiLocation.setLatitude(marker.getPosition().latitude);
        poiLocation.setLongitude(marker.getPosition().longitude);
        m = marker;
        m.getTitle();

        return false;
    }

    @Override
    public void onLocationChanged(Location location) {
        // Monitor the distance and alarm the user when he is close to the POI
        distance = poiLocation.distanceTo(location);

        if (setAlarm) {
            if (distance < pickedValue && check) {
                check = false;
                snackbar = Snackbar.make(findViewById(android.R.id.content), "Show details about the Place", Snackbar.LENGTH_LONG);
                snackbar.setAction("GO", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MapsActivity.this, Details.class);
                        intent.putExtra("Title", m.getTitle());
                        Toast.makeText(MapsActivity.this, m.getTitle(), Toast.LENGTH_LONG).show();
                        startActivity(intent);
                    }
                });
                snackbar.show();
            } else if (distance >= pickedValue && !check) {
                check = true;
                Toast.makeText(MapsActivity.this, "You are getting away", Toast.LENGTH_LONG).show();
            }
        }
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override
    public void onProviderEnabled(String provider) {}
    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //TODO: Search for a Poi by the name in the SearchView field
        getMenuInflater().inflate(R.menu.options_menu,menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                // Get all the Markers on the map
                return true;
            case R.id.action_places:
                // Show all markers in the database
                mMap.clear();
                myref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        double aLat, aLong;
                        LatLng coordinates;
                        String aTitle, aDesc;

                        for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                            aLat = (double) snapshot.child("latitude").getValue();
                            aLong = (double) snapshot.child("longitude").getValue();
                            coordinates = new LatLng(aLat, aLong);
                            aTitle = (String) snapshot.child("title").getValue();
                            aDesc = (String) snapshot.child("description").getValue();
                            mMap.addMarker(new MarkerOptions().position(coordinates).title(aTitle).snippet(aDesc));
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Failed to read value
                        Log.w("Poi", "Failed to read value", databaseError.toException());
                    }
                });
                return true;
            case R.id.set_distance:
                //TODO: Set the distance in meters
                final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
                LayoutInflater inflater = this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.set_distance_layout, null);
                builder.setView(dialogView);
                numberPicker = dialogView.findViewById(R.id.np);
                int minValue = 1;
                int maxValue = 20;
                final int step = 50;

                String[] numberValues = new String[maxValue - minValue + 1];
                for (int i = 0; i <= maxValue-minValue; i++)
                    numberValues[i] = String.valueOf((minValue + i)*step);

                numberPicker.setMaxValue(maxValue);
                numberPicker.setMinValue(minValue);
                numberPicker.setWrapSelectorWheel(false);
                numberPicker.setDisplayedValues(numberValues);

                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setAlarm = true;
                        pickedValue = numberPicker.getValue() * step;
                    }
                });
                builder.show();

                return true;
            case R.id.disable_alarm:
                setAlarm = false;
                return true;
                default:
                    return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            //TODO: Show the markers grouping by category
            case R.id.coffee:
                break;
            case R.id.shopping:
                break;
            case R.id.nightlife:
                break;
            case R.id.restaurant:
                break;
            case R.id.art:
                break;
            case R.id.family:
                break;
            case R.id.add_poi:
                Intent intent2 = new Intent(this, AddPoiActivity.class);
                startActivity(intent2);
                break;
            case R.id.clear_map:
                mMap.clear();
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grandResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grandResults);
        if (grandResults[0] == PackageManager.PERMISSION_GRANTED)
            Toast.makeText(getApplicationContext(), "GPS Granded", Toast.LENGTH_SHORT).show();
        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        else
            Toast.makeText(getApplicationContext(), "The app needs GPS", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else
            super.onBackPressed();
    }
}
