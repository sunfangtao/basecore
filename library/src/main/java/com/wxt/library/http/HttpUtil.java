package com.wxt.library.http;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;

import com.wxt.library.base.application.BaseApplication;
import com.wxt.library.contanst.Constant;
import com.wxt.library.http.cookie.CookiesManager;
import com.wxt.library.http.exception.HttpExceptionHandler;
import com.wxt.library.http.listener.BaseHttpParseListener;
import com.wxt.library.http.listener.HttpParseListener;
import com.wxt.library.http.listener.SimpleHttpParseListener;
import com.wxt.library.http.parse.HttpParseHelper;
import com.wxt.library.http.util.HttpPrintUtil;
import com.wxt.library.priva.listener.ActivityStateChangedListener;
import com.wxt.library.priva.listener.FragmentStateChangedListener;
import com.wxt.library.priva.util.ActivityChangedUtil;
import com.wxt.library.priva.util.FragmentChangedUtil;
import com.wxt.library.util.JSONUtil;
import com.wxt.library.util.Util;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.FileNameMap;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Administrator on 2018/3/26.
 */

public final class HttpUtil implements ActivityStateChangedListener, FragmentStateChangedListener {

    private final String DEFAULT_TYPE = "DEFAULT_TYPE";

    private ResponderHandler handler;
    private Map<String, String> defaultParamsMap;
    private Map<BaseHttpParseListener, List<Map<String, Call>>> callMap;
    private Map<BaseHttpParseListener, Call> callList;
    private static CookiesManager cookiesManager = new CookiesManager(BaseApplication.getInstance().getApplicationContext());
    private static Map<Long, ThreadHttpClient> clientMap = new HashMap<>();

    private HttpExceptionHandler httpExceptionHandler;

    private static HttpUtil instance = null;

    public final static synchronized HttpUtil getInstance(int... times) {
        if (instance == null) {
            synchronized (HttpUtil.class) {
                if (instance == null) {
                    instance = new HttpUtil();
                }
            }
        }
        setTimeOut(times);
        return instance;
    }

    private static void setTimeOut(int... times) {
        long threadId = Thread.currentThread().getId();
        ThreadHttpClient threadHttpClient = clientMap.get(threadId);
        if (threadHttpClient == null) {
            threadHttpClient = new ThreadHttpClient();
            threadHttpClient.connectTime = Math.max(times.length > 0 ? times[0] : 10_000, 2_000);
            threadHttpClient.readTime = Math.max(times.length > 1 ? times[1] : 10_000, 2_000);
            threadHttpClient.writeTime = Math.max(times.length > 2 ? times[2] : 10_000, 2_000);
            threadHttpClient.okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(threadHttpClient.connectTime, TimeUnit.MILLISECONDS)
                    .readTimeout(threadHttpClient.readTime, TimeUnit.MILLISECONDS)
                    .writeTimeout(threadHttpClient.writeTime, TimeUnit.MILLISECONDS)
                    .cookieJar(cookiesManager)
                    .build();
            clientMap.put(threadId, threadHttpClient);
        } else {
            ThreadHttpClient tempClient = new ThreadHttpClient();
//            tempClient.connectTime = Math.max(times.length > 0 ? times[0] : threadHttpClient.connectTime, 2_000);
//            tempClient.readTime = Math.max(times.length > 1 ? times[1] : threadHttpClient.readTime, 2_000);
//            tempClient.writeTime = Math.max(times.length > 2 ? times[2] : threadHttpClient.writeTime, 2_000);

            tempClient.connectTime = Math.max(times.length > 0 ? times[0] : 10_000, 2_000);
            tempClient.readTime = Math.max(times.length > 1 ? times[1] : 10_000, 2_000);
            tempClient.writeTime = Math.max(times.length > 2 ? times[2] : 10_000, 2_000);

            if (!tempClient.equals(threadHttpClient)) {

                threadHttpClient.connectTime = tempClient.connectTime;
                threadHttpClient.readTime = tempClient.readTime;
                threadHttpClient.writeTime = tempClient.writeTime;

                threadHttpClient.okHttpClient = new OkHttpClient.Builder()
                        .connectTimeout(threadHttpClient.connectTime, TimeUnit.MILLISECONDS)
                        .readTimeout(threadHttpClient.readTime, TimeUnit.MILLISECONDS)
                        .writeTimeout(threadHttpClient.writeTime, TimeUnit.MILLISECONDS)
                        .cookieJar(cookiesManager)
                        .build();
            }
        }
    }

