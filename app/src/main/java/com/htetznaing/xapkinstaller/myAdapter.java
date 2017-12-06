package com.htetznaing.xapkinstaller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by HtetzNaing on 12/5/2017.
 */

public class myAdapter extends BaseAdapter {
    Context context;
    ArrayList<String> arrayList;
    ArrayList<String> fileSize;
    public myAdapter(Context context, ArrayList<String> arrayList,ArrayList<String> fileSize) {
        this.context = context;
        this.arrayList = arrayList;
        this.fileSize = fileSize;
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.list,null);
        TextView textView = view.findViewById(R.id.tvTitle);
        textView.setText(arrayList.get(i));

        TextView t = view.findViewById(R.id.tvSize);
        t.setText(fileSize.get(i));
        return view;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }
}
