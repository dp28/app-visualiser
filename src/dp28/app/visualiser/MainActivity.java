package dp28.app.visualiser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import dp28.app.visualiser.model.InstalledPackage;
import dp28.app.visualiser.model.InstalledPackageComparator;
import dp28.app.visualiser.model.PackageDataCollector;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ListView listView = (ListView) findViewById(R.id.listView1);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
																android.R.layout.simple_list_item_1);
		listView.setAdapter(adapter);
		adapter.add("test");
		PackageManager packageManager = getPackageManager();
		PackageDataCollector collector = new PackageDataCollector(packageManager);
		List<InstalledPackage> packages = new ArrayList<InstalledPackage>();

		Date start = new Date();
		Collection<PackageStats> packStats = collector.findPackageStatistics();
		Log.d("MAIN", "find time: " + ((new Date()).getTime() - start.getTime()) / 1000.0);
		start = new Date();

		for (PackageStats stats : packStats)
			packages.add(new InstalledPackage(stats, packageManager));

		Log.d("MAIN", "convert time: " + ((new Date()).getTime() - start.getTime()) / 1000.0);
		start = new Date();

		Collections.sort(packages, new InstalledPackageComparator());

		Log.d("MAIN", "sort time: " + ((new Date()).getTime() - start.getTime()) / 1000.0);
		start = new Date();

		for (InstalledPackage pack : packages)
			adapter.add(pack.getName() + " " + pack.getTotalSizeInMb());

		Log.d("MAIN", "add time: " + ((new Date()).getTime() - start.getTime()) / 1000.0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
