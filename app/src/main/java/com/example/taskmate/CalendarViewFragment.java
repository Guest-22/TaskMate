package com.example.taskmate;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.applandeo.materialcalendarview.CalendarDay;
import com.applandeo.materialcalendarview.CalendarView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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

    private CalendarView calendarView;

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

    // Credits: Applandeo Material Calendar View on Github.
    // Source Link: https://github.com/Applandeo/Material-Calendar-View?tab=readme-ov-file
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar_view, container, false);
        calendarView = view.findViewById(R.id.calendarView);
        refreshCalendar(); // Initial load

        FloatingActionButton fab = view.findViewById(R.id.floatingActionButton2);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddTaskActivity.class);
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (calendarView != null) {
            refreshCalendar(); // Reload when returning
        }
    }

    private void refreshCalendar() {
        TaskDBHelper dbHelper = new TaskDBHelper(requireContext());
        List<Task> tasks = dbHelper.getAllTasks();

        List<CalendarDay> calendarDays = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        long todayMillis = System.currentTimeMillis();

        for (int i = 0; i < tasks.size(); i++) {
            Task current = tasks.get(i);
            String currentDate = current.getDate();
            if (currentDate == null) continue;

            if (isDateAlreadyAdded(calendarDays, currentDate, sdf)) continue;

            List<Task> sameDayTasks = new ArrayList<>();
            sameDayTasks.add(current);
            for (int j = i + 1; j < tasks.size(); j++) {
                if (currentDate.equals(tasks.get(j).getDate())) {
                    sameDayTasks.add(tasks.get(j));
                }
            }

            int iconRes = TaskColorAssigner.getDotIcon(requireContext(), sameDayTasks);

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

        calendarView.setCalendarDays(calendarDays);
    }

    // Date checker.
    private boolean isDateAlreadyAdded(List<CalendarDay> days, String dateStr, SimpleDateFormat sdf) {
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
}