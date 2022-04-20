//package Chongshou.MS.MCRA;

public class Tool {
	
	public void Sort(int[] List){		
		int length = List.length;		
		int Temp; 
		for(int i = 0 ; i < length  - 1; ++i){
			for(int j = 0 ; j < length - i -1; ++j){
				if(List[j] > List[j+1]){
					Temp = List[j];
					List[j] = List[j+1];
					List[j+1] = Temp;					
				}
			}
		}
	}
	public static void Order(double[] Index, int[] Rank, int SequenceLength){
		Double[] TempIndex = new Double[SequenceLength];
		Integer[]  TempRank = new Integer[SequenceLength];
		for(int i = 0 ; i < SequenceLength ; ++i){
			TempIndex[i] = Index[i];
			TempRank[i] = i ;
		}
		int temp;
		double tempQI;
		for(int i = 0 ; i < SequenceLength  - 1; ++i){
			for(int j = 0 ; j < SequenceLength - i -1; ++j){
				if(TempIndex[j] < TempIndex[j+1]){
					tempQI = TempIndex[j];
					TempIndex[j] = TempIndex[j+1];
					TempIndex[j+1] = tempQI;
					
					temp = TempRank[j];
					TempRank[j] = TempRank[j+1];
					TempRank[j+1] = temp;
				}
			}
		}
		for(int i = 0 ; i < SequenceLength ; ++i){
			Rank[TempRank[i]] = i;
		}
	}
	
	
	public static void nonDecreasingOrder(int[] Values, int[] Rank){
		//Rank[i]: item with index Rank[i] and Values[Rank[i]] is ranked at position i(starting from 0)
		int SequenceLength = Values.length;
		int[] TempIndex = new int[SequenceLength];
		Integer[]  TempRank = new Integer[SequenceLength];
		for(int i = 0 ; i < SequenceLength ; ++i){
			TempIndex[i] = Values[i];
			TempRank[i] = i ;
		}
		int temp;
		int tempQI;
		
		for(int i = 0 ; i < SequenceLength - 1; ++i){
			for(int j = 0 ; j < SequenceLength - i -1; ++j){
				if(TempIndex[j] > TempIndex[j+1]){
					tempQI = TempIndex[j];
					TempIndex[j] = TempIndex[j+1];
					TempIndex[j+1] = tempQI;
					
					temp = TempRank[j];
					TempRank[j] = TempRank[j+1];
					TempRank[j+1] = temp;
				}
			}
		}
		
		for(int i = 0 ; i < SequenceLength ; ++i){
			Rank[i] = TempRank[i];
		}
	}
	public static void GetIncreasingOrder(int[] Weight, int[] OrderedSequence){
		//non-decreasing order(Starting Index from 0)
		int size = Weight.length;		
		int[] TempRank = new int[size];
		int[] TempWeight = new int[size];
		
		for(int i = 0 ; i < size; ++i){
			TempRank[i] = i;
			TempWeight[i] = Weight[i];
		}
		int temp;
		int tempWeight;
		
		for(int i = 0 ; i < size  - 1; ++i){
			for(int j = 0 ; j < size - i -1; ++j){
				if(TempWeight[j] > TempWeight[j+1]){
					
					tempWeight = TempWeight[j];
					TempWeight[j] = TempWeight[j+1];
					TempWeight[j+1] = tempWeight;
					
					temp = TempRank[j];
					TempRank[j] = TempRank[j+1];
					TempRank[j+1] = temp;
				}
			}
		}		
		for(int i = 0; i < size; ++i){
			OrderedSequence[i] = TempRank[i];
		}	
	}
	public void GetNonIncreasingOrder(int[] Weight,int[] OrderedSequence){
		//descending order(Starting Index from 0)
		int size = Weight.length;		
		int[] TempRank = new int[size];
		int[] TempWeight = new int[size];
		
		for(int i = 0 ; i < size; ++i){
			TempRank[i] = i;
			TempWeight[i] = Weight[i];
		}
		int temp;
		int tempWeight;
		
		for(int i = 0 ; i < size  - 1; ++i){
			for(int j = 0 ; j < size - i -1; ++j){
				if(TempWeight[j] < TempWeight[j+1]){
					
					tempWeight = TempWeight[j];
					TempWeight[j] = TempWeight[j+1];
					TempWeight[j+1] = tempWeight;
					
					temp = TempRank[j];
					TempRank[j] = TempRank[j+1];
					TempRank[j+1] = temp;
				}
			}
		}		
		for(int i = 0; i < size; ++i){
			OrderedSequence[i] = TempRank[i];
		}	
	}
	public void GetOrderedSequence(int[] Weight, int[] OrderedSequence){
		//descending order(Starting Index from 0)
		int size = Weight.length;		
		int[] TempRank = new int[size];
		int[] TempWeight = new int[size];
		
		for(int i = 0 ; i < size; ++i){
			TempRank[i] = i;
			TempWeight[i] = Weight[i];
		}
		int temp;
		int tempWeight;
		
		for(int i = 0 ; i < size  - 1; ++i){
			for(int j = 0 ; j < size - i -1; ++j){
				if(TempWeight[j] < TempWeight[j+1]){
					
					tempWeight = TempWeight[j];
					TempWeight[j] = TempWeight[j+1];
					TempWeight[j+1] = tempWeight;
					
					temp = TempRank[j];
					TempRank[j] = TempRank[j+1];
					TempRank[j+1] = temp;
				}
			}
		}		
		for(int i = 0; i < size; ++i){
			OrderedSequence[i] = TempRank[i];
		}		
	} 
	
	
	/*public static void nonDecreasingOrder(int[] Values, int[] Rank) {
		//Values[Rank[i]] is ranked at position i 
		int length = Values.length;
		Rank = new int[length];
		int[] tempValues = new int[length];
		
		for(int i = 0; i < length; ++i) {
			tempValues[i] = Values[i];			
		}
		
		int temp;
		for(int i = 0 ; i < length  - 1; ++i){
			for(int j = 0 ; j < length - i -1; ++j){
				if(tempValues[j] > tempValues[j+1]){
					temp = tempValues[j];
					tempValues[j] = tempValues[j+1];
					tempValues[j+1] = temp;
				}
			}
		}	
	}*/
	
	/*public static void main(String[] args){
		int[] values = {0,5,22,3,0};
		int[] rank = new int[values.length];
		nonDecreasingOrder(values, rank);
		for(int i = 0; i < values.length; ++i) {
			System.out.println(i+"\t"+values[i]+"\t"+rank[i]);
		}		
	}*/
	
	
	
}
