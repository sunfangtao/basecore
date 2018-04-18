package com.wxt.library.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.wxt.library.retention.NotProguard;

import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;

/**
 * Created by SunFangtao on 2017/1/12.
 */
@NotProguard
public class SharedPreferenceUtil {

    private static SharedPreferenceUtil sharedPreferenceUtil;

    // 默认SharePreference存储的文件名称（使用包名）
    private String defaultName = "";
    // Application对象
    private Context context;
    // 指定文件名的前缀
    private String prefix;

    public static SharedPreferenceUtil getInstance(Context context) {
        if (sharedPreferenceUtil == null) {
            synchronized (SharedPreferenceUtil.class) {
                if (sharedPreferenceUtil == null) {
                    sharedPreferenceUtil = new SharedPreferenceUtil(context.getApplicationContext());
                }
            }
        }
        return sharedPreferenceUtil;
    }

    private SharedPreferenceUtil(Context context) {
        this.context = context;
        this.defaultName = context.getPackageName();
        this.prefix = this.defaultName + "_";
    }

    private SharedPreferences getPreference(String fileName) {
        SharedPreferences preferences = null;
        if (fileName == null || fileName.length() == 0) {
            preferences = context.getSharedPreferences(this.defaultName, Context.MODE_PRIVATE);
        } else {
            preferences = context.getSharedPreferences(this.prefix + fileName, Context.MODE_PRIVATE);
        }
        return preferences;
    }

    public Map<String, ?> getAllShareKey(String fileName) {
        try {
            return getPreference(fileName).getAll();
        } catch (NullPointerException e) {

        }
        return null;
    }

    /**
     * 清除文件
     *
     * @param fileName
     */
    public void removeParam(String fileName) {
        getPreference(fileName).edit().clear().commit();
    }

    public void removeParam(String fileName, String key) {
        getPreference(fileName).edit().remove(key).commit();
    }

    /**
     * 保存属性
     *
     * @param key
     * @param value
     */
    public boolean saveParam(String key, Object value) {
        return saveParam(this.defaultName, key, value);
    }

    /**
     * 保存属性
     *
     * @param fileName
     * @param key
     * @param value
     */
    public boolean saveParam(String fileName, String key, Object value) {
        if (TextUtils.isEmpty(key)) {
            return false;
        }

        SharedPreferences.Editor editor = getPreference(fileName).edit();

        if (value instanceof Number || value instanceof Boolean || value instanceof String) {
            editor.putString(key, "" + value).commit();
            return true;
        } else {
            // 创建字节输出流
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            // 创建对象输出流，并封装字节流
            ObjectOutputStream objectOutputStream = null;
            try {
                objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
                // 将对象写入字节流
                objectOutputStream.writeObject(value);
                // 将字节流编码成base64的字符窜
                String oAuth_Base64 = new String(Base64.encodeBase64(byteArrayOutputStream.toByteArray()));
                editor.putString(key, oAuth_Base64);
                editor.commit();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (OutOfMemoryError err) {
                // writeObject
                err.getMessage();
            } finally {
                try {
                    byteArrayOutputStream.close();
                    if (objectOutputStream != null)
                        objectOutputStream.close();
                } catch (IOException e) {

                }
            }
        }

        return false;
    }


    public <T> T readObj(String key) {
        return readObj(this.defaultName, key);
    }

    public <T> T readObj(String fileName, String key) {
        Object obj = readObject(fileName, key);
        if (obj != null) {
            return (T) obj;
        }
        return null;
    }

    public <T> List<T> readList(Class<T> clazz, String key) {
        return readList(this.defaultName, key);
    }

    public <T> List<T> readList(String fileName, String key) {
        Object obj = readObject(fileName, key);
        if (obj != null) {
            return (List<T>) obj;
        }
        return null;
    }

    private Object readObject(String fileName, String key) {
        if (TextUtils.isEmpty(key)) {
            return null;
        }
        SharedPreferences preferences = getPreference(fileName);

        String productBase64 = preferences.getString(key, "");
        // 读取字节
        byte[] base64 = Base64.decodeBase64(productBase64.getBytes());
        // 封装到字节流
        ByteArrayInputStream bais = new ByteArrayInputStream(base64);
        // 再次封装
        ObjectInputStream bis = null;
        try {
            bis = new ObjectInputStream(bais);
            // 读取对象
            return bis.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                bais.close();
                if (bis != null) {
                    bis.close();
                }
            } catch (Exception e) {

            }
        }
        return null;
    }

    public String readStrParam(String key, String defaultValue) {
        return readStrParam(this.defaultName, key, defaultValue);
    }

    public String readStrParam(String fileName, String key, String defaultValue) {
        SharedPreferences preferences = getPreference(fileName);
        return preferences.getString(key, defaultValue);
    }

    public int readIntParam(String key, int defaultValue) {
        return readIntParam(this.defaultName, key, defaultValue);
    }

    public int readIntParam(String fileName, String key, int defaultValue) {
        SharedPreferences preferences = getPreference(fileName);
        String value = preferences.getString(key, defaultValue + "");
        try {
            if (!TextUtils.isEmpty(value)) {
                return Integer.parseInt(value);
            }
        } catch (Exception e) {

        }
        return defaultValue;
    }

    public boolean readBooleanParam(String key, boolean defaultValue) {
        return readBooleanParam(this.defaultName, key, defaultValue);
    }

    public boolean readBooleanParam(String fileName, String key, boolean defaultValue) {
        SharedPreferences preferences = getPreference(fileName);
        String value = preferences.getString(key, defaultValue + "");
        try {
            if (!TextUtils.isEmpty(value)) {
                return Boolean.parseBoolean(value);
            }
        } catch (Exception e) {

        }
        return defaultValue;
    }

    public float readFloatParam(String key, float defaultValue) throws NumberFormatException {
        return readFloatParam(this.defaultName, key, defaultValue);
    }

    public float readFloatParam(String fileName, String key, float defaultValue) throws NumberFormatException {
        SharedPreferences preferences = getPreference(fileName);
        String value = preferences.getString(key, defaultValue + "");
        try {
            if (!TextUtils.isEmpty(value)) {
                return Float.parseFloat(value);
            }
        } catch (Exception e) {

        }
        return defaultValue;
    }

    public long readLongParam(String key, long defaultValue) throws NumberFormatException {
        return readLongParam(this.defaultName, key, defaultValue);
    }

    public long readLongParam(String fileName, String key, long defaultValue) throws NumberFormatException {
        SharedPreferences preferences = getPreference(fileName);
        String value = preferences.getString(key, defaultValue + "");
        try {
            if (!TextUtils.isEmpty(value)) {
                return Long.parseLong(value);
            }
        } catch (Exception e) {

        }
        return defaultValue;
    }

}
