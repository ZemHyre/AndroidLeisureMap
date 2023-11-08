package com.example.leisuremap;

import android.Manifest;
import android.content.Context;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LeisureMap extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap gMap;
    private static final String TAG = LeisureMap.class.getSimpleName();
    SupportMapFragment mapFragment;
    SearchView searchView;
    ListView listView;
    List<Marker> cityMarkers = new ArrayList<>();
    List<Marker> townMarkers = new ArrayList<>();
    List<Object> objectList = new ArrayList<>();
    List<Marker> objectMarkers = new ArrayList<>();
    Marker currentMarker = null;

    private LocationRequest locationRequest;

    ArrayList <String> clickedObjectId = new ArrayList<>();
    ArrayList <String> clickedObjectType = new ArrayList<>();


    boolean isLoggedIn = false;
    String username;

    LatLng userPos;
    LatLng newUserPos;

    Marker userPolyMarker;
    Polyline polyline;
    boolean stopRoute = false;

    ArrayList<String> arrayList = new ArrayList<>();
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leisure_map);
        Activity a = this;
        if(a.getCallingActivity().getClassName().toString().equals("com.example.leisuremap.MainMenu")){
            clickedObjectId.addAll(getIntent().getStringArrayListExtra("oID"));
            clickedObjectType.addAll(getIntent().getStringArrayListExtra("oType"));
        }

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);

        SupportMapFragment supportMapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);

        searchView = findViewById(R.id.sv_location);
        searchView.setQueryHint("Type here to search");
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        listView = findViewById(R.id.list_view);
        adapter = new ArrayAdapter<String>(LeisureMap.this,
                R.layout.list_item, R.id.text_view, arrayList);
        listView.setAdapter(adapter);
        listView.setVisibility(View.GONE);

        userPos = getIntent().getParcelableExtra("UserPos");
        isLoggedIn = checkUserStatus();
        username = checkUsername();
    }

    private void pointToPosition(LatLng position) {
        CameraPosition cameraPosition = new CameraPosition.Builder().target(position).zoom(18).build();
        gMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        gMap = googleMap;
        LatLngBounds bounds = new LatLngBounds(
                new LatLng(53.8967893, 20.653783), // SW bounds
                new LatLng(56.4504213, 26.8355198)  // NE bounds
        );
        gMap.setLatLngBoundsForCameraTarget(bounds);
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bounds.getCenter(), 5));
        gMap.setMinZoomPreference(8.0f);

        try {
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));
            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        LatLng userPos = getIntent().getParcelableExtra("UserPos");
        RequestQueue queue_objects = Volley.newRequestQueue(LeisureMap.this);
        String url_objects = "http://.../table?name=place";
        JsonObjectRequest request_objects = new JsonObjectRequest(Request.Method.GET, url_objects, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {

                    JSONArray array = response.getJSONArray("Table");
                    for(int i = 0; i < array.length(); i++) {
                        JSONObject element = (JSONObject) array.get(i);
                        String objId = element.get("id").toString();
                        String objName = element.get("name").toString();
                        String latitude = element.get("lat").toString();
                        String longitude = element.get("lon").toString();
                        String objType = element.get("type").toString();
                        String objCity = element.get("city").toString();
                        if(objName.equals("null")) {
                            objName = objType;
                        }
                        double lon = Double.valueOf(longitude);
                        double lat = Double.valueOf(latitude);
                        LatLng pos = new LatLng(lat, lon);

                        Location startPoint=new Location("UserPos");
                        if(userPos == null) {
                            startPoint.setLatitude(0);
                            startPoint.setLongitude(0);
                        }
                        else {
                            startPoint.setLatitude(userPos.latitude);
                            startPoint.setLongitude(userPos.longitude);
                        }

                        Location endPoint=new Location("Object");
                        endPoint.setLatitude(pos.latitude);
                        endPoint.setLongitude(pos.longitude);

                        double distance=startPoint.distanceTo(endPoint) / 1000;
                        Object object = new Object(objName, lat, lon, objType, String.format("%.3f", distance), objCity, objId);
                        objectList.add(object);

                        String objNameDistance = objName + " | distance: " + String.format("%.3f", distance) + " km";
                        arrayList.add(objNameDistance);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(LeisureMap.this, "ERROR", Toast.LENGTH_SHORT).show();
            }
        });
        request_objects.setRetryPolicy(new DefaultRetryPolicy(60000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue_objects.add(request_objects);

        RequestQueue queue_cities = Volley.newRequestQueue(LeisureMap.this);
        String url_cities = "http://.../table?name=city";
        JsonObjectRequest request_cities = new JsonObjectRequest(Request.Method.GET, url_cities, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray array = response.getJSONArray("Table");
                    for(int i = 0; i < array.length(); i++) {
                        JSONObject element = (JSONObject) array.get(i);
                        String latitude = element.get("lat").toString();
                        String longitude = element.get("lon").toString();
                        String cityName = element.get("name").toString();
                        String type = element.get("type").toString();
                        double lon = Double.valueOf(longitude);
                        double lat = Double.valueOf(latitude);
                        LatLng pos = new LatLng(lat, lon);
                        Location startPoint=new Location("UserPos");
                        if(userPos == null) {
                            startPoint.setLatitude(0);
                            startPoint.setLongitude(0);
                        }
                        else {
                            startPoint.setLatitude(userPos.latitude);
                            startPoint.setLongitude(userPos.longitude);
                        }

                        Location endPoint=new Location("Object");
                        endPoint.setLatitude(pos.latitude);
                        endPoint.setLongitude(pos.longitude);

                        double distance=startPoint.distanceTo(endPoint) / 1000;
                        if (type.equals("city")) {
                            Marker cityMarker = gMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).title(cityName).snippet(type).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                            cityMarker.setVisible(false);
                            cityMarkers.add(cityMarker);
                            String cityNameDistance = cityName + " | distance: " + String.format("%.3f", distance) + " km";
                            arrayList.add(cityNameDistance);
                        }
                        else {
                            Marker townMarker = gMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).title(cityName).snippet(type).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                            townMarker.setVisible(false);
                            townMarkers.add(townMarker);
                            String cityNameDistance = cityName + " | distance: " + String.format("%.3f", distance) + " km";
                            arrayList.add(cityNameDistance);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(LeisureMap.this, "ERROR", Toast.LENGTH_SHORT).show();
            }
        });
        request_cities.setRetryPolicy(new DefaultRetryPolicy(60000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue_cities.add(request_cities);

        gMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                CameraPosition cameraPosition = gMap.getCameraPosition();
                for(Marker m:cityMarkers) {
                    m.setVisible(cameraPosition.zoom > 9);
                }
                for(Marker m:townMarkers) {
                    m.setVisible(cameraPosition.zoom > 12);
                }
            }
        });

        gMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                if(!stopRoute)
                    stopRoute = true;
                String title = marker.getTitle();
                String snip = marker.getSnippet();
                if(Objects.equals(snip, "city") || Objects.equals(snip, "town"))
                {
                    Intent intentCity = new Intent(getBaseContext(), CityPopUp.class);
                    intentCity.putExtra("City", title);
                    startActivityForResult(intentCity, 0);
                    currentMarker = marker;
                }
                else if(Objects.nonNull(snip)) {
                    Intent intentObject = new Intent(getBaseContext(), ObjectPopUp.class);
                    for(Object o : objectList) {
                        if(title.equals(o.getName())) {
                            intentObject.putExtra("Username", username);
                            intentObject.putExtra("UserStatus", isLoggedIn);
                            intentObject.putExtra("City", o.getCity());
                            intentObject.putExtra("Id", o.getId());
                        }
                    }
                    startActivityForResult(intentObject, 0);
                }
                return false;
            }
        });


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            private void resetSelectedMarker(){
                if(currentMarker != null){
                    currentMarker.remove();
                    currentMarker = null;
                }
            }
            private String removeDiacritics(String src) {
                return Normalizer
                        .normalize(src, Normalizer.Form.NFD)
                        .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
            }
            //removes symbol '|' and chars after symbol '|', extracts location name from a string
            private String removeCharsAfter(String itemSelected) {
                return itemSelected.replaceAll("\\|.*","");
            }
            //removes symbol ':' and chars before symbol ':', extracts kilometers from a string
            private String removeCharsBefore(String itemSelected) {
                return itemSelected.substring(itemSelected.indexOf(": ")+1, itemSelected.lastIndexOf("k"));
            }
            //removes leading and trailing white spaces from a string
            private String removeSpaces(String itemSelected) {
                return itemSelected.trim();
            }

            @Override
            public boolean onQueryTextSubmit(String s) {
                String location = searchView.getQuery().toString();

                for (Marker m : cityMarkers) {
                    if(removeDiacritics(m.getTitle().toLowerCase()).equals(removeDiacritics(location.toLowerCase()))) {
                        listView.setVisibility(View.INVISIBLE);
                        pointToPosition(m.getPosition());
                        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(m.getPosition(), 10));
                    }
                }
                for (Marker m : townMarkers) {
                    if(removeDiacritics(m.getTitle().toLowerCase()).equals(removeDiacritics(location.toLowerCase()))) {
                        listView.setVisibility(View.INVISIBLE);
                        pointToPosition(m.getPosition());
                        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(m.getPosition(), 13));
                    }
                }
                resetSelectedMarker();
                for (Object o : objectList) {
                    if(removeDiacritics(o.getName().toLowerCase()).equals(removeDiacritics(location.toLowerCase()))) {
                        if (currentMarker==null) {
                            currentMarker = gMap.addMarker(new MarkerOptions().position(new LatLng(o.getLat(), o.getLon())).title(o.getName()).snippet(o.getType()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                            listView.setVisibility(View.INVISIBLE);
                            pointToPosition(currentMarker.getPosition());
                            gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentMarker.getPosition(), 15));
                            objectMarkers.add(currentMarker);
                            clickedObjectId.add(o.getId());
                            clickedObjectType.add(o.getType());
                        }
                    }
                }

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        String selectedItem = (String) adapterView.getItemAtPosition(i);

                        resetSelectedMarker();
                        for (Object o : objectList) {
                            if((removeSpaces(removeCharsBefore(selectedItem))).equals(o.getDistanceString())){
                                if (currentMarker==null) {
                                    currentMarker = gMap.addMarker(new MarkerOptions().position(new LatLng(o.getLat(), o.getLon())).title(o.getName()).snippet(o.getType()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                                    listView.setVisibility(View.INVISIBLE);
                                    pointToPosition(currentMarker.getPosition());
                                    gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentMarker.getPosition(), 15));
                                    objectMarkers.add(currentMarker);
                                    clickedObjectId.add(o.getId());
                                    clickedObjectType.add(o.getType());
                                }
                            }
                        }
                        for (Marker m : cityMarkers) {
                            if(m.getTitle().equals(removeSpaces(removeCharsAfter(selectedItem)))) {
                                listView.setVisibility(View.INVISIBLE);
                                pointToPosition(m.getPosition());
                                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(m.getPosition(), 10));
                            }
                        }
                        for (Marker m : townMarkers) {
                            if(m.getTitle().equals(removeSpaces(removeCharsAfter(selectedItem)))) {
                                listView.setVisibility(View.INVISIBLE);
                                pointToPosition(m.getPosition());
                                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(m.getPosition(), 13));
                            }
                        }
                    }
                });
                adapter.getFilter().filter(s);
                return false;
            }

            private void pointToPosition(LatLng position) {
                CameraPosition cameraPosition = new CameraPosition.Builder().target(position).zoom(18).build();
                gMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }

            @Override
            public boolean onQueryTextChange(String s) {

                if(!stopRoute && polyline != null)
                    stopRoute = true;
                if(polyline != null)
                    polyline.remove();
                if(userPolyMarker != null)
                    userPolyMarker.remove();
                if(currentMarker != null)
                    currentMarker.remove();

                if (s!= null)
                {
                    if (s.isEmpty()) {
                        listView.setVisibility(View.INVISIBLE);
                    }
                    else {
                        listView.setVisibility(View.VISIBLE);
                        adapter.getFilter().filter(s);
                    }
                }

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        String selectedItem = (String) adapterView.getItemAtPosition(i);

                        resetSelectedMarker();
                        for (Object o : objectList) {
                            if((removeSpaces(removeCharsBefore(selectedItem))).equals(o.getDistanceString())){
                                if (currentMarker==null) {
                                    currentMarker = gMap.addMarker(new MarkerOptions().position(new LatLng(o.getLat(), o.getLon())).title(o.getName()).snippet(o.getType()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                                    listView.setVisibility(View.INVISIBLE);
                                    pointToPosition(currentMarker.getPosition());
                                    gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentMarker.getPosition(), 15));
                                    objectMarkers.add(currentMarker);
                                    clickedObjectId.add(o.getId());
                                    clickedObjectType.add(o.getType());
                                }
                            }
                        }
                        for (Marker m : cityMarkers) {
                            if(m.getTitle().equals(removeSpaces(removeCharsAfter(selectedItem)))) {
                                pointToPosition(m.getPosition());
                                listView.setVisibility(View.INVISIBLE);
                                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(m.getPosition(), 10));
                            }
                        }
                        for (Marker m : townMarkers) {
                            if(m.getTitle().equals(removeSpaces(removeCharsAfter(selectedItem)))) {
                                pointToPosition(m.getPosition());
                                listView.setVisibility(View.INVISIBLE);
                                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(m.getPosition(), 13));
                            }
                        }
                    }
                });
                return false;
            }
        });


        LatLng pos = getIntent().getParcelableExtra("Position");

        if(pos != null) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    for (Object o : objectList) {
                        LatLng pos2 = new LatLng(o.getLat(), o.getLon());
                        if(pos.equals(pos2)){
                            currentMarker = gMap.addMarker(new MarkerOptions().position(new LatLng(o.getLat(), o.getLon())).title(o.getName()).snippet(o.getType()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                            pointToPosition(currentMarker.getPosition());
                            gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentMarker.getPosition(), 15));
                            objectMarkers.add(currentMarker);
                        }
                    }
                }
            }, 1000);
        }
    }


    private void direction(LatLng userPos, String type, boolean zoom) {

        String s1 = currentMarker.getPosition().latitude + ", " + currentMarker.getPosition().longitude;
        String s2 = userPos.latitude + ", " + userPos.longitude;
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = Uri.parse("https://maps.googleapis.com/maps/api/directions/json").buildUpon()
                .appendQueryParameter("destination", s1)
                .appendQueryParameter("origin", s2)
                .appendQueryParameter("mode", type)
                .appendQueryParameter("key", "AIzaSyArYVmc3LXMibkjG-PSRdqFIrVXmkO7jWs").toString();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String status = response.getString("status");
                    if (status.equals("OK")) {
                        JSONArray routes = response.getJSONArray("routes");
                        ArrayList<LatLng> points;
                        PolylineOptions polylineOptions = null;

                        for(int i = 0; i < routes.length(); i++) {
                            points = new ArrayList<>();
                            polylineOptions = new PolylineOptions();
                            JSONArray legs = routes.getJSONObject(i).getJSONArray("legs");

                            for(int j = 0; j < legs.length(); j++) {
                                JSONArray steps = legs.getJSONObject(j).getJSONArray("steps");

                                for(int k = 0; k < steps.length(); k++) {
                                    String polyline = steps.getJSONObject(k).getJSONObject("polyline").getString("points");
                                    List<LatLng> list = decodePoly(polyline);

                                    for(int l = 0; l < list.size(); l++) {
                                        LatLng position = new LatLng(list.get(l).latitude, list.get(l).longitude);
                                        points.add(position);
                                    }
                                }
                            }
                            polylineOptions.addAll(points);
                            polylineOptions.width(10);
                            polylineOptions.color(ContextCompat.getColor(LeisureMap.this, R.color.teal_200));
                            polylineOptions.geodesic(true);
                        }
                        polyline = gMap.addPolyline(polylineOptions);
                        userPolyMarker = gMap.addMarker(new MarkerOptions().position(new LatLng(userPos.latitude, userPos.longitude)).title("Your position"));

                        LatLngBounds bounds = new LatLngBounds.Builder()
                                .include(new LatLng(userPos.latitude, userPos.longitude))
                                .include(new LatLng(currentMarker.getPosition().latitude, currentMarker.getPosition().longitude)).build();
                        Point point = new Point();
                        getWindowManager().getDefaultDisplay().getSize(point);
                        if(!zoom)
                            gMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, point.x, 150, 30));
                        else
                            gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userPolyMarker.getPosition(), 20));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(LeisureMap.this, "ERROR", Toast.LENGTH_SHORT).show();
            }
        });
        request.setRetryPolicy(new DefaultRetryPolicy(60000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(request);
    }

    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0;
        int length = encoded.length();
        int lat = 0, lng = 0;
        while(index < length) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while(b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0;
            result = 0;

            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while(b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)), ((double) lng / 1E5));
            poly.add(p);
        }
        return poly;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(polyline != null)
            polyline.remove();
        if(resultCode==1) {
            if(userPolyMarker != null)
                userPolyMarker.remove();
            stopRoute = false;
            newUserPos = userPos;
            direction(userPos, "walking", false);
            continueRoute("walking");
        }
        else if(resultCode==2) {
            if(userPolyMarker != null)
                userPolyMarker.remove();
            stopRoute = false;
            newUserPos = userPos;
            direction(userPos, "driving", false);
            continueRoute("driving");
        }
    }

    @Override
    public void onBackPressed() {

        if (getIntent().getStringExtra("Parent") == null) {
            Intent intent = new Intent(this, FindActivities.class);
            intent.putStringArrayListExtra("result3", clickedObjectId);
            intent.putStringArrayListExtra("result4", clickedObjectType);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
        else {
            Intent intent = new Intent(this, MainMenu.class);
            intent.putStringArrayListExtra("result1", clickedObjectId);
            intent.putStringArrayListExtra("result2", clickedObjectType);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRoute = true;
        Intent intent = new Intent(this, ExitService.class);
        intent.putStringArrayListExtra("ID", clickedObjectId);
        intent.putStringArrayListExtra("Type", clickedObjectType);
        startService(intent);
    }

    public boolean checkUserStatus() {
        return getIntent().getBooleanExtra("UserStatus", false);
    }
    public String checkUsername() {
        return getIntent().getStringExtra("Username");
    }


    private void getCurrentLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(LeisureMap.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (isGPSEnabled()) {
                    LocationServices.getFusedLocationProviderClient(LeisureMap.this)
                            .requestLocationUpdates(locationRequest, new LocationCallback() {
                                @Override
                                public void onLocationResult(@NonNull LocationResult locationResult) {
                                    super.onLocationResult(locationResult);

                                    LocationServices.getFusedLocationProviderClient(LeisureMap.this)
                                            .removeLocationUpdates(this);

                                    if (locationResult.getLocations().size() > 0){
                                        int index = locationResult.getLocations().size() - 1;
                                        double latitude = locationResult.getLocations().get(index).getLatitude();
                                        double longitude = locationResult.getLocations().get(index).getLongitude();
                                        newUserPos = new LatLng(latitude, longitude);
                                    }
                                }
                            }, Looper.getMainLooper());
                    waitForPosition();
                }
                else {
                    turnOnGPS();
                }
            }
            else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    private void turnOnGPS() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getApplicationContext())
                .checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    Toast.makeText(LeisureMap.this, "GPS is turned on", Toast.LENGTH_SHORT).show();
                }
                catch (ApiException e) {
                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(LeisureMap.this, 2);
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                            break;

                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            break;
                    }
                }
            }
        });
    }

    private boolean isGPSEnabled() {
        LocationManager locationManager = null;
        boolean isEnabled = false;
        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }
        isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isEnabled;
    }

    public void waitForPosition() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                if(newUserPos == null)
                    waitForPosition();
            }
        }, 1000);
    }

    public void continueRoute(String type) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                if(polyline != null && !stopRoute) {
                    userPos = newUserPos;
                    getCurrentLocation();
                    if(userPolyMarker != null)
                        userPolyMarker.remove();
                    if(polyline != null)
                        polyline.remove();
                    direction(newUserPos, type, true);
                    continueRoute(type);
                }
            }
        }, 5000);
    }
}