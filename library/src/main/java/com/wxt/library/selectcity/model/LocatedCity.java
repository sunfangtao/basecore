package com.wxt.library.selectcity.model;

public final class LocatedCity extends City {

    public LocatedCity(String name, String province, String code) {
        super(name, province, "定位城市", code);
        if (name == null) {
            throw new IllegalArgumentException("name不能为空");
        }
    }
}
