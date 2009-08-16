package pacman.modele;

import java.util.Vector;

import pacman.controlleur.Controlleur;

public class Game {
	private Pacman pacman;
	private Vector<Fantome> fantomes;
	private Level level;
	
	Controlleur controlleur;
	
	public Game(Controlleur controlleur){
		this.controlleur = controlleur;

		level = new Level();
		level.addLevelListener(controlleur);

		pacman = new Pacman(level);
		pacman.addPacmanListener(controlleur);

		fantomes = new Vector<Fantome>();
		fantomes.add(new Fantome(level, Fantome.NIVEAU1));
		fantomes.add(new Fantome(level, Fantome.NIVEAU2));
		fantomes.add(new Fantome(level, Fantome.NIVEAU3));
		fantomes.add(new Fantome(level, Fantome.NIVEAU4));
		pacman.addPacmanListener(fantomes.get(0));
		pacman.addPacmanListener(fantomes.get(1));
		pacman.addPacmanListener(fantomes.get(2));
		pacman.addPacmanListener(fantomes.get(3));
	}
	
	public Pacman getPacman() {
		return pacman;
	}
	public void setPacman(Pacman pacman) {
		this.pacman = pacman;
	}
	public Vector<Fantome> getFantomes() {
		return fantomes;
	}
	public void setFantomes(Vector<Fantome> fantomes) {
		this.fantomes = fantomes;
	}
	public Level getLevel() {
		return level;
	}
	public void setLevel(Level level) {
		this.level = level;
	}
}
