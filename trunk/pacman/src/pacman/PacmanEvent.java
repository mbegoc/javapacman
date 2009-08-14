package pacman;

import java.util.EventObject;

public class PacmanEvent extends EventObject {
	private int point = 0;
	private Fantome fantome;
	
	public PacmanEvent(Object source, Fantome fantome, int point) {
		super(source);
		this.point = point;
		this.fantome = fantome;
	}

	public PacmanEvent(Object source, Fantome fantome) {
		super(source);
		this.fantome = fantome;
	}

	public int getPoint() {
		return point;
	}

	public Fantome getFantome() {
		return fantome;
	}

}
