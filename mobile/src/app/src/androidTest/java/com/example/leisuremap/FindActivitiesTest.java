package com.example.leisuremap;

import static org.junit.Assert.assertNotNull;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.test.espresso.intent.Intents;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import com.google.android.gms.maps.model.LatLng;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class FindActivitiesTest {
    @Rule
    public ActivityTestRule<FindActivities> findActivitiesActivityTestRule = new ActivityTestRule<>(FindActivities.class);
    private FindActivities findActivities = null;


    @Before
    public void setUp() throws Exception {
        findActivities = findActivitiesActivityTestRule.getActivity();
        Intents.init();
    }

    @Test
    public void testLaunch() {
        View view = findActivities.findViewById(R.id.findAct);
        assertNotNull(view);
    }

    @Test
    public void testElements() {
        TextView textViewFind = findActivities.findViewById(R.id.findActivities);
        assertNotNull(textViewFind);
        TextView textViewRec = findActivities.findViewById(R.id.ourRec);
        assertNotNull(textViewRec);
        Button buttonSearch = findActivities.findViewById(R.id.search);
        assertNotNull(buttonSearch);
        LinearLayout layout = findActivities.findViewById(R.id.linearLayout);
        assertNotNull(layout);
        Spinner spinner1 = findActivities.findViewById(R.id.spinner1);
        assertNotNull(spinner1);
        Spinner spinner2 = findActivities.findViewById(R.id.spinner2);
        assertNotNull(spinner2);
        Spinner spinner3 = findActivities.findViewById(R.id.spinner3);
        assertNotNull(spinner3);
        Spinner spinner4 = findActivities.findViewById(R.id.spinner4);
        assertNotNull(spinner4);
    }

    @After
    public void tearDown() throws Exception {
        findActivities = null;
        Intents.release();
    }
}