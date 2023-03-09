package dbHandler;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DbHandler {

    public static void clearDb(FeedReaderDbHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.delete(FeedReaderContract.ImgRefsEntry.TABLE_NAME, null, null);
        db.delete(FeedReaderContract.CoordinatesEntry.TABLE_NAME, null, null);
        db.delete(FeedReaderContract.ImgPathEntry.TABLE_NAME, null, null);
    }

    public static void addImgRef(FeedReaderDbHelper dbHelper, int captureId, String imgRef) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(FeedReaderContract.ImgRefsEntry.COLUMN_NAME_CAPTURE_ID, captureId);
        values.put(FeedReaderContract.ImgRefsEntry.COLUMN_NAME_REF, imgRef);

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(FeedReaderContract.ImgRefsEntry.TABLE_NAME, null, values);
    }

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

    public static void addImgPath(FeedReaderDbHelper dbHelper, int captureId, String imgPath) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(FeedReaderContract.ImgPathEntry.COLUMN_NAME_CAPTURE_ID, captureId);
        values.put(FeedReaderContract.ImgPathEntry.COLUMN_NAME_PATH, imgPath);

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(FeedReaderContract.ImgPathEntry.TABLE_NAME, null, values);
    }

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
