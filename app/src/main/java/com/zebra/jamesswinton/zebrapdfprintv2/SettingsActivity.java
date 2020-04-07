package com.zebra.jamesswinton.zebrapdfprintv2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.view.MenuItem;

import com.zebra.jamesswinton.zebrapdfprintv2.databinding.ActivitySettingsBinding;
import com.zebra.sdk.settings.Setting;

public class SettingsActivity extends AppCompatActivity {

    // Debugging
    private static final String TAG = "SettingsActivity";

    // Constants


    // Private Variables


    // Public Variables
    private ActivitySettingsBinding mDataBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_settings);
        setSupportActionBar(mDataBinding.toolbarLayout.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Settings");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Init Fragment
        getSupportFragmentManager()
                .beginTransaction()
                .replace(mDataBinding.fragmentHolder.getId(), new SettingsFragment(), "SETTINGS-FRAGMENT")
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }
}