    private HttpUtil() {
        setTimeOut();
        handler = new ResponderHandler(Looper.getMainLooper());
        defaultParamsMap = new HashMap<>();
        callList = new HashMap<>();
        callMap = new HashMap<>();
        ActivityChangedUtil.getInstance().addActivityChangedListener(this);
        FragmentChangedUtil.getInstance().addFragmentChangedListener(this);
    }

    public final void clearCookies() {
        cookiesManager.clearCookies();
    }

    public final void cancelAllCall(HttpParseListener listener) {
        synchronized (callMap) {
            List<Map<String, Call>> callList = callMap.get(listener);
            if (callList != null) {
                int length = callList.size();
                for (int i = 0; i < length; i++) {
                    for (Call call : callList.get(i).values()) {
                        if (!call.isCanceled())
                            call.cancel();
                    }
                }
                callList.clear();
            }
            callMap.remove(listener);
        }
    }

    public final void cancel(SimpleHttpParseListener listener) {
        synchronized (callList) {
            if (listener != null) {
                Call call = callList.get(listener);
                if (call != null && !call.isCanceled()) {
                    call.cancel();
                }
                callList.remove(listener);
            }
        }
    }

    public final void cancel(HttpParseListener listener, String type) {
        synchronized (callMap) {
            if (listener == null || type == null) {
                return;
            }
            List<Map<String, Call>> callList = callMap.get(listener);
            if (callList == null) {
                return;
            }
            int length = callList.size();
            for (int i = 0; i < length; i++) {
                Map<String, Call> map = callList.get(i);
                if (map == null) {
                    continue;
                }
                if (map.containsKey(type)) {
                    if (!map.get(type).isCanceled()) {
                        map.get(type).cancel();
                        map.remove(type);
                    }
                    if (map.size() == 0) {
                        callList.remove(i);
                        if (callList.size() == 0) {
                            callMap.remove(listener);
                        }
                    }
                    return;
                }
            }
        }
    }

    private final void addCall(String type, Call call, BaseHttpParseListener listener) {
        synchronized (callMap) {
            if (listener instanceof HttpParseListener) {
                List<Map<String, Call>> callList = callMap.get(listener);
                if (callList == null) {
                    callList = new ArrayList<>();
                    callMap.put(listener, callList);
                }
                Map<String, Call> map = new HashMap();
                map.put(type, call);
                callList.add(map);
            } else {
//                callList.put(listener, call);
            }
        }
    }

    private HttpParseHelper.ReturnObject getResponseReturnObject(Response response, BaseHttpParseListener listener, String type, Call call) {
        if (listener != null) {
            HttpParseHelper parseHelper = listener.getParseHelper();
            if (parseHelper != null) {
                return parseHelper.parse(type, call, response);
            }
        }
        return null;
    }

    private HttpParseHelper.ReturnObject getFailReturnObject(String url, String type, IOException e) {
        HttpParseHelper.ReturnObject returnObject = new HttpParseHelper.ReturnObject();
        returnObject.isSuccess = false;
        returnObject.httpType = type;
        returnObject.url = url;
        returnObject.stateCode = 0;
        returnObject.failReason = "请稍后再试";
        if (e instanceof SocketTimeoutException) {
            // TODO 请求超时
            returnObject.resultType = Constant.ReturnType.CONNECT_FAIL;
        } else if (e instanceof UnknownHostException) {
            returnObject.resultType = Constant.ReturnType.UNKNOWN_HOST_EXCEPTION;
        } else if (e instanceof ConnectException) {
            // 无网络
            returnObject.resultType = Constant.ReturnType.NO_NETWORK;
        } else if (e instanceof SocketException || e instanceof IOException) {
            // 用户取消网络请求
            returnObject.failReason = "用户取消请求";
            returnObject.resultType = Constant.ReturnType.CANCLE;
        } else {
            // 其他情况
            returnObject.failReason = "未知" + e.toString();
            returnObject.resultType = Constant.ReturnType.UNKNOW;
        }

        if (HttpPrintUtil.isShowHttpLog(type))
            Util.print("返回数据 type=" + type + " url=" + url + " failReason=" + returnObject.failReason);
        return returnObject;
    }

