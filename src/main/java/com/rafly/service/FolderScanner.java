package com.rafly.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FolderScanner {

    public List<File> scan(File folder){

        List<File> result=new ArrayList<>();

        File[] files=folder.listFiles();

        if(files==null)
            return result;

        for(File f:files){

            if(!f.isFile())
                continue;

            String n=f.getName().toLowerCase();

            if(n.endsWith(".txt")||
               n.endsWith(".docx")||
               n.endsWith(".pdf")){

                result.add(f);

            }

        }

        return result;

    }

}