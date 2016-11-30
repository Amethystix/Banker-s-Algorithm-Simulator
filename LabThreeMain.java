import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * @author Lauren DiGiovanni
 * Main File for Lab 3 of Operating Systems
 */
public class LabThreeMain {
	
	/**Main method for Lab #3.  Takes the input and makes it into Process objects contained in a list, then runs 
	 * both the FIFO and Banker's method of avoiding deadlock.
	 * 
	 * @param args arguments when running the program.  In this case, there should be one argument, a text file containing
	 * the input for the program
	 */
	public static void main(String[] args){
		
		File f = new File(args[0]);
		ArrayList<Integer> resources = new ArrayList<Integer>();
		ArrayList<Integer> resourcesBanker = new ArrayList<Integer>();
		
		try {
			Scanner sc = new Scanner(f);
			
			int numOfTasks = sc.nextInt();
			ArrayList<Process> tasks = new ArrayList<Process>(numOfTasks);
			ArrayList<Process> bankerTasks = new ArrayList<Process>(numOfTasks);
			int numOfResources = sc.nextInt();
			//Fill the Resource ArrayList with input
			for(int i = 0; i < numOfResources; i++)
			{
				int next = sc.nextInt();
				resources.add(next);
				resourcesBanker.add(next);
			}
			//Input all of the Processes into the tasks list
			while(sc.hasNext())
			{
				String current = sc.next();
				
					if (current.equals("initiate"))
					{
						int theNext = sc.nextInt();
						if(theNext > tasks.size() || tasks.size() == 0)
						{
							tasks.add(new Process(theNext, numOfResources));
							bankerTasks.add(new Process(theNext, numOfResources));
						}
						Instruction currIn = new Instruction(current, sc.nextInt(), sc.nextInt());
						tasks.get(theNext - 1).addInstruction(currIn);
						bankerTasks.get(theNext - 1).addInstruction(currIn);
					}
					else{
						int theNext = sc.nextInt() - 1;
						Instruction currIn = new Instruction(current, sc.nextInt(), sc.nextInt());
						tasks.get(theNext).addInstruction(currIn);
						bankerTasks.get(theNext).addInstruction(currIn);
					}
					tasks.get(tasks.size()-1).incrementNumOfInstructions();
					bankerTasks.get(bankerTasks.size() -1).incrementNumOfInstructions();
			}
			sc.close();

			FIFO(tasks, resources);
			bankers(bankerTasks, resourcesBanker);
			
		} catch (FileNotFoundException e) {
			System.out.println("The specified file could not be found");
			e.printStackTrace();
		}
		
	}
	/** This method uses the optimistic algorithm of hopefully avoiding deadlock.  It can still come to a deadlocked
	 * state, and if it does, will abort a process and release its resources with the hope that this will eliminate the 
	 * deadlock in the state.  
	 * 
	 * @param tasks is a list of processes that need to carry out their instructions to completion
	 * @param resources is a list of all available resources
	 */
	public static void FIFO(ArrayList<Process> tasks, ArrayList<Integer> resources)
	{
		int currCycle = 0;
		boolean checkNothingMoreAbort = false;
		boolean allTerm = false;
		ArrayList<Process> blocked = new ArrayList<Process>();
		ArrayList<Integer> releasedResources = new ArrayList<Integer>();
		ArrayList<Process> aborted = new ArrayList<Process>();
		//Set all of the values in releasedResources to 0
		for(int rr = 0; rr < resources.size(); rr++){
			releasedResources.add(0);
		}

		while(!allTerm)
		{
			//Check to see if any blocked task's requests can be granted
			if(!(blocked.isEmpty())){
				for(int b = 0; b < blocked.size(); b++){
					Instruction cbi = blocked.get(b).getInstructions().get(0);
					if (cbi.getQuantity() <= resources.get(cbi.getType() - 1)){
						int r = cbi.getQuantity();
						int rt = cbi.getType() - 1;
						blocked.get(b).getHeldResources().set(rt, r + blocked.get(b).getHeldResources().get(rt));
						resources.set(rt, resources.get(rt) - r);
						blocked.get(b).getInstructions().remove(0);
						tasks.add(blocked.get(b));
						blocked.remove(blocked.get(b));
						checkNothingMoreAbort = false;
						b--;
					}
				}
			}
			for(int i = 0; i < tasks.size(); i++)
			{
				
				Process current = tasks.get(i);
				//Make sure it is not blocked, not computing, and is not terminated
				if(!(current.isBlocked()) && current.isReady() && !(current.isTerminated())){
					Instruction instrCurr = current.getInstructions().get(0);
					int r = instrCurr.getQuantity();
					int rt = instrCurr.getType() - 1;
					if(current.getInstructions().get(0).getInstruct().equals("initiate")){
						current.getInstructions().remove(0);
					}
					//check to see if there are enough resources available to request
					else if(instrCurr.getInstruct().equals("request")){
						if(resources.get((instrCurr.getType() - 1)) < instrCurr.getQuantity())
						{
							instrCurr.setCanBeGranted(false);
							current.setBlocked(true);
							blocked.add(current);
							tasks.remove(current);
							i--;
						}
						else{
							current.getHeldResources().set(rt, r + current.getHeldResources().get(rt));
							resources.set(rt, resources.get(rt) - r);
							current.getInstructions().remove(0);
						}
					}
					//release resources for next cycle
					else if(instrCurr.getInstruct().equals("release")){
						releasedResources.set((instrCurr.getType() - 1), current.getInstructions().get(0).getQuantity());
						current.getHeldResources().set(instrCurr.getType()-1, (current.getHeldResources().get(instrCurr.getType()-1)) - current.getInstructions().get(0).getQuantity());
						current.getInstructions().remove(0);
					}
					//Run compute until the task is finished computing, do not execute other instructions
					else if(instrCurr.getInstruct().equals("compute")){
						current.setComputingClock(instrCurr.getType());
						current.setReady(false);
						current.getInstructions().remove(0);
					}
					//Terminate the task
					else if(instrCurr.getInstruct().equals("terminate")){
						for(int l = 0; l < releasedResources.size(); l++){
							int add = current.getHeldResources().get(l);
							current.getHeldResources().set(l, 0);
							releasedResources.set(l, releasedResources.get(l) + add);
						}
						current.setTerminated(true);
						current.setRunTime(currCycle);
						current.getInstructions().remove(0);
					}
				}
				//Checks to see if the task is done computing
				if(!(current.isReady()))
				{
					current.decrementComputingClock();
				}
			}
			if(!checkNothingMoreAbort)
				currCycle++;
			//Calculates the Waiting Time for each Task
			for(int wt = 0; wt < blocked.size(); wt++){
				if(!checkNothingMoreAbort)
					blocked.get(wt).setWaitingTime(blocked.get(wt).getWaitingTime() + 1);
			}
			//Takes any released resources and adds them back to the system for use the next cycle
			for(int r = 0; r < resources.size(); r++){
				resources.set(r, releasedResources.get(r) + resources.get(r));
				releasedResources.set(r, 0);
			}
			//Any previously blocked tasks that are now able to run are set to be unblocked
			for(int i = 0; i < tasks.size(); i++)
			{
				if(tasks.get(i).isBlocked())
				{
					tasks.get(i).setBlocked(false);
				}
			}
			//Checks to see if all tasks have terminated
			if(blocked.isEmpty()){
				allTerm = true;
				for(int t = 0; t < tasks.size(); t++){
					if(!(tasks.get(t).isTerminated())){
						allTerm = false;
					}
				}
			}
			//Check to see if there is deadlock, and a process needs to be aborted
			boolean notBlockedTerm = true;
			if(tasks.isEmpty() && !(blocked.isEmpty())){
				int k;
				Process abortingProcess = blocked.get(0);
				for(k = 0; k < blocked.size(); k++){
					if(blocked.get(k).getTaskNumber() < abortingProcess.getTaskNumber()){
						abortingProcess = blocked.get(k);
					}
				}
				for(int ar = 0; ar < resources.size(); ar++){
					resources.set(ar, resources.get(ar) + abortingProcess.getHeldResources().get(ar));
				}
				abortingProcess.setAborted(true);
				aborted.add(abortingProcess);
				blocked.remove(abortingProcess);
				checkNothingMoreAbort = true;
			}
			//Check to see if a task needs to be aborted, but there are objects in tasks (terminated)
			else if(!(blocked.isEmpty())){
				for(int a = 0; a < tasks.size(); a++){
					if(!(tasks.get(a).isTerminated())){
						notBlockedTerm = false;
					}
				}
				if(notBlockedTerm){
					Process abortingProcess = blocked.get(0);
					int k;
					for(k = 0; k < blocked.size(); k++){
						if(blocked.get(k).getTaskNumber() < abortingProcess.getTaskNumber()){
							abortingProcess = blocked.get(k);
						}
					}
					for(int ar = 0; ar < resources.size(); ar++){
						resources.set(ar, resources.get(ar) + abortingProcess.getHeldResources().get(ar));
					}
					abortingProcess.setAborted(true);
					aborted.add(abortingProcess);
					blocked.remove(abortingProcess);
					checkNothingMoreAbort = true;
				}
			}
		}
		for(int i = 0; i < aborted.size(); i++){
			tasks.add(aborted.get(i));
		}
		System.out.println("\tFIFO");
		printResults(tasks);
	} 
	
