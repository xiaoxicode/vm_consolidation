package site.mwq.dependence;

import java.util.ArrayList;

/**
 * 一次活动表示一次迁移
 * @author Email:qiuweimin@126.com
 * @date 2016年1月17日
 */
public class Activity {
	public String label;					//活动标签,如A1
	public double migTime;				//活动持续时间
	public ArrayList<Activity> depends;		//依赖关系
	
	//起始host与目的host
	public int from;
	public int to;
	
	public Activity(String label,double duration){
		this.label = label;
		this.migTime = duration;
		this.depends = new ArrayList<Activity>();
		
	}
}
