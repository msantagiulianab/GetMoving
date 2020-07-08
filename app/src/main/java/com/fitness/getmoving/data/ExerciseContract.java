package com.fitness.getmoving.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Contract for the GetMoving app
 * A Contract class is a container for constants that define names for URIs, tables, and columns.
 */

public final class ExerciseContract {

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.fitness.getmoving.data";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.fitness.getmoving/exercises/ is a valid path for
     * looking at exercise data. content://com.fitness.getmoving/things/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "things".
     */
    public static final String PATH_EXERCISES = "exercises";

    /**
     * To prevent someone from accidentally instantiating the contract class,
     *     give it an empty constructor.
     */
    private ExerciseContract() {
    }

    /**
     * Inner class that defines constant values for the exercises database table.
     * Each entry in the table represents a single exercise.
     */
    public static final class ExerciseEntry implements BaseColumns {

        /**
         * The content URI to access the exercise data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_EXERCISES);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of exercises.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_EXERCISES;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single exercise.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_EXERCISES;

        /**
         * Name of database table for exercises
         */
        public final static String TABLE_NAME = "exercises";

        /**
         * Unique ID number for the exercise (only for use in the database table).
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Day of the workout.
         */
        public final static String COLUMN_EXERCISE_DAY = "day";

        /**
         * Name of the exercise.
         */
        public final static String COLUMN_EXERCISE_NAME = "name";

        /**
         * Place where I workout.
         * The only possible values are {@link #PLACE_HOME}, {@link #PLACE_GYM},
         * or {@link #PLACE_PARK}.
         */
        public final static String COLUMN_EXERCISE_PLACE = "place";

        /**
         * Reps for the exercise.
         */
        public final static String COLUMN_EXERCISE_REPS = "reps";

        /**
         * Possible values for the place.
         */
        public static final int PLACE_HOME = 0;
        public static final int PLACE_GYM = 1;
        public static final int PLACE_PARK = 2;

        /**
         * Returns whether or not the given place is {@link #PLACE_HOME}, {@link #PLACE_GYM},
         * or {@link #PLACE_PARK}.
         */
        public static boolean isValidPlace(int place) {
            return place == PLACE_HOME || place == PLACE_GYM || place == PLACE_PARK;
        }
    }
}
