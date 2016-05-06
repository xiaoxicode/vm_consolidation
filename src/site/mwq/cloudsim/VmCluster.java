package site.mwq.cloudsim;

import java.util.ArrayList;

import site.mwq.main.DataSet;

/**
 * 
 * 为了在sandpiper算法中进行虚拟机Swap 
 *	之存储资源利用信息
 * @author dem
 *
 */
public class VmCluster{

	
	//各种资源使用情况
	private int memUsed;
	private int peUsed;
	private long netUsed;
	
	public ArrayList<Integer> vmIds;

	
	public VmCluster(){
		this.vmIds = new ArrayList<Integer>();
	}
	
	/**
	 * 将一台vm添加到这个cluster中
	 * @param vm
	 */
	public void addVm(VmDc vm){
		this.vmIds.add(vm.getId());
		this.memUsed += vm.getRam();
		this.peUsed += vm.getNumberOfPes();
		this.netUsed += vm.getBw();
	}
	
	/**
	 * 上面函数的重载，参数为vmId
	 * @param vmId
	 */
	public void addVm(int vmId){
		VmDc vm = DataSet.vms.get(vmId);
		addVm(vm);
	}
	
	public int getMemUsed() {
		return memUsed;
	}
	public void setMemUsed(int memUsed) {
		this.memUsed = memUsed;
	}
	public int getPeUsed() {
		return peUsed;
	}
	public void setPeUsed(int peUsed) {
		this.peUsed = peUsed;
	}
	public long getNetUsed() {
		return netUsed;
	}
	public void setNetUsed(long netUsed) {
		this.netUsed = netUsed;
	}
	
}
