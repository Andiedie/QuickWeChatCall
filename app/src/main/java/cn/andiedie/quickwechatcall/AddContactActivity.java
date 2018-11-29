package cn.andiedie.quickwechatcall;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.InputStream;

public class AddContactActivity extends AppCompatActivity {
    private static final String TAG = "AddContactActivity";
    private static final int PICK_AVATAR = 38453;
    private static final Bitmap.CompressFormat UCROP_FORMAT = Bitmap.CompressFormat.JPEG;
    private static final String CROPPED_AVATAR_NAME = "cropped_avatar.jpg";
    private ImageView avatarImageView;
    private Uri avatarUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        initUI();
    }

    private void initUI() {
        avatarImageView = findViewById(R.id.iv_avatar);
        avatarImageView.setOnClickListener(onAvatarClick);
    }

    private View.OnClickListener onAvatarClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "选择头像"), PICK_AVATAR);
        }
    };

    private void startCrop(Uri source) {
        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(UCROP_FORMAT);
        options.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        options.setActiveWidgetColor(ContextCompat.getColor(this, R.color.colorAccent));
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        UCrop.of(source, Uri.fromFile(new File(getCacheDir(), CROPPED_AVATAR_NAME)))
                .withAspectRatio(1, 1)
                .withOptions(options)
                .start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            Log.d(TAG, "ActivityResult BAD" + data);
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
}
