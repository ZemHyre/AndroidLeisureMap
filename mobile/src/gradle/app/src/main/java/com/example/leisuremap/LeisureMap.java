package com.example.leisuremap;

import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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

    ArrayList<String> arrayList = new ArrayList<>();
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leisure_map);

        SupportMapFragment supportMapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);

        searchView = findViewById(R.id.sv_location);
        searchView.setQueryHint("Type here to search");
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        listView = findViewById(R.id.list_view);
        adapter = new ArrayAdapter<String>(LeisureMap.this,
                android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(adapter);
        listView.setVisibility(View.GONE);

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

        LatLng userPos = getIntent().getParcelableExtra("Pos"); // getting user's position
        RequestQueue queue_objects = Volley.newRequestQueue(LeisureMap.this);
        //String url_objects = "https://overpass-api.de/api/interpreter?data=%2F*%0AThis%20has%20been%20generated%20by%20the%20overpass-turbo%20wizard.%0AThe%20original%20search%20was%3A%0A%E2%80%9Ctourism%3Dmuseum%20in%20lithuania%E2%80%9D%0A*%2F%0A%5Bout%3Ajson%5D%5Btimeout%3A25%5D%3B%0A%2F%2F%20fetch%20area%20%E2%80%9Clithuania%E2%80%9D%20to%20search%20in%0Aarea%28id%3A3600072596%29-%3E.searchArea%3B%0A%2F%2F%20gather%20results%0A%28%0A%20%20%2F%2F%20query%20part%20for%3A%20%E2%80%9Ctourism%3Dmuseum%E2%80%9D%0A%20%20node%5B%22tourism%22%3D%22museum%22%5D%28area.searchArea%29%3B%0A%20%20way%5B%22tourism%22%3D%22museum%22%5D%28area.searchArea%29%3B%0A%20%20relation%5B%22tourism%22%3D%22museum%22%5D%28area.searchArea%29%3B%0A%29%3B%0A%2F%2F%20print%20results%0Aout%20body%3B%0A%3E%3B%0Aout%20skel%20qt%3B";
        String url_objects = "http://.../table?name=place";
        JsonObjectRequest request_objects = new JsonObjectRequest(Request.Method.GET, url_objects, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    //JSONArray array = response.getJSONArray("elements");
                    JSONArray array = response.getJSONArray("Table");
                    for(int i = 0; i < array.length(); i++) {
                        JSONObject element = (JSONObject) array.get(i);
                        String objName = element.get("name").toString();
                        String latitude = element.get("lat").toString();
                        String longitude = element.get("lon").toString();
                        String objType = element.get("type").toString();
                        if(objName.equals("null")) {
                            objName = objType;
                        }
                        //JSONObject obj = (JSONObject) element.get("tags");
                        //String objName = obj.get("name").toString();
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
                        Object object = new Object(objName, lat, lon, objType, String.format("%.3f", distance));
                        objectList.add(object);
                        System.out.println("object distance: " + object.getDistanceString());

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
        //String url_cities = "https://overpass-api.de/api/interpreter?data=[out:json][timeout:25];area(id:3600072596)-%3E.searchArea;(node[%22place%22=%22city%22](area.searchArea);way[%22place%22=%22city%22](area.searchArea);relation[%22place%22=%22city%22](area.searchArea););out%20body;%3E;out%20skel%20qt;";
        String url_cities = "http://.../table?name=city";
        JsonObjectRequest request_cities = new JsonObjectRequest(Request.Method.GET, url_cities, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray array = response.getJSONArray("Table");
                    //JSONArray array = response.getJSONArray("elements");
                    for(int i = 0; i < array.length(); i++) {
                        JSONObject element = (JSONObject) array.get(i);
                        String latitude = element.get("lat").toString();
                        String longitude = element.get("lon").toString();
                        String cityName = element.get("name").toString();
                        String type = element.get("type").toString();
                        //JSONObject obj = (JSONObject) element.get("tags");
                        //String cityName = obj.get("name").toString();
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
                for(Marker m:cityMarkers){
                    m.setVisible(cameraPosition.zoom > 9);
                }
                for(Marker m:townMarkers) {
                    m.setVisible(cameraPosition.zoom > 12);
                }
                for(Marker m:objectMarkers) {
                    m.setVisible(cameraPosition.zoom > 14);
                }

            }
        });

        gMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                String city = marker.getTitle();
                String snip = marker.getSnippet();
                if(Objects.equals(snip, "city")|| Objects.equals(snip, "town"))
                {
                    Intent intent = new Intent(getBaseContext(), CityPopUpActivity.class);
                    intent.putExtra("City", city);
                    startActivity(intent);
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
                if (s!= null)
                {
                    if (s.isEmpty()){
                        listView.setVisibility(View.INVISIBLE);
                    }
                    else{
                        listView.setVisibility(View.VISIBLE);
                        adapter.getFilter().filter(s);
                    }
                }
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        String selectedItem = (String) adapterView.getItemAtPosition(i);

                        //System.out.println("selected item:" + removeCharsBefore(selectedItem));
                        resetSelectedMarker();
                        for (Object o : objectList) {
                            if((removeSpaces(removeCharsBefore(selectedItem))).equals(o.getDistanceString())){
                                //System.out.println("selecteditem: " + removeSpaces(removeCharsBefore(selectedItem)) + " object distance: " + o.getDistanceString());
                                if (currentMarker==null) {
                                    currentMarker = gMap.addMarker(new MarkerOptions().position(new LatLng(o.getLat(), o.getLon())).title(o.getName()).snippet(o.getType()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                                    listView.setVisibility(View.INVISIBLE);
                                    pointToPosition(currentMarker.getPosition());
                                    gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentMarker.getPosition(), 15));
                                    objectMarkers.add(currentMarker);
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
                        System.out.println("Pos1 = " + pos + " Pos2 = "  + pos2);
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
}