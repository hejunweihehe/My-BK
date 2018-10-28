package com.example.test.testdemo;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.fanbianyi.mybufferknife.ButterKnife;
import com.fanbianyi.mybutterknife.BindView;


public class MainActivity extends AppCompatActivity {
    @BindView(R.id.btn_a)
    Button btn_a;
    @BindView(R.id.txt1)
    TextView txt1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        txt1.setText("hello world");
    }
}
