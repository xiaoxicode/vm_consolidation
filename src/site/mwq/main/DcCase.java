package site.mwq.main;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;

import site.mwq.cloudsim.DcBroker;
import site.mwq.policy.VmAllocationPolicySimpleModify;
import site.mwq.utils.Utils;

/**
 * 程序运行入口，一个数据中心实例
 * @author Email:qiuweimin@126.com
 * @date 2016年1月17日
 */
public class DcCase {

	/**默认创建的host数和虚拟机数**/
	public static final int hostNum = 18;
	public static final int vmNum = 40;
	
	public static void main(String[] args) {
		
		DataSet.init(hostNum, vmNum); 								//初始化数据集合
		
		DcCase dcCase = new DcCase();
		@SuppressWarnings("unused")
		Datacenter dc = dcCase.createDatacenter("MyDataCenter");	//创建数据中心
		DatacenterBroker dcb = dcCase.createBroker();				//创建自定义代理
		
		//vmNum = 40
		List<Vm> vms = Factory.createVmsRandomly(vmNum, dcb.getId());	//创建一些列VM
		dcb.submitVmList(vms);
		
		dcCase.runCloudlets(dcb);							//运行Cloudlets
		Utils.disVmHostMap(DataSet.hostVmMap);				//打印 VM host映射
	}
	
	/**
	 * 构造函数
	 * 运行Cloudsim的第一步，初始化相关参数
	 */
	public DcCase(){
		int num_user = 1; 								// number of cloud users
		Calendar calendar = Calendar.getInstance();
		boolean trace_flag = false; 					// mean trace events
		CloudSim.init(num_user, calendar, trace_flag);	// Initialize the CloudSim library
	}
	
	/**
	 * 创建一个数据中心
	 * @param name 数据中心的名字
	 * @return Datacenter 数据中心的实例
	 */
	public Datacenter createDatacenter(String name) {

		
		//根据三层网络结构，创建18台物理机，8核，每个核1000Mips，4G内存
		List<Host> hosts = Factory.createHost(hostNum);

		LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are not adding SAN devices by now

		//用默认参数创建DatacenterCharacteristics
		DatacenterCharacteristics characteristics = Factory.createDcCharacteristics(hosts);

		//创建数据中心，指定VM分配策略
		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimpleModify(hosts), storageList, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}

	// We strongly encourage users to develop their own broker policies, to
	// submit vms and cloudlets according to the specific rules of the simulated scenario
	/**
	 * 创建代理，这里的代理是自己的代理，重写了原代理的部分方法
	 * @return the datacenter broker
	 */
	public DatacenterBroker createBroker() {
		DatacenterBroker broker = null;
		try {
			broker = new DcBroker("Broker");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}
	
	/**
	 * Prints the Cloudlet objects.
	 *
	 * @param list list of Cloudlets
	 */
	public void printCloudletList(List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet;

		String indent = "    ";
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent
				+ "Data center ID" + indent + "VM ID" + indent + "Time" + indent
				+ "Start Time" + indent + "Finish Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);

			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
				Log.print("SUCCESS");

				Log.printLine(indent + indent + cloudlet.getResourceId()
						+ indent + indent + indent + cloudlet.getVmId()
						+ indent + indent
						+ dft.format(cloudlet.getActualCPUTime()) + indent
						+ indent + dft.format(cloudlet.getExecStartTime())
						+ indent + indent
						+ dft.format(cloudlet.getFinishTime()));
			}
		}
	}
	
	public void runCloudlets(DatacenterBroker dcb ){
		// Cloudlet properties
		
		List<Cloudlet> cloudletList = new ArrayList<Cloudlet>();
		int id = 0;
		long length = 400000;
		long fileSize = 300;
		long outputSize = 300;
		UtilizationModel utilizationModel = new UtilizationModelFull();

		Cloudlet cloudlet = new Cloudlet(id, length, 1, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
		cloudlet.setUserId(dcb.getId());
		cloudlet.setVmId(0);

		// add the cloudlet to the list
		cloudletList.add(cloudlet);

		// submit cloudlet list to the broker
		dcb.submitCloudletList(cloudletList);

		// Sixth step: Starts the simulation
		
		
		CloudSim.startSimulation();
		CloudSim.stopSimulation();

		//Final step: Print results when simulation is over
		List<Cloudlet> newList = dcb.getCloudletReceivedList();
		printCloudletList(newList);

		Log.printLine("CloudSimExample1 finished!");
	}
	
}
