package Aquarium.Items;

import java.awt.*;

public class StoneFactory extends AquariumItemFactory<Stone> {

    @Override
    public AquariumItem newItem() {
        AquariumItem aquarium_item = new Stone();
        Point random_point = super.generate_random_point(aquarium_item.width, aquarium_item.height);
        aquarium_item.setPosition(random_point);
        return aquarium_item;
    }

}
