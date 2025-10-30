package com.example.taskmate;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
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
    private Menu topMenu;
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

        // Ask the user for post-notification permission at app launch.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.frame, listViewFragment).commit(); // Display list view as current frame.

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

                    if (topMenu != null) {
                        topMenu.findItem(R.id.sort).setVisible(true); // Show sort in ListView.
                    }
                    return true;
                } else if (id == R.id.calendarView) { // Else, replace it with calendar view
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.frame, calendarViewFragment)
                            .commit();
                    if (topMenu != null) {
                        topMenu.findItem(R.id.sort).setVisible(false); // Hide sort in CalendarView.
                    }
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
        topMenu = menu; // Stores menu for reference.
        return true;
    }

    // Email Feedback.
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.feedback) {
            openEmailApp();
            return true;
        }
        else if (item.getItemId() == R.id.restore_notifications) {
            restoreNotifications(this);
            return true;
        }
        else if (item.getItemId() == R.id.sort) {
            showSortOptions();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Opens the Email app with a message template.
    private void openEmailApp() {
        String adminEmail = "guest22developer@gmail.com"; // Receiver; dummy account.
        String subject = "App Feedback";
        String message = "Hey! Got some feedback:\n";

        // Subject and body template.
        String uriText = "mailto:" + adminEmail +
                        "?subject=" + Uri.encode(subject) +
                        "&body=" + Uri.encode(message);

        Uri uri = Uri.parse(uriText);
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, uri);

        try {
            startActivity(Intent.createChooser(emailIntent, "Send feedback via email")); // Allows user to choose an Email app of their likings.
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "No email app installed.", Toast.LENGTH_SHORT).show();
        }
    }

    // Reschedule all notifications manually.
    public void restoreNotifications(Context context) {
        AlarmScheduler.scheduleAllAlarms(context); // Reschedules all alarm notifs.
        Toast.makeText(context, "Notifications restored", Toast.LENGTH_SHORT).show();
    }

    // Show sort options.
    private void showSortOptions() {
        String[] options = {
                "Sort by Creation Date",
                "Sort by Due Date",
                "Sort by Schedule Type"
        };
        new AlertDialog.Builder(this)
                .setTitle("Sort Tasks By")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: listViewFragment.sortTasks("created"); break;
                        case 1: listViewFragment.sortTasks("date"); break;
                        case 2: listViewFragment.sortTasks("type"); break;
                    }
                })
                .show();
    }
}