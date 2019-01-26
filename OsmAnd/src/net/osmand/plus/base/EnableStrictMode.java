package net.osmand.plus.base;

import android.os.StrictMode;

public class EnableStrictMode {

	public EnableStrictMode(){
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().
				penaltyLog().build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());
	}
}
