package cn.andiedie.quickwechatcall;

import android.graphics.Bitmap;

class Constants {
    static final String SHARE_PREFERENCES_NAME = "QuickWeChatCall";
    static final String CONTACTS_KEY = "CONTACTS_KEY";
    static final String AUTO_ACCEPT_KEY = "AUTO_ACCEPT_KEY";
    static final Bitmap.CompressFormat UCROP_FORMAT = Bitmap.CompressFormat.JPEG;
    static final String FORMAT_EXTENSION = ".jpg";
    static final String CROPPED_AVATAR_NAME = "cropped_avatar" + FORMAT_EXTENSION;
    static final String WECHAT_NAME_INTENT_EXTRA_KEY = "WECHAT_NAME_INTENT_EXTRA_KEY";
    static final String SERVICE_START_TYPE = "SERVICE_START_TYPE";
    static final int NOT_SERVICE_START_TYPE = -1;
    static final int NEW_TASK_SERVICE_START_TYPE = 0;
    static final int AUTO_ACCEPT_CHANGE_SERVICE_START_TYPE = 1;
    static final String TARGET_INTENT_EXTRA_KEY = "TARGET_INTENT_EXTRA_KEY";
    static final String AUTO_ACCEPT_INTENT_EXTRA_KEY = "AUTO_ACCEPT_INTENT_EXTRA_KEY";
}
