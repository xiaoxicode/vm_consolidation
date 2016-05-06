package site.mwq.targets;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;

import site.mwq.cloudsim.HostDc;
import site.mwq.cloudsim.VmCluster;
import site.mwq.dependence.Activity;
import site.mwq.gene.Individual;
import site.mwq.main.DataSet;
import site.mwq.utils.Utils;

/**
 * 预测累加迁移时间，（区别整合时间，并行迁移可以使整合时间小于总的迁移时间）
 * 
 * @author E-mail:qiuweimin@126.com
 * @version 创建时间：2015年12月23日 上午11:53:29
 */
public class MigTime implements ObjInterface {
	
	
	/** 内存剩余阈值
	 * 当剩余这些资源时，热迁移不再进行
	 */
	private static final double Vthd = 50;
	
	/**迁移的最大轮数*/
	private static final int max_round = 30;
	
	/** 参数a,b,c是用来计算伽马(r)的参数*/
//	private static final double a = -0.0463;
//	private static final double b = -0.0001;
//	private static final double c = 0.3586;
	
	/**在目的物理机恢复VM的时间，暂不考虑*/
	private static final double Tresume = 0;

	
	/**
	 * 计算总的迁移时间（累加时间）
	 */
	@Override
	public double objVal(Individual ind) {
		
		
		List<Activity> acts = new ArrayList<Activity>();						//活动集合
		
		/**key为hostId，value包含从这个host上移除VM的活动 
		 * 一台物理机的迁出列表*/
		HashMap<Integer,HashSet<Activity>> sendHosts = new HashMap<Integer,HashSet<Activity>>();
		
		/**key为hostId，value包含迁入这个host的活动
		 *一台物理机的接收列表*/
		HashMap<Integer,HashSet<Activity>> recvHosts = new HashMap<Integer,HashSet<Activity>>();

		//1、找出所有的迁移活动，为所有PM确定迁入和迁出列表
		findActSentRevList(ind, acts, sendHosts, recvHosts);
		
		
		//key为迁移层次，value为处于这个层次的活动，TreeMap为有序的哈希表，这里自定义了比较器
		//所以调用keySet()的时候默认是按升序排列的
		TreeMap<Integer,ArrayList<Activity>> levelOfMig = null;
		levelOfMig = new TreeMap<Integer,ArrayList<Activity>>(new Comparator<Integer>(){
			@Override 
			public int compare(Integer arg0, Integer arg1) {return arg0-arg1;}
		});
		
		ArrayList<Activity> swapList = new ArrayList<Activity>();
		//2、寻找可以并行迁移的活动
		findMigLevels(ind,levelOfMig, acts, sendHosts, recvHosts,swapList);
		
		//3、计算累加迁移时间和 整合时间
		List<HostDc> hosts = DataSet.getCopyOfHosts();
		double migTime = 0;
		double consolidTime = 0;
		
		for(int level:levelOfMig.keySet()){
			
			double maxTime = Integer.MIN_VALUE;
			
			for(Activity act:levelOfMig.get(level)){
				double time = getMigTime(act.vmId,act.from,act.to,hosts);
				migTime += time;
				Utils.removeVm(hosts.get(act.from), null, act.vmId);
				Utils.addVm(hosts.get(act.to), null, act.vmId, null);
				if(time>maxTime){
					maxTime = time;
				}
			}
			consolidTime += maxTime;
		}
		
		for(Activity act:swapList){			//时间再调整一下
			double time = getMigTimeSwap(act.vmId,act.from,hosts);
			migTime += 2*time;
			consolidTime += 2*time;
		}
		
		ind.consolidationTime = (double)((int)(10*consolidTime))/10;
		
		return (double)((int)(migTime*10))/10;
	}
	
