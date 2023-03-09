package dbHandler;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FeedReaderDbHelper extends SQLiteOpenHelper {

    private static final String SQL_CREATE_IMGREFS_TABLE =
            "CREATE TABLE " + FeedReaderContract.ImgRefsEntry.TABLE_NAME + " (" +
                    FeedReaderContract.ImgRefsEntry._ID + " INTEGER PRIMARY KEY," +
                    FeedReaderContract.ImgRefsEntry.COLUMN_NAME_CAPTURE_ID + " INTEGER," +
                    FeedReaderContract.ImgRefsEntry.COLUMN_NAME_REF + " TEXT)";

    private static final String SQL_DELETE_IMGREFS_TABLE =
            "DROP TABLE IF EXISTS " + FeedReaderContract.ImgRefsEntry.TABLE_NAME;

    private static final String SQL_CREATE_COORDINATES_TABLE =
            "CREATE TABLE " + FeedReaderContract.CoordinatesEntry.TABLE_NAME + " (" +
                    FeedReaderContract.CoordinatesEntry._ID + " INTEGER PRIMARY KEY," +
                    FeedReaderContract.CoordinatesEntry.COLUMN_NAME_CAPTURE_ID + " INTEGER," +
                    FeedReaderContract.CoordinatesEntry.COLUMN_NAME_LATITUDE + " REAL," +
                    FeedReaderContract.CoordinatesEntry.COLUMN_NAME_LONGITUDE + " REAL," +
                    FeedReaderContract.CoordinatesEntry.COLUMN_NAME_TIMESTAMP + " TEXT)";

    private static final String SQL_DELETE_COORDINATES_TABLE =
            "DROP TABLE IF EXISTS " + FeedReaderContract.CoordinatesEntry.TABLE_NAME;

    private static final String SQL_CREATE_IMGPATH_TABLE =
            "CREATE TABLE " + FeedReaderContract.ImgPathEntry.TABLE_NAME + " (" +
                    FeedReaderContract.ImgPathEntry._ID + " INTEGER PRIMARY KEY," +
                    FeedReaderContract.ImgPathEntry.COLUMN_NAME_CAPTURE_ID + " INTEGER," +
                    FeedReaderContract.ImgPathEntry.COLUMN_NAME_PATH + " TEXT)";

    private static final String SQL_DELETE_IMGPATH_TABLE =
            "DROP TABLE IF EXISTS " + FeedReaderContract.ImgPathEntry.TABLE_NAME;


    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "FeedReader.db";


    public FeedReaderDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_IMGREFS_TABLE);
        db.execSQL(SQL_CREATE_COORDINATES_TABLE);
        db.execSQL(SQL_CREATE_IMGPATH_TABLE);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_IMGREFS_TABLE);
        db.execSQL(SQL_DELETE_COORDINATES_TABLE);
        db.execSQL(SQL_DELETE_IMGPATH_TABLE);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
