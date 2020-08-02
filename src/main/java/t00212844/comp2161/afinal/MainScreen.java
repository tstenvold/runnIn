/*
 * Copyright (c) 2020.
 * Programmed by: Terence Stenvold
 * Version 1.0
 *
 *
 */

package t00212844.comp2161.afinal;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * This is the main view with the bottom navigation bar that the user interacts with
 */
public class MainScreen extends AppCompatActivity {

    private Fragment runs;
    private Fragment user;
    private int curItem;

    //the main navigation bar
    BottomNavigationView bottomNavigation;
    final BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.runs:
                            if (curItem != R.id.runs) {
                                switchFragment(runs);
                            }
                            curItem = item.getItemId();
                            return true;
                        case R.id.record:
                            if (curItem != R.id.record) {
                                Intent intent = new Intent(getBaseContext(), Record.class);
                                startActivity(intent);
                            }
                            return false;
                        case R.id.user:
                            if (curItem != R.id.user) {
                                switchFragment(user);
                            }
                            curItem = item.getItemId();
                            return true;
                    }
                    return false;
                }
            };

    /**
     * Creates the view and populates if based on which item in the Bottom Nav Bar is selected
     *
     * @param savedInstanceState bundle of saved state variables
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        bottomNavigation = findViewById(R.id.bottomNav);
        bottomNavigation.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        curItem = R.id.runs;
        if (savedInstanceState != null) {
            curItem = savedInstanceState.getInt(getString(R.string.curitem), 0);
        }
        runs = ActivitiesFragment.newInstance();
        user = UserInfo.newInstance();
        bottomNavigation.setSelectedItemId(curItem);

        switch (curItem) {
            case R.id.runs:
            case R.id.record:
                switchFragment(runs);
                curItem = R.id.runs;
                break;
            case R.id.user:
                switchFragment(user);
                curItem = R.id.user;
                break;
        }
    }

    /**
     * Generalized switch to a different fragment
     *
     * @param fragment the fragment to be swtiched to.
     */
    public void switchFragment(Fragment fragment) {
        FragmentManager transaction = getSupportFragmentManager();
        transaction.beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).replace(R.id.container, fragment).commit();
    }

    /**
     * Saves the activities state information.
     *
     * @param outState bundle to saved state info into
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(getString(R.string.curitem), curItem);

    }

    /**
     * Close the app if the back button is pressed
     */
    @Override
    public void onBackPressed() {
        finish();
    }

    /**
     * Opens the permissions screen again if the logo in the top right is clicked.
     *
     */
    public void openPermissionWelcome(View view) {
        Intent intent = new Intent(getBaseContext(), PermissionsWelcome.class);
        startActivity(intent);
    }
}