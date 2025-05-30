package dk.util;

import java.security.SecureRandom;

public class Generator {
    private static final SecureRandom random = new SecureRandom();

    //max 10
    public static String generate(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10)); // Generates a digit between 0 and 9
        }
        return sb.toString();
    }
}
