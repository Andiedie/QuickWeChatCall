package cn.andiedie.quickwechatcall;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.litesuits.common.data.DataKeeper;
import com.litesuits.common.io.IOUtils;
import com.litesuits.common.utils.HexUtil;
import com.litesuits.common.utils.MD5Util;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AddContactActivity extends AppCompatActivity {
    private static final String TAG = "AddContactActivity";
    private static final int PICK_AVATAR = 38453;

    private ImageView avatarImageView;
    private EditText wechatNameEditText;
    private Uri avatarUri = null;
    private List<String> contacts = null;
    private DataKeeper dk = null;
    private View.OnClickListener onAvatarClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "选择头像"), PICK_AVATAR);
        }
    };
    private View.OnClickListener onAddContactButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String wechatName = wechatNameEditText.getText().toString();
            if (TextUtils.isEmpty(wechatName)) {
                wechatNameEditText.setError("请填入微信昵称");
                return;
            }
            if (contacts == null) {
                initContacts();
            }
            if (contacts.indexOf(wechatName) != -1) {
                wechatNameEditText.setError("该昵称已存在");
                return;
            }
            if (avatarUri != null) {
                String filename = HexUtil.encodeHexStr(MD5Util.md5(wechatName)) + Constants.FORMAT_EXTENSION;
                try {
                    IOUtils.copy(new FileInputStream(avatarUri.getPath()), openFileOutput(filename, Context.MODE_PRIVATE));
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(AddContactActivity.this, "保存头像失败", Toast.LENGTH_SHORT).show();
                }
            }

            contacts.add(wechatName);
            dk.put(Constants.CONTACTS_KEY, contacts);
            Intent intent = new Intent();
            intent.putExtra(Constants.WECHAT_NAME_INTENT_EXTRA_KEY, wechatName);
            setResult(RESULT_OK, intent);
            AddContactActivity.this.finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        initUI();
    }

    private void initUI() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        dk = new DataKeeper(AddContactActivity.this, Constants.SHARE_PREFERENCES_NAME);
        avatarImageView = findViewById(R.id.iv_avatar);
        wechatNameEditText = findViewById(R.id.et_wechat_name);
        avatarImageView.setOnClickListener(onAvatarClick);
        findViewById(R.id.btn_add_contact).setOnClickListener(onAddContactButtonClick);
    }

    private void initContacts() {
        Object object = dk.get(Constants.CONTACTS_KEY);
        if (object == null) {
            object = new ArrayList<String>();
        }
        @SuppressWarnings("unchecked")
        List<String> contacts = (List<String>) object;
        this.contacts = contacts;
    }

    private void startCrop(Uri source) {
        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Constants.UCROP_FORMAT);
        options.setToolbarColor(getColor(R.color.colorPrimary));
        options.setActiveWidgetColor(getColor(R.color.colorAccent));
        options.setStatusBarColor(getColor(R.color.colorPrimary));
        UCrop.of(source, Uri.fromFile(new File(getCacheDir(), Constants.CROPPED_AVATAR_NAME)))
                .withAspectRatio(1, 1)
                .withOptions(options)
                .start(this);
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
        switch (requestCode) {
            case PICK_AVATAR:
                startCrop(data.getData());
                break;
            case UCrop.REQUEST_CROP:
                avatarUri = UCrop.getOutput(data);
                avatarImageView.setImageURI(avatarUri);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
