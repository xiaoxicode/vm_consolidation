package site.mwq.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import site.mwq.cloudsim.HostDc;
import site.mwq.cloudsim.VmDc;
import site.mwq.gene.Individual;
import site.mwq.gene.Pop;
import site.mwq.main.DataSet;
import site.mwq.targets.Balance;
import site.mwq.targets.ComCost;
import site.mwq.targets.MigCnt;
import site.mwq.targets.MigTime;
import site.mwq.targets.Objs;
import site.mwq.targets.PmCnt;

public class Utils {
	
	//求目标值用的
	public static MigCnt mc = new MigCnt();
	public static PmCnt pc = new PmCnt();
	public static ComCost cc = new ComCost();
	public static Balance ba = new Balance();
	public static MigTime mt = new MigTime();
	
	/**随机数生成器，项目所有的随机数均由其生成*/
	public static Random random = new Random(System.currentTimeMillis());
	
	
	/**
	 * 获得两台物理机之间的距离（相隔的交换机的个数）
	 * 该系统物理机架构参考 ComCost类
	 * 使用三层树形交换机架构，分为核心层、聚集层、接入层、机架、物理机
	 * 从上到下，交换机和物理机数目为 2 3 6 36
	 * 
	 * 同一个物理机距离为			0
	 * 同一个机架交换机距离为		1
	 * 同一个聚集交换机距离为		3
	 * 否则距离为				5
	 * @param pmi
	 * @param pmj
	 * @return
	 */
	public static int getPmDis(int pmIdi,int pmIdj){ 
		
		if(pmIdi==pmIdj){	//物理机编号相等，返回0
			return 0;
		}
		
		pmIdi	/= 6;		//TODO 设置一台机架上有10台物理机，一个机架交换机与10台物理机相连
		pmIdj	/= 6;
		
		if(pmIdi==pmIdj){	//机架编号相等，返回1
			return 1;
		}
		
		pmIdi /= 2;			//一个聚集层交换机与两个机架相连
		pmIdj /= 2;
		
		if(pmIdi==pmIdj){	//聚集层交换机编号相等，返回3，否则返回5
			return 3;
		}
		
		return 5;
	}
	
	
	/**
	 * 计算两个虚拟机的通信距离，用经过的交换机个数来衡量
	 * 使用三层树形交换机架构，分为核心层、聚集层、接入层、机架、物理机
	 * 从上到下，交换机和物理机数目为 2 3 6 18
	 * 
	 * 同一个物理机距离为			0
	 * 同一个机架交换机距离为		1
	 * 同一个聚集交换机距离为		3
	 * 否则距离为				5
	 * 
	 * @param i 虚拟机i
	 * @param j 虚拟机j
	 * @return
	 */
	public static int vmDistance(Individual ind, int i,int j){
		
		if(i==j){		//虚拟机编号相等，返回0
			return 0;
		}
		int idi = 0;	//虚拟机i所在的物理机编号
		try{
			idi = ind.vmHostMap.get(i);
		}catch(Exception e){
			System.err.println("error in ComCost");
			System.exit(1);
		}
		int idj = 0;	//虚拟机j所在的物理机编号
		
		try{
			idj	= ind.vmHostMap.get(j);
		}catch (Exception e){
			System.err.println("another error in ComCost");
			System.exit(1);
		}
		
		return Utils.getPmDis(idi, idj);
	}
	
	/**
	 * 打印矩阵
	 * @param matrix
	 */
	public static void disMatrix(int[][] matrix){
		int row = matrix.length;
		int col = matrix[0].length;
		
		for(int i=0;i<row;i++){
			for(int j=0;j<col;j++){
				System.out.print(matrix[i][j]+" ");
			}
			System.out.println();
		}
	}
	
	/**
	 * 打印一个个体的host的资源剩余情况
	 * @param ind
	 */
	public static void disHostAvail(Individual ind){
		for(HostDc host:ind.indHosts){
			System.out.println("Id:"+host.getId()+", mem:"+host.getMemAvail()+", pe:"+host.getPeAvail()+", bw:"+host.getNetAvail());
		}
	}
	
