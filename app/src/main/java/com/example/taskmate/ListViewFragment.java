package com.example.taskmate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ListViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListViewFragment extends Fragment {
    // Initializing variables.
    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private List<Task> taskList;
    private TaskDBHelper dbHelper;
    private String currentSortMode = "created"; // default sort.


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ListViewFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ListViewFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ListViewFragment newInstance(String param1, String param2) {
        ListViewFragment fragment = new ListViewFragment();
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

    // Called when the fragment's view is being created.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout XML file for this fragment and store it in a View object.
        View view = inflater.inflate(R.layout.fragment_list_view, container, false);

        recyclerView = view.findViewById(R.id.rvListItem); // Find the RecyclerView from the inflated layout

        dbHelper = new TaskDBHelper(getContext()); // Create an instance of the database helper to access stored tasks.
        taskList = dbHelper.getAllTasks(); // Retrieve all tasks from the database.

        adapter = new TaskAdapter(taskList); // Create a TaskAdapter using the retrieved task list.
        recyclerView.setAdapter(adapter); // Set the adapter to the RecyclerView to display the tasks.

        // Find the FloatingActionButton from the layout.
        FloatingActionButton fab = view.findViewById(R.id.floatingActionButton);
        // Set a click listener on the FAB to open AddTaskActivity when clicked.
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddTaskActivity.class);
            startActivity(intent); // Launch the activity to add a new task.
        });

        // Return the fully prepared view to be displayed.
        return view;
    }

    // Called when the fragment becomes visible again (e.g., after returning from another activity).
    @Override
    public void onResume() {
        super.onResume();

        if (dbHelper == null) dbHelper = new TaskDBHelper(getContext());

        sortTasks(currentSortMode); // Reapply the last selected sort.
        adapter.notifyDataSetChanged(); // Refresh the existing adapter
    }

    // Sorts tasks as per selected criteria.
    public void sortTasks(String criteria) {
        if (taskList == null || adapter == null || dbHelper == null) return;

        currentSortMode = criteria;

        taskList.clear(); // Clear old task list data.

        // Repopulate task list.
        switch (criteria) {
            case "date":
                taskList.addAll(dbHelper.getTasksSortedByDueDate());
                break;
            case "type":
                taskList.addAll(dbHelper.getTasksSortedByScheduleType());
                break;
            case "created":
            default:
                taskList.addAll(dbHelper.getTasksSortedByCreation());
                break;
        }
        adapter.notifyDataSetChanged(); // Refresh RecyclerView
    }
}