package com.example.ai_droid;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NotificationHistory extends AppCompatActivity {


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notifications_activity);
        RecyclerView recyclerView = findViewById(R.id.notifications_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<String> notifications = MyFirebaseMessagingService.messageList.isEmpty() ? new ArrayList<>(List.of("No Notifications")) : MyFirebaseMessagingService.messageList;
        NotificationAdapter adapter = new NotificationAdapter(notifications);
        recyclerView.setAdapter(adapter);


    }

}
