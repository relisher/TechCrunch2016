package com.example.arelin.yarr;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import com.ibm.watson.developer_cloud.http.HttpMediaType;
import com.ibm.watson.developer_cloud.speech_to_text.v1.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.BaseRecognizeCallback;
import com.sigpwned.jsonification.JsonValue;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created by arelin on 5/8/16.
 */
import com.harman.everestelite.HeadPhoneCtrl;
public class AudioIn extends Thread  {
    private boolean stopped    = false;

    public AudioIn() {

        start();
    }

    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        AudioRecord recorder = null;
        short[][]   buffers  = new short[256][160];
        int         ix       = 0;

        try { // ... initialise


            SpeechToText service = new SpeechToText();
            RecognizeOptions.Builder builder = new RecognizeOptions.Builder();
            builder.continuous(true).interimResults(true).contentType("audio/l16; rate=16000");
            service.setUsernameAndPassword("92ecaafd-80cc-4acf-8b77-06b464f6f1c3", "pmcyy56RTfSF");
            int N = AudioRecord.getMinBufferSize(16000, AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);
            String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/8k16bitMono.pcm";
            final AudioManager mgr=(AudioManager)MainActivity.c.getSystemService(Context.AUDIO_SERVICE);


            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    16000,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    N*10);

            recorder.startRecording();

            // ... loop

            while(!stopped) {
                short[] buffer =  new short[44650];
                FileOutputStream os = null;
                try {
                    os = new FileOutputStream(filePath);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                N = recorder.read(buffer,0,44650);
                Thread.sleep(1000);
                //process is what you will do with the data...not defined here
                byte bData[] = short2byte(buffer);
                os.write(bData, 0, 44650);

                service.recognizeUsingWebSocket( new ByteArrayInputStream(bData),
                        builder.build(), new BaseRecognizeCallback() {
                            @Override
                            public void onError(Exception e)
                            {
                                Log.d("Error!", e.toString());
                                e.printStackTrace();
                            }
                            @Override
                            public void onTranscription(SpeechResults speechResults) {
                                System.out.println(speechResults);
                                Log.d("output:", speechResults.toString());
                                for (String s : AddKeywords.list)
                                {
                                    if (speechResults.toString().toLowerCase().contains(s.toLowerCase()))
                                    {
                                        Log.d("result","YAGOTME");
                                        mgr.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                                        BaseActivity.headphCtrl.ancCtrl.switchANC(false);

                                    }
                                }

                            }
                        }
                );
                //os.close();
            }
        } catch(Throwable x) {
            Log.d("ERROR","Error reading voice audio",x);
        } finally {
            close();
        }
    }
    private byte[] short2byte(short[] sData) {
        int shortArrsize = sData.length;
        byte[] bytes = new byte[shortArrsize * 2];

        for (int i = 0; i < shortArrsize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;
    }

    private void close() {
        stopped = true;
    }

}


