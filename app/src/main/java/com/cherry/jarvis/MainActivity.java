package com.cherry.jarvis;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.InputType;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener {
    private static final int REQUEST_SPEECH = 101;
    private static final int REQUEST_AUDIO = 102;
    private static final String PREFS = "jarvis_settings";
    private static final String KEY_API = "openai_api_key";
    private TextView status;
    private Button talk;
    private TextToSpeech tts;
    private SharedPreferences prefs;
    private boolean requestInProgress = false;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        getWindow().getDecorView().setBackgroundColor(0xFF07111F);
        tts = new TextToSpeech(this, this);

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
        subtitle.setText("VOICE ASSISTANT  •  CLOUD AI");
        subtitle.setTextSize(12);
        subtitle.setTextColor(0xFF9EB6CC);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 8, 0, 24);

        ScrollView scroll = new ScrollView(this);
        status = new TextView(this);
        status.setText("Systems online. Add your API key in Settings, then tap to speak.");
        status.setTextSize(17);
        status.setTextColor(0xFFFFFFFF);
        status.setGravity(Gravity.CENTER);
        status.setPadding(8, 18, 8, 18);
        scroll.addView(status, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        root.addView(title);
        root.addView(subtitle);
        root.addView(scroll, scrollParams);

        talk = new Button(this);
        talk.setText("TALK TO JARVIS");
        talk.setOnClickListener(v -> listen());
        root.addView(talk, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        Button settings = new Button(this);
        settings.setText("API KEY SETTINGS");
        settings.setOnClickListener(v -> showApiKeyDialog());
        root.addView(settings, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(root);
    }

    private void showApiKeyDialog() {
        EditText keyInput = new EditText(this);
        keyInput.setSingleLine(true);
        keyInput.setHint("sk-...");
        keyInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        String existing = prefs.getString(KEY_API, "");
        if (!existing.isEmpty()) keyInput.setText(existing);
        keyInput.setSelection(keyInput.length());

        new AlertDialog.Builder(this)
                .setTitle("OpenAI API key")
                .setMessage("Enter your API key. It is saved in this app's private local storage. Never share it with anyone.")
                .setView(keyInput)
                .setPositiveButton("Save", (dialog, which) -> {
                    String key = keyInput.getText().toString().trim();
                    if (key.isEmpty()) {
                        prefs.edit().remove(KEY_API).apply();
                        say("API key removed. Add a key to use cloud intelligence.");
                    } else if (!key.startsWith("sk-")) {
                        say("That key does not look like an OpenAI API key. Please check it and try again.");
                    } else {
                        prefs.edit().putString(KEY_API, key).apply();
                        say("API key saved on this phone. Cloud intelligence is ready to test.");
                    }
                })
                .setNegativeButton("Cancel", null)
                .setNeutralButton("Remove key", (dialog, which) -> {
                    prefs.edit().remove(KEY_API).apply();
                    say("API key removed.");
                })
                .show();
    }

    private void listen() {
        if (requestInProgress) return;
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
            say("Speech recognition is unavailable on this device.");
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == REQUEST_AUDIO && results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) listen();
        else if (requestCode == REQUEST_AUDIO) say("Microphone permission is required.");
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
            say("Opening phone settings.");
            startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
        } else if (c.contains("open youtube")) {
            Intent launch = getPackageManager().getLaunchIntentForPackage("com.google.android.youtube");
            if (launch != null) { say("Opening YouTube."); startActivity(launch); }
            else say("YouTube is not installed.");
        } else if (c.contains("what time") || c.equals("time")) {
            SimpleDateFormat f = new SimpleDateFormat("HH:mm", Locale.getDefault());
            say("The time is " + f.format(new Date()));
        } else {
            askCloudAI(spoken);
        }
    }

    private void askCloudAI(String prompt) {
        String apiKey = prefs.getString(KEY_API, "");
        if (apiKey.isEmpty()) {
            say("Cloud intelligence needs your API key. Tap API Key Settings to add it.");
            return;
        }
        requestInProgress = true;
        talk.setEnabled(false);
        status.setText("Thinking… connecting to cloud intelligence.");
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL("https://api.openai.com/v1/chat/completions");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(20000);
                connection.setReadTimeout(45000);
                connection.setDoOutput(true);
                connection.setRequestProperty("Authorization", "Bearer " + apiKey);
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");

                JSONObject body = new JSONObject();
                body.put("model", "gpt-4.1-mini");
                body.put("max_tokens", 450);
                JSONArray messages = new JSONArray();
                JSONObject system = new JSONObject();
                system.put("role", "system");
                system.put("content", "You are JARVIS, a helpful, calm, concise voice assistant. Answer naturally for speech. Do not claim to control phone features unless the app actually supports them. Prefer short answers unless the user asks for detail.");
                messages.put(system);
                JSONObject user = new JSONObject();
                user.put("role", "user");
                user.put("content", prompt);
                messages.put(user);
                body.put("messages", messages);

                byte[] bytes = body.toString().getBytes(StandardCharsets.UTF_8);
                try (OutputStream out = connection.getOutputStream()) { out.write(bytes); }

                int code = connection.getResponseCode();
                InputStream stream = code >= 200 && code < 300
                        ? connection.getInputStream() : connection.getErrorStream();
                StringBuilder response = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) response.append(line);
                }
                JSONObject json = new JSONObject(response.toString());
                String answer;
                if (code >= 200 && code < 300) {
                    answer = json.getJSONArray("choices").getJSONObject(0)
                            .getJSONObject("message").getString("content").trim();
                    if (answer.isEmpty()) answer = "I received an empty answer. Please try again.";
                } else {
                    String message = json.optJSONObject("error") != null
                            ? json.getJSONObject("error").optString("message", "Request failed.")
                            : "Request failed.";
                    if (code == 401) answer = "The API key was rejected. Check your key in API Key Settings.";
                    else if (code == 429) answer = "The API limit was reached or billing is not enabled. Check your OpenAI API account.";
                    else answer = "Cloud request failed: " + message;
                }
                final String finalAnswer = answer;
                runOnUiThread(() -> finishCloudReply(finalAnswer));
            } catch (Exception e) {
                runOnUiThread(() -> finishCloudReply("I couldn't connect to cloud AI. Check your internet connection and try again."));
            } finally {
                if (connection != null) connection.disconnect();
            }
        }).start();
    }

    private void finishCloudReply(String answer) {
        requestInProgress = false;
        talk.setEnabled(true);
        say(answer);
    }

    private void say(String text) {
        if (status != null) status.setText(text);
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