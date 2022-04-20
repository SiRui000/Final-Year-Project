//package Chongshou.MS.MCRA;

/* ------------------ * 
 * This Java file is not used for the paper 2021. 
 * ---- Note by CsLi, 12 Sep 2021 
 * ------------------ *
 */


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.TreeSet;

public class DataProducer {
	
	/* In this data generation program, we consider two preference relationship only: > or < 
	 * The maximum consistent ranking sequences are fully distinguished; 
	 * */
	
	public int M; //number of items; Indices set {0, 1, ..., M-1}; Assume that M >= 100.
	public int N; //number of individual rankings; Indices set{0, 1, ...., N-1}; Assume that N >= 3;
	public double Ratio; //(Upper bound of Length of MCR Lists)/M = Ratio; 
		
	public Random RandomGenerator = null; //random generator;
	private long  RandomSeed;   //seed for current random generator;
	
	public int lenTruth; //length of Maximum Consistent Ranked Lists, lenTruth <= m;	
	public int Optimals; //Number of Optimal solutions;	
	
	
	//Ground Truth Order; 
	//There should be Optimals Ground Truth Sequences; length of each should be lenTruth; 
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
	
	//Pairwise Comparison Matrix, PCM[i][j][k], representing the preference 
	//relationship between any two items
	//0 <= i <= (N-1);  0 <= j <= (M-1);  0 <= k <= (M-1);
	//If the relationship between item j and item k is j > k in individual ranking i, 
	//PCM[i][j][k] = true;
	//otherwise, PCM[i][j][k] = false;
	public boolean[][][] PCM = null;
	
	//Aggregated Pairwise Comparison Matrix
	public boolean[][] APCM = null;  
	
	
	public DataProducer(int NumberOfItems, int NumberOfRankings, double c_Ratio){
		this.M = NumberOfItems;
		this.N = NumberOfRankings;
		this.Ratio = c_Ratio; 
	}	
	
	public DataProducer(){		
	} 
	
	public void Initialization(int NumberOfItems, int NumberOfRankings, double c_Ratio){
		this.M = NumberOfItems;
		this.N = NumberOfRankings;
		this.Ratio = c_Ratio; 
	}
	
	public void GenerateGTLenOps(){		
		int len = (int)(this.M*this.Ratio);		
		if(len <= 1){
			System.out.println("The Ratio of Upper bound of MCR Lists' Upper bound to the number of " +
					"\n all items(m) is too small!");
		}				
		//Random.nextInt(n), return a pseudorandom, uniformly distributed int value between 0(inclusive)
		//the specified value n (exclusive)
		this.lenTruth = this.RandomGenerator.nextInt(len - 1) + 2;		
		
		//lenTruth - 1 + Optimals <= m
		//int OptimalsUpper = this.M - this.lenTruth + 1;		
		//Use this Upper bound to consider that each ground truth rank list is 
		//completely different from each other; 
		int OptimalsUpper = (int)(1/this.Ratio); 
		this.Optimals = this.RandomGenerator.nextInt(OptimalsUpper - 1) + 2;
	}
	
	public void GenerateGroundTruthLists(){
		this.G_Order = new int[this.Optimals][this.lenTruth];
		TreeSet<Integer> ItemsSet = new TreeSet<Integer>(); //indices set of items
		for(int i = 0 ; i < this.M ; ++i){
			ItemsSet.add(i);
		}
		this.G_Items_Set = new TreeSet<Integer>();		
		for(int i = 0 ; i < this.Optimals; ++i){
			for(int j = 0 ; j < this.lenTruth ; ++j){
				int currentItemIndex = this.RandomGenerator.nextInt(this.M);
				while(!ItemsSet.contains(currentItemIndex)){
					currentItemIndex = this.RandomGenerator.nextInt(this.M);
				}
				this.G_Order[i][j] = currentItemIndex;
				ItemsSet.remove(currentItemIndex);
				this.G_Items_Set.add(currentItemIndex);
			}
		}
	}
	
