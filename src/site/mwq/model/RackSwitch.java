package site.mwq.model;

import java.util.ArrayList;

/**
 * 机架交换机
 *
 * @author Email:qiuweimin@126.com
 * @version 2016年1月14日
 */
public class RackSwitch {
	
	public ClusterSwitch clusterSwitch;
	public ArrayList<Host> hosts;
	
	public RackSwitch(){
		this.clusterSwitch = new ClusterSwitch();
		this.hosts = new ArrayList<Host>();
	}
}
