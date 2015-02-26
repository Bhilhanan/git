
/**
 * 
 * @author Bhilhanan A Jeyaram
 *
 */
public class Diner {
	private final int arrivalTime, id, numBurgers, numFries;
	private final boolean coke;
	private static int totalDiners = 1;

	public Diner(int arrivalTime, int numBurgers, int numFries, boolean coke) {
		this.arrivalTime = arrivalTime;
		this.numBurgers = numBurgers;
		this.numFries = numFries;
		this.coke = coke;

		this.id = totalDiners;
		totalDiners++;
	}

	public final int getArrivalTime() {
		return arrivalTime;
	}

	public final int getId() {
		return id;
	}

	public final int getBurgerCount() {
		return numBurgers;
	}

	public final int getFriesCount() {
		return numFries;
	}

	public final boolean getCoke() {
		return coke;
	}

}
