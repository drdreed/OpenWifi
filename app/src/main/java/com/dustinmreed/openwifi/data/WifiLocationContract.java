/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dustinmreed.openwifi.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class WifiLocationContract {

    public static final String CONTENT_AUTHORITY = "com.dustinmreed.openwifi";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_WIFILOCATION = "wifilocation";

    public static final class WiFiLocationEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_WIFILOCATION).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_WIFILOCATION;
        public static final String TABLE_NAME = "wifilocation";
        public static final String COLUMN_SITE_NAME = "site_name";
        public static final String COLUMN_SITE_TYPE = "site_type";
        public static final String COLUMN_STREET_ADDRESS = "street_address";
        public static final String COLUMN_COORD_LAT = "coord_lat";
        public static final String COLUMN_COORD_LONG = "coord_long";
        public static final String COLUMN_ADDRESS = "address";
        public static final String COLUMN_CITY = "city";
        public static final String COLUMN_STATE = "state";
        public static final String COLUMN_ZIPCODE = "zipcode";

        public static Uri buildWiFiLocationUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildWiFiLocation() {
            return CONTENT_URI.buildUpon().build();
        }

        public static Uri buildWiFiLocationWithName(String nameSetting) {
            return CONTENT_URI.buildUpon().appendPath(nameSetting).build();
        }

        public static Uri buildWiFiLocationsWithType(String type, String location) {
            return CONTENT_URI.buildUpon().appendPath(type).appendPath(location).build();
        }

        public static String getNameSettingFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String getFilterFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String getTypeFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }
    }
}
