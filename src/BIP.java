//package Chongshou.MS.MCRA;

import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.ArrayList;

public class BIP {
	
	/* Use Cplex to solve BIP model
	 * */
	
	/*---Use C-plex to solve SBIP model-----
	 * */	
	public static void ReadAPM(int[][] AM, int Items, String AMFile) 
			throws IOException{
		
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
	
	
	public static void main(String[] args) throws IOException, IloException{	
		//Memory Setting: -Xms1024m -Xmx10240m		
		String Dir = "D:/Chongshou Li  Folder/My Research Topic" +
				"/Aggregation of Partial Rankings/MS_MCRA_DataSet_5/Data_Set_";	
		//"D:/Chongshou Li  Folder/My Research Topic" +
		//		"/Aggregation of Partial Rankings/MS_MCRA_DataSet_4/Data_Set_";	
		
		String Cplex_BIP_Linear_Information = "D:/Chongshou Li  Folder/My Research Topic" +
						"/Aggregation of Partial Rankings/MS_MCRA_DataSet_5/Cplex_BIP.txt";
		//"D:/Chongshou Li  Folder/My Research Topic" +
		//	 "/Aggregation of Partial Rankings/MS_MCRA_DataSet_4/Cplex_BIP.txt";
		
		
		File fin = new File(Cplex_BIP_Linear_Information);
		FileWriter fw = new FileWriter(fin);
		PrintWriter pw = new PrintWriter(fw);
		pw.println("InstanceNumber\tSolverStatus\tOptimalValue(int)"
				+ "\tOptimalValue(Double)\tOptimalSolutions" + 
				"\tFeasibleSolutions\tStatus\tTime");
		
		double timelimit = 60*10; //10 minutes
		int Instance_Index = 0;
		for(int m = 10; m <= 50; m += 10){//Number Of Items			
			for(int n = 10; n <= 50; n += 10){//Number Of Individual Rankings							
				//read the PCM matrices
				++Instance_Index;
				int[][][]  PCM = new int[n][m][m]; //Individual's pairwise comparison matrix
				System.out.println("Instance:\t" + Instance_Index+ "....");
				System.out.println("\tReading Matrices...");
				String path = Dir + Instance_Index + "_" + m + "_" + n ;				
				for(int l = 0; l < n; ++l){
					String File_APCM = path + "/PCM/_" + l +"_.txt";
					ReadAPM(PCM[l],m,File_APCM);
				}
				
				/*int[][] APCM = new int[m][m];
				for(int i = 0; i < m; ++i){
					for(int j = 0; j < m ; ++j){
						APCM[i][j] = 1;
						for(int l = 0; l < n; ++l){
							if(APCM[i][j] > PCM[l][i][j]){
								APCM[i][j] = PCM[l][i][j];
							}
						}
					}
				}*/
				
			
				System.out.println("\tDone!\n\tBuilding Model....");				
				IloCplex cplex = new IloCplex();					
				//cplex.setParam(IloCplex.IntPa, arg1)
				cplex.setParam(IloCplex.IntParam.AdvInd, 0);
				
				//MIP start; Do not use advanced start information;				
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
				
				
				String cplex_File = path +"/Cplex_BIP_FeasibleSolution.txt";
				File cplex_out = new File(cplex_File);
				FileWriter cplex_fw = new FileWriter(cplex_out);
				PrintWriter cplex_pw = new PrintWriter(cplex_fw);
				
				
				String cplex_R = path + "/Cplex_BIP_OptimalSolutions.txt";
				File cplex_Result = new File(cplex_R);
				FileWriter cplex_fw_r = new FileWriter(cplex_Result);
				PrintWriter cplex_pw_r = new PrintWriter(cplex_fw_r);
				
				System.out.println("\tConstructing variable E....");				
				IloNumVar[] e = new IloNumVar[m];
				for(int i = 0 ; i < m; ++i){
					e[i] = cplex.boolVar();
				}

				System.out.println("\tConstructing variable X....");				
				IloNumVar[][] x = new IloNumVar[m][m];
				for(int i = 0; i < m; ++i){
					for(int j = 0; j < m; ++j){
						x[i][j] = cplex.boolVar();
					}
				}

				System.out.println("\tConstructing Objective Function....");				
				double[]  Obj = new double[m];
				for(int i = 0 ; i < m; ++i){
					Obj[i]  = 1.0;
				}			
				cplex.addMaximize(cplex.scalProd(e, Obj));
				
				int pOne = 1;	
				int nOne = -1;
				
				
				System.out.println("\tConstructing Constraints(5)....");
				//Constraints(5) in BIP model
				for(int i = 0; i < m - 1; ++i){
					for(int j = i + 1; j < m ; ++j){
						cplex.addLe(cplex.sum(cplex.prod(e[i], pOne),
				                              cplex.prod(e[j], pOne),
				                              cplex.prod(x[i][j], nOne),
				                              cplex.prod(x[j][i], nOne)), 
								                    1);
					}
				}	
				

				System.out.println("\tConstructing Constraints(4)....");  
				//Constraints (4) in BIP model				
				for(int i = 0; i < m; ++i){
					for(int j = 0; j < m; ++j){
						//cplex.addLe(cplex.prod(x[i][j], pOne), APCM[i][j]);
						for(int l = 0; l < n; ++l){
							cplex.addLe(cplex.prod(x[i][j], pOne), PCM[l][i][j]);
						}
					}
				}
				
				

				System.out.println("\tConstructing Constraints(2)....");				
				//No circle condition;
				//Constraints (2);
				for(int i = 0; i < m - 1; ++i){
					for(int j = i + 1; j < m; ++j){
						cplex.addLe(cplex.sum(cplex.prod(x[i][j], pOne),
											  cplex.prod(x[j][i], pOne)), 1);
					}
				}

				System.out.println("\tConstructing Constraints(3)....");			
				//Constraints (3);
				for(int i = 0; i < m; ++i){
					for(int j = 0; j < m ; ++j){
						for(int k = 0; k < m ; ++k){
							if(i != j && j != k && i != k ){
								cplex.addLe(cplex.sum(cplex.prod(x[i][k], pOne),
										              cplex.prod(x[k][j], pOne),
										              cplex.prod(x[i][j], nOne)), 1);
							}
						}
					}
				}
				
				System.out.println("\tDone\nSolving...\t");
				Long StartTime = System.nanoTime();
				boolean solveable = cplex.populate();
				Long EndTime = System.nanoTime();					
				double Gap = (EndTime - StartTime)/1000000000.00; 
				
				if(solveable){						
					System.out.println("Successful!\t" + Gap);						
					int numsol = cplex.getSolnPoolNsolns(); //Number
					double[] cd_obj = new double[numsol]; //objective value in the format of double
					int[] ci_obj = new int[numsol];	//objective value in the format of int		
					
					double MaxDObj = Double.MIN_VALUE;
					int    MaxIObj = 0;					
					
					cplex_pw.println("Solution Index\tObjective Value(double)" +
							"\tObjective Value(int)\tSolution(Selected Items)");
					
					for(int k = 0; k < numsol; ++k){						
						cd_obj[k] = cplex.getObjValue(k);
						ci_obj[k] = (int) Math.round(cd_obj[k]);						
						cplex_pw.print(k+"\t"+cd_obj[k]+"\t"+ci_obj[k]+"\t");
						double[] c_v = cplex.getValues(e,k); //value of vector E;
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
					cplex_pw_r.print("Optimal Solution Index\tSolution\n");
					for(Integer solution:OpSIndices){
						double[] c_v = cplex.getValues(e, solution);
						cplex_pw_r.print(solution);
						for(int j = 0; j < c_v.length; ++j){
							if(c_v[j] > 0.499){
								cplex_pw_r.print("\t"+j);
							}
						}
						cplex_pw_r.print("\n");
					}
					
					pw.println(Instance_Index+"\t"+cplex.getStatus()+"\t"+MaxIObj+"\t"+MaxDObj+"\t"+opts
							+"\t"+numsol+"\tSuccessful\t"+Gap);		
					
					
					String cplex_X_Dir = path + "/PCM/Cplex_BIP_X_Optimal_Matrices";
					File X_Dir = new File(cplex_X_Dir);
					X_Dir.mkdir();
					
					for(Integer solution:OpSIndices){
						
						File cplex_X = new File(cplex_X_Dir + "/_" + solution + "_.txt");
						FileWriter cplex_fw_X = new FileWriter(cplex_X);
						PrintWriter cplex_pw_X = new PrintWriter(cplex_fw_X);	
						
						for(int i = 0; i < m; ++i){
							double[] cx = cplex.getValues(x[i], solution);
							for(int j = 0; j < cx.length - 1; ++j){
								if(cx[i] > 0.4999){
									cplex_pw_X.print("1\t");
								}
								else{
									cplex_pw_X.print("0\t");
								}
							}
							
							if(cx[cx.length - 1] > 0.499){
								cplex_pw_X.println("1\t");
							}
							else{
								cplex_pw_X.println("0\t");
							}
						}											
						cplex_pw_X.close();		
					}										
				}
				else{
					System.out.println("Failure!");
					cplex_pw_r.println("Fail to solve it!");
					pw.println("-\tFailure\t-");
				}
				cplex_pw.close();
				cplex_pw_r.close();	
			}
		}		
		pw.close();	
	}
}
