package com.cherry.jarvis;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final int REQUEST_AUDIO = 102;
    private TextView status;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setBackgroundColor(0xFF07111F);
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(28, 30, 28, 30);

        TextView title = new TextView(this);
        title.setText("J.A.R.V.I.S");
        title.setTextSize(32);
        title.setTextColor(0xFF61D8FF);
        title.setGravity(Gravity.CENTER);
        TextView subtitle = new TextView(this);
        subtitle.setText("VOICE ASSISTANT • PHONE CONTROL");
        subtitle.setTextSize(12);
        subtitle.setTextColor(0xFF9EB6CC);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 8, 0, 22);

        status = new TextView(this);
        status.setText("Ready. Start listening, then say “Hey Jarvis” followed by a command.");
        status.setTextSize(16);
        status.setTextColor(0xFFFFFFFF);
        status.setGravity(Gravity.CENTER);
        status.setPadding(8, 16, 8, 24);

        addButton(root, "START HEY JARVIS", v -> startWakeListening());
        addButton(root, "STOP LISTENING", v -> {
            stopService(new Intent(this, WakeWordService.class));
            status.setText("Listening stopped.");
        });
        addButton(root, "ENABLE PHONE CONTROL", v -> {
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            status.setText("In Accessibility settings, open Installed apps / Downloaded apps, select Jarvis phone controls, and enable it.");
        });
        addButton(root, "OPEN SPEECH SETTINGS", v -> {
            try { startActivity(new Intent("com.android.settings.TTS_SETTINGS")); }
            catch (Exception e) { startActivity(new Intent(Settings.ACTION_SETTINGS)); }
        });

        TextView help = new TextView(this);
        help.setText("TRY SAYING\n• Hey Jarvis, open WhatsApp\n• Hey Jarvis, search for today's weather\n• Hey Jarvis, open Wi-Fi settings\n• Hey Jarvis, increase volume\n• Hey Jarvis, go home / go back\n• Hey Jarvis, tell me a joke\n• Hey Jarvis, what can you do?\n\nCOST & LIMITS\nJarvis uses Android speech recognition and text-to-speech where available. General ChatGPT-level answers need an AI model or service; this no-key build can hand questions to web search but cannot read ChatGPT's private replies. Always-on wake detection and lock-screen operation depend on Android/Samsung restrictions.");
        help.setTextSize(14);
        help.setTextColor(0xFFB9C9D8);
        help.setPadding(4, 22, 4, 0);

        root.addView(title);
        root.addView(subtitle);
        root.addView(status, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(help);
        scroll.addView(root);
        setContentView(scroll);
    }

    private void addButton(LinearLayout root, String text, android.view.View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setOnClickListener(listener);
        root.addView(button, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void startWakeListening() {
        if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_AUDIO);
            return;
        }
        try {
            startForegroundService(new Intent(this, WakeWordService.class));
            status.setText("Jarvis is listening. Keep its notification visible. Say “Hey Jarvis” plus your command.");
        } catch (Exception e) {
            try { startService(new Intent(this, WakeWordService.class)); }
            catch (Exception ignored) { status.setText("Could not start listening. Open Jarvis and try again."); }
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_AUDIO && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startWakeListening();
        } else if (requestCode == REQUEST_AUDIO) {
            status.setText("Microphone permission is required for voice commands.");
        }
    }
}