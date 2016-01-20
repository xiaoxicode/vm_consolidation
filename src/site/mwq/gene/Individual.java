package site.mwq.gene;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import site.mwq.cloudsim.HostDc;
import site.mwq.main.DataSet;
import site.mwq.targets.Objs;

/**
 *基因算法中的个体，也即多目标优化算法的一个解 
 *也即一个染色体，
 *
 *Individual的equals方法：host编号相同，且host所包含的虚拟机相同
 */
public class Individual {
	
	/**这个解对应的host列表，主要封装利用率什么的一些信息 */	//host列表
	public List<HostDc> hostInds;
	
	/**key为hostId，value为包含的vm，用vmId表示*/
	public Map<Integer,ArrayList<Integer>> hostVmMap;		//host到vm的映射，均用id表示
	
	/**key为vmId，value为hostId*/
	public Map<Integer,Integer> vmHostMap;					//vm到host的映射，均用id表示
	
	/** nsga2算法中的Np，即支配这个解的解的个数*/
	public int nsgaNp;
	
	/** nsga2算法中的Rank*/
	public int nsgaRank;
	
	/**nsga2算法中，这个解所支配的解的集合*/
	public ArrayList<Individual> nsgaDoms; 
	
	/**nsga2算法中的拥挤距离*/
	public double nsgaCrowDis;
	
	/**此个体对各个优化目标的值，顺序按照Objs中给出的顺序*/
	public double[] objVals = new double[Objs.OBJNUM];

	/**
	 * 通过host到vm的映射初始化一个解
	 * 通过这个映射得到vm到host的映射
	 * @param hostVmMap
	 */
	public Individual(Map<Integer,ArrayList<Integer>> hostVmMap){
		hostInds = new ArrayList<HostDc>();	//从DataSet拷贝一份Host的信息
		hostInds.addAll(DataSet.hosts);
		
		
//		this.hostVmMap = new TreeMap<Integer,ArrayList<Integer>>(hostVmMap);
		
		//TODO 手动复制TreeMap，必须手动复制
		this.hostVmMap = new TreeMap<Integer,ArrayList<Integer>>();
		
		for(int i:hostVmMap.keySet()){
			this.hostVmMap.put(i, new ArrayList<Integer>(hostVmMap.get(i)));
		}
		
		vmHostMap = new TreeMap<Integer,Integer>();
		for(int i:hostVmMap.keySet()){						//i是host的Id
			for(int j=0;j<hostVmMap.get(i).size();j++){		//vm的Id是hostVmMap.get(i).get(j)
				vmHostMap.put(hostVmMap.get(i).get(j), i);
			}
		}
	}
	
	/**
	 * 判断一个解是否支配(dominate)另一个解
	 * @param ind
	 * @return 1：支配, 0:无法比较，-1：参数支配调用者
	 */
	public int dominate(Individual ind){
		
		int greatNum = 0,lessNum = 0;
		for(int i=0;i<Objs.OBJNUM;i++){
			if(this.objVals[i]<ind.objVals[i]){
				lessNum++;
			}else if(this.objVals[i]>objVals[i]){
				greatNum++;
			}
		}
		//5个目标都比ind小
		if(lessNum==Objs.OBJNUM){
			return 1;
		}
		//5个目标都比ind大
		if(greatNum==Objs.OBJNUM){
			return -1;
		}
		return 0;
	}
	
	
	/**
	 * 根据vm与host的映射获取资源利用率
	 */
	public void getUtilityRate(){
		
		int usedMem,usedPe;
		long usedNet;
		int availMem,availPe;
		long availNet;
		
		for(int i=0;i<DataSet.hosts.size();i++){
			
			usedMem = 0; usedPe = 0; usedNet = 0;
			
			List<Integer> vmsInHosti = hostVmMap.get(i);
			for(int j=0;j<vmsInHosti.size();j++){
				usedMem += DataSet.vms.get(vmsInHosti.get(j)).getRam();
				usedPe += DataSet.vms.get(vmsInHosti.get(j)).getNumberOfPes();
				usedNet += DataSet.vms.get(vmsInHosti.get(j)).getBw();
			}
			
			availMem = DataSet.hosts.get(i).getRam()-usedMem;
			availPe = DataSet.hosts.get(i).getNumberOfPes()-usedPe;
			availNet = DataSet.hosts.get(i).getBw()-usedNet;
			
			hostInds.get(i).setMemUsed(usedMem);
			hostInds.get(i).setNetUsed(usedNet);
			hostInds.get(i).setPeUsed(usedPe);
			
			hostInds.get(i).setMemAvail(availMem);
			hostInds.get(i).setNetAvail(availNet);
			hostInds.get(i).setPeAvail(availPe);
			//System.out.println("id:"+i+" mem "+hostInds.get(i).getMemRate()+" pe "+hostInds.get(i).getPeRate());
		}
	}

}
