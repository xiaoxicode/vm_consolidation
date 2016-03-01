package site.mwq.cloudsim;

import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

import site.mwq.compare.Sandpiper;
import site.mwq.main.DataSet;

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
	
	/**
	 * 判断一个host能否容纳一个vm，并且各个资源都不超过阈值
	 * @param vm
	 * @return
	 */
	public boolean canHoldAndNotOverLoad(VmDc vm){
	
		double memUsed = this.memUsed+vm.getRam();
		double cpuUsed = this.peUsed+vm.getNumberOfPes();
		double netUsed = this.netUsed+vm.getBw();
		
		if(memUsed/getRam()<Sandpiper.memThreshold
				&& cpuUsed/getNumberOfPes()<Sandpiper.cpuThreshold
				&& netUsed/getBw()<Sandpiper.netThreshold){
			return true;
		}
		
		return false;
	}
	
	/**
	 * 判断一个host能否将一台VM与其他host上的一个VM cluster对换，
	 * 且不能超过负载
	 * 
	 * @param vmId 换出的VM的id
	 * @param vc	换入的cluster，封装了资源利用信息
	 * @return
	 */
	public boolean canSwapVmWithCluster(int vmId,VmCluster vc){
	
		VmDc vm = DataSet.vms.get(vmId);
		
		double memUsed = this.memUsed+vc.getMemUsed();
		double cpuUsed = this.peUsed+vc.getPeUsed();
		double netUsed = this.netUsed+vc.getNetUsed();
		
		memUsed -= vm.getRam();
		cpuUsed -= vm.getNumberOfPes();
		netUsed -= vm.getBw();
		
		if(memUsed/getRam()<Sandpiper.memThreshold
				&& cpuUsed/getNumberOfPes()<Sandpiper.cpuThreshold
				&& netUsed/getBw()<Sandpiper.netThreshold){
			return true;
		}
		
		return false;
	}
	
	/**
	 * 换出一个cluster,换入一个vm，看看能否不超载
	 * 
	 * @param vc 换出的VmCluster
	 * @param vmId	换入的Vm
	 * @return
	 */
	public boolean canSwapClusterWithVm(VmCluster vc,int vmId){
		
		VmDc vm = DataSet.vms.get(vmId);
		
		double memUsed = this.memUsed-vc.getMemUsed();
		double cpuUsed = this.peUsed-vc.getPeUsed();
		double netUsed = this.netUsed-vc.getNetUsed();
		
		memUsed += vm.getRam();
		cpuUsed += vm.getNumberOfPes();
		netUsed += vm.getBw();
		
		if(memUsed/getRam()<Sandpiper.memThreshold
				&& cpuUsed/getNumberOfPes()<Sandpiper.cpuThreshold
				&& netUsed/getBw()<Sandpiper.netThreshold){
			return true;
		}
		
		return false;
	}
	
	
	/**
	 * 移除一台vm，同时更新内存，pe,带宽数据
	 * @param vmId
	 */
	public void removeVmUpdateResource(int vmId){
		
		VmDc vm = DataSet.vms.get(vmId);
		
		this.memAvail += vm.getRam();
		this.memUsed -= vm.getRam();
		this.netAvail += vm.getBw();
		this.netUsed -= vm.getBw();
		this.peAvail += vm.getNumberOfPes();
		this.peUsed -= vm.getNumberOfPes();
		
		//TODO super.getVmList().remove(vmId)，有待确认
	//	super.getVmList().remove(vm);
	}
	
	/**
	 * 将一台vm加入到此host，主要更新资源利用率信息
	 * @param vmId
	 */
	public void addVmUpdateResource(int vmId){
		VmDc vm = DataSet.vms.get(vmId);
		
		this.memAvail -= vm.getRam();
		this.memUsed += vm.getRam();
		this.netAvail -= vm.getBw();
		this.netUsed += vm.getBw();
		this.peAvail -= vm.getNumberOfPes();
		this.peUsed += vm.getNumberOfPes();
		
		//TODO super.getVmList().add(vmId)，有待确认
	//	super.getVmList().add(vm);
	}
	
	/**
	 * sandpiper算法中的volume值，衡量一个vm或host负载的变量
	 * @return double
	 */
	public double getVol(){
		
		double cpuRate = getCpuRate();
		double netRate = getNetRate();
		double memRate = getMemRate();
		
		if(cpuRate==1){
			cpuRate = 1-Sandpiper.littleValue;
		}
		if(netRate==1){
			netRate = 1-Sandpiper.littleValue;
		}
		if(memRate==1){
			memRate = 1-Sandpiper.littleValue;
		}
		
		double vol = (1/(1-cpuRate))*(1/(1-netRate))*(1/(1-memRate));
		return vol;
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


	public double getCpuRate() {
		return (double)peUsed/super.getNumberOfPes();
	}

	public double getNetRate() {
		return (double)netUsed/super.getBw();
	}
	
//	/**
//	 * 获取host的vsr, 即 volume/size
//	 * @return
//	 */
//	public double getVsr(){
//		double vsr = 0;
//		
//		vsr = getVol()/getMemUsed();
//		
//		return vsr;
//	}
	
	/**
	 * 判断一个host是否overload，只要有一个资源利用率超过阈值，
	 * 即认为是overload，参数设置在Sandpiper类中
	 * @return
	 */
	public boolean isOverLoad(){
		if(getMemRate() > Sandpiper.memThreshold
				|| getCpuRate() > Sandpiper.cpuThreshold
				|| getNetRate() > Sandpiper.netThreshold){
			return true;
		}
		return false;
	}
	
	/**
	 * 打印资源利用率
	 */
	public void displayRate(){
		System.out.println("mem:"+getMemRate()+
				" cpu:"+getCpuRate()+" net:"+getNetRate());
	}
}
