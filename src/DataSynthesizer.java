//package Chongshou.MS.MCRA;

/* ------------------ * 
 * This Java file is used for the paper 2021. 
 * ---- Note by CsLi, 12 Sep 2021 
 * ------------------ *
 */

// -Xms4000m -Xmx5000m


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.TreeSet;


public class DataSynthesizer {
	
	
	/* In this data generation program, we consider two preference relationship only: > or < 
	 * The maximum consistent ranking sequences are not distinguished; 
	 * */	
	public int M; //number of items; Indices set {0,1,,...,M-1}; Assume that M >= 100.
	public int N; //number of individual rankings; Indices set{0,1,....,N-1}; Assume that N >= 3;	
//	public double Ratio; //(Upper bound of Length of MCR Lists)/M = Ratio; 
		
	public Random RandomGenerator = null; //random generator;
	private long  RandomSeed;   //seed for current random generator;
	
	public int lenTruth; //length of Maximum Consistent Ranked Lists, lenTruth <= m;	
	public int Optimals; //Number of Optimal solutions;
	//	public int OptimalsForGroups; //Minimum Number of Optimal Solutions such that it can 
	//have groups;
	
		
	//there exist groups of ground truth rankings and in each group, rankings have some common rank positions;
	//Group Index is {0,,...,CommonGroups}
	//Number of Groups are randomly determined;
	public int CommonGroups = 0; //Number of Groups in Ground Truth Rankings which have common items;
	public TreeSet<Integer>[] CommonPosition = null; //For each group rankings, there are common positions
	public int[] ComPs = null; //In group i, there are Comps[i] common rank positions;
	public int[] GroupLabel = null; //Group Number of Ranking Sequence i is GroupLabel[i]
	public int[][] ComItems = null; //ComItems[i][j] = k; Item k is ranked at position j in Group i;
	
	
	//If CommonGroups == 0, this value is equal to FixedOptimals == Optimals;
	//If CommonGroups > 0,  this value is different from Optimals and can be much larger than Optimals;
	public int FixedOptimals; 
	//ArrayList format of CommonPosition; 
	//This Datastructure is used for Output Result Ranking;
	public ArrayList<Integer>[] CommonPositionVector = null; 
	public ArrayList<ArrayList<Integer>> FixedGroudTruths = null;
	
	//Ground Truth Order; 
	//There are Optimals Ground Truth Sequences; length of each is lenTruth; 
	//G_Order[i][j] = k:  in Ground Truth Sequence i; the rank of item k is j;
	//0<= i <= (Optimals - 1);  0 <= j <= (lenTruth - 1);  0 <= k <= (M - 1);
	//i,j and k are all indices.
	public int[][]  G_Order = null;
	public TreeSet<Integer> G_Items_Set = null;	
	
	//R_Data[i][j] = k; in individual ranking i; the rank of item k is j;
	//0 <= i <= (N-1);  0 <= j <= (M - 1); -1 <= k <= (M - 1)
	//if k = -1, it means no item is ranked at that position and positions after it;
	//the first rank(Highest Rank) is 0;
	public int[][]  R_Data = null;	
	
	//Pairwise Comparison Matrix, PCM[i][j][k], representing the preference relationship between any two items
	//0 <= i <= (N-1);  0 <= j <= (M-1);  0 <= k <= (M-1);
	//If the relationship between item j and item k is j > k in individual ranking i, PCM[i][j][k] = true;
	//otherwise, PCM[i][j][k] = false;
	public boolean[][][] PCM = null;
	
	//Aggregated Pairwise Comparison Matrix
	public boolean[][] APCM = null;  
	
