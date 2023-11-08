package com.example.leisuremap;

import com.google.android.gms.maps.model.Marker;

import java.text.Normalizer;

public class Tests {

    //all methods that we wanted to test from the LeisureMap class were added here because to perform Unit Testing it is not possible to create an object of class which is associated with the activity
    //if the object of that class which is associated with the activity is created then while running tests it throws illegal null pointer exception

    public String removeDiacritics(String src) {
        return Normalizer
                .normalize(src, Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
    }
    public String removeCharsAfter(String itemSelected) {
        return itemSelected.replaceAll("\\|.*","");
    }
    public String removeCharsBefore(String itemSelected) {
        return itemSelected.substring(itemSelected.indexOf(": ")+1, itemSelected.lastIndexOf("k"));
    }
    public String removeSpaces(String itemSelected) {
        return itemSelected.trim();
    }

}
