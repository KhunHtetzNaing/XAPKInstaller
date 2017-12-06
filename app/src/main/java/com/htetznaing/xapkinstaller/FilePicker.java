package com.htetznaing.xapkinstaller;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.nononsenseapps.filepicker.AbstractFilePickerFragment;
import com.nononsenseapps.filepicker.FilePickerActivity;
import com.nononsenseapps.filepicker.FilePickerFragment;

import java.io.File;
import java.util.Arrays;

/**
 * Created by HtetzNaing on 11/25/2017.
 */

public class FilePicker extends FilePickerActivity {

    public FilePicker() {
        super();
    }

    @Override
    protected AbstractFilePickerFragment<File> getFragment(
            @Nullable final String startPath, final int mode, final boolean allowMultiple,
            final boolean allowCreateDir, final boolean allowExistingFile,
            final boolean singleClick) {
        AbstractFilePickerFragment<File> fragment = new CustomFilePickerFragment();
        // startPath is allowed to be null. In that case, default folder should be SD-card and not "/"
        fragment.setArgs(startPath != null ? startPath : Environment.getExternalStorageDirectory().getPath(),
                mode, allowMultiple, allowCreateDir, allowExistingFile, singleClick);
        return fragment;
    }



    public static class CustomFilePickerFragment extends FilePickerFragment {

        // File extension to filter on
        private static final String[] EXTENSIONS = new String[] {".xapk",".XAPK"};

        /**
         *
         * @param file
         * @return The file extension. If file has no extension, it returns null.
         */
        private String getExtension(@NonNull File file) {
            String path = file.getPath();
            int i = path.lastIndexOf(".");
            if (i < 0) {
                return null;
            } else {
                return path.substring(i);
            }
        }

        @Override
        protected boolean isItemVisible(final File file) {
            boolean ret = super.isItemVisible(file);
            if (ret && !isDir(file) && (mode == MODE_FILE || mode == MODE_FILE_AND_DIR)) {
                String ext = getExtension(file);
                return ext != null && Arrays.asList(EXTENSIONS).contains(ext);
            }
            return ret;
        }

    }
}