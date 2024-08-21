/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011-2023, FrostWire(R). All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.frostwire.gui.components.slides;

import com.frostwire.util.HttpClientFactory;
import com.frostwire.util.Logger;
import com.frostwire.util.http.HttpClient;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.settings.SharingSettings;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * @author gubatron
 * @author aldenml
 */
public class ImageCache {
    private static final Logger LOG = Logger.getLogger(ImageCache.class);
    private static ImageCache instance;

    private ImageCache() {
    }

    public synchronized static ImageCache instance() {
        if (instance == null) {
            instance = new ImageCache();
        }
        return instance;
    }

    public BufferedImage getImage(URL url, OnLoadedListener listener) {
        if (isCached(url)) {
            return loadFromCache(url, listener);
        } else if (!url.getProtocol().equals("http")) {
            return loadFromResource(url, listener);
        } else {
            loadFromUrl(url, listener);
            return null;
        }
    }

    private File getCacheFile(URL url) {
        String host = url.getHost();
        String path = url.getPath();
        if (host == null || host.isEmpty()) { // dealing with local resource images, not perfect
            host = "localhost";
            path = new File(path).getName();
        }
        return new File(SharingSettings.getImageCacheDirectory(), File.separator + host + File.separator + path);
    }

    private boolean isCached(URL url) {
        File file = getCacheFile(url);
        return file.exists();
    }

    private BufferedImage loadFromCache(URL url, OnLoadedListener listener) {
        try {
            File file = getCacheFile(url);
            BufferedImage image = ImageIO.read(file);
            listener.onLoaded(url, image, true, false);
            return image;
        } catch (Throwable e) {
            LOG.error("Failed to load image from cache: " + url, e);
            if (e instanceof OutOfMemoryError) {
                e.printStackTrace(); // this is a special condition
            }
            listener.onLoaded(url, null, false, true);
            return null;
        }
    }

    private BufferedImage loadFromResource(URL url, OnLoadedListener listener) {
        try {
            // Use JdkHttpClient to fetch the image data
            HttpClient newInstance = HttpClientFactory.getInstance(HttpClientFactory.HttpContext.MISC);
            byte[] data = newInstance.getBytes(url.toString());
            if (data == null) {
                throw new IOException("ImageCache.loadFromResource() got nothing at " + url.toString());
            }
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(data));
            saveToCache(url, image, 0);
            listener.onLoaded(url, image, false, false);
            return image;
        } catch (Throwable e) {
            LOG.error("Failed to load image from resource: " + url, e);
            listener.onLoaded(url, null, false, true);
            return null;
        }
    }

    private void loadFromUrl(final URL url, final OnLoadedListener listener) {
        GUIMediator.instance().uiThreadPool().execute(() -> {
            try {
                BufferedImage image;
                HttpClient newInstance = HttpClientFactory.getInstance(HttpClientFactory.HttpContext.MISC);
                byte[] data = newInstance.getBytes(url.toString());
                if (data == null) {
                    throw new IOException("ImageCache.loadUrl() got nothing at " + url.toString());
                }
                image = ImageIO.read(new ByteArrayInputStream(data));
                saveToCache(url, image, System.currentTimeMillis());
                if (listener != null && image != null) {
                    listener.onLoaded(url, image, false, false);
                }
            } catch (Throwable e) {
                LOG.error("Failed to load image from: " + url, e);
                listener.onLoaded(url, null, false, true);
            }
        });
    }

    private void saveToCache(URL url, BufferedImage image, long date) {
        try {
            File file = getCacheFile(url);
            if (file.exists()) {
                file.delete();
            }
            String filename = file.getName();
            int dotIndex = filename.lastIndexOf('.');
            String ext = filename.substring(dotIndex + 1);
            String formatName = ImageIO.getImageReadersBySuffix(ext).next().getFormatName();
            if (!file.getParentFile().exists()) {
                file.mkdirs();
            }
            ImageIO.write(image, formatName, file);
            file.setLastModified(date);
        } catch (Throwable e) {
            LOG.error("Failed to save image to cache: " + url, e);
        }
    }

    public interface OnLoadedListener {
        /**
         * This is called in the event that the image was downloaded and cached
         */
        void onLoaded(URL url, BufferedImage image, boolean fromCache, boolean fail);
    }
}
