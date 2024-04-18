package com.darcy.voicerecognizer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class DialogManager implements TextToSpeech.OnInitListener {
    private static final String TAG = "DialogManager";

    private TextToSpeech tts;
    private SpeechRecognizer speechRecognizer;
    private final Context context;
    private final List<String> prompts;
    private final List<Consumer<String>> responseHandlers;
    private int currentPromptIndex = 0;

    public DialogManager(Context context, List<String> prompts, List<Consumer<String>> responseHandlers) {
        this.context = context;
        this.prompts = prompts;
        this.responseHandlers = responseHandlers;
    }

    private void initializeTTS() {
        Log.d(TAG, "initializeTTS");
        tts = new TextToSpeech(context, this);
    }

    private void initializeSpeechRecognizer() {
        Log.d(TAG, "initializeSpeechRecognizer");
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {
            }

            @Override
            public void onBeginningOfSpeech() {
            }

            @Override
            public void onRmsChanged(float v) {
            }

            @Override
            public void onBufferReceived(byte[] bytes) {
            }

            @Override
            public void onEndOfSpeech() {
            }

            @Override
            public void onError(int i) {
            }

            @Override
            public void onResults(Bundle results) {
                String result = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).get(0);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    responseHandlers.get(currentPromptIndex).accept(result);
                }
                currentPromptIndex++;
                if (currentPromptIndex < prompts.size()) {
                    speak(prompts.get(currentPromptIndex));
                }
            }

            @Override
            public void onPartialResults(Bundle bundle) {
            }

            @Override
            public void onEvent(int i, Bundle bundle) {
            }
        });
    }

    public void start() {
        initializeTTS();
        initializeSpeechRecognizer();
    }

    private void speak(String text) {
        Log.d(TAG, "Attempting to speak: " + text);
        if (tts != null && text != null) {
            HashMap<String, String> params = new HashMap<>();
            params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "DialogID");
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, params);
        }
    }

    private void startListening() {
        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(() -> Toast.makeText(context, "음성 인식을 시작합니다.", Toast.LENGTH_SHORT).show());
        }

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.KOREAN);
        speechRecognizer.startListening(intent);
    }

    @Override
    public void onInit(int status) {
        Log.d(TAG, "onInit");

        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.KOREAN);
            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                }

                @Override
                public void onDone(String utteranceId) {
                    if (currentPromptIndex < prompts.size()) {
                        if (context instanceof Activity) {
                            ((Activity) context).runOnUiThread(()->startListening());
                        }
                    }
                }

                @Override
                public void onError(String utteranceId) {
                }
            });

            if (!prompts.isEmpty()) {
                speak(prompts.get(currentPromptIndex));
            }
        } else {
            Log.e(TAG, "Initialization Failed!");
        }
    }

    public void cleanUp() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
}
