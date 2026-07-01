package com.rafly;

import javax.swing.SwingUtilities;

import com.rafly.ai.ModelManager;
import com.rafly.gui.MainFrame;
import com.rafly.service.TextPreprocessor;

public class App {

    public static void main(String[] args) {

        ModelManager.getInstance();

        SwingUtilities.invokeLater(() -> {

            new MainFrame();

        });

    }

}