	public DataSynthesizer(int NumberOfItems, int NumberOfRankings){
		this.M = NumberOfItems;
		this.N = NumberOfRankings;
	}	
	public DataSynthesizer(){		
	} 	
	public void Initialization(int NumberOfItems, int NumberOfRankings){
		this.M = NumberOfItems;
		this.N = NumberOfRankings;
	}	
	/*Set Random Generator by two approaches */
	/*the first approach is to use current time as seed */
	public void SetRandomGenerator(){
		this.RandomSeed = System.currentTimeMillis();
		this.RandomGenerator = new Random(this.RandomSeed);
	}	
	/*the second approach is to use input parameter as seed */
	public void SetRandomGenerator(long seed){
		this.RandomSeed = seed;
		this.RandomGenerator = new Random(this.RandomSeed);
	}
	
	
	//Generate Data;
	public void GenerateGTLenComOps(){		
		
		this.Optimals = this.RandomGenerator.nextInt(this.M - 2) + 2;//the minimum MCR is 2;
		this.lenTruth = this.RandomGenerator.nextInt(this.N - 2) + 2;//the minimum rankings is 2;
		
		//System.out.println("The optimal rankings:\t"+this.Optimals);
		//System.out.println("The optimal length:\t"+this.lenTruth);
		
		//For small values of testing, comment this part
		while(this.Optimals * this.lenTruth >= (int)(0.8*this.M)){
			this.Optimals = this.RandomGenerator.nextInt(this.M - 2) + 2;
			this.lenTruth = this.RandomGenerator.nextInt(this.N - 2) + 2;	
		}
		
		System.out.println("\tLength of Optimal Solutions:\t"+this.lenTruth);
		System.out.println("\tNumber of Optimal Solutions:\t"+this.Optimals);
		
		double IsCommon = this.RandomGenerator.nextDouble();
		
		//randomly determine how many number of group rankings can have common rank position;
		if(IsCommon > 0.1){
		//There will be no rankings which have the common items
			this.CommonGroups = 0;
			this.FixedOptimals = this.Optimals;
		} 
		else{
			
			int UpperGroups = (int)(this.Optimals/2);				
			//there will be some rankings which have the common items;
			this.CommonGroups = this.RandomGenerator.nextInt(UpperGroups) + 1;			
			this.CommonPosition = new TreeSet[this.CommonGroups];			
			this.ComPs = new int[this.CommonGroups];
			
			for(int i = 0 ; i < this.CommonGroups; ++i){
				int  cPs =  (int) ((int)(this.lenTruth)*this.RandomGenerator.nextFloat());
				while(cPs < 1 || cPs == this.lenTruth){
					cPs =  (int) ((int)(this.lenTruth)*this.RandomGenerator.nextFloat());
				}
				this.ComPs[i] = cPs;		
				this.CommonPosition[i] = new TreeSet<Integer>();//Common Rank Positions for Group i
			}			
			
			//Generate Common Rank Positions
			TreeSet<Integer> currentIndices = new TreeSet<Integer>();
			for(int i = 0; i < this.CommonGroups; ++i){			
				currentIndices.clear();
				for(int j = 0 ; j < this.ComPs[i]; ++j){
					int cindex = this.RandomGenerator.nextInt(this.lenTruth);
					while(currentIndices.contains(cindex)){
						cindex = this.RandomGenerator.nextInt(this.lenTruth);
					}
					this.CommonPosition[i].add(cindex);
					currentIndices.add(cindex);
				}
			}
			
			//Assign group label to every ranking sequence
			this.GroupLabel = new int[this.Optimals];
			for(int kk = 0; kk < this.Optimals; ++kk){
				this.GroupLabel[kk] = -1;				
			}
			
			//for each group, we first assign two generated ranking sequence
			for(int g = 0; g < this.CommonGroups; ++g){
				int One = this.RandomGenerator.nextInt(this.Optimals);
				while(this.GroupLabel[One]!=-1){
					One = this.RandomGenerator.nextInt(this.Optimals);
				}
				this.GroupLabel[One] = g;
				
				int two = this.RandomGenerator.nextInt(this.Optimals);
				while(this.GroupLabel[two] != -1){
					two = this.RandomGenerator.nextInt(this.Optimals);
				}
				this.GroupLabel[two] = g;
			}
			
			//assign a group label to MCR sequence which does not have a group label
			for(int kk = 0; kk < this.Optimals; ++kk){
				if(this.GroupLabel[kk] == -1){
					this.GroupLabel[kk] = this.RandomGenerator.nextInt(this.CommonGroups);
				}
			}			
		}
		System.out.println("\tNumber of Groups:\t"+this.CommonGroups);
	}
	public void GenerateGroundTruthLists(){
		
		this.G_Order = new int[this.Optimals][this.lenTruth];		
		for(int i = 0 ; i < this.Optimals; ++i){
			for(int j = 0; j < this.lenTruth; ++j){
				this.G_Order[i][j] = -1;
			}
		}
		this.G_Items_Set = new TreeSet<Integer>();
		TreeSet<Integer> UsedItems = new TreeSet<Integer>();		
		this.ComItems = new int[this.CommonGroups][this.lenTruth];
		
		//fulfill common rank positions by randomly choosing items 
		for(int g = 0; g < this.CommonGroups; ++g){
			for(int j = 0 ; j < this.lenTruth; ++j){
				this.ComItems[g][j] = -1;
			}
			for(Integer e: this.CommonPosition[g]){
				int Item_index = this.RandomGenerator.nextInt(this.M);
				while(UsedItems.contains(Item_index)){
					Item_index = this.RandomGenerator.nextInt(this.M);
				}
				this.ComItems[g][e] = Item_index;
				UsedItems.add(Item_index);
				this.G_Items_Set.add(Item_index); 
			}
		}
		
		if(this.CommonGroups >= 1 ){
			for(int i = 0; i < this.Optimals; ++i){
				for(int j = 0; j < this.lenTruth; ++j){
					this.G_Order[i][j] = this.ComItems[this.GroupLabel[i]][j];
				}
			}
		}		
		for(int i = 0 ; i < this.Optimals; ++i){					
			for(int j = 0 ; j < this.lenTruth ; ++j){	
				if(this.G_Order[i][j] == -1){
					int currentItemIndex = this.RandomGenerator.nextInt(this.M);
					while(UsedItems.contains(currentItemIndex)){
						currentItemIndex = this.RandomGenerator.nextInt(this.M);
					}
					this.G_Order[i][j] = currentItemIndex;
					UsedItems.add(currentItemIndex);
					this.G_Items_Set.add(currentItemIndex);
				}
			}			
		}		
	}
	public void GenerateFixedGroundTruthLists(){
		if(this.CommonGroups <= 0)
			return;
		
		this.CommonPositionVector = new ArrayList[this.CommonGroups];
		for(int i = 0; i < this.CommonGroups; ++i){
			this.CommonPositionVector[i] = new ArrayList<Integer>();
			for(Integer e: this.CommonPosition[i]){
				this.CommonPositionVector[i].add(e);
			}
		}	
		//Get the FixedOptimals
		int countSolution = 0;
		int c_Group_Size;
		int Segments;
		for(int i = 0; i < this.CommonGroups; ++i){
			c_Group_Size = 0;			
			//System.out.println();
			for(int j = 0; j < this.Optimals; ++j){
				if(this.GroupLabel[j] == i){
					++c_Group_Size;
					//System.out.println("Group:\t"+i+"\tSequence:\t"+j);
				}
			}
			Segments = 0;
			int startIndex = 0;
			for(int cp = 0 ; cp < this.ComPs[i]; ++cp){				
				int currentComm = this.CommonPositionVector[i].get(cp);	
				//System.out.println("StartIndex:\t"+startIndex);
				//System.out.println("currentComm:\t"+currentComm);
				if(startIndex < currentComm){
					++Segments;
					while(startIndex < currentComm){
						++startIndex;
					}
					++startIndex;
				}
				else if(startIndex == currentComm){
					++startIndex;
				}
			}
			
			if(startIndex <= this.lenTruth - 1){
				++Segments;
			}
			countSolution += (int)Math.pow(c_Group_Size, Segments);			
			//System.out.println("\n"+"Segments:\t"+Segments);
			//System.out.println("StartIndex:\t"+startIndex);	
		}		
		this.FixedOptimals = countSolution;
		//System.out.println("Number of solutions:\t"+this.FixedOptimals);
		
		this.FixedGroudTruths = new ArrayList<ArrayList<Integer>>();
		
		ArrayList<Integer> cRL = new ArrayList<Integer>();
		for(int i = 0; i < this.CommonGroups; ++i){
			cRL.clear();
			this.DFS_Optimals(0, 0, i, 0, cRL);
		}
	}
	public void DFS_Optimals(int currentIndex, int seqIndex, int GroupIndex,
			int ComPosIndex, ArrayList<Integer> currentRL){
		
		if(currentIndex >= this.lenTruth){
			ArrayList<Integer> cResult = new ArrayList<Integer>();
			for(Integer e: currentRL){
				cResult.add(e);
			}
			this.FixedGroudTruths.add(cResult);
		}
		else{			
			if(ComPosIndex >= this.ComPs[GroupIndex]){
				for(int i = seqIndex; i < this.Optimals; ++i){
					if(this.GroupLabel[i] == GroupIndex){
						int c_I;
						for(c_I = currentIndex; c_I < this.lenTruth; ++c_I){
							currentRL.add(this.G_Order[i][c_I]);
						}
						
						this.DFS_Optimals(c_I, i, GroupIndex, ComPosIndex, currentRL);
						
						for(c_I = currentIndex; c_I < this.lenTruth; ++c_I){							
							currentRL.remove((Integer)this.G_Order[i][c_I]);
						}
					}
				}
			}
			else{
				int CommonIndex = this.CommonPositionVector[GroupIndex].get(ComPosIndex);
				if(currentIndex == CommonIndex){
					currentRL.add(this.ComItems[GroupIndex][currentIndex]);
					this.DFS_Optimals(currentIndex+1, 0, GroupIndex, ComPosIndex+1, currentRL);
					currentRL.remove((Integer)this.ComItems[GroupIndex][currentIndex]);
				}
				else if(currentIndex < CommonIndex){
					for(int i = seqIndex; i < this.Optimals; ++i){
						if(this.GroupLabel[i] == GroupIndex){
							int c_I;
							for(c_I = currentIndex; c_I < CommonIndex; ++c_I){
								currentRL.add(this.G_Order[i][c_I]);
							}
							this.DFS_Optimals(c_I, i, GroupIndex, ComPosIndex, currentRL);
							for(c_I = currentIndex; c_I < CommonIndex; ++c_I){
								currentRL.remove((Integer)this.G_Order[i][c_I]);
							}
						}
					}					
				}
			}
		}
	}
	public void GenerateIndividualRankings(){		
		
		if(this.CommonGroups == 0){
			this.GenerateDistinguishedIndividualRanking();
			return ;
		}		
		
		ArrayList<Integer>[] Group_Ascending = new ArrayList[this.CommonGroups];	
		for(int i = 0; i < this.CommonGroups; ++i){
				Group_Ascending[i] = new ArrayList<Integer>();		
		}					
		
		for(int i = 0; i < this.CommonGroups; ++i){
			int c_Index;
			int s_Index = 0;
			for(Integer e: this.CommonPosition[i]){				
				for(int j = 0; j < this.Optimals; ++j){
					if(this.GroupLabel[j] == i){
						c_Index = s_Index;
						while(c_Index < this.lenTruth && c_Index < e){
							Group_Ascending[i].add(this.G_Order[j][c_Index]);
							++c_Index;
						}
					}
				}
				Group_Ascending[i].add(this.ComItems[i][e]);
				s_Index = e + 1;
			}
			
			if(s_Index < this.lenTruth){
				for(int j = 0; j < this.Optimals; ++j){
					if(this.GroupLabel[j] == i){
						c_Index = s_Index;
						while(c_Index < this.lenTruth){
							Group_Ascending[i].add(this.G_Order[j][c_Index]);
							++c_Index;
						}
					}
				}
			}
		}		
		ArrayList<Integer>   Aggregated_Groups_Ascending = new ArrayList<Integer>();
		
		ArrayList<Integer> 	 As_Rem_Order = new ArrayList<Integer>();
		for(int i = 0 ; i < this.CommonGroups; ++i){
			for(Integer e: Group_Ascending[i]){
				Aggregated_Groups_Ascending.add(e);
				As_Rem_Order.add(e);
			}
		}		
		
		for(int item = 0 ; item < this.M; ++item){
			if(!this.G_Items_Set.contains(item)){
				As_Rem_Order.add(item);
			}
		}	
		
		
		ArrayList<Integer>[] Group_Descending = new ArrayList[this.CommonGroups];	
		for(int i = 0; i < this.CommonGroups; ++i){
			Group_Descending[i] = new ArrayList<Integer>();		
		}
		for(int i = 0; i < this.CommonGroups; ++i){
			int c_Index;
			int s_Index = 0;
			for(Integer e: this.CommonPosition[i]){				
				for(int j = this.Optimals - 1; j >=0; --j){
					if(this.GroupLabel[j] == i){
						c_Index = s_Index;
						while(c_Index < this.lenTruth && c_Index < e){
							Group_Descending[i].add(this.G_Order[j][c_Index]);
							++c_Index;
						}
					}
				}
				Group_Descending[i].add(this.ComItems[i][e]);
				s_Index = e + 1;
			}		
			
			if(s_Index < this.lenTruth){
				for(int j = this.Optimals - 1; j >=0; --j){
					if(this.GroupLabel[j] == i){
						c_Index = s_Index;
						while(c_Index < this.lenTruth ){
							Group_Descending[i].add(this.G_Order[j][c_Index]);
							++c_Index;
						}
					}
				}
			}
		}	
		
		ArrayList<Integer>   Aggregated_Groups_Dscending = new ArrayList<Integer>();
		ArrayList<Integer> 	 Rem_Order_Ds = new ArrayList<Integer>();		
		for(int item = this.M - 1 ; item >= 0; --item){
			if(!this.G_Items_Set.contains(item)){
				Rem_Order_Ds.add(item);
			}
		}		
		for(int i = this.CommonGroups - 1 ; i >=0; --i){
			for(Integer e: Group_Descending[i]){
				Aggregated_Groups_Dscending.add(e);
				Rem_Order_Ds.add(e);
			}
		}		
		
		//Construct Individual Ranking Data;
		this.R_Data = new int[this.N][this.M];
		for(int i = 0 ; i < this.N; ++i){
			for(int j = 0; j < this.M; ++j){
				this.R_Data[i][j] = -1;
			}
		}
		
		//Randomly choose two rows to store		
		int[] TwoIndices = this.Randomly_Select_K_From_Z(2, this.N);
		int Index1 = TwoIndices[0];
		int Index2 = TwoIndices[1];
		
		for(int i = 0; i < this.M; ++i){
			this.R_Data[Index1][i] = As_Rem_Order.get(i);
			this.R_Data[Index2][i] = Rem_Order_Ds.get(i);
		}
		
		int[] PositionForGround = null;
		int   LengthofGroundTruth = this.G_Items_Set.size();
		double RatioOfNoItem = 1/(this.M - LengthofGroundTruth+1);
		boolean[] IsRemaining = new boolean[this.M];
		
		for(int i = 0; i < this.M; ++i){
			if(this.G_Items_Set.contains(i)){
				IsRemaining[i] = false;
			}
			else{
				IsRemaining[i] = true;
			}
		}
		
		for(int i = 0; i < this.N; ++i){
			if(i != Index1 && i != Index2){
				PositionForGround = this.Randomly_Select_K_From_Z(LengthofGroundTruth, this.M);
				Tool sorter = new Tool();
				sorter.Sort(PositionForGround);
				if(this.RandomGenerator.nextFloat() > 0.5){//choose the ascending order as bases;
					for(int j = 0; j < LengthofGroundTruth; ++j){
						this.R_Data[i][PositionForGround[j]] = Aggregated_Groups_Ascending.get(j);
					}
				}
				else{				
					for(int j = 0; j < LengthofGroundTruth; ++j){//choose the descending order as bases;
						this.R_Data[i][PositionForGround[j]] = Aggregated_Groups_Dscending.get(j);
					}
				}				
				for(int j = 0; j < this.M; ++j){
					if(this.R_Data[i][j] < 0){
						if(this.RandomGenerator.nextDouble() > RatioOfNoItem){
							int currentSelectedItem = this.RandomGenerator.nextInt(this.M);
							while(!IsRemaining[currentSelectedItem]){
								currentSelectedItem = this.RandomGenerator.nextInt(this.M);
							}
							this.R_Data[i][j] = currentSelectedItem;
						}
					}
				}				
				PositionForGround = null;
			}		
		}
			
	}
	
