package com.example.albaayo;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.albaayo.location.LocationService;
import com.example.http.Http;
import com.example.http.dto.Id;
import com.example.http.dto.RequestCommuteDto;

import net.daum.mf.map.api.CameraUpdateFactory;
import net.daum.mf.map.api.MapCircle;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapPointBounds;
import net.daum.mf.map.api.MapView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import lombok.SneakyThrows;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WorkerCommute extends AppCompatActivity implements MapView.CurrentLocationEventListener, MapView.MapViewEventListener {

    private GpsTracker gpsTracker;
    private static final int MY_PERMISSION_LOCATION = 1111;
    private static final String LOG_TAG = "MainActivity";
    private MapView mapView;
    private ViewGroup mapViewContainer;
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION};
    private Long companyId;
    private String companyName, companyLocation;
    private Geocoder geocoder;
    private double latitude, longitude;
    private double companyLatitude, companyLongitude;
    private List<Address> fromLocationName;
    private Address companyAddress;
    private SharedPreferences sf;
    private SharedPreferences.Editor editor;

    @SneakyThrows
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.worker_commute);
        checkPermission();

        initData();

        mapCircle();


        goToWork();

        offWork();

//        goToWorkButton.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View arg0)
//            {
//
//                gpsTracker = new GpsTracker(WorkerCommute.this);
//
//                double latitude = gpsTracker.getLatitude();
//                double longitude = gpsTracker.getLongitude();
//
//                String address = getCurrentAddress(latitude, longitude);
////                textview_address.setText(address);
//
//                Toast.makeText(WorkerCommute.this, "???????????? \n?????? " + latitude + "\n?????? " + longitude, Toast.LENGTH_LONG).show();
//
//            }
//        });
    }

    private void offWork() {
        Button offWorkButton = findViewById(R.id.off_work);
        offWorkButton.setOnClickListener(v -> {

            new AlertDialog.Builder(WorkerCommute.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                    .setMessage("?????????????????????????")
                    .setNegativeButton("??????", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which){
                            if (latitude + 0.003 >= companyLatitude && latitude - 0.003 <= companyLatitude) {
                                if (longitude + 0.003 >= companyLongitude && longitude - 0.003 <= companyLongitude) {
                                    RequestCommuteDto request = RequestCommuteDto.builder().workerId(Id.getInstance().getId()).companyId(companyId).build();
                                    Call<Void> call = Http.getInstance().getApiService()
                                            .offWork(Id.getInstance().getAccessToken(), request);
                                    call.enqueue(new Callback<Void>() {
                                        @SneakyThrows
                                        @Override
                                        public void onResponse(Call<Void> call, Response<Void> response) {
                                            if (response.code() == 401) {
                                                Id.getInstance().setAccessToken(response.headers().get("Authorization"));
                                                editor.putString("accessToken", response.headers().get("Authorization"));
                                                editor.commit();

                                                Call<Void> reCall = Http.getInstance().getApiService()
                                                        .offWork(Id.getInstance().getAccessToken(), request);
                                                reCall.enqueue(new Callback<Void>() {
                                                    @Override
                                                    public void onResponse(Call<Void> call, Response<Void> response) {
                                                        if (response.code() != 500) {
                                                            new AlertDialog.Builder(WorkerCommute.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                                                    .setMessage("?????? ??????")
                                                                    .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                                                                        public void onClick(DialogInterface dialog, int which){
                                                                            Intent stopLocationService =
                                                                                    new Intent(WorkerCommute.this, LocationService.class);
                                                                            stopService(stopLocationService);
                                                                            Call<Void> deleteCall =
                                                                                    Http.getInstance().getApiService()
                                                                                            .deleteLocation(Id.getInstance().getAccessToken(), Id.getInstance().getId());
                                                                            deleteCall.enqueue(new Callback<Void>() {
                                                                                @Override
                                                                                public void onResponse(Call<Void> call, Response<Void> response) {
                                                                                    if (response.code() == 401) {
                                                                                        Id.getInstance().setAccessToken(response.headers().get("Authorization"));
                                                                                        editor.putString("accessToken", response.headers().get("Authorization"));
                                                                                        editor.commit();

                                                                                        Call<Void> deleteCall =
                                                                                                Http.getInstance().getApiService()
                                                                                                        .deleteLocation(Id.getInstance().getAccessToken(), Id.getInstance().getId());
                                                                                        deleteCall.enqueue(new Callback<Void>() {
                                                                                            @Override
                                                                                            public void onResponse(Call<Void> call, Response<Void> response) {
                                                                                            }

                                                                                            @Override
                                                                                            public void onFailure(Call<Void> call, Throwable t) {
                                                                                                Toast.makeText(WorkerCommute.this, "???????????? ?????? ??????", Toast.LENGTH_SHORT).show();
                                                                                            }
                                                                                        });
                                                                                    }
                                                                                }

                                                                                @Override
                                                                                public void onFailure(Call<Void> call, Throwable t) {
                                                                                    Toast.makeText(WorkerCommute.this, "???????????? ?????? ??????", Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            });
                                                                        }
                                                                    })
                                                                    .show();
                                                        } else {
                                                            new AlertDialog.Builder(WorkerCommute.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                                                    .setMessage("????????? ?????? ???????????????.")
                                                                    .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                                                                        public void onClick(DialogInterface dialog, int which){
                                                                        }
                                                                    })
                                                                    .show();
                                                        }
                                                    }

                                                    @Override
                                                    public void onFailure(Call<Void> call, Throwable t) {
                                                        Toast.makeText(WorkerCommute.this, "???????????? ?????? ??????", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            } else if (response.code() != 500) {
                                                new AlertDialog.Builder(WorkerCommute.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                                        .setMessage("?????? ??????")
                                                        .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int which){
                                                                Intent stopLocationService =
                                                                        new Intent(WorkerCommute.this, LocationService.class);
                                                                stopService(stopLocationService);
                                                                Call<Void> deleteCall =
                                                                        Http.getInstance().getApiService()
                                                                                .deleteLocation(Id.getInstance().getAccessToken(), Id.getInstance().getId());
                                                                deleteCall.enqueue(new Callback<Void>() {
                                                                    @Override
                                                                    public void onResponse(Call<Void> call, Response<Void> response) {
                                                                        if (response.code() == 401) {
                                                                            Id.getInstance().setAccessToken(response.headers().get("Authorization"));
                                                                            editor.putString("accessToken", response.headers().get("Authorization"));
                                                                            editor.commit();

                                                                            Call<Void> deleteCall =
                                                                                    Http.getInstance().getApiService()
                                                                                            .deleteLocation(Id.getInstance().getAccessToken(), Id.getInstance().getId());
                                                                            deleteCall.enqueue(new Callback<Void>() {
                                                                                @Override
                                                                                public void onResponse(Call<Void> call, Response<Void> response) {

                                                                                }

                                                                                @Override
                                                                                public void onFailure(Call<Void> call, Throwable t) {

                                                                                }
                                                                            });
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onFailure(Call<Void> call, Throwable t) {
                                                                        Toast.makeText(WorkerCommute.this, "???????????? ?????? ??????", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                            }
                                                        })
                                                        .show();
                                            } else {
                                                new AlertDialog.Builder(WorkerCommute.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                                        .setMessage("????????? ?????? ???????????????.")
                                                        .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int which){
                                                            }
                                                        })
                                                        .show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<Void> call, Throwable t) {
                                            Toast.makeText(WorkerCommute.this, "???????????? ?????? ??????", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    new AlertDialog.Builder(WorkerCommute.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                            .setMessage("????????? ?????? ????????????.")
                                            .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which){
                                                }
                                            })
                                            .show();
                                }
                            } else {
                                new AlertDialog.Builder(WorkerCommute.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                        .setMessage("????????? ?????? ????????????.")
                                        .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which){
                                            }
                                        })
                                        .show();
                            }

                        }
                    })
                    .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();
        });
    }

    private void goToWork() {
        Button goToWorkButton = (Button) findViewById(R.id.go_to_work);
        goToWorkButton.setOnClickListener(v -> {

            new AlertDialog.Builder(WorkerCommute.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                    .setMessage("?????????????????????????")
                    .setNegativeButton("??????", new DialogInterface.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        public void onClick(DialogInterface dialog, int which){

                            if (latitude + 0.003 >= companyLatitude && latitude - 0.003 <= companyLatitude) {
                                if (longitude + 0.003 >= companyLongitude && longitude - 0.003 <= companyLongitude) {
                                    RequestCommuteDto request = RequestCommuteDto.builder().workerId(Id.getInstance().getId()).companyId(companyId).build();
                                    Call<Void> call = Http.getInstance().getApiService()
                                            .goToWork(Id.getInstance().getAccessToken(), request);
                                    call.enqueue(new Callback<Void>() {
                                        @SneakyThrows
                                        @Override
                                        public void onResponse(Call<Void> call, Response<Void> response) {
                                            if (response.code() == 401) {
                                                Id.getInstance().setAccessToken(response.headers().get("Authorization"));
                                                editor.putString("accessToken", response.headers().get("Authorization"));
                                                editor.commit();

                                                Call<Void> reCall = Http.getInstance().getApiService()
                                                        .goToWork(Id.getInstance().getAccessToken(), request);
                                                reCall.enqueue(new Callback<Void>() {
                                                    @Override
                                                    public void onResponse(Call<Void> call, Response<Void> response) {
                                                        if (response.code() != 500) {
                                                            new AlertDialog.Builder(WorkerCommute.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                                                    .setMessage("?????? ??????")
                                                                    .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                                                                        public void onClick(DialogInterface dialog, int which) {
                                                                            Intent startLocationService =
                                                                                    new Intent(WorkerCommute.this, LocationService.class);
                                                                            startLocationService.putExtra("companyId", companyId);
                                                                            startService(startLocationService);
                                                                        }
                                                                    })
                                                                    .show();
                                                        } else {
                                                            new AlertDialog.Builder(WorkerCommute.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                                                    .setMessage("????????? ?????? ???????????????.")
                                                                    .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                                                                        public void onClick(DialogInterface dialog, int which){
                                                                        }
                                                                    })
                                                                    .show();
                                                        }
                                                    }

                                                    @Override
                                                    public void onFailure(Call<Void> call, Throwable t) {
                                                        Toast.makeText(WorkerCommute.this, "???????????? ?????? ??????", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            } else if (response.code() != 500) {
                                                new AlertDialog.Builder(WorkerCommute.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                                        .setMessage("?????? ??????")
                                                        .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                Intent startLocationService =
                                                                        new Intent(WorkerCommute.this, LocationService.class);
                                                                startLocationService.putExtra("companyId", companyId);
                                                                startService(startLocationService);
                                                            }
                                                        })
                                                        .show();
                                            } else {
                                                new AlertDialog.Builder(WorkerCommute.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                                        .setMessage("????????? ?????? ???????????????.")
                                                        .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int which){
                                                            }
                                                        })
                                                        .show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<Void> call, Throwable t) {
                                            Toast.makeText(WorkerCommute.this, "???????????? ?????? ??????", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    new AlertDialog.Builder(WorkerCommute.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                            .setMessage("????????? ?????? ????????????.")
                                            .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which){
                                                }
                                            })
                                            .show();
                                }
                            } else {
                                new AlertDialog.Builder(WorkerCommute.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                        .setMessage("????????? ?????? ????????????.")
                                        .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which){
                                            }
                                        })
                                        .show();
                            }
                        }
                    })
                    .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();
        });
    }

    private void initData() throws IOException {
        sf = getSharedPreferences("sFile", MODE_PRIVATE);
        editor = sf.edit();

//        progressDialog.show();
        Intent intent = getIntent();
        companyId = intent.getLongExtra("companyId", 0);
        companyName = intent.getStringExtra("companyName");
        companyLocation = intent.getStringExtra("companyLocation");
        TextView header = findViewById(R.id.header_name_text);
        header.setText(companyName);
        //????????? ?????????
        // java code
        mapView = new MapView(this);
        mapViewContainer = (ViewGroup) findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);
        mapView.setMapViewEventListener(this);
        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);

        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();
        } else {
            checkRunTimePermission();
        }

        gpsTracker = new GpsTracker(WorkerCommute.this);

        latitude = gpsTracker.getLatitude();
        longitude = gpsTracker.getLongitude();

        geocoder = new Geocoder(this, Locale.getDefault());
        fromLocationName = geocoder.getFromLocationName(companyLocation, 1);
        companyAddress = fromLocationName.get(0);
        companyLatitude = companyAddress.getLatitude();
        companyLongitude = companyAddress.getLongitude();
    }

    private void mapCircle() {
        MapPoint point = MapPoint.mapPointWithGeoCoord(companyLatitude, companyLongitude);

        MapPOIItem myLocation = new MapPOIItem();
        myLocation.setItemName(companyName);
        myLocation.setTag(0);
        myLocation.setMapPoint(point);
        myLocation.setMarkerType(MapPOIItem.MarkerType.BluePin); // ???????????? ???????????? ????????? ?????? ??????
        myLocation.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // ????????? ???????????????, ????????? ?????? ???????????? ??????
        mapView.addPOIItem(myLocation);

        MapCircle circle = new MapCircle(
                MapPoint.mapPointWithGeoCoord(companyLatitude, companyLongitude), // center
                100, // radius
                Color.argb(128, 255, 0, 0), // strokeColor
                Color.argb(128, 255, 255, 0) // fillColor
        );
        circle.setTag(5678);
        mapView.addCircle(circle);

        MapPointBounds[] mapPointBoundsArray = { circle.getBound() };
        MapPointBounds mapPointBounds = new MapPointBounds(mapPointBoundsArray);
        int padding = 80; // px
        mapView.moveCamera(CameraUpdateFactory.newMapPointBounds(mapPointBounds, padding));
    }


    public String getCurrentAddress(double latitude, double longitude) {
        //????????????... GPS??? ????????? ??????
        geocoder = new Geocoder(this, Locale.getDefault());


        List<Address> addresses;

        try {

            List<Address> fromLocationName = geocoder.getFromLocationName(companyLocation, 1000);
            companyAddress = fromLocationName.get(0);
            System.out.println("address1.getAddressLine(0).toString(); = " + companyAddress.getAddressLine(0).toString());
            System.out.println("address1 = " + companyAddress.getLatitude());
            System.out.println("address1.getLongitude() = " + companyAddress.getLongitude());
            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {
            //???????????? ??????
            Toast.makeText(this, "???????????? ????????? ????????????", Toast.LENGTH_LONG).show();
            return "???????????? ????????? ????????????";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "????????? GPS ??????", Toast.LENGTH_LONG).show();
            return "????????? GPS ??????";

        }
        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "?????? ?????????", Toast.LENGTH_LONG).show();
            return "?????? ?????????";

        }
        Address address = addresses.get(0);
        return address.getAddressLine(0).toString()+"\n";

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapViewContainer.removeAllViews();
    }

    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint currentLocation, float accuracyInMeters) {
        MapPoint.GeoCoordinate mapPointGeo = currentLocation.getMapPointGeoCoord();
        Log.i(LOG_TAG, String.format("MapView onCurrentLocationUpdate (%f,%f) accuracy (%f)", mapPointGeo.latitude, mapPointGeo.longitude, accuracyInMeters));
    }

    @Override
    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {
    }

    @Override
    public void onCurrentLocationUpdateFailed(MapView mapView) {
    }

    @Override
    public void onCurrentLocationUpdateCancelled(MapView mapView) {
    }


    private void onFinishReverseGeoCoding(String result) {
//        Toast.makeText(LocationDemoActivity.this, "Reverse Geo-coding : " + result, Toast.LENGTH_SHORT).show();
    }

    // ActivityCompat.requestPermissions??? ????????? ????????? ????????? ????????? ???????????? ?????????
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // ?????? ????????? PERMISSIONS_REQUEST_CODE ??????, ????????? ????????? ???????????? ??????????????????
            boolean check_result = true;

            // ?????? ???????????? ??????????????? ???????????????.
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if (check_result) {
                Log.d("@@@", "start");
                //?????? ?????? ????????? ??? ??????

            } else {
                // ????????? ???????????? ????????? ?????? ????????? ??? ?????? ????????? ??????????????? ?????? ???????????????.2 ?????? ????????? ??????
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {
                    finish();
                } else {
                }
            }
        }
    }

    void checkRunTimePermission() {

        //????????? ????????? ??????
        // 1. ?????? ???????????? ????????? ????????? ???????????????.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(WorkerCommute.this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED) {
            // 2. ?????? ???????????? ????????? ?????????
            // ( ??????????????? 6.0 ?????? ????????? ????????? ???????????? ???????????? ????????? ?????? ????????? ?????? ???????????????.)
            // 3.  ?????? ?????? ????????? ??? ??????

        } else {  //2. ????????? ????????? ????????? ?????? ????????? ????????? ????????? ???????????????. 2?????? ??????(3-1, 4-1)??? ????????????.
            // 3-1. ???????????? ????????? ????????? ??? ?????? ?????? ????????????
            if (ActivityCompat.shouldShowRequestPermissionRationale(WorkerCommute.this, REQUIRED_PERMISSIONS[0])) {
                // 3-2. ????????? ???????????? ?????? ?????????????????? ???????????? ????????? ????????? ???????????? ????????? ????????????.
                // 3-3. ??????????????? ????????? ????????? ?????????. ?????? ????????? onRequestPermissionResult?????? ???????????????.
                ActivityCompat.requestPermissions(WorkerCommute.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            } else {
                // 4-1. ???????????? ????????? ????????? ??? ?????? ?????? ???????????? ????????? ????????? ?????? ?????????.
                // ?????? ????????? onRequestPermissionResult?????? ???????????????.
                ActivityCompat.requestPermissions(WorkerCommute.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }
        }
    }

    //??????????????? GPS ???????????? ?????? ????????????
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(WorkerCommute.this);
        builder.setTitle("?????? ????????? ????????????");
        builder.setMessage("?????? ???????????? ???????????? ?????? ???????????? ???????????????.\n"
                + "?????? ????????? ???????????????????");
        builder.setCancelable(true);
        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //???????????? GPS ?????? ???????????? ??????
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d("@@@", "onActivityResult : GPS ????????? ?????????");
                        checkRunTimePermission();
                        return;
                    }
                }

                break;
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public void onMapViewInitialized(MapView mapView) {

    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {

    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {

    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // ?????? ?????? ?????? ????????? ???????????? ??? ????????? ?????? ????????? ????????? ?????? ??? (?????? else{..} ?????? ??????)
            // ActivityCompat.requestPermissions((Activity)mContext, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSION_CAMERA);

            // ?????? ???????????? if()?????? ????????? false??? ?????? ??? -> else{..}??? ???????????? ?????????
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(this)
                        .setTitle("??????")
                        .setMessage("????????? ????????? ?????????????????????. ????????? ???????????? ???????????? ?????? ????????? ?????? ??????????????? ?????????.")
                        .setNeutralButton("??????", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.parse("package:" + getPackageName()));
                                startActivity(intent);
                            }
                        })
                        .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        })
                        .setCancelable(false)
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_LOCATION);
            }
        }
    }
}