	/**
	 * 寻找可以并行迁移的活动，组织成不同的迁移层次
	 * @param levelOfMig
	 * @param acts
	 * @param sendHosts
	 * @param recvHosts
	 * @param swapList，被迁移到交换host的activity列表
	 * @return mvNum 在这个函数中处理过的vm数，正常的话等于acts的size()
	 */
	private int findMigLevels(Individual ind,
			TreeMap<Integer,ArrayList<Activity>> levelOfMig,
			List<Activity> acts,
			HashMap<Integer,HashSet<Activity>> sendHosts,
			HashMap<Integer,HashSet<Activity>> recvHosts,
			ArrayList<Activity> swapList){

		int mvNum = 0;
		int migLevel = 0;
		
		//执行并行迁移操作
		//while循环结束的条件为所有PM的迁入列表都为空
		//每一次while循环表示一层可以同时迁移的活动
		while(true){
			
			ArrayList<Activity> levelActs = new ArrayList<Activity>();
			HashSet<Integer> sendHostSet = new HashSet<Integer>();
			HashSet<Integer> recvHostSet = new HashSet<Integer>();
			
			//1、遍历recvHosts列表，寻找可以迁移的虚拟机
			for(int hostId:recvHosts.keySet()){
				
				//迁出列表至少有一个活动
				if(recvHosts.get(hostId).size()>=1){	
					
					//迁入列表至少包含一个迁入活动，并且这台物理机并没有迁出活动，
					//一定是可以容纳迁入的（一台）虚拟机的，可以立即执行一个活动
					if(!sendHosts.containsKey(hostId)					//检查这个host的发送列表，不包含任何发送活动
								|| sendHosts.get(hostId).size()==0){
						Activity act = null;
						for(Activity a:recvHosts.get(hostId)){
							if(!sendHostSet.contains(a.from) && !recvHostSet.contains(a.to)){
								act = a;
								sendHostSet.add(a.from);
								recvHostSet.add(a.to);
								break;
							}
						}
						if(act!=null){ levelActs.add(act); mvNum++; }
					}else{					//这个host的发送列表有一个或多个活动

						VmCluster cluster = new VmCluster();						//将这些vm聚成一个集合
						for(Activity act:sendHosts.get(hostId)){	
							cluster.addVm(act.vmId);
						}
						
						if(ind.indHosts.get(hostId).canHoldCluster(cluster)){		//查看此host能否容纳这些vm
							Activity act = null;
							for(Activity a:recvHosts.get(hostId)){					
								if(!sendHostSet.contains(a.from) && !recvHostSet.contains(a.to)){
									act = a;
									sendHostSet.add(a.from);
									recvHostSet.add(a.to);
									break;
								}
							}
							if(act!=null){ levelActs.add(act);mvNum++; }
						}
					}
				}

			}//查找一层活动结束

			//从列表中删除已经移动的vm
			for(Activity act:levelActs){
				recvHosts.get(act.to).remove(act);						//将这个活动从host的接收列表中删除
				sendHosts.get(act.from).remove(act);					//将这个活动从其发送端删除
			}
			
			//1、size大于0表示至少改动了一个活动，添加一层活动，并结束此次查找
			if(levelActs.size()>0){										
				levelOfMig.put(migLevel++, levelActs);
				continue;		//结束一次查找
			}
			
			//2、一次活动都没有更改
			//检查所有的活动是否已经处理
			boolean over = true;
 			for(int hostId:recvHosts.keySet()){
				if(recvHosts.get(hostId).size()!=0){
					over =false;break;									//至少还有一个活动没有安排migLevel
				}
			}
			if(over){break;}											//物理机接收列表已经没有活动，已经全部迁移完毕
			
			//没有处理完毕，且没有处理一个Activity，即存在环和相互依赖的情况
			for(int hostId:recvHosts.keySet()){
				for(Activity act:recvHosts.get(hostId)){  				//如果还有虚拟机没有迁移，则迁移
					swapList.add(act);
					mvNum++;
					recvHosts.get(act.to).remove(act);						
					sendHosts.get(act.from).remove(act);
					break;
				}
			}
		
		}//统计迁移层次结束
		
		return mvNum;
	}
	
	
	/**
	 * 找出所有的活动，为所有PM确定迁入和迁出列表
	 * @param ind
	 * @param acts
	 * @param sendHosts
	 * @param recvHosts
	 */
	private void findActSentRevList(Individual ind,List<Activity> acts,HashMap<Integer,HashSet<Activity>> sendHosts,
			HashMap<Integer,HashSet<Activity>> recvHosts){
		//1、找出所有的Activity
		for(int vmId:DataSet.vmHostMap.keySet()){
			
			//如果所在物理节点不一致则创建一个活动
			if(DataSet.vmHostMap.get(vmId) != ind.vmHostMap.get(vmId)){	
				
				int from = DataSet.vmHostMap.get(vmId);
				int to = ind.vmHostMap.get(vmId);
				
				Activity activity = new Activity(vmId,from,to);
				acts.add(activity);
				
				if(sendHosts.containsKey(from)){
					sendHosts.get(from).add(activity);
				}else{
					HashSet<Activity> activitys = new HashSet<Activity>();
					activitys.add(activity);
					sendHosts.put(from, activitys);
				}
				
				if(recvHosts.containsKey(to)){
					recvHosts.get(to).add(activity);
				}else{
					HashSet<Activity> activitys = new HashSet<Activity>();
					activitys.add(activity);
					recvHosts.put(to, activitys);
				}
				
			}
		}
	}
	