	//If (n*m*m) is too large, this function needs large memory;  
	public void  ConstructPCM(){
			this.PCM = new boolean[this.N][this.M][this.M];
			for(int i = 0; i < this.N; ++i){
				for(int j = 0; j < this.M; ++j){
					for(int k = 0; k < this.M; ++k){
						this.PCM[i][j][k] = false;
					}
				}
			}		
			for(int i = 0 ; i < this.N; ++i){			
				for(int j = 0; j < this.M - 1; ++j){
					for(int k = (j+1); k < this.M; ++k){
						if(this.R_Data[i][j] >= 0 && this.R_Data[i][k] >= 0){
							int Item_Index_1 = this.R_Data[i][j];
							int Item_Index_2 = this.R_Data[i][k];
							this.PCM[i][Item_Index_1][Item_Index_2] = true;
						}
					}
				}
			}
		}  
	public void  ConstructAPCM(){
			//construct this matrix via Lemma 1; 
			this.APCM = new boolean[this.M][this.M];
			
			for(int i = 0 ; i < this.M; ++i){
				for(int j = 0 ; j < this.M; ++j){
					this.APCM[i][j] = true;
				}
			}
			
			for(int i = 0 ; i < this.N; ++i){
				int[][] BinaryMatrix = new int[this.M][this.M];			
				for(int j = 0; j < this.M; ++j){
					for(int k = 0; k < this.M; ++k){
						BinaryMatrix[j][k] = 0;
					}
				}
				for(int j = 0; j < this.M - 1; ++j){
					for(int k = (j+1); k < this.M; ++k){
						if(this.R_Data[i][j] >= 0 && this.R_Data[i][k] >= 0){
							int Item_Index_1 = this.R_Data[i][j];
							int Item_Index_2 = this.R_Data[i][k];
							BinaryMatrix[Item_Index_1][Item_Index_2] = 1;
						}
					}
				}
				
				for(int j = 0; j < this.M; ++j){
					for(int k = 0 ; k < this.M; ++k){
						if(this.APCM[j][k] && BinaryMatrix[j][k] == 0){
							this.APCM[j][k] = false;
						}
					}
				}			
			}		
		}
	
