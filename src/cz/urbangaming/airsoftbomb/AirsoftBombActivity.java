package cz.urbangaming.airsoftbomb;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class AirsoftBombActivity extends ActionBarActivity implements ActionBar.OnNavigationListener {

    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
    private ArrayAdapter<String> aAdpt = null;
    public static final int MODE_PYROTECHNIC = R.drawable.omnitool;
    public static final int MODE_OMNITOOL = R.drawable.operator;
    public static final int MODE_OPERATOR = R.drawable.pyrotechnic;
    private int currentMode = MODE_OMNITOOL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_airsoft_bomb);
        // Set up the action bar to show a dropdown list.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        // Set up the dropdown list navigation in the action bar.
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

    }

    public void setBackgroundImage() {
        (((ViewGroup) findViewById(android.R.id.content)).getChildAt(0)).setBackgroundResource(currentMode);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Restore the previously serialized current dropdown position.
        if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            getSupportActionBar().setSelectedNavigationItem(savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Serialize the current dropdown position.
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