	/**
	 * 计算一次迁移持续的时间
	 * @param vmId	迁移的虚拟机
	 * @param from	源物理机id
	 * @param to	目的物理机id
	 * @return
	 */
	public static double getMigTime(int vmId, int from,int to,List<HostDc> hosts){
		
		double vmMem = DataSet.vms.get(vmId).getRam();
		double dirtyRate = DataSet.vms.get(vmId).getDirtyRate();
		double pmBandFrom = hosts.get(from).getNetAvail();
		double pmBandTo = hosts.get(to).getNetAvail();
		
		double bandWidth = Math.min(pmBandFrom, pmBandTo);
		
		double[] datas = performaceModel(vmMem, dirtyRate, bandWidth);
		
		//返回的数据中第一项表示迁移时间
		return datas[0];
	}
	
	/**
	 * 迁往Swap物理机时的迁移时间
	 * @param vmId
	 * @param from
	 * @param hosts
	 * @return
	 */
	public static double getMigTimeSwap(int vmId,int from,List<HostDc> hosts){
		double vmMem = DataSet.vms.get(vmId).getRam();
		double dirtyRate = DataSet.vms.get(vmId).getDirtyRate();
		double pmBandFrom = hosts.get(from).getNetAvail();
		
		double[] datas = performaceModel(vmMem, dirtyRate, pmBandFrom);
		
		//返回的数据中第一项表示迁移时间
		return datas[0];
	}
	
	
	/**
	 * 
	 * @param Vmem	虚拟机内存
	 * @param D		虚拟机脏页率
	 * @param R		可用带宽 TODO 物理机剩余带宽是动态的，随vm迁移而变化
	 * @return double[] 迁移时间、停机时间、迁移的数据量
	 */
	public static double[] performaceModel(double Vmem,double D,double R){
		
		double[] res = new double[3];
		double Tmig=0,Tdown=0,Vmig=0;
		
		double Vi = Vmem;
		double Vi1 = 0;
		double Ti = 0;
		double Ti1 = 0;
//		double r = 0;
//		double Wi1 = 0;		//表示可写工作集 writable working set 
		
		for(int i=0;i<max_round;i++){
			Ti = Vi / R;
//			r = a * Ti + b * D + c;
//			Wi1 = r * Ti * D;		//下一轮的可写工作集
//			Vi1 = Ti * D - Wi1;
			Vi1 = Ti * D;
			
			Tmig += Ti;	//累加
			
			Vmig += Vi;
			
			if(Vi1<= Vthd || Vi1 > Vi || i==(max_round-1)){	//最后一轮
				Vi1 = Ti * D;
				Ti1 = Vi1 / R;
				Tdown = Ti1 + Tresume;
				break;
			}
			Vi = Vi1;
		}
		
		res[0] = Tmig;
		res[1] = Tdown;
		res[2] = Vmig;
		return res;
	}

}
