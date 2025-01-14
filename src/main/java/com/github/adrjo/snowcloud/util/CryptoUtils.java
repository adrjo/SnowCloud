package com.github.adrjo.snowcloud.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

import java.nio.charset.StandardCharsets;

public class CryptoUtils {

    //TODO: is this good?
    private static final int COST = 10;

    public static String hashPassword(String password) {
        return BCrypt.withDefaults().hashToString(COST, password.toCharArray());
    }

    public static boolean verifyHash(String password, String hash) {
        return BCrypt.verifyer().verify(password.getBytes(StandardCharsets.UTF_8), hash.getBytes(StandardCharsets.UTF_8)).verified;
    }
}
