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

package net.i09158knct.android.development;

import java.util.List;

import android.app.LauncherActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.provider.Settings;

public class Development extends LauncherActivity
{
    @Override
    protected Intent getTargetIntent() {
        Intent targetIntent = new Intent(Intent.ACTION_MAIN, null);
        targetIntent.addCategory("android.intent.category.TEST2");
        return targetIntent;
    }

    protected void onSortResultList(List<ResolveInfo> results) {
//        super.onSortResultList(results);
        Intent settingsIntent = new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
        //In API 26, there are multiplte developer settings activities, choose the one with highest priority
        ResolveInfo topItem = getPackageManager().resolveActivity(settingsIntent, PackageManager.MATCH_DEFAULT_ONLY);
        if (topItem != null) {
            results.add(0, topItem);
        }
    }
}
