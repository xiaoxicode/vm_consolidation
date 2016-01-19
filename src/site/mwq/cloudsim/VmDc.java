package site.mwq.cloudsim;

import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.Vm;

import site.mwq.utils.Utils;

/**
 * 继承自Cloudsim中的Vm，是对Vm的扩展
 * @author Email:qiuweimin@126.com
 * @date 2016年1月18日
 */
public class VmDc extends Vm{

	/**脏页率在50-100之间*/
	private int dirtyRate;
	
	public VmDc(int id, int userId, double mips, int numberOfPes, int ram,
			long bw, long size, String vmm, CloudletScheduler cloudletScheduler) {
		
		super(id, userId, mips, numberOfPes, ram, bw, size, vmm, cloudletScheduler);
		this.dirtyRate = Utils.random.nextInt(50)+50;
	}

	public int getDirtyRate() {
		return dirtyRate;
	}

	public void setDirtyRate(int dirtyRate) {
		this.dirtyRate = dirtyRate;
	}

}
