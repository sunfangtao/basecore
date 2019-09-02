package com.wxt.library.http.parse;

import com.wxt.library.contanst.Constant;
import com.wxt.library.http.util.HttpPrintUtil;
import com.wxt.library.retention.NotProguard;
import com.wxt.library.util.Util;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by Administrator on 2018/2/10.
 */

@NotProguard
public class HttpParseHelper {

    protected final String PARSE_ERR_MESSAGE = "数据解析异常";
    protected final String FORMAT_ERR_MESSAGE = "数据格式错误";
    protected final String PARSE_ERR_FAIL_RESULT = "数据不可用";
    protected final String PARSE_ERR_NO_RESULT = "没有result";
    protected final String PARSE_ERR_NO_DATA = "没有data";
    protected final String PARSE_ERR_NO_TYPE = "没有type";

    @NotProguard
    public ReturnObject parse(final String type, final Call call, final Response response) {
        // 对返回的数据进行统一的处理，比如数据是否可用，是否有效等等，如果可用交由子类处理
        ReturnObject returnObject = new ReturnObject();
        returnObject.httpType = type;
        returnObject.isSuccess = false;
        returnObject.stateCode = -1;
        returnObject.url = call.request().url() + "";
        JSONObject jsonObject = null;

        try {
            String result = response.body().string();

            if (HttpPrintUtil.isShowHttpLog(type))
                Util.print("返回数据 type=" + type + " code = " + response.code() + " url=" + call.request().url() + " json=" + result);

            if (!response.isSuccessful()) {
                returnObject.stateCode = response.code();
                switch (returnObject.stateCode) {
                    case 500:
                        returnObject.resultType = Constant.ReturnType.SERVER_EXCEPTION;
                        break;
                    case 404:
                        returnObject.resultType = Constant.ReturnType.NOT_FOUND;
                        break;
                    default:
//                        http://221.0.91.34:5180/autoLoan/i
//                        http://10.100.15.106:8888/autoLoan/i 192.168.17.109 autoloan2
                        returnObject.resultType = Constant.ReturnType.UNKNOW;
                }
                return returnObject;
            }
            jsonObject = new JSONObject(result);
        } catch (Exception e) {
            returnObject.failReason = FORMAT_ERR_MESSAGE;
            returnObject.resultType = Constant.ReturnType.FORMAT_ERROR;
            return returnObject;
        }

        try {
            if (jsonObject.has("result")) {
                if (jsonObject.has("type")) {
                    returnObject.resultType = jsonObject.getString("type");
                    if (jsonObject.getString("result").equalsIgnoreCase("success")) {
                        // 获取成功
                        if (jsonObject.has("data")) {
                            try {
                                JSONObject object = jsonObject.getJSONObject("data");
                                returnObject.json = object.toString();
                                if (object.has("info")) {
                                    returnObject.jsonObject = object.getJSONObject("info");
                                } else if (object.has("array") || object.has("list")) {
                                    int page = 0;
                                    if (object.has("page")) {
                                        page = object.getInt("page");
                                    } else if (object.has("pageNo")) {
                                        page = object.getInt("pageNo");
                                    }
                                    int pageSize = 0;
                                    if (object.has("pageSize")) {
                                        pageSize = object.getInt("pageSize");
                                    }
                                    int count = 0;
                                    if (object.has("count")) {
                                        count = object.getInt("count");
                                    }
                                    String key = "array";
                                    if (object.has("list")) {
                                        key = "list";
                                    }
                                    returnObject.jsonArray = object.getJSONArray(key);
                                    returnObject.page = page;
                                    returnObject.pageSize = pageSize;
                                    returnObject.count = count;
                                } else {
                                    // TODO 不是json或者json array
                                    returnObject.jsonObject = object;
                                }
                                returnObject.stateCode = 200;
                                returnObject.failReason = "";
                                returnObject.isSuccess = true;
                            } catch (Exception e) {
                                e.printStackTrace();
                                // 格式错误
                                returnObject.failReason = PARSE_ERR_MESSAGE;
                            }
                        } else {
                            // 格式错误
                            returnObject.failReason = PARSE_ERR_NO_DATA;
                        }
                    } else {
                        // 获取失败
                        returnObject.failReason = PARSE_ERR_FAIL_RESULT;
                        if (jsonObject.has("message")) {
                            returnObject.failReason = jsonObject.getString("message");
                        }
                    }
                } else {
                    // 格式错误
                    returnObject.failReason = PARSE_ERR_NO_TYPE;
                    returnObject.resultType = Constant.ReturnType.FORMAT_ERROR;
                }
            } else {
                // 格式错误
                returnObject.failReason = PARSE_ERR_NO_RESULT;
                returnObject.resultType = Constant.ReturnType.FORMAT_ERROR;
            }
        } catch (Exception e) {
            // 格式错误
            returnObject.failReason = PARSE_ERR_MESSAGE;
            returnObject.resultType = Constant.ReturnType.FORMAT_ERROR;
        }

        return returnObject;
    }

    @NotProguard
    public static class ReturnObject {
        // 返回结果中的data值
        public String json;
        // 返回结果的jsonObject，与jsonArray只存在一个
        public JSONObject jsonObject;
        // 返回结果的jsonArray，与jsonObject只存在一个
        public JSONArray jsonArray;
        // 分页时的页数
        public int page;
        // 分页时每页的大小
        public int pageSize;
        // 分页时数据的总数目
        public int count;
        // http 返回结果是否可用
        public boolean isSuccess;
        // 失败的提示文字
        public String failReason = "请稍后再试！";
        // http标识
        public String httpType;
        // 服务器返回的状态（需要重新登录、服务器异常、没有数据等等）或者网络问题直接返回结果，不用于提示
        public String resultType;
        // http 状态码
        public int stateCode;
        // http url
        public String url;
        // 返回数据需要解析时，解析后的对象（Object可以是List）
        public Object parseObj;
    }

}
