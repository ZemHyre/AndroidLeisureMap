package com.example.leisuremap;

import static org.junit.Assert.assertNotNull;

import android.view.View;
import android.widget.SearchView;
import androidx.test.espresso.intent.Intents;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class LeisureMapTest {
    @Rule
    public ActivityTestRule<LeisureMap> mapActivityTestRule = new ActivityTestRule<>(LeisureMap.class);
    private LeisureMap mapAct = null;

    @Before
    public void setUp() throws Exception {
        mapAct = mapActivityTestRule.getActivity();
        Intents.init();
    }

    @Test
    public void testLaunch() {
        View view = mapAct.findViewById(R.id.map);
        assertNotNull(view);
    }

    @Test
    public void testElements() {
        SearchView searchView = mapAct.findViewById(R.id.sv_location);
        assertNotNull(searchView);
        View mapFrag = mapAct.findViewById(R.id.map);
        assertNotNull(mapFrag);
    }

    @After
    public void tearDown() throws Exception {
        mapAct = null;
        Intents.release();
    }
}