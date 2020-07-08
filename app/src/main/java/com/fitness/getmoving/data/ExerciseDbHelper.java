package com.fitness.getmoving.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.fitness.getmoving.data.ExerciseContract.ExerciseEntry;

/**
 * {@link SQLiteOpenHelper} for the GetMoving app
 * An Helper class manages database creation and version management.
 */
public class ExerciseDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = ExerciseDbHelper.class.getSimpleName();

    /**
     * Name of the database file
     */
    private static final String DATABASE_NAME = "workout.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link ExerciseDbHelper}.
     * @param context of the app
     */
    public ExerciseDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the exercises table
        String SQL_CREATE_EXERCISES_TABLE = "CREATE TABLE " + ExerciseEntry.TABLE_NAME + " ("
                + ExerciseEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ExerciseEntry.COLUMN_EXERCISE_DAY + " TEXT NOT NULL, "
                + ExerciseEntry.COLUMN_EXERCISE_NAME + " TEXT, "
                + ExerciseEntry.COLUMN_EXERCISE_PLACE + " INTEGER NOT NULL, "
                + ExerciseEntry.COLUMN_EXERCISE_REPS + " INTEGER NOT NULL DEFAULT 0);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_EXERCISES_TABLE);

    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to be done here.
    }
}
