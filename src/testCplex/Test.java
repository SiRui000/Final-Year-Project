//package Chongshou.MS.MCRA;

package testCplex;

import ilog.concert.IloException;

//import ilog.cplex.IloCplex;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

public class Test {
	
	public static void main(String[] args) throws IOException, IloException{	
		
		Queue<Integer> queue = new LinkedList<Integer>();
		
		queue.offer(1);
		queue.offer(3);
		queue.offer(0);
		
		
		Integer x = queue.poll();
		System.out.println(x);
		System.out.println(queue);
		
		
		queue.offer(0);
		System.out.println(queue);
		
		//System.out.println("Hello");
		//IloCplex cplex = new IloCplex();
		//System.out.println(cplex);
		//System.out.println("Hello");
	}
}
