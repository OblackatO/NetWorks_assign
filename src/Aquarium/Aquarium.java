package Aquarium;

import Aquarium.Items.*;
import Aquarium.Utilities.RandomNumber;
import Aquarium.Utilities.Time;
import Networking.UDPClient;

import javax.swing.*;
import java.awt.*;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;

public class Aquarium extends JPanel{

    private Collection<AquariumItem> items;
    private Collection<OwnedItems> externalItems;
    final private int NB_STONES = 20;
    final private int NB_SEAWEED = 15;
    final private int NB_FISHES = 5;

    private Time threadX;

    //Defines size of aquarium
    private static final int X_Coordinate = 800;
    private static final int Y_Coordinate = 800;

    private Image buffer;
    private Graphics gContext;

    //UDPClient
    private UDPClient aquarium_client;

    public Aquarium(){

        final Aquarium aquarium = this;
        Thread UDPClient_thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    aquarium_client = new UDPClient(Start.SERVER_PORT, Start.SERVER_IP, aquarium);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            }
        });
        UDPClient_thread.start();

        this.items = new ArrayList<AquariumItem>();
        this.externalItems = new ArrayList<OwnedItems>();
        this.setBackground(Color.BLUE);

        StoneFactory stone_factory = new StoneFactory();
        this.fill(stone_factory);

        SeaWeedFactory seaweed_factory = new SeaWeedFactory();
        this.fill(seaweed_factory);

        this.make_fishes();

        this.threadX = new Time(this);
        this.threadX.start();

    }

    public void fill(AquariumItemFactory factory){
        /**Fills aquarium with AquariumItems*/

        try{
            /**Creates and adds to items NB_Stones stones, using
             * the StoneFacotry class.
             */
            StoneFactory stone_factory = (StoneFactory) factory;
            for(int i=0; i<this.NB_STONES; ++i){
                AquariumItem stone_item = stone_factory.newItem();
                if(stone_factory.sink(this.items, stone_item)){
                    this.items.add(stone_item);
                }
            }

        }catch (ClassCastException e){
            /**Creates and adds to items NB_SEAWEED seaweeds, using
             * the SeaWeedFacotry class.
             */
            System.out.println("Cast to StoneFactory failed. SeaWeedFactory Detected.");
            SeaWeedFactory seaweed_factory = (SeaWeedFactory) factory;
            for(int i=0; i<this.NB_SEAWEED; ++i){
                AquariumItem seaweed_item = seaweed_factory.newItem();
                if(seaweed_factory.sink(this.items, seaweed_item)){
                    this.items.add(seaweed_item);
                }
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        /** Adds AquariumItems and paints them.
         * Please note that the method explained on the exercise
         * "items.elementAt(i)" does not work with java 10, hence
         * I use a simple for loop, which gives the same result."
         */
        /*
        super.paint(g);
        for(AquariumItem item: this.items){
            //System.out.println("Point X:"+item.getPosition().x);
            //System.out.println("Point Y"+item.getPosition().y);
            item.draw(g);
        }
        */
        g.drawImage(buffer, 0, 0, this);
    }

    public void make_fishes(){
        /**
         * Adds fishes to the Aquarium.
         */
        for(int i = 0; i < this.NB_FISHES; i++){
            this.items.add(new Fish());
        }
    }

    public void go(){

        for(AquariumItem item: this.items){
            if(item instanceof MobileItem) {
                Point random_point = RandomNumber.randomPoint(0, Aquarium.getcoordinateX()
                        - item.getWidth(), 0, Aquarium.getcoordinateY() - item.getHeight());

                MobileItem m_item = (MobileItem) item;
                m_item.move(random_point);
                this.aquarium_client.sendFish(item);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    System.out.println("Program was interrupted.");
                }

            }

        }
        this.draw();
    }

    private void draw(){
        if (this.buffer == null) {
            this.buffer = createImage(X_Coordinate, Y_Coordinate);
            if (this.buffer == null){
                throw new RuntimeException("An error occured while creating buffer for image.");
            }else{
                this.gContext = buffer.getGraphics();
            }

        }
        this.gContext.setColor(Color.BLUE);
        this.gContext.fillRect(0, 0, Aquarium.X_Coordinate, Aquarium.Y_Coordinate);
        for (AquariumItem item : items) {
            item.draw(this.gContext);
        }
        for (OwnedItems extItem : externalItems) {
            extItem.item.draw(this.gContext);
        }
        this.repaint();
    }

    /**
     * @return the coordinate x of the aquarium: width.
     */
    public static int getcoordinateX() {
        return Aquarium.X_Coordinate;
    }

    /**
     * @return the coordinate y of the aquarium: height.
     */
    public static int getcoordinateY() {
        return Aquarium.Y_Coordinate;
    }

    public UDPClient getUDPClient(){ return this.aquarium_client; }

    public void updateExternalFish(String clientID, String itemID, int posX, int posY){
        if (!isExtFishPresent(itemID)) {
            OwnedItems newFish = new OwnedItems();
            newFish.ownerID = clientID;
            newFish.item = new Fish(itemID);
            newFish.item.setPosition(new Point(posX, posY));

            externalItems.add(newFish);
        } else {
            OwnedItems fishToUpdate = null;
            for (OwnedItems oneItem: this.externalItems) {
                if( oneItem.item.getItemID().equals(itemID) ){
                    fishToUpdate = oneItem;
                }
            }
            fishToUpdate.item.setPosition(new Point(posX, posY));
        }
    }

    private boolean isExtFishPresent(String itemID){
        boolean isPresent = false;

        for (OwnedItems oneItem: this.externalItems) {
            if( oneItem.item.getItemID().equals(itemID) ){
                isPresent = true;
            }
        }

        return isPresent;
    }

    public void deleteExtFishFrom(String clientID) {
        for (OwnedItems oneItem: this.externalItems) {
            if( oneItem.item.getItemID().equals(clientID) ){
                externalItems.remove(oneItem);
            }
        }
    }

}
