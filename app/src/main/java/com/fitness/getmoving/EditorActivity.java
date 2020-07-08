package com.fitness.getmoving;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import com.fitness.getmoving.data.ExerciseContract.ExerciseEntry;

import java.util.Calendar;
import java.util.Random;

/**
 * Activity that allows the user to create a new exercise or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    DatePickerDialog picker;
    EditText eText;
    Button btnGet;

    /**
     * Identifier for the exercise data loader
     */
    private static final int EXISTING_EXERCISE_LOADER = 0;

    /**
     * Content URI for the existing exercise (null if it's a new exercise)
     */
    private Uri mCurrentExerciseUri;
    /**
     * EditText field to enter the day of the exercise
     */
    private EditText mDayEditText;
    /**
     * EditText field to enter the name of the exercise
     */
    private EditText mNameEditText;
    /**
     * EditText field to enter the reps for the exercise
     */
    private EditText mRepsEditText;
    /**
     * EditText field to enter the place where we exercise
     */
    private Spinner mPlaceSpinner;
    /**
     * Place where we exercise. The possible valid values are in the ExerciseContract.java file:
     * {@link ExerciseEntry#PLACE_HOME}, {@link ExerciseEntry#PLACE_GYM}, or
     * {@link ExerciseEntry#PLACE_PARK}.
     */
    private int mPlace = ExerciseEntry.PLACE_HOME;
    /**
     * Boolean flag that keeps track of whether the exercise has been edited (true) or not (false)
     */
    private boolean mExerciseHasChanged = false;
    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mExerciseHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mExerciseHasChanged = true;
            return false;
        }
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new exercise or editing an existing one.
        Intent intent = getIntent();
        mCurrentExerciseUri = intent.getData();

        // If the intent DOES NOT contain a exercise content URI, then we know that we are
        // creating a new exercise.
        if (mCurrentExerciseUri == null) {
            // This is a new exercise, so change the app bar to say "Add an Exercise"
            setTitle(getString(R.string.editor_activity_title_new_exercise));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete an exercise that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing exercise, so change app bar to say "Edit Exercise"
            setTitle(getString(R.string.editor_activity_title_edit_exercise));

            // Initialize a loader to read the exercise data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_EXERCISE_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mDayEditText = findViewById(R.id.edit_exercise_day);
        mNameEditText = findViewById(R.id.edit_exercise_name);
        mRepsEditText = findViewById(R.id.edit_exercise_reps);
        mPlaceSpinner = findViewById(R.id.spinner_place);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mDayEditText.setOnTouchListener(mTouchListener);
        mNameEditText.setOnTouchListener(mTouchListener);
        mRepsEditText.setOnTouchListener(mTouchListener);
        mPlaceSpinner.setOnTouchListener(mTouchListener);

        setupSpinner();

        eText = findViewById(R.id.edit_exercise_day);
        eText.setInputType(InputType.TYPE_NULL);

        eText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);

                // date picker dialog
                picker = new DatePickerDialog(EditorActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                                String dayStr;

                                if (dayOfMonth <= 9) {
                                    dayStr = "0" + dayOfMonth;
                                } else {
                                    dayStr = String.valueOf(dayOfMonth);
                                }

                                if (monthOfYear <= 8) {
                                    eText.setText(dayStr + "/" + "0" + (monthOfYear + 1) + "/" + year);
                                } else {
                                    eText.setText(dayStr + "/" + (monthOfYear + 1) + "/" + year);
                                }
                            }
                        }, year, month, day);
                picker.show();
            }
        });


        btnGet = findViewById(R.id.button1);
        btnGet.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String[] funnyStatements = new String[5];
                funnyStatements[0] = getString(R.string.legDay);
                funnyStatements[1] = getString(R.string.doCardio);
                funnyStatements[2] = getString(R.string.stretching);
                funnyStatements[3] = getString(R.string.focus);
                funnyStatements[4] = getString(R.string.pizza);
                int rnd = new Random().nextInt(funnyStatements.length);

                Toast.makeText(EditorActivity.this, funnyStatements[rnd],
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Setup the dropdown spinner that allows the user to select the place where we exercise.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter placeSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_place_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        placeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mPlaceSpinner.setAdapter(placeSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mPlaceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.place_gym))) {
                        mPlace = ExerciseEntry.PLACE_GYM;
                    } else if (selection.equals(getString(R.string.place_park))) {
                        mPlace = ExerciseEntry.PLACE_PARK;
                    } else {
                        mPlace = ExerciseEntry.PLACE_HOME;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mPlace = ExerciseEntry.PLACE_HOME;
            }
        });
    }

    /**
     * Get user input from editor and save exercise into database.
     */
    private void saveExercise() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String dayString = mDayEditText.getText().toString().trim();
        String nameString = mNameEditText.getText().toString().trim();
        String repsString = mRepsEditText.getText().toString().trim();

        // Check if this is supposed to be a new exercise
        // and check if all the fields in the editor are blank
        if (mCurrentExerciseUri == null &&
                TextUtils.isEmpty(dayString) && TextUtils.isEmpty(nameString) &&
                TextUtils.isEmpty(repsString) && mPlace == ExerciseEntry.PLACE_HOME) {
            // Since no fields were modified, we can return early without creating a new exercise.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and exercise attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(ExerciseEntry.COLUMN_EXERCISE_DAY, dayString);
        values.put(ExerciseEntry.COLUMN_EXERCISE_NAME, nameString);
        values.put(ExerciseEntry.COLUMN_EXERCISE_PLACE, mPlace);
        // If the reps are not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int reps = 0;
        if (!TextUtils.isEmpty(repsString)) {
            reps = Integer.parseInt(repsString);
        }
        values.put(ExerciseEntry.COLUMN_EXERCISE_REPS, reps);

        // Determine if this is a new or existing exercise by checking if mCurrentExerciseUri is null or not
        if (mCurrentExerciseUri == null) {
            // This is a NEW exercise, so insert a new exercise into the provider,
            // returning the content URI for the new exercise.
            Uri newUri = getContentResolver().insert(ExerciseEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_exercise_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_exercise_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING exercise, so update the exercise with content URI: mCurrentExerciseUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentExerciseUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentExerciseUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_exercise_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_exercise_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new exercise, hide the "Delete" menu item.
        if (mCurrentExerciseUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save exercise to database
                saveExercise();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the exercise hasn't changed, continue with navigating up to parent activity
                // which is the {@link MainActivity}.
                if (!mExerciseHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the exercise hasn't changed, continue with handling back button press
        if (!mExerciseHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all exercise attributes, define a projection that contains
        // all columns from the exercise table
        String[] projection = {
                ExerciseEntry._ID,
                ExerciseEntry.COLUMN_EXERCISE_DAY,
                ExerciseEntry.COLUMN_EXERCISE_NAME,
                ExerciseEntry.COLUMN_EXERCISE_PLACE,
                ExerciseEntry.COLUMN_EXERCISE_REPS};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentExerciseUri,         // Query the content URI for the current exercise
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of exercise attributes that we're interested in
            int dayColumnIndex = cursor.getColumnIndex(ExerciseEntry.COLUMN_EXERCISE_DAY);
            int nameColumnIndex = cursor.getColumnIndex(ExerciseEntry.COLUMN_EXERCISE_NAME);
            int placeColumnIndex = cursor.getColumnIndex(ExerciseEntry.COLUMN_EXERCISE_PLACE);
            int repsColumnIndex = cursor.getColumnIndex(ExerciseEntry.COLUMN_EXERCISE_REPS);

            // Extract out the value from the Cursor for the given column index
            String day = cursor.getString(dayColumnIndex);
            String name = cursor.getString(nameColumnIndex);
            int place = cursor.getInt(placeColumnIndex);
            int reps = cursor.getInt(repsColumnIndex);

            // Update the views on the screen with the values from the database
            mDayEditText.setText(day);
            mNameEditText.setText(name);
            mRepsEditText.setText(Integer.toString(reps));

            // Place is a dropdown spinner, so map the constant value from the database
            // into one of the dropdown options (0 is Other, 1 is Gym, 2 is Park).
            // Then call setSelection() so that option is displayed on screen as the current selection.
            switch (place) {
                case ExerciseEntry.PLACE_GYM:
                    mPlaceSpinner.setSelection(1);
                    break;
                case ExerciseEntry.PLACE_PARK:
                    mPlaceSpinner.setSelection(2);
                    break;
                default:
                    mPlaceSpinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mDayEditText.setText("");
        mNameEditText.setText("");
        mRepsEditText.setText("");
        mPlaceSpinner.setSelection(0);
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the exercise.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this exercise.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the exercise.
                deleteExercise();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the exercise.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the exercise in the database.
     */
    private void deleteExercise() {
        // Only perform the delete if this is an existing exercise.
        if (mCurrentExerciseUri != null) {
            // Call the ContentResolver to delete the exercise at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentExerciseUri
            // content URI already identifies the exercise that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentExerciseUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_exercise_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_exercise_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }
}
