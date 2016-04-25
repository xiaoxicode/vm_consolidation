package site.mwq.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class FileUtils {

	public static String path = System.getProperty("user.dir");
	
	public static File geneFile = null;
	public static File sandFile = null;
	public static File rialFile = null;
	public static File oriFile = null;
	
	public static PrintWriter genePrint = null;
	public static PrintWriter sandPrint = null;
	public static PrintWriter rialPrint = null;
	public static PrintWriter oriPrint = null;
	
	static{
		geneFile = new File(path+"/gene.txt");
		sandFile = new File(path+"/sand.txt");
		rialFile = new File(path+"/rial.txt");
		oriFile = new File(path+"/original.txt");
		
		//清空原有数据
		geneFile.delete();
		sandFile.delete();
		rialFile.delete();
		oriFile.delete();
		try {
			geneFile.createNewFile();
			sandFile.createNewFile();
			rialFile.createNewFile();
			oriFile.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		try {
			//fileWriter的第二个参数为true表示追加文件
			//printWriter的第二个参数为true表示自动flush
			genePrint = new PrintWriter(new FileWriter(geneFile,true));
			sandPrint = new PrintWriter(new FileWriter(sandFile,true));
			rialPrint = new PrintWriter(new FileWriter(rialFile,true));
			oriPrint = new PrintWriter(new FileWriter(oriFile,true));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 输出一个结果到gene.txt文件
	 * @param res
	 */
	public static void printGene(double[] res){
		String line = "";
		for(int i=0;i<res.length;i++){
			line += res[i]+" ";
		}
		genePrint.println(line);
		genePrint.flush();
	}
	
	/**
	 * 输出一个结果到original.txt文件
	 * @param res
	 */
	public static void printOri(double[] res){
		String line = "";
		for(int i=0;i<res.length;i++){
			line += res[i]+" ";
		}
		oriPrint.println(line);
		oriPrint.flush();
	}
	
	/**
	 * 输出一个结果到sand.txt文件
	 * @param res
	 */
	public static void printSand(double[] res){
		String line = "";
		for(int i=0;i<res.length;i++){
			line += res[i]+" ";
		}
		sandPrint.println(line);
		sandPrint.flush();
	}
	
	/**
	 * 输出一个结果到rial.txt文件
	 * @param res
	 */
	public static void printRial(double[] res){
		String line = "";
		for(int i=0;i<res.length;i++){
			line += res[i]+" ";
		}
		rialPrint.println(line);
		rialPrint.flush();
	}
	
	public static void main(String[] args) {
		double[] a = {12,23,34,34,2,12,9};
		try{
			printRial(a);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
