package site.mwq.resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 采集网络带宽使用率
 * @author Email:qiuweimin@126.com
 * @date 2016年2月19日
 */
public class NetUsage implements ResourceUsage {

	private static NetUsage INSTANCE = new NetUsage();
	private final static float TotalBandwidth = 1000;	//网口带宽,Mbps,使用命令ethtool eth0查看
	
	public static NetUsage getInstance(){
		return INSTANCE;
	}
	
	public String[] getNetData(Runtime r){
		
		Process pro1 = null;
		String command = "cat /proc/net/dev";
		String[] res = null;
		
		try {
			
			pro1 = r.exec(command);
			BufferedReader in1 = new BufferedReader(new InputStreamReader(pro1.getInputStream()));
			String line = null;
			while((line=in1.readLine()) != null){	
				line = line.trim();
				if(line.startsWith("eth0")){
					res = line.split("\\s+"); 
					break;
				}				
			}	
			in1.close();
			pro1.destroy();
		} catch (IOException e) {
			e.printStackTrace();
		}	
		return res;
	}
	
	/**
	 * @Purpose:采集网络带宽使用率
	 * @param args
	 * @return float,网络带宽使用率,小于1
	 */
	@Override
	public double getResUsage() {
		
		double netUsage = 0.0;
		long inSize1 = 0, outSize1 = 0;
		long inSize2 = 0 ,outSize2 = 0;
		
		///
		Runtime r = Runtime.getRuntime();
		long startTime = System.currentTimeMillis();

		String[] data1 = getNetData(r);						//第一次测数据
		inSize1 = Long.parseLong(data1[1]);					//Receive bytes,单位为Byte
		outSize1 = Long.parseLong(data1[9]);				//Transmit bytes,单位为Byte
		
		try {
			Thread.sleep(1000);		/**等待一段时间**/
		} catch (InterruptedException e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
		}
		
		long endTime = System.currentTimeMillis();		
		String[] data2 = getNetData(r);						//第二次测数据
		inSize2 = Long.parseLong(data2[1]);
		outSize2 = Long.parseLong(data2[9]);
		
		if(inSize1 != 0 && outSize1 !=0 && inSize2 != 0 && outSize2 !=0){
			double interval = (double)(endTime - startTime);				//单位为毫秒
			double curRate = (inSize2 - inSize1 + outSize2 - outSize1)*8/(1000*interval); //网口传输速度, 单位为bps bit每秒
			netUsage = curRate/TotalBandwidth;
		}
		
		return netUsage;
	}

	/**
	 * main方法，用于测试
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		while(true){
			System.out.println(NetUsage.getInstance().getResUsage());
			Thread.sleep(5000);
		}
	}
}