package com.base_maker.java.basemaker;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    TextView mEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mEditText = (TextView) findViewById(R.id.main_edit_text);
        mEditText.setText(TBL.Profile.CREATE);
    }
}
