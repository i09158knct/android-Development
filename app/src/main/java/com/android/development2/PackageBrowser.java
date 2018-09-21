/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.android.development2;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PackageBrowser extends ListActivity {
    static class MyPackageInfo {
        PackageInfo info;
        String label;
        boolean enabled;
        boolean isSystemApp;

        public int getWeight() {
            return (enabled ? 10 : 0) + (isSystemApp ? 0 : 1);
        }
    }
    
    private PackageListAdapter mAdapter;
    private List<MyPackageInfo> mPackageInfoList = new ArrayList<MyPackageInfo>();
    private Handler mHandler;
    private BroadcastReceiver mRegisteredReceiver;

    public class PackageListAdapter extends ArrayAdapter<MyPackageInfo> {

        public PackageListAdapter(Context context) {
            super(context, R.layout.package_list_item);
            List<PackageInfo> pkgs = context.getPackageManager().getInstalledPackages(0);
            for (int i=0; i<pkgs.size(); i++) {
                MyPackageInfo info = new MyPackageInfo();
                info.info = pkgs.get(i);
                info.label = info.info.applicationInfo.loadLabel(getPackageManager()).toString();
                info.enabled = info.info.applicationInfo.enabled;
                info.isSystemApp = (info.info.applicationInfo.flags& ApplicationInfo.FLAG_SYSTEM) != 0;
                mPackageInfoList.add(info);
            }
            if (mPackageInfoList != null) {
                Collections.sort(mPackageInfoList, sDisplayNameComparator);
            }
            setSource(mPackageInfoList);
        }
    
        @Override
        public void bindView(View view, MyPackageInfo info) {
            ImageView icon = (ImageView)view.findViewById(R.id.icon);
            TextView name = (TextView)view.findViewById(R.id.name);
            TextView description = (TextView)view.findViewById(R.id.description);
            icon.setImageDrawable(info.info.applicationInfo.loadIcon(getPackageManager()));
            name.setText((info.isSystemApp ? "*" : "") +  info.label);

            if (!info.enabled) {
                view.setBackgroundColor(0x88888888);
            }
            else {
                view.setBackgroundColor(Color.TRANSPARENT);
            }

            description.setText(info.info.packageName);
        }
    }

    /**
     * Receives notifications when applications are added/removed.
     */
    private class ApplicationsIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // todo: this is a bit brute force.  We should probably get the action and package name
            //       from the intent and just add to or delete from the mPackageInfoList
            setupAdapter();
        }
    }

    private final static Comparator<MyPackageInfo> sDisplayNameComparator
            = new Comparator<MyPackageInfo>() {
        public final int
        compare(MyPackageInfo a, MyPackageInfo b) {
            if (a.enabled != b.enabled || a.isSystemApp != b.isSystemApp) {
                return a.getWeight() - b.getWeight();
            }
            return collator.compare(a.info.packageName, b.info.packageName);
        }

        private final Collator   collator = Collator.getInstance();
    };

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setupAdapter();
        mHandler= new Handler();
        registerIntentReceivers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRegisteredReceiver != null) {
            unregisterReceiver(mRegisteredReceiver);
        }
    }

    private void setupAdapter() {
        mAdapter = new PackageListAdapter(this);
        setListAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        menu.add(0, 0, 0, "Delete package").setOnMenuItemClickListener(
//                new MenuItem.OnMenuItemClickListener() {
//            public boolean onMenuItemClick(MenuItem item) {
//                deletePackage();
//                return true;
//            }
//        });
        return true;
    }

    private void deletePackage() {

    }

    private void registerIntentReceivers() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme("package");
        mRegisteredReceiver = new ApplicationsIntentReceiver();
        registerReceiver(mRegisteredReceiver, filter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        MyPackageInfo info =
            mAdapter.itemForPosition(position);
        if (info != null) {
            Intent intent = new Intent(
                null, Uri.fromParts("package", info.info.packageName, null));
            intent.setClass(this, PackageSummary.class);
            startActivity(intent);
        }
    }
}
