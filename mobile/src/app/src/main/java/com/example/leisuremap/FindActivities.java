package com.example.leisuremap;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FindActivities extends AppCompatActivity {

    private Button b_pref;
    List<Object> objects = new ArrayList<>();

    LatLng userPos;
    boolean isLoggedIn = false;
    String username;
    ArrayList <String> clickedObjectId = new ArrayList<>();
    ArrayList <String> clickedObjectType = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_activities);

        userPos = getIntent().getParcelableExtra("UserPos");
        Activity a = this;
        if(a.getCallingActivity().getClassName().toString().equals("com.example.leisuremap.MainMenu")){
            clickedObjectId.clear();
            clickedObjectType.clear();
            clickedObjectId.addAll(getIntent().getStringArrayListExtra("oID"));
            clickedObjectType.addAll(getIntent().getStringArrayListExtra("oType"));
        }
        isLoggedIn = checkUserStatus();
        username = checkUsername();

        //------------------------------------------------------SPINNERS----------------------------------------------------------------
        //distance spinner
        Spinner spinner1 = findViewById(R.id.spinner1);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this, R.array.distance_array, R.layout.spinner_item);
        adapter1.setDropDownViewResource(R.layout.spinner_dropdown);
        spinner1.setAdapter(adapter1);

        //rating spinner
        Spinner spinner2 = findViewById(R.id.spinner2);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.rating_array, R.layout.spinner_item);
        adapter2.setDropDownViewResource(R.layout.spinner_dropdown);
        spinner2.setAdapter(adapter2);

        //city pinner
        ArrayList<String> select_cities = new ArrayList<>();
        select_cities.add("Select cities");
        Spinner spinner3 = findViewById(R.id.spinner3);
        ArrayList<SpinnerState> listVOsCities = new ArrayList<>();

        //type spinner
        ArrayList<String> select_types = new ArrayList<>();
        select_types.add("Select types");
        Spinner spinner4 = findViewById(R.id.spinner4);
        ArrayList<SpinnerState> listVOsTypes = new ArrayList<>();
        //----------------------------------------------------------------------------------------------------------------------------


        RequestQueue queue_objects = Volley.newRequestQueue(FindActivities.this);
        String url_objects = "http://.../view?name=places";
        JsonObjectRequest request_objects = new JsonObjectRequest(Request.Method.GET, url_objects, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray array = response.getJSONArray("Table");
                    for(int i = 0; i < array.length(); i++) {
                        JSONObject element = (JSONObject) array.get(i);
                        String objId = element.get("id").toString();
                        String latitude = element.get("lat").toString();
                        String longitude = element.get("lon").toString();
                        double lon = Double.valueOf(longitude);
                        double lat = Double.valueOf(latitude);
                        LatLng pos = new LatLng(lat, lon);
                        String objType = element.get("type").toString();
                        String objName = element.get("name").toString();
                        String rating = element.get("rating").toString();
                        double objRating = Double.parseDouble(rating);

                        if(objName.equals("null"))
                            objName = objType;

                        String objCity = element.get("city").toString();

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

                        if(!select_cities.contains(objCity)){
                            select_cities.add(objCity);
                        }

                        if(!select_types.contains(objType)){
                            select_types.add(objType);
                        }

                        double score = (10 - Math.sqrt(distance)) * 2 + (objRating * 2);

                        Object obj = new Object(objName, distance, lat, lon, objRating, objCity, objType, score, objId);
                        objects.add(obj);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                for (int i = 0; i < select_cities.size(); i++) {
                    SpinnerState stateCities = new SpinnerState();
                    stateCities.setTitle(select_cities.get(i));
                    stateCities.setSelected(false);
                    listVOsCities.add(stateCities);
                }
                MyAdapter myAdapterCities = new MyAdapter(FindActivities.this, 0, listVOsCities);
                spinner3.setAdapter(myAdapterCities);

                for (int i = 0; i < select_types.size(); i++) {
                    SpinnerState stateTypes = new SpinnerState();
                    stateTypes.setTitle(select_types.get(i));
                    stateTypes.setSelected(false);
                    listVOsTypes.add(stateTypes);
                }
                MyAdapter myAdapterTypes = new MyAdapter(FindActivities.this, 0, listVOsTypes);
                spinner4.setAdapter(myAdapterTypes);

                sortList();
                for(Object o:objects) {
                    createTextViews(o);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(FindActivities.this, "ERROR", Toast.LENGTH_SHORT).show();
            }
        });
        request_objects.setRetryPolicy(new DefaultRetryPolicy(60000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue_objects.add(request_objects);

        b_pref = findViewById(R.id.search);
        b_pref.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int chosenDist;
                if (!spinner1.getSelectedItem().toString().matches("Choose distance")) {
                    String distance = spinner1.getSelectedItem().toString();
                    String distNumbersOnly = distance.replaceAll("[^0-9]", "");
                    chosenDist = Integer.parseInt(distNumbersOnly);
                } else
                    chosenDist = 500; // to display all objects

                int chosenRating;
                if (!spinner2.getSelectedItem().toString().matches("Choose rating")) {
                    String rating = spinner2.getSelectedItem().toString();
                    String ratingNumbersOnly = rating.replaceAll("[^0-9]", "");
                    chosenRating = Integer.parseInt(ratingNumbersOnly);
                } else
                    chosenRating = 1; // to display all objects

                ArrayList<String> chosenCities = new ArrayList<>();
                for (SpinnerState s : listVOsCities) {
                    if (s.isSelected()) {
                        chosenCities.add(s.getTitle());
                    }
                }

                ArrayList<String> chosenTypes = new ArrayList<>();
                for (SpinnerState s : listVOsTypes) {
                    if (s.isSelected()) {
                        chosenTypes.add(s.getTitle());
                    }
                }
                searchObject(chosenDist, chosenRating, chosenCities, chosenTypes);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 3) {
            clickedObjectId.addAll(data.getStringArrayListExtra("result3"));
            clickedObjectType.addAll(data.getStringArrayListExtra("result4"));
        }
    }

    public void searchObject(int distance, int rating, ArrayList<String> chosenCities, ArrayList<String> chosenTypes) {
        LinearLayout linearLayout = findViewById(R.id.linearLayout);
        linearLayout.removeAllViews();
        int n = 0; //counting objects found
        for(Object o:objects) {
            if(o.getDistance() <= distance && o.getRating() >= rating && (chosenCities.stream().anyMatch(o.getCity()::contains) || chosenCities.isEmpty()) && (chosenTypes.stream().anyMatch(o.getType()::contains) || chosenTypes.isEmpty())) {
                createTextViews(o);
                n++;
            }
        }
        if(n == 0) {
            Toast.makeText(FindActivities.this, "No objects found in this distance", Toast.LENGTH_SHORT).show();
        }
    }

    public void createTextViews(Object object) {
        LatLng pos = new LatLng(object.getLat(), object.getLon());
        String id = object.getId();
        String type = object.getType();
        LinearLayout linearLayout = findViewById(R.id.linearLayout);
        TextView textView = new TextView(FindActivities.this);
        LayoutParams layoutParams = new LayoutParams(1000, 300);
        layoutParams.gravity = Gravity.CENTER;
        textView.setLayoutParams(layoutParams);
        layoutParams.setMargins(10, 10, 10, 10);
        textView.setBackgroundDrawable(getResources().getDrawable(R.drawable.pop_up_style));
        textView.setTextColor(Color.BLACK);
        String formatDist = String.format("%.1f", object.getDistance());
        String formatScore = String.format("%.1f", object.getScore());
        textView.setText("Name: " + object.getName() + "\nDistance: " + formatDist + " km, " + "Rating: " + object.getRating() + " stars, " + "City: " + object.getCity() + ", Type: " + object.getType() + ", Score: " + formatScore);
        textView.setPadding(20, 20, 20, 20);
        textView.setGravity(Gravity.CENTER);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickedObjectId.add(id);
                clickedObjectType.add(type);
                Intent intent = new Intent(getBaseContext(), LeisureMap.class);
                intent.putExtra("Position", pos);
                intent.putExtra("UserPos", userPos);
                intent.putExtra("Username", username);
                intent.putExtra("UserStatus", isLoggedIn);
                intent.putStringArrayListExtra("oID", clickedObjectId);
                intent.putStringArrayListExtra("oType", clickedObjectType);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivityForResult(intent, 3);
            }
        });
        linearLayout.addView(textView);
    }

    public void sortList() {
        Collections.sort(objects, new Comparator<Object>() {
            public int compare(Object o1, Object o2) {
                return Double.compare(o2.getScore(), o1.getScore());
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainMenu.class);
        intent.putStringArrayListExtra("result3", clickedObjectId);
        intent.putStringArrayListExtra("result4", clickedObjectType);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
}
