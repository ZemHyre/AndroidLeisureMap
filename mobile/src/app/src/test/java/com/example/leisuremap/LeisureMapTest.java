package com.example.leisuremap;

import static org.junit.Assert.*;

import com.google.android.gms.maps.model.Marker;

import org.junit.Test;

public class LeisureMapTest {

    @Test
    public void removeCharsAfterSymbol () {
        //removes symbol '|' and chars after symbol '|', extracts location name from a string
        Tests tests = new Tests();
        String s = "Vilnius | distance: 85";
        String expected = "Vilnius ";
        assertEquals(expected, tests.removeCharsAfter(s));
    }

    @Test
    public void removeDiacriticsFromWord () {
        //removes diacritic letters
        Tests tests = new Tests();
        String s = "Žąsynė";
        String expected = "Zasyne";
        assertEquals(expected, tests.removeDiacritics(s));
    }

    @Test
    public void removeCharsBeforeSymbol () {
        //removes symbol ':' and chars before symbol ':', extracts kilometers from a string
        Tests tests = new Tests();
        String s = "Vilnius | distance: 85 km";
        String expected = " 85 ";
        assertEquals(expected, tests.removeCharsBefore(s));
    }

    @Test
    public void removeWhiteSpaces () {
        //removes leading and trailing white spaces from a string
        Tests tests = new Tests();
        String s = " 85  ";
        String expected = "85";
        assertEquals(expected, tests.removeSpaces(s));
    }

}
