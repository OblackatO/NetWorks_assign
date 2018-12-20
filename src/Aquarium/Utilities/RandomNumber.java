package Aquarium.Utilities;

import java.awt.*;

public class RandomNumber {

    public static int randomValue(int min, int max) {
        return ((int)(Math.random() * (max - min + 1)) + min);
    }

    public static Point randomPoint(int min_x, int max_x, int min_y, int max_y) {
        return new Point(randomValue(min_x, max_x), randomValue(min_y, max_y));
    }

}
