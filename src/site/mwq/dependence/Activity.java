package site.mwq.dependence;

import java.util.ArrayList;

public class Activity {
	public String label;	//活动标签,如A1
	public double duration;		//活动持续时间
	public ArrayList<Activity> depends;		//依赖关系
	
	public Activity(String label,double duration){
		this.label = label;
		this.duration = duration;
		this.depends = new ArrayList<Activity>();
		
	}
}
