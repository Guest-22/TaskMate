package com.example.taskmate;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.applandeo.materialcalendarview.CalendarDay;
import com.applandeo.materialcalendarview.CalendarView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CalendarViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CalendarViewFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CalendarViewFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CalendarViewFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CalendarViewFragment newInstance(String param1, String param2) {
        CalendarViewFragment fragment = new CalendarViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    /*
    // Single dot events.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_calendar_view, container, false);
        CalendarView calendarView = view.findViewById(R.id.calendarView);

        TaskDBHelper dbHelper = new TaskDBHelper(requireContext());
        List<Task> tasks = dbHelper.getAllTasks();

        List<CalendarDay> calendarDays = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        long todayMillis = System.currentTimeMillis();

        for (Task task : tasks) {
            try {
                String dateStr = task.getDate();
                String schedType = task.getType();

                if (dateStr == null || schedType == null) continue;

                Date parsedDate = sdf.parse(dateStr);
                if (parsedDate == null) continue;

                // Convert date into Calendar like docs.
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(parsedDate);

                // Determine icon based on type + date difference.
                long diffMillis = parsedDate.getTime() - todayMillis;
                long diffDays = diffMillis / (1000L * 60L * 60L * 24L);

                int iconRes;
                if ("Weekly".equalsIgnoreCase(schedType)) {
                    iconRes = R.drawable.dot_blue;
                } else {
                    if (diffDays <= 3) {
                        iconRes = R.drawable.dot_red;
                    } else if (diffDays <= 7) {
                        iconRes = R.drawable.dot_yellow;
                    } else {
                        iconRes = R.drawable.dot_green;
                    }
                }

                // Matches library docs style.
                CalendarDay calendarDay = new CalendarDay(calendar);
                calendarDay.setImageResource(iconRes);

                calendarDays.add(calendarDay);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Apply to CalendarView (docs approach)
        if (!calendarDays.isEmpty()) {
            calendarView.setCalendarDays(calendarDays);
        }
        return view;
    }
     */

    // Credits: Applandeo Material Calendar View on Github.
    // Source Link: https://github.com/Applandeo/Material-Calendar-View?tab=readme-ov-file
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_calendar_view, container, false);
        CalendarView calendarView = view.findViewById(R.id.calendarView);

        TaskDBHelper dbHelper = new TaskDBHelper(requireContext());
        List<Task> tasks = dbHelper.getAllTasks();

        List<CalendarDay> calendarDays = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        long todayMillis = System.currentTimeMillis();

        // Step 1: Loop through tasks manually by date.
        for (int i = 0; i < tasks.size(); i++) {
            Task current = tasks.get(i);
            String currentDate = current.getDate();
            if (currentDate == null) continue;

            // Check if we already handled this date.
            if (dateAlreadyAdded(calendarDays, currentDate, sdf)) {
                continue;
            }

            // Collect tasks with the same date.
            List<Task> sameDayTasks = new ArrayList<>();
            sameDayTasks.add(current);
            for (int j = i + 1; j < tasks.size(); j++) {
                if (currentDate.equals(tasks.get(j).getDate())) {
                    sameDayTasks.add(tasks.get(j));
                }
            }

            // Decide what icon to use.
            int iconRes;
            if (sameDayTasks.size() == 1) {
                iconRes = getSingleDotIcon(sameDayTasks.get(0), sdf, todayMillis);
            } else {
                iconRes = getDualDotIcon(sameDayTasks, sdf, todayMillis);
            }

            try {
                Date parsed = sdf.parse(currentDate);
                if (parsed != null) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(parsed);
                    CalendarDay day = new CalendarDay(cal);
                    day.setImageResource(iconRes);
                    calendarDays.add(day);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!calendarDays.isEmpty()) {
            calendarView.setCalendarDays(calendarDays);
        }

        return view;
    }

    // Date checker.
    private boolean dateAlreadyAdded(List<CalendarDay> days, String dateStr, SimpleDateFormat sdf) {
        try {
            Date target = sdf.parse(dateStr);
            Calendar calTarget = Calendar.getInstance();
            calTarget.setTime(target);

            for (CalendarDay cd : days) {
                Calendar existing = cd.getCalendar();
                if (existing.get(Calendar.YEAR) == calTarget.get(Calendar.YEAR) &&
                        existing.get(Calendar.MONTH) == calTarget.get(Calendar.MONTH) &&
                        existing.get(Calendar.DAY_OF_MONTH) == calTarget.get(Calendar.DAY_OF_MONTH)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // For single task in a single day.
    private int getSingleDotIcon(Task task, SimpleDateFormat sdf, long todayMillis) {
        try {
            Date parsedDate = sdf.parse(task.getDate());
            long diffMillis = parsedDate.getTime() - todayMillis;
            long diffDays = diffMillis / (1000L * 60L * 60L * 24L);

            if ("Weekly".equalsIgnoreCase(task.getType())) {
                return R.drawable.dot_blue;
            }
            if (diffDays <= 3) return R.drawable.dot_red;
            else if (diffDays <= 7) return R.drawable.dot_yellow;
            else return R.drawable.dot_green;

        } catch (Exception e) {
            e.printStackTrace();
            return R.drawable.dot_green;
        }
    }

    // For two task in a single day.
    private int getDualDotIcon(List<Task> sameDayTasks, SimpleDateFormat sdf, long todayMillis) {
        boolean hasWeekly = false;
        boolean hasOneTime = false;
        int urgencyIcon = R.drawable.dot_green; // Default.

        for (Task t : sameDayTasks) {
            if ("Weekly".equalsIgnoreCase(t.getType())) {
                hasWeekly = true;
            } else {
                hasOneTime = true;

                try {
                    Date parsedDate = sdf.parse(t.getDate());
                    long diffMillis = parsedDate.getTime() - todayMillis;
                    long diffDays = diffMillis / (1000L * 60L * 60L * 24L);

                    if (diffDays <= 3) urgencyIcon = R.drawable.dot_dual_red;
                    else if (diffDays <= 7) urgencyIcon = R.drawable.dot_dual_yellow;
                    else urgencyIcon = R.drawable.dot_dual_green;

                } catch (Exception e) {
                    e.printStackTrace();
                    urgencyIcon = R.drawable.dot_dual_red;
                }
            }
        }

        if (hasWeekly && hasOneTime) {
            return urgencyIcon; // Dual-dot based on urgency.
        }

        if (hasWeekly) return R.drawable.dot_blue; // Single blue dot.
        return urgencyIcon; // Fallback to urgency-only.
    }
}