package com.example.arelin.yarr;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.ibm.watson.developer_cloud.http.HttpMediaType;
import com.ibm.watson.developer_cloud.speech_to_text.v1.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.BaseRecognizeCallback;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by arelin on 5/7/16.
 */
public class RecordingHandler implements Runnable

{

    SoundMeter soundMeter = new SoundMeter();
    @Override
    public void run() {

        final SpeechToText service = new SpeechToText();
        final RecognizeOptions.Builder builder = new RecognizeOptions.Builder();

        service.setUsernameAndPassword("92ecaafd-80cc-4acf-8b77-06b464f6f1c3", "pmcyy56RTfSF");
        while(true){
            try {
                soundMeter.start();
                do {
                    Thread.sleep(1000);
                }
                while (soundMeter.getAmplitude() > 4.0);
                soundMeter.stop();
                builder.continuous(true).interimResults(true).contentType(HttpMediaType.AUDIO_WAV);
                FFmpeg ffmpeg = FFmpeg.getInstance(MainActivity.c);
                String[] cmd = {"-i", Environment.getExternalStorageDirectory().getAbsolutePath() + "/audiorecordtest.AAC",
                        Environment.getExternalStorageDirectory().getAbsolutePath() + "/audiorecordtest.wav"};
                try {
                    // to execute "ffmpeg -version" command you just need to pass "-version"
                    ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {

                        @Override
                        public void onStart() {}

                        @Override
                        public void onProgress(String message) {
                            Log.d("progress:", message);
                        }

                        @Override
                        public void onFailure(String message) {
                            Log.d("progress:", message);
                        }

                        @Override
                        public void onSuccess(String message) {
                            Log.d("progress:", message);
                        }

                        @Override
                        public void onFinish() {
                            try {
                                service.recognizeUsingWebSocket(new FileInputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + "/audiorecordtest.wav"),
                                        builder.build(), new BaseRecognizeCallback() {
                                            @Override
                                            public void onTranscription(SpeechResults speechResults) {
                                                System.out.println(speechResults);
                                                Log.d("output:", speechResults.toString());
                                            }
                                        }
                                );
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (FFmpegCommandAlreadyRunningException e) {
                    // Handle if FFmpeg is already running
                }

            } catch (InterruptedException e) {
                Log.e("error", e.getMessage());
            }

        }
    }
}