	//Output Generated Data to File
	public void PrintAPCMToFile(String APCMFile) 
			throws IOException{
		
		File fout = new File(APCMFile);
		FileWriter fw = new FileWriter(fout);
		PrintWriter pw = new PrintWriter(fw);
		
		for(int i = 0 ; i < this.M ; ++i){
			for(int j = 0 ; j < this.M; ++j){
				if(this.APCM[i][j]){
					pw.print(1+"\t");
				}
				else{
					pw.print(0+"\t");
				}
			}
			pw.print("\n");
		}
		
		pw.close();
	}
	public void PrintPCMsToFolderFromConstructing(String path) throws IOException{		
		for(int i = 0 ; i < this.N; ++i){
			String fileName = path + "_" + i + "_.txt";
			File fout = new File(fileName);
			FileWriter fw = new FileWriter(fout);
			PrintWriter pw = new PrintWriter(fw);
			
			int[][] BinaryMatrix = new int[this.M][this.M];			
			for(int j = 0; j < this.M; ++j){
				for(int k = 0; k < this.M; ++k){
					BinaryMatrix[j][k] = 0;
				}
			}
			for(int j = 0; j < this.M - 1; ++j){
				for(int k = (j+1); k < this.M; ++k){
					if(this.R_Data[i][j] >= 0 && this.R_Data[i][k] >= 0){
						int Item_Index_1 = this.R_Data[i][j];
						int Item_Index_2 = this.R_Data[i][k];
						BinaryMatrix[Item_Index_1][Item_Index_2] = 1;
					}
				}
			}			
			
			//print the Binary Matrix to File
			for(int j = 0; j < this.M; ++j){
				for(int k = 0; k < this.M - 1; ++k){
					pw.print(BinaryMatrix[j][k]+"\t");
				}
				pw.println(BinaryMatrix[j][this.M -1]);
			}				
			pw.close();			
		}		
	}
	public void PrintPCMsToFolder(String path) throws IOException{
		for(int i = 0 ; i < this.N; ++i){
			String fileName = path + "_" + i + "_.txt";
			File fout = new File(fileName);
			FileWriter fw = new FileWriter(fout);
			PrintWriter pw = new PrintWriter(fw);			
			
			for(int j = 0; j < this.M; ++j){
				for(int k = 0; k < this.M - 1; ++k){
					if(this.PCM[i][j][k]){
						pw.print(1+"\t");
					}
					else{
						pw.print(0+"\t");
					}
				}
				if(this.PCM[i][j][this.M - 1]){
					pw.println(1+"\t");
				}
				else{
					pw.println(0+"\t");
				}
			}				
			pw.close();			
		}		
	}
	

