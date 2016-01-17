package model;

import java.util.ArrayList;

/**
 * 核心层交换机
 *
 * @author Email:qiuweimin@126.com
 * @version 2016年1月14日
 */
public class CoreSwitch {
	public ArrayList<ClusterSwitch> clusterSwitch;
	
	public CoreSwitch(){
		this.clusterSwitch = new ArrayList<ClusterSwitch>();
	}
}
