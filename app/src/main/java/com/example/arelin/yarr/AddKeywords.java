package com.example.arelin.yarr;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by yasmeen on 5/7/16.
 */
public class AddKeywords extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.keywords);

        String[] keywords = {"Hello", "Goodbye"};

        // Build Adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,               // Context for the activity
                R.layout.keywords,  // Layout to use (create)
                keywords);          // Items to be displayed

        //Configure the List View
        ListView list = (ListView) findViewById(R.id.listView);
        list.setAdapter(adapter);
    }
    private void registerClickCallback() {
        ListView list = (ListView) findViewById(R.id.listView);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {
                TextView textView = (TextView) viewClicked;
                String message = "You clicked # " + position
                                + ", which is string: " + textView.getText().toString();
                Toast.makeText(AddKeywords.this, message, Toast.LENGTH_LONG).show();

            }
        });
    }
}
