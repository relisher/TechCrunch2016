package com.example.arelin.yarr;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import java.io.*;
// import com.darkprograms.speech.*;
import com.darkprograms.speech.microphone.MicrophoneAnalyzer;
import com.ibm.watson.developer_cloud.http.HttpMediaType;
import com.ibm.watson.developer_cloud.speech_to_text.v1.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.BaseRecognizeCallback;
import javax.sound.sampled.AudioFileFormat;
import javaFlacEncoder.FLACFileWriter;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button clickButton = (Button) findViewById(R.id.Listening);
        clickButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ambientListeningLoop();
            }
        });
        Button clickButton2 = (Button) findViewById(R.id.Keyword);
        clickButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(this, whatever.class);
                //startActivity(intent);
            }
        });
    }
    protected void ambientListeningLoop() {
        MicrophoneAnalyzer mic = new MicrophoneAnalyzer(FLACFileWriter.FLAC);
        SpeechToText service = new SpeechToText();
        service.setUsernameAndPassword("<username>", "<password>");
        service.setEndPoint("https://stream.watsonplatform.net/text-to-speech/api");
        mic.setAudioFile(new File("AudioInput.flac"));
        while(true){
            mic.open();
            final int THRESHOLD = 8;
            int volume = mic.getAudioVolume();
            boolean isSpeaking = (volume > THRESHOLD);
            if(isSpeaking){
                try {
                    System.out.println("RECORDING...");
                    mic.captureAudioToFile(mic.getAudioFile());//Saves audio to file.
                    do{
                        Thread.sleep(2000);//Updates every second
                    }
                    while(mic.getAudioVolume() > THRESHOLD);
                    RecognizeOptions.Builder builder = new RecognizeOptions.Builder();

                    builder.continuous(true).interimResults(true).contentType(HttpMediaType.AUDIO_FLAC);
                    service.recognizeUsingWebSocket(new FileInputStream(mic.getAudioFile()), builder.build(), new BaseRecognizeCallback() {
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
                }
                finally{
                    mic.close();//Makes sure microphone closes on exit.
                }
            }
        }
    }


}