	/** This method utilizes the Banker's pessimistic algorithm to prevent deadlock.  It does this by taking in the 
	 * ArrayList of tasks to run, and differs from the optimistic algorithm in that for every single request instruction,
	 * the manager first checks to see if completing that request will result in a safe or unsafe state.  If the 
	 * state is unsafe, or the request cannot be satisfied, it is added to a list of blocked processes
	 * 
	 * @param tasks is an ArrayList of Processes that need to be run without running into deadlock
	 * @param resources is an ArrayList of integers containing the quantities of all available resources
	 */
	public static void bankers(ArrayList<Process> tasks, ArrayList<Integer> resources)
	{
			int currCycle = 0;
			boolean allTerm = false;
			ArrayList<Process> blocked = new ArrayList<Process>();
			ArrayList<Integer> releasedResources = new ArrayList<Integer>();
			ArrayList<Process> aborted = new ArrayList<Process>();
			//Set all of the values in releasedResources to 0
			for(int rr = 0; rr < resources.size(); rr++){
				releasedResources.add(0);
			}
			//Check to make sure that no task claims more resources than available
			for(int i = 0; i < tasks.size(); i++){
				for(int r = 0; r < resources.size(); r++){
					if(tasks.get(i).getClaims().get(r) > resources.get(r)){
						System.out.println("Task Number " + tasks.get(i).getTaskNumber() + " claims more than resources available in the system.  Aborted");
						tasks.get(i).setAborted(true);
					}
				}
				if(tasks.get(i).isAborted()){
					aborted.add(tasks.get(i));
					tasks.remove(tasks.get(i));
					i--;
				}
			}
			while(!allTerm)
			{
				//First check blocked task list
				if(!(blocked.isEmpty())){
					for(int b = 0; b < blocked.size(); b++){
						Instruction cbi = blocked.get(b).getInstructions().get(0);
						if (cbi.getQuantity() <= resources.get(cbi.getType() - 1)){
							//add blocked tasks back to the tasks list for the purpose of checking the safety of the state
							for(int a = 0; a < blocked.size(); a++){
								tasks.add(blocked.get(a));
							}
							//Check to see if safe
							if(isSafe(tasks, resources, cbi, blocked.get(b))){
								
								//remove the blocked tasks from the tasks list
								for(int a = 0; a < blocked.size(); a++){
									if(blocked.get(a).getTaskNumber() != blocked.get(b).getTaskNumber()){
										tasks.remove(blocked.get(a));
									}
								}
								int r = cbi.getQuantity();
								int rt = cbi.getType() - 1;
								blocked.get(b).getHeldResources().set(rt, r + blocked.get(b).getHeldResources().get(rt));
								resources.set(rt, resources.get(rt) - r);
								blocked.get(b).getInstructions().remove(0);
								blocked.remove(blocked.get(b));
								b--;
							}
							
							else{
								for(int a = 0; a < blocked.size(); a++)
									tasks.remove(blocked.get(a));
							}
						}
					}
				}
				//Now check non-blocked task list
				for(int i = 0; i < tasks.size(); i++)
				{
					Process current = tasks.get(i);
					if(!(current.isBlocked()) && current.isReady() && !(current.isTerminated())){
						Instruction instrCurr = current.getInstructions().get(0);
						int r = instrCurr.getQuantity();
						int rt = instrCurr.getType() - 1;
						if(current.getInstructions().get(0).getInstruct().equals("initiate")){
							current.getInstructions().remove(0);
						}
						//Requesting resources
						else if(instrCurr.getInstruct().equals("request")){
							//if it cannot fulfill the request due to lack of resources available
							if(resources.get((instrCurr.getType() - 1)) < instrCurr.getQuantity())
							{
								instrCurr.setCanBeGranted(false);
								current.setBlocked(true);
								blocked.add(current);
								tasks.remove(current);
								i--;
							}
							//still needs to check and see if the resulting state is safe
							else{
								if(isSafe(tasks, resources, instrCurr, current)){
									current.getHeldResources().set(rt, r + current.getHeldResources().get(rt));
									resources.set(rt, resources.get(rt) - r);
									current.getInstructions().remove(0);
								}
								//block if not safe
								else{
									current.setBlocked(true);
									blocked.add(current);
									tasks.remove(current);
									i--;
								}
							}
						}
						//Release resources to be available next cycle
						else if(instrCurr.getInstruct().equals("release")){
							releasedResources.set((instrCurr.getType() - 1), current.getInstructions().get(0).getQuantity());
							current.getHeldResources().set(instrCurr.getType()-1, (current.getHeldResources().get(instrCurr.getType()-1)) - current.getInstructions().get(0).getQuantity());
							current.getInstructions().remove(0);
						}
						//Make the process compute and remain inactive until finished computing
						else if(instrCurr.getInstruct().equals("compute")){
							current.setComputingClock(instrCurr.getType());
							current.setReady(false);
							current.getInstructions().remove(0);
						}
						//Terminate and release resources
						else if(instrCurr.getInstruct().equals("terminate")){
							for(int l = 0; l < releasedResources.size(); l++){
								int add = current.getHeldResources().get(l);
								current.getHeldResources().set(l, 0);
								releasedResources.set(l, releasedResources.get(l) + add);
							}
							current.setTerminated(true);
							current.setRunTime(currCycle);
							current.getInstructions().remove(0);
						}
					}
					//Checks to see if the task is done computing
					if(!(current.isReady()))
					{
						current.decrementComputingClock();
						if(current.getComputingClock() == 0)
						{
							current.setReady(true);
						}
					}
				}
				currCycle++;
				//Calculates the Waiting Time for each Task
				for(int wt = 0; wt < blocked.size(); wt++){
					blocked.get(wt).setWaitingTime(blocked.get(wt).getWaitingTime() + 1);
				}
				for(int t = 0; t < tasks.size(); t++){
					for(int r = 0; r < resources.size(); r++){
						//Checks to see if the held resources exceeds the claim amount
						if(tasks.get(t).getHeldResources().get(r) > tasks.get(t).getClaims().get(r)){
							tasks.get(t).setAborted(true);
							System.out.println("Task number " + tasks.get(t).getTaskNumber() + " requests more resources than its initial claim.  Aborted.");
						}
						
					}
					//releases resources if a task is aborted
					if(tasks.get(t).isAborted()){
						aborted.add(tasks.get(t));
						for(int r = 0; r < resources.size(); r++)
						{
							releasedResources.set(r, tasks.get(t).getHeldResources().get(r) + releasedResources.get(r));
						}
						tasks.remove(tasks.get(t));
						
					}
				}
				//Takes any released resources and adds them back to the system for use the next cycle
				for(int r = 0; r < resources.size(); r++){
					resources.set(r, releasedResources.get(r) + resources.get(r));
					releasedResources.set(r, 0);
				}
				//Any previously blocked tasks that are now able to run are set to be unblocked
				for(int i = 0; i < tasks.size(); i++)
				{
					if(tasks.get(i).isBlocked())
					{
						tasks.get(i).setBlocked(false);
					}
				}
				//Checks to see if all tasks have terminated
				if(blocked.isEmpty()){
					allTerm = true;
					for(int t = 0; t < tasks.size(); t++){
						if(!(tasks.get(t).isTerminated())){
							allTerm = false;
						}
					}
				}
			}
			//Adds aborted tasks back to the tasks list for printing purposes
			for(int i = 0; i < aborted.size(); i++){
				tasks.add(aborted.get(i));
			}
			System.out.println("\tBANKERS");
			printResults(tasks);
		} 
	/** Determines whether or not the current instruction will result in a safe state or not.  If the state is not safe,
	 * will return false, if it is safe, returns true
	 * 
	 * @param tasks is the list of tasks, including blocked tasks, that need to be tested to see if the current
	 * action is safe or not
	 * @param resources is an ArrayList of the total resources held by the system
	 * @param test is the instruction being checked for its safety
	 * @param curr is the process that is executing the instruction test
	 * @return a boolean "safe" that is whether implementing instruction test will result in a safe state or not
	 */
	public static boolean isSafe(ArrayList<Process> tasks, ArrayList<Integer> resources, Instruction test, Process curr){
		boolean safe = true;
		//Needs to make copies of lists to simulate with so that the existing task list is not modified
		ArrayList<Process> simTasks = new ArrayList<Process>();
		ArrayList<Integer> simResource = new ArrayList<Integer>();
		ArrayList<Process> waitingTasks = new ArrayList<Process>();
		Process simCurr = new Process(curr.getTaskNumber(), curr.getHeldResources().size());
		int priorheldResources = curr.getHeldResources().get(test.getType() - 1);
		for(int t = 0; t < tasks.size(); t++){
			if(!(tasks.get(t).isTerminated()) && !(tasks.get(t).getInstructions().get(0).getInstruct().equals("terminate"))){
				Process simpr = tasks.get(t);
				simTasks.add(simpr);
				if(curr.getTaskNumber() == simpr.getTaskNumber()){
					simCurr = simpr;
				}
			}
		}
		for(int r = 0; r < resources.size(); r++){
			simResource.add(resources.get(r));
		}
		//Sets the resources at the task being tested to that of if its request is granted.  Also changes the simulated 
		//system's resources
		simResource.set(test.getType() - 1, simResource.get(test.getType() - 1) - test.getQuantity());
		simCurr.getHeldResources().set(test.getType() - 1, curr.getHeldResources().get(test.getType()-1) + test.getQuantity());
		
		//Iterate through all of the processes and ensure that all can complete
		for(int t = 0; t < simTasks.size(); t++){	
			//First, check any waiting tasks
			for(int w = 0; w < waitingTasks.size(); w++){
				boolean canFinish = true;
				for(int i = 0; i < resources.size(); i++){
					Process current = waitingTasks.get(w);
					if(current.getClaims().get(i) <= simResource.get(i) + current.getHeldResources().get(i))
					{
						//nothing happens and if canFinish has not been set to false at any point, it remains true
					}
					else{
						//Any one resource claim being unsatisfied leads the task to not finish
						canFinish = false;
					}
				}
				//"terminate" the process and release its resources if it can finish
				if(canFinish){
					for(int i = 0; i < resources.size(); i++){
						Process current = waitingTasks.get(w);
						simResource.set(i, (simResource.get(i) + current.getHeldResources().get(i)));;
					}
					waitingTasks.remove(waitingTasks.get(w));
					w--;
				}
			}
			//Very similar algorithm to that above.  This time not checking the waiting tasks.
			boolean canFinish = true;
			for(int i = 0; i < resources.size(); i++){
				Process current = simTasks.get(t);
				if(current.getClaims().get(i) <= simResource.get(i) + current.getHeldResources().get(i))
				{
					
				}
				else{
					canFinish = false;
				}
			}
			if(canFinish){
				for(int i = 0; i < resources.size(); i++){
					Process current = simTasks.get(t);
					simResource.set(i, (current.getHeldResources().get(i) + simResource.get(i)));
				}
				simTasks.remove(simTasks.get(t));
				t--;
			}
			//If the process can't finish yet, it is added to the waiting tasks
			if(!canFinish){
				waitingTasks.add(simTasks.get(t));
				simTasks.remove(simTasks.get(t));
				//adjusts the value of t to account for the size of the ArrayList
				t--;
			}
		}
		boolean nothingToDo = false;
		//If no waiting process can complete in one iteration of the while loop, then nothingToDo is equal to true and
		//the state is not safe if the waiting process list is not empty
		while(!nothingToDo){
			nothingToDo = true;
			for(int w = 0; w < waitingTasks.size(); w++){
				boolean canFinish = true;
				for(int i = 0; i < resources.size(); i++){
					Process current = waitingTasks.get(w);
					if(current.getClaims().get(i) <= simResource.get(i) + current.getHeldResources().get(i))
					{}
					else{
						canFinish = false;
					}
				}
				if(canFinish){
					for(int i = 0; i < resources.size(); i++){
						Process current = waitingTasks.get(w);
						simResource.set(i, (simResource.get(i) + current.getHeldResources().get(i)));
					}
					waitingTasks.remove(waitingTasks.get(w));
					w--;
					if(w > 0){
						nothingToDo = false;
					}
				}
			}
		}
		//If everything can complete, then the state will be safe
		if(waitingTasks.isEmpty() && simTasks.isEmpty()){
			safe = true;
		}
		//If something has the potential to deadlock, then the state is not safe
		else{
			safe = false;
		}
		simCurr.getHeldResources().set(test.getType() - 1, priorheldResources);
		return safe;
	}
	/** Prints out the results to the console
	 * 
	 * @param p is the ArrayList of processes after they have been run through either the Banker's algorithm, or the
	 * optimistic FIFO algorithm
	 */
	public static void printResults(ArrayList<Process> p){
		
		//First, sort the tasks back into their proper order.  Sorted by task number.
		for(int i = 0; i < p.size(); i++)
		{
			for(int i2 = i; i2 < p.size(); i2++)
			{
				if(p.get(i).getTaskNumber() > p.get(i2).getTaskNumber()){
					Process temp = p.get(i);
					p.set(i, p.get(i2));
					p.set(i2, temp);
				}
			}
		}
		//Variables to add up for the total
		int totalCycles = 0;
		int totalWaitingTime = 0;
		
		for(int j = 0; j < p.size(); j++){
			Process current = p.get(j);
			if(!(current.isAborted())){
				
				int ft = current.getRunTime();
				totalCycles += ft;
				int wt = current.getWaitingTime();
				totalWaitingTime += wt;
				int pw = (wt * 100) / ft;
				System.out.println("Task " + current.getTaskNumber() + "\t" + ft + "\t" + wt + "\t" + pw + "%");
				
			}
			//If the task was aborted, print this instead
			else
				System.out.println("Task " + current.getTaskNumber() + "\t aborted");
		}
		int pwait = (totalWaitingTime * 100) / totalCycles;
		System.out.println("Total" + "\t" + totalCycles + "\t" + totalWaitingTime + "\t" + pwait + "%");
	}
	
}