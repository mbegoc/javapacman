package pacman.vue;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.Timer;

import pacman.modele.Fantome;
import pacman.modele.Level;
import pacman.modele.Pacman;

public class GameDrawer extends JPanel implements ActionListener{
	private static final long serialVersionUID = 1L;

	/*		VARIABLES		*/
	private Vue parent;
	
	//pour la bouche du pacman
	private int modifAngle = 0;
	private int valModifAngle = -10;
	
	private Timer tClignotant;
	private boolean clignotant = true;
	
	public GameDrawer(Vue parent) {
		super();
		this.setOpaque(false);
		
		this.parent = parent;
		
		tClignotant = new Timer(Vue.TEMPS_CLIGNOTEMENT, this);
		
		setSize(parent.getGame().getLevel().getWidth()*Vue.ECHELLE, parent.getGame().getLevel().getHeight()*Vue.ECHELLE);
	}

	

	public void paintComponent(Graphics g){
		Date debut = new Date();
		Level level = parent.getGame().getLevel();
		
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		super.paintComponent(g2);
		
		//dessin des gommes
		for(int i = 0 ; i < level.getWidth() ; i++){
			for(int j = 0 ; j < level.getHeight() ; j++){
				dessinerGommes(i, j, g2);
			}
		}
		
		dessinerPacman(g2);
		dessinerFantomes(g2);
		
//		System.out.print((new Date()).getTime() - debut.getTime() + " - ");
	}
	
	private void dessinerPacman(Graphics2D g2){
		Pacman pacman = parent.getGame().getPacman();
		
		//on commence par calculer l'angle de la bouche du pacman en fonction de son orientation
		int angleDepart = Pacman.ANGLE_BOUCHE;
		int angle = 270;
		switch(pacman.getOrientation()){
		case Pacman.NS:
			angleDepart=Pacman.ANGLE_BOUCHE+270;
			break;
		case Pacman.EW:
			angleDepart=Pacman.ANGLE_BOUCHE+180;
			break;
		case Pacman.SN:
			angleDepart=Pacman.ANGLE_BOUCHE+90;
		}
		
		//on modifie cet angle pour ouvrir et fermer la bouche
		if(pacman.enMouvement){
			angleDepart -= modifAngle;
			angle += (modifAngle*2);
		}
		
		g2.setColor(Color.YELLOW);
		g2.fillArc(pacman.getX()*Vue.ECHELLE, pacman.getY()*Vue.ECHELLE, parent.getTailleCase(), parent.getTailleCase(), angleDepart, angle);			
	}
	
	private void dessinerFantomes(Graphics2D g2){
		Vector<Fantome> fantomes = parent.getGame().getFantomes();
		
		for(Fantome fantome: fantomes){
			if(fantome.getBurstMode())
				if(clignotant)
					g2.setColor(Color.BLUE);
				else
					g2.setColor(Color.WHITE);
			else
				g2.setColor(fantome.getColor());
			
			int x = fantome.getX()*Vue.ECHELLE;
			int y = fantome.getY()*Vue.ECHELLE;
			int demi = parent.getTailleCase()/2;
			int tiers = parent.getTailleCase()/3;
			int quart = parent.getTailleCase()/4;
			int troisQuart = 3*quart;
			//taille des yeux
			int blanc = parent.getTailleCase()/3;
			int pupille = parent.getTailleCase()/6;
			
			if(fantome.estHabille()){
				//haut
				g2.fillArc(x, y, parent.getTailleCase(), parent.getTailleCase(), 0, 180);
				g2.fillRect(x, y+demi, parent.getTailleCase(), tiers);
				//gondoles
				g2.fillOval(x, y+troisQuart, quart, quart);
				g2.fillOval(x+quart, y+troisQuart, quart, quart);
				g2.fillOval(x+demi, y+troisQuart, quart, quart);
				g2.fillOval(x+troisQuart, y+troisQuart, quart, quart);
			}
			
			// blanc yeux
			g2.setColor(Color.WHITE);
			g2.fillOval(x+troisQuart-blanc/2, y+quart, blanc, blanc);
			g2.fillOval(x+quart-blanc/2, y+quart, blanc, blanc);
			//pupille
			g2.setColor(Color.BLACK);
			int xPupilleDroite = x+troisQuart-blanc/4;
			int xPupilleGauche = x+quart-blanc/4;
			int yPupilles = y+quart+blanc/4;
			
			if(!fantome.getBurstMode() || !fantome.estHabille()){
				if(fantome.getOrientation() == Fantome.WE){
					xPupilleDroite += pupille/2;
					xPupilleGauche += pupille/2;
				}else if(fantome.getOrientation() == Fantome.EW){
					xPupilleDroite -= pupille/2;
					xPupilleGauche -= pupille/2;
				}else if(fantome.getOrientation() == Fantome.NS){
					yPupilles += pupille/2;
				}else if(fantome.getOrientation() == Fantome.SN){					
					yPupilles -= pupille/2;
				}
			}
			g2.fillOval(xPupilleDroite, yPupilles, pupille, pupille);
			g2.fillOval(xPupilleGauche, yPupilles, pupille, pupille);
		}
	}
	
	private void dessinerGommes(int x, int y, Graphics2D g){
		
		int type = parent.getGame().getLevel().getCase(x, y);
		
		x*=Vue.ECHELLE;
		y*=Vue.ECHELLE;
		
		Color c = g.getColor();
		g.setColor(Color.WHITE);
		
		if((type & Level.NORMALE) != 0){
			int tailleGomme = Vue.TAILLE_BASE_GOMMES * Vue.ECHELLE;
			int decallage = (parent.getTailleCase() - tailleGomme) / 2;
			g.fillOval(x+decallage, y+decallage, tailleGomme, tailleGomme);
		}else if((type & Level.SUPER) != 0){
			int tailleGomme = Vue.TAILLE_BASE_GOMMES * Vue.ECHELLE * 2;
			int decallage = (parent.getTailleCase() - tailleGomme) / 2;
			g.fillOval(x+decallage, y+decallage, tailleGomme, tailleGomme);
		}
		
		g.setColor(c);
	}
	
	public void corrigerBouchePacman(){
		if(modifAngle <= 0 || modifAngle >= Pacman.ANGLE_BOUCHE)
			valModifAngle *= -1;
		modifAngle += valModifAngle;		
	}
	
	public void resetBouchePacman(){
		modifAngle = 0;
		valModifAngle = -10;		
	}
	
	public void setClignotant(int delay){
		tClignotant.setDelay(delay);
		tClignotant.start();
		clignotant = true;		
	}
	
	public void stopClignotant(){
		tClignotant.stop();
	}
	
	public void actionPerformed(ActionEvent ae) {
		if(ae.getSource() == tClignotant){
			if(tClignotant.getDelay() != Vue.TEMPS_CLIGNOTEMENT*100)
				tClignotant.setDelay(Vue.TEMPS_CLIGNOTEMENT*100);
			else
				clignotant = !clignotant;
		}
	}	
}