package Aquarium.Items;


import java.util.UUID;

public class Fish extends MobileItem{

    final static int MIN_FISH_WIDTH = 200;
    final static int MAX_FISH_WIDTH = 220;

    public Fish() {
        super(MIN_FISH_WIDTH, MAX_FISH_WIDTH);
    }
    public Fish(String itemID) {
        super(itemID);
    }


    @Override
    public int getMinWidth() {
        return MIN_FISH_WIDTH;
    }

    @Override
    public int getMaxWidth() {
        return MAX_FISH_WIDTH;
    }
}
