package t00212844.comp2161.afinal;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainScreen extends AppCompatActivity {

    BottomNavigationView bottomNavigation;
    BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.runs:
                            switchFragment(ActivitiesFragment.newInstance(null, null));
                            return true;
                        case R.id.record:
                            switchFragment(RecordFragment.newInstance(null, null));
                            return true;
                        case R.id.user:
                            //switchFragment(User.newInstance(null , null));
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

        switchFragment(ActivitiesFragment.newInstance(null, null));
        bottomNavigation.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

    }

    public void switchFragment(Fragment fragment) {
        FragmentManager transaction = getSupportFragmentManager();
        transaction.beginTransaction().replace(R.id.container, fragment).commit();
    }
}