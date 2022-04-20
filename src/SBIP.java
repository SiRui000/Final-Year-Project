/**
 * This Java file is used for paper 2021. 
 * 
 *           ----Note by CsLi, 12 Sep 2021
 * */


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
import java.util.ArrayList;

public class SBIP {

	/* Employing Cplex to solve proposed S-BIP model. 
	 * This program solves proposed S-BIP  model;
	 * */
	
	/*Use Cplex to solve SBIP model*/	
	public static void ReadAPM(int[][] AM, int Items, 
			String AMFile) throws IOException{
		
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
	
	public static void main(String[] args) throws IOException, 
	     IloException{
		
		//Number of Items
		int sM = 500;
		int eM = 1000;
		int stepM = 100;
		
		//Number of Individual Rankings
		int sN = 50;
		int eN = 100;
		int stepN = 10;
		
		String Dir = "D:/CA科研论文/2020ConstraintedRankAggregation/JavaProgram/MCRADataSet/Data_Set_";		

		String Cplex_BIP_Linear_Information = "D:/CA科研论文/2020ConstraintedRankAggregation"
				+ "/JavaProgram/MCRA_Cplex_S_BIP.txt";

		
		File fin = new File(Cplex_BIP_Linear_Information);
		FileWriter fw = new FileWriter(fin);
		PrintWriter pw = new PrintWriter(fw);
		pw.println("InstanceNumber\tSolverStatus\tOptimalValue(int)\tOptimalValue(Double)\tOptimalSolutions" +
				"\tFeasibleSolutions\tStatus\tTime");
		
		double timelimit = 60*10; //10 minutes		
		int Instance_Index = 1;		
		
		for(int m = sM; m <= eM; m += stepM){//Number Of Items
			int[][] APM = new int[m][m];
			for(int n = sN; n <= eN; n += stepN){//Number Of Individual Rankings
				
				IloCplex cplex = new IloCplex();				
				//cplex.setParam(IloCplex.IntPa, arg1)
				cplex.setParam(IloCplex.IntParam.AdvInd, 0); //MIP start; Do not use advanced start information;
				
				//cplex.setParam(IloCplex, arg1)										
				//cplex.setParam(IloCplex.IntParam.NodeFileInd, Conf.CPLEX_NodeFileInd);
				//cplex.setParam(IloCplex.DoubleParam.TreLim, Conf.CPLEX_TreLim);				
				//SolnPoolIntensity: 4: generate all solutions
				cplex.setParam(IloCplex.IntParam.SolnPoolIntensity, 4);
				
				//SolnPoolReplace: 2: Replace the solution which has the worst
				//Objective when the solution pool reaches it capacity;
				cplex.setParam(IloCplex.IntParam.SolnPoolReplace, 1);					
				
				
				//SolnPoolCapacity: maximum number of solutions
				cplex.setParam(IloCplex.IntParam.SolnPoolCapacity, 1000);
					
				
				//http://pic.dhe.ibm.com/infocenter/cosinfoc/
				//v12r2/index.jsp?topic=%2Filog.odms.cplex.help%2FContent%2F
				//Optimization%2FDocumentation%2FCPLEX%2F_pubskel%2FCPLEX923.html
				
				/**
				Sets an absolute tolerance on the objective value for the solutions in the solution pool. 
				Solutions that are worse (either greater in the case of a minimization, 
				or less in the case of a maximization) 
				than the objective of the incumbent solution according to 
				this measure are not kept in the solution pool */					
				cplex.setParam(IloCplex.DoubleParam.SolnPoolAGap, (double)m);
				//cplex.setParam(IloCplex.DoubleParam.SolnPoolAGap, 0.00);
				
				//Sets a relative tolerance on the objective value for the solutions in the solution pool. 
				//Solutions that are worse (either greater in the case of a minimization, 
				//or less in the case of a maximization) 
				//than the incumbent solution by this measure are not kept in the solution pool. 
				//For example, if you set this parameter to 0.01, then solutions worse than the incumbent by 1% or more will be discarded.
				cplex.setParam(IloCplex.DoubleParam.SolnPoolGap, 0.999);					
				//Here we use absolute tolerance and relative tolerance simultaneously;
				//The program will choose the stricter condition to construct the pool;				
									
				cplex.setParam(IloCplex.IntParam.Threads, 1);
				
				
				//Set the maximum time (in seconds) that Cplex can run;
				//current the time is 600 seconds
				cplex.setParam(IloCplex.DoubleParam.TiLim, timelimit);				
				cplex.setParam(IloCplex.IntParam.PopulateLim, 2000000);
				
				/* *
				 we did not set Absolute and Relative MIP gap tolerance;
				 These two values are default;
				 Absolute MIP gap tolerance is: 1e-06;
				 Relative MIP gap tolerance is: 1e-04.
				 * */
				
				//set the Clock-Type to 2 which is the Wall clock time;	
				//then the TiLim can be set in second.
				//cplex.setParam(IloCplex.IntParam.ClockType, 2);				
				cplex.setOut(null);
				
				String path = Dir + Instance_Index + "_" + m + "_" + n;
				String File_APCM = path + "/APCM.txt";
				ReadAPM(APM, m, File_APCM);
				
				String cplex_File = path +"/Cplex_S_BIP_FeasibleSolution.txt";
				File cplex_out = new File(cplex_File);
				FileWriter cplex_fw = new FileWriter(cplex_out);
				PrintWriter cplex_pw = new PrintWriter(cplex_fw);
				
				String cplex_R = path + "/Cplex_S_BIP_Result.txt";
				File cplex_Result = new File(cplex_R);
				FileWriter cplex_fw_r = new FileWriter(cplex_Result);
				PrintWriter cplex_pw_r = new PrintWriter(cplex_fw_r);
				
				IloNumVar[] e = new IloNumVar[m];
				for(int i = 0 ; i < m; ++i){
					e[i] = cplex.boolVar();
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
				Long StartTime = System.nanoTime();
				boolean solveable = cplex.populate();
				Long EndTime = System.nanoTime();					
				double Gap = (EndTime - StartTime)/1000000000.00; 				
				pw.print(Instance_Index+"\t");
				
				if(solveable){							
					System.out.println("Successful!\t" + Gap);						
					int numsol = cplex.getSolnPoolNsolns(); //Number
					double[] cd_obj = new double[numsol];
					int[] ci_obj = new int[numsol];							
					double MaxDObj = Double.MIN_VALUE;
					int    MaxIObj = 0;					
					cplex_pw.println("Solution Index\tObjective Value(double)" +
							"\tObjective Value(int)\tSolution(Selected Items)");
					
					for(int k = 0; k < numsol; ++k){						
						cd_obj[k] = cplex.getObjValue(k);
						ci_obj[k] = (int) Math.round(cd_obj[k]);						
						cplex_pw.print(k+"\t"+cd_obj[k]+"\t"+ci_obj[k]+"\t");
						double[] c_v = cplex.getValues(e,k);
						for(int j = 0; j < c_v.length; ++j){
							if(c_v[j] > 0.000001){
								cplex_pw.print(j+"\t");
							}
						}
						cplex_pw.print("\n");
						
						if(MaxDObj < cd_obj[k]){
							MaxDObj = cd_obj[k];
							MaxIObj = ci_obj[k];
						}
					}
					
					int opts = 0;
					ArrayList<Integer> OpSIndices = new ArrayList<Integer>();
					for(int i = 0; i < numsol; ++i){
						if(ci_obj[i] == MaxIObj){
							++opts;
							OpSIndices.add(i);
						}
					}
					
					cplex_pw_r.println("Solution status :\t" + cplex.getStatus());
					cplex_pw_r.println("Optimal Solution value(int) :\t" + MaxIObj);
					cplex_pw_r.println("Optimal Solution value(double):\t" + MaxDObj);
					cplex_pw_r.println("Number of Optimal Solutions :\t"+ opts);
					cplex_pw_r.print("Optimal Solutions:\n");
					for(Integer solution:OpSIndices){
						double[] c_v = cplex.getValues(e, solution);
						for(int j = 0; j < c_v.length; ++j){
							if(c_v[j] > 0.4999){
								cplex_pw_r.print(j+"\t");
							}
						}
						cplex_pw_r.print("\n");
					}
													
									
					pw.println(cplex.getStatus()+"\t"+MaxIObj+"\t"+MaxDObj+"\t"+opts
							+"\t"+numsol+"\tSuccessful\t"+Gap);						
				}
				else{
					System.out.println("Failure!");
					cplex_pw_r.println("Fail to solve it!");
					pw.println("-\tFailure\t-");
				}
				
				cplex_pw.close();
				cplex_pw_r.close();
				++Instance_Index; 
			}
		}		
		pw.close();		
	}
}
