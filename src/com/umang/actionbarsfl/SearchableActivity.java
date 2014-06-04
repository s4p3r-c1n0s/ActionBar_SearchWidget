package com.umang.actionbarsfl;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ListActivity;
import android.app.SearchManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;

public class SearchableActivity extends ListActivity {
	
   public void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);
              setContentView(R.layout.search_main);
                  handleIntent(getIntent());
   }

   @Override
   protected void onNewIntent(Intent intent) {
       setIntent(intent);
       handleIntent(intent);
   }
   
   private void handleIntent(Intent intent) {
      Log.d("UmangX","SPARTA");
       if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
             String query = intent.getStringExtra(SearchManager.QUERY);
         Toast.makeText(SearchableActivity.this, query, Toast.LENGTH_LONG).show();
           }
   }

}
