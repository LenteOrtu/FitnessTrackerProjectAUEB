package com.example.ds_frontend;

public class FileMetadata {

    final private String name;
    final private long size;

    public FileMetadata(String name, long size){
        this.name = name;
        this.size = size;
    }

    public String getName(){
        return name;
    }

    public long getSize(){
        return size;
    }
}
