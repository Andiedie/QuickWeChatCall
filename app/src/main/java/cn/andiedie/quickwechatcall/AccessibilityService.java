package cn.andiedie.quickwechatcall;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

public class AccessibilityService extends android.accessibilityservice.AccessibilityService {
    private static final String TAG = "AccessibilityService";
    private static final String INFORMATION_DESCRIPTION = "聊天信息";
    private static final String CALL_TEXT = "视频通话";
    private static final String RECEIVE_DESCRIPTION = "接听";
    private Step currentStep = Step.WAITING;
    private String target = null;

    private static final int WAIT = 100;
    private boolean finished = true;
    private Handler handler = null;
    private AccessibilityEvent input = null;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            _onAccessibilityEvent(input);
            finished = true;
        }
    };

    private enum Step {
        WAITING,
        CLICK_TARGET,
        CLICK_INFORMATION,
        CLICK_AVATAR,
        CLICK_CALL,
        CLICK_VIDEO_CALL;

        private Step next() {
            return values()[(this.ordinal() + 1) % values().length];
        }
    }

    private enum Property {
        TEXT,
        CLASS_NAME,
        DESCRIPTION
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service Created");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service Destroyed");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "Service Connected");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            target = intent.getStringExtra("target");
            if (target != null) {
                currentStep = Step.CLICK_TARGET;
                Log.d(TAG, "new task: " + target);
                launchWeChat();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "Service Interrupted");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (finished) {
            finished = false;
        } else {
            Log.v(TAG, "bounce");
            handler.removeCallbacks(runnable);
        }
        input = event;
        handler = new Handler();
        handler.postDelayed(runnable, WAIT);
    }

    private void _onAccessibilityEvent(AccessibilityEvent event) {
        Log.v(TAG, "onAccessibilityEvent" + event.toString());
        switch (currentStep) {
            case WAITING:
                if (step(Property.DESCRIPTION, RECEIVE_DESCRIPTION)) {
                    currentStep = Step.WAITING;
                    Toast.makeText(this, "自动接听视频/语音聊天", Toast.LENGTH_LONG).show();
                }
                break;
            case CLICK_TARGET:
                step(Property.TEXT, target);
                break;
            case CLICK_INFORMATION:
                step(Property.DESCRIPTION, INFORMATION_DESCRIPTION);
                break;
            case CLICK_AVATAR:
                step(Property.TEXT, target);
                break;
            case CLICK_CALL:
                step(Property.TEXT, CALL_TEXT);
                break;
            case CLICK_VIDEO_CALL:
                if (step(Property.TEXT, CALL_TEXT)) {
                    Log.d(TAG, "finish, now: " + currentStep);
                    Toast.makeText(this, "成功发起与" + target + "的视频聊天", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private boolean step(Property type, String text) {
        AccessibilityNodeInfo node = findNode(getRootInActiveWindow(), type, text);
        if (node != null) {
            clickNode(node);
            Log.d(TAG, "" + currentStep + " done");
            currentStep = currentStep.next();
            Log.d(TAG, "next: " + currentStep + "");
            return true;
        }
        return false;
    }

    private AccessibilityNodeInfo findNode(AccessibilityNodeInfo root, Property type, String text) {
        if (root == null) return null;
        boolean satisfied = false;
        switch (type) {
            case TEXT:
                satisfied = root.getText() != null && text.contentEquals(root.getText());
                break;
            case CLASS_NAME:
                satisfied = root.getClassName() != null && text.contentEquals(root.getClassName());
                break;
            case DESCRIPTION:
                satisfied = root.getContentDescription() != null && text.contentEquals(root.getContentDescription());
                break;
        }
        if (satisfied) {
            return root;
        } else {
            for (int i = 0; i < root.getChildCount(); i++) {
                AccessibilityNodeInfo result = findNode(root.getChild(i), type, text);
                if (result != null) {
                    return result;
                }
            }
        }
        root.recycle();
        return null;
    }

    private void clickNode(AccessibilityNodeInfo node) {
        if (node.isClickable()) {
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            node.recycle();
        } else {
            AccessibilityNodeInfo parent = node.getParent();
            node.recycle();
            clickNode(parent);
        }
    }

    private void launchWeChat() {
        Intent intent = new Intent();
        ComponentName cmp = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI");
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setComponent(cmp);
        startActivity(intent);
    }
}
