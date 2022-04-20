//package Chongshou.MS.MCRA;

import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;


public class SBLP {
	
	/*Use Cplex to solve Linear relaxed SBIP model*/	
	
	public static void ReadAPM(int[][] AM, int Items, String AMFile) throws IOException{
		File fin = new File(AMFile);
		FileReader fir = new FileReader(fin);
		LineNumberReader stdin = new LineNumberReader(fir);
		String line;
		int row_Index = 0;		 
		while((line = stdin.readLine())!= null){
			String[] words = line.split("\t");
			for(int i = 0; i < Items; ++i){
				AM[row_Index][i] = Integer.valueOf(words[i]);
			}
			++row_Index; 
		}
		stdin.close();
	}
	
	public static void main(String[] args) throws IOException{
		
		String Dir = "D:/Chongshou Li  Folder/My Research Topic" +
				"/Aggregation of Partial Rankings/MS_MCRA_Data/Data_Set_";		
		/*
		  Long startTime;
		  Long endTime;
		  Long gap;
		  double gap_seconds;
		*/
		
		double low = 0;
		double upper = 1;
		
		String Cplex_BIP_Linear_Information = "D:/Chongshou Li  Folder/My Research Topic" +
				"/Aggregation of Partial Rankings/MS_MCRA_Data/Cplex_BIP_Linear.txt";
		
		
		File fin = new File(Cplex_BIP_Linear_Information);
		FileWriter fw = new FileWriter(fin);
		PrintWriter pw = new PrintWriter(fw);
		pw.println("InstanceNumber\tOptimalValue\tStatus\tIsInteger");
		
		int Instance_Index = 1;
		for(int m = 100; m <= 600; m += 100){//Number Of Items
			int[][] APM = new int[m][m];
			for(int n = 100; n <= 600; n += 100){//Number Of Individual Rankings
				
				String path = Dir + Instance_Index + "_" + m + "_" + n;
				String File_APCM = path + "/APCM.txt";
				ReadAPM(APM, m, File_APCM);
				
				String cplex_File = path + "/cplex_BIP_Linear_Result.txt";
				File cplex_out = new File(cplex_File);
				FileWriter cplex_fw = new FileWriter(cplex_out);
				PrintWriter cplex_pw = new PrintWriter(cplex_fw);
				
				try{
					IloCplex cplex = new IloCplex();					
					IloNumVar[] e = new IloNumVar[m];
					for(int i = 0 ; i < m; ++i){
						e[i] = cplex.numVar(low,upper);
					}
					double[]  Obj = new double[m];
					for(int i = 0 ; i < m; ++i){
						Obj[i]  = 1.0;
					}			
					cplex.addMaximize(cplex.scalProd(e, Obj));
					
					int pOne = 1;
					
					IloRange[][] rng_nc = new  IloRange[m][m];
					for(int i = 0; i < m - 1; ++i){
						for(int j = i+1; j < m ; ++j){
							rng_nc[i][j] = cplex.addLe(cplex.sum(cplex.prod(e[i], pOne),
									              cplex.prod(e[j], pOne)), (APM[i][j]+APM[j][i] + 1));
						}
					}	
					
					System.out.println("Solving Instance:\t" + Instance_Index+ "....");
					boolean solveable = cplex.solve();
					pw.print(Instance_Index+"\t");
					
					if(solveable){
						System.out.println("Successful!");						
						cplex_pw.println("Solution status = " + cplex.getStatus());
						cplex_pw.println("Solution value = " + cplex.getObjValue());
						boolean IsInteger = true;
						for(int i = 0; i < m ; ++i){
							double ce = cplex.getValue(e[i]);
							cplex_pw.println(i + "\t" + cplex.getValue(e[i]));
							if(ce > 0.000001 && ce < 0.9999){
								IsInteger = false;
							}
						}
						pw.println(cplex.getObjValue()+"\tSuccessful\t"+IsInteger);						
					}
					else{
						System.out.println("Failure!");
						cplex_pw.println("Fail to solve it!");
						pw.println("-\tFailure\t-");
					}
				}
				catch (IloException e){
					pw.println(Instance_Index + "\t-\tException\t-");
					cplex_pw.println("Concert exception '" + e + "' caught");			
				}
				cplex_pw.close();
				++Instance_Index; 
			}
		}		
		pw.close();
		
	}

}
