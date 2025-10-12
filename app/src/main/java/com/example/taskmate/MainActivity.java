package com.example.taskmate;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    Toolbar topNav;
    BottomNavigationView botNav;
    ListViewFragment listViewFragment = new ListViewFragment();
    CalendarViewFragment calendarViewFragment = new CalendarViewFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.frame, listViewFragment).commit();

        botNav = findViewById(R.id.botNav);
        botNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();

                // If listview was selected, display it's fragment.
                if (id == R.id.listView) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.frame, listViewFragment)
                            .commit();
                    return true;
                } else if (id == R.id.calendarView) { // Else, replace it with calendar view
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.frame, calendarViewFragment)
                            .commit();
                    return true;
                }
                return false;
            }
        });

        topNav = findViewById(R.id.topNav);
        setSupportActionBar(topNav);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_nav_menu, menu);

        // Find the search item from the menu
        MenuItem searchItem = menu.findItem(R.id.search);
        androidx.appcompat.widget.SearchView searchView =
                (androidx.appcompat.widget.SearchView) searchItem.getActionView();

        // Placeholder text
        searchView.setQueryHint("Search");

        // Listener for typing or submitting
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // For now, just show a toast with typed text
                Toast.makeText(MainActivity.this, "You typed: " + query, Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // You can handle live search here if you want
                return false;
            }
        });

        return true;
    }

    // Email Feedback
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.feedback) {
            openEmailApp();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openEmailApp() {
        String adminEmail = "freshbiteshomecooks@gmail.com";
        String subject = "App Feedback";
        String message = "Hey! Got some feedback:\n";

        // Encode subject and body
        String uriText = "mailto:" + adminEmail +
                "?subject=" + Uri.encode(subject) +
                "&body=" + Uri.encode(message);

        Uri uri = Uri.parse(uriText);
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, uri);

        try {
            startActivity(Intent.createChooser(emailIntent, "Send feedback via email"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "No email app installed.", Toast.LENGTH_SHORT).show();
        }
    }


}