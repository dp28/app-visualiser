package dp28.app.visualiser.model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

import android.content.pm.IPackageStatsObserver;
import android.content.pm.IPackageStatsObserver.Stub;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.os.RemoteException;

/**
 * Acquires information (in the form of {@link PackageStats}) about all
 * installed packages on an Android device.
 * 
 * Based on
 * http://nsamteladze.blogspot.co.uk/2012/10/get-apks-code-size-in-android.html
 */
public class PackageDataCollector {

	private Collection<PackageStats> packages;
	private PackageManager packageManager;
	private Semaphore semaphore;

	public PackageDataCollector(PackageManager packageManager) {
		packages = new ConcurrentLinkedQueue<PackageStats>();
		this.packageManager = packageManager;
		semaphore = new Semaphore(1, true);
	}

	public Collection<PackageStats> findPackageStatistics() {
		List<PackageInfo> installedPackages = packageManager.getInstalledPackages(0);

		packages.clear();
		addAllPackageStats(installedPackages);
		return packages;
	}

	private void addAllPackageStats(List<PackageInfo> installedApplications) {
		for (PackageInfo packageInfo : installedApplications) {
			addPackageStatsForPackage(packageInfo);
		}
	}

	private void addPackageStatsForPackage(PackageInfo packageInfo) {
		aquireSemaphore();
		invokeGetPackageSizeInfo(packageInfo);
	}

	/**
	 * 
	 * @param packageInfo
	 */
	private void invokeGetPackageSizeInfo(PackageInfo packageInfo) {
		try {
			Method getPackageSizeInfo = packageManager.getClass()
														.getMethod(	"getPackageSizeInfo",
																	String.class,
																	IPackageStatsObserver.class);
			IPackageStatsObserver.Stub observer = createPackageStatsObserver();
			getPackageSizeInfo.invoke(packageManager, packageInfo.packageName, observer);
		}
		catch (InvocationTargetException e) {
			e.getTargetException().printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Ensures that the code following the call to this method is only accessed
	 * by one thread at a time. Necessary as the aquisition of the statistics
	 * runs on a separate thread.
	 */
	private void aquireSemaphore() {
		try {
			semaphore.acquire();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates an {@link IPackageStatsObserver}.Stub which saves the
	 * {@link PackageStats} for an individual package and releases the
	 * semaphore.
	 * 
	 * @return
	 */
	private Stub createPackageStatsObserver() {

		IPackageStatsObserver.Stub observer = new IPackageStatsObserver.Stub() {

			@Override
			public void onGetStatsCompleted(PackageStats packageStats, boolean succeeded) throws RemoteException {
				packages.add(packageStats);
				semaphore.release();
			}
		};

		return observer;
	}
}
