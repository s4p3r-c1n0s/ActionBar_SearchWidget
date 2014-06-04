package com.umang.actionbarsfl;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;

public class MainActivity extends ActionBarActivity {
	
	private ShareActionProvider mShareActionProvider;
   private Menu menuBar;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	    actionBar.setDisplayShowTitleEnabled(false);

	    Tab tab = actionBar.newTab()
	                       .setText(R.string.action_settings)
	                       .setTabListener(new TabListener<ArtistFragment>(this, "artist", ArtistFragment.class));
	    actionBar.addTab(tab);

	    tab = actionBar.newTab()
	                   .setText(R.string.hello_world)
	                   .setTabListener(new TabListener<AlbumFragment>(this, "album", AlbumFragment.class));
	    actionBar.addTab(tab);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
      menuBar = menu;
		
	    MenuItem shareItem = menu.findItem(R.id.action_compose);
	    mShareActionProvider = (ShareActionProvider)
	            MenuItemCompat.getActionProvider(shareItem);
	    mShareActionProvider.setShareIntent(getDefaultIntent());
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
	        case R.id.action_search:
               menuBar.setGroupVisible(R.id.group1, true);
	            Toast.makeText(MainActivity.this, "SEARCH", Toast.LENGTH_LONG).show();
	            return true;
	        case R.id.action_compose:
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
         case R.id.action_search : 
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
