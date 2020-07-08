package com.fitness.getmoving;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.fitness.getmoving.data.ExerciseContract.ExerciseEntry;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Activity that displays list of exercises that were entered and stored in the app.
 */
public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, GoogleApiClient.OnConnectionFailedListener {

    /**
     * Identifier for the exercise data loader
     */
    private static final int EXERCISE_LOADER = 0;
    public final String APP_TAG = "GetMoving";

    /**
     * Adapter for the ListView
     */
    ExerciseCursorAdapter mCursorAdapter;
    File photoFile;

    /**
     * Firebase instance variables
     */
    private FirebaseAuth mFirebaseAuth;
    private GoogleApiClient mGoogleApiClient;
    private String mUsername;

    /**
     * Necessary for authentication
     */
    public static final String ANONYMOUS = "anonymous";
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public final static int CAMERA_PERMISSION_REQUEST_CODE = 4192;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set default username is anonymous.
        mUsername = ANONYMOUS;

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            mUsername = mFirebaseUser.getDisplayName();
            if (mFirebaseUser.getPhotoUrl() != null) {
                String mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = findViewById(R.id.button_create_exercise);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the exercise data
        ListView exerciseListView = findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        exerciseListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of exercise data in the Cursor.
        // There is no exercise data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new ExerciseCursorAdapter(this, null);
        exerciseListView.setAdapter(mCursorAdapter);

        // Setup the item click listener
        exerciseListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);

                // Form the content URI that represents the specific exercise that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link ExerciseEntry#CONTENT_URI}.
                // For example, the URI would be "content://com.example.getmoving"
                // if the exercise with ID 2 was clicked on.
                Uri currentExerciseUri = ContentUris.withAppendedId(ExerciseEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentExerciseUri);

                // Launch the {@link EditorActivity} to display the data for the current exercise.
                startActivity(intent);
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(EXERCISE_LOADER, null, this);
    }

    /**
     * Helper method to insert hardcoded exercise data into the database. For debugging purposes only.
     */
    private void insertExercise() {
        // Create a ContentValues object where column names are the keys,
        // and attributes of the specific exercise are the values.
        ContentValues values = new ContentValues();
        values.put(ExerciseEntry.COLUMN_EXERCISE_DAY, new SimpleDateFormat("dd/MM/yyyy",
                Locale.getDefault()).format(new Date()));
        values.put(ExerciseEntry.COLUMN_EXERCISE_NAME, "Push-Up");
        values.put(ExerciseEntry.COLUMN_EXERCISE_PLACE, ExerciseEntry.PLACE_GYM);
        values.put(ExerciseEntry.COLUMN_EXERCISE_REPS, 12);

        // Insert a new row for Monday into the provider using the ContentResolver.
        // Use the {@link ExerciseEntry#CONTENT_URI} to indicate that we want to insert
        // into the exercises database table.
        // Receive the new content URI that will allow us to access Monday data in the future.
        Uri newUri = getContentResolver().insert(ExerciseEntry.CONTENT_URI, values);
    }

    /**
     * Helper method to delete all exercises in the database.
     */
    private void deleteAllExercises() {
        int rowsDeleted = getContentResolver().delete(ExerciseEntry.CONTENT_URI, null, null);
        Log.v("MainActivity", rowsDeleted + " rows deleted from exercise database");
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_list);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the exercise.
                deleteAllExercises();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertExercise();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                showDeleteConfirmationDialog();
                return true;
            case R.id.sign_out_menu:
                mFirebaseAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                mUsername = ANONYMOUS;
                startActivity(new Intent(this, SignInActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                ExerciseEntry._ID,
                ExerciseEntry.COLUMN_EXERCISE_DAY,
                ExerciseEntry.COLUMN_EXERCISE_NAME};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                ExerciseEntry.CONTENT_URI,   // Provider content URI to query
                projection,     // Columns to include in the resulting Cursor
                null,       // No selection clause
                null,       // No selection arguments
                getString(R.string.sqlSortingDescending));              // Call a String to sort (SQL)
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link ExerciseCursorAdapter} with this new cursor containing updated exercise data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }

    public void takePhoto(View view) throws IOException {
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            invokeCamera();
        } else {
            // Request permission
            String[] permissionRequest = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

            // Show dialog box to ask for permission
            requestPermissions(permissionRequest, 4192);
        }
    }


    /**
     * Check permissions before starting the camera
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                try {
                    invokeCamera();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, R.string.cannotStartCamera, Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Start the camera
     */
    private void invokeCamera() throws IOException {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        // Use timestamp to generate the filename for the photo
        String timestamp = new SimpleDateFormat("yyyyMMdd_hh-mm-ss'.jpg'").format(new Date());

        String photoFileName = "Pic_" + timestamp;

        photoFile = getPhotoFileUri(photoFileName);

        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(this,
                    "com.fitness.getmoving.provider",
                    photoFile);

            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

            if (intent.resolveActivity(getPackageManager()) != null) {
                // Start the image capture intent to take photo
                startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
            }
        }
    }


    private File getPhotoFileUri(String photoFileName) throws IOException {

        File mediaStorageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(APP_TAG, "failed to create directory");
        }

        File file = File.createTempFile(
                photoFileName,  /* prefix */
                ".jpg",         /* suffix */
                mediaStorageDir      /* directory */
        );

        return file;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(APP_TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }
}
