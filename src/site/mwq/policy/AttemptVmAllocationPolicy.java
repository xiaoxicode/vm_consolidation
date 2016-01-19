package site.mwq.policy;

import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;

/**
 * 此类作为分配策略的试探类，不写具体的业务逻辑
 * 
 * @author Email:qiuweimin@126.com
 * @date 2016年1月17日
 */
public class AttemptVmAllocationPolicy extends VmAllocationPolicy{

	/**
	 * vmTable的String存储VM的Uid(Uid是userId-id的组合，即包含user的id，也包含vmid)，
	 * Host则对应VM所在的Host
	 */
	@SuppressWarnings("unused")
	private Map<String, Host> vmTable;
	
	/**
	 * usedPes的String存储VM的Uid(Uid是userId-id的组合，即包含user的id，也包含vmid)，
	 * Integer则对应VM所使用或已使用的pes数
	 */
	@SuppressWarnings("unused")
	private Map<String, Integer> usedPes;
	
	/**
	 * 对应着hostList，对应存储着每台host未使用的pes数
	 */
	@SuppressWarnings("unused")
	private List<Integer> freePes;
	
	
	
	
	public AttemptVmAllocationPolicy(List<? extends Host> list) {
		super(list);
		// TODO Auto-generated constructor stub
	} 



	public static void main(String[] args) {

	}

	
	
	@Override
	public boolean allocateHostForVm(Vm vm) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean allocateHostForVm(Vm vm, Host host) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Map<String, Object>> optimizeAllocation(
			List<? extends Vm> vmList) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deallocateHostForVm(Vm vm) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Host getHost(Vm vm) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Host getHost(int vmId, int userId) {
		// TODO Auto-generated method stub
		return null;
	}

}