	/**
	 * 打印host vm映射数组
	 * @param hostVmMap
	 */
	public static void disHostVmMap(Map<Integer,HashSet<Integer>> hostVmMap){
		for(int hostId:hostVmMap.keySet()){ 
			System.out.println(hostId+" : "+hostVmMap.get(hostId));
		}
	}
	
	/**
	 * 打印host vm映射数组
	 * @param hostVmMap
	 */
	public static void disHostVmMapDetail(Map<Integer,ArrayList<Integer>> hostVmMap){
		for(int hostId:hostVmMap.keySet()){ 
			System.out.print(hostId+": ");
			System.out.print("pe "+DataSet.hosts.get(hostId).getPeList().size()+" ");
			System.out.print("mem "+DataSet.hosts.get(hostId).getRam()+" ");
			
			List<Integer> vms = hostVmMap.get(hostId);
			System.out.print("[");
			for(int vmId:vms){
				System.out.print(vmId+": pe "+DataSet.vms.get(vmId).getNumberOfPes()+" ");
				System.out.print("mem "+DataSet.vms.get(vmId).getRam()+"; ");
			}
			System.out.println(" ]");
		}
	}
	
	/**
	 * 打印vm host映射数组
	 * @param vmHostMap
	 */
	public static void disVmHostMap(Map<Integer,Integer> vmHostMap){
		for(int vmId:vmHostMap.keySet()){ 
			System.out.println(vmId+" : "+vmHostMap.get(vmId));
		}
	}
	
	
	/**
	 * 打印每个vm的详细信息
	 * @param vms
	 */
	public static void disVms(List<VmDc> vms){
		
		int width = 10;
		
		for(int i=0;i<vms.size();i++){
			
			System.out.println(vms.get(i).getId()+"："+vms.get(i).getNumberOfPes()+","+vms.get(i).getRam());
			
			if(i%width == 0){
				//System.out.println();
			}
		}
	}
	
	
	/**
	 * 打印数组
	 * @param objs
	 */
	public static void disArray(double[] objs){
		for(int i=0;i<objs.length;i++){
			System.out.print(objs[i]+" ");
		}
	}
	
	/**
	 * 打印整个种群的host vm映射
	 */
	public static void disPopu(){ 
		for(int i=0;i<Pop.inds.size();i++){
			
			disHostVmMap(Pop.inds.get(i).hostVmMap);
			System.out.println("------------");
		}
		
		System.out.println("Population size:"+Pop.inds.size());
	}
	
	
	/**
	 * 计算一个个体的各个目标值
	 * @param ind
	 */
	public static void disIndVal(Individual ind){
		System.out.println("migCnt:"+mc.objVal(ind)
				+"  pmCnt:"+pc.objVal(ind)
				+"  comcost:"+cc.objVal(ind)
				+" ban:"+ba.objVal(ind)
				+"  migTime:"+mt.objVal(ind));
	}
	
	/**
	 * 返回一个个体的各个目标值，顺序为：迁移次数 迁移时间 物理机数目 平衡度 通信代价 
	 * @param ind
	 * @return
	 */
	public static double[] getIndVal(Individual ind){
		double[] res = new double[5];
		res[0] = mc.objVal(ind);
		res[1] = mt.objVal(ind);
		res[2] = pc.objVal(ind);
		res[3] = ba.objVal(ind);
		res[4] = cc.objVal(ind);

		return res;
	}
	
	/**
	 * 打印一个个体各个物理机的剩余的资源
	 * @param ind
	 */
	public static void disIndResUsage(Individual ind){
		List<HostDc> indHosts = ind.indHosts;
		
		for(HostDc host:indHosts){
			System.out.println("id:"+host.getId()+" cpu:"+host.getPeAvail()+" mem:"+host.getMemAvail()+" net:"+host.getNetAvail());
		}
		
	}
	
	/**
	 * 打印一台物理机的资源剩余情况
	 * @param host
	 */
	public static void disHostResAvail(HostDc host){
		System.out.println("PM id:"+host.getId()+" cpu:"+host.getPeAvail()+" mem:"+host.getMemAvail()+" net:"+host.getNetAvail());
	}
	
