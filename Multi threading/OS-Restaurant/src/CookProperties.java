
/**
 * 
 * @author Bhilhanan A Jeyaram
 *
 */
public class CookProperties {
	public Diner serving_diner;
	public Object lock_onDiner;
	public Object orderedFood;
	public int id;

	public CookProperties(Diner diner, Object lock_onDiner, Object orderedFood) {
		this.serving_diner = diner;
		this.lock_onDiner = lock_onDiner;
		this.orderedFood = orderedFood;
	}
}
