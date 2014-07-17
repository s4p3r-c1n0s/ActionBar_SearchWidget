
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
    public class AppListAdapter extends ArrayAdapter<AppEntry> {
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
               for(AppEntry ae : data){
               add(ae);
               }
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

            TextView  useless = 
               (TextView)view.findViewById(R.id.useless);
            useless.setVisibility(View.VISIBLE);

            AppEntry item = getItem(position);
            Log.d("UmangXAA","getting AppEntry Icon");
            ((ImageView)view.findViewById(R.id.icon)).setImageDrawable(item.getIcon());
            Log.d("UmangXAA","getting AppEntry label");
            ((TextView)view.findViewById(R.id.text)).setText(item.getLabel());

            return view;
        }
    }
