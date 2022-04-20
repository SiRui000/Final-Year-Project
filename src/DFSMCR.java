//package Chongshou.MS.MCRA;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.ArrayList;



/**
 * This Java file is used for paper 2021. 
 * 
 *           ----Note by CsLi, 12 Sep 2021
 * This is not developed in 2021 and was developed before. 
 * */

public class DFSMCR {
	
	public int Items;
	//public int IndividualRankings;
	
	public int[][] APM = null;
	public int[]   IR = null; 
		
	public int     Length = 0;
	public int     Optimals = 0;
	public ArrayList<ArrayList<Integer>>  ResultSequence = null;
	
	
	
	public DFSMCR(){
	}
	
	public void Initialization(int NumberofItems){
		this.Items = NumberofItems;
		this.APM = new int[this.Items + 1][this.Items + 1];
		this.IR = new int[this.Items + 1];
		this.ResultSequence = new ArrayList<ArrayList<Integer>>();
		for(int i = 0 ; i < this.Items + 1; ++i){
			for(int j = 0; j < this.Items + 1; ++j){
				this.APM[i][j] = 0;
			}
		}
		for(int i = 0 ; i < this.Items + 1; ++i){
			this.IR[i] = -1;
		}	
	}
	
	/*Sub-Procedure: Depth-First Search For Labeling Rank*/
	public void DFSLR(int currentItem, int cRank){
		if(this.IR[currentItem] > cRank)
			return ;
		this.IR[currentItem] = cRank;
		for(int i = 0 ; i < this.Items; ++i){
			if(this.APM[currentItem][i] == 1){
				this.DFSLR(i, cRank + 1);
			}
		}
	}
	
	public void Main_DepthFirstSearchRankAggregation(){
		
		//LBinaryComparison[l][i][j]:   
		//1 if i > j in ranking l;
		//0 otherwise.
		
		/*Step Two: Compute In Degree and Out Degree*/
		int[] ID = new int[this.Items];
		int[] OD = new int[this.Items];
		for(int i = 0 ; i < this.Items; ++i){
			ID[i] = 0;
			OD[i] = 0;
			for(int j = 0 ; j < this.Items; ++j){
				ID[i] += this.APM[j][i];
				OD[i] += this.APM[i][j];				
			}
		}
				
		/*Step Three: Construct a tree by adding a new root node*/
		for(int i = 0; i < this.Items; ++i){
			if(OD[i] > 0 && ID[i] == 0){
				this.APM[this.Items][i] = 1;
			}
		}
		
		/*Step Four: Label Item Rank -1*/
		this.DFSLR(this.Items, 0);  //root node is this.Items(Number of Items)
		
		
		/*Step Five: Sort Item by its Rank in non-decreasing order*/
		int[] IROrder = new int[this.Items + 1];	
		Tool Sorter = new Tool();		
		Sorter.GetIncreasingOrder(this.IR, IROrder);
		this.Length = IR[IROrder[this.Items]];
		this.ResultSequence.clear();
		if(this.Length <= 1){
			return ;
		}
		int K = -1;
		for(int i = 0; i < this.Items + 1; ++i){
			if(IROrder[i] == this.Items){
				K = i;
				break ;
			}
		}
		
		ArrayList<Integer> SubRanking = new ArrayList<Integer>();
		SubRanking.clear();
		this.DFSGLS(K, SubRanking, IROrder);
		this.Optimals = this.ResultSequence.size();
	}
	
	/*Sub-Procedure: Depth-First Search for Generating Longest SubRankings*/
	public void DFSGLS(int Index, ArrayList<Integer> cSubRanking, int[] Order){
		int currentItem = Order[Index];
		
		if(this.IR[currentItem] == this.Length){
			ArrayList<Integer> longestorder = new ArrayList<Integer>();
			for(Integer s: cSubRanking){
				longestorder.add(s);
			}
			this.ResultSequence.add(longestorder);
			return ;
		}
		
		for(int j = Index + 1 ; j < this.Items + 1; ++j){
			int cI = Order[j];
			if(this.IR[cI] == ( 1 + this.IR[currentItem]) && this.APM[currentItem][cI] == 1){
				cSubRanking.add(cI);
				this.DFSGLS(j, cSubRanking, Order);
				cSubRanking.remove((Integer)cI);
			}
		}
	}
	
