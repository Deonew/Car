package com.example.deonew.car.Text;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.deonew.car.R;

import java.util.List;

/**
 * Created by theo on 17-5-19.
 */

/**
 * Created by theo on 17-5-18.
 */

public class RecvAdapter extends BaseAdapter {

    private List<ReceiveItem> data;
    private LayoutInflater inflater;

    public RecvAdapter(LayoutInflater in, List<ReceiveItem> d){
        this.inflater = in;
        this.data = d;
    }
    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemView = inflater.inflate(R.layout.text_recv_item,null);
        ReceiveItem s = data.get(position);
        TextView c = (TextView)itemView.findViewById(R.id.recvTextContent);
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

