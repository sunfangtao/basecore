package com.wxt.library.selectcity.model;

import android.text.TextUtils;

import com.wxt.library.retention.NotProguard;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NotProguard
public class City implements Serializable{
    private String name;
    private String province;
    private String pinyin;
    private String code;

    public City(String name, String province, String pinyin, String code) {
        this.name = name;
        this.province = province;
        this.pinyin = pinyin;
        this.code = code;
    }

    @Override
    public String toString() {
        return "City{" +
                "name='" + name + '\'' +
                ", province='" + province + '\'' +
                ", pinyin='" + pinyin + '\'' +
                ", code='" + code + '\'' +
                '}';
    }

    /***
     * 获取悬浮栏文本，（#、定位、热门 需要特殊处理）
     * @return
     */
    public String getSection() {
        if (TextUtils.isEmpty(pinyin)) {
            return "#";
        } else {
            String c = pinyin.substring(0, 1);
            Pattern p = Pattern.compile("[a-zA-Z]");
            Matcher m = p.matcher(c);
            if (m.matches()) {
                return c.toUpperCase();
            }
            //在添加定位和热门数据时设置的section就是‘定’、’热‘开头
            else if (TextUtils.equals(c, "定") || TextUtils.equals(c, "热"))
                return pinyin;
            else
                return "#";
        }
    }

    public String getName() {
        return name;
    }

    public String getPinyin() {
        return pinyin;
    }

    public String getProvince() {
        return province;
    }

    public String getCode() {
        return code;
    }

}