	public void Clear(){
		this.Items = 0;
		this.APM = null;
		this.IR = null;
		this.Length = 0;
		this.ResultSequence = null;
	}
	public void ReadAPCMFromFile(String APCM) throws IOException{
		File fin = new File(APCM);
		FileReader fir = new FileReader(fin);
		LineNumberReader stdin = new LineNumberReader(fir);
		String line;
		int row_Index = 0;
		while((line = stdin.readLine())!= null){
			String[] words = line.split("\t");
			for(int i = 0; i < this.Items; ++i){
				this.APM[row_Index][i] = Integer.valueOf(words[i]);
			}
			++row_Index; 
		}
		stdin.close(); 
	}
	
	public void OutputAPCMToFile(String Result_Ranking) throws IOException{
		File fout = new File(Result_Ranking);
		FileWriter fw = new FileWriter(fout);
		PrintWriter pw = new PrintWriter(fw);
		
		pw.println("The Length of MCR is:\t" + this.Length);
		pw.println("Number of Optimal Solutions:\t" + this.Optimals);		
		for(ArrayList<Integer> MCR: this.ResultSequence){
			for(Integer e: MCR){
				pw.print(e+"\t");
			}
			pw.print("\n");
		}
		pw.close(); 
	}
	
	
	public static void main(String[] args) throws IOException{
		
		//Number of Items
		int sM = 500;
		int eM = 1000;
		int stepM = 100;
		
		//Number of Individual Rankings
		int sN = 50;
		int eN = 100;
		int stepN = 10;
		
		String Dir = "D:/C科研论文/2020ConstraintedRankAggregation/JavaProgram/MCRADataSet/Data_Set_";
		//"D:/Chongshou Li  Folder/My Research Topic" +
		//				"/Aggregation of Partial Rankings/MS_MCRA_DataSet_5/Data_Set_";				
		//"D:/Chongshou Li  Folder/My Research Topic" +
		//		"/Aggregation of Partial Rankings/MS_MCRA_DataSet_4/Data_Set_";
		

		String solverInformation = "D:/C科研论文/2020ConstraintedRankAggregation"
				+ "/JavaProgram/MCRA_DFS_Solver.txt";
		//"D:/Chongshou Li  Folder/My Research Topic" +
		//				"/Aggregation of Partial Rankings/MS_MCRA_DataSet_5/DFS_Solver.txt";		
		//"D:/Chongshou Li  Folder/My Research Topic" +
		//		"/Aggregation of Partial Rankings/MS_MCRA_DataSet_4/DFS_Solver.txt";
		
		Long startTime;
		Long endTime;
		Long gap;
		double gap_seconds;
		
		File fin = new File(solverInformation);
		FileWriter fw = new FileWriter(fin);
		PrintWriter pw = new PrintWriter(fw);
		pw.println("InstanceNumber\tItems\tIndividualRankings"
				+ "\tLongestLength\tOptimalSoultions\tTime(nanoSecond)\tTime(Second)");
		
		DFSMCR solver = new DFSMCR();
		int Instance_Index = 1;
		
		
		for(int m = sM; m <= eM; m += stepM){//Number Of Items
			for(int n = sN; n <= eN; n += stepN){//Number Of Individual Rankings		
			
				solver.Initialization(m);				
				String path = Dir + Instance_Index + "_" + m + "_" + n;
				String File_APCM = path + "/APCM.txt";
				
				//Get the data from file
				solver.ReadAPCMFromFile(File_APCM);
				
				//Solving....
				System.out.println("Solving Instance " + Instance_Index + "\t.....");
				startTime = System.nanoTime();
				solver.Main_DepthFirstSearchRankAggregation();
				endTime = System.nanoTime();
				gap = endTime - startTime;				
				gap_seconds = gap/1000000000.0; 				
				System.out.println("Done!");
				//Done!				
				
				String Result_File = path + "/DFS_Result.txt";
				solver.OutputAPCMToFile(Result_File);
				pw.println(Instance_Index+"\t"+m+"\t"+n+"\t"+solver.Length+"\t"+solver.Optimals+"\t"+gap+"\t"+gap_seconds);
				++Instance_Index; 
				solver.Clear();
			}
		}
		pw.close();		
	}	
}
