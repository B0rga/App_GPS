package com.example.gps;

import static com.example.gps.utils.Constants.MAPVIEW_BUNDLE_KEY;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public class gnss_view extends AppCompatActivity implements OnMapReadyCallback {
    private static final long DEFAULT_UPDATE_INTERVAL = 30;
    private static final long FAST_UPDATE_INTERVAL = 50;
    private static final int PERMISSIONS_FINE_LOCATION = 99;
    private GoogleMap map;
    private MapView mapView;
    private TextView tv_nome, tv_lat, tv_lon, tv_alt, tv_acu, tv_vel, tv_endereco, tv_sensor;
    private Switch sw_updates, sw_gps;

    // Configuração para os dados de localização
    LocationRequest locationRequest;

    // API da google responsável pelos serviços de localização
    FusedLocationProviderClient fusedLocationProviderClient;

    LocationCallback locationCallback;

    // Variável que define se a localização está sendo atualizada
    boolean updateOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gnss_view);

        tv_nome = findViewById(R.id.tv_nome);
        tv_lat = findViewById(R.id.tv_lat);
        tv_lon = findViewById(R.id.tv_lon);
        tv_alt = findViewById(R.id.tv_alt);
        tv_acu = findViewById(R.id.tv_acu);
        tv_vel = findViewById(R.id.tv_vel);
        tv_endereco = findViewById(R.id.tv_endereco);
        tv_sensor = findViewById(R.id.tv_sensor);
        sw_updates = findViewById(R.id.sw_updates);
        sw_gps = findViewById(R.id.sw_gps);
        mapView = findViewById(R.id.mapView);

        // Definindo todas as propriedades do LocationRequest
        locationRequest = new LocationRequest();

        // Definindo a frequência mínima de atualização da localização
        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL);

        // Definindo a frequência máxima de atualização da localização
        locationRequest.setFastestInterval(1000 * FAST_UPDATE_INTERVAL);

        locationRequest.setPriority(locationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        // Evento que é acionado após cada intervalo de atualização
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                // Salvar a localização
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    DefinirValores(location);
                    DefinirMarcador(location);
                }
            }
        };

        // Adicionando um listener ao switch de GPS/Sinal de Telefone ou Wifi
        sw_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sw_gps.isChecked()){
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    tv_sensor.setText("Usando sensor GPS");
                }
                else{
                    locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                    tv_sensor.setText("Usando sinal de telefone ou wifi");
                }
            }
        });

        sw_updates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sw_updates.isChecked()){
                    // Ligar atualização do local em tempo real
                    StartLocationUpdates();
                }else{
                    // Desligar
                    StopLocationUpdates();
                }
            }
        });
        ReceberNome();
        UpdateGPS();
        iniciarGoogleMaps(savedInstanceState);
    }

    private void ReceberNome(){
        Bundle extras = getIntent().getExtras();
        String meuNome = extras.getString("nome");
        tv_nome.setText("Seja bem-vindo(a) "+meuNome);
    }

    private void StartLocationUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        UpdateGPS();
    }

    private void StopLocationUpdates() {
        tv_lat.setText("Sem rastreamento");
        tv_lon.setText("Sem rastreamento");
        tv_alt.setText("Sem rastreamento");
        tv_acu.setText("Sem rastreamento");
        tv_vel.setText("Sem rastreamento");
        tv_endereco.setText("Sem rastreamento");
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case PERMISSIONS_FINE_LOCATION:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    UpdateGPS();
                }else{
                    Toast.makeText(this, "As permissões devem ser aceitas para que a aplicação funcione apropriedamente ", Toast.LENGTH_SHORT).show();
                    finish();
                }
            break;
        }
    }

    private void UpdateGPS(){

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(gnss_view.this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // Permissão da localização liberada
                    if(location!=null){
                        DefinirValores(location);
                        DefinirMarcador(location);

                    }
                }
            });
        }
        else{
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
            }
        }
    }

    public void DefinirValores(Location location){

        // Atualizar os dados na tela
        tv_lat.setText(String.valueOf(location.getLatitude()));
        tv_lon.setText(String.valueOf(location.getLongitude()));
        tv_acu.setText(String.valueOf(location.getAccuracy()));

        if(location.hasAltitude()){
            tv_alt.setText(String.valueOf(location.getAltitude()));
        }else{
            tv_alt.setText("Indisponível");
        }

        if(location.hasSpeed()){
            tv_vel.setText(String.valueOf(location.getSpeed()));
        }else{
            tv_vel.setText("Indisponível");
        }

        Geocoder geocoder = new Geocoder(gnss_view.this);
        try{
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            tv_endereco.setText(addresses.get(0).getAddressLine(0));
        }catch (Exception e){
            tv_endereco.setText("Endereço não encontrado");
        }

    }

    private void iniciarGoogleMaps(Bundle savedInstanceState){
        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
        }
    }

    public void DefinirMarcador(Location location){
        LatLng local = new LatLng(location.getLatitude(), location.getLongitude());
        map.addMarker(new MarkerOptions().position(local).title("New Marker"));
        map.moveCamera(CameraUpdateFactory.newLatLng(local));
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}