package android.e.footstepdetectiontest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.musicg.api.ClapApi;
import com.musicg.fingerprint.FingerprintSimilarity;
import com.musicg.wave.Wave;

public class MainActivity extends AppCompatActivity {
    private static final int RECORDER_BPP = 16;
    private static final int RECORDER_SAMPLERATE = 44100;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private AudioRecord recorder = null;
    private int bufferSize = 0;
    private Thread recordingThread = null;
    private boolean isRecording = false;
    private String name = "";
    private String outputName = "";
    private TextView simBox;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setButtonHandlers();
        enableButtons(false);
        setSim();

        bufferSize = AudioRecord.getMinBufferSize(8000,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
    }

    private void setButtonHandlers() {
        ((Button)findViewById(R.id.btnStart)).setOnClickListener(btnClick);
        ((Button)findViewById(R.id.btnStop)).setOnClickListener(btnClick);
    }

    private void enableButton(int id,boolean isEnable){
        ((Button)findViewById(id)).setEnabled(isEnable);
    }

    private void enableButtons(boolean isRecording) {
        enableButton(R.id.btnStart,!isRecording);
        enableButton(R.id.btnStop,isRecording);
    }

    private void setSim() {
        simBox = findViewById(R.id.sim);
    }

    private void startRecording(){
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING, bufferSize);

        int i = recorder.getState();
        if(i==1)
            recorder.startRecording();

        isRecording = true;

        recordingThread = new Thread(new Runnable() {

            @Override
            public void run() {
                writeAudioDataToFile();
            }
        },"AudioRecorder Thread");

        recordingThread.start();
    }

    private void writeAudioDataToFile(){
        byte data[] = new byte[bufferSize];
        File outputDir = this.getCacheDir();
        File outputFile = null;
        try {
            outputFile = File.createTempFile("aud", ".raw", outputDir);
            name = outputFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        FileOutputStream os = null;

        try {
            os = new FileOutputStream(outputFile);
        } catch (FileNotFoundException e) {
// TODO Auto-generated catch block
            e.printStackTrace();
        }

        int read = 0;

        if(null != os){
            while(isRecording){
                read = recorder.read(data, 0, bufferSize);

                if(AudioRecord.ERROR_INVALID_OPERATION != read){
                    try {
                        os.write(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void stopRecording(){
        if(null != recorder){
            isRecording = false;

            int i = recorder.getState();
            if(i==1)
                recorder.stop();
            recorder.release();

            recorder = null;
            recordingThread = null;
        }

        File outputWavDir = this.getCacheDir();
        File outputWavFile = null;
        try {
            outputWavFile = File.createTempFile("aud", ".wav", outputWavDir);
            outputName = outputWavFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        }

        copyWaveFile(name,outputName);
        deleteTempFile();
        compareAudio();
    }

    private void deleteTempFile() {
        File file = new File(name);

        file.delete();
    }

    private void copyWaveFile(String inFilename,String outFilename){
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = RECORDER_SAMPLERATE;
        int channels = 2;
        long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels/8;

        byte[] data = new byte[bufferSize];

        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;

            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);

            while(in.read(data) != -1){
                out.write(data);
            }

            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void WriteWaveFileHeader(
            FileOutputStream out, long totalAudioLen,
            long totalDataLen, long longSampleRate, int channels,
            long byteRate) throws IOException {

        byte[] header = new byte[44];

        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8); // block align
        header[33] = 0;
        header[34] = RECORDER_BPP; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
    }

    private void compareAudio() {
        Wave recorded = new Wave(outputName); // Base Audio file
        InputStream dog = this.getResources().openRawResource(R.raw.dog);
        InputStream clap = this.getResources().openRawResource(R.raw.clapping);
        InputStream speaking = this.getResources().openRawResource(R.raw.speaking);
        Wave clp = new Wave(clap); // Audio file to compare
        Wave dg = new Wave(dog);
        Wave spk = new Wave(speaking);
        // Finding Audio Fingerprint Similarity
        FingerprintSimilarity clps = recorded.getFingerprintSimilarity(clp);
        FingerprintSimilarity dgs = recorded.getFingerprintSimilarity(dg);
        FingerprintSimilarity spks = recorded.getFingerprintSimilarity(spk);
        float clpsim = clps.getSimilarity();
        float dgsim = dgs.getSimilarity();
        float spksim = spks.getSimilarity();
        String noise = "";
        if (clpsim > dgsim && clpsim > spksim) {
            noise = "CLAP";
        } else if (dgsim > spksim) {
            noise = "DOG";
        } else {
            noise = "SPEAKING";
        }
        simBox.setText(noise);
    }

    private View.OnClickListener btnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.btnStart:{

                    enableButtons(true);
                    startRecording();

                    break;
                }
                case R.id.btnStop:{

                    enableButtons(false);
                    stopRecording();

                    break;
                }
            }
        }
    };
}
