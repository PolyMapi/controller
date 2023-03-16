package dbHandler;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DbHandler {

    //=========================== UTILS ============================
    public static int getNewCaptureId(FeedReaderDbHelper dbHelper) {

        int[] array1 = getImgRefsCaptureId(dbHelper);
        int[] array2 = getCoordinatesCaptureId(dbHelper);
        int[] array3 = getImgPathsCaptureId(dbHelper);

        // Combine the arrays
        int[] combinedArray = new int[array1.length + array2.length + array3.length];
        System.arraycopy(array1, 0, combinedArray, 0, array1.length);
        System.arraycopy(array2, 0, combinedArray, array1.length, array2.length);
        System.arraycopy(array3, 0, combinedArray, array1.length + array2.length, array3.length);

        // Sort the array
        Arrays.sort(combinedArray);

        // Iterate through the array and find the lowest non-negative integer
        int lowestNonNegative = 0;
        for (int i = 0; i < combinedArray.length; i++) {
            if (combinedArray[i] >= 0) {
                if (combinedArray[i] > lowestNonNegative) {
                    break;
                }
                lowestNonNegative++;
            }
        }

        return lowestNonNegative;
    }

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

        Log.d("Debug", "addImgRef: saved img " + imgRef);
    }

    /*
     * Get a list of the capture-ids that are currently stored in the ImgRefs table
     */
    public static int[] getImgRefsCaptureId(FeedReaderDbHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String[] projection = {
                FeedReaderContract.ImgRefsEntry.COLUMN_NAME_CAPTURE_ID
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
    public static ArrayList<ImgRefObj> getImgRefsData(FeedReaderDbHelper dbHelper, int captureId) {
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
    public static void deleteImgRefsByCaptureId(FeedReaderDbHelper dbHelper, int captureId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String selection = FeedReaderContract.ImgRefsEntry.COLUMN_NAME_CAPTURE_ID + " = ?";
        String[] selectionArgs = { String.valueOf(captureId) };

        db.delete(FeedReaderContract.ImgRefsEntry.TABLE_NAME, selection, selectionArgs);
    }

    //=========================== COORDINATES ============================
    public static void addCoordinates(FeedReaderDbHelper dbHelper, int captureId, double latitude, double longitude, String timestamp) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(FeedReaderContract.CoordinatesEntry.COLUMN_NAME_CAPTURE_ID, captureId);
        values.put(FeedReaderContract.CoordinatesEntry.COLUMN_NAME_LATITUDE, latitude);
        values.put(FeedReaderContract.CoordinatesEntry.COLUMN_NAME_LONGITUDE, longitude);
        values.put(FeedReaderContract.CoordinatesEntry.COLUMN_NAME_TIMESTAMP, timestamp);

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(FeedReaderContract.CoordinatesEntry.TABLE_NAME, null, values);

        Log.d("Debug", "addCoordinates: saved coordinates lat : " + latitude + " long : " + longitude + " timestamp : " + timestamp);
    }

    /*
     * Get a list of the capture-ids that are currently stored in the Coordinates table
     */
    public static int[] getCoordinatesCaptureId(FeedReaderDbHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String[] projection = {
                FeedReaderContract.CoordinatesEntry.COLUMN_NAME_CAPTURE_ID
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
    public static ArrayList<CoordinatesObj> getCoordinatesData(FeedReaderDbHelper dbHelper, int captureId) {
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
                double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(FeedReaderContract.CoordinatesEntry.COLUMN_NAME_LATITUDE));
                double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(FeedReaderContract.CoordinatesEntry.COLUMN_NAME_LONGITUDE));
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
    public static void deleteCoordinatesByCaptureId(FeedReaderDbHelper dbHelper, int captureId) {
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
                FeedReaderContract.ImgPathEntry.COLUMN_NAME_CAPTURE_ID
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
    public static ArrayList<ImgPathObj> getImgPathsData(FeedReaderDbHelper dbHelper, int captureId) {
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
    public static void deleteImgPathsByCaptureId(FeedReaderDbHelper dbHelper, int captureId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String selection = FeedReaderContract.ImgPathEntry.COLUMN_NAME_CAPTURE_ID + " = ?";
        String[] selectionArgs = { String.valueOf(captureId) };

        db.delete(FeedReaderContract.ImgPathEntry.TABLE_NAME, selection, selectionArgs);
    }

    //=========================== TESTING ============================

    public static void databaseTest(FeedReaderDbHelper dbHelper) {

        clearDb(dbHelper);

        // add elements
        addImgRef(dbHelper, 0, "111");
        addImgRef(dbHelper, 0, "222");
        addImgRef(dbHelper, 1, "333");
        addImgRef(dbHelper, 1, "444");

        // get and log all capture ids
        int[] allCaptureIds = getImgRefsCaptureId(dbHelper);
        for (int i : allCaptureIds) {
            Log.d("dbTest", "capture id : " + i);
        }

        // get and log data for capture session 0
        ArrayList<ImgRefObj> imgRefsData = getImgRefsData(dbHelper, 0);
        for (ImgRefObj imgRef : imgRefsData) {
            Log.d("dbTest", "imgRefData : " + imgRef.imgRef);
        }

        // delete capture 0 and log remaining captures
        deleteImgRefsByCaptureId(dbHelper, 0);
        allCaptureIds = getImgRefsCaptureId(dbHelper);
        for (int i : allCaptureIds) {
            Log.d("dbTest", "capture id : " + i);
        }

        Log.d("dbTest", "STOP IMGREF TEST");
        Log.d("dbTest", "START COORDINATES TEST");

        clearDb(dbHelper);

        // add elements
        addCoordinates(dbHelper,0, 0.0, 0.0, "0");
        addCoordinates(dbHelper,0, 1.0, 1.0, "1");
        addCoordinates(dbHelper,1, 2.0, 2.0, "2");
        addCoordinates(dbHelper,1, 3.0, 3.0, "3");

        // get and log all capture ids
        allCaptureIds = getCoordinatesCaptureId(dbHelper);
        for (int i : allCaptureIds) {
            Log.d("dbTest", "capture id : " + i);
        }

        // get and log data for capture session 0
        ArrayList<CoordinatesObj> coordinatesData = getCoordinatesData(dbHelper, 0);
        for (CoordinatesObj coordinates : coordinatesData) {
            Log.d("dbTest", "coordinatesData : " + coordinates.latitude);
            Log.d("dbTest", "coordinatesData : " + coordinates.longitude);
            Log.d("dbTest", "coordinatesData : " + coordinates.timestamp);
        }

        // delete capture 0 and log remaining captures
        deleteCoordinatesByCaptureId(dbHelper, 0);
        allCaptureIds = getCoordinatesCaptureId(dbHelper);
        for (int i : allCaptureIds) {
            Log.d("dbTest", "capture id : " + i);
        }

        Log.d("dbTest", "STOP COORDINATES TEST");
        Log.d("dbTest", "START IMGPATH TEST");

        clearDb(dbHelper);

        // add elements
        addImgPath(dbHelper, 0, "images/111");
        addImgPath(dbHelper, 0, "images/222");
        addImgPath(dbHelper, 1, "images/333");
        addImgPath(dbHelper, 1, "images/444");

        // get and log all capture ids
        allCaptureIds = getImgPathsCaptureId(dbHelper);
        for (int i : allCaptureIds) {
            Log.d("dbTest", "capture id : " + i);
        }

        // get and log data for capture session 0
        ArrayList<ImgPathObj> imgPathsData = getImgPathsData(dbHelper, 0);
        for (ImgPathObj imgPath : imgPathsData) {
            Log.d("dbTest", "imgPathData : " + imgPath.imgPath);
        }

        // delete capture 0 and log remaining captures
        deleteImgPathsByCaptureId(dbHelper, 0);
        allCaptureIds = getImgPathsCaptureId(dbHelper);
        for (int i : allCaptureIds) {
            Log.d("dbTest", "capture id : " + i);
        }

        Log.d("dbTest", "STOP IMGPATH TEST");
    }

    public static void databasePersistenceTestBegin(FeedReaderDbHelper dbHelper) {
        clearDb(dbHelper);

        addImgRef(dbHelper, 0, "000");
        addImgRef(dbHelper, 1, "111");
        addImgRef(dbHelper, 2, "222");

        Log.d("dbTest", "databasePersistenceTestBegin: wrote 3 captures");
    }

    public static void databasePersistenceTestEnd(FeedReaderDbHelper dbHelper) {
        // get and log all capture ids
        int[] allCaptureIds = getImgRefsCaptureId(dbHelper);
        for (int i : allCaptureIds) {
            Log.d("dbTest", "capture id : " + i);
        }
    }

}
