package com.umang.actionbarsfl;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.app.SearchableInfo;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.Loader;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnCloseListener;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.support.v7.widget.ShareActionProvider;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends ActionBarActivity implements OnQueryTextListener {
	
	private ShareActionProvider mShareActionProvider;
   private Menu menuBar;

    public static class AppEntry {

        private final AppListLoader mLoader;
        private final ApplicationInfo mInfo;
        private final File mApkFile;
        private String mLabel;
        private Drawable mIcon;
        private boolean mMounted;

        public AppEntry(AppListLoader loader, ApplicationInfo info) {
           Log.d("UmangXAE","Constructing the AppEntry");
            mLoader = loader;
            mInfo = info;
            mApkFile = new File(info.sourceDir);
        }

        public ApplicationInfo getApplicationInfo() {
            Log.d("UmangXAE","get mInfo");
            return mInfo;
        }

        public String getLabel() {
            Log.d("UmangXAE","get label");
            return mLabel;
        }

        public Drawable getIcon() {
            Log.d("UmangXAE","icon getter");
            if (mIcon == null) {
               Log.d("UmangXAE","icon null");
                if (mApkFile.exists()) {
                    mIcon = mInfo.loadIcon(mLoader.mPm);
                    return mIcon;
                } else {
                    mMounted = false;
                }
            } else if (!mMounted) {
               Log.d("UmangXAE","app newly mounted");
                // If the app wasn't mounted but is now mounted, reload
                // its icon.
                if (mApkFile.exists()) {
                    mMounted = true;
                    mIcon = mInfo.loadIcon(mLoader.mPm);
                    return mIcon;
                }
            } else {
               Log.d("UmangXAE","icon not null");
                return mIcon;
            }

            return mLoader.getContext().getResources().getDrawable(
                    android.R.drawable.sym_def_app_icon);
        }

        @Override public String toString() {
            Log.d("UmangXAE","Label to String");
            return mLabel;
        }

        void loadLabel(Context context) {
            Log.d("UmangXAE","Loading Label");
            if (mLabel == null || !mMounted) {
                if (!mApkFile.exists()) {
                  Log.d("UmangXAE","apk file not existent so mLabel is : " + mInfo.packageName);
                    mMounted = false;
                    mLabel = mInfo.packageName;
                } else {
                  Log.d("UmangXAE","Loading Label");
                    mMounted = true;
                    CharSequence label = mInfo.loadLabel(context.getPackageManager());
                  Log.d("UmangXAE","apk file is existent so mLabel is : " + label);
                    mLabel = label != null ? label.toString() : mInfo.packageName;
                }
            }
        }
    }

    public static final Comparator<AppEntry> ALPHA_COMPARATOR = new Comparator<AppEntry>() {
        private final Collator sCollator = Collator.getInstance();
        @Override
        public int compare(AppEntry object1, AppEntry object2) {
            return sCollator.compare(object1.getLabel(), object2.getLabel());
        }
    };

    public static class InterestingConfigChanges {
        final Configuration mLastConfiguration = new Configuration();
        int mLastDensity;

        boolean applyNewConfig(Resources res) {
            int configChanges = mLastConfiguration.updateFrom(res.getConfiguration());
            boolean densityChanged = mLastDensity != res.getDisplayMetrics().densityDpi;
            if (densityChanged || (configChanges&(ActivityInfo.CONFIG_LOCALE
                    |ActivityInfo.CONFIG_UI_MODE|ActivityInfo.CONFIG_SCREEN_LAYOUT)) != 0) {
                mLastDensity = res.getDisplayMetrics().densityDpi;
                return true;
            }
            return false;
        }
    }

    public static class PackageIntentReceiver extends BroadcastReceiver {
        final AppListLoader mLoader;

        public PackageIntentReceiver(AppListLoader loader) {
            mLoader = loader;
            IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
            filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
            filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
            filter.addDataScheme("package");
            mLoader.getContext().registerReceiver(this, filter);
            // Register for events related to sdcard installation.
            IntentFilter sdFilter = new IntentFilter();
            sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
            sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
            mLoader.getContext().registerReceiver(this, sdFilter);
        }

        @Override public void onReceive(Context context, Intent intent) {
            // Tell the loader about the change.
            mLoader.onContentChanged();
        }
    }

    public static class AppListLoader extends AsyncTaskLoader<List<AppEntry>> {
        final InterestingConfigChanges mLastConfig = new InterestingConfigChanges();
        final PackageManager mPm;

        List<AppEntry> mApps;
        PackageIntentReceiver mPackageObserver;

        public AppListLoader(Context context) {
            super(context);
            Log.d("UmangX","loader constructor");

            // Retrieve the package manager for later use; note we don't
            // use 'context' directly but instead the save global application
            // context returned by getContext().
            mPm = getContext().getPackageManager();
        }

        /**
         * This is where the bulk of our work is done.  This function is
         * called in a background thread and should generate a new set of
         * data to be published by the loader.
         */
        @Override public List<AppEntry> loadInBackground() {
            // Retrieve all known applications.
            Log.d("UmangX","Loading apps in background");
            List<ApplicationInfo> apps = mPm.getInstalledApplications(
                    PackageManager.GET_UNINSTALLED_PACKAGES |
                    PackageManager.GET_DISABLED_COMPONENTS);
            Log.d("UmangX","packlage manager returns app installed apps");
            if (apps == null) {
               Log.d("UmangX","but apps are null so creating new object");
                apps = new ArrayList<ApplicationInfo>();
            }

            final Context context = getContext();

            // Create corresponding array of entries and load their labels.
            List<AppEntry> entries = new ArrayList<AppEntry>(apps.size());
            Log.d("UmangX","new list entries created with size : " + apps.size());
            for (int i=0; i<apps.size(); i++) {
                AppEntry entry = new AppEntry(this, apps.get(i));
               Log.d("UmangX","loadingLabel and adding a new entry to the entries list : "  + entry.toString());
                entry.loadLabel(context);
                entries.add(entry);
            }

            // Sort the list.
            Collections.sort(entries, ALPHA_COMPARATOR);

            // Done!
            return entries;
        }

        /**
         * Called when there is new data to deliver to the client.  The
         * super class will take care of delivering it; the implementation
         * here just adds a little more logic.
         */
        @Override public void deliverResult(List<AppEntry> apps) {
            Log.d("UmangX","deliver Result");
            if (isReset()) {
               Log.d("UmangX","is Reset and apps loader stopped and apps list released");
                // An async query came in while the loader is stopped.  We
                // don't need the result.
                if (apps != null) {
               Log.d("UmangX","apps not null and isreset so releasing resources");
                    onReleaseResources(apps);
                }
            }
            Log.d("UmangX","getting new result saving old result somewhere else");
            List<AppEntry> oldApps = mApps;
            mApps = apps;

            if (isStarted()) {
               Log.d("UmangX","if isStarted calling supper");
                // If the Loader is currently started, we can immediately
                // deliver its results.
                super.deliverResult(apps);
            }

            // At this point we can release the resources associated with
            // 'oldApps' if needed; now that the new result is delivered we
            // know that it is no longer in use.
            if (oldApps != null) {
               Log.d("UmangX","releasing old results");
                onReleaseResources(oldApps);
            }
        }

        /**
         * Handles a request to start the Loader.
         */
        @Override protected void onStartLoading() {
            Log.d("UmangX","onStart Loading");
            if (mApps != null) {
               Log.d("UmangX","mapps non null, delivering result");
                // If we currently have a result available, deliver it
                // immediately.
                deliverResult(mApps);
            }

            // Start watching for changes in the app data.
            if (mPackageObserver == null) {
               Log.d("UmangX","new BR object");
                mPackageObserver = new PackageIntentReceiver(this);
            }

            // Has something interesting in the configuration changed since we
            // last built the app list?
            boolean configChange = mLastConfig.applyNewConfig(getContext().getResources());

            if (takeContentChanged() || mApps == null || configChange) {
               Log.d("UmangX","forceloading");
                // If the data has changed since the last time it was loaded
                // or is not currently available, start a load.
                forceLoad();
            }
        }

        /**
         * Handles a request to stop the Loader.
         */
        @Override protected void onStopLoading() {
            Log.d("UmangX","onStopLoad, cancel loading");
            // Attempt to cancel the current load task if possible.
            cancelLoad();
        }

        /**
         * Handles a request to cancel a load.
         */
        @Override public void onCanceled(List<AppEntry> apps) {
            super.onCanceled(apps);
            Log.d("UmangX","releasing resources");

            // At this point we can release the resources associated with 'apps'
            // if needed.
            onReleaseResources(apps);
        }

        /**
         * Handles a request to completely reset the Loader.
         */
        @Override protected void onReset() {
            Log.d("UmangX","onreset called");
            super.onReset();

            // Ensure the loader is stopped
            Log.d("UmangX","specially calling onStop :( ");
            onStopLoading();

            // At this point we can release the resources associated with 'apps'
            // if needed.
            if (mApps != null) {
               Log.d("UmangX","making mApps nulli and releasing resources");
                onReleaseResources(mApps);
                mApps = null;
            }

            // Stop monitoring for changes.
            if (mPackageObserver != null) {
               Log.d("UmangX","unregistering BR");
                getContext().unregisterReceiver(mPackageObserver);
                mPackageObserver = null;
            }
        }

        /**
         * Helper function to take care of releasing resources associated
         * with an actively loaded data set.
         */
        protected void onReleaseResources(List<AppEntry> apps) {
            Log.d("UmangX","onReleasesResources called");
            // For a simple List<> there is nothing to do.  For something
            // like a Cursor, we would close it here.
        }
    }


    public static class AppListAdapter extends ArrayAdapter<AppEntry> {
        private final LayoutInflater mInflater;

        public AppListAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_2);
            Log.d("UmangXAA","constructor AppListAdapter");
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void setData(List<AppEntry> data) {
            Log.d("UmangXAA","setData");
            clear();
            if (data != null) {
               Log.d("UmangXAA","data not null, adding data");
                addAll(data);
            }
        }

        /**
         * Populate new items in the list.
         */
        @Override public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            Log.d("UmangXAA","get View");

            if (convertView == null) {
               Log.d("UmangXAA","convertView null, inflating");
                view = mInflater.inflate(R.layout.list_item_icon_text, parent, false);
            } else {
               Log.d("UmangXAA","converViewn not null");
                view = convertView;
            }

            AppEntry item = getItem(position);
            Log.d("UmangXAA","getting AppEntry Icon");
            ((ImageView)view.findViewById(R.id.icon)).setImageDrawable(item.getIcon());
            Log.d("UmangXAA","getting AppEntry label");
            ((TextView)view.findViewById(R.id.text)).setText(item.getLabel());

            return view;
        }
    }

    public static class AppListFragment extends ListFragment
            implements LoaderManager.LoaderCallbacks<List<AppEntry>> {

        // This is the Adapter being used to display the list's data.
        AppListAdapter mAdapter;

        // The SearchView for doing filtering.
        // SearchView mSearchView;

        // If non-null, this is the current filter the user has provided.
        String mCurFilter;

        @Override public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            // Give some text to display if there is no data.  In a real
            // application this would come from a resource.
            setEmptyText("No applications");

            // We have a menu item to show in action bar.
            setHasOptionsMenu(true);

            // Create an empty adapter we will use to display the loaded data.
            mAdapter = new AppListAdapter(getActivity());
            setListAdapter(mAdapter);

            // Start out with a progress indicator.
            setListShown(false);

            // Prepare the loader.  Either re-connect with an existing one,
            // or start a new one.
            getLoaderManager().initLoader(0, null, this);
        }

        @Override public Loader<List<AppEntry>> onCreateLoader(int id, Bundle args) {
            // This is called when a new Loader needs to be created.  This
            // sample only has one Loader with no arguments, so it is simple.
            return new AppListLoader(getActivity());
        }

        @Override public void onLoadFinished(Loader<List<AppEntry>> loader, List<AppEntry> data) {
            // Set the new data in the adapter.
            mAdapter.setData(data);

            // The list should now be shown.
            if (isResumed()) {
                setListShown(true);
            } else {
                setListShownNoAnimation(true);
            }
        }

        @Override public void onLoaderReset(Loader<List<AppEntry>> loader) {
            // Clear the data in the adapter.
            mAdapter.setData(null);
        }
    }

   @Override public boolean onQueryTextChange(String newText) {
      // Called when the action bar search text has changed.  Since this
      // is a simple array adapter, we can just have it do the filtering.
      Toast.makeText(MainActivity.this, newText, Toast.LENGTH_SHORT).show();
      //mCurFilter = !TextUtils.isEmpty(newText) ? newText : null;
      //mAdapter.getFilter().filter(mCurFilter);
      return true;
   }

   @Override public boolean onQueryTextSubmit(String query) {
      Toast.makeText(MainActivity.this, query, Toast.LENGTH_SHORT).show();
      return true;
   }
   @Override
   public boolean onSearchRequested() {
      Toast.makeText(MainActivity.this, "REQUEST_SEARCH", Toast.LENGTH_SHORT).show();
      return super.onSearchRequested();
   }


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//setContentView(R.layout.activity_main);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);
		//actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

      Log.d("UmangX","onCreate");

      FragmentManager fm = getSupportFragmentManager();

      // Create the list fragment and add it as our sole content.
      if (fm.findFragmentById(android.R.id.content) == null) {
        Log.d("UmangX","frag manager's id.content is null, adding applistfragement");
         AppListFragment list = new AppListFragment();
         fm.beginTransaction().add(android.R.id.content, list).commit();
      }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
      menuBar = menu;
		
      MenuItem shareItem = menu.findItem(R.id.action_compose);
	    mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
	    mShareActionProvider.setShareIntent(getDefaultIntent());

       SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
       Log.d("UmangX","manager : "+ (searchManager != null));
       MenuItem searchItem = menu.findItem(R.id.action_search);
       SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
      searchView.setOnQueryTextListener(this);
       Log.d("UmangX","view : "+ (searchView != null));
       ComponentName comp = new ComponentName("com.umang.actionbarsfl","com.umang.actionbarsfl.SearchableActivity");
       Log.d("UmangX","comp : "+ comp);
       SearchableInfo searchableInfo = searchManager.getSearchableInfo(comp);
       Log.d("UmangX","info : "+ searchableInfo);
       searchView.setSearchableInfo(searchableInfo);
       searchView.setIconifiedByDefault(true);
		return true;
	}
	
	private Intent getDefaultIntent() {
	    Intent intent = new Intent(Intent.ACTION_SEND);
	    intent.setType("image/*");
	    return intent;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.action_compose:
               menuBar.setGroupVisible(R.id.group1, true);
	            Toast.makeText(MainActivity.this, "SEARCH", Toast.LENGTH_LONG).show();
	            return true;
	        case R.id.action_search:
               menuBar.setGroupVisible(R.id.group1, false);
               Toast.makeText(MainActivity.this, "COMPOSE", Toast.LENGTH_LONG).show();
	            return true;
	        case R.id.group1:
               Toast.makeText(MainActivity.this, "GROUP", Toast.LENGTH_LONG).show();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}


   public void onGroupItemClick(MenuItem item){
      Toast.makeText(MainActivity.this, "ITEM", Toast.LENGTH_LONG).show();
      switch (item.getItemId()) {
         case R.id.action_compose : 
            Toast.makeText(MainActivity.this, "SEARCH", Toast.LENGTH_LONG).show();
            menuBar.setGroupVisible(item.getGroupId(), false);
            menuBar.setGroupVisible(R.id.group2, true);
            break;
         case R.id.star :
            menuBar.setGroupVisible(item.getGroupId(), false);
            menuBar.setGroupVisible(R.id.group1, true);
            Toast.makeText(MainActivity.this, "STAR", Toast.LENGTH_LONG).show();
            break;
         default : 
            Toast.makeText(MainActivity.this, "DEFAULT", Toast.LENGTH_LONG).show();
            break;
      }
   }
	
	
	public static class TabListener<T extends Fragment> implements ActionBar.TabListener {
	    private Fragment mFragment;
	    private final Activity mActivity;
	    private final String mTag;
	    private final Class<T> mClass;

	    /** Constructor used each time a new tab is created.
	      * @param activity  The host Activity, used to instantiate the fragment
	      * @param tag  The identifier tag for the fragment
	      * @param clz  The fragment's Class, used to instantiate the fragment
	      */
	    public TabListener(Activity activity, String tag, Class<T> clz) {
	        mActivity = activity;
	        mTag = tag;
	        mClass = clz;
	    }

	    /* The following are each of the ActionBar.TabListener callbacks */

		public void onTabSelected(Tab tab, FragmentTransaction ft) {
	        // Check if the fragment is already initialized
	        if (mFragment == null) {
	            // If not, instantiate and add it to the activity
	            mFragment = Fragment.instantiate(mActivity, mClass.getName());
	            ft.add(android.R.id.content, mFragment, mTag);
	        } else {
	            // If it exists, simply attach it in order to show it
	            ft.attach(mFragment);
	        }
	    }

		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	        if (mFragment != null) {
	            // Detach the fragment, because another one is being attached
	            ft.detach(mFragment);
	        }
	    }

	    public void onTabReselected(Tab tab, FragmentTransaction ft) {
	        // User selected the already selected tab. Usually do nothing.
	    }

	}
	
	public static class ArtistFragment extends Fragment {
	}
	
	public static class AlbumFragment extends Fragment {
		
	}
}
