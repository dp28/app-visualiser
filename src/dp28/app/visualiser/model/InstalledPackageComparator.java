package dp28.app.visualiser.model;

import java.util.Comparator;

/**
 * Compares {@link InstalledPackage}s so that they can be sorted in descending order in terms of
 * size.
 * 
 */
public class InstalledPackageComparator implements Comparator<InstalledPackage> {

	@Override
	public int compare(InstalledPackage lhs, InstalledPackage rhs) {
		double radiusDifference = lhs.getTotalSizeInMb() - rhs.getTotalSizeInMb();
		if (radiusDifference > 0)
			return -1;
		else if (radiusDifference < 0)
			return 1;
		else
			return 0;
	}

}
