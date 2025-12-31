package com.a4455jkjh.apktool.fragment.files;

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.ImageView;

import com.a4455jkjh.apktool.R;
import com.a4455jkjh.apktool.task.DecodeTask;
import com.a4455jkjh.apktool.task.ImportFrameworkTask;
import com.a4455jkjh.apktool.util.PopupUtils;

import java.io.File;

public class ApplicationItem extends ErrorTree {
    private final boolean isSystem;

    public ApplicationItem(CharSequence msg, boolean isSystem) {
        super(msg);
        this.isSystem = isSystem;
    }

    @Override
    public void setIcon(ImageView icon) {
        FileItem.Icon.APK.set(icon);
    }

    public void addApp(final PackageInfo pkg, final PackageManager pm) {
        addChild(new ErrorTree(String.format("%s_%s", pkg.applicationInfo.loadLabel(pm), pkg.versionName)) {
            @Override
            public void setIcon(ImageView icon) {
                icon.setImageDrawable(pkg.applicationInfo.loadIcon(pm));
            }

            @Override
            public void click(View view) {
                PopupUtils.show(view, R.menu.app,
                        new PopupUtils.Callback() {
                            @Override
                            public void call(Context ctx, int id) {
                                File file = new File(pkg.applicationInfo.sourceDir);
                                switch (id) {
                                    case R.id.details:
                                        showAppDetails(ctx, pkg, pm);
                                        break;
                                    case R.id.import_framework:
                                        new ImportFrameworkTask(ctx).execute(file);
                                        break;
                                    default:
                                        decode(ctx, file, id, getMessage() + ".apk");
                                        break;
                                }
                            }
                        });
            }
        });
    }

    protected void decode(Context ctx, File file, int id, String name) {
        switch (id) {
            case R.id.decompile_all:
                new DecodeTask(ctx, null, 3, name).execute(file);
                break;
            case R.id.decompile_res:
                new DecodeTask(ctx, null, 2, name).execute(file);
                break;
            case R.id.decompile_dex:
                new DecodeTask(ctx, null, 1, name).execute(file);
                break;
        }
    }

    protected static void showAppDetails(Context context, PackageInfo pkg, PackageManager pm) {
        StringBuilder sb = new StringBuilder();

        ApplicationInfo info = pkg.applicationInfo;

        sb.append("Package Name: ").append(info.packageName).append("\n");
        sb.append("Version Name: ").append(pkg.versionName).append("\n");
        sb.append("Version Code: ").append(pkg.versionCode).append("\n");
        sb.append("Source Dir: ").append(info.sourceDir).append("\n");
        sb.append("Data Dir: ").append(info.dataDir).append("\n");
        sb.append("System App: ").append((info.flags & ApplicationInfo.FLAG_SYSTEM) != 0 ? "Yes" : "No").append("\n");

        sb.append("Min SDK: ").append(info.minSdkVersion >= 0 ? info.minSdkVersion : "Unknown").append("\n");
        sb.append("Target SDK: ").append(pkg.applicationInfo.targetSdkVersion).append("\n");

File apkFile = new File(info.sourceDir);
double sizeMB = apkFile.length() / (1024.0 * 1024.0);
sb.append(String.format("APK Size: %.2f MB\n", sizeMB));

        try {
            PackageInfo permPkg = pm.getPackageInfo(info.packageName, PackageManager.GET_PERMISSIONS);
            if (permPkg.requestedPermissions != null && permPkg.requestedPermissions.length > 0) {
                sb.append("Permissions:\n");
                for (int i = 0; i < permPkg.requestedPermissions.length; i++) {
                    String perm = permPkg.requestedPermissions[i];
                    boolean granted = false;
                    if (permPkg.requestedPermissionsFlags != null && i < permPkg.requestedPermissionsFlags.length) {
                        granted = (permPkg.requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0;
                    }
                    sb.append("  - ").append(perm);
                    sb.append(granted ? " (granted)" : " (not granted)").append("\n");
                }
            } else {
                sb.append("Permissions: None\n");
            }
        } catch (Exception e) {
            sb.append("Permissions: Unable to retrieve\n");
        }

        if (pkg.activities != null) sb.append("Activities: ").append(pkg.activities.length).append("\n");
        if (pkg.services != null) sb.append("Services: ").append(pkg.services.length).append("\n");
        if (pkg.receivers != null) sb.append("Broadcast Receivers: ").append(pkg.receivers.length).append("\n");
        if (pkg.providers != null) sb.append("Content Providers: ").append(pkg.providers.length).append("\n");

        new AlertDialog.Builder(context)
                .setTitle(info.loadLabel(pm))
                .setIcon(info.loadIcon(pm))
                .setPositiveButton(R.string.ok, null)
                .setMessage(sb.toString())
                .show();
    }
}							@Override
							public void call(Context ctx, int id) {
								File file = new File(pkg.applicationInfo.sourceDir);
								switch (id) {
									case R.id.details:
										showAppDetails(ctx, pkg, pm);
										break;
									case R.id.import_framework:
										new ImportFrameworkTask(ctx).execute(file);
										break;
									default:
										decode(ctx, file, id, getMessage() + ".apk");
										break;
								}
							}
						});
				}
			});
	}
	protected void decode(Context ctx, File file, int id, String name) {
		switch (id) {
			case R.id.decompile_all:
				new DecodeTask(ctx, null, 3, name).execute(file);
				break;
			case R.id.decompile_res:
				new DecodeTask(ctx, null, 2, name).execute(file);
				break;
			case R.id.decompile_dex:
				new DecodeTask(ctx, null, 1, name).execute(file);
				break;
		}
	}
	protected static void showAppDetails(Context context, PackageInfo pkg, PackageManager pm) {
		StringBuilder sb = new StringBuilder();
		sb.append("VersionCode: ");
		sb.append(pkg.versionCode);
		sb.append("\nVersionName: ");
		sb.append(pkg.versionName);
		sb.append("\nPackage: ");
		ApplicationInfo info = pkg.applicationInfo;
		sb.append(info.packageName);
		sb.append("\nPath: ");
		sb.append(info.sourceDir);
		new AlertDialog.Builder(context).
			setTitle(info.loadLabel(pm)).
			setIcon(info.loadIcon(pm)).
			setPositiveButton(R.string.ok, null).
			setMessage(sb).
			show();
	}
}
