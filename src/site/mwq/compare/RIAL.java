package site.mwq.compare;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import site.mwq.cloudsim.HostDc;
import site.mwq.cloudsim.VmDc;
import site.mwq.gene.Individual;
import site.mwq.main.DataSet;
import site.mwq.targets.MigTime;
import site.mwq.utils.Utils;

/**
 * 2014 infocom
 * @author Email:qiuweimin@126.com
 * @date 2016年2月29日
 */
public class RIAL {
	/**RIAL只对一个解进行一次整合*/
	public Individual ind;
	
	//此RIAL所对应的数据结构，即某一时刻的映射状态
	public List<HostDc> hosts;
	public TreeMap<Integer,HashSet<Integer>> hostVmMap;		//host到vm的映射，均用id表示
	public Map<Integer,Integer> vmHostMap;					//vm到host的映射，均用id表示
	
	public static int moveCnt = 0;				//迁移次数
	public static double migTime = 0;			//总迁移时间
	
	/**分别表示cpu,mem,net资源的权重，对于每个负载过高的物理机，这三个值是不一样的*/
	public double[] wik = new double[3]; 		//w0k,w1k,w2k;
	
	/**wt是虚拟机通信量Tij相对于资源利用的权重，如何确定wt的值？*/
	public double wt = 0.2;
	
	/**wd是性能下降的权重（迁移代价），如何确定wd的值？*/
	public double wd = 0.3;
	
	/**负载过高的物理机集合*/
	public ArrayList<Integer> heavyPms; 
	
	/**负载不高的物理机集合*/
	public ArrayList<Integer> lightPms;
	
	/**key为vmId，
	 * value的第一项为源物理机，第二项为目的物理机
	 * LinkedHashMap能够按插入顺序访问元素
	 * */
	public LinkedHashMap<Integer,ArrayList<Integer>> vmSourceDest;
	
	/**
	 * RIAL的构造函数
	 * @param hostVmMap
	 */
	public RIAL(Map<Integer,HashSet<Integer>> hostVmMap){
		ind = new Individual(hostVmMap);
		this.hosts = ind.indHosts;
		this.hostVmMap = ind.hostVmMap;
		this.vmHostMap = ind.vmHostMap;
		
		this.heavyPms = new ArrayList<Integer>();
		this.lightPms = new ArrayList<Integer>();
		this.vmSourceDest = new LinkedHashMap<Integer,ArrayList<Integer>>();
	}
	
	/**
	 * 将host划分为负载过高的集合与负载不高的集合
	 */
	private void divideHosts(){
		
		for(HostDc host:hosts){
			if(host.isOverLoad()){
				heavyPms.add(host.getId());
			}else{
				lightPms.add(host.getId());
			}
		}
	}
	
