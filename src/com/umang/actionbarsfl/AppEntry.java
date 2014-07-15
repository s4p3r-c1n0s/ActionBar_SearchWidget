

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
    public class AppEntry {

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
