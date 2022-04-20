import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.TreeSet;

//This file checks the results....

//Double check the results and 
//see whether it is correct or not!


public class ResultChecking {
	
	public static void main(String[] args) throws IOException{
		
		String Dir = "D:/CA科研论文/2020ConstraintedRankAggregation/JavaProgram/";
		String output = "D:/CA科研论文/2020ConstraintedRankAggregation/JavaProgram/MCRA_DP_SIP_Result.txt";
		//String output = "D:/C科研论文/2020ConstraintedRankAggregation
		///JavaProgram/MCRA_DP_SIP_Result.txt";				

		
		int totalIntances = 36;		
		File fout = new File(output);
		FileWriter fw = new FileWriter(fout);
		PrintWriter pw = new PrintWriter(fw);
		
		//First Read the Ground Truth Data Information		
		String g_f = Dir + "MCRADataSet/DataInformation.txt";
		File fin_g = new File(g_f);
		FileReader fir_g = new FileReader(fin_g);
		LineNumberReader stdin_g = new LineNumberReader(fir_g);
		
		int[] g_len = new int[totalIntances];
		int[] g_Opts = new int[totalIntances];
		String line_gr = stdin_g.readLine();
		int c_I = 0;
		while((line_gr = stdin_g.readLine())!= null){
			String[] words = line_gr.split("\t");
			g_len[c_I] = Integer.parseInt(words[3].trim());
			g_Opts[c_I] = Integer.parseInt(words[6].trim());
			++c_I;
		}		
		stdin_g.close();
		
		//Second, read the DFS overall solving information
		//String DFS_f = Dir + "/MCRA_Cplex_S_BIP.txt";
		String DFS_f = Dir + "/MCRA_DFS_Solver.txt";
		File fin_dfso = new File(DFS_f);
		FileReader fir_dfso = new FileReader(fin_dfso);
		LineNumberReader stdin_dfso = new LineNumberReader(fir_dfso);
		
		//dfs_len: the optimal length 
		int[] dfs_len = new int[totalIntances];
		//dfs_Opts: the number of optimal solutions
		int[] dfs_Opts = new int[totalIntances];
		
		c_I = 0;
		String line_dfso = stdin_dfso.readLine(); //read out the first title line		
		while((line_dfso = stdin_dfso.readLine())!= null){
			String[] words = line_dfso.split("\t");
			dfs_len[c_I] = Integer.parseInt(words[3].trim());
			dfs_Opts[c_I] = Integer.parseInt(words[4].trim());
			++c_I;
		}		
		stdin_dfso.close();
		
		
		//String DFS_SBIPo = Dir + "/MCRA_DFS_Solver.txt";
		String DFS_SBIPo = Dir + "/MCRA_Cplex_S_BIP.txt";
		File fin_SBIPo = new File(DFS_SBIPo);
		FileReader fir_SBIPo = new FileReader(fin_SBIPo);
		LineNumberReader stdin_SBIPo = new LineNumberReader(fir_SBIPo);
		
		int[] SBIP_len = new int[totalIntances];
		int[] SBIP_Opts = new int[totalIntances];
		
		c_I = 0;
		String line_SBIPo = stdin_SBIPo.readLine(); //read out the first title line		
		while((line_SBIPo = stdin_SBIPo.readLine())!= null){
			String[] words = line_SBIPo.split("\t");
			SBIP_len[c_I] = Integer.parseInt(words[2].trim());
			//System.out.println(SBIP_len[c_I]);
			SBIP_Opts[c_I] = Integer.parseInt(words[4].trim());
			++c_I;
		}		
		stdin_SBIPo.close();
		
		System.out.println("Checking OverAll Inforamtion!");
		pw.println("Checking OverAll Inforamtion!");
		boolean DFS_Right = true;
		boolean SBIP_Right = true;
		
		for(int i = 0; i < totalIntances; ++i){
			if(dfs_len[i]!= g_len[i] || dfs_Opts[i] != g_Opts[i]){
				DFS_Right = false;
				System.out.println("DFS Eorror:\t"+ "Instance:\t" + (i+1));
				pw.println("DFS Eorror:\t"+ "Instance:\t" + (i+1));
			}
			if(SBIP_len[i] != g_len[i] || SBIP_Opts[i] != g_Opts[i]){
				SBIP_Right = false;
				System.out.println("SBIP Eorror:\t"+ "Instance:\t"+ (i+1));
				pw.println("SBIP Eorror:\t"+ "Instance:\t"+ (i+1));
			}		
		}
		pw.println("DFS Result:\t"+DFS_Right);
		pw.println("SBIP Result:\t"+SBIP_Right);
		System.out.println("Done!");
		
		
		int Instance_Index = 1;
		for(int m = 500; m <= 1000; m = m + 100){
			for(int n = 50; n <= 100; n = n + 10){
				
				pw.println("Instance Number:\t"+Instance_Index+".........");
				System.out.println("Instance Number:\t"+Instance_Index+".........");
				
				pw.println("\tReading Ground Truth data.....");
				ArrayList<ArrayList<Integer>> GroundTruth  = new ArrayList<ArrayList<Integer>>();				
				//Read ground-truth rankings from file
				String name_grou = Dir + "/MCRADataSet/Data_Set_" + Instance_Index + "_" + m + "_" + n 
						+ "/GroundTruth.txt";
				File fin_grou = new File(name_grou);
				FileReader fir_grou = new FileReader(fin_grou);
				LineNumberReader stdin_grou = new LineNumberReader(fir_grou);
				String line_g;				
				while((line_g = stdin_grou.readLine())!= null){
					ArrayList<Integer> Rank = new ArrayList<Integer>();
					String[] words = line_g.split("\t");
					for(int i = 0 ; i < words.length; ++i){
						Rank.add(Integer.parseInt(words[i].trim()));
					}
					GroundTruth.add(Rank);
				}							
				stdin_grou.close();
				pw.println("\tDone....");
			
				pw.println("\tReading and Checking DFS Results.....");
				
				//Read the DFS result rankings				
				String name_dfs = Dir + "/MCRADataSet/Data_Set_" + Instance_Index + "_" 
				       + m + "_" + n +"/DFS_Result.txt";
				File fin_dfs = new File(name_dfs);
				FileReader fir_dfs = new FileReader(fin_dfs);
				LineNumberReader stdin_dfs = new LineNumberReader(fir_dfs);
				String line_dfs = stdin_dfs.readLine(); //read the first line
				line_dfs = stdin_dfs.readLine(); //read the second line
				
				boolean AllCorrect = true;
				while((line_dfs = stdin_dfs.readLine())!= null){
					
					String[] words = line_dfs.split("\t");
					int[] Ranking = new int[words.length];
					for(int i = 0; i < words.length; ++i ){
						Ranking[i] = Integer.parseInt(words[i].trim());
					}
					
					boolean IsCorrect = false;					
									
					for(ArrayList<Integer> r: GroundTruth){
						int cI = 0;
						boolean checking = true;						
						for(Integer k: r){
							if((int)k != Ranking[cI]){
								checking = false;
								break;
							}
							++cI;
						}
						if(checking){
							IsCorrect = true;
							break;
						}
					}
					
					if(!IsCorrect){
						System.out.println(Instance_Index+":\tDFS\t"+IsCorrect);
						pw.println("DFS\t"+IsCorrect);
						AllCorrect = false;
					}
				}
				stdin_dfs.close();
				
				if(AllCorrect){
					pw.println("\tAll are:\t"+AllCorrect);
					pw.println("\tDone!");
				}
				else{
					System.out.println(Instance_Index+ "\tDFS\t" +
							"Eorror:\t"+AllCorrect+"........................"); 
					pw.println("Eorror:\t"+AllCorrect+"........................"); 
				}
								
				
				pw.println("\tReading and Checking SBIP Results.....");
				//Read the DFS result rankings				
				String name_SBIP = Dir + "/MCRADataSet/Data_Set_" + Instance_Index 
						+ "_" + m + "_" + n +"/Cplex_S_BIP_Result.txt";
				File fin_SBIP = new File(name_SBIP);
				FileReader fir_SBIP = new FileReader(fin_SBIP);
				LineNumberReader stdin_SBIP = new LineNumberReader(fir_SBIP);
				String line_SBIP = stdin_SBIP.readLine(); //read the first line
				line_SBIP = stdin_SBIP.readLine(); //read the second line
				line_SBIP = stdin_SBIP.readLine(); //read the third line
				line_SBIP = stdin_SBIP.readLine(); //read the fourth line
				line_SBIP = stdin_SBIP.readLine(); //read the fifth line
				
				boolean Allright = true; 
				while((line_SBIP = stdin_SBIP.readLine())!= null){
					String[] words = line_SBIP.split("\t");
					TreeSet<Integer> RS = new TreeSet<Integer>();					
					for(int i = 0; i < words.length; ++i){
						if(!words[i].equals("")){
							RS.add(Integer.parseInt(words[i].trim()));
						}
						else{
							RS.add(-1);
						}
					}
					
					boolean Isright = false;					
					
					for(ArrayList<Integer> r: GroundTruth){
						boolean checking = true;						
						for(Integer k: r){
							if(!RS.contains(k)){
								checking = false;
								break;
							}
						}
						
						if(checking){
							Isright= true;
							break;
						}
					}
					
					if(!Isright){
						System.out.println(Instance_Index+":\tSBIP\t"+Isright);
						
						pw.print("\t\t"+Isright);
						Allright = false;
					}				
				}
				stdin_SBIP.close();
				
				if(Allright){
					pw.println("\tAll are:\t"+Allright);
					pw.println("\tDone!");
				}
				else{
					System.out.println(Instance_Index + ":\tSBIP\tEorror:\t"
								+Allright+"........................"); 
					pw.print("Eorror:\t"+Allright+"........................");
				}
				++Instance_Index; 
				System.out.println("Done!");
			}		
		}
		pw.close();		
	}

}
