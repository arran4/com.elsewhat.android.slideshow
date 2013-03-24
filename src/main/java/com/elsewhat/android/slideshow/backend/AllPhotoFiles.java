package com.elsewhat.android.slideshow.backend;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import com.elsewhat.android.slideshow.activities.SlideshowActivity;
import com.elsewhat.android.slideshow.api.FileUtils;
import com.elsewhat.android.slideshow.api.SlideshowPhoto;
import com.elsewhat.android.slideshow.api.SlideshowPhotoCached;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: arran
 * Date: 13/01/13
 * Time: 5:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class AllPhotoFiles {
    private List<String> paths;

    public AllPhotoFiles(List<String> paths) {
        this.paths = paths;
    }

    public List<SlideshowPhoto> getSlideshowPhotos(final Context context) throws Throwable {
        List<File> files = new ArrayList<File>();
        recurseFindFiles(files, paths);
        return new ArrayList<SlideshowPhoto>(CollectionUtils.collect(files, new Transformer() {
            @Override
            public Object transform(Object input) {
                SlideshowPhoto slideshowPhoto = new LocalSlideshowPhoto(context, (File) input);
                slideshowPhoto.setTitle("");
                slideshowPhoto.setDescription("");
                slideshowPhoto.setLargePhoto(((File) input).getAbsolutePath());
                return slideshowPhoto;
            }
        }));
    }

    private void recurseFindFiles(List<File> files, Collection<String> paths) {
        for (String path : paths)
        {
            File dir = new File(path);
            if (!dir.exists() || !dir.isDirectory())
                return;
            recurseFindFiles(files, CollectionUtils.collect(Arrays.asList(dir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.isDirectory() && !file.getName().matches("(.*\\.$|^\\..*)");
                }
            })), new Transformer() {
                @Override
                public Object transform(Object input) {
                    return ((File)input).getAbsolutePath();
                }
            }));
            files.addAll(Arrays.asList(dir.listFiles(new FileFilter() {
                Pattern pattern = Pattern.compile(".jpe?g$", Pattern.CASE_INSENSITIVE);
                @Override
                public boolean accept(File file) {
                    return pattern.matcher(file.getName()).find();
                }
            })));
        }
    }

    public List<String> getPaths() {
        return paths;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }

    public static class LocalSlideshowPhoto extends SlideshowPhotoCached
    {

        public LocalSlideshowPhoto(Context context, File file) {
            super(context, file);
        }

        @Override
        public Drawable getLargePhotoDrawable(File folder, int maxWidth, int maxHeight)throws IOException {
            long startTime = System.currentTimeMillis();
            Drawable retDrawable=  FileUtils.readPurgableBitmapFromFile(new File(getFileName()), maxWidth, maxHeight);
            long endTime = System.currentTimeMillis();
            Log.d(SlideshowActivity.LOG_PREFIX, "File IO used " + (endTime - startTime) + " ms");
            return retDrawable;
        }

    }
}