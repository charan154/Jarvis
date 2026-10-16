package com.cherry.jarvis;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final int REQUEST_AUDIO = 102;
    private TextView status;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setBackgroundColor(0xFF07111F);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(28, 28, 28, 28);

        TextView title = new TextView(this);
        title.setText("J.A.R.V.I.S");
        title.setTextSize(32);
        title.setTextColor(0xFF61D8FF);
        title.setGravity(Gravity.CENTER);

        TextView subtitle = new TextView(this);
        subtitle.setText("VOICE ASSISTANT • WAKE-WORD TEST");
        subtitle.setTextSize(12);
        subtitle.setTextColor(0xFF9EB6CC);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 8, 0, 24);

        status = new TextView(this);
        status.setText("Enable listening to test “Hey Jarvis”. Keep the notification visible while listening.");
        status.setTextSize(16);
        status.setTextColor(0xFFFFFFFF);
        status.setGravity(Gravity.CENTER);
        status.setPadding(8, 18, 8, 28);

        Button start = new Button(this);
        start.setText("START HEY JARVIS");
        start.setOnClickListener(v -> startWakeListening());

        Button stop = new Button(this);
        stop.setText("STOP LISTENING");
        stop.setOnClickListener(v -> {
            stopService(new Intent(this, WakeWordService.class));
            status.setText("Wake-word listening stopped.");
        });

        Button accessibility = new Button(this);
        accessibility.setText("ACCESSIBILITY SETTINGS");
        accessibility.setOnClickListener(v -> {
            try { startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)); }
            catch (Exception e) { status.setText("Could not open Accessibility settings."); }
        });

        TextView help = new TextView(this);
        help.setText("Prototype commands: “Hey Jarvis, what time is it”, “open settings”, “open YouTube”, “open ChatGPT”.\n\nThis is an experimental speech-recognition wake loop, not a dedicated offline wake-word engine. Recognition may time out or be affected by Android battery and lock-screen restrictions.");
        help.setTextSize(13);
        help.setTextColor(0xFFB9C9D8);
        help.setPadding(0, 22, 0, 0);

        root.addView(title);
        root.addView(subtitle);
        root.addView(status, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(start, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(stop, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(accessibility, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(help);
        setContentView(root);
    }

    private void startWakeListening() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_AUDIO);
            return;
        }
        try {
            Intent service = new Intent(this, WakeWordService.class);
            if (android.os.Build.VERSION.SDK_INT >= 26) startForegroundService(service);
            else startService(service);
            status.setText("Wake-word service started. Lock the phone and say “Hey Jarvis”.");
        } catch (Exception e) {
            status.setText("Could not start listening. Reopen Jarvis and check microphone permission.");
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == REQUEST_AUDIO && results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
            startWakeListening();
        } else if (requestCode == REQUEST_AUDIO) {
            status.setText("Microphone permission is required for wake-word listening.");
        }
    }
}