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
	
	/**使用的内存量，仅用在sandpiper中*/
	private int memUsed;
	private int netUsed;
	private int cpuUsed;
	
	public VmDc(int id, int userId, double mips, int numberOfPes, int ram,
			long bw, long size, String vmm, CloudletScheduler cloudletScheduler) {
		
		super(id, userId, mips, numberOfPes, ram, bw, size, vmm, cloudletScheduler);
		this.dirtyRate = Utils.random.nextInt(50)+50;
		
		
		//初始化资源利用，目前只用在sandpiper中
		this.memUsed = Utils.random.nextInt(ram);
		if(this.memUsed<100){
			this.memUsed += 100;
		}
		
		this.netUsed = Utils.random.nextInt((int)bw);	//TODO 带宽 vm的不应该是随机生成，而应该是通信矩阵相加
		this.cpuUsed = Utils.random.nextInt(numberOfPes);
		if(this.cpuUsed<1){
			this.cpuUsed = 1;
		}
	}

	public int getDirtyRate() {
		return dirtyRate;
	}

	public void setDirtyRate(int dirtyRate) {
		this.dirtyRate = dirtyRate;
	}
	
	/**
	 * 为vm随机生成mem利用率，用在sandpiper中
	 * @return
	 */
	public double getMemRate() {
		
		return memUsed/getRam();
	}

	/**
	 * 为vm随机生成cpu利用率，用在sandpiper中
	 * @return
	 */
	public double getCpuRate() {
		return cpuUsed/super.getNumberOfPes();
	}

	/**
	 * 生成网络利用率，用在sandpiper中
	 * @return
	 */
	public double getNetRate() {
		return netUsed/super.getBw();
	}
	
	/**
	 * 获取host的vsr, 即 volume/size
	 * @return
	 */
	public double getVsr(){
		double vsr = 0;
		double volume = (1/(1-getMemRate()))*(1/(1-getCpuRate()))*(1/(1-getNetRate()));
		vsr = volume/memUsed;
		return vsr;
	}

}
