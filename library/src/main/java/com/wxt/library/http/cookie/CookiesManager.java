package com.wxt.library.http.cookie;

import android.content.Context;

import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * 自动管理Cookies
 */
public class CookiesManager implements CookieJar {
    private PersistentCookieStore cookieStore;

    public CookiesManager(Context context) {
        cookieStore = new PersistentCookieStore(context.getApplicationContext());
    }

    @Override
    public final void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        if (cookies != null && cookies.size() > 0) {
            for (Cookie item : cookies) {
                cookieStore.add(url, item);
            }
        }
    }

    @Override
    public final List<Cookie> loadForRequest(HttpUrl url) {
        return cookieStore.get(url);
    }

    public final void clearCookies(){
        cookieStore.clearCookies();
    }
}