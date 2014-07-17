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
import android.support.v7.view.ActionMode.Callback;
import android.support.v7.view.ActionMode;
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

import com.umang.actionbarsfl.AppListFragment;

public class MainActivity extends ActionBarActivity implements OnQueryTextListener, OnCloseListener {

   private static SearchView searchView;
   private static String mCurFilter;
   private AppListFragment list;
   private static AppListAdapter mAdapter;
   private static String mCuriFilter;
	private ShareActionProvider mShareActionProvider;
   private Menu menuBar;


   @Override public boolean onQueryTextChange(String newText) {
      mCurFilter = !TextUtils.isEmpty(newText) ? newText : null;
      ((AppListAdapter)list.getListAdapter()).getFilter().filter(mCurFilter);
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
   public boolean onClose() {
      if (!TextUtils.isEmpty(searchView.getQuery())) {
          searchView.setQuery(null, true);
      }
      return true;
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
         list = new AppListFragment();
         fm.beginTransaction().add(android.R.id.content, list).commit();
      }
	}

   @Override
   protected void onResume(){
      super.onResume();
         mAdapter = (AppListAdapter)list.getListAdapter();
         if(mAdapter == null) Log.d("FUCK","this is wrong, its null");
         else Log.d("FUCK","this is right, its not null");
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
       searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
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

   private void showXTextView(){
   if(list.getListAdapter() == null) Log.d("BHENCHOD","CHUTIYA");
      ((AppListAdapter)list.getListAdapter()).notifyDataSetChanged();
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
            startSupportActionMode(mActionModeCallback);
            showXTextView();
            Toast.makeText(MainActivity.this, "STAR", Toast.LENGTH_LONG).show();
            break;
         default : 
            Toast.makeText(MainActivity.this, "DEFAULT", Toast.LENGTH_LONG).show();
            break;
      }
   }

   private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
      @Override 
      public boolean onCreateActionMode(ActionMode mode, Menu menu){
         MenuInflater inflater = mode.getMenuInflater();
         inflater.inflate(R.menu.context_menu,menu);
         MenuItem searchItem = menu.findItem(R.id.menu_search);
          searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
          searchView.setOnQueryTextListener(MainActivity.this);
         searchView.setOnCloseListener(MainActivity.this);
         return true;
      }

      @Override
      public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
         return false; // Return false if nothing is done
      }

      @Override
      public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
         switch (item.getItemId()) {
            case R.id.menu_search:
               mode.finish(); // Action picked, so close the CAB
               return true;
            default:
               return false;
         }
      }

      public void onDestroyActionMode(ActionMode mode) {
         if (!TextUtils.isEmpty(searchView.getQuery())) {
             searchView.setQuery(null, true);
         }
      }

   };

}
