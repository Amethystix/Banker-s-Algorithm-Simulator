
/**
 * @author Lauren
 * Instruction Class for Lab 3
 */
public class Instruction {
	private String instruct;
	private int type;
	private int quantity;
	private boolean canBeGranted;
	
	/** Overloaded constructor for the Instruction class
	 * 
	 * @param instruct is the String identifying the instruction
	 * @param type is the type of the resource being modified by the instruction
	 * @param quantity is the quantity of the resource being modified by the instruction
	 */
	public Instruction(String instruct, int type, int quantity)
	{
		this.instruct = instruct;
		this.type = type;
		this.quantity = quantity;
	}
	
	/** Checks to see if an instruction can be granted
	 * @return true if can be granted, false otherwise
	 */
	public boolean isCanBeGranted() {
		return canBeGranted;
	}

	/** Set if an instruction can be granted
	 * @param canBeGranted is true if it can be granted, false otherwise
	 */
	public void setCanBeGranted(boolean canBeGranted) {
		this.canBeGranted = canBeGranted;
	}

	/** Sets the Instruct String 
	 * 
	 * @param instruct is the name of the instruction
	 */
	public void setInstruct(String instruct) {
		this.instruct = instruct;
	}

	/** Returns the Instruct String
	 * 
	 * @return a string representing the name of the instruction
	 */
	public String getInstruct(){
		return instruct;
	}
	/**Returns the type of resource
	 * 
	 * @return an int representing the type of resource
	 */
	public int getType() {
		return type;
	}

	/**Sets the type of the Instruction
	 * 
	 * @param type an int representing the resource type
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**Gets the quantity of the resource
	 * 
	 * @return the amount of the resource as an int
	 */
	public int getQuantity() {
		return quantity;
	}

	/**Sets the quantity of the resource
	 * @param quantity is an int that is the amount of the resource
	 */
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	
}
