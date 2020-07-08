package com.fitness.getmoving;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.fitness.getmoving.data.ExerciseContract.ExerciseEntry;

/**
 * {@link CursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of exercise data as its data source. This adapter knows
 * how to create list items for each row of exercise data in the {@link Cursor}.
 */
public class ExerciseCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link ExerciseCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public ExerciseCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the exercise data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current exercise can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = view.findViewById(R.id.name);
        TextView summaryTextView = view.findViewById(R.id.summary);

        // Find the columns of exercise attributes that we're interested in
        int dayColumnIndex = cursor.getColumnIndex(ExerciseEntry.COLUMN_EXERCISE_DAY);
        int nameColumnIndex = cursor.getColumnIndex(ExerciseEntry.COLUMN_EXERCISE_NAME);

        // Read the exercise attributes from the Cursor for the current exercise
        String exerciseDay = cursor.getString(dayColumnIndex);
        String exerciseName = cursor.getString(nameColumnIndex);


        // If the exercise name is empty string or null, then use some default text
        // so the TextView isn't blank.
        if (TextUtils.isEmpty(exerciseName)) {
            exerciseName = context.getString(R.string.unknown_exercise);
        }

        // If the exercise date is empty string or null, then use some default text
        // so the TextView isn't blank.
        if (TextUtils.isEmpty(exerciseDay)) {
            exerciseDay = context.getString(R.string.unknown_date);
        }

        // Update the TextViews with the attributes for the current exercise
        nameTextView.setText(exerciseDay);
        summaryTextView.setText(exerciseName);
    }
}
