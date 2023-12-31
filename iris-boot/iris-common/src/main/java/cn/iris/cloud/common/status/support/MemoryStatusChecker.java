package cn.iris.cloud.common.status.support;

import cn.iris.cloud.common.extension.Activate;
import cn.iris.cloud.common.status.Status;
import cn.iris.cloud.common.status.StatusChecker;

/**
 * MemoryStatus
 */
@Activate
public class MemoryStatusChecker implements StatusChecker {

	@Override
	public Status check() {
		Runtime runtime = Runtime.getRuntime();
		long freeMemory = runtime.freeMemory();
		long totalMemory = runtime.totalMemory();
		long maxMemory = runtime.maxMemory();
		boolean ok = (maxMemory - (totalMemory - freeMemory) > 2048); // Alarm when spare memory < 2M
		String msg = "max:" + (maxMemory / 1024 / 1024) + "M,total:"
				+ (totalMemory / 1024 / 1024) + "M,used:" + ((totalMemory / 1024 / 1024) - (freeMemory / 1024 / 1024)) + "M,free:" + (freeMemory / 1024 / 1024) + "M";
		return new Status(ok ? Status.Level.OK : Status.Level.WARN, msg);
	}

}
