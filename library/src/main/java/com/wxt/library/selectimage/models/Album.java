package com.wxt.library.selectimage.models;

import java.io.Serializable;

public class Album implements Serializable {
    public long id;
    public String name;
    public String cover;

    public Album(long id,String name, String cover) {
        this.id = id;
        this.name = name;
        this.cover = cover;
    }
}
