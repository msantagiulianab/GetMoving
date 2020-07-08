package com.fitness.getmoving.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.fitness.getmoving.data.ExerciseContract.ExerciseEntry;

/**
 * {@link ContentProvider} for the GetMoving app
 * A content provider manages access to a central repository of data.
 */
public class ExerciseProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = ExerciseProvider.class.getSimpleName();

    /**
     * URI matcher code for the content URI for the exercises table
     */
    private static final int EXERCISES = 100;

    /**
     * URI matcher code for the content URI for a single exercise in the exercises table
     */
    private static final int EXERCISE_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        sUriMatcher.addURI(ExerciseContract.CONTENT_AUTHORITY, ExerciseContract.PATH_EXERCISES, EXERCISES);

        sUriMatcher.addURI(ExerciseContract.CONTENT_AUTHORITY, ExerciseContract.PATH_EXERCISES + "/#", EXERCISE_ID);
    }

    /**
     * Database helper object
     */
    private ExerciseDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new ExerciseDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case EXERCISES:
                // For the EXERCISES code, query the exercises table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the exercises table.
                cursor = database.query(ExerciseEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case EXERCISE_ID:
                // For the EXERCISE_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.fitness.getmoving/exercises/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = ExerciseEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the exercises table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(ExerciseEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the cursor
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case EXERCISES:
                return insertExercise(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert an exercise into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertExercise(Uri uri, ContentValues values) {
        // Check that the day is not null
        String day = values.getAsString(ExerciseEntry.COLUMN_EXERCISE_DAY);
        if (day == null) {
            throw new IllegalArgumentException("Exercise requires a day");
        }

        // Check that the place is valid
        Integer place = values.getAsInteger(ExerciseEntry.COLUMN_EXERCISE_PLACE);
        if (place == null || !ExerciseEntry.isValidPlace(place)) {
            throw new IllegalArgumentException("Exercise requires valid place");
        }

        // If the reps are provided, check that it's greater than or equal to 0
        Integer reps = values.getAsInteger(ExerciseEntry.COLUMN_EXERCISE_REPS);
        if (reps != null && reps < 0) {
            throw new IllegalArgumentException("Exercise requires valid reps");
        }

        // No need to check the breed, any value is valid (including null).

        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new exercise with the given values
        long id = database.insert(ExerciseEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the exercise content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case EXERCISES:
                return updateExercise(uri, contentValues, selection, selectionArgs);
            case EXERCISE_ID:
                // For the EXERCISE_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = ExerciseEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateExercise(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update exercises in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more exercises).
     * Return the number of rows that were successfully updated.
     */
    private int updateExercise(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link ExerciseEntry#COLUMN_EXERCISE_DAY} key is present,
        // check that the name value is not null.
        if (values.containsKey(ExerciseEntry.COLUMN_EXERCISE_DAY)) {
            String day = values.getAsString(ExerciseEntry.COLUMN_EXERCISE_DAY);
            if (day == null) {
                throw new IllegalArgumentException("Exercise requires a day");
            }
        }

        // If the {@link ExerciseEntry#COLUMN_EXERCISE_PLACE} key is present,
        // check that the place value is valid.
        if (values.containsKey(ExerciseEntry.COLUMN_EXERCISE_PLACE)) {
            Integer place = values.getAsInteger(ExerciseEntry.COLUMN_EXERCISE_PLACE);
            if (place == null || !ExerciseEntry.isValidPlace(place)) {
                throw new IllegalArgumentException("Exercise requires valid place");
            }
        }

        // If the {@link ExerciseEntry#COLUMN_EXERCISE_REPS} key is present,
        // check that the reps value is valid.
        if (values.containsKey(ExerciseEntry.COLUMN_EXERCISE_REPS)) {
            // Check that the number of reps is greater than or equal to 0
            Integer reps = values.getAsInteger(ExerciseEntry.COLUMN_EXERCISE_REPS);
            if (reps != null && reps < 0) {
                throw new IllegalArgumentException("Exercise requires valid reps");
            }
        }

        // No need to check the breed, any value is valid (including null).

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(ExerciseEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case EXERCISES:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(ExerciseEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case EXERCISE_ID:
                // Delete a single row given by the ID in the URI
                selection = ExerciseEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(ExerciseEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case EXERCISES:
                return ExerciseEntry.CONTENT_LIST_TYPE;
            case EXERCISE_ID:
                return ExerciseEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
