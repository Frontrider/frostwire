/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011-2016, FrostWire(R). All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.frostwire.android;

import android.app.Application;
import com.frostwire.android.core.ConfigurationManager;
import com.frostwire.android.core.Constants;
import com.frostwire.platform.SystemPaths;

import java.io.File;

/**
 * @author gubatron
 * @author aldenml
 */
final class AndroidPaths implements SystemPaths {

    private static final String STORAGE_PATH = "FrostWire";
    private static final String TORRENT_DATA_PATH = "TorrentsData";
    private static final String TORRENTS_PATH = "Torrents";


    private final Application app;

    public AndroidPaths(Application app) {
        this.app = app;
    }

    @Override
    public File data() {
        return new File(storage(), TORRENT_DATA_PATH);
    }

    @Override
    public File torrents() {
        return new File(storage(), TORRENTS_PATH);
    }

    @Override
    public File temp() {
        return null;
    }

    @Override
    public File libTorrent() {
        return null;
    }

    @Override
    public File update() {
        return null;
    }

    private static File storage() {
        String path = ConfigurationManager.instance().getString(Constants.PREF_KEY_STORAGE_PATH);
        return new File(path, STORAGE_PATH);
    }
}
