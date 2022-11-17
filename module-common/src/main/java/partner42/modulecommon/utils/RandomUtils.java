package partner42.modulecommon.utils;

import java.util.Random;

public class RandomUtils {

    private static final Random random = new Random();

    public static String getRandomString(Integer range) {
        return Character.toString((random.nextInt() / range));
    }
}
