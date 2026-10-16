package com.cherry.jarvis;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener {
    private static final int REQUEST_SPEECH = 101;
    private static final int REQUEST_AUDIO = 102;
    private TextView status;
    private TextToSpeech tts;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setBackgroundColor(0xFF07111F);
        tts = new TextToSpeech(this, this);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(32, 32, 32, 32);

        TextView title = new TextView(this);
        title.setText("J.A.R.V.I.S");
        title.setTextSize(32);
        title.setTextColor(0xFF61D8FF);
        title.setGravity(Gravity.CENTER);

        status = new TextView(this);
        status.setText("Systems online. Tap to speak.");
        status.setTextSize(18);
        status.setTextColor(0xFFFFFFFF);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, 32, 0, 32);

        Button talk = new Button(this);
        talk.setText("TALK TO JARVIS");
        talk.setOnClickListener(v -> listen());

        root.addView(title);
        root.addView(status);
        root.addView(talk);
        setContentView(root);
    }

    private void listen() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_AUDIO);
            return;
        }
        try {
            Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            i.putExtra(RecognizerIntent.EXTRA_PROMPT, "I'm listening");
            startActivityForResult(i, REQUEST_SPEECH);
        } catch (Exception e) {
            status.setText("Speech recognition is unavailable on this device.");
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == REQUEST_AUDIO && results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) listen();
        else if (requestCode == REQUEST_AUDIO) status.setText("Microphone permission is required.");
    }

    @Override @SuppressWarnings("deprecation")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_SPEECH || resultCode != RESULT_OK || data == null) return;
        ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        if (matches == null || matches.isEmpty()) return;
        handle(matches.get(0));
    }

    private void handle(String spoken) {
        String c = spoken.toLowerCase(Locale.ROOT);
        if (c.contains("open settings")) {
            say("Opening settings.");
            startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
        } else if (c.contains("open youtube")) {
            Intent launch = getPackageManager().getLaunchIntentForPackage("com.google.android.youtube");
            if (launch != null) { say("Opening YouTube."); startActivity(launch); }
            else say("YouTube is not installed.");
        } else if (c.contains("hello") || c.contains("hey jarvis")) {
            say("Hello. Jarvis is online.");
        } else if (c.contains("time")) {
            java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("HH:mm", Locale.getDefault());
            say("The time is " + f.format(new java.util.Date()));
        } else {
            say("I heard: " + spoken + ". Cloud intelligence is not connected in this starter build.");
        }
    }

    private void say(String text) {
        status.setText(text);
        if (tts != null) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "jarvis_reply");
    }

    @Override public void onInit(int result) {
        if (result == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.US);
            tts.setSpeechRate(0.92f);
        }
    }

    @Override protected void onDestroy() {
        if (tts != null) { tts.stop(); tts.shutdown(); }
        super.onDestroy();
    }
}
