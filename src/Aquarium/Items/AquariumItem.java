package Aquarium.Items;

import Aquarium.Utilities.RandomNumber;

import java.awt.*;
import java.util.Collection;
import java.util.UUID;

public abstract class AquariumItem {

    protected Point position;
    protected int width;
    protected int height;
    Image image;

    String ItemID;

    public AquariumItem(int min_width, int max_width, Image image){
        /**
         * It has a minimum width and max width, so the final width
         * will be calculated randomly according to min and max.
         * (not stated in the exercises, but more dynamic.)
         */
        this.width = RandomNumber.randomValue(min_width, max_width);
        this.image = image;
        this.height = (int) (0.33 * width);
        this.ItemID = UUID.randomUUID().toString();
    }

    public AquariumItem(String itemID, Image image){
        //TODO get width from other clients?
        this.width = 200;
        this.image = image;
        this.height = (int) (0.33 * this.width);
        this.ItemID = itemID;
    }

    public void setPosition(Point p){
        this.position = p;
    }
    public Point getPosition(){ return this.position;}

    public Rectangle rectangle(){
        Rectangle rectangle = new Rectangle(new Dimension(this.width, this.height));
        rectangle.x = this.position.x;
        rectangle.y = this.position.y;
        return rectangle;
    }

    public void draw(Graphics g) {

        g.drawImage(this.image, this.position.x,
                    this.position.y, this.width,
                    this.height, null);
    }


    public boolean intersects(Collection<AquariumItem> c){
        /**
         * Note that Rectangles have by default their own method
         * of intersects, and I use it here.
         */
        if(c.isEmpty()){
            return false;
        }
        for(AquariumItem item: c){
            if(this.rectangle().intersects(item.rectangle())){
                return true;
            }
        }
        return false;
    }

    public int getWidth(){
        return this.width;
    }

    public int getHeight(){
        return this.height;
    }

    public String getItemID(){
        return this.ItemID;
    }

    public abstract int getMinWidth();
    public abstract int getMaxWidth();


}
