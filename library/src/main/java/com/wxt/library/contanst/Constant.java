package com.wxt.library.contanst;

/**
 * Created by Administrator on 2017/4/10.
 */

public final class Constant {

    public static String IS_SHOW_GUIDE = "1";

    public static class SharedPreferenceFileName {
        public static final String CRASH_PARAMS_FILE = "crash_Params_File";
        public static final String SHARE_FILE_NAME_FOR_SAVEINSTANCE = "sharefilenameforsaveinstance";
        public static final String SHARE_FILE_NAME_FOR_SAVEINSTANCE_NULLRECYCLERVIEW = "sharefilenameforsaveinstance_nullrecyclerview";
    }

    public static class SharePreferenceKey {
        public static final String IS_HANDLE_CRASH = "_isHandleCrash";
        public static final String IS_EXIT = "_isExit";
        public static final String IS_EXIT_BY_CRASH = "_isExitByCrash";
        public static final String IS_CONFIRM_DIALOG = "_isConfirmDialog";
        public static final String APP_SESSION = "_appSession";

        public static final String FIRST_RUN_APP = "firstRunApp";

        public static final String IS_EXIT_BY_AUTH = "_isExitByAuth";
        public static final String IS_DIALOG_DISMISS = "_isDialogDismiss";

        public static final String LOGIN_USER_INFO = "_loginUserInfo";
    }

    public static class MetaKey {
        public static final String APP_ID = "appId";
        public static final String URL = "URL";
        public static final String CHECK_URL = "CHECK_URL";
        public static final String UPLOAD_URL = "UPLOAD_URL";
    }

    public static class HttpPrivateKey {
        public static final String APP_CHECK = "HttpPrivateKey_appCheck";
        public static final String AUTO_LOGIN = "HttpPrivateKey_autoLogin";
        public static final String AUTO_UPDATE = "HttpPrivateKey_autoUpdate";
        public static final String AUTO_UPLOAD = "HttpPrivateKey_autoUpload";
    }

    public static class HttpPrivateParam {
        public static final String HTTP_PARAMS = "httpParams";
    }

    public static class CrashKey {
        public static final String APP_ID = "appId";
        public static final String INNER_APP_STYLE = "innerAppStyle";
        public static final String INNER_APP_NAME = "innerAppName";
        public static final String INNER_APP_ID = "innerAppId";
        public static final String INNER_APP_VERSION = "innerAppVersion";
        public static final String INNER_APP_NET = "innerAppNet";
        public static final String INNER_APP_HADRWARE = "innerAppHardware";
        public static final String INNER_APP_HARDWARE_VERSION = "innerAppHardwareVersion";
        public static final String INNER_BUG_STYLE = "innerBugStyle";
        public static final String INNER_APP_TIME = "innerAppTime";
    }

    public static class IntentKey {
        public static final String INDEX_ACTIVITY = "indexActivity";
        public static final String LOGIN_ACTIVITY = "loginActivity";
        public static final String LOGIN_CALLBACK = "loginCallBack";
        public static final String LOGIN_IS_NEED = "loginIsNeed";
    }

    public static class UrlKey {
        public static final String LOGIN_URL = "/loginController/login";
        public static final String UPDATE_URL = "/version/getNewVersion";
    }

    public static class ReturnType {
        public static final String SERVER_EXCEPTION = "serverException";
        public static final String NOT_FOUND = "notFound";
        public static final String UNKNOW = "unknow";
        public static final String NO_NETWORK = "noNetWork";
        public static final String CANCLE = "cancle";
        public static final String CONNECT_FAIL = "connectFail";
        public static final String UNKNOWN_HOST_EXCEPTION = "UnknownHostException";
        public static final String FORMAT_ERROR = "formatError";
    }

}
