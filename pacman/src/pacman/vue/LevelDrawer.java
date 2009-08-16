package pacman.vue;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Date;

import javax.swing.JPanel;

import pacman.modele.Level;

	//	SOUS CLASSE POUR LA ZONE DE JEU
	public class LevelDrawer extends JPanel {
		private static final long serialVersionUID = 1L;
		
		private Vue parent;
		private Level level;

		public LevelDrawer(Vue parent) {
			super();
			this.parent = parent;
			level = parent.getGame().getLevel();
			this.setBackground(Color.BLACK);
			setSize(level.getWidth()*Vue.ECHELLE, level.getHeight()*Vue.ECHELLE);	
		}
		
		public void paintComponent(Graphics g){
			Date debut = new Date();
			Graphics2D g2 = (Graphics2D)g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
			super.paintComponent(g2);
			
			//dessin du plateau de jeu
			for(int i = 0 ; i < level.getWidth() ; i++){
				for(int j = 0 ; j < level.getHeight() ; j++){
					dessinerMur(i, j, g2);
				}
			}
			System.err.print((new Date()).getTime() - debut.getTime() + " - ");
		}
		
		private void dessinerMur(int x, int y, Graphics2D g){
			int type = level.getCase(x, y);
			int marge = Level.MARGE;
			int tailleCase = parent.getTailleCase();
			
			//on inverse la marge si le mur se trouve sur un chemin, car ce ne sera plus un retrait dans le même sens
			if(level.isOnPath(x, y))
				marge *= -1;
			
			x*=Vue.ECHELLE;
			y*=Vue.ECHELLE;
			
			g.setColor(Color.BLUE);

			//gauche
			if((type & Level.GAUCHE) != 0 && (type & Level.BAS) == 0 && (type & Level.HAUT) == 0)
				g.drawLine(x+marge, y, x+marge, y+tailleCase);

			//droite
			if((type & Level.HAUT) != 0 && (type & Level.GAUCHE) == 0 && (type & Level.DROITE) == 0)
				g.drawLine(x+tailleCase, y+marge, x, y+marge);

			//gauche
			if((type & Level.BAS) != 0 && (type & Level.GAUCHE) == 0 && (type & Level.DROITE) == 0)
				g.drawLine(x, y+tailleCase-marge, x+tailleCase, y+tailleCase-marge);

			//droite
			if((type & Level.DROITE) != 0 && (type & Level.HAUT) == 0 && (type & Level.BAS) == 0)
				g.drawLine(x+tailleCase-marge, y, x+tailleCase-marge, y+tailleCase);

			//bas gauche
			if((type & Level.BAS_GAUCHE) == Level.BAS_GAUCHE){
				g.drawArc(x+marge, y+marge, tailleCase-marge*2, tailleCase-marge*2, -90, -90);
				//demi ligne bas
				g.drawLine(x+tailleCase/2, y+tailleCase-marge, x+tailleCase, y+tailleCase-marge);
				//demi ligne gauche
				g.drawLine(x+marge, y, x+marge, y+tailleCase/2);
			}

			//bas droite
			if((type & Level.BAS_DROITE) == Level.BAS_DROITE){
				g.drawArc(x+marge, y+marge, tailleCase-marge*2, tailleCase-marge*2, 0, -90);
				//demi ligne bas
				g.drawLine(x, y+tailleCase-marge, x+tailleCase/2, y+tailleCase-marge);
				//demi ligne droite
				g.drawLine(x+tailleCase-marge, y, x+tailleCase-marge, y+tailleCase/2);
			}

			//haut gauche
			if((type & Level.HAUT_GAUCHE) == Level.HAUT_GAUCHE){
				g.drawArc(x+marge, y+marge, tailleCase-marge*2, tailleCase-marge*2, 90, 90);
				//demi ligne haut
				g.drawLine(x+tailleCase/2, y+marge, x+tailleCase, y+marge);
				//demi ligne gauche
				g.drawLine(x+marge, y+tailleCase/2, x+marge, y+tailleCase);
			}
			
			//haut droite
			if((type & Level.HAUT_DROITE) == Level.HAUT_DROITE){
				g.drawArc(x+marge, y+marge, tailleCase-marge*2, tailleCase-marge*2, 0, 90);
				//demi ligne haut
				g.drawLine(x, y+marge, x+tailleCase/2, y+marge);
				//demi ligne droite
				g.drawLine(x+tailleCase-marge, y+tailleCase/2, x+tailleCase-marge, y+tailleCase);
			}
		}	
	}