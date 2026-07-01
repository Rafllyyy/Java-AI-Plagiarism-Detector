package com.rafly.ai;

public class ModelManager {

    private static ModelManager instance;

    private ModelManager() {

        System.out.println("========================");
        System.out.println("AI Model Manager Ready");
        System.out.println("========================");

    }

    public static ModelManager getInstance() {

        if(instance==null){

            instance=new ModelManager();

        }

        return instance;

    }

}