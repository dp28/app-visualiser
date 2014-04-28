package dp28.app.visualiser.model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

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

	private Collection<InstalledPackage> packages;
	private PackageManager packageManager;
	private int packagesToConvert;

	public PackageDataCollector(PackageManager packageManager) {
		packages = new ConcurrentLinkedQueue<InstalledPackage>();
		this.packageManager = packageManager;
	}

	/**
	 * A synchronous method for finding all the installed packages on the
	 * device. WARNING: this may take a long time, depending on the device (eg,
	 * Samsung Galaxy Ace takes up to 20 seconds)
	 * 
	 * @return
	 */
	public Collection<InstalledPackage> findAllInstalledPackages() {
		startFindInstalledPackages();
		while (packagesToConvert > 0)
			;// Wait until size data has been discovered for all packages.
		return packages;
	}

	/**
	 * Starts an asynchronous search for every package installed on this device.
	 */
	public void startFindInstalledPackages() {
		List<PackageInfo> installedPackages = packageManager.getInstalledPackages(0);
		packagesToConvert = installedPackages.size();
		packages.clear();
		addAllPackageStats(installedPackages);
	}

	/**
	 * Whether the search for installed packages has finished yet.
	 */
	public boolean isFinishedFindingInstalledPackages() {
		return packagesToConvert == 0;
	}

	/**
	 * 
	 * @return A {@link Collection} of {@link InstalledPackage}s, one for each
	 *         package installed on this device. This will be empty unless
	 *         {@link #startFindInstalledPackages()} has been called beforehand
	 *         and may change if {@link #isFinishedFindingInstalledPackages()}
	 *         is not true.
	 */
	public Collection<InstalledPackage> getInstalledPackages() {
		return packages;
	}

	private void addAllPackageStats(List<PackageInfo> installedApplications) {
		for (PackageInfo packageInfo : installedApplications) {
			addPackageStatsForPackage(packageInfo);
		}
	}

	private void addPackageStatsForPackage(PackageInfo packageInfo) {
		try {
			invokeGetPackageSizeInfo(packageInfo);
		}
		catch (InvocationTargetException e) {
			e.getTargetException().printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param packageInfo
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private void invokeGetPackageSizeInfo(PackageInfo packageInfo)	throws NoSuchMethodException,
																	IllegalAccessException,
																	IllegalArgumentException,
																	InvocationTargetException {
		Method getPackageSizeInfo = packageManager.getClass()
													.getMethod(	"getPackageSizeInfo",
																String.class,
																IPackageStatsObserver.class);
		IPackageStatsObserver.Stub observer = createPackageStatsObserver();
		getPackageSizeInfo.invoke(packageManager, packageInfo.packageName, observer);

	}

	/**
	 * Creates an {@link IPackageStatsObserver}.Stub which saves the
	 * {@link PackageStats} for an individual package and releases the
	 * semaphore.
	 * 
	 * @return
	 */
	private Stub createPackageStatsObserver() {
		return new PackageStatsObserver();
	}

	private class PackageStatsObserver extends IPackageStatsObserver.Stub {

		@Override
		public void onGetStatsCompleted(PackageStats packageStats, boolean succeeded) throws RemoteException {
			InstalledPackage installed = new InstalledPackage(packageStats, packageManager);
			packages.add(installed);
			packagesToConvert--;
		}
	}
}
