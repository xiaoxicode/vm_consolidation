package site.mwq.main;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.core.CloudSim;

import site.mwq.cloudsim.BrokerDc;
import site.mwq.cloudsim.HostDc;
import site.mwq.cloudsim.VmDc;
import site.mwq.compare.RIAL;
import site.mwq.compare.Sandpiper;
import site.mwq.gene.IndComp;
import site.mwq.gene.Individual;
import site.mwq.gene.Nsga;
import site.mwq.gene.Pop;
import site.mwq.policy.RandomVmAllocationPolicy;
import site.mwq.utils.FileUtils;
import site.mwq.utils.Utils;

/**
 * 程序运行入口，一个数据中心实例
 * @author Email:qiuweimin@126.com
 * @date 2016年1月17日
 */
public class DcCase {

	/**默认创建的host数和虚拟机数**/
	public static final int hostNum = 50;
	public static final int vmNum = 150;
	
	/**
	 * 构造函数
	 * 
	 * 运行Cloudsim的第一步，初始化相关参数
	 */
	public DcCase(){
		int num_user = 1; 								// number of cloud users
		Calendar calendar = Calendar.getInstance();
		boolean trace_flag = false; 					// mean trace events
		CloudSim.init(num_user, calendar, trace_flag);	// Initialize the CloudSim library
	}
	
	public static void runProgram() {
		
		//首先使用VmAllocationPolicySimple策略生成一个数据中心
		//并生成初始解，后续解都与这个解对比
		
		DataSet.init(hostNum, vmNum); 				//初始化数据集合
		Factory.init();
		DcCase dcCase = new DcCase();
		@SuppressWarnings("unused")
		Datacenter dc = dcCase.createDcSimpleVmAlloc("MyDataCenter");	//创建数据中心
		DatacenterBroker dcb = dcCase.createBroker();				//创建自定义代理
			
		//vmNum = 40
		List<VmDc> vms = Factory.createVmsRandomly(vmNum, dcb.getId());	//创建一些列VM
		DataSet.vms.addAll(vms);							//添加到数据集合中	
		
		dcb.submitVmList(vms);
		dcCase.runCloudlets(dcb);							//运行Cloudlets，模拟开始与结束
		DataSet.initFirstInd();
		
		//1、复制100个个体作为初始的种群  100->200
		for(int i=0;i<100;i++){
			Pop.inds.add(new Individual(DataSet.hostVmMap));
		}
		
		//Pop.copyParentToChild();
		
		//2、迭代计算
		for(int i=0;i<100;i++){
			
			Pop.select();

			Pop.crossoverVersion2(Pop.children);
			Pop.mutationVersion2(Pop.children);
			
			Nsga.calculateObj();
			Pop.inds = Nsga.nsgaMain(Pop.inds, Pop.children);
			Pop.children.clear();
			
		}
		
		Collections.sort(Pop.inds,new IndComp());
		
		//3、打印前30个结果
		for(int i=0;i<20;i++){
			Utils.disIndVal(Pop.inds.get(i));
		}
		
		////////////比较实验
		System.out.println("-------分割线------");
		Sandpiper sand = new Sandpiper(DataSet.hostVmMap);
		double[] sandRes = sand.moveVm();
		FileUtils.printSand(sandRes);
		
		System.out.println("-------分割线------");
		RIAL rial = new RIAL(DataSet.hostVmMap);
		double[] rialRes = rial.move();
		FileUtils.printRial(rialRes);
		
		double minMigInSandRial = Math.min(sandRes[0], rialRes[0]);
		
		//打印结果矩阵
		int index = 0;
		double migCnt = Utils.mc.objVal(Pop.inds.get(index));
		ArrayList<double[]> matrix = new ArrayList<double[]>();

		while(migCnt==0){
			matrix.add(null);	//为保证matrix下标和Pop.inds的下标对应，在matrix中加入null值
			index++;
			migCnt = Utils.mc.objVal(Pop.inds.get(index));
		}
		
		double[] sum = new double[5];
		
		while(migCnt<=minMigInSandRial){
			double[] resIndex = Utils.getIndVal(Pop.inds.get(index));
			for(int j=0;j<resIndex.length;j++){
				sum[j] += resIndex[j];
			}
			matrix.add(resIndex);
			migCnt = Utils.mc.objVal(Pop.inds.get(++index));
		}
		
		//归一化，同时选择累加值的最小值
		double minVal = Double.MAX_VALUE;
		int minIndex = 0;

		for(int i=0;i<matrix.size();i++){
			if(matrix.get(i)==null){
				continue;
			}
			double[] row = matrix.get(i);
			
			double curVal = 0;
			for(int j=1;j<row.length-1;j++){	//忽略第一项和最后一项
				if(j==2){
					row[j] /= sum[j];
					row[j] *= 0.4;     //通信代价的权重是0.4
					curVal += row[j];
				}else{
					row[j] /= sum[j];
					row[j] *= 0.2;		//其他指标代价为0.2
					curVal += row[j];
				}
			}
			
			if(curVal<minVal){
				minVal = curVal;
				minIndex = i;
			}
		}
		
		System.out.println("-----ours-----");
		Utils.disIndVal(Pop.inds.get(0));
		FileUtils.printOri(Utils.getIndVal(Pop.inds.get(0)));
		Utils.disIndVal(Pop.inds.get(minIndex));
		FileUtils.printGene(Utils.getIndVal(Pop.inds.get(minIndex)));
		
	}//runProgram方法结束
	
