package org.example.util;

public class StringUtil {
    public static String normalise(String S) {
        S = S.trim().toLowerCase();
        if(S.isEmpty()) {
            return null;
        }
        return S;
    }
}
