package com.example.arelin.yarr;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import java.io.*;
import com.ibm.watson.developer_cloud.http.HttpMediaType;
import com.ibm.watson.developer_cloud.speech_to_text.v1.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.BaseRecognizeCallback;


public class MainActivity extends Activity {

    private static final int RECORDER_SAMPLERATE = 8000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BufferElements2Rec = 1024;
    private String filePath = "";

    private AudioRecord inputStream = null;
    private boolean isRecording;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button clickButton = (Button) findViewById(R.id.Listening);
        clickButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ambientListeningLoop();
            }
        });
        Button clickButton2 = (Button) findViewById(R.id.Keyword);
        clickButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(this, whatever.class);
                //startActivity(intent);
            }
        });
    }

    protected void ambientListeningLoop() {

        SpeechToText service = new SpeechToText();
        int BytesPerElement = 2; // 2 bytes in 16bit format
        service.setUsernameAndPassword("<username>", "<password>");


        while (true) {
            inputStream = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    RECORDER_SAMPLERATE, RECORDER_CHANNELS,
                    RECORDER_AUDIO_ENCODING, BufferElements2Rec * BytesPerElement);
            inputStream.startRecording();
            isRecording = true;
            final int THRESHOLD = 300;
            int volume = inputStream.getSampleRate();
            boolean isSpeaking = (volume > THRESHOLD);
            if (isSpeaking) {
                try {
                    System.out.println("RECORDING...");

                    do {
                        Thread.sleep(2000);//Updates every second
                    }
                    while (inputStream.getSampleRate() > THRESHOLD);
                    RecognizeOptions.Builder builder = new RecognizeOptions.Builder();
                    writeAudioDataToFile();
                    builder.continuous(true).interimResults(true).contentType(HttpMediaType.AUDIO_RAW);
                    service.recognizeUsingWebSocket(openFileInput(filePath), builder.build(), new BaseRecognizeCallback() {
                                @Override
                                public void onTranscription(SpeechResults speechResults) {
                                    System.out.println(speechResults);
                                }
                            }
                    );

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    System.out.println("Error Occured");
                } finally {
                   stopRecording();//Makes sure microphone closes on exit.
                }
            }
        }
    }

    private void stopRecording() {
        // stops the recording activity
        if (null != inputStream) {
            isRecording = false;


            inputStream.stop();
            inputStream.release();

            inputStream = null;
            inputStream = null;
        }
    }

    private void writeAudioDataToFile() {
        // Write the output audio in byte
        filePath = Environment.getDataDirectory() + "8k16bitMono.pcm";

        short sData[] = new short[BufferElements2Rec];

        FileOutputStream os = null;
        try {
            os = new FileOutputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    }


