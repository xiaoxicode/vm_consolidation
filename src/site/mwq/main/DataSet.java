package site.mwq.main;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import site.mwq.cloudsim.HostDc;
import site.mwq.cloudsim.VmDc;
import site.mwq.gene.Individual;
import site.mwq.utils.Utils;

/**
 * 数据集合，所有数据中心中的数据都保存在这里
 * 包含初始解
 * 注意，这个类中保存的信息都是固定不变的
 * 
 * @author Email:qiuweimin@126.com
 * @date 2016年1月18日
 */
public class DataSet {
	
	//保存host与vm的配置信息
	public static List<HostDc> hosts = new ArrayList<HostDc>();
	public static List<VmDc> vms = new ArrayList<VmDc>();
	
	//初始的vm与host映射关系
	public static Map<Integer,ArrayList<Integer>> hostVmMap;		//host到vm的映射，均用id表示
	public static Map<Integer,Integer> vmHostMap;					//vm到host的映射，均用id表示
	
	/**由初始映射关系得到的初始解*/
	public static Individual firstInd;
	
	//通信矩阵
	public static final double probOfCom = 0.3;
	public static int[][] comMatrix;
	
	public static void initFirstInd(){
		firstInd = new Individual(hostVmMap);
		firstInd.getUtilityRate();
		for(int i : firstInd.hostVmMap.keySet()){
			System.out.println(firstInd.hostInds.get(i).getMemRate());
		}
	}
	
	/**
	 * 初始化，所有数据为空
	 * @param hostNum
	 * @param vmNum
	 */
	public static void init(int hostNum,int vmNum){
		
		hostVmMap = new TreeMap<Integer,ArrayList<Integer>>();
		for(int i=0;i<hostNum;i++){
			hostVmMap.put(i, new ArrayList<Integer>());
		}
		vmHostMap = new TreeMap<Integer,Integer>();
		
		initCommunicationMatrix(vmNum);	//随机产生一个通信矩阵
	}
	
	/**
	 * 根据vmNum初始化一个通信矩阵
	 * TODO 后续考虑减小通信矩阵带来的空间开销
	 * 
	 * 两个虚拟机相互通信的概率为0.3，通信量从10-200随机
	 * vm带宽默认为1000
	 * @param vmNum
	 * @return
	 */
	public static void initCommunicationMatrix(int vmNum){
		
		comMatrix = new int[vmNum][vmNum]; 
		
		double comProb = 0;
		int comNum = 0;
		
		for(int i=0;i<vmNum;i++){
			for(int j=0;j<vmNum;j++){
				comProb = Utils.random.nextDouble();
				if(comProb<probOfCom){
					comNum = 10+Utils.random.nextInt(190);
					comMatrix[i][j] = comNum;
				}
			}
		}
	}
}