	/**对负载过高的PM进行迁移操作*/
	public void move(){
		
		divideHosts();		//划分物理机集合
		
		for(int hostId:heavyPms){	//遍历每个负载过高的物理机
			
			HostDc host = hosts.get(hostId);
			
			//1、给每种资源设定权重，区分使用过量的资源和使用不过量的资源
			if(host.getCpuRate()>Sandpiper.cpuThreshold){
				wik[0] = 1/(1-host.getCpuRate());
			}else{
				wik[0] = 1-host.getCpuRate();
			}
			
			if(host.getMemRate()>Sandpiper.memThreshold){
				wik[1] = 1/(1-host.getMemRate());
			}else{
				wik[1] = 1-host.getMemRate();
			}
			
			if(host.getNetRate()>Sandpiper.netThreshold){
				wik[2] = 1/(1-host.getNetRate());
			}else{
				wik[2] = 1-host.getNetRate();
			}
			
			//虚拟机列表
			HashSet<Integer> vms = hostVmMap.get(hostId);
			
			//2、MCDM决策矩阵（Multi-Criteria Decision Making）
			//每一列表示一台虚拟机，每一行表示一种资源（一共三种资源）
			//矩阵中每个元素时某种资源的利用率，是利用率（仅凭利用率决定是否迁移）
			double[][] mcdm = new double[3][vms.size()];
			
			//用vmIds来存储每一列代表的vm编号
			//用comms来存储这个虚拟机与本机其他虚拟机的通信量
			int[] vmIds = new int[vms.size()];
			int[] comms = new int[vms.size()];
			int column = 0;
			for(int vmId : vms){
				VmDc vm = DataSet.vms.get(vmId);
				vmIds[column] = vmId;
				
				mcdm[0][column] = vm.getCpuRate();
				mcdm[1][column] = vm.getMemRate();
				mcdm[2][column] = vm.getNetRate();
				
				//计算通信量
				int comm = 0;
				for(int vmIdTmp:vms){
					comm += DataSet.comMatrix[vmId][vmIdTmp];
				}
				comms[column] = comm;
				
				//列号增1
				column++;
			}
			
			//3、规范化
			nomalizeMatrix(mcdm);
			
			//4、定义最佳迁移VM
			double[] idealVm = new double[3];
			
			for(int i=0;i<wik.length;i++){
				if(wik[i]>1){	//资源利用超过负载，选择最大值
					idealVm[i] = selectMax(mcdm,i);
				}else{			//资源利用未超过负载，选择最小值
					idealVm[i] = selectMin(mcdm,i);
				}
				
			}
			
			//5、将所有column按照欧式距离进行 “升序” 排序，
			//dists表示每一列的欧氏距离值
			double[] dists = new double[vms.size()];
			
			for(int i=0;i<dists.length;i++){
				dists[i] = dist(mcdm,comms,i,idealVm);
			}
			
			ArrayList<Integer> columns = new ArrayList<Integer>();
			
			for(int i=0;i<vms.size();i++){
				columns.add(i);
			}
			Collections.sort(columns,new EuclideanCmp(dists));		//排序
			
			//6、如果物理机一直处于负载过高状态，就选择vm进行迁移
			
			//index表示要迁移的虚拟机的下一个，index所指不需要迁移
			int index = 0;
			while(host.isOverLoad() && index<= columns.size()){
				int vmId = vmIds[columns.get(index)];
				
				//将虚拟机添加到待迁移列表中
				ArrayList<Integer> sourceDest = new ArrayList<Integer>();
				sourceDest.add(host.getId());
				vmSourceDest.put(vmId, sourceDest);
				
				//移除操作
				host.removeVmUpdateResource(vmId);
				hostVmMap.get(host.getId()).remove(vmId);
				index++;
			}
			
			
			//TODO 为每一台要迁移的虚拟机选择目的物理机
			
			for(int vmId:vmSourceDest.keySet()){
				
				//表示此虚拟机已经迁移过了，即已经包含源物理机和目的物理机
				if(vmSourceDest.get(vmId).size()>1){
					continue;
				}
			
				//7、将所有物理机的资源利用率做一个矩阵
				//行表示一种资源，列表示一台物理机关于资源的利用率
				double[][] pmMcdm = new double[3][lightPms.size()];
				
				//记录某一列所表示的物理机Id
				int[] pmIdInColumn = new int[lightPms.size()];
				
				//表示虚拟机与这一台物理机的通信量
				double[] commPm = new double[lightPms.size()];
				
				//记录虚拟机迁移到这台物理机的迁移代价
				double[] Dijps = new double[lightPms.size()];
				
				int columnIndex = 0;
				for(int lightHostId:lightPms){
					
					HostDc lightHost = hosts.get(lightHostId);
					pmMcdm[0][columnIndex] = lightHost.getCpuRate();
					pmMcdm[1][columnIndex] = lightHost.getMemRate();
					pmMcdm[2][columnIndex] = lightHost.getNetRate();
					
					double commPms = 0;
					for(int destVmId:hostVmMap.get(lightHostId)){
						commPms += DataSet.comMatrix[vmId][destVmId];
					}
					
					pmIdInColumn[columnIndex] = lightHostId;	//记录本列所代表的pmId
					commPm[columnIndex] = commPms;				//记录通信代价
					
					//记录迁移代价
					Dijps[columnIndex] = getDijp(vmId, vmSourceDest.get(vmId).get(0), lightHostId);
				
					columnIndex++;
				}
				
				//选择最大通信量
				double maxComm = 0;
				for(int i=0;i<commPm.length;i++){
					if(commPm[i]>maxComm){
						maxComm = commPm[i];
					}
				}
				
				//8.规范化
				nomalizeMatrix(pmMcdm);
	
				//9.选择理想PM，各种资源利用率都比较低
				double[] idealPm = new double[3];
				for(int i=0;i<3;i++){ 
					idealPm[i] = selectMin(pmMcdm,i);
				}
			
				//10、计算lpij，即衡量一个物理机的标准，越小越好
				double[] lpijs = new double[lightPms.size()];
				
				for(int i=0;i<lightPms.size();i++){
					lpijs[i] = getLpij(pmMcdm, idealPm, commPm, Dijps, maxComm, i);
				}
				
				ArrayList<Integer> pmColumns = new ArrayList<Integer>();
				
				for(int i=0;i<lightPms.size();i++){
					pmColumns.add(i);
				}
				
				//11、升序排列
				Collections.sort(pmColumns,new EuclideanCmp(lpijs));
				
				
				//12、选择距离最小的一个作为目的物理机
				for(int col:pmColumns){
					int pmId = pmIdInColumn[pmColumns.get(col)];
					

					//TODO　暂时不考虑由于热迁移导致目的物理机的负载过高，只选择一个最佳的物理机				
					//if(hosts.get(pmId).canHoldAndNotOverLoad(DataSet.vms.get(vmId))){
						vmSourceDest.get(vmId).add(pmId);
						
						 migTime += MigTime.getMigTime(vmId, vmSourceDest.get(vmId).get(0),pmId,hosts);
						
						//更新资源利用及映射信息
						hosts.get(pmId).addVmUpdateResource(vmId);
						hostVmMap.get(pmId).add(vmId);
						vmHostMap.put(vmId,pmId);
						break;
					//}
				}
			
			}//为特定虚拟机选择目的物理机结束
			
		}//处理一台负载过高的物理机结束
		
		
//		for(int vmId:vmSourceDest.keySet()){
//			System.out.print(vmId);
//			System.out.println(":"+vmSourceDest.get(vmId));
//		}
		
		System.out.println("RIAL:");
		System.out.println("comCost:"+Utils.cc.objVal(ind));
		System.out.println("pmCnt:"+Utils.pc.objVal(ind));
		System.out.println("migTime:"+(double)((int)(migTime*100))/100);
		System.out.println("moveCnt: "+vmSourceDest.size());
		System.out.println("balance:"+Utils.ba.objVal(ind));
		
		
	}//迁移算法结束
	
