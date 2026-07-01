package com.rafly.model;

public class ComparisonResult {

    private final String fileName;
    private final double similarity;

    public ComparisonResult(String fileName,double similarity){

        this.fileName=fileName;
        this.similarity=similarity;

    }

    public String getFileName(){
        return fileName;
    }

    public double getSimilarity(){
        return similarity;
    }

}