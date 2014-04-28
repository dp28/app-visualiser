package dp28.app.visualiser.model;

import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageStats;
import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * A wrapper around the Android {@link PackageStats}, providing convenience
 * methods to convert the information into a more useable form (such as in
 * megabytes rather than bytes).
 * 
 */
public class InstalledPackage {

	public static final long MEGABYTES_IN_BYTES = 1024 * 1024;
	private PackageStats packageStats;
	private Drawable icon;

	public InstalledPackage(PackageStats packageStats, PackageManager packageManager) {
		this.packageStats = packageStats;
		findIcon(packageManager);
	}

	private void findIcon(PackageManager packageManager) {
		try {
			icon = packageManager.getApplicationIcon(packageStats.packageName);
		}
		catch (NameNotFoundException e) {
			Log.w("InstalledPackage", e.getMessage());
		}
	}

	public Drawable getIcon() {
		return icon;
	}

	public String getName() {
		return packageStats.packageName;
	}

	public String getLastPartOfPackageName() {
		String[] packagePieces = packageStats.packageName.split("\\.");
		return packagePieces[packagePieces.length - 1];
	}

	public String getPackageNameWithoutLastPart() {
		String name = packageStats.packageName;
		name = name.replace(getLastPartOfPackageName(), "");
		return name;
	}

	public double getCodeSizeInMb() {
		return (double) packageStats.codeSize / MEGABYTES_IN_BYTES;
	}

	public double getCacheSizeInMb() {
		return (double) packageStats.cacheSize / MEGABYTES_IN_BYTES;
	}

	public double getDataSizeInMb() {
		return (double) packageStats.dataSize / MEGABYTES_IN_BYTES;
	}

	public double getTotalSizeInMb() {
		return getCacheSizeInMb() + getCodeSizeInMb() + getDataSizeInMb();
	}

	@Override
	public String toString() {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(getLastPartOfPackageName().toUpperCase());
		stringBuffer.append("\n    Full Package: ");
		stringBuffer.append(getName());
		stringBuffer.append("\n    Total size in Mb: ");
		stringBuffer.append(getTotalSizeInMb());
		stringBuffer.append("\n    Code size in Mb: ");
		stringBuffer.append(getCodeSizeInMb());
		stringBuffer.append("\n    Cache size in Mb: ");
		stringBuffer.append(getCacheSizeInMb());
		stringBuffer.append("\n    Data size in Mb: ");
		stringBuffer.append(getDataSizeInMb());
		return stringBuffer.toString();
	}

}
