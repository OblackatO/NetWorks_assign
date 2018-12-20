package Aquarium;

import Aquarium.Items.AquariumItem;

import java.awt.*;
import java.util.Collection;

public interface Mobile {

    boolean move(Point destination); // false si on est arrivé à destination
    Point target(Collection<AquariumItem> neighbours); // décider de sa destination
}
