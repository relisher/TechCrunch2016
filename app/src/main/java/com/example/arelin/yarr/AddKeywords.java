package com.example.arelin.yarr;

import java.util.ArrayList;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;

/**
 * Created by yasmeen on 5/7/16.
 */
public class AddKeywords extends ListActivity {


        /** Items entered by the user is stored in this ArrayList variable */
        public static ArrayList<String> list = new ArrayList<String>();

        /** Declaring an ArrayAdapter to set items to ListView */
        ArrayAdapter<String> adapter;

        /** Called when the activity is first created. */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            /** Setting a custom layout for the list activity */
            setContentView(R.layout.keywords);

            /** Reference to the button of the layout main.xml */
            Button btn = (Button) findViewById(R.id.btnAdd);

            /** Defining the ArrayAdapter to set items to ListView */
            adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);

            /** Defining a click event listener for the button "Add" */
            OnClickListener listener = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditText edit = (EditText) findViewById(R.id.txtItem);
                    list.add(edit.getText().toString());
                    edit.setText("");
                    adapter.notifyDataSetChanged();

                }
            };

            /** Setting the event listener for the add button */
            btn.setOnClickListener(listener);

            /** Setting the adapter to the ListView */
            setListAdapter(adapter);
        }

}
