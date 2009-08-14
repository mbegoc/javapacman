package pacman;

import java.util.EventObject;

public class LevelEvent extends EventObject {
	private int type;
	private String message;
	
	public final static int NOUVEAU_NIVEAU_CODE = 1;
	public final static int RESET_CODE = 2;
	public final static int BEGIN_BURSTMODE_CODE = 3;
	public final static int END_BURSTMODE_CODE = 4;
	
	public final static String NOUVEAU_NIVEAU_MESSAGE = "Nouveau niveau !";
	public final static String RESET_MESSAGE = "Le pacman est mort.";
	public final static String BEGIN_BURSTMODE_MESSAGE = "Debut du mode invincible du Pacman";
	public final static String END_BURSTMODE_MESSAGE = "Fin du mode invincible du Pacman";

	public LevelEvent(Object source, int type, String message) {
		super(source);
		this.type = type;
		this.message = message;
	}

	public int getType() {
		return type;
	}

	public String getMessage() {
		return message;
	}

}
