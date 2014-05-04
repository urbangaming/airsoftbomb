package cz.urbangaming.airsoftbomb;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class AirsoftBombActivity extends ActionBarActivity implements ActionBar.OnNavigationListener {
    public static final String DEBUG_TAG = "KARM";
    private static final Pattern pattern = Pattern.compile("(operator|pyrotechnic)#([^#]*)#([0-9]*)");

    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

    private ArrayAdapter<String> aAdpt = null;

    public static final int MODE_PYROTECHNIC = R.drawable.pyrotechnic;
    public static final int MODE_OMNITOOL = R.drawable.omnitool;
    public static final int MODE_OPERATOR = R.drawable.operator;

    private int currentMode = MODE_OMNITOOL;
    private ImageButton greenScanButton = null;
    private ImageButton redStopButton = null;
    Vibrator vibrator = null;
    IntentIntegrator scanIntegrator = null;
    TextView message = null;
    TextView countdown = null;
    CountDownTimer countDownTimer = null;
    MediaPlayer mediaPlayer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_airsoft_bomb);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        this.aAdpt = new ArrayAdapter<String>(
                actionBar.getThemedContext(),
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                new String[] {
                        getString(R.string.omnitool),
                        getString(R.string.pyrotechnic),
                        getString(R.string.operator),
                });
        actionBar.setListNavigationCallbacks(this.aAdpt, this);

        initMediaPlayer();

        scanIntegrator = new IntentIntegrator(AirsoftBombActivity.this);
        vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        greenScanButton = (ImageButton) findViewById(R.id.greenbutton);

        greenScanButton.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                vibrator.vibrate(100);
                scanIntegrator.initiateScan();
            }
        });

        redStopButton = (ImageButton) findViewById(R.id.redbutton);

        redStopButton.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                vibrator.vibrate(100);
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                    countDownTimer = null;
                    countdown.setText(getResources().getString(R.string.countdown_init));
                    message.setText(message.getText() + getResources().getString(R.string.countdown_aborted));
                }
            }
        });

        message = (TextView) findViewById(R.id.messagex);
        countdown = (TextView) findViewById(R.id.countdown);

        setBackgroundImage();
    }

    private void initMediaPlayer() {
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        float maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mediaPlayer = MediaPlayer.create(this, R.raw.alarm);
        /*
         * mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
         * public void onCompletion(MediaPlayer mediaPlayer) {
         * playingFlag = false;
         * }
         * });
         */
        mediaPlayer.setVolume(maxVolume, maxVolume);
    }

    public void setCountDown(int minutes) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countdown.setText(getResources().getString(R.string.countdown_init));
            countDownTimer = null;
        }
        countDownTimer = new CountDownTimer(minutes * 60 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsUntilFinished = (int) (millisUntilFinished / 1000) % 60;
                int minutesUntilFinished = (int) (millisUntilFinished / 1000 / 60);
                countdown.setText(((minutesUntilFinished < 10) ? "0" + minutesUntilFinished : minutesUntilFinished) +
                        ":" + ((secondsUntilFinished < 10) ? "0" + secondsUntilFinished : secondsUntilFinished));
            }

            @Override
            public void onFinish() {
                countdown.setText(getResources().getString(R.string.countdown_init));
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.start();
                vibrator.vibrate(2000);
            }

        }.start();
        //countDownTimer.cancel();
    }

    public void processMessage(String text, String time) {
        if (text != null && !text.isEmpty()) {
            message.setText(getResources().getString(R.string.message_prefix) + text);
        } else {
            message.setText(getResources().getString(R.string.message_empty));
        }
        if (time != null && !time.isEmpty()) {
            try {
                int minutes = Integer.parseInt(time);
                setCountDown(minutes);
            } catch (NumberFormatException ex) {
                // Silence is golden.
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            String scanContent = scanResult.getContents();
            String scanFormat = scanResult.getFormatName();
            Log.d(DEBUG_TAG, "scanContent: " + scanContent);
            Log.d(DEBUG_TAG, "scanFormat: " + scanFormat);
            if (scanContent != null && !scanContent.isEmpty()) {
                Matcher matcher = pattern.matcher(scanContent);
                if (matcher.matches() && matcher.group(1) != null) {
                    Log.d(DEBUG_TAG, "Mode from message: " + matcher.group(1));
                    Log.d(DEBUG_TAG, "Text from message: " + matcher.group(2));
                    Log.d(DEBUG_TAG, "Minutes from message: " + matcher.group(3));
                    if (currentMode != MODE_OMNITOOL) {
                        if ((matcher.group(1).equalsIgnoreCase(getResources().getString(R.string.pyrotechnic)) && currentMode == MODE_PYROTECHNIC)
                                || (matcher.group(1).equalsIgnoreCase(getResources().getString(R.string.operator)) && currentMode == MODE_OPERATOR)) {
                            processMessage(matcher.group(2), matcher.group(3));
                        } else {
                            message.setText(getResources().getString(R.string.error_wrong_mode));
                        }
                    } else {
                        processMessage(matcher.group(2), matcher.group(3));
                    }
                } else {
                    message.setText(getResources().getString(R.string.error_wrong_scan));
                }
            }
        } else {
            Toast.makeText(AirsoftBombActivity.this, getResources().getString(R.string.error_no_scan), Toast.LENGTH_SHORT).show();
        }
    }

    public void setBackgroundImage() {
        (((ViewGroup) findViewById(android.R.id.content)).getChildAt(0)).setBackgroundResource(currentMode);
        if (message != null) {
            switch (currentMode) {
            case MODE_OMNITOOL:
                message.setBackgroundResource(R.drawable.omnitool_message);
                countdown.setBackgroundResource(R.drawable.omnitool_message);
                break;
            case MODE_PYROTECHNIC:
                message.setBackgroundResource(R.drawable.pyrotechnic_message);
                countdown.setBackgroundResource(R.drawable.pyrotechnic_message);
                break;
            case MODE_OPERATOR:
                message.setBackgroundResource(R.drawable.operator_message);
                countdown.setBackgroundResource(R.drawable.operator_message);
                break;
            default:
                break;
            }
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            getSupportActionBar().setSelectedNavigationItem(savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getSupportActionBar().getSelectedNavigationIndex());
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        boolean handled = true;
        if (aAdpt != null && itemPosition == aAdpt.getPosition(getResources().getString(R.string.omnitool))) {
            currentMode = MODE_OMNITOOL;
        } else if (aAdpt != null && itemPosition == aAdpt.getPosition(getResources().getString(R.string.pyrotechnic))) {
            currentMode = MODE_PYROTECHNIC;
        } else if (aAdpt != null && itemPosition == aAdpt.getPosition(getResources().getString(R.string.operator))) {
            currentMode = MODE_OPERATOR;
        } else {
            handled = false;
        }
        if (handled) {
            setBackgroundImage();
        }
        return handled;
    }

}
