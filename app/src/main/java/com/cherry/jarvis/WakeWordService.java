package com.cherry.jarvis;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.provider.Settings;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class WakeWordService extends Service implements TextToSpeech.OnInitListener {
    private static final String CHANNEL_ID = "jarvis_wake_channel";
    private static final int NOTIFICATION_ID = 41;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private SpeechRecognizer recognizer;
    private TextToSpeech tts;
    private boolean awaitingCommand = false;
    private boolean speaking = false;
    private boolean stopped = false;
    private boolean ttsReady = false;

    @Override public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        tts = new TextToSpeech(this, this);
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        stopped = false;
        startForeground(NOTIFICATION_ID, buildNotification());
        handler.postDelayed(this::ensureListening, 700);
        return START_STICKY;
    }

    private Notification buildNotification() {
        Intent open = new Intent(this, MainActivity.class);
        PendingIntent pending = PendingIntent.getActivity(this, 0, open,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0));
        Notification.Builder builder = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(this, CHANNEL_ID) : new Notification.Builder(this);
        return builder.setContentTitle("Jarvis is listening")
                .setContentText("Say “Hey Jarvis” followed by a command")
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .setContentIntent(pending)
                .setOngoing(true)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Jarvis wake listening", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Persistent notification while Jarvis listens for its wake phrase");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private void ensureListening() {
        if (stopped || speaking) return;
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            speak("Speech recognition is not available on this phone. Please enable Google's speech services.");
            return;
        }
        try {
            if (recognizer == null) {
                recognizer = SpeechRecognizer.createSpeechRecognizer(this);
                recognizer.setRecognitionListener(new RecognitionListener() {
                    @Override public void onReadyForSpeech(android.os.Bundle params) {}
                    @Override public void onBeginningOfSpeech() {}
                    @Override public void onRmsChanged(float rmsdB) {}
                    @Override public void onBufferReceived(byte[] buffer) {}
                    @Override public void onEndOfSpeech() {}
                    @Override public void onError(int error) { scheduleListen(1000); }
                    @Override public void onResults(android.os.Bundle results) {
                        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                        if (matches != null && !matches.isEmpty()) handleRecognized(matches.get(0));
                        else scheduleListen(500);
                    }
                    @Override public void onPartialResults(android.os.Bundle partialResults) { }
                    @Override public void onEvent(int eventType, android.os.Bundle params) {}
                });
            }
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
            // Allow the installed speech service to choose online or on-device recognition.
            intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false);
            recognizer.startListening(intent);
        } catch (Exception e) {
            scheduleListen(1500);
        }
    }

    private void handleRecognized(String phrase) {
        if (stopped || phrase == null) { scheduleListen(500); return; }
        String lower = phrase.toLowerCase(Locale.ROOT).trim();
        if (!awaitingCommand) {
            int wake = lower.indexOf("jarvis");
            if (wake < 0) { scheduleListen(350); return; }
            String command = phrase.substring(Math.min(phrase.length(), wake + "jarvis".length())).trim();
            while (command.startsWith(",") || command.startsWith(".")) command = command.substring(1).trim();
            awaitingCommand = true;
            if (!command.isEmpty()) executeCommand(command);
            else speak("Yes? What can I do for you?");
        } else {
            awaitingCommand = false;
            executeCommand(phrase);
        }
    }

    private void executeCommand(String phrase) {
        String c = phrase.toLowerCase(Locale.ROOT).trim();
        awaitingCommand = false;

        if (c.contains("what time") || c.equals("time") || c.contains("current time")) {
            speak("The time is " + new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()));
        } else if (c.contains("what date") || c.contains("today's date") || c.equals("date") || c.contains("what day is it")) {
            speak("Today is " + new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(new Date()));
        } else if (c.contains("battery")) {
            BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
            int level = bm == null ? -1 : bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
            speak(level >= 0 ? "Your battery is at " + level + " percent." : "I couldn't read the battery level.");
        } else if (c.contains("who are you") || c.contains("what are you")) {
            speak("I am Jarvis, your voice assistant. I can open apps, help with basic phone actions, tell you the time and date, adjust volume, and hand questions to web search. Full AI answers need an AI model or service.");
        } else if (c.contains("what can you do") || c.contains("help me") || c.equals("help")) {
            speak("Try opening WhatsApp, Maps, Camera, or Settings; ask me to search the web; change volume; open Wi-Fi settings; tell the time or date; check battery; or say go home, go back, or show notifications. Some navigation commands need Accessibility enabled.");
        } else if (c.contains("tell me a joke") || c.equals("joke")) {
            speak("Why do programmers prefer dark mode? Because light attracts bugs.");
        } else if (c.contains("increase volume") || c.contains("volume up") || c.contains("turn volume up")) {
            adjustVolume(AudioManager.ADJUST_RAISE, "Increasing volume.");
        } else if (c.contains("decrease volume") || c.contains("volume down") || c.contains("turn volume down")) {
            adjustVolume(AudioManager.ADJUST_LOWER, "Decreasing volume.");
        } else if (c.contains("mute volume") || c.equals("mute")) {
            AudioManager audio = (AudioManager) getSystemService(AUDIO_SERVICE);
            if (audio != null) audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
            speak("Media volume muted.");
        } else if (c.contains("unmute")) {
            AudioManager audio = (AudioManager) getSystemService(AUDIO_SERVICE);
            if (audio != null) audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0);
            speak("Media volume restored.");
        } else if (c.contains("go home") || c.equals("home screen") || c.equals("home")) {
            globalAction(AccessibilityService.GLOBAL_ACTION_HOME, "Going home.");
        } else if (c.contains("go back") || c.equals("back")) {
            globalAction(AccessibilityService.GLOBAL_ACTION_BACK, "Going back.");
        } else if (c.contains("recent apps") || c.contains("show recents")) {
            globalAction(AccessibilityService.GLOBAL_ACTION_RECENTS, "Showing recent apps.");
        } else if (c.contains("show notifications") || c.equals("notifications")) {
            globalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS, "Opening notifications.");
        } else if (c.contains("quick settings")) {
            globalAction(AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS, "Opening Quick Settings.");
        } else if (c.contains("open wi-fi") || c.contains("open wifi") || c.contains("wifi settings") || c.contains("internet settings")) {
            openWifiSettings();
        } else if (c.contains("open bluetooth") || c.contains("bluetooth settings")) {
            openBluetoothSettings();
        } else if (c.startsWith("call ")) {
            String number = phrase.substring(Math.min(phrase.length(), 5)).trim();
            Intent dial = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(number)));
            speak("Opening the dialer. Review the number and tap call yourself.");
            handler.postDelayed(() -> launch(dial), 700);
        } else if (c.startsWith("search for ") || c.startsWith("search ")) {
            String query = phrase.replaceFirst("(?i)^search( for)?\\s+", "").trim();
            searchWeb(query);
        } else if (c.startsWith("ask chatgpt ")) {
            String question = phrase.substring(Math.min(phrase.length(), "ask chatgpt ".length())).trim();
            speak("Opening ChatGPT with your question. You may need to sign in.");
            handler.postDelayed(() -> launch(new Intent(Intent.ACTION_VIEW, Uri.parse("https://chatgpt.com/?q=" + Uri.encode(question)))), 800);
        } else if (c.contains("open settings") || c.equals("settings")) {
            speak("Opening phone settings.");
            handler.postDelayed(() -> launch(new Intent(Settings.ACTION_SETTINGS)), 700);
        } else if (c.contains("open youtube") || c.equals("youtube")) {
            openPackage("com.google.android.youtube", "Opening YouTube.", "YouTube is not installed.");
        } else if (c.contains("open chatgpt") || c.equals("chatgpt")) {
            Intent app = getPackageManager().getLaunchIntentForPackage("com.openai.chatgpt");
            if (app != null) { speak("Opening ChatGPT."); handler.postDelayed(() -> launch(app), 700); }
            else { speak("Opening ChatGPT in your browser."); handler.postDelayed(() -> launch(new Intent(Intent.ACTION_VIEW, Uri.parse("https://chatgpt.com"))), 700); }
        } else if (c.contains("open whatsapp") || c.equals("whatsapp")) {
            openPackage("com.whatsapp", "Opening WhatsApp.", "WhatsApp is not installed.");
        } else if (c.contains("open maps") || c.contains("open google maps") || c.equals("maps")) {
            openPackage("com.google.android.apps.maps", "Opening Maps.", "Maps is not installed.");
        } else if (c.contains("open camera") || c.equals("camera")) {
            speak("Opening camera.");
            handler.postDelayed(() -> launch(new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)), 700);
        } else if (c.contains("open gallery") || c.contains("open photos")) {
            openPackage("com.google.android.apps.photos", "Opening Photos.", "Photos app is not installed.");
        } else if (c.contains("open phone") || c.equals("dialer")) {
            speak("Opening the phone dialer.");
            handler.postDelayed(() -> launch(new Intent(Intent.ACTION_DIAL)), 700);
        } else if (c.contains("open messages") || c.contains("open sms")) {
            openMessaging();
        } else if (c.contains("open chrome") || c.contains("open browser")) {
            speak("Opening browser.");
            handler.postDelayed(() -> launch(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))), 700);
        } else if (c.contains("stop listening") || c.contains("go to sleep")) {
            speak("Okay. To resume wake listening, open Jarvis and start it again.");
            handler.postDelayed(this::stopSelf, 1800);
        } else {
            // No API key required: hand broad questions to a browser search instead of pretending to know.
            searchWeb(phrase);
        }
    }

    private void adjustVolume(int direction, String response) {
        AudioManager audio = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audio != null) audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, 0);
        speak(response);
    }

    private void globalAction(int action, String response) {
        speak(response);
        handler.postDelayed(() -> {
            if (!JarvisAccessibilityService.doGlobalAction(action)) {
                speak("Please enable Jarvis phone controls in Accessibility settings first.");
            }
        }, 650);
    }

    private void openWifiSettings() {
        speak("Opening internet settings.");
        Intent intent;
        if (Build.VERSION.SDK_INT >= 29) intent = new Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY);
        else intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        handler.postDelayed(() -> launch(intent), 700);
    }

    private void openBluetoothSettings() {
        speak("Opening Bluetooth settings.");
        Intent intent = Build.VERSION.SDK_INT >= 29
                ? new Intent(Settings.Panel.ACTION_BLUETOOTH)
                : new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
        handler.postDelayed(() -> launch(intent), 700);
    }

    private void openMessaging() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_APP_MESSAGING);
        speak("Opening messages.");
        handler.postDelayed(() -> launch(intent), 700);
    }

    private void searchWeb(String query) {
        if (query == null || query.trim().isEmpty()) {
            speak("Tell me what you want to search for.");
            return;
        }
        speak("I'll search the web for " + query);
        Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + Uri.encode(query)));
        handler.postDelayed(() -> launch(browser), 800);
    }

    private void openPackage(String packageName, String success, String missing) {
        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent == null) { speak(missing); return; }
        speak(success);
        handler.postDelayed(() -> launch(intent), 700);
    }

    private void launch(Intent intent) {
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            speak("Android blocked that action. Unlock the phone and try again.");
        }
    }

    private void speak(String text) {
        speaking = true;
        if (tts != null && ttsReady) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "jarvis_say");
            handler.postDelayed(() -> {
                speaking = false;
                scheduleListen(400);
            }, Math.max(1500, text.length() * 65L));
        } else {
            speaking = false;
            scheduleListen(600);
        }
    }

    private void scheduleListen(long delay) {
        if (stopped) return;
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(this::ensureListening, delay);
    }

    @Override public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS && tts != null) {
            ttsReady = true;
            tts.setLanguage(Locale.US);
            tts.setSpeechRate(0.92f);
        }
    }

    @Override public void onDestroy() {
        stopped = true;
        handler.removeCallbacksAndMessages(null);
        if (recognizer != null) { recognizer.destroy(); recognizer = null; }
        if (tts != null) { tts.stop(); tts.shutdown(); tts = null; }
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}