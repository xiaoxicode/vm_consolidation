package site.mwq.targets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import site.mwq.cloudsim.HostDc;
import site.mwq.dependence.Activity;
import site.mwq.gene.Individual;
import site.mwq.main.DataSet;
import site.mwq.utils.Utils;

/**
 * TODO 总的预测迁移时间 【先放弃】
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
		
		//key为hostId，value为要从这个host移出的vm的 活动
		HashMap<Integer,ArrayList<Activity>> sendHosts = new HashMap<Integer,ArrayList<Activity>>();
		
		for(int vmId:DataSet.vmHostMap.keySet()){
			if(DataSet.vmHostMap.get(vmId) != ind.vmHostMap.get(vmId)){			//如果所在物理节点不一致则创建一个活动
				int from = DataSet.vmHostMap.get(vmId);
				int to = ind.vmHostMap.get(vmId);
				
				Activity activity = new Activity(vmId,from,to);
				acts.add(activity);
				if(sendHosts.containsKey(from)){
					sendHosts.get(from).add(activity);
				}else{
					ArrayList<Activity> activitys = new ArrayList<Activity>();
					activitys.add(activity);
					sendHosts.put(from, activitys);
				}
			}
		}
		
		List<HostDc> hosts = ind.hostInds;
		for(int i=0;i<acts.size();i++){
			Activity act = acts.get(i);
			
			//TODO 目的host不能容纳待迁移的vm，选择一个待迁出的vm作为依赖，目前是选择了第一个，可能不满足条件
			//资源依赖，同一个host同一时间只能有一个Activity，这两种依赖，第一种依赖从当前向前找
			if(!hosts.get(act.to).canHold(DataSet.vms.get(act.vmId))){	
				try{
					act.depend(sendHosts.get(act.to).get(0));
				}catch(NullPointerException e){
					System.err.println("null pointer Exception");
//					System.out.println(DataSet.firstInd.hostVmMap.get(act.to));
//					System.out.println(ind.hostVmMap.get(act.to));
				}
			}
			
			//检查初始host
			for(int j=i-1;j>=0;j--){
				if(acts.get(j).relatedHosts.contains(act.from)){
					act.depend(acts.get(j));
					break;
				}
			}
			
			//检查目的host
			for(int j=i-1;j>=0;j--){
				if(acts.get(j).relatedHosts.contains(act.to)){
					act.depend(acts.get(j));
					break;
				}
			}
			
		}
		
		//打印依赖关系
		for(int i=0;i<acts.size();i++){
			//System.out.println(acts.get(i));
		}
		
		System.out.println("*********");
		//TODO 迁移时间，返回一个随机数
		return ((int)(Utils.random.nextDouble()*10))/10.0;
	}
	
	/**
	 * 
	 * @param Vmem	虚拟机内存
	 * @param D		虚拟机脏页率
	 * @param R		可用带宽 TODO 物理机剩余带宽是动态的，随vm迁移而变化
	 * @return double[] 迁移时间、停机时间、迁移的数据量
	 */
	public double[] performaceModel(double Vmem,double D,double R){
		
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
			
			System.out.print((int)Ti+" ");
			
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
