package com.rafly;

import javax.swing.SwingUtilities;
import com.rafly.gui.MainFrame;

public class App {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            new MainFrame();

        });

    }

}