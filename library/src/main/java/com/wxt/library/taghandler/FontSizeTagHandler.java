package com.wxt.library.taghandler;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;

import com.wxt.library.retention.NotProguard;
import com.wxt.library.util.Util;

import org.xml.sax.XMLReader;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;

public class FontSizeTagHandler implements Html.TagHandler, Serializable {
    private static final String TAG_BLUE_FONT = "customfont";

    private Context myContext;
    private int startI = 0;
    private int stopI = 0;
    private final HashMap<String, String> attributes = new HashMap<String, String>();

    public FontSizeTagHandler(Context context) {
        this.myContext = context.getApplicationContext();
    }

    @Override
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
        processAttributes(xmlReader);
        if (tag.equalsIgnoreCase(TAG_BLUE_FONT)) {
            if (opening) {
                startFont(tag, output, xmlReader);
            } else {
                endFont(tag, output, xmlReader);
            }
        }
    }

    private void startFont(String tag, Editable output, XMLReader xmlReader) {
        startI = output.length();
    }

    private void endFont(String tag, Editable output, XMLReader xmlReader) {
        stopI = output.length();

        String color = attributes.get("color");
        String size = attributes.get("size");
        size = size.split("sp")[0];

        if (!TextUtils.isEmpty(color)) {
            output.setSpan(new ForegroundColorSpan(Color.parseColor(color)), startI, stopI, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (!TextUtils.isEmpty(size)) {
            output.setSpan(new AbsoluteSizeSpan(Util.sp2px(myContext, Integer.parseInt(size))), startI, stopI, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private void processAttributes(final XMLReader xmlReader) {
        try {
            Field elementField = xmlReader.getClass().getDeclaredField("theNewElement");
            elementField.setAccessible(true);
            Object element = elementField.get(xmlReader);
            Field attsField = element.getClass().getDeclaredField("theAtts");
            attsField.setAccessible(true);
            Object atts = attsField.get(element);
            Field dataField = atts.getClass().getDeclaredField("data");
            dataField.setAccessible(true);
            String[] data = (String[]) dataField.get(atts);
            Field lengthField = atts.getClass().getDeclaredField("length");
            lengthField.setAccessible(true);
            int len = (Integer) lengthField.get(atts);

            for (int i = 0; i < len; i++) {
                attributes.put(data[i * 5 + 1], data[i * 5 + 4]);
            }
        } catch (Exception e) {

        }
    }

}