package com.example.exercisegreenroad.ui;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.exercisegreenroad.R;
import com.example.exercisegreenroad.geofencing.GeofenceService;
import com.example.exercisegreenroad.location.LocationTaskListener;
import com.example.exercisegreenroad.location.TrackingLocationTask;
import com.example.exercisegreenroad.manage.AppManager;
import com.example.exercisegreenroad.objects.GLocation;
import com.example.exercisegreenroad.objects.LocationHistory;
import com.example.exercisegreenroad.utils.Logger;
import com.example.exercisegreenroad.utils.Utils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Date;
//import com.example.exercisegreenroad.ui.databinding.ActivityMapsBinding;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, LocationTaskListener, GLocationAdapter.GLocationAdapterListener {
    public static final int REQUEST_CODE_PERMISSIONS = 101;

    boolean menuVisibility = false,mainPointFocused=false;
    private GoogleMap mMap;
    BroadcastReceiver updateUiReceiver;
    BroadcastReceiver logReceiver;
    TrackingLocationTask locationTask;

    GLocationAdapter adapter;

    View cl_menu, ll_noMainLocation, ll_points, ll_points_2;
    ImageView iv_menu;
    ListView lv_points,lv_points_2;
    TextView tv_log,tv_allowNetworkLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ll_noMainLocation = findViewById(R.id.ll_noMainLocation);
        tv_allowNetworkLocation = findViewById(R.id.tv_allowNetworkLocation);
        ll_points = findViewById(R.id.ll_points);
        iv_menu = findViewById(R.id.iv_menu);
        lv_points = findViewById(R.id.lv_points);
        cl_menu = findViewById(R.id.cl_menu);
        tv_log = findViewById(R.id.tv_log);
        ll_points_2 = findViewById(R.id.ll_points_2);
        lv_points_2 = findViewById(R.id.lv_points_2);

        updateUiReceiver = getUIBroadcastReceiver();
        logReceiver = getLogBroadcastReceiver();
        locationTask = new TrackingLocationTask();

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        boolean isNew=mMap==null;

        mMap = googleMap;
        if(isNew) {
            mMap.setOnMarkerClickListener(this);
            initVisible();
            updateMap();
            focusOnPoint(AppManager.getInstance().getMainPoint());
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Toast.makeText(getApplicationContext(), marker.getTitle(), Toast.LENGTH_LONG).show();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logger.i("MapsActivity", "onResume");


        registerReceiver(updateUiReceiver, new IntentFilter(AppManager.ACTION_updateUI));
        registerReceiver(logReceiver, new IntentFilter(Logger.ACTION_LOG));

        checkLocationPermission();

        if (!locationTask.isRunning()) {
            locationTask.start(this);
        }

        initVisible();
        updateMap();
        updateHistory();

        GeofenceService.start();

        focusOnPoint(AppManager.getInstance().getMainPoint());
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(updateUiReceiver);
        unregisterReceiver(logReceiver);
    }

    @Override
    protected void onDestroy() {
        locationTask.stopRunning();
        super.onDestroy();
    }

    private void checkLocationPermission() {
        boolean hasPermission = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (hasPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            hasPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }

        if (!hasPermission) {
            String[] array = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                array = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION};
            }

            ActivityCompat.requestPermissions(this,
                    array, REQUEST_CODE_PERMISSIONS);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        checkLocationPermission();
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    public boolean onLocationTaskReceived(Location location) {
        GLocation  gl=new GLocation();
        gl.lat=location.getLatitude();
        gl.lon=location.getLongitude();
        gl.date=new Date();
        gl.type=GLocation.TYPE_TRACKING;

        AppManager.getInstance().setLastLocation(gl);
        updateMap();
        return true;
    }

    @Override
    public void onLocationTaskStopRunning() {

    }

    @Override
    public void onSelectGLocation(GLocation location) {
        AppManager.getInstance().setMainPoint(location);
        toggleMenu();
    }

    private void updateLog() {
        if (Logger.log == null || Logger.log.size() == 0) {
            return;
        }

        StringBuilder builder = new StringBuilder();
        String lastTag = "";

        for (Logger.Line l : Logger.log) {
            if (!lastTag.equals(l.tag)) {
                builder.append(Utils.getHTMLText_underline(Utils.getHTMLText_bold(l.tag)));
                builder.append(Utils.getHTMLEnter());
            }
            lastTag = l.tag;
            builder.append(Utils.getHTMLText_blue(Utils.getFullTime(l.date)));
            builder.append(" ");
            builder.append(l.text);
            builder.append(Utils.getHTMLEnter());
        }

        tv_log.setText(Html.fromHtml(builder.toString()));
    }

    public void toggleMenu() {
        menuVisibility = !menuVisibility;

        if (menuVisibility) {
            iv_menu.setRotation(180);
        } else {
            iv_menu.setRotation(0);
        }

        Animation aniRotate = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_180);
        iv_menu.startAnimation(aniRotate);
        adapter.setShowFullData(menuVisibility);

        initVisible();
    }

    private void initVisible() {
        if (!AppManager.getInstance().hasMainPoint()) {
            ll_noMainLocation.setVisibility(View.VISIBLE);
            tv_allowNetworkLocation.setVisibility(AppManager.getInstance().isAllowMainPointByNetwork() ? View.GONE : View.VISIBLE);
            cl_menu.setVisibility(View.GONE);
            return;
        }else{
            ll_noMainLocation.setVisibility(View.GONE);
        }

        cl_menu.setVisibility(View.VISIBLE);
        ll_points.setVisibility(menuVisibility ? View.GONE: View.VISIBLE);
        ll_points_2.setVisibility(menuVisibility ? View.VISIBLE: View.GONE);
    }

    private void updateMap() {

        if (mMap == null) {
            return;
        }

        mMap.clear();

        LocationHistory history = AppManager.getInstance().getHistory();
        for (GLocation l : history.locations) {
            if(l.type == GLocation.TYPE_ENTER) {
                addCircle(l, 16, R.color.transparent, 3f, R.color.enter_point);
            }else if(l.type == GLocation.TYPE_EXIT ) {
                addCircle(l, 16, R.color.transparent, 3f, R.color.exit_point);
            }
        }

        if (AppManager.getInstance().hasMainPoint()) {
            GLocation mainPoint = AppManager.getInstance().getMainPoint();
            addCircle(mainPoint, GeofenceService.RADIUS_IN_METERS, R.color.main_point_radius, 1f, R.color.main_point_radius_stroke);
            addCircle(mainPoint, 12, R.color.main_point);
        }

        if(AppManager.getInstance().hasLastLocation()){
            LatLng point = AppManager.getInstance().getLastLocation().getPoint();
            mMap.addMarker(new MarkerOptions()
                    .position(point)
                    .title(AppManager.getInstance().getLastLocation().description));
        }
}

    private void addCircle(GLocation point, int radius, int color, float strokeWidth, int stroke_color) {
        mMap.addCircle(new CircleOptions()
                .center(point.getPoint())
                .radius(radius)
                .strokeWidth(strokeWidth)
                .strokeColor(Utils.getColor(stroke_color))
                .fillColor(Utils.getColor(color)));
    }

    private void addCircle(GLocation point, int radius, int color) {
        addCircle(point,radius,color,0,color);
    }


    private BroadcastReceiver getUIBroadcastReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Utils.runOnUI(() -> {
                    initVisible();
                    updateMap();
                    updateHistory();

                    if(!mainPointFocused){
                        focusOnPoint(AppManager.getInstance().getMainPoint());
                    }
                });
            }
        };
    }

    private void updateHistory() {
        adapter = new GLocationAdapter(getApplicationContext(),this);
        lv_points.setAdapter(adapter);
        lv_points_2.setAdapter(adapter);
    }


    private void focusOnPoint(GLocation location) {
        if(location==null ||mMap==null){
            return;
        }
        mainPointFocused=true;
        LatLng point = location.getPoint();
        mMap.moveCamera(CameraUpdateFactory.newLatLng(point));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 13.0f));
    }

    public void onAllowNetworkLocationClick(View view) {
        AppManager.getInstance().allowMainPointByNetwork();
        view.setVisibility(View.GONE);
    }

    public void onToggleMenuClick(View view) {
        toggleMenu();
    }

    private BroadcastReceiver getLogBroadcastReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Utils.runOnUI(() -> {
                    updateLog();
                });
            }
        };
    }


    public void onToggleLog(View view) {
       tv_log.setVisibility(tv_log.getVisibility()==View.VISIBLE?View.GONE:View.VISIBLE);
    }
}