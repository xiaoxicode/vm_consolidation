package site.mwq.dependence;

import java.util.HashSet;

/**
 * 一次活动表示一次迁移
 * @author Email:qiuweimin@126.com
 * @date 2016年1月17日
 */
public class Activity {
	public String label;					//活动标签,如A1
	public double migTime;					//活动持续时间
	public HashSet<Activity> depends;		//依赖关系
	
	public HashSet<Integer> relatedHosts;
	
	//虚拟机id, 起始host与目的host
	public int vmId;
	public int from;
	public int to;
	
	/**
	 * 一个vm,源节点，目的节点，唯一决定一个活动
	 * 同时初始化依赖关系和 涉及到的host集合
	 * @param vmId
	 * @param from
	 * @param to
	 */
	public Activity(int vmId,int from,int to){
		this.depends = new HashSet<Activity>();
		this.relatedHosts = new HashSet<Integer>();
		
		this.vmId = vmId;
		this.from = from;
		this.to = to;
		relatedHosts.add(from);
		relatedHosts.add(to);
	}

	/**
	 * 添加对一个活动的依赖
	 * @param act 所依赖的活动
	 */
	public void depend(Activity act){
		this.depends.add(act);
		this.relatedHosts.add(act.from);
		this.relatedHosts.add(act.to);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + from;
		result = prime * result + to;
		result = prime * result + vmId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Activity other = (Activity) obj;
		if (from != other.from)
			return false;
		if (to != other.to)
			return false;
		if (vmId != other.vmId)
			return false;
		return true;
	}

	@Override
	public String toString() {
		
		String str = "Activity [vmId=" + vmId + ", from="
				+ from + ", to=" + to + "]";
		
		for(Activity act: depends){
			str += "("+act.vmId+" "+act.from+" "+act.to+") ";
		}
		
		return str;
	}

}
