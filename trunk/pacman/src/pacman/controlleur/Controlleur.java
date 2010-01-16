package pacman.controlleur;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.Date;

import javax.swing.Timer;

import pacman.modele.Fantome;
import pacman.modele.Game;
import pacman.modele.Level;
import pacman.modele.lang.Direction;
import pacman.modele.lang.LevelListener;
import pacman.modele.lang.PacmanEvent;
import pacman.modele.lang.PacmanListener;
import pacman.vue.Vue;


public class Controlleur implements ActionListener, KeyListener, LevelListener, PacmanListener{

	public static final long serialVersionUID = 1L;
	private static final int FREQUENCE_JEU = 40;
	public static final int TEMPO_DEBUT = 3;
	
	private Timer tJeu, tDebutPartie;
	
	//gameplay
	private boolean pause = false;
	private int tempsPause = -1,
				tempoDebut = TEMPO_DEBUT;
	
	private Game game;
	private Vue vue;
	
	private Date dureeBoucle = new Date();
	
	public Controlleur(){
		super();
		
		game = new Game(this);
		vue = new Vue(this, false);

		vue.addKeyListener(this);
		vue.getMessager().setBigMessage(Integer.toString(tempoDebut));
		
		tJeu = new Timer(FREQUENCE_JEU, this);
		tDebutPartie = new Timer(1000, this);
		tDebutPartie.start();
	}
	
	public static void main(String[] args) throws IOException {
		new Controlleur();
	}

	

	/*		METHODES EVENEMENTIELLES		*/
	//swing
	public void actionPerformed(ActionEvent ae) {
		if(ae.getSource() == tJeu){
			Date ancDate = dureeBoucle;
			dureeBoucle = new Date();
//			setDebugMessage(this, "délai boucle principale: ", Long.toString(dureeBoucle.getTime()));
			
			if(!pause){
				vue.getGameDrawer().corrigerBouchePacman();
				game.tour();
				
				vue.repaint();
			}else if(tempsPause >= 0){
				if(tempsPause-- == 0){
					pause = false;
				}
			}
		}else if(ae.getSource() == tDebutPartie){
			if(--tempoDebut != 0){
				vue.getMessager().setBigMessage(Integer.toString(tempoDebut));
				vue.repaint();
			}else{
				tDebutPartie.stop();
				tJeu.start();
				vue.getMessager().clearBigMessage();
			}
		}
	}

	public void keyTyped(KeyEvent ke) {
		if(ke.getKeyChar() == 'p'){
			pause = !pause;
		}else if(ke.getKeyChar() == 'r'){
//			if(gestionScores != null){
//				setVisible(true);
//				parent.remove(gestionScores);
//				gestionScores = null;
//			}
			reinitialiser();
		}
	}
	
	protected void reinitialiser(){
		vue.getMessager().setBigMessage(Integer.toString(TEMPO_DEBUT));
		game.getLevel().initialiser();
		for(Fantome fantome: game.getFantomes())
			fantome.reinitialiser(true);
		game.getPacman().reinitialiser();
		vue.getMessager().initPointsFantomes();
		vue.getMessager().setMessage("", -1);
		tempoDebut = TEMPO_DEBUT;
		vue.repaint();
	}
	
	protected void reinitialiser(String map){
		vue.getMessager().setBigMessage(Integer.toString(TEMPO_DEBUT));
		game.getLevel().initialiser(map);
		for(Fantome fantome: game.getFantomes())
			fantome.reinitialiser(true);
		game.getPacman().reinitialiser();
		vue.getMessager().initPointsFantomes();
		vue.getMessager().setMessage("", -1);
		tempoDebut = TEMPO_DEBUT;
		vue.repaint();
	}
	
	public void keyPressed(KeyEvent ke) {
		switch(ke.getKeyCode()){
		case 39:
			game.getPacman().setOrientationSuivante(Direction.WE);
			break;
		case 40:
			game.getPacman().setOrientationSuivante(Direction.NS);
			break;
		case 37:
			game.getPacman().setOrientationSuivante(Direction.EW);
			break;
		case 38:
			game.getPacman().setOrientationSuivante(Direction.SN);
			break;
		case 79:
			reinitialiser("O");
			break;
		case 49:
			reinitialiser("1");
			break;
		case 50:
			reinitialiser("2");
			break;
		case 51:
			reinitialiser("3");			
		}
	}

	//inutilisées
	public void keyReleased(KeyEvent arg0) {}

	//level
	public void beginBurstMode() {
		vue.getGameDrawer().setClignotant(3000*Level.DUREE_BURST_MODE/4);
	}

	public void endBurstMode() {
		vue.getGameDrawer().stopClignotant();
	}

	public void levelUp() {
		tJeu.stop();
		tDebutPartie.start();
		
		tempoDebut = TEMPO_DEBUT;
		vue.getMessager().setBigMessage(Integer.toString(TEMPO_DEBUT));
		vue.getMessager().initPointsFantomes();
		vue.getMessager().setMessage("", -1);
	}

	//pacman
	public void estAttrape(PacmanEvent pe) {
		vue.getGameDrawer().resetBouchePacman();
		
		tJeu.stop();
		tDebutPartie.restart();
		tempoDebut = TEMPO_DEBUT;
		vue.getMessager().setBigMessage(Integer.toString(TEMPO_DEBUT));
		vue.getMessager().setMessage(pe.getFantome().getMessage(), Vue.TEMPS_MESSAGE);
	}

	public void aMangeFantome(PacmanEvent pe) {
		int[] point = new int[4];
		point[0] = pe.getPoint();
		point[1] = pe.getFantome().getX();
		point[2] = pe.getFantome().getY();
		point[3] = Vue.TEMPS_POINTS;
		vue.getMessager().addPointsFantomes(point);
		pause(10);
		vue.getMessager().setMessage(pe.getFantome(), Vue.TEMPS_MESSAGE);
	}
	
	//gestion de la fin de partie quand le pacman est mort
	public void estMort(PacmanEvent pe) {
		tJeu.stop();
		tDebutPartie.stop();
		vue.getMessager().setBigMessage("Game Over");

//		this.setVisible(false);
//		gestionScores = new GestionScores(this.parent, pacman.getScore());
//		this.parent.getContentPane().add(gestionScores);
	}
	
	
	public boolean isPaused(){
		return pause;
	}
	
	//pause pour ralentir cycle / -1 = temps indetermine
	private void pause(int delay){
		pause = true;
		tempsPause = delay;
	}
	
	public Vue getVue(){
		return vue;
	}

	public Game getGame() {
		return game;
	}
	
	public void setDebugMessage(Object source, String nom, String message){
		vue.getDebugger().addMessage(source, nom, message);
	}
}
	

	


	
	
	
	
	
	
