package Aquarium.Items;

import Aquarium.Aquarium;
import Aquarium.Mobile;
import Aquarium.Utilities.RandomNumber;

import java.awt.*;
import java.net.URL;
import java.util.Collection;


public abstract class MobileItem extends AquariumItem implements Mobile {

    int MAX_WIDTH;
    int MIN_WIDTH;
    final int SPEED = 3;

    //Image to fish
    private static URL u = ClassLoader.getSystemResource("Images/fish.png");
    private static Image fishImage = Toolkit.getDefaultToolkit().createImage(u);

    public MobileItem(int min_width, int max_width) {
        super(min_width, max_width, fishImage);
        this.MAX_WIDTH = max_width;
        this.MIN_WIDTH = min_width;
        Point random_point = RandomNumber.randomPoint(0, Aquarium.getcoordinateX()
                - super.width, 0, Aquarium.getcoordinateY() - super.height);
        super.setPosition(random_point);
    }

    @Override
    public boolean move(Point destination){
        /**
         *  cos and sin are used to convert a point
         *  into a direction in the graphic user interface.
         *  It is pretty much the same conception as converting
         *  the time of an analog clock to its pointers position.
         */

        int direction_coordinate_x = destination.x - super.position.x;
        int direction_coordinate_y = destination.y - super.position.y;

        double direction = (double) Math.atan2(direction_coordinate_x, direction_coordinate_y);

        double width_arg = this.MAX_WIDTH/super.width;
        double speed = 3*width_arg;

        int cor_x = (int) (speed * Math.cos(direction));
        int cor_y = (int) (speed * Math.sin(direction));
        Point p = this.position;
        p.translate(cor_x, cor_y);
        boolean stays_in_aquarium = ((p.getX()) >= 0) &&
                ((p.getX()) <= Aquarium.getcoordinateX() - this.width) &&
                ((p.getY()) >= 0) &&
                ((p.getY()) <= Aquarium.getcoordinateY() - this.height);

        if(stays_in_aquarium){

            setPosition(p);
        }

        return false; // false si on est arrivé à destination
    }

    //PROBABLY THIS METHOD DOES NOT NEED TO BE HERE.
    @Override
    public Point target(Collection<AquariumItem> neighbours){
        /**
         * Checks if some fish will be upon one of the already added AquariumItems.
         * This method is similar to the method sink in the AquariumItemFactory class,
         * but it returns a Point and not a boolean. Furthermore, if after 50 tries a
         * random Point is not found, then an already existing Point is returned. This is
         * to avoid an infinite loop.
         */
        int total_tries = 0;
        Point random_point = null;
        while(this.intersects(neighbours)) {
            random_point = RandomNumber.randomPoint(0, Aquarium.getcoordinateX()
                    - this.width, 0, Aquarium.getcoordinateY() - this.height);
            this.setPosition(random_point);
            total_tries++;
            if (total_tries >= 50){
                return random_point;
            }
        }
        return random_point;

    }
}
