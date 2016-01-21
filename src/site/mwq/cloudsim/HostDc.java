package site.mwq.cloudsim;

import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

/**
 * 继承自Cloudsim中的Host，是对Host的扩展，代码中实际使用的 主机类
 * @author Email:qiuweimin@126.com
 * @date 2016年1月18日
 */
public class HostDc extends Host{

	//各种资源数据，包括已经使用的，以及剩余的
	private int memUsed;
	private int memAvail;
	private int peUsed;
	private int peAvail;
	private long netUsed;
	private long netAvail;
	
	/**
	 * 普通构造方法
	 * @param id
	 * @param ramProvisioner
	 * @param bwProvisioner
	 * @param storage
	 * @param peList
	 * @param vmScheduler
	 */
	public HostDc(int id, RamProvisioner ramProvisioner,
			BwProvisioner bwProvisioner, long storage,
			List<? extends Pe> peList, VmScheduler vmScheduler) {
		super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler);
	}

	/**
	 * 拷贝构造方法
	 * @param hostDc
	 */
	public HostDc(HostDc hostDc){
		super(hostDc.getId(), 
				hostDc.getRamProvisioner(), 
				hostDc.getBwProvisioner(),
				hostDc.getStorage(), 
				hostDc.getPeList(), 
				hostDc.getVmScheduler());
		this.setMemAvail(hostDc.getMemAvail());
		this.setMemUsed(hostDc.getMemUsed());
		this.setPeUsed(hostDc.getPeUsed());
		this.setPeAvail(hostDc.getPeAvail());
		this.setNetAvail(hostDc.getNetAvail());
		this.setNetUsed(hostDc.getNetUsed());
	}
	
	
	/**
	 * 用来判断一个host是否能容纳一个vm
	 * 考虑内存、cpu、带宽三种资源
	 * @param vm
	 * @param host
	 * @return
	 */
	public boolean canHold(VmDc vm){
		
		if(getMemAvail() >= vm.getRam()
				&& getPeAvail() >= vm.getNumberOfPes()
				&& getNetAvail() >= vm.getBw()){
			return true;
		}
		
		return false;
	}
	
	public int getMemUsed() {
		return memUsed;
	}


	public void setMemUsed(int memUsed) {
		this.memUsed = memUsed;
	}


	public int getMemAvail() {
		return memAvail;
	}


	public void setMemAvail(int memAvail) {
		this.memAvail = memAvail;
	}


	public int getPeUsed() {
		return peUsed;
	}


	public void setPeUsed(int peUsed) {
		this.peUsed = peUsed;
	}


	public int getPeAvail() {
		return peAvail;
	}


	public void setPeAvail(int peAvail) {
		this.peAvail = peAvail;
	}


	public long getNetUsed() {
		return netUsed;
	}


	public void setNetUsed(long netUsed) {
		this.netUsed = netUsed;
	}


	public long getNetAvail() {
		return netAvail;
	}


	public void setNetAvail(long netAvail) {
		this.netAvail = netAvail;
	}


	public double getMemRate() {
		return (double)memUsed/super.getRam();
	}


	public double getPeRate() {
		return (double)peUsed/super.getPeList().size();
	}

}
