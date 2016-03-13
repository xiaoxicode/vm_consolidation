package site.mwq.rmi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * 模拟Libvirt类，执行java程序时需要添加sudo
 * @author Email:qiuweimin@126.com
 * @date 2016年2月19日
 */
public class LibvirtSim {

	/**
	 * 迁移接口，调用mig.py python模块，传入相关参数
	 * @param sourcePm
	 * @param destPm
	 * @param vm
	 */
	public static void migrate(String sourcePm,String destPm,String vm){
		String cmd = "python mig.py "+sourcePm+" "+destPm+" "+vm;	
		
		Runtime r = Runtime.getRuntime();
		Process pro = null;
		
		try { 
			pro = r.exec(cmd);
			pro.waitFor();		//调用waitFor方法
			pro.destroy();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 返回正在运行的虚拟机列表
	 * @return
	 */
	public static ArrayList<String> virshList(){
		ArrayList<String> vmNames = new ArrayList<String>();
		String cmd = "./virshList.sh";
		
		Runtime r = Runtime.getRuntime();
		Process pro = null;
		
		try { 
			pro = r.exec(cmd);		//执行shell命令 java执行需要root权限的shell命令，用sudo java Test
			BufferedReader in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
			String line = "";
			in.readLine();			//忽略前两行
			in.readLine();
			while((line=in.readLine()) != null){
				if(line.trim().equals("")){
					continue;
				}
				System.out.println(line);
				String[] strs = line.trim().split("\\s+");	//一定要先trim()再split()
				vmNames.add(strs[1]);
			}	
			in.close();
			pro.destroy();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(vmNames);
		return vmNames;
	}
	
	/**
	 * 动态调整vm的vcpu个数
	 * @param vmName	vm名字
	 * @param cpuNum 	cpu个数
	 */
	public static void virshSetvcpus(String vmName,int cpuNum){
		
		String cmd = "./virshSetvcpus.sh "+vmName+" "+cpuNum;
		
		Runtime r = Runtime.getRuntime();
		Process pro = null;
		
		try { 
			pro = r.exec(cmd);
			pro.waitFor();		//调用waitFor()方法
			pro.destroy();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	
	//测试
	public static void main(String[] args) {
		try{
			//migrate("server004", "server005", "vm1");
			virshList();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