	public void PrintGroundTruth(String FileName) throws IOException{
		File fout = new File(FileName);
		FileWriter fw = new FileWriter(fout);
		PrintWriter pw = new PrintWriter(fw);
		if(this.CommonGroups <= 0){
			for(int i = 0; i < this.Optimals; ++i){
				for(int j = 0; j < this.lenTruth - 1; ++j){
					pw.print(this.G_Order[i][j]+"\t");
				}
				pw.println(this.G_Order[i][this.lenTruth - 1]);
			}
		}
		else{
			for(ArrayList<Integer> Res: this.FixedGroudTruths){
				for(int i = 0; i < Res.size() - 1; ++i){
					pw.print(Res.get(i)+"\t");
				}
				pw.println(Res.get(Res.size()-1));
			}			
		}
		pw.close();
	}
	public void PrintOriginalGroundTruth(String FileName) throws IOException{
		File fout = new File(FileName);
		FileWriter fw = new FileWriter(fout);
		PrintWriter pw = new PrintWriter(fw);	
		for(int i = 0; i < this.Optimals; ++i){
			for(int j = 0; j < this.lenTruth - 1; ++j){
				pw.print(this.G_Order[i][j]+"\t");
			}
			pw.println(this.G_Order[i][this.lenTruth - 1]);
		}	
		pw.close();
	}
	public void  OutputIndividualRankings(String FileName) throws IOException{
		
		File fout = new File(FileName);
		FileWriter fw = new FileWriter(fout);
		PrintWriter pw = new PrintWriter(fw);
		
		boolean firstitem = true;
		for(int i = 0; i < this.N; ++i){
			firstitem = true;
			for(int j = 0; j < this.M; ++j){
				if(this.R_Data[i][j] >= 0){
					if(firstitem){
						pw.print(this.R_Data[i][j]);
						firstitem = false;
					}
					else{
						pw.print("\t"+this.R_Data[i][j]);
					}
				}
			}
			pw.print("\n");
		}		
		pw.close();		
	}
	
	
	public void GenerateDistinguishedIndividualRanking(){
		this.R_Data = new int[this.N][this.M];
		for(int i = 0 ; i < this.N; ++i){
			for(int j = 0; j < this.M; ++j){
				this.R_Data[i][j] = -1;
			}
		}
		ArrayList<Integer>  Order_Ascending = new ArrayList<Integer>();
		ArrayList<Integer>  Ground_Ascending = new ArrayList<Integer>();
		
		ArrayList<Integer>  Order_Descending = new ArrayList<Integer>();
		ArrayList<Integer>  Ground_Descending = new ArrayList<Integer>();
		
		/* Construct two full ranking sequences such that the two relationships between any two items
		 * from different ground truth sequences in the first two individual rankings are opposite
		 **/		
		for(int i = 0 ; i < this.Optimals; ++i){
			for(int j = 0; j < this.lenTruth; ++j){
				Order_Ascending.add(this.G_Order[i][j]);
				Ground_Ascending.add(this.G_Order[i][j]);
			   //System.out.print(this.G_Order[i][j]+"\t");
			}
		}
		
		//add the items at the end of the first ground truth which are not used 
		//by Ground Truth Ranking Sequence Constructing
		for(int i = 0; i < this.M; ++i){
			if(!this.G_Items_Set.contains(i)){
				Order_Ascending.add(i);
			}
		}
		
		//System.out.print("\n");		
		//add the items at the front of the second ground truth which are not used 
		//by Ground Truth Ranking List Constructing;
		for(int i = this.M - 1 ; i >= 0; --i){
			if(!this.G_Items_Set.contains(i)){
				Order_Descending.add(i);
			}
		}		
		for(int i = this.Optimals - 1 ; i >= 0; --i){
			for(int j = 0; j < this.lenTruth; ++j){
				Order_Descending.add(this.G_Order[i][j]);
				Ground_Descending.add(this.G_Order[i][j]);
				//System.out.print(this.G_Order[i][j]+"\t");
			}
		}
		//System.out.println(Order_Ascending.size());
		//System.out.println(Order_Descending.size());
		
		
		//Randomly choose two rows to store		
		int[] TwoIndices = this.Randomly_Select_K_From_Z(2, this.N);
		int Index1 = TwoIndices[0];
		int Index2 = TwoIndices[1];
		
		for(int i = 0; i < this.M; ++i){
			this.R_Data[Index1][i] = Order_Ascending.get(i);
			this.R_Data[Index2][i] = Order_Descending.get(i);
		}
		
		int[] PositionForGround = null;
		int   LengthofGroundTruth = this.G_Items_Set.size();
		double RatioOfNoItem = 1/(this.M - LengthofGroundTruth+1);
		boolean[] IsRemaining = new boolean[this.M];
		
		for(int i = 0; i < this.M; ++i){
			if(this.G_Items_Set.contains(i)){
				IsRemaining[i] = false;
			}
			else{
				IsRemaining[i] = true;
			}
		}
		
		for(int i = 0; i < this.N; ++i){
			if(i != Index1 && i != Index2){
				PositionForGround = this.Randomly_Select_K_From_Z(LengthofGroundTruth, this.M);
				Tool sorter = new Tool();
				sorter.Sort(PositionForGround);
				if(this.RandomGenerator.nextFloat() > 0.5){//choose the ascending order as bases;
					for(int j = 0; j < LengthofGroundTruth; ++j){
						this.R_Data[i][PositionForGround[j]] = Ground_Ascending.get(j);
					}
				}
				else{				
					for(int j = 0; j < LengthofGroundTruth; ++j){//choose the descending order as bases;
						this.R_Data[i][PositionForGround[j]] = Ground_Descending.get(j);
					}
				}				
				for(int j = 0; j < this.M; ++j){
					if(this.R_Data[i][j] < 0){
						if(this.RandomGenerator.nextDouble() > RatioOfNoItem){
							int currentSelectedItem = this.RandomGenerator.nextInt(this.M);
							while(!IsRemaining[currentSelectedItem]){
								currentSelectedItem = this.RandomGenerator.nextInt(this.M);
							}
							this.R_Data[i][j] = currentSelectedItem;
						}
					}
				}				
				PositionForGround = null;
			}		
		}
	}  
	public int[] Randomly_Select_K_From_Z(int K, int Z){
		int[] Result_Vector = new int[K];
		TreeSet<Integer>  SelectedItemsSet = new TreeSet<Integer>();		
		int currentPosition;	
		for(int i = 0; i < K; ++i){
			currentPosition = this.RandomGenerator.nextInt(Z);
			while(SelectedItemsSet.contains(currentPosition)){
				currentPosition = this.RandomGenerator.nextInt(Z);
			}
			Result_Vector[i] = currentPosition;
			SelectedItemsSet.add(currentPosition);
		}		
		return Result_Vector;
	}
	
	
	public static void main(String[] args) throws IOException{
		
		//Problem instance
		//String Dir =  "D:/CA科研论文/2020ConstraintedRankAggregation"
		//		+ "/JavaProgram/MCRADataSet/";
		
		//Small instances for test 
		String Dir =  "D:/CA科研论文/2020ConstraintedRankAggregation"
					+ "/JavaProgram/SimpleExampleForDebug/";
				
		String DataInformation = Dir + "/DataInformation.txt";
		File fout = new File(DataInformation);
		FileWriter fw = new FileWriter(fout);
		PrintWriter pw = new PrintWriter(fw);
		
		pw.println("InstanceNo.\tItems(M)\tRankings(N)\tOptimalValue\tOptimalSolutions" +
				"\tGroups\tFixedOptimals\tRandomSeed(Long)");		
		int DataSetNo = 0;
		
		//int NumberInstance = 25;		
		/*Long[] Seeds = new Long[NumberInstance];		
		String seedfile = Dir +"/Seed.txt";
		File fin = new File(seedfile);
		FileReader fir = new FileReader(fin);
		LineNumberReader stdin = new LineNumberReader(fir);
		String line;
		int c = 0;
		while((line = stdin.readLine())!= null){
			Seeds[c++] = Long.parseLong(line.trim());
		}		
		stdin.close();*/
		
		//for data instances 
		//for(int m = 500; m <= 1000; m = m + 100){ //number of items
		//	for(int n = 50; n <= 100; n = n + 10){//number of individual rankings
		
		//for small instances of debug
		for(int m = 6; m <= 6; m = m + 1) {
			for(int n = 5; n <= 5; n = n + 1) {
				
				System.out.println("Generating Data Set " + (DataSetNo+1) +".....");
				System.out.println("\tNumber Of Items:\t"+m+"\n\tNumber of Rankings:\t"+n);
				String Data_Path = Dir + "Data_Set_" + (DataSetNo+1) + "_" + m + "_" + n;
				File Data_Dir = new File(Data_Path);
				Data_Dir.mkdir(); 
				
				String IndividualRanking = Data_Path + "/IndividualRankings.txt";
				String groundtruth = Data_Path + "/GroundTruth.txt";
				String originalgroundtruth = Data_Path +"/OriginalGroundTruth.txt";
				String APCMFile = Data_Path + "/APCM.txt";
			
				String PCM_Path = Data_Path + "/PCM/";
				File PCM_Dir = new File(PCM_Path);
				PCM_Dir.mkdir();
				
				
				//Generate Data
				DataSynthesizer DPer = new DataSynthesizer(m,n); //,ratio,MinimumOptimals);				
				DPer.SetRandomGenerator();
				//DPer.SetRandomGenerator(1394801862565L);
				
				//Randomly generate the length of optimal solution;
				//Randomly generate the number of optimal solutions;
				System.out.println("\tStart generating GT Len ComOps ......");
				DPer.GenerateGTLenComOps(); 
				
				//Randomly generate optimal solutions;
				System.out.println("\tGenerating Ground Truth Rankings....");
				DPer.GenerateGroundTruthLists();
				System.out.println("\tDone!\n\tGenerating Fixed Ground Truth Rankings...");
				DPer.GenerateFixedGroundTruthLists();
				System.out.println("\tDone!");
				
				//Randomly generate the set of ranking sequences such that the maximum consistent rank sequences are 
				//generated optimal solutions;
				DPer.GenerateIndividualRankings();
				
				//Construct the Binary Pairwise Comparison Matrix of those generated ranking sequences; 
				DPer.ConstructPCM();
				
				//Generate the aggregated binary Pairwise Comparison Matrix; 
				DPer.ConstructAPCM();		
				
				DPer.OutputIndividualRankings(IndividualRanking);		
				DPer.PrintGroundTruth(groundtruth);		
				DPer.PrintOriginalGroundTruth(originalgroundtruth);
				DPer.PrintPCMsToFolder(PCM_Path);		
				DPer.PrintAPCMToFile(APCMFile);
				
				pw.println((DataSetNo+1)+"\t"+m+"\t"+n+"\t"+DPer.lenTruth+"\t"+DPer.Optimals+"\t"+
						DPer.CommonGroups + "\t" + DPer.FixedOptimals + "\t" + DPer.RandomSeed);
				++DataSetNo;				
				System.out.println("Done!");
			}
		}		
		pw.close();
	}

}
