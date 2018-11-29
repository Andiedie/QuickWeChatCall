package cn.andiedie.quickwechatcall;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.litesuits.common.data.DataKeeper;
import com.litesuits.common.utils.HexUtil;
import com.litesuits.common.utils.MD5Util;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int BACK_FROM_ADD_CONTACT_ACTIVITY = 10086;
    private List<String> contacts;
    private ContactAdapter adapter;
    private DataKeeper dataKeeper;
    private boolean autoAccept;
    private AdapterView.OnItemClickListener onItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            launchTask(contacts.get(position));
        }
    };
    private AdapterView.OnItemLongClickListener onLongClick = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle("删除");
            alertDialog.setMessage("确定要删除快捷联系人\"" + contacts.get(position) + "\"吗？");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "确定",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String wechatName = contacts.get(position);
                            contacts.remove(position);
                            String filename = HexUtil.encodeHexStr(MD5Util.md5(wechatName)) + Constants.FORMAT_EXTENSION;
                            deleteFile(filename);
                            dataKeeper.put(Constants.CONTACTS_KEY, contacts);
                            adapter.notifyDataSetChanged();
                            dialog.dismiss();
                        }
                    });
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "取消",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkAccessibility();
        initData();
        initUI();
    }

    private void initUI() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        GridView contactsList = findViewById(R.id.contacts_list);
        adapter = new ContactAdapter(this, contacts);
        contactsList.setAdapter(adapter);
        contactsList.setOnItemClickListener(onItemClick);
        contactsList.setOnItemLongClickListener(onLongClick);
    }

    private void initData() {
        dataKeeper = new DataKeeper(this, Constants.SHARE_PREFERENCES_NAME);
        autoAccept = dataKeeper.get(Constants.AUTO_ACCEPT_KEY, false);
        Object object = dataKeeper.get(Constants.CONTACTS_KEY);
        if (object == null) {
            object = new ArrayList<String>();
        }
        @SuppressWarnings("unchecked")
        List<String> contacts = (List<String>) object;
        this.contacts = contacts;
    }

    private void checkAccessibility() {
        if (!isAccessibilitySettingsOn(getApplicationContext())) {
            Toast.makeText(this, "请先打开无障碍服务", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        }
    }

    public void launchTask(String target) {
        Intent intent = new Intent(this, AccessibilityService.class);
        intent.putExtra(Constants.SERVICE_START_TYPE, Constants.NEW_TASK_SERVICE_START_TYPE);
        intent.putExtra(Constants.TARGET_INTENT_EXTRA_KEY, target);
        startService(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        MenuItem item = menu.findItem(R.id.auto_accept);
        item.setIcon(autoAccept ? R.drawable.ic_auto_accept : R.drawable.ic_no_auto_accept);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_add:
                intent = new Intent(this, AddContactActivity.class);
                startActivityForResult(intent, BACK_FROM_ADD_CONTACT_ACTIVITY);
                break;
            case R.id.auto_accept:
                if (autoAccept) {
                    item.setIcon(R.drawable.ic_no_auto_accept);
                    Toast.makeText(this, "不再自动接听视频聊天", Toast.LENGTH_SHORT).show();
                } else {
                    item.setIcon(R.drawable.ic_auto_accept);
                    Toast.makeText(this, "自动接听视频聊天", Toast.LENGTH_SHORT).show();
                }
                autoAccept = !autoAccept;
                dataKeeper.put(Constants.AUTO_ACCEPT_KEY, autoAccept);
                intent = new Intent(this, AccessibilityService.class);
                intent.putExtra(Constants.SERVICE_START_TYPE, Constants.AUTO_ACCEPT_CHANGE_SERVICE_START_TYPE);
                intent.putExtra(Constants.AUTO_ACCEPT_INTENT_EXTRA_KEY, autoAccept);
                startService(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            Log.d(TAG, "requestCode: " + requestCode);
            Log.d(TAG, "resultCode: " + resultCode);
            return;
        }
        if (data == null) {
            Log.d(TAG, "Data is null");
            return;
        }
        if (requestCode == BACK_FROM_ADD_CONTACT_ACTIVITY) {
            contacts.add(data.getStringExtra(Constants.WECHAT_NAME_INTENT_EXTRA_KEY));
            adapter.notifyDataSetChanged();
        }
    }

    private boolean isAccessibilitySettingsOn(Context mContext) {
        int accessibilityEnabled = 0;
        final String service = getPackageName() + "/" + AccessibilityService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "Error finding setting, default accessibility to not found: " + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(
                    mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        Log.v(TAG, "***ACCESSIBILITY IS ENABLED*** -----------------");
                        return true;
                    }
                }
            }
        }
        Log.v(TAG, "***ACCESSIBILITY IS DISABLED***");
        return false;
    }
}
