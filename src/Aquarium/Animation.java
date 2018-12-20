package Aquarium;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Animation extends JFrame {


    public Animation(){

        this.add(new Aquarium());

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
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        }else{
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        }
    }



}