	/**
	 * 计算lpij，即衡量一个pm适合作为迁移目的地的标准
	 * 
	 * @param pmMcdm	资源利用率矩阵
	 * @param idealPm	完美物理机的资源利用，（即资源利用最低）
	 * @param comPm		跟物理机的通信量
	 * @param Dijps		迁移到此物理机的代价	（性能下降）
	 * @param maxCom	最大的通信量，通信量越大越好
	 * @param column	所计算的列
	 * @return
	 */
	private double getLpij(double[][] pmMcdm,double[] idealPm,double[] commPm,double[] Dijps,double maxComm,int column){
		
		double res = 0;
		
		for(int i=0;i<wik.length;i++){
			res += Math.pow(wik[i]*(pmMcdm[i][column]-idealPm[i]), 2);
		}
		
		res += Math.pow(wt*(commPm[column]-maxComm), 2);
		res += Math.pow(wd*(Dijps[column]), 2);
		
		res = Math.sqrt(res);
		return res;
	}
	
	
	/**
	 * 计算迁移代价，跟 距离、迁移时间、cpu利用率成正比
	 * @param vmId 
	 * @param sourceId
	 * @param destId
	 * @return
	 */
	private double getDijp(int vmId,int sourceId,int destId){
		
		VmDc vm = DataSet.vms.get(vmId);
		
		double dip = Utils.getPmDis(sourceId, destId);		//距离
		double Mij = vm.getRam();							//虚拟机使用的内存
		double uijt = vm.getCpuRate();						//虚拟机cpu利用率
		
		double sourceAvail = hosts.get(sourceId).getNetAvail();
		double destAvail = hosts.get(destId).getNetAvail();
		
		// TODO 如何判断两个物理机之间的可用带宽，直接使用两个物理机带宽的最小值
		double Bip = Math.min(sourceAvail, destAvail);
		
		double Dijp = dip*(Mij/Bip)*uijt;
		
		return Dijp;
	}
	
	
	
	/**
	 * 将一个矩阵规范化
	 * @param mcdm
	 */
	private void nomalizeMatrix(double[][] mcdm){
		
		int row = mcdm.length;
		int column = mcdm[0].length;
		
		for(int i=0;i<row;i++){	//对于每一行，求平方和，然后开根号，每个数再除以这个结果
			
			double sqrtSum = 0;
			for(int j=0;j<column;j++){	//求平方和
				sqrtSum += Math.pow(mcdm[i][j], 2);
			}
			sqrtSum = Math.sqrt(sqrtSum);	//开根号
			
			for(int j=0;j<column;j++){	//再除以这个数
				mcdm[i][j] = mcdm[i][j]/sqrtSum;
			}
		}
	}
	
	/**
	 * 根据论文中的公式计算欧氏距离
	 * @param mcdm
	 * @param comm
	 * @param columnId
	 * @return
	 */
	private double dist(double[][] mcdm,int[] comm,int columnId,double[] idealVm){
		double res = 0;
		
		//mcdm[i][columnId]表示第columnId列的对于资源i的利用率
		for(int i=0;i<wik.length;i++){
			res += Math.pow(wik[i]*(mcdm[i][columnId]-idealVm[i]), 2);
		}
		
		res += Math.pow(wt*(comm[columnId]), 2);
		res = Math.sqrt(res);
		return res;
	}
	
	/**
	 * 选择矩阵某一行的最大值
	 * @param mcdm
	 * @param row
	 * @return
	 */
	private double selectMax(double[][] mcdm,int row){
		double max = 0;
		
		for(int i=0;i<mcdm[row].length;i++){
			if(mcdm[row][i]>max){
				max = mcdm[row][i];
			}
		}
		return max;
	}
	
	/**
	 * 选择矩阵某一行的最小值
	 * @param mcdm
	 * @param row
	 * @return
	 */
	private double selectMin(double[][] mcdm,int row){
		double min = Double.MAX_VALUE;
		
		for(int i=0;i<mcdm[row].length;i++){
			if(mcdm[row][i]<min){
				min = mcdm[row][i];
			}
		}
		return min;
	}
	
}

/**
 * 将所有列按照dist升序排列的比较类
 * @author Email:qiuweimin@126.com
 * @date 2016年3月1日
 */
class EuclideanCmp implements Comparator<Integer>{

	public double[] dists;
	
	public EuclideanCmp(double[] dists) {
		this.dists = dists;
	}
	
	@Override
	public int compare(Integer id0, Integer id1) {
		
		if(dists[id0]<dists[id1]){
			return -1;
		}else if(dists[id0]>dists[id1]){
			return 1;
		}
		
		return 0;
	}
}