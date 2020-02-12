package com.gkouskos.unipitouristapp;

import android.location.Address;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.location.Geocoder;

import com.google.android.gms.common.internal.Objects;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AddPoiActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    Toolbar addpoi_toolbar;
    ActionBar actionBar;
    Button addButton;
    EditText title, address, description, city, zipCode;
    Spinner categorySpinner;
    String catStr;
    FirebaseDatabase fbDatabase;
    DatabaseReference myref;
    Geocoder gc;
    ArrayList<Poi> pois = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_poi);
        addpoi_toolbar= findViewById(R.id.addpoi_toolbar);
        setSupportActionBar(addpoi_toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        fbDatabase = FirebaseDatabase.getInstance();
        myref = fbDatabase.getReference("pois");

        title = findViewById(R.id.title);
        address = findViewById(R.id.address);
        categorySpinner = findViewById(R.id.category);
        addButton = findViewById(R.id.add_button);
        description = findViewById(R.id.description);
        city = findViewById(R.id.city);
        zipCode = findViewById(R.id.zip_code);

        //Select from a spinner the category, not to make a spelling mistake
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.categories,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
        categorySpinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String item = parent.getItemAtPosition(position).toString();
        catStr = item;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
    public void addPoi(View view) {
        String titleStr = title.getText().toString();
        String descriptionStr = description.getText().toString();
        String addressStr = address.getText().toString();
        String cityStr = city.getText().toString();
        String zipCodeStr = zipCode.getText().toString();

        String fullAddressStr = addressStr + ", " + cityStr + ", " + zipCodeStr;
        // Translate the address that the user gave to latitude and longitude, using Geocoder
        gc = new Geocoder(this);
        if (gc.isPresent()) {
            try {
                List<Address> list = gc.getFromLocationName(fullAddressStr, 1);
                Address address = list.get(0);

                double lat = address.getLatitude();
                double lng = address.getLongitude();

                Poi poi = new Poi(
                        titleStr,
                        descriptionStr,
                        catStr,
                        lat,
                        lng
                );
                myref.child(poi.title).setValue(poi);
                Toast.makeText(this, poi.title + " has been successfully added", Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        title.getText().clear();
        address.getText().clear();
        city.getText().clear();
        zipCode.getText().clear();
        description.getText().clear();
    }
}