	public void GenerateIndividualRanking(){
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
		
		/* Construct two full ranking sequences such that the 
		 *  two relationships between any two items
		 * from different ground truth sequences in the first two individual rankings are opposite
		 **/		
		for(int i = 0 ; i < this.Optimals; ++i){
			for(int j = 0; j < this.lenTruth; ++j){
				Order_Ascending.add(this.G_Order[i][j]);
				Ground_Ascending.add(this.G_Order[i][j]);
			   //System.out.print(this.G_Order[i][j]+"\t");
			}
		}		
		//add the items at the end of the first ground truth which are not 
		//used by Ground Truth Ranking Sequence Constructing
		for(int i = 0; i < this.M; ++i){
			if(!this.G_Items_Set.contains(i)){
				Order_Ascending.add(i);
			}
		}
		//System.out.print("\n");		
		//add the items at the front of the second ground truth which are not used by Ground Truth Ranking List Constructing;
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
		double RatioOfNoItem = 1/(this.M - LengthofGroundTruth + 1);
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
		}//End-for-i	
	}  
	
	
	public void PrintBasicDataInformation(String FileName) throws IOException{		
		File fout = new File(FileName);
		FileWriter fw = new FileWriter(fout);
		PrintWriter pw = new PrintWriter(fw);
		
		pw.println("Number of Items:\t" + this.M + "\nIndices Set of Items:\t" 
		   + "{0, 1, 2, ...,"+ (this.M -1) +"}");
		pw.println("Number of Individual Rankings:\t" + this.N + 
				"\nIndices Set of Individual Ranking:\t" + "{0,1,2,...," + (this.N - 1) + "}");
		pw.println("Current Seed is:\t" + this.RandomSeed + "L");
		pw.println("Ratio of Upper Bound of Length of Result to the number of Items:\t" 
		         + this.Ratio);
		pw.println("Number of Optimal Solutions:\t"
		         + this.Optimals);
		pw.println("Length of Optimal Solutions:\t" + this.lenTruth);
		pw.println("Number of Items Used in Ground Truth:\t" + this.G_Items_Set.size());
		pw.println("Number of Remaining Items for Constructing Data:\t"+(this.M - this.G_Items_Set.size())+"\n\n");
		pw.println("Ground Truth Rankings are:\t");
		
		for(int i = 0 ; i < this.Optimals; ++i){
			for(int j = 0; j < this.lenTruth - 1 ; ++j){
				pw.print(this.G_Order[i][j]+"\t>\t");
			}			
			pw.println(this.G_Order[i][this.lenTruth - 1]);
		}
		pw.close();
	}
	
	
	public void PrintGroundTruth(String FileName) throws IOException{
		
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
						pw.print("\t>\t"+this.R_Data[i][j]);
					}
				}
			}
			pw.print("\n");
		}		
		pw.close();		
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
		}//End-for-i
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
	
	
	
	
	public static void main(String[] args) 
			throws IOException{
		
		String Dir = "D:/C科研论文/2020ConstraintedRankAggregation/JavaProgram/DataSetForPaper/";
		//"D:/Chongshou Li  Folder/My Research Topic		
		//---/Aggregation of Partial Rankings/MS_MCRA_Data/";
		
		int DataSetNo = 1;		
		double ratio = 0.05;
		
		for(int m = 500; m <= 1000; m = m + 100){ //number of items
			for(int n = 50; n <= 100; n = n + 10){//number of individual rankings
				
				System.out.print("Generating Data Set " + DataSetNo +".....");
				String Data_Path = Dir + "Data_Set_" + DataSetNo + "_" + m + "_" + n;
				File Data_Dir = new File(Data_Path);
				Data_Dir.mkdir(); 
				
				String DataInformation = Data_Path + "/DataInformation.txt";
				String IndividualRanking = Data_Path + "/IndividualRankings.txt";
				String groundtruth = Data_Path + "/GroundTruth.txt";
				String APCMFile = Data_Path + "/APCM.txt";
			
				String PCM_Path = Data_Path + "/PCM/";
				File PCM_Dir = new File(PCM_Path);
				PCM_Dir.mkdir();
				
				DataProducer DPer = new DataProducer(m,n,ratio);				
				DPer.SetRandomGenerator();	
				
				//Randomly generate the length of optimal solution;
				//Randomly generate the number of optimal solutions;
				DPer.GenerateGTLenOps(); 
				
				//Randomly generate optimal solutions;
				DPer.GenerateGroundTruthLists();
				
				//Randomly generate the set of ranking sequences such that the 
				// maximum consistent rank sequences are 
				//generated optimal solutions;
				DPer.GenerateIndividualRanking();
				
				//Construct the Binary Pairwise Comparison Matrix of those generated ranking sequences; 
				DPer.ConstructPCM();
				
				//Generate the aggregated binary Pairwise Comparison Matrix; 
				DPer.ConstructAPCM();		
				
				DPer.PrintBasicDataInformation(DataInformation);
				DPer.OutputIndividualRankings(IndividualRanking);		
				DPer.PrintGroundTruth(groundtruth);		
				DPer.PrintPCMsToFolder(PCM_Path);		
				DPer.PrintAPCMToFile(APCMFile);
				
				++DataSetNo;				
				System.out.println("Done!");
			}
		}		
	}	
}
