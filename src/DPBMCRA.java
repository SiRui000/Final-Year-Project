import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
//import java.util.LinkedList;
//import java.util.Queue;
import java.util.Queue;


/* ---- Newly created Algorithm 
 * ---- Current Error is that the optimal length is always less than 1 * 
 * */


public class DPBMCRA {
	
	public int Items;
	//public int IndividualRankings;
	
	public int[][] APM = null;
	public int[]   Dist = null; 
	public int topOrderLen = -1;
		
	public int     Length = 0;
	public int     Optimals = 0;
	public ArrayList<ArrayList<Integer>>  ResultSequence = null;
	
	public DPBMCRA(){
	}
	
	public void Initialization(int NumberofItems){
		this.Items = NumberofItems;
		this.APM = new int[this.Items + 1][this.Items + 1];
		this.Dist = new int[this.Items + 1];
		this.ResultSequence = new ArrayList<ArrayList<Integer>>();
		for(int i = 0 ; i < this.Items + 1; ++i){
			for(int j = 0; j < this.Items + 1; ++j){
				this.APM[i][j] = 0;
			}
		}
		for(int i = 0 ; i < this.Items + 1; ++i){
			this.Dist[i] = 0;
		}	
	}
	
	public void ReadAPCMFromFile(String APCM) 
			throws IOException{
		
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
	
	//Depth-First Search for Generating Maximum Consistent Ranking (DFSMCR)
	public void DFSMCR(int Index, int[] tpOrder, ArrayList<Integer> cSubRanking){
		
		int currentItem = tpOrder[Index];
		//if(currentItem < 0)
		//	return ;
		
		if(this.Dist[currentItem] == Length) {
			ArrayList<Integer> longestorder = new ArrayList<Integer>();
			for(Integer s: cSubRanking){
				longestorder.add(s);
			}
			this.ResultSequence.add(longestorder);
			return ;
		}
		
		//for(int j = Index + 1 ; j < this.Items + 1; ++j){
		for(int j = Index + 1; j < this.topOrderLen; ++j) {
			int cI = tpOrder[j];
			if(this.Dist[cI] == ( 1 + this.Dist[currentItem]) 
			&& this.APM[currentItem][cI] == 1){
				cSubRanking.add(cI);
				DFSMCR(j, tpOrder, cSubRanking);
				cSubRanking.remove((Integer)cI);
			}
		}
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
	
	public void Clear(){
		this.Items = 0;
		this.APM = null;
		this.Dist = null;
		this.Length = 0;
		this.ResultSequence = null;
	}
	public void DynamicProgrammingBasedMCRA(){
		
		//LBinaryComparison[l][i][j]:   
		//1 if i > j in ranking l;
		//0 otherwise.
		
		/*-------Step Two: Compute In Degree and Out Degree-----*/
		int[] ID = new int[this.Items];
		int[] OD = new int[this.Items];
		
		ArrayList[] Adj = new ArrayList[this.Items + 1];
		for(int i = 0; i < this.Items; ++i) {
			Adj[i] = new ArrayList<Integer>();
			for(int j = 0; j < this.Items; ++j){
				if(this.APM[i][j] > 0) {
					Adj[i].add(j);
				}//End-if
			}//End-j
		}//End-i
		
		for(int i = 0 ; i < this.Items; ++i){
			ID[i] = 0;
			OD[i] = 0;
			for(int j = 0 ; j < this.Items; ++j){
				ID[i] += this.APM[j][i];
				OD[i] += this.APM[i][j];				
			}
			//for debug
			//System.out.println("Node Index:\t"+i);
			//System.out.println("Indegree:\t"+ID[i]);
			//System.out.println("Outdegree:\t"+OD[i]);
			//System.out.println("\n");
		}
	
				
		/*-----Step Three: Construct a tree by adding a new root node----*/
		Adj[this.Items] = new ArrayList<Integer>();
		for(int i = 0; i < this.Items; ++i){
			if(OD[i] > 0 && ID[i] == 0){
				Adj[this.Items].add(i);
				//for debug
				//System.out.println("Added Node Index:\t"+i);
				this.APM[this.Items][i] = 1;
				ID[i] = ID[i] + 1;
			}
		}
				
		//Topologically order 
		Queue<Integer> cHeadQueue = new LinkedList<Integer>();
		cHeadQueue.offer(this.Items);
		int[] tp_order = new int[this.Items + 1]; 
		for(int i = 0; i < this.Items + 1; ++i){
			tp_order[i] = -1;
		}
		
		int currentRank = 0;
		//tp_order[currentRank] = this.Items; 
		//++ currentRank;
			
		while(cHeadQueue.size() > 0){
			//System.out.println("\nCurrent Queue:\t"+cHeadQueue.toString());
			int cNode = (int)cHeadQueue.poll();
			tp_order[currentRank] = cNode;
			
			//System.out.println("Current Rank:\t"+currentRank);
			//System.out.println("Current Node:\t"+cNode);
			++ currentRank;
			int currentAdjLength = Adj[cNode].size();
			
			//for debug
			//System.out.println("\nCurrent Queue:\t"+cHeadQueue.toString());
			//System.out.println("Current node index:\t"+cNode);
			//System.out.println("Current AdjList size:\t"+currentAdjLength+"\n");
			for(int i = 0; i < currentAdjLength; ++i){
				int currentNeighbor = (Integer) Adj[cNode].get(i);
				//System.out.println("Current Neighbor:\t" + currentNeighbor);
				ID[currentNeighbor] = ID[currentNeighbor] - 1;
				//System.out.println("Current ID:\t" + ID[currentNeighbor]);
				if(ID[currentNeighbor] == 0){
					cHeadQueue.offer(currentNeighbor);
				}
			}//End-loop-for-i
		}//End-While	
		
		this.topOrderLen = currentRank;		
		int currentMax = -1, sendingOutIndex, receivingIndex;
		int maxLength = -1;
		//for(int i = 1; i < this.Items + 1; ++i) {
		for(int i = 1; i < this.topOrderLen; ++i) {
			currentMax = -1;
			receivingIndex = tp_order[i];
			for(int j = 0; j < i; ++j){
				sendingOutIndex = tp_order[j];
				if(APM[sendingOutIndex][receivingIndex] == 1 
				&& currentMax < (Dist[sendingOutIndex] + 1)) {
					
					currentMax = Dist[sendingOutIndex] + 1;
				}
			}
			
			Dist[receivingIndex] = currentMax;
			if(maxLength < currentMax){
				maxLength = currentMax;
			}
		}//Loop-for
		
		this.Length = maxLength;
		this.ResultSequence.clear();
		ArrayList<Integer> SubRanking = new ArrayList<Integer>();
		SubRanking.clear();
		DFSMCR(0, tp_order, SubRanking);
		this.Optimals = this.ResultSequence.size();
		
		//System.out.println(this.Length+"\t"+this.Optimals);
	}
	
	
	
	public static void main(String[] args) 
			throws IOException {
		//Data Instances for Paper
		//Number of Items
		int sM = 500;
		int eM = 1000;
		int stepM = 100;
		
		//Number of Individual Rankings
		int sN = 50;
		int eN = 100;
		int stepN = 10;
		
		//Input Data Folder
		String Dir = "D:/CA科研论文/2020ConstraintedRankAggregation/JavaProgram"
				+ "/MCRADataSet/Data_Set_";	
		
		//Output Data Folder
		String solverInformation = "D:/CA科研论文/2020ConstraintedRankAggregation"
				+ "/JavaProgram/MCRA_DFS_Solver.txt";
		
		
		/*
		//For debug purpose 
		// Small data instances for debug
		int sM = 6;
		int eM = 6;
		int stepM = 1;
		
		// Number of individual Rankings
		int sN = 5; 
		int eN = 5; 
		int stepN = 1;
		
		String Dir = "D:/CA科研论文/2020ConstraintedRankAggregation/JavaProgram"
				+ "/SimpleExampleForDebug/Data_Set_";	
		String solverInformation = "D:/CA科研论文/2020ConstraintedRankAggregation"
				+ "/JavaProgram/MCRA_DFS_Solver_Small.txt";
		*/
		
		
		Long startTime;
		Long endTime;
		Long gap;
		double gap_seconds;
		
		File fin = new File(solverInformation);
		FileWriter fw = new FileWriter(fin);
		PrintWriter pw = new PrintWriter(fw);
		pw.println("InstanceNumber\tItems\tIndividualRankings"
				+ "\tLongestLength\tOptimalSoultions\tTime(nanoSecond)\tTime(Second)");
		
		DPBMCRA solver = new DPBMCRA();
		int Instance_Index = 1;	
		
		
		for(int m = sM; m <= eM; m += stepM){//Number Of Items
			for(int n = sN; n <= eN; n += stepN){//Number Of Individual Rankings		
				 
				 //int m = 600;
				 //int n = 60;
				 solver.Initialization(m); 
				 String path = Dir + Instance_Index + "_" + m + "_" + n;
				 String File_APCM = path + "/APCM.txt";
					
				//Get the data from file
				solver.ReadAPCMFromFile(File_APCM);
				
				//Solving....
				System.out.println("Solving Instance " + Instance_Index + "\t.....");
				startTime = System.nanoTime();
				solver.DynamicProgrammingBasedMCRA();
				//solver.Main_DepthFirstSearchRankAggregation();
				endTime = System.nanoTime();
				gap = endTime - startTime;				
				gap_seconds = gap/1000000000.0; 				
				System.out.println("Done!");
				
				String Result_File = path + "/DFS_Result.txt";
				solver.OutputAPCMToFile(Result_File);
				pw.println(Instance_Index+"\t"+m+"\t"+n+"\t"+solver.Length+"\t"
							+solver.Optimals+"\t"+gap+"\t"+gap_seconds);
				++Instance_Index; 
				solver.Clear();
			}
		}		
		pw.close();
	}

}
