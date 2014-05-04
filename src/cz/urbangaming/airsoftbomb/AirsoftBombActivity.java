package cz.urbangaming.airsoftbomb;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

    private ArrayAdapter<String> aAdpt = null;

    public static final int MODE_PYROTECHNIC = R.drawable.pyrotechnic;
    public static final int MODE_OMNITOOL = R.drawable.omnitool;
    public static final int MODE_OPERATOR = R.drawable.operator;

    private int currentMode = MODE_OMNITOOL;

    Vibrator vibrator = null;
    IntentIntegrator scanIntegrator = null;
    private ImageButton greenScanButton = null;
    private TextView message = null;

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

        scanIntegrator = new IntentIntegrator(AirsoftBombActivity.this);
        vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        greenScanButton = (ImageButton) findViewById(R.id.greenbutton);

        greenScanButton.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                vibrator.vibrate(100);
                scanIntegrator.initiateScan();
            }
        });

        message = (TextView) findViewById(R.id.message);
        setBackgroundImage();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            String scanContent = scanResult.getContents();
            String scanFormat = scanResult.getFormatName();
            Log.d(DEBUG_TAG, "scanContent: " + scanContent);
            Log.d(DEBUG_TAG, "scanFormat: " + scanFormat);
            message.setText(scanContent);
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
                break;
            case MODE_PYROTECHNIC:
                message.setBackgroundResource(R.drawable.pyrotechnic_message);
                break;
            case MODE_OPERATOR:
                message.setBackgroundResource(R.drawable.operator_message);
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
