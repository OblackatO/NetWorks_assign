package Aquarium.Items;

import Aquarium.Aquarium;
import Aquarium.Utilities.RandomNumber;

import java.awt.*;
import java.util.Collection;

public abstract class AquariumItemFactory<T extends AquariumItem>{

    public AquariumItemFactory() {}

    public abstract AquariumItem newItem();

    public Point generate_random_point(int width, int height){

        Point random_point = RandomNumber.randomPoint(0, Aquarium.getcoordinateX()
                - width, 0, Aquarium.getcoordinateY() - height);
        /*
        Point random_point = RandomNumber.randomPoint(0, 450
                - width, 0, 450 - height);
                */
        return random_point;
    }

    public boolean sink(Collection<AquariumItem> items, AquariumItem instance) {

        /**
         * The while loop makes that the Item find some place. If after 50 tries
         * the item does not find a place, then it is not added. This is done to avoid
         * an infinity loop.
         */

        int total_tries = 0;
        Point random_point;
        while(instance.intersects(items)) {
            random_point = this.generate_random_point(instance.width, instance.height);
            instance.setPosition(random_point);
            total_tries++;
            if (total_tries >= 50){
                return false;
            }
        }
        return true;
    }
}
