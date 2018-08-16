package com.cheng.app2048;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.cheng.app2048.view.View2048;

public class Activity2048 extends AppCompatActivity {
    private View2048 view2048;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2048);

        view2048 = findViewById(R.id.view2048);
    }

    public void revoke(View view) {
        view2048.revoke();
    }
}
