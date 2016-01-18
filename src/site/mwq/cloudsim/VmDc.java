package site.mwq.cloudsim;

import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.Vm;

/**
 * 继承自Cloudsim中的Vm，是对Vm的扩展
 * @author Email:qiuweimin@126.com
 * @date 2016年1月18日
 */
public class VmDc extends Vm{

	public VmDc(int id, int userId, double mips, int numberOfPes, int ram,
			long bw, long size, String vmm, CloudletScheduler cloudletScheduler) {
		super(id, userId, mips, numberOfPes, ram, bw, size, vmm, cloudletScheduler);
	}

}
