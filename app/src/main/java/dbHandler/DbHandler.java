package dbHandler;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class DbHandler {

    public static void clearDb(FeedReaderDbHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.delete(FeedReaderContract.ImgRefsEntry.TABLE_NAME, null, null);
        db.delete(FeedReaderContract.CoordinatesEntry.TABLE_NAME, null, null);
        db.delete(FeedReaderContract.ImgPathEntry.TABLE_NAME, null, null);
    }

    //=========================== IMGREFS ============================

    /*
     * Add a new element to the ImgRefs table
     */
    public static void addImgRef(FeedReaderDbHelper dbHelper, int captureId, String imgRef) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(FeedReaderContract.ImgRefsEntry.COLUMN_NAME_CAPTURE_ID, captureId);
        values.put(FeedReaderContract.ImgRefsEntry.COLUMN_NAME_REF, imgRef);

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(FeedReaderContract.ImgRefsEntry.TABLE_NAME, null, values);
    }

    /*
     * Get a list of the capture-ids that are currently stored in the ImgRefs table
     */
    public static int[] getImgRefsCaptureId(FeedReaderDbHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String[] projection = {
                FeedReaderContract.ImgRefsEntry._ID,
                FeedReaderContract.ImgRefsEntry.COLUMN_NAME_CAPTURE_ID,
                FeedReaderContract.ImgRefsEntry.COLUMN_NAME_REF
        };

        Cursor cursor = db.query(true,                       // DISTINCT keyword to retrieve only unique rows
                FeedReaderContract.ImgRefsEntry.TABLE_NAME,         // The table to query
                projection,                                         // The array of columns to return (pass null to get all)
                null,                                               // No WHERE clause
                null,                                               // No selection args
                null,                                               // Don't group the rows
                null,                                               // Don't filter by row groups
                null,                                               // No sort order
                null                                                // No limit
        );

        int[] res;
        int i = 0;
        if (cursor.moveToFirst()) {
            res = new int[cursor.getCount()];
            do {
                int itemId = cursor.getInt(cursor.getColumnIndexOrThrow(FeedReaderContract.ImgRefsEntry.COLUMN_NAME_CAPTURE_ID));
                res[i++] = itemId;
            } while (cursor.moveToNext());
        } else {
            res = new int[0];
        }
        cursor.close();

        return res;
    }

    /*
     * Get a list of ImgRefObj that have a given capture_id.
     * Returns all information relative to a capture session.
     */
    public ArrayList<ImgRefObj> getImgRefsData(FeedReaderDbHelper dbHelper, int captureId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ArrayList<ImgRefObj> imgRefs = new ArrayList<>();

        String[] projection = {
                FeedReaderContract.ImgRefsEntry._ID,
                FeedReaderContract.ImgRefsEntry.COLUMN_NAME_CAPTURE_ID,
                FeedReaderContract.ImgRefsEntry.COLUMN_NAME_REF
        };

        String selection = FeedReaderContract.ImgRefsEntry.COLUMN_NAME_CAPTURE_ID + " = ?";
        String[] selectionArgs = { String.valueOf(captureId) };

        Cursor cursor = db.query(
                FeedReaderContract.ImgRefsEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(FeedReaderContract.ImgRefsEntry._ID));
            String ref = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.ImgRefsEntry.COLUMN_NAME_REF));

            ImgRefObj imgRef = new ImgRefObj(captureId, ref);
            imgRefs.add(imgRef);
        }

        cursor.close();

        return imgRefs;
    }

    /*
     * Delete a capture session from the ImgRefs table
     */
    public void deleteImgRefsByCaptureId(FeedReaderDbHelper dbHelper, int captureId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String selection = FeedReaderContract.ImgRefsEntry.COLUMN_NAME_CAPTURE_ID + " = ?";
        String[] selectionArgs = { String.valueOf(captureId) };

        db.delete(FeedReaderContract.ImgRefsEntry.TABLE_NAME, selection, selectionArgs);
    }

    //=========================== COORDINATES ============================
    public static void addCoordinates(FeedReaderDbHelper dbHelper, int captureId, float latitude, float longitude, String timestamp) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(FeedReaderContract.CoordinatesEntry.COLUMN_NAME_CAPTURE_ID, captureId);
        values.put(FeedReaderContract.CoordinatesEntry.COLUMN_NAME_LATITUDE, latitude);
        values.put(FeedReaderContract.CoordinatesEntry.COLUMN_NAME_LONGITUDE, longitude);
        values.put(FeedReaderContract.CoordinatesEntry.COLUMN_NAME_TIMESTAMP, timestamp);

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(FeedReaderContract.CoordinatesEntry.TABLE_NAME, null, values);
    }

    /*
     * Get a list of the capture-ids that are currently stored in the Coordinates table
     */
    public static int[] getCoordinatesCaptureId(FeedReaderDbHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String[] projection = {
                FeedReaderContract.CoordinatesEntry._ID,
                FeedReaderContract.CoordinatesEntry.COLUMN_NAME_CAPTURE_ID,
                FeedReaderContract.CoordinatesEntry.COLUMN_NAME_LATITUDE,
                FeedReaderContract.CoordinatesEntry.COLUMN_NAME_LONGITUDE,
                FeedReaderContract.CoordinatesEntry.COLUMN_NAME_TIMESTAMP,
        };

        Cursor cursor = db.query(true,                       // DISTINCT keyword to retrieve only unique rows
                FeedReaderContract.CoordinatesEntry.TABLE_NAME,     // The table to query
                projection,                                         // The array of columns to return (pass null to get all)
                null,                                               // No WHERE clause
                null,                                               // No selection args
                null,                                               // Don't group the rows
                null,                                               // Don't filter by row groups
                null,                                               // No sort order
                null                                                // No limit
        );

        int[] res;
        int i = 0;
        if (cursor.moveToFirst()) {
            res = new int[cursor.getCount()];
            do {
                int itemId = cursor.getInt(cursor.getColumnIndexOrThrow(FeedReaderContract.CoordinatesEntry.COLUMN_NAME_CAPTURE_ID));
                res[i++] = itemId;
            } while (cursor.moveToNext());
        } else {
            res = new int[0];
        }
        cursor.close();

        return res;
    }

    /*
     * Get a list of CoordinatesObj that have a given capture_id.
     * Returns all information relative to a capture session.
     */
    public ArrayList<CoordinatesObj> getCoordinatesData(FeedReaderDbHelper dbHelper, int captureId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ArrayList<CoordinatesObj> coordinatesList = new ArrayList<>();

        String[] projection = {
                FeedReaderContract.CoordinatesEntry._ID,
                FeedReaderContract.CoordinatesEntry.COLUMN_NAME_CAPTURE_ID,
                FeedReaderContract.CoordinatesEntry.COLUMN_NAME_LATITUDE,
                FeedReaderContract.CoordinatesEntry.COLUMN_NAME_LONGITUDE,
                FeedReaderContract.CoordinatesEntry.COLUMN_NAME_TIMESTAMP
        };
        String selection = FeedReaderContract.CoordinatesEntry.COLUMN_NAME_CAPTURE_ID + " = ?";
        String[] selectionArgs = { String.valueOf(captureId) };

        Cursor cursor = db.query(
                FeedReaderContract.CoordinatesEntry.TABLE_NAME,   // The table to query
                projection,                                       // The array of columns to return (pass null to get all)
                selection,                                        // The columns for the WHERE clause
                selectionArgs,                                    // The values for the WHERE clause
                null,                                             // don't group the rows
                null,                                             // don't filter by row groups
                null                                              // The sort order
        );

        if (cursor.moveToFirst()) {
            do {
                int capture_id = cursor.getInt(cursor.getColumnIndexOrThrow(FeedReaderContract.CoordinatesEntry.COLUMN_NAME_CAPTURE_ID));
                float latitude = cursor.getFloat(cursor.getColumnIndexOrThrow(FeedReaderContract.CoordinatesEntry.COLUMN_NAME_LATITUDE));
                float longitude = cursor.getFloat(cursor.getColumnIndexOrThrow(FeedReaderContract.CoordinatesEntry.COLUMN_NAME_LONGITUDE));
                String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.CoordinatesEntry.COLUMN_NAME_TIMESTAMP));
                CoordinatesObj coordinatesObj = new CoordinatesObj(capture_id, latitude, longitude, timestamp);
                coordinatesList.add(coordinatesObj);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return coordinatesList;
    }

    /*
     * Delete a capture session from the Coordinates table
     */
    public void deleteCoordinatessByCaptureId(FeedReaderDbHelper dbHelper, int captureId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String selection = FeedReaderContract.CoordinatesEntry.COLUMN_NAME_CAPTURE_ID + " = ?";
        String[] selectionArgs = { String.valueOf(captureId) };

        db.delete(FeedReaderContract.CoordinatesEntry.TABLE_NAME, selection, selectionArgs);
    }

    //=========================== IMGPATHS ============================

    /*
     * Add a new element to the ImgPaths table
     */
    public static void addImgPath(FeedReaderDbHelper dbHelper, int captureId, String imgPath) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(FeedReaderContract.ImgPathEntry.COLUMN_NAME_CAPTURE_ID, captureId);
        values.put(FeedReaderContract.ImgPathEntry.COLUMN_NAME_PATH, imgPath);

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(FeedReaderContract.ImgPathEntry.TABLE_NAME, null, values);
    }

    /*
     * Get a list of the capture-ids that are currently stored in the ImgPaths table
     */
    public static int[] getImgPathsCaptureId(FeedReaderDbHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String[] projection = {
                FeedReaderContract.ImgPathEntry._ID,
                FeedReaderContract.ImgPathEntry.COLUMN_NAME_CAPTURE_ID,
                FeedReaderContract.ImgPathEntry.COLUMN_NAME_PATH
        };

        Cursor cursor = db.query(true,                       // DISTINCT keyword to retrieve only unique rows
                FeedReaderContract.ImgPathEntry.TABLE_NAME,         // The table to query
                projection,                                         // The array of columns to return (pass null to get all)
                null,                                               // No WHERE clause
                null,                                               // No selection args
                null,                                               // Don't group the rows
                null,                                               // Don't filter by row groups
                null,                                               // No sort order
                null                                                // No limit
        );

        int[] res;
        int i = 0;
        if (cursor.moveToFirst()) {
            res = new int[cursor.getCount()];
            do {
                int itemId = cursor.getInt(cursor.getColumnIndexOrThrow(FeedReaderContract.ImgPathEntry.COLUMN_NAME_CAPTURE_ID));
                res[i++] = itemId;
            } while (cursor.moveToNext());
        } else {
            res = new int[0];
        }
        cursor.close();

        return res;
    }

    /*
     * Get a list of ImgPathObj that have a given capture_id.
     * Returns all information relative to a capture session.
     */
    public ArrayList<ImgPathObj> getImgPathsData(FeedReaderDbHelper dbHelper, int captureId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ArrayList<ImgPathObj> imgPaths = new ArrayList<>();

        String[] projection = {
                FeedReaderContract.ImgPathEntry._ID,
                FeedReaderContract.ImgPathEntry.COLUMN_NAME_CAPTURE_ID,
                FeedReaderContract.ImgPathEntry.COLUMN_NAME_PATH
        };

        String selection = FeedReaderContract.ImgRefsEntry.COLUMN_NAME_CAPTURE_ID + " = ?";
        String[] selectionArgs = { String.valueOf(captureId) };

        Cursor cursor = db.query(
                FeedReaderContract.ImgPathEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(FeedReaderContract.ImgPathEntry._ID));
            String path = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.ImgPathEntry.COLUMN_NAME_PATH));

            ImgPathObj imgPath = new ImgPathObj(captureId, path);
            imgPaths.add(imgPath);
        }

        cursor.close();

        return imgPaths;
    }

    /*
     * Delete a capture session from the ImgPaths table
     */
    public void deleteImgPathsByCaptureId(FeedReaderDbHelper dbHelper, int captureId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String selection = FeedReaderContract.ImgPathEntry.COLUMN_NAME_CAPTURE_ID + " = ?";
        String[] selectionArgs = { String.valueOf(captureId) };

        db.delete(FeedReaderContract.ImgPathEntry.TABLE_NAME, selection, selectionArgs);
    }

    //=========================== TESTING ============================

    private void databaseTest(FeedReaderDbHelper dbHelper) {
        // Gets the data repository in write mode
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Clear the table by deleting all rows
        int rowsDeleted = db.delete(FeedReaderContract.ImgRefsEntry.TABLE_NAME, null, null);

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(FeedReaderContract.ImgRefsEntry.COLUMN_NAME_CAPTURE_ID, 7);
        values.put(FeedReaderContract.ImgRefsEntry.COLUMN_NAME_REF, "1234567");

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(FeedReaderContract.ImgRefsEntry.TABLE_NAME, null, values);

        Cursor cursor = db.query(
                FeedReaderContract.ImgRefsEntry.TABLE_NAME,   // The table to query
                null,                                         // The array of columns to return (pass null to get all)
                null,                                         // The columns for the WHERE clause
                null,                                         // The values for the WHERE clause
                null,                                         // don't group the rows
                null,                                         // don't filter by row groups
                null                                          // The sort order
        );

        cursor.moveToNext();
        int capture_Id = cursor.getInt(cursor.getColumnIndexOrThrow(FeedReaderContract.ImgRefsEntry.COLUMN_NAME_CAPTURE_ID));
        cursor.close();

    }

}
