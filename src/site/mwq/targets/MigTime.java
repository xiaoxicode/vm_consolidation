package site.mwq.targets;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;

import site.mwq.cloudsim.HostDc;
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
	 * TODO 计算总的迁移时间（累加时间），这个不容易
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
		
		int migLevel = 0;
		
		//TODO key为迁移层次，value为处于这个层次的活动，TreeMap为有序的哈希表，这里自定义了比较器
		//所以调用keySet()的时候默认是按升序排列的
		TreeMap<Integer,ArrayList<Activity>> levelOfMig = null;
		levelOfMig = new TreeMap<Integer,ArrayList<Activity>>(new Comparator<Integer>(){
			@Override 
			public int compare(Integer arg0, Integer arg1) {
				return arg0-arg1;
			}
		});
		
		while(true){
			
			ArrayList<Activity> levelActs = new ArrayList<Activity>();
			for(int hostId:recvHosts.keySet()){
				
				//1、至少包含一个，并且这台物理机并没有迁出活动，一定是可以容纳迁入的（一台）虚拟机的，可以立即执行一个活动
				if(recvHosts.get(hostId).size()>=1 && 
						(!sendHosts.containsKey(hostId)					//不含有活动
								|| sendHosts.get(hostId).size()==0)){	//含有活动，但是已经迁移完毕
					
					Activity act = null;
					for(Activity a:recvHosts.get(hostId)){		//找任意一个活动
						act = a;
						break;
					}
					levelActs.add(act);
					recvHosts.get(act.to).remove(act);					//将这个活动从host的接收列表中删除
					sendHosts.get(act.from).remove(act);	//将这个活动从其发送端删除
				}

			}//查找一层活动结束
		
			
			//添加一层活动
			if(levelActs.size()>0){
				levelOfMig.put(migLevel++, levelActs);
			}
			
			//检查所有的活动是否已经处理
			boolean over = true;
 			for(int hostId:recvHosts.keySet()){
				if(recvHosts.get(hostId).size()!=0){
					over =false;	//至少还有一个活动没有安排migLevel
					break;
				}
			}
			if(over){	//物理机接收列表已经没有活动，已经全部迁移完毕
				break;
			}
			
			//没有处理完毕，且没有处理一个Activity
			//只要至少发生了一次迁移
			//TODO　先这样简单考虑，后期一定要改
			//TODO 后期要改
			if(levelActs.size()==0){
				for(int hostId:recvHosts.keySet()){
					for(Activity act:recvHosts.get(hostId)){  //如果还有虚拟机没有迁移，则迁移
						levelActs.add(act);
					}
				}
				
				levelOfMig.put(migLevel, levelActs);
				break;
			}
		
		}//统计迁移层次结束
		
		List<HostDc> hosts = DataSet.getCopyOfHosts();
		double migTime = 0;
		
		for(int level:levelOfMig.keySet()){
			for(Activity act:levelOfMig.get(level)){
				migTime += getMigTime(act.vmId,act.from,act.to,hosts);
				
				Utils.removeVm(hosts.get(act.from), null, act.vmId);
				Utils.addVm(hosts.get(act.to), null, act.vmId, null);
			}
		}
		
		return (double)((int)(migTime*10))/10;
	}
	
	
	/**
	 * 计算一次迁移持续的时间
	 * @param vmId	迁移的虚拟机
	 * @param from	源物理机id
	 * @param to	目的物理机id
	 * @return
	 */
	public static double getMigTime(int vmId, int from,int to,List<HostDc> hosts){
		
		// performaceModel(double Vmem,double D,double R)
		
		double vmMem = DataSet.vms.get(vmId).getRam();
		double dirtyRate = DataSet.vms.get(vmId).getDirtyRate();
		double pmBandFrom = hosts.get(from).getNetAvail();
		double pmBandTo = hosts.get(to).getNetAvail();
		
		double bandWidth = Math.min(pmBandFrom, pmBandTo);
		
		//System.out.println("vm Mem:"+vmMem+" bandwidth:"+bandWidth);
		
		double[] datas = performaceModel(vmMem, dirtyRate, bandWidth);
		
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
