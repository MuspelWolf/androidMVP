package com.example.miau.mvp30;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import java.io.IOException;
import java.net.InetSocketAddress;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static android.content.ContentValues.TAG;

public class Room extends AppCompatActivity implements RecognitionListener {

    private SpeechRecognizer speech = null;
    private static Queue<String> recordingQueue = new LinkedList<>();
    private static String fullRecord = "";
    private static boolean responseReceived = true;
    private String response;
    private int noPupil;

    private static boolean chronoState = false;
    long stopTime = 0;

    private ArrayList<String> speechResults;

    byte[] endSpeech = new byte[]{0, 0, 1, 1};
    byte[] speechRestart = new byte[]{1, 1, 0, 0};

    int current_volume;
    AudioManager audio;
    String WIFI;
    String PIN;

    private Button btnPlay;
    private Button btnStop;
    private Button btnPlayPause;
    private Chronometer chrono;
    private TextView pinName;
    private TextView wifiName;
    private TextView PupilsNo;

    private Intent speechIntent;

    InetSocketAddress inetSocketAddress = new InetSocketAddress( 8080 );
    Server ServerControl = new Server( inetSocketAddress );


    @Override
    public void onCreate(Bundle SavedInstanceState) {
        super.onCreate( SavedInstanceState );
        setContentView( R.layout.activity_room );
        btnPlay = findViewById( R.id.btnStart );
        btnStop = findViewById( R.id.btnStop );
        btnPlayPause = findViewById( R.id.btnPlayPause );
        chrono = findViewById( R.id.chronometer );

        ServerControl.start();
        speechIntent = new Intent( RecognizerIntent.ACTION_RECOGNIZE_SPEECH );

        pinName = findViewById( R.id.pinName );
        PupilsNo = findViewById( R.id.pupilNumber );
        wifiName = findViewById( R.id.wifiName );

        PupilsNo.setText(getPreferences(MODE_PRIVATE).getInt("PupilsCount",1));


        saveCurrentAudio();
        Bundle bundle;
        bundle = getIntent().getExtras();
        PIN = bundle.getString( "PIN" );
        WIFI = bundle.getString( "wifiName" );
        pinName.setText(PIN);
        wifiName.setText( WIFI );

    }

    public void ButtonStartEvent(View view) {
        btnPlay.setVisibility( View.GONE );
        btnPlayPause.setVisibility( View.VISIBLE );
        btnStop.setVisibility( View.VISIBLE );
        chrono.start();
        chrono.setBase( SystemClock.elapsedRealtime() + stopTime );
        btnPlayPause.setText( "Pause" );
        chronoState = false;
        startVoiceRecognitionCycle( speechIntent );
        muteAudio();
    }

    public void ButtonStopEvent(View view) {
        chrono.stop();
        ServerControl.broadcast( endSpeech );
        stopVoiceRecognition();
        resetAudio();
        finish();
    }

    public void ButtonPlayPauseEvent(View view) {
        if (!chronoState) {
            chrono.stop();
            btnPlayPause.setBackgroundResource(R.mipmap.play);
            stopTime = chrono.getBase() - SystemClock.elapsedRealtime();
            speech.cancel();
            ServerControl.broadcast( speechRestart );
            chronoState = true;
        } else {
            chrono.start();
            btnPlayPause.setText( "Pause" );
            chrono.setBase( SystemClock.elapsedRealtime() + stopTime );
            startVoiceRecognitionCycle( speechIntent );
            chronoState = false;
        }
    }
    private SpeechRecognizer getSpeechRecognizer() {
        if (speech == null) {
            speech = SpeechRecognizer.createSpeechRecognizer( this );
            speech.setRecognitionListener( this );
        }
        return speech;
    }

    //Init speech recognizer
    public void startVoiceRecognitionCycle(Intent speechIntent) {
        speechIntent.putExtra( RecognizerIntent.EXTRA_LANGUAGE, "es-ES" );
        speechIntent.putExtra( RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM );
        speechIntent.putExtra( RecognizerIntent.EXTRA_PARTIAL_RESULTS, true );
        getSpeechRecognizer().startListening( speechIntent );
    }

