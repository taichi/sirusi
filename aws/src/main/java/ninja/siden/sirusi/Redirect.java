package ninja.siden.sirusi;

public class Redirect extends RuntimeException {

	private static final long serialVersionUID = -2829555627800387809L;

	public Redirect(String location) {
		super(location);
	}
}
