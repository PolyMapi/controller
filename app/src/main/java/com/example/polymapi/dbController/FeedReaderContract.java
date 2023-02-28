package com.example.polymapi.dbController;

import android.provider.BaseColumns;

public final class FeedReaderContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private FeedReaderContract() {}

    /* Inner class that defines the table contents for the "ImgRefs" table */
    public static class ImgRefsEntry implements BaseColumns {
        public static final String TABLE_NAME = "ImgRefs";
        public static final String COLUMN_NAME_CAPTURE_ID = "capture_id";
        public static final String COLUMN_NAME_REF = "ref";
    }

    /* Inner class that defines the table contents for the "Coordinates" table */
    public static class CoordinatesEntry implements BaseColumns {
        public static final String TABLE_NAME = "Coordinates";
        public static final String COLUMN_NAME_CAPTURE_ID = "capture_id";
        public static final String COLUMN_NAME_LATITUDE = "latitude";
        public static final String COLUMN_NAME_LONGITUDE = "longitude";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
    }
}

