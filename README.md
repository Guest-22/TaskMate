# TaskMate
TaskMate is a simple Android application that helps users manage their tasks through a calendar and list-based interface. It uses visual cues like colored dots and list highlights to indicate task urgency and type.

## Features
- Calendar view with urgency-based dot icons.
- List view with color-coded task entries.
- Add, edit, and delete tasks with title, description, date, time, and type.
- Sort tasks by date, urgency, or type.
- Restore notifications after reboot or app restart (automatic and manual options available).
- SQLite database for local task storage.

## Color Indicators
TaskMate uses color-coded visuals to represent task urgency and type across both the calendar and list views.
- **Gray**: Expired one-time task (past due).
- **Red**: High urgency (3 days before deadline).
- **Yellow**: Moderate urgency (7 days before deadline).
- **Green**: Low urgency (more than 7 days).
- **Blue**: Recurring task (weekly).
- 
> Calendar dots are limited to one or two per date, depending on task type. We do not stack multiple dots — dual-dot logic is applied only when both weekly and one-time tasks fall on the same day.
