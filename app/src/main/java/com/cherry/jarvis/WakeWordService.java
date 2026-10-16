package com.cherry.jarvis;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
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
                .setContentText("Say “Hey Jarvis” to activate")
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
            speak("Speech recognition is not available on this phone.");
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
                    @Override public void onError(int error) { scheduleListen(900); }
                    @Override public void onResults(android.os.Bundle results) {
                        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                        if (matches != null && !matches.isEmpty()) handleRecognized(matches.get(0));
                        else scheduleListen(400);
                    }
                    @Override public void onPartialResults(android.os.Bundle partialResults) {
                        ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                        if (matches != null && !matches.isEmpty()) {
                            String phrase = matches.get(0).toLowerCase(Locale.ROOT);
                            if (!awaitingCommand && (phrase.contains("hey jarvis") || phrase.contains("hey, jarvis"))) {
                                try { recognizer.stopListening(); } catch (Exception ignored) {}
                                handleRecognized(matches.get(0));
                            }
                        }
                    }
                    @Override public void onEvent(int eventType, android.os.Bundle params) {}
                });
            }
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
            intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);
            recognizer.startListening(intent);
        } catch (Exception e) {
            scheduleListen(1400);
        }
    }

    private void handleRecognized(String phrase) {
        if (stopped || phrase == null) { scheduleListen(500); return; }
        String lower = phrase.toLowerCase(Locale.ROOT);
        if (!awaitingCommand) {
            int wake = lower.indexOf("jarvis");
            if (wake < 0) { scheduleListen(350); return; }
            String command = phrase.substring(Math.min(phrase.length(), wake + "jarvis".length())).trim();
            if (command.startsWith(",")) command = command.substring(1).trim();
            awaitingCommand = true;
            if (!command.isEmpty()) {
                executeCommand(command);
            } else {
                speak("Yes?");
            }
        } else {
            awaitingCommand = false;
            executeCommand(phrase);
        }
    }

    private void executeCommand(String phrase) {
        String c = phrase.toLowerCase(Locale.ROOT);
        awaitingCommand = false;
        if (c.contains("what time") || c.equals("time") || c.contains("current time")) {
            String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
            speak("The time is " + time);
        } else if (c.contains("open settings") || c.equals("settings")) {
            speak("Opening phone settings.");
            handler.postDelayed(() -> launch(new Intent(Settings.ACTION_SETTINGS)), 700);
        } else if (c.contains("open youtube") || c.equals("youtube")) {
            openPackage("com.google.android.youtube", "Opening YouTube.", "YouTube is not installed.");
        } else if (c.contains("open chatgpt") || c.equals("chatgpt")) {
            openPackage("com.openai.chatgpt", "Opening ChatGPT.", "ChatGPT is not installed.");
        } else if (c.contains("stop listening") || c.contains("go to sleep")) {
            speak("Okay. To resume wake listening, open Jarvis and start it again.");
            handler.postDelayed(() -> stopSelf(), 1800);
        } else {
            speak("I heard " + phrase + ". I can tell the time and open supported apps right now. More phone controls need to be added and tested.");
        }
    }

    private void openPackage(String packageName, String success, String missing) {
        Intent launch = getPackageManager().getLaunchIntentForPackage(packageName);
        if (launch == null) { speak(missing); return; }
        speak(success);
        handler.postDelayed(() -> launch(launch), 700);
    }

    private void launch(Intent intent) {
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            speak("Android blocked that action from the background. Unlock the phone and try again.");
        }
    }

    private void speak(String text) {
        speaking = true;
        if (tts != null && ttsReady) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "jarvis_say");
            handler.postDelayed(() -> {
                speaking = false;
                scheduleListen(350);
            }, Math.max(1400, text.length() * 65L));
        } else {
            speaking = false;
            scheduleListen(500);
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