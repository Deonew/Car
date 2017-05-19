package com.example.deonew.car.Text;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.deonew.car.R;

import java.util.List;

/**
 * Created by theo on 17-5-18.
 */

public class SendHistoryAdapter extends BaseAdapter {

    public SendHistoryAdapter(LayoutInflater in,List<SendTextItem> d){
        this.inflater = in;
        this.data = d;
    }


    private List<SendTextItem> data;
    private LayoutInflater inflater;

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemView = inflater.inflate(R.layout.text_send_item,null);
        SendTextItem s = data.get(position);
        TextView c = (TextView)itemView.findViewById(R.id.sendTextContent);
        c.setText(s.getContent());
        return itemView;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }
}