    private OkHttpClient getThreadClient() {
        long threadId = Thread.currentThread().getId();
        if (clientMap.containsKey(threadId)) {
            ThreadHttpClient client = clientMap.get(threadId);
            if (threadId != Looper.getMainLooper().getThread().getId())
                clientMap.remove(threadId);
            if (client != null) {
                return client.okHttpClient;
            }
        }
        throw new IllegalArgumentException("没有找到线程");
    }

    public final <T extends Object> void sendGet(String url, final Map<String, T> paramsMap, Class<?>... clazz) {
        sendGet(null, null, url, paramsMap, clazz);
    }

    public final <T extends Object> void sendGet(final BaseHttpParseListener listener, String url, final Map<String, T> paramsMap, Class<?>... clazz) {
        sendGet(listener, null, url, paramsMap, clazz);
    }

    public final <T extends Object> void sendGet(final BaseHttpParseListener listener, String type, String url, Map<String, T> paramsMap, Class<?>... clazz) {

        if (listener != null) {
            if (listener instanceof HttpParseListener && TextUtils.isEmpty(type)) {
                // 使用Activity实现接口方式，统一处理返回结果，需要要指定type
                throw new IllegalArgumentException("通过实现接口方式发起请求，请指定type！");
            }
        }

        if (paramsMap == null) {
            paramsMap = new HashMap<>();
        }

        for (String key : defaultParamsMap.keySet()) {
            if (!paramsMap.containsKey(key)) {
                paramsMap.put(key, (T) defaultParamsMap.get(key));
            }
        }

        if (paramsMap.size() > 0) {
            StringBuffer sb = new StringBuffer("?");
            for (String key : paramsMap.keySet()) {
                Object value = paramsMap.get(key);
                if (value == null)
                    continue;
                if (value instanceof String && value.equals("")) {
                    continue;
                }
                if (value instanceof Number || value instanceof String || value instanceof Boolean || value instanceof StringBuffer) {
                    // get请求只处理上述3中类型
                    sb.append(key).append("=").append(value).append("&");
                }
            }
            url = url + sb.toString().substring(0, sb.length() - 1);
        }

        Request request = new Request.Builder().url(url).build();

        String resultType = convertType(type);
        if (HttpPrintUtil.isShowHttpLog(resultType))
            Util.print("type = " + resultType + " url = " + url);

        Call call = getThreadClient().newCall(request);
        if (listener != null) {
            addCall(resultType, call, listener);
        }
        call.enqueue(new SimpleOkHttpCallBack(listener, resultType, (clazz != null && clazz.length > 0) ? clazz[0] : null));
    }

    public final <T extends Object> void sendPost(String url, final Map<String, T> paramsMap, Class<?>... clazz) {
        sendPost(null, null, url, paramsMap, clazz);
    }

    public final <T extends Object> void sendPost(final BaseHttpParseListener listener, String url, final Map<String, T> paramsMap, Class<?>... clazz) {
        sendPost(listener, null, url, paramsMap, clazz);
    }

