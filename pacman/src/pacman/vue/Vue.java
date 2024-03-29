package pacman.vue;

import java.awt.Insets;
import java.awt.Toolkit;

import javax.swing.JFrame;

import pacman.controlleur.Controlleur;
import pacman.modele.Game;
import pacman.modele.Level;
import perso.utils.Debug;

public class Vue extends JFrame {

	private static final long serialVersionUID = 1L;

	/*		CONSTANTES		*/
	public static final int TAILLE_BASE_GOMMES = 3,
		ECHELLE = 2,
		TEMPS_POINTS = 30,//temps d'affichage du score quand on mange un fantome
		TEMPS_MESSAGE = 40,
		TEMPS_CLIGNOTEMENT = 5;
	
	Controlleur controlleur;
	
	private int tailleCase = ECHELLE * Level.PAS;
	private GameDrawer gameDrawer;
	private Messager messager;
	private LevelDrawer levelDrawer;
	private Debug debugger;
	
	public Vue(Controlleur controlleur){
		this(controlleur, false);
	}
	
	public Vue(Controlleur controlleur, boolean debug){
		this.controlleur = controlleur;
		
		setTitle("Pacman java");
//		this.setResizable(false);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setVisible(true);
		
		//il faut que la fenetre soit visible pour calculer sa taille, ne pas deplacer ce code
		Level level = controlleur.getGame().getLevel();
		Insets insets = this.getInsets();
		this.setSize(level.getWidth()*ECHELLE + insets.left + insets.right, level.getHeight() * ECHELLE + insets.top + insets.bottom);
		
		//il faut la fenetre soit dimensionnee avant de la centrer
		int w = Toolkit.getDefaultToolkit().getScreenSize().width;
		int h = Toolkit.getDefaultToolkit().getScreenSize().height;
		this.setLocation((w - this.getWidth())/2, (h - this.getHeight())/2);

		gameDrawer = new GameDrawer(this);
		levelDrawer = new LevelDrawer(this);
		messager = new Messager(this, debug);
		debugger = new Debug(200, 200);
		
		this.getLayeredPane().add(debugger, new Integer(-10));
		this.getLayeredPane().add(gameDrawer, new Integer(-100));
		this.getLayeredPane().add(levelDrawer, new Integer(-200));
		this.getLayeredPane().add(messager, new Integer(-50));
	}

	public Game getGame(){
		return controlleur.getGame();
	}
	
	public int getTailleCase(){
		return tailleCase;
	}

	public GameDrawer getGameDrawer() {
		return gameDrawer;
	}

	public Messager getMessager() {
		return messager;
	}

	public LevelDrawer getLevelDrawer() {
		return levelDrawer;
	}
	
	public Debug getDebugger(){
		return debugger;
	}
}
