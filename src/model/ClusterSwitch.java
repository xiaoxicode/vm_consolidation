package model;

import java.util.ArrayList;

/**
 * 聚集层交换机
 *
 * @author Email:qiuweimin@126.com
 * @version 2016年1月14日
 */
public class ClusterSwitch {
	
	public ArrayList<CoreSwitch> coreSwitchs;
	public ArrayList<RackSwitch> rackSwitchs;
	
	public ClusterSwitch(){
		this.coreSwitchs = new ArrayList<CoreSwitch>();
		this.rackSwitchs = new ArrayList<RackSwitch>();
	}
}
