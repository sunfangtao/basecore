package com.wxt.library.selectimage.models;

import java.io.Serializable;

public class Album implements Serializable {
    public String name;
    public String cover;

    public Album(String name, String cover) {
        this.name = name;
        this.cover = cover;
    }
}
