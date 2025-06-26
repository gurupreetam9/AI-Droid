package com.example.ai_droid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class Info extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_activity);
        EdgeToEdge.enable(this);

        TextView join_now = findViewById(R.id.join_now);

        join_now.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JoinNow joinNow = new JoinNow();
                Intent intent = joinNow.join_now();
                startActivity(intent);
            }
        });
    }
}
