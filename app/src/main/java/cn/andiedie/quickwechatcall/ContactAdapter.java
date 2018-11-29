package cn.andiedie.quickwechatcall;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.litesuits.common.utils.HexUtil;
import com.litesuits.common.utils.MD5Util;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

public class ContactAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater inflater;
    private List<String> contacts;

    public ContactAdapter(Context context, List<String> contacts) {
        this.mContext = context;
        this.inflater = LayoutInflater.from(context);
        this.contacts = contacts;
    }

    @Override
    public int getCount() {
        return contacts.size();
    }

    @Override
    public Object getItem(int position) {
        return contacts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = inflater.inflate(R.layout.contact_item, parent, false);
        String wechatName = contacts.get(position);
        String filename = HexUtil.encodeHexStr(MD5Util.md5(wechatName)) + Constants.FORMAT_EXTENSION;
        try {
            InputStream is = mContext.openFileInput(filename);
            ((ImageView) convertView.findViewById(R.id.iv_avatar)).setImageBitmap(BitmapFactory.decodeStream(is));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ((TextView) convertView.findViewById(R.id.tv_wechat_name)).setText(wechatName);
        return convertView;
    }
}
