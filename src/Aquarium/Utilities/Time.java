package Aquarium.Utilities;


import Aquarium.Aquarium;

public class Time extends Thread{

    public Aquarium aquarium;

    public Time(Aquarium aquarium){
        this.aquarium = aquarium;
    }

    @Override
    public void run() {
        while(true) {
            this.aquarium.go();

        }
    }
}
