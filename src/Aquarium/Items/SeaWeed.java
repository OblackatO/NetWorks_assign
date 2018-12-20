package Aquarium.Items;

import java.awt.*;
import java.net.URL;

public class SeaWeed extends AquariumItem {

    final static int MIN_WIDTH = 100;
    final static int MAX_WIDTH = 120;

    //Image to seaweed
    private static URL u = ClassLoader.getSystemResource("Images/sea_weed.png");
    private static Image seaweedImage = Toolkit.getDefaultToolkit().createImage(u);


    public SeaWeed() {
        super(MIN_WIDTH, MAX_WIDTH, seaweedImage);
    }

    @Override
    public int getMinWidth() {
        return this.MIN_WIDTH;
    }

    @Override
    public int getMaxWidth() {
        return this.MAX_WIDTH;
    }
}
