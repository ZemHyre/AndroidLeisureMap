package com.example.leisuremap;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertNotNull;

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

@RunWith(AndroidJUnit4.class)
public class LoginTest {
    @Rule
    public ActivityTestRule<Login> loginActivityTestRule = new ActivityTestRule<>(Login.class);
    private Login login = null;

    @Before
    public void setUp() throws Exception {
        login = loginActivityTestRule.getActivity();
        Intents.init();
    }

    @Test
    public void testLaunch() {
        View view = login.findViewById(R.id.loginAct);
        assertNotNull(view);
    }

    @Test
    public void testElements() {
        TextView textView = login.findViewById(R.id.textView);
        assertNotNull(textView);
        TextView usernameTextView = login.findViewById(R.id.username);
        assertNotNull(usernameTextView);
        TextView passwordTextView = login.findViewById(R.id.password);
        assertNotNull(passwordTextView);
        Button buttonChangePass = login.findViewById(R.id.changePass);
        assertNotNull(buttonChangePass);
        Button buttonSignUp = login.findViewById(R.id.signUp);
        assertNotNull(buttonSignUp);
        Button buttonSignIn = login.findViewById(R.id.signIn);
        assertNotNull(buttonSignIn);
    }

    @Test
    public void testButtons() {
        onView(withId(R.id.changePass)).perform(click());
        intended(hasComponent(ChangePassword.class.getName()));
        pressBack();
        onView(withId(R.id.signUp)).perform(click());
        intended(hasComponent(SignUp.class.getName()));
        pressBack();
        onView(withId(R.id.signIn)).perform(click());
        onView(withId(R.id.guide));
        onView(withText("Please enter username")).check(matches(isDisplayed()));
    }

    @After
    public void tearDown() throws Exception {
        login = null;
        Intents.release();
    }
}