package pacman.controlleur;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.Timer;

import pacman.modele.Fantome;
import pacman.modele.Game;
import pacman.modele.Level;
import pacman.modele.Pacman;
import pacman.modele.lang.Direction;
import pacman.modele.lang.LevelListener;
import pacman.modele.lang.PacmanEvent;
import pacman.modele.lang.PacmanListener;
import pacman.vue.Vue;


public class Controlleur implements ActionListener, KeyListener, LevelListener, PacmanListener{

	public static final long serialVersionUID = 1L;
	private static final int FREQUENCE_JEU = 40;
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
		vue = new Vue(this, false);

		vue.addKeyListener(this);
		
		tJeu = new Timer(FREQUENCE_JEU, this);
		tDebutPartie = new Timer(TEMPO_DEBUT * 1000, this);
		tDebutPartie.start();
		
//		try {
//			AudioInputStream a = AudioSystem.getAudioInputStream(new File("src/pacman/sounds/burzum.wav"));
//		    DataLine.Info info = new DataLine.Info(Clip.class, a.getFormat());
//		    Clip c = (Clip) AudioSystem.getLine(info);
//		    c.open(a);
//			c.start();
//		} catch (UnsupportedAudioFileException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (LineUnavailableException e) {
//			e.printStackTrace();
//		}
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
	
	public void setDebugMessage(Object source, String nom, String message){
		vue.getDebugger().addMessage(source, nom, message);
	}
}
	

	


	
	
	
	
	
	
