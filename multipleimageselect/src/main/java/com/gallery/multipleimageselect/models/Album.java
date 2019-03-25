package com.gallery.multipleimageselect.models;

/**
 * Created by Darshan on 4/14/2015.
 */
public class Album {
    public String name;
    public String cover;
    public String picturePath;
    public Album(String name, String cover, String picturePath) {
        this.name = name;
        this.cover = cover;
        this.picturePath=picturePath;
    }
}
