package com.a4455jkjh.apktool.fragment.files;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.webkit.MimeTypeMap;

import com.a4455jkjh.apktool.R;
import com.a4455jkjh.apktool.fragment.FilesFragment;
import com.a4455jkjh.apktool.lexer.LexerUtil;
import com.a4455jkjh.apktool.util.FileUtils;
import com.a4455jkjh.apktool.util.Settings;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class FileItem implements Item {

    private final File file;

    // Cache icon APK agar tidak load ulang (penting untuk performance)
    private static final Map<String, Drawable> APK_ICON_CACHE = new HashMap<>();

    public FileItem(File file) {
        this.file = file;
    }

    @Override
    public void setup(ImageView icon, TextView name) {
        String n = file.getName();
        name.setText(n);
        n = n.toLowerCase();

        if (file.isDirectory()) {
            Icon.FOLDER.set(icon);
        } else if (n.endsWith(".xml")) {
            Icon.XML.set(icon);
        } else if (n.endsWith(".yml")) {
            Icon.YML.set(icon);
        } else if (n.endsWith(".smali")) {
            Icon.SMALI.set(icon);
        } else if (isJKS(n)) {
            Icon.JKS.set(icon);
        } else if (n.endsWith(".apk")) {
            setRealApkIcon(icon);
        } else {
            Icon.UNKNOWN.set(icon);
        }
    }

    /**
     * Load icon APK asli dari file .apk
     */
    private void setRealApkIcon(ImageView icon) {
        String path = file.getAbsolutePath();

        Drawable cached = APK_ICON_CACHE.get(path);
        if (cached != null) {
            icon.setImageDrawable(cached);
            return;
        }

        try {
            PackageManager pm = icon.getContext().getPackageManager();
            PackageInfo info = pm.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);

            if (info != null && info.applicationInfo != null) {
                info.applicationInfo.sourceDir = path;
                info.applicationInfo.publicSourceDir = path;

                Drawable d = info.applicationInfo.loadIcon(pm);
                if (d != null) {
                    APK_ICON_CACHE.put(path, d);
                    icon.setImageDrawable(d);
                    return;
                }
            }
        } catch (Throwable ignored) {
        }

        // Fallback jika APK rusak / gagal load
        Icon.APK.set(icon);
    }

    @Override
    public boolean edit(FilesFragment frag) {
        if (file.isFile()) {
            String name = file.getName();
            int p = name.lastIndexOf('.');
            if (p >= 0) {
                String type = name.substring(p + 1);
                String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(type);
                if ((mime != null && mime.startsWith("text/")) || LexerUtil.isText(type)) {
                    frag.edit(file);
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isJKS(String n) {
        return n.endsWith(".jks") || n.endsWith(".keystore");
    }

    public static boolean isKey(String n) {
        return n.toLowerCase().matches(".*\\.(jks|keystore|p12|pk8|x509|pem)");
    }

    @Override
    public boolean click(View view, Refreshable refresh) {
        if (file.isDirectory())
            return false;
        FileUtils.open(file, view, refresh);
        return true;
    }

    @Override
    public void process(FilesAdapter adapter) {
        adapter.refresh(file);
    }

    @Override
    public boolean longClick(View view, Refreshable refresh) {
        FileUtils.file(file, view, refresh);
        return true;
    }

    @Override
    public int compareTo(Item p1) {
        if (!(p1 instanceof FileItem))
            return 1;
        File f1 = file;
        File f2 = ((FileItem) p1).file;
        if (f1.isDirectory() && f2.isFile())
            return -1;
        if (f2.isDirectory() && f1.isFile())
            return 1;
        return FileComparator.getDefaultAdapter().compare(f1, f2);
    }

    @Override
    public int getProperty() {
        return PROPERTY_FILE;
    }

    public enum Icon {
        FOLDER(R.drawable.folder, R.drawable.folder_dark),
        XML(R.drawable.xml, R.drawable.xml_dark),
        YML(R.drawable.yaml, R.drawable.yaml_dark),
        SMALI(R.drawable.smali, R.drawable.smali_dark),
        JKS(R.drawable.jks, R.drawable.jks_dark),
        APK(R.drawable.apk, R.drawable.apk_dark),
        UNKNOWN(R.drawable.file, R.drawable.file_dark);

        int light;
        int dark;

        Icon(int light, int dark) {
            this.light = light;
            this.dark = dark;
        }

        public void set(ImageView image) {
            if (Settings.lightTheme)
                image.setImageResource(light);
            else
                image.setImageResource(dark);
        }
    }
}
