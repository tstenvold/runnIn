package t00212844.comp2161.afinal;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainScreen extends AppCompatActivity {

    private Fragment runs;
    private Fragment user;
    private int curItem;

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
        runs = ActivitiesFragment.newInstance(null, null);
        user = UserInfo.newInstance(null, null);
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

    public void switchFragment(Fragment fragment) {
        FragmentManager transaction = getSupportFragmentManager();
        transaction.beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).replace(R.id.container, fragment).commit();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(getString(R.string.curitem), curItem);

    }

    @Override
    public void onBackPressed() {
        finish();
    }
}