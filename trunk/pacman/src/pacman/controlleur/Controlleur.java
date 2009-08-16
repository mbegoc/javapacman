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
import pacman.modele.Pacman;
import pacman.modele.lang.LevelListener;
import pacman.modele.lang.PacmanEvent;
import pacman.modele.lang.PacmanListener;
import pacman.vue.Vue;


public class Controlleur implements ActionListener, KeyListener, LevelListener, PacmanListener{

	public static final long serialVersionUID = 1L;
	private static final int FREQUENCE_JEU = 50;
	public static final int TEMPO_DEBUT = 5;//temporisation au début en secondes
	
	private Timer tJeu, tDebutPartie;
	
	//gameplay
	private boolean pause = false;
	private int tempsPause = -1;
	
	private Game game;
	private Vue vue;
	
	private Date dureeBoucle = new Date();
	
	public Controlleur(){
		super();
		
		game = new Game(this);
		vue = new Vue(this, true);

		vue.addKeyListener(this);
		
		tJeu = new Timer(FREQUENCE_JEU, this);
		tDebutPartie = new Timer(TEMPO_DEBUT * 1000, this);
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
			System.out.print(dureeBoucle.getTime() - ancDate.getTime() + " - ");
			
			if(!pause){
				vue.getGameDrawer().corrigerBouchePacman();
				
				game.getPacman().deplacer();
				for(Fantome fantome: game.getFantomes()){
					fantome.deplacer();
					game.getPacman().confronter(fantome);
				}
				
				vue.getGameDrawer().repaint();
			}else if(tempsPause >= 0){
				if(tempsPause-- == 0){
					pause = false;
				}
			}
		}else if(ae.getSource() == tDebutPartie){
			tDebutPartie.stop();
			tJeu.start();
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
			game.getLevel().initialiser();
			for(Fantome fantome: game.getFantomes())
				fantome.reinitialiser(true);
			game.getPacman().reinitialiser();
			vue.getMessager().initPointsFantomes();
			vue.getMessager().setMessage("", -1);
		}	
	}
	
	public void keyPressed(KeyEvent ke) {
		switch(ke.getKeyCode()){
		case 39:
			game.getPacman().setOrientationSuivante(Pacman.WE);
			break;
		case 40:
			game.getPacman().setOrientationSuivante(Pacman.NS);
			break;
		case 37:
			game.getPacman().setOrientationSuivante(Pacman.EW);
			break;
		case 38:
			game.getPacman().setOrientationSuivante(Pacman.SN);
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
		
		vue.getMessager().initPointsFantomes();
		vue.getMessager().setMessage("", -1);
	}

	//pacman
	public void estAttrape(PacmanEvent pe) {
		vue.getGameDrawer().resetBouchePacman();
		
		tJeu.stop();
		tDebutPartie.restart();
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
	
	public void setDebugMessage(String message){
		
	}
}
	

	


	
	
	
	
	
	