	/**
	 * 打印一台虚拟机的资源利用情况
	 * @param vm
	 */
	public static void disVmResUsage(VmDc vm){
		System.out.println("VM id:"+vm.getId()+" cpu:"+vm.getNumberOfPes()+" mem:"+vm.getRam()+" net:"+vm.getBw());

	}
	
	/**
	 * 移除一台VM，更新hostVM映射，更新主机资源利用信息
	 * @param host			迁出主机
	 * @param hostVmMap		host vm映射，如果为null，则只更新资源信息
	 * @param vmId			虚拟机id
	 */
	public static void removeVm(HostDc host,TreeMap<Integer,HashSet<Integer>> hostVmMap,int vmId){
		
		if(hostVmMap != null){	//为null，只更新资源信息
			hostVmMap.get(host.getId()).remove(vmId);
		}
		host.removeVmUpdateResource(vmId);
	}
	
	/**
	 * 添加一台映射，更新host vm映射，更新主机资源利用信息，更新vmHost映射
	 * @param host			接收虚拟机的主机
	 * @param hostVmMap		host vm映射  如果为null，则值更新资源信息
	 * @param vmId			虚拟机id
	 * @param vmHostMap		vm host映射  如果为null，则值更新资源信息
	 */
	public static void addVm(HostDc host,TreeMap<Integer,HashSet<Integer>> hostVmMap,int vmId,Map<Integer,Integer> vmHostMap){
		
		if(hostVmMap!=null){
			hostVmMap.get(host.getId()).add(vmId);
		}
		if(vmHostMap!=null){
			vmHostMap.put(vmId, host.getId());
		}
		host.addVmUpdateResource(vmId);
	}
	
	/**
	 * 平均内存利用率
	 * @return
	 */
	public static double getAvgMem(Individual ind) {
		double res = 0;
		int count = 0;
		for(int i=0;i<ind.indHosts.size();i++){
			if(ind.hostVmMap.get(i).size()==0){
				continue;
			}
			res += ind.indHosts.get(i).getMemRate();
			count++;
		}
		res /= count;
		return res;
	}

	/**
	 * 获得平均CPU利用率
	 * @return
	 */
	public static double getAvgCpu(Individual ind) {
		double res = 0;
		int count = 0;
		for(int i=0;i<ind.indHosts.size();i++){
			if(ind.hostVmMap.get(i).size()==0){
				continue;
			}
			res += ind.indHosts.get(i).getCpuRate();
			count++;
		}
		res /= count;
		return res;
	}
	
	/**为浮点数保留两位小数*/
	public static double to2(double ori){
		return (double)((int)(ori*10))/10;
	}
	
	/**
	 * 按照偏好，生成一个权重序列，w1+w2+w3+w4+w5=1
	 * 且，w1<w2<w3<w4<w5
	 *                    迁移次数 迁移时间 物理机数目 平衡度 通信代价 
	 * @return
	 */
	public static double[] genWeights(){
		
		double[] weights = new double[5];
		
		int offset = 0;
		double sum = 0;
		for(int i=2;i<weights.length;i++){
			weights[i] = offset+Math.random();
			offset += 1;
			sum += weights[i];
		}
		
//		weights[1] = 0.08;
//		weights[2] = 0.08;
//		weights[3] = 1;
//		weights[4] = 3;
		
//		weights[2] = 0.5;
//		weights[3] = 1.5;
//		weights[4] = 2.5;
		
		for(int i=0;i<weights.length;i++){
			sum += weights[i];
		}
		
		for(int i=0;i<weights.length;i++){
			weights[i] /= sum;
			
			System.out.print(weights[i]+" ");
		}
		System.out.println();
		
		return weights;
	}
	
	/**
	 * 更新一个ind的各个目标值
	 * @param ind
	 */
	public static void updateObjVals(Individual ind){
		for(int j=0;j<ind.objVals.length;j++){	//对ind调用每个目标函数
			ind.objVals[j] = Objs.OBJS[j].objVal(ind);
		}
	}
	
	public static void main(String[] args) {
		genWeights();
	}
}