    public final <T extends Object> void sendPost(final BaseHttpParseListener listener, String type, final String url, Map<String, T> paramsMap, Class<?>... clazz) {

        if (listener != null) {
            if (listener instanceof HttpParseListener && TextUtils.isEmpty(type)) {
                // 使用Activity实现接口方式，统一处理返回结果，需要要指定type
                throw new IllegalArgumentException("通过实现接口方式发起请求，请指定type！");
            }
        }
        Map<String, List<File>> fileMap = new HashMap<>();

        if (paramsMap == null) {
            paramsMap = new HashMap<>();
        }

        for (String key : defaultParamsMap.keySet()) {
            if (!paramsMap.containsKey(key)) {
                paramsMap.put(key, (T) defaultParamsMap.get(key));
            }
        }

        Request request = null;

        StringBuffer sb = new StringBuffer(url);

        if (paramsMap.size() > 0) {
            for (String key : paramsMap.keySet()) {
                Object value = paramsMap.get(key);
                if (value instanceof File) {
                    List<File> fileList = fileMap.get(key);
                    if (fileList == null) {
                        fileList = new ArrayList<>();
                        fileMap.put(key, fileList);
                    }
                    fileList.add((File) value);
                } else if (value instanceof List) {
                    List<File> fileList = fileMap.get(key);
                    if (fileList == null) {
                        fileList = new ArrayList<>();
                        fileMap.put(key, fileList);
                    }
                    List<?> list = (List<?>) value;
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i) instanceof File) {
                            fileList.add((File) list.get(i));
                        }
                    }
                } else if (value instanceof File[]) {
                    List<File> fileList = fileMap.get(key);
                    if (fileList == null) {
                        fileList = new ArrayList<>();
                        fileMap.put(key, fileList);
                    }
                    File[] files = (File[]) value;
                    for (int i = 0; i < files.length; i++) {
                        fileList.add(files[i]);
                    }
                }
            }

            if (fileMap.size() > 0) {

                sb.append("?");

                MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
                for (String key : paramsMap.keySet()) {
                    Object value = paramsMap.get(key);
                    if (value == null)
                        continue;
                    if (value instanceof String && value.equals("")) {
                        continue;
                    }
                    if (value instanceof Number || value instanceof String || value instanceof Boolean || value instanceof StringBuffer) {
                        // get请求只处理上述3中类型
                        builder.addFormDataPart(key, null, RequestBody.create(MediaType.parse("application/x-www-form-urlencoded;charset=utf-8"), value + ""));
                        sb.append(key).append("=").append(value).append("&");
                    }
                }
                // 添加文件
                for (String key : fileMap.keySet()) {
                    List<File> fileList = fileMap.get(key);
                    for (int i = 0; i < fileList.size(); i++) {
                        File file = fileList.get(i);
                        //根据文件的后缀名，获得文件类型
                        String fileType = getMimeType(file.getName());
                        builder.addFormDataPart( //给Builder添加上传的文件
                                key,  //请求的名字
                                file.getName(), //文件的文字，服务器端用来解析的
                                RequestBody.create(MediaType.parse(fileType), file) //创建RequestBody，把上传的文件放入
                        );
                    }
                }
                request = new Request.Builder().url(url).post(builder.build()).build();
            } else {
                FormBody.Builder builder = new FormBody.Builder(Charset.forName("utf-8"));
                if (paramsMap.size() > 0) {
                    sb.append("?");
                }
                for (String key : paramsMap.keySet()) {
                    Object value = paramsMap.get(key);
                    if (value == null)
                        continue;
                    if (value instanceof String && value.equals("")) {
                        continue;
                    }
                    if (value instanceof Number || value instanceof String || value instanceof Boolean || value instanceof StringBuffer) {
                        // get请求只处理上述3中类型
                        builder.add(key, value + "");
                        sb.append(key).append("=").append(value).append("&");
                    }
                }
                request = new Request.Builder().url(url).post(builder.build()).build();
            }

        } else {
            request = new Request.Builder().url(url).post(new FormBody.Builder(Charset.forName("utf-8")).build()).build();
        }

        String resultType = convertType(type);

        if (HttpPrintUtil.isShowHttpLog(resultType))
            Util.print("type = " + resultType + " url = " + sb.toString().substring(0, sb.length() - 1));

        Call call = getThreadClient().newCall(request);
        if (listener != null) {
            addCall(resultType, call, listener);
        }
        call.enqueue(new SimpleOkHttpCallBack(listener, resultType, (clazz != null && clazz.length > 0) ? clazz[0] : null));
    }

    private String convertType(String type) {
        if (DEFAULT_TYPE.equals(type)) {
            throw new IllegalArgumentException(DEFAULT_TYPE + "为系统使用，请更换！");
        }
        return TextUtils.isEmpty(type) ? DEFAULT_TYPE : type;
    }

    private void routeHttpResponse(Call call, Class<?> clazz, BaseHttpParseListener listener, HttpParseHelper.ReturnObject returnObject) {
        if (listener == null) {
            return;
        }

        InnerModel innerModel = new InnerModel();
        innerModel.listener = listener;
        innerModel.returnObject = returnObject;

        if (listener instanceof HttpParseListener) {
            synchronized (callMap) {
                // 清理对应的请求map
                List<Map<String, Call>> callList = callMap.get(listener);
                if (callList != null) {
                    boolean isBreak = false;
                    int length = callList.size();
                    for (int i = 0; i < length; i++) {
                        Map<String, Call> map = callList.get(i);
                        for (Call mapCall : map.values()) {
                            if (mapCall.equals(call)) {
                                callList.remove(i);
                                if (callList.size() == 0) {
                                    callMap.remove(listener);
                                }
                                // 找到后退出循环
                                isBreak = true;
                            }
                        }
                        if (isBreak) {
                            break;
                        }
                    }
                }
            }
        } else if (listener instanceof SimpleHttpParseListener) {
            synchronized (callList) {
                // 清理对应的请求map
                callList.remove(listener);
            }
        }

        if (returnObject != null) {
            if (listener instanceof SimpleHttpParseListener) {
                try {
                    Type type = listener.getClass().getGenericSuperclass();
                    if (type instanceof ParameterizedType) {
                        Type actualTypeArgument = ((ParameterizedType) type).getActualTypeArguments()[0];
                        clazz = (Class) actualTypeArgument;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (clazz != null && !clazz.getName().equals("java.lang.Object")) {
                try {
                    // 需要在子线程进行解析，完成后通知主线程
                    if (returnObject.jsonObject != null) {
                        returnObject.parseObj = JSONUtil.toJavaBean(clazz, returnObject.jsonObject);
                    } else if (returnObject.jsonArray != null) {
                        returnObject.parseObj = JSONUtil.toJavaBeanList(clazz, returnObject.jsonArray);
                    }
                } catch (Exception e) {

                }
            }

            Message message = new Message();
            message.obj = innerModel;
            handler.sendMessage(message);
        }

        // 对接 BugListener
        if (httpExceptionHandler != null && returnObject != null) {
            if ((Constant.ReturnType.CONNECT_FAIL.equals(returnObject.resultType)
                    || Constant.ReturnType.FORMAT_ERROR.equals(returnObject.resultType)
                    || Constant.ReturnType.NO_NETWORK.equals(returnObject.resultType)
                    || Constant.ReturnType.NOT_FOUND.equals(returnObject.resultType)
                    || Constant.ReturnType.SERVER_EXCEPTION.equals(returnObject.resultType)
                    || Constant.ReturnType.UNKNOW.equals(returnObject.resultType)
                    || Constant.ReturnType.UNKNOWN_HOST_EXCEPTION.equals(returnObject.resultType))
                    && !Constant.HttpPrivateKey.AUTO_UPLOAD.equals(returnObject.httpType)
                    && !returnObject.isSuccess) {
                httpExceptionHandler.onHttpFail(returnObject);
            }

//            if (!returnObject.isSuccess
//                    && !Constant.HttpPrivateKey.AUTO_UPLOAD.equals(returnObject.httpType)
//                    && !Constant.ReturnType.CANCLE.equals(returnObject.resultType)) {
//                // 上传bug失败不做处理;用户取消请求不做处理
//                httpExceptionHandler.onHttpFail(returnObject);
//            }
        }
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        if (httpExceptionHandler == null) {
            httpExceptionHandler = new HttpExceptionHandler(activity.getApplicationContext());
        }
        String params = Util.getMetaValue(activity, Constant.HttpPrivateParam.HTTP_PARAMS);
        if (!TextUtils.isEmpty(params)) {
            String[] paramArr = params.split(";");
            int length = paramArr.length;
            for (int i = 0; i < length; i++) {
                String value = paramArr[i];
                if (value.contains(":")) {
                    defaultParamsMap.put(value.split(":")[0], value.split(":")[1]);
                }
            }
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        if (activity instanceof HttpParseListener) {
            cancelAllCall((HttpParseListener) activity);
        }
    }

    @Override
    public void onFragmentAttach(Fragment fragment, Activity activity) {

    }

    @Override
    public void onFragmentViewStateRestored(Fragment fragment, Bundle savedInstanceState) {

    }

    @Override
    public View onFragmentCreateView(Fragment fragment, View view) {
        return null;
    }

    @Override
    public void onFragmentDestroyView(Fragment fragment) {

    }

    @Override
    public void onFragmentResume(Fragment fragment) {

    }

    @Override
    public void onFragmentPause(Fragment fragment) {

    }

    @Override
    public void onFragmentDestroy(Fragment fragment) {
        if (fragment instanceof HttpParseListener) {
            cancelAllCall((HttpParseListener) fragment);
        }
    }

    @Override
    public void onFragmentDetach(Fragment fragment) {

    }

    private static class ResponderHandler extends Handler {

        ResponderHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            try {
                InnerModel innerModel = (InnerModel) msg.obj;
                HttpParseHelper.ReturnObject returnObject = innerModel.returnObject;
                BaseHttpParseListener listener = innerModel.listener;

                if (listener != null) {
                    if (Constant.ReturnType.CANCLE.equals(returnObject.resultType)) {
                        // 取消的请求不处理
                        return;
                    }
                    if (listener instanceof SimpleHttpParseListener) {
                        SimpleHttpParseListener simpleHttpParseListener = (SimpleHttpParseListener) listener;
                        if (returnObject.isSuccess) {
                            if (returnObject.jsonObject != null) {
                                simpleHttpParseListener.onHttpSuccess(returnObject.httpType, returnObject.jsonObject, returnObject.parseObj);
                                simpleHttpParseListener.onHttpSuccess(returnObject.httpType, returnObject.json, returnObject.parseObj);
                            } else if (returnObject.jsonArray != null) {
                                simpleHttpParseListener.onHttpSuccess(returnObject.httpType, returnObject.jsonArray, returnObject.page, returnObject.pageSize, returnObject.count, (List) returnObject.parseObj);
                                simpleHttpParseListener.onHttpSuccess(returnObject.httpType, returnObject.json, returnObject.page, returnObject.pageSize, returnObject.count, (List) returnObject.parseObj);
                            }
                        } else {
                            simpleHttpParseListener.onHttpFailure(returnObject.httpType, returnObject.stateCode, returnObject.failReason, returnObject.resultType);
                        }
                    } else if (listener instanceof HttpParseListener) {
                        HttpParseListener httpParseListener = (HttpParseListener) listener;
                        if (returnObject.isSuccess) {
                            if (returnObject.jsonObject != null) {
                                httpParseListener.onHttpSuccess(returnObject.httpType, returnObject.jsonObject, returnObject.parseObj);
                                httpParseListener.onHttpSuccess(returnObject.httpType, returnObject.json, returnObject.parseObj);
                            } else if (returnObject.jsonArray != null) {
                                httpParseListener.onHttpSuccess(returnObject.httpType, returnObject.jsonArray, returnObject.page, returnObject.pageSize, returnObject.count, (List) returnObject.parseObj);
                                httpParseListener.onHttpSuccess(returnObject.httpType, returnObject.json, returnObject.page, returnObject.pageSize, returnObject.count, (List) returnObject.parseObj);
                            }
                        } else {
                            httpParseListener.onHttpFailure(returnObject.httpType, returnObject.stateCode, returnObject.failReason, returnObject.resultType);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                throw new IllegalArgumentException(e.getMessage());
            }
        }
    }

    private String getMimeType(String filename) {
        FileNameMap filenameMap = URLConnection.getFileNameMap();
        String contentType = filenameMap.getContentTypeFor(filename);
        if (contentType == null) {
            contentType = "application/octet-stream"; //* exe,所有的可执行程序
        }
        return contentType;
    }

    private class SimpleOkHttpCallBack implements Callback {

        private Class<?> clazz;
        private String type;
        private BaseHttpParseListener listener;

        public SimpleOkHttpCallBack(BaseHttpParseListener listener, String type, Class<?> clazz) {
            this.listener = listener;
            this.type = type;
            this.clazz = clazz;
        }

        @Override
        public void onFailure(Call call, IOException e) {
            routeHttpResponse(call, clazz, listener, getFailReturnObject(call.request().url() + "", type, e));
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            routeHttpResponse(call, clazz, listener, getResponseReturnObject(response, listener, type, call));
        }

    }

    private static class InnerModel {
        public BaseHttpParseListener listener;
        public HttpParseHelper.ReturnObject returnObject;
    }

    public static class ThreadHttpClient {
        public int connectTime;
        public int readTime;
        public int writeTime;
        public OkHttpClient okHttpClient;

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ThreadHttpClient) {
                ThreadHttpClient client = (ThreadHttpClient) obj;
                if (client.connectTime == connectTime && client.readTime == readTime && client.writeTime == writeTime) {
                    return true;
                }
            }
            return false;
        }
    }
}
