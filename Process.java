import java.util.ArrayList;

/**
 * @author Lauren
 * Process Class for Lab 3
 */
public class Process {
	private ArrayList<Integer> claims;
	private ArrayList<Instruction> instructions;
	private ArrayList<Integer> heldResources;
	private int taskNumber;
	private int runTime;
	private int numOfInstructions;
	private boolean ready;
	private boolean aborted;
	private boolean terminated;
	private boolean blocked;
	private int computingClock;
	private int waitingTime;
	
	/** Overloaded constructor for a Process
	 * 
	 * @param taskNumber is the id number of the Process
	 * @param numOfResources is the number of resources that exist within the system
	 */
	public Process(int taskNumber, int numOfResources)
	{
		claims = new ArrayList<Integer>();
		heldResources = new ArrayList<Integer>();
		
		for(int i = 0; i < numOfResources; i++)
		{
			claims.add(0);
			heldResources.add(0);
		}
		
		instructions = new ArrayList<Instruction>();
		this.taskNumber = taskNumber;
		numOfInstructions = 0;
		ready = true;
	}
	/** Gets the amount of time spent waiting
	 * 
	 * @return int waiting time, the amount of time spent waiting
	 */
	public int getWaitingTime() {
		return waitingTime;
	}
	/** Sets the waiting time for the current process, 
	 * meaning the time that it is blocked and cannot have its request granted
	 * 
	 * @param waitingTime is an int representing the time the Process has waited
	 */
	public void setWaitingTime(int waitingTime) {
		this.waitingTime = waitingTime;
	}
	/** Get if the current process is blocked
	 * 
	 * @return a boolean that's true if the process is blocked, false otherwise
	 */
	public boolean isBlocked() {
		return blocked;
	}
	/** Set if the process is blocked
	 * 
	 * @param blocked is a boolean representing whether the process is currently blocked or not
	 */
	public void setBlocked(boolean blocked) {
		this.blocked = blocked;
	}
	/** Get whether or not the process is aborted
	 * 
	 * @return whether or not the process is aborted, true if it is, false otherwise.
	 */
	public boolean isAborted() {
		return aborted;
	}
	/** Sets the process to whether it is aborted or not, true for aborted, false for otherwise
	 * 
	 * @param aborted is a boolean variable representing if the process is aborted
	 */
	public void setAborted(boolean aborted) {
		this.aborted = aborted;
	}
	/** Gets the boolean for whether a process has terminated or not
	 * 
	 * @return boolean variable terminated
	 */
	public boolean isTerminated() {
		return terminated;
	}
	/** Sets if the process has terminated
	 * 
	 * @param terminated represents whether the process has terminated or not.  Boolean variable.
	 */
	public void setTerminated(boolean terminated) {
		this.terminated = terminated;
	}
	/** Gets the List of held resources
	 * 
	 * @return an ArrayList of the held resources
	 */
	public ArrayList<Integer> getHeldResources(){
		return heldResources;
	}
	/** Checks to see if a process is ready or not
	 * 
	 * @return a boolean, ready
	 */
	public boolean isReady(){
		return ready;
	}
	/** Sets whether the Process is done computing or not
	 * 
	 * @param r is a boolean variable representing the ready state
	 */
	public void setReady(boolean r){
		this.ready = r;
	}
	/** Gets the computingClock variable
	 * 
	 * @return the computingClock integer, representing how many more cycles the compute instruction will take to complete.
	 */
	public int getComputingClock() {
		return computingClock;
	}
	/** Sets the computingClock when the compute instruction is called. 
	 * 
	 * @param computingClock is the number of cycles it will take for compute to complete
	 */
	public void setComputingClock(int computingClock) {
		this.computingClock = computingClock;
		if(computingClock > 0){
			setReady(false);
		}
	}
	/** If the process is not ready, decrements the computingClock.  If the computingClock is decremented to 0,
	 * then the process is set to ready.
	 */
	public void decrementComputingClock(){
		computingClock--;
		if(computingClock == 0){
			setReady(true);
		}
	}
	/** Set the number of instructions to the parameter
	 * 
	 * @param numOfInstructions represents the total number of Instructions
	 */
	public void setNumOfInstructions(int numOfInstructions) {
		this.numOfInstructions = numOfInstructions;
	}
	/** Gets the initial number of instructions
	 * 
	 * @return an int representing the number of total instructions for this process
	 */
	public int getNumOfInstructions(){
		return numOfInstructions;
	}
	/** Used to increment the number of instructions parameter to show the initial total amount of instructions.
	 */
	public void incrementNumOfInstructions(){
		numOfInstructions++;
	}
	/** Gets the ArrayList of instructions contained in the Process
	 * 
	 * @return an ArrayList of Instructions 
	 */
	public ArrayList<Instruction> getInstructions() {
		return instructions;
	}
	/** Gets the claims ArrayList of the Process
	 * 
	 * @return an ArrayList of Integers containing the Process claims
	 */
	public ArrayList<Integer> getClaims() {
		return claims;
	}
	/** Sets the claim of the process.  Invoked when an initiate instruction is added to the Process.
	 * 
	 * @param sNum is the resource type, and the index at which it is added
	 * @param i is the quantity of the resource being claimed
	 */
	public void setClaim(int sNum, int i){
		claims.set(sNum, i + claims.get(sNum));
	}
	/** Gets the taskNumber of the Process
	 * 
	 * @return an int representing the Process's taskNumber
	 */
	public int getTaskNumber() {
		return taskNumber;
	}
	/** Gets the amount of cycles that the process takes to complete
	 * 
	 * @return int runTime, representing the amount of time the process took to complete
	 */
	public int getRunTime() {
		return runTime;
	}

	/** Sets the runTime parameter to the cycle that the Process finished at
	 * 
	 * @param runTime is an int representing the total run time of the Process
	 */
	public void setRunTime(int runTime) {
		this.runTime = runTime;
	}
	/** Adds a new instruction, while adding to claims if the instruction is initiate
	 * 
	 * @param i is an instruction object to be added to the ArrayList Instructions 
	 */
	public void addInstruction(Instruction i)
	{
		this.instructions.add(i);
		if(i.getInstruct().equals("initiate")){
			this.setClaim(i.getType() - 1, i.getQuantity());
		}
	}

}
