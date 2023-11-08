package com.example.leisuremap;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class HashPassword {
    private String password;

    public HashPassword(String password) {
        this.password = password;
    }

    public String hashing() {
        String bcryptHashString = BCrypt.withDefaults().hashToString(12, password.toCharArray());
        return bcryptHashString;
    }
}
