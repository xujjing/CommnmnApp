package com.streambus.commonmodule;

import com.streambus.basemodule.utils.PreferencesUtils;
import com.streambus.basemodule.utils.SecurePreferences;

import androidx.lifecycle.MutableLiveData;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2020/11/13
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class Constants {

    /************************************* - LiveData - ****************************************/
    //状态需求改变  //true:锁着状态，看不到Adult内容； false:锁打开状态,显示Adult内容. //但接口传值与状态值的意义相反
    public static final MutableLiveData<Boolean> SUBJECT_LOCK = new MutableLiveData<>(true);
    public static final MutableLiveData<Boolean> SUBJECT_LIVE_LOCK = new MutableLiveData<>(true); //live > true:锁着状态

    public static final MutableLiveData<Integer> SUBJECT_LOGIN_TYPE = new MutableLiveData<>(PreferencesUtils.get(Constants.KEY_LOGIN_METHOD_TYPE, 0));

    /************************************* - TAG - ****************************************/
    public static final String KEY_PARENTAL_PWD = "key_parental_pwd";
    public static final String DEFAULT_PARENTAL_PWD = "8989";
    public static final String LOG_FILE_DIRECTORY = "LogCache";
    public static final String BASE_DB_NAME = "fav_his_channel_db";
    public static final String COLUMN_TYPE_VOD = "vod";
    public static final String HOME_TYPE_RECOMMEND = "recommend";
    public static final String HOME_TYPE_NEWEST = "newest";
    public static final String HOME_TYPE_MOVIES = "movies_home";
    public static final String HOME_TYPE_TV_SERIALS = "serials_home";
    public static final String HOME_TYPE_KIDS = "anime_home";
    public static final String HOME_TYPE_SPECIAL = "special_home";
    public static final String HOME_TYPE_SHOW = "show_home";
    public static final String HOME_TYPE_SPORT = "sport";//自定义
    public static final String HOME_TYPE_NOW_PLAYING = "now_playing_home";
    public static final String HOME_TYPE_ADULT = "adult";
    public static final String LOG_AES_KEY = "streambus_iptv"; ///请求的时候加密生成，数据返回后解密需要

    public static final int REQUEST_CODE_GOOGLE = 9001;

    public static final String ACTION_BUY = "action_buy";
    public static final String ACTION_LAUNCHER_PLAY = "action_launcher_play";

    public static final String PPMGR_PERFCOS_AES_KEY = "%f%95zjCPv#bo#waVO0ioHVhaLYmSQg#";
    public static final String PPMGR_MEMBER_AES_KEY = "VjzlNOV8kgZOEui8L5LeRZAybd1dfig2";

    /************************************* - KEY - ****************************************/
    public static final String KEY_LOGIN_METHOD_TYPE = "key_login_method_type";
    public static final String KEY_LOGIN_ACCOUNT_ID = "key_login_account_id";

    public static final String KEY_LOGIN_ACCOUNT_NAME = "key_login_account_name";
    public static final String KEY_LOGIN_ACCOUNT_PASSWORD = "key_login_account_password";

    public static final String KEY_LOGIN_THIRD_PARTY = "key_login_third_party";
    public static final String KEY_LOGIN_THIRD_UID = "key_login_third_uid";
    public static final String KEY_LOGIN_THIRD_DISPLAYNAME = "key_login_third_displayname";
    public static final String KEY_LOGIN_THIRD_EMAIL = "key_login_third_email";

    public static final String KEY_LOGIN_TOKEN = "key_login_token";
    public static final String KEY_LOGIN_SERVICE_NAME = "key_login_service_name";
    public static final String KEY_LOGIN_AUTO_RENEW = "key_login_auto_renew";
    public static final String KEY_OVERAGE_VALIDITY = "key_overage_validity";
    public static final String KEY_LAST_CMS_URL = "key_last_cms_url";
    public static final String KEY_LAST_FILE_URL = "key_last_file_url";
    public static final String KEY_LAST_EPG_URL = "key_last_epg_url";
    public static final String KEY_CRASH_EXCEPTION = "key_crash_exception";
    public static final String KEY_CRASH_EXCEPTION_FILE = "key_crash_exception_file";
    public static final String KEY_HOME_TYPE = "key_home_type";
    public static final String KEY_CATEGORY_EXTRA_INDEX = "key_category_extra_index";
    public static final String KEY_CATEGORY_LIST = "key_category_list";
    public static final String KEY_CHANNEL_TYPE = "key_channel_type";
    public static final String KEY_VOD_CHANNEL = "key_vod_channel";
    public static final String KEY_VOD_DOWNLOAD_CHANNEL = "key_vod_download_channel";
    public static final String KEY_VOD_CATEGORY = "key_vod_category";
    public static final String KEY_VOD_DIRECTOR = "key_vod_director";
    public static final String KEY_RESOURCE_SORT = "key_resource_sort";
    public static final String KEY_LIVE_PLAY_CATEGORY = "key_last_live_category";
    public static final String KEY_LIVE_PLAY_CHANNEL = "key_last_live_channel";
    public static final String KEY_VOD_REVIEW = "key_vod_review_";
    public static final String KEY_HOME_CATEGORY_NAME = "key_home_category_name";
    public static final String KEY_COLUMN_IDS = "key_column_ids";

    public static final String KEY_LOG_DESC = "desc";
    public static final String KEY_LOG_IMAGE = "log_image";
    public static final String KEY_LOG_VIDEO = "log_video";
    public static final String KEY_LOG_MEDIA = "log_media";
    public static final String KEY_LOG_USER_NAME = "log_user_name";
    public static final String KEY_LOG_CONTACT = "log_contact";
    public static final String KEY_ACTION = "key_action";
    public static final String KEY_DATA = "key_data";
    public static final String KEY_UPGRADE_IGNORE_VERSION_CODE = "key_upgrade_ignore_version_code";
    /************************************* Setting ******************************************/
    public static final String KEY_SETTING_SUBTITLE_VALUE = "key_setting_subtitle_value";
    public static final String KEY_SETTING_AUDIO_VALUE = "key_setting_audio_value";

    /************************************* - VALUE - ****************************************/
    public static boolean VALUE_IS_BARRAGE = true; //是否打开弹幕
    public static String VALUE_LOGIN_ACCOUNT_ID = SecurePreferences.get(Constants.KEY_LOGIN_ACCOUNT_ID,"");
    public static String VALUE_LOGIN_ACCOUNT_NAME = PreferencesUtils.get(Constants.KEY_LOGIN_ACCOUNT_NAME,"");
    public static String VALUE_LOGIN_TOKEN = SecurePreferences.get(Constants.KEY_LOGIN_TOKEN,"");


    /*************************************LIVE***********************************/
    public static final String OUT_LOGIN="outLogin";
    public static final String LIVE_CATEGORY_KEY="live_category_id";  //海报页当前节目对应的栏目ID

    // Interceptor
    public static String KEY_EMAIL = "key_mail";  //

    public static int PAGE_SELECT=0;


    /************************************* - play Url - ****************************************/
    public static final String FILM_PLAY_PATH = "http://127.0.0.1:9908/";
    public final static String S_PAREMT = "%s?mode=1&params=e7e05c13ef4c69f&protocolVersion=%s";


    public static String generatePlayUrl(String url, int protocolVersion) {
        return String.format(S_PAREMT, url, protocolVersion);
    }
}
