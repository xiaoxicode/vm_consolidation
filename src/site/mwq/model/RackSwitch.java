package site.mwq.model;

import java.util.ArrayList;

import site.mwq.cloudsim.HostDc;

/**
 * 机架交换机
 *
 * @author Email:qiuweimin@126.com
 * @version 2016年1月14日
 */
public class RackSwitch {
	
	public ClusterSwitch clusterSwitch;
	public ArrayList<HostDc> hosts;
	
	public RackSwitch(){
		this.clusterSwitch = new ClusterSwitch();
		this.hosts = new ArrayList<HostDc>();
	}
}