    //Cancel speech recognition
    public void stopVoiceRecognition() {
        if (speech != null) {
            speech.stopListening();
            speech.cancel();
            speech.destroy();
            speech = null;
            try {
                ServerControl.stop();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //Mutes Beep Speech Sound
    public void muteAudio() {
        AudioManager audio = (AudioManager) getSystemService( Context.AUDIO_SERVICE );
        audio.setStreamVolume( AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_SHOW_UI );
    }

    //Save User Audio
    public void saveCurrentAudio() {
        AudioManager audioManager = (AudioManager) getSystemService( Context.AUDIO_SERVICE );
        current_volume = audioManager.getStreamVolume( AudioManager.STREAM_MUSIC );
    }

    //Reset to beginning
    public void resetAudio() {
        audio = (AudioManager) getSystemService( Context.AUDIO_SERVICE );
        audio.setStreamVolume( AudioManager.STREAM_MUSIC, audio.getStreamMaxVolume( AudioManager.STREAM_MUSIC ),
                AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE );
    }

    //Listener Class
    @Override
    public void onReadyForSpeech(Bundle params) {
        Log.d( TAG, "onReadyForSpeech" );
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.d( TAG, "onBeginningOfSpeech" );
    }

    @Override
    public void onRmsChanged(float rmsdB) {}

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.d( TAG, "onBufferReceived" );
    }

    @Override
    public void onEndOfSpeech() {
        Log.d( TAG, "onEndOfSpeech" );

    }

    @Override
    public void onError(int error) {
        String message = null;
        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                startVoiceRecognitionCycle( speechIntent );
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Not recognised";
                break;
        }
        Log.d( TAG, "onError code:" + error + " message: " + message );
    }

    @Override
    public void onResults(Bundle results) {
        startVoiceRecognitionCycle( speechIntent );
        ServerControl.broadcast( speechRestart );
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        receiveResults( partialResults );
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        Log.d( TAG, "onEvent" );
    }

    private void receiveResults(Bundle results) {
        if ((results != null) && results.containsKey( SpeechRecognizer.RESULTS_RECOGNITION )) {
            List<String> heard = results.getStringArrayList( SpeechRecognizer.RESULTS_RECOGNITION );
            recordingQueue.add( heard.get( 0 ) );
            fullRecord = fullRecord + "" + (heard.get( 0 ));
            if (responseReceived) {
                response = response + "" + (heard.get( 0 ));
            }
            Log.d( TAG, String.valueOf( results.getStringArrayList( SpeechRecognizer.RESULTS_RECOGNITION ) ) );
            speechResults = results.getStringArrayList( SpeechRecognizer.RESULTS_RECOGNITION );
            ServerControl.broadcast( speechResults.toString().replace( "[", "" ).replace( "]", "" ) );
        }
    }

    public void onBackPressed() {
    }

    private BroadcastReceiver mWifiStateChangedReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            int extraWifiState = intent.getIntExtra( WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);

            switch (extraWifiState) {
                case WifiManager.WIFI_STATE_DISABLED:
                case WifiManager.WIFI_STATE_DISABLING:
                    updateUI(false);
                    break;
                case WifiManager.WIFI_STATE_ENABLED:
                    ConnectivityManager conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    while(conMan.getActiveNetworkInfo() == null || conMan.getActiveNetworkInfo().getState() != NetworkInfo.State.CONNECTED) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    updateUI(true);
                    break;
                case WifiManager.WIFI_STATE_ENABLING:
                    break;
                case WifiManager.WIFI_STATE_UNKNOWN:
                    break;
            }

        }
    };

    private void updateUI(boolean isConnected) {

        if(!isConnected) {
            new AlertDialog.Builder(Room.this).
                    setTitle("Conexion Perdida").
                    setMessage("Vas a volver a la pantalla de creacion de sala").
                    setCancelable( false ).
                    setPositiveButton( "Aceptar", new DialogInterface.OnClickListener() {
                        @Override
                        public
                        void onClick(DialogInterface dialog, int which) {
                            Intent backToCreateRoom = new Intent( Room.this, RoomCreate.class );
                            startActivity( backToCreateRoom );
                        }
                    } );

        }
    }
}
