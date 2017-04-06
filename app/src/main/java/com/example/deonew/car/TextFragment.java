package com.example.deonew.car;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

/**
 * Created by deonew on 17-4-4.
 */

public class TextFragment extends Fragment{
    public static TextFragment newInstance(int page){
        TextFragment textFragment = new TextFragment();
        return textFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_text,container,false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //set click event
        final FloatingActionButton floatingActionButton = (FloatingActionButton)getActivity().findViewById(R.id.TextFragmentPen);
        floatingActionButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
//                floatingActionButton.
                penClick();
            }
        });
    }
    public void penClick(){
        //submit
        isSendText = true;
    }
    private EditText toSendEditText = null;
    private boolean isSendText = false;
    class sendTextRun implements Runnable{
        @Override
        public void run() {
            while (true){
                if (isSendText){
                    //send

                    //get content
                    toSendEditText = (EditText) getActivity().findViewById(R.id.sendTextInput);
                    String s = toSendEditText.getText().toString();

                    //socket send



                    //
                    isSendText = true;
                }
            }
        }
    }
}
