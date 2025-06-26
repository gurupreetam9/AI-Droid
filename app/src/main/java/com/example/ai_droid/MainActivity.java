package com.example.ai_droid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences("prefs",MODE_PRIVATE);
        boolean isFirstLaunch = prefs.getBoolean("first_ui_tour",true);

        if (isFirstLaunch) {
            showUiTour();

            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("first_ui_tour",false);
            editor.apply();
        }

        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,new String[] {
                    android.Manifest.permission.POST_NOTIFICATIONS
            },1);
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView join_now = findViewById(R.id.join_button);

        join_now.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JoinNow joinNow=new JoinNow();
                Intent intent = joinNow.join_now();
                startActivity(intent);

            }
        });

        ImageView info = findViewById(R.id.info);

        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(),Info.class);
                startActivity(intent);
            }
        });

        ImageView notifications = findViewById(R.id.bell);
        notifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(),NotificationHistory.class);
                startActivity(intent);
            }
        });

        ImageView smilyButton = findViewById(R.id.simlyGif);
        Glide.with(this)
                .asGif()
                .load(R.drawable.zoro)
                .into(smilyButton);
        smilyButton.setOnClickListener(v -> {
            Intent intent = new Intent(getBaseContext(),FaceExpression.class);
            startActivity(intent);
        });
    }
    private void showUiTour() {
        ImageView imgbt[] = {findViewById(R.id.bell),findViewById(R.id.info),findViewById(R.id.simlyGif),findViewById(R.id.join_button)};
        TapTargetSequence sequence = new TapTargetSequence(this)
                .targets(
                        TapTarget.forView(imgbt[0],"Notifications","You can view previous notifications here.")
                                .cancelable(false)
                                .tintTarget(true),
                        TapTarget.forView(imgbt[1],"Info","Know more about us.")
                                .cancelable(false)
                                .tintTarget(true),
                        TapTarget.forView(imgbt[2],"Face Expression Model","This is a face expression model we used as a demo.")
                                .cancelable(false)
                                .tintTarget(false),
                        TapTarget.forView(imgbt[3],"Join Button","Click on this to join us now.")
                                .outerCircleAlpha(0.89f)
                                .cancelable(false)
                                .tintTarget(false)
                )
                .listener(new TapTargetSequence.Listener() {
                    @Override
                    public void onSequenceFinish() {
                        Toast.makeText(getApplicationContext(),"You're all set!",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {

                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {
                        Toast.makeText(getApplicationContext(),"You've skipped the UI tour!",Toast.LENGTH_SHORT).show();

                    }
                });
        sequence.start();
    }
}