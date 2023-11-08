package com.example.leisuremap;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.test.espresso.intent.Intents;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import java.security.Permission;

@RunWith(AndroidJUnit4.class)
public class MainMenuTest {
    @Rule
    public ActivityTestRule<MainMenu> mainMenuActivityTestRule = new ActivityTestRule<>(MainMenu.class);
    private MainMenu mainMenu = null;

    @Before
    public void setUp() throws Exception {
        mainMenu = mainMenuActivityTestRule.getActivity();
        Intents.init();
    }

    @Test
    public void testLaunch() {
        View view = mainMenu.findViewById(R.id.mainMenu);
        assertNotNull(view);
    }

    @Test
    public void testElements() {
        TextView textView = mainMenu.findViewById(R.id.textView);
        assertNotNull(textView);
        Button buttonMap = mainMenu.findViewById(R.id.map);
        assertNotNull(buttonMap);
        Button buttonActivities = mainMenu.findViewById(R.id.findActivities);
        assertNotNull(buttonActivities);
        Button buttonLogin = mainMenu.findViewById(R.id.signIn);
        assertNotNull(buttonLogin);
    }

    @Test
    public void testButtons() {
        onView(withId(R.id.map)).perform(click());
        intended(hasComponent(LeisureMap.class.getName()));
        pressBack();
        onView(withId(R.id.signIn)).perform(click());
        intended(hasComponent(Login.class.getName()));
        pressBack();
    }

    @After
    public void tearDown() throws Exception {
        mainMenu = null;
        Intents.release();
    }
}