package com.dustinmreed.openwifi;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.dustinmreed.openwifi.data.WifiLocationContract;

import static com.dustinmreed.openwifi.Utilities.readFromPreferences;

public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String LOG_TAG = MainActivityFragment.class.getSimpleName();
    static final int COL_WIFILOCATION_NAME = 1;
    static final int COL_WIFILOCATION_TYPE = 2;

    private static final String SELECTED_KEY = "selected_position";
    private static final int WIFILOCATION_LOADER = 0;
    private static final String[] WIFILOCATION_COLUMNS = {
            WifiLocationContract.WiFiLocationEntry.TABLE_NAME + "." + WifiLocationContract.WiFiLocationEntry._ID,
            WifiLocationContract.WiFiLocationEntry.COLUMN_SITE_NAME,
            WifiLocationContract.WiFiLocationEntry.COLUMN_SITE_TYPE,
            WifiLocationContract.WiFiLocationEntry.COLUMN_STREET_ADDRESS,
            WifiLocationContract.WiFiLocationEntry.COLUMN_COORD_LAT,
            WifiLocationContract.WiFiLocationEntry.COLUMN_COORD_LONG
    };
    private static final String KEY_MAIN_LISTVIEW_FILTER = "main_listview_filter";
    private MainActivityAdapter mWiFiLocationAdapter;
    private ListView mListView;
    private int mPosition = ListView.INVALID_POSITION;
    private boolean mUseTodayLayout;

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            /*case R.id.action_map:
                Intent intent = new Intent(getActivity(), MapActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_library:
                saveToPreferences(getActivity(), KEY_MAIN_LISTVIEW_FILTER, "library");
                mWiFiLocationAdapter.notifyDataSetChanged();
                mListView.invalidateViews();
                getLoaderManager().restartLoader(0, null, this);
                mWiFiLocationAdapter.swapCursor(null);
                return true;
            case R.id.action_communitycenter:
                saveToPreferences(getActivity(), KEY_MAIN_LISTVIEW_FILTER, "communitycenter");
                mWiFiLocationAdapter.notifyDataSetChanged();
                mListView.invalidateViews();
                getLoaderManager().restartLoader(0, null, this);
                mWiFiLocationAdapter.swapCursor(null);
                return true;
            case R.id.action_publicgathering:
                saveToPreferences(getActivity(), KEY_MAIN_LISTVIEW_FILTER, "publicgathering");
                mWiFiLocationAdapter.notifyDataSetChanged();
                mListView.invalidateViews();
                getLoaderManager().restartLoader(0, null, this);
                mWiFiLocationAdapter.swapCursor(null);
                return true;
            case R.id.action_all:
                saveToPreferences(getActivity(), KEY_MAIN_LISTVIEW_FILTER, "all");
                mWiFiLocationAdapter.notifyDataSetChanged();
                mListView.invalidateViews();
                getLoaderManager().restartLoader(0, null, this);
                mWiFiLocationAdapter.swapCursor(null);
                return true;*/
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // The ForecastAdapter will take data from a source and
        // use it to populate the ListView it's attached to.
        mWiFiLocationAdapter = new MainActivityAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        mListView = (ListView) rootView.findViewById(R.id.listview_wifi_location);
        mListView.setAdapter(mWiFiLocationAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    ((Callback) getActivity())
                            .onItemSelected(WifiLocationContract.WiFiLocationEntry.buildWiFiLocationWithName(cursor.getString(COL_WIFILOCATION_NAME)
                            ));
                }
                mPosition = position;
            }
        });

        // If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things.  It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*.
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        mWiFiLocationAdapter.setUseTodayLayout(mUseTodayLayout);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(WIFILOCATION_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String mMainListviewFilter = readFromPreferences(getActivity(), KEY_MAIN_LISTVIEW_FILTER, "all");
        Uri wifiForLocationUri;

        switch (mMainListviewFilter) {
            case "all":
                wifiForLocationUri = WifiLocationContract.WiFiLocationEntry.buildWiFiLocation();
                break;
            case "library":
                wifiForLocationUri = WifiLocationContract.WiFiLocationEntry.buildWiFiLocationsWithType("type", "Library");
                break;
            case "communitycenter":
                wifiForLocationUri = WifiLocationContract.WiFiLocationEntry.buildWiFiLocationsWithType("type", "Regional Community Center");
                break;
            case "publicgathering":
                wifiForLocationUri = WifiLocationContract.WiFiLocationEntry.buildWiFiLocationsWithType("type", "Public Gathering");
                break;
            default:
                wifiForLocationUri = WifiLocationContract.WiFiLocationEntry.buildWiFiLocation();
                break;
        }

        return new CursorLoader(getActivity(),
                wifiForLocationUri,
                WIFILOCATION_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mWiFiLocationAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mListView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mWiFiLocationAdapter.swapCursor(null);
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
        if (mWiFiLocationAdapter != null) {
            mWiFiLocationAdapter.setUseTodayLayout(mUseTodayLayout);
        }
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        void onItemSelected(Uri locationUri);
    }
}