	//TODO main方法
	public static void main(String[] args) {
		
		long cur = System.currentTimeMillis();
		for(int i=0;i<1;i++){
			runProgram();
			long end = System.currentTimeMillis();
			System.out.println("++++++++++++"+i+"+++++++"+(end-cur)/1000);
			cur = System.currentTimeMillis();
		}
	}
	
	
	/**
	 * 创建一个数据中心，以及物理机，虚拟机随机分配
	 * @param name 数据中心的名字
	 * @return Datacenter 数据中心的实例
	 */
	@SuppressWarnings("unused")
	private Datacenter createDcRandomVmAlloc(String name) {

		
		//根据三层网络结构，创建18台物理机，8核，每个核1000Mips，4G内存
		List<HostDc> hosts = Factory.createHost(hostNum);

		LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are not adding SAN devices by now

		//用默认参数创建DatacenterCharacteristics
		DatacenterCharacteristics characteristics = Factory.createDcCharacteristics(hosts);

		//创建数据中心，指定VM分配策略
		Datacenter datacenter = null;
		try {
			//TODO 在这里修改虚拟机分配策略
			datacenter = new Datacenter(name, characteristics, new RandomVmAllocationPolicy(hosts), storageList, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}
	
	/**
	 * 创建一个数据中心，以及物理机，虚拟机均匀分配
	 * @param name 数据中心的名字
	 * @return Datacenter 数据中心的实例
	 */
	private Datacenter createDcSimpleVmAlloc(String name) {

		
		//根据三层网络结构，创建18台物理机，8核，每个核1000Mips，4G内存
		List<HostDc> hosts = Factory.createHost(hostNum);

		LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are not adding SAN devices by now

		//用默认参数创建DatacenterCharacteristics
		DatacenterCharacteristics characteristics = Factory.createDcCharacteristics(hosts);

		//创建数据中心，指定VM分配策略
		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, characteristics, new RandomVmAllocationPolicy(hosts), storageList, 0);
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
	private DatacenterBroker createBroker() {
		DatacenterBroker broker = null;
		try {
			broker = new BrokerDc("Broker");
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
	private void printCloudletList(List<Cloudlet> list) {
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
	
	private void runCloudlets(DatacenterBroker dcb ){
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
