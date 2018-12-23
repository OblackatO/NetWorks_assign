package Aquarium;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Animation extends JFrame {

    private final Aquarium aquarium;

    public Animation(){

        Aquarium new_aquarium = new Aquarium();
        this.aquarium = new_aquarium;
        this.add(new_aquarium);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                HandleCloseOperation();

            }
        });
    }

    private void HandleCloseOperation(){
        /** Confirms if the user wants to close the window
         * or not, as stated in the exercise.
         */

        int choice = JOptionPane.showConfirmDialog(null,
                "êtes-vous sûr de vouloir quitter ?", "Attention!",
                JOptionPane.YES_NO_OPTION);

        if(choice == JOptionPane.YES_OPTION) {
            //asks the server to be disconnected, so all the other clients know
            //it will not send its items'positions anymore.
            if(this.aquarium.getUDPClient().DISCONNECTRequest()){
                System.out.println("[>]Disconnection from the server successful.");
            }else{
                System.out.println("[>]A problem occurred, while disconnecting from the server..");
            }
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        }else{
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        }
    }



}
