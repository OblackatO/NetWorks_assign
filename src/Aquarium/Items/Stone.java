package Aquarium.Items;

import java.awt.*;
import java.net.URL;

public class Stone extends AquariumItem {

    final static int MIN_WIDTH = 120;
    final static int MAX_WIDTH = 140;

    //Image to stone
    private static URL u = ClassLoader.getSystemResource("Images/sea_stone.png");
    private static Image stoneImage = Toolkit.getDefaultToolkit().createImage(u);

    public Stone() {
        super(MIN_WIDTH, MAX_WIDTH, stoneImage);
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
