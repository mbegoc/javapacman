/**
 * 
 */
package pacman.modele;

import java.awt.Toolkit;
import java.util.Vector;

import pacman.modele.lang.Direction;
import pacman.modele.lang.LevelListener;
import pacman.modele.lang.PacmanEvent;
import pacman.modele.lang.PacmanListener;



/**
 * @author a8begocmi
 *
 */

public class Pacman implements LevelListener{
	//	CONSTANTES
	public final static int
		ANGLE_BOUCHE = 45,
		DEPLACEMENT = 4,
		POINTS_FANTOMES = 200;

	//VARIABLES
	private int x, y,
		score = 0,
		vies = 2,
		pointsFantomes = POINTS_FANTOMES;
	
	private Level level;
	private Direction orientation = Direction.EW,
		orientSuivante = Direction.NONE;
	
	public boolean enMouvement = false,
		burstMode = false,
		pause = false;
	private int tempsPause = -1;

	private Vector<PacmanListener> listeners = new Vector<PacmanListener>();
	

	
	public Pacman(Level level){
		this.level = level;
		level.addLevelListener(this);
		
		this.x = level.getXDepart();
		this.y = level.getYDepart();
	}
	
	public void deplacer(){
		if(!pause){
			if(enMouvement){
				//on fait d'abord le calcul pour l'orientation suivante qu'on stockera dans un tableau
				int[] position = new int[2];
				position[0] = x;
				position[1] = y;
				calculerDeplacement(position, orientSuivante);
				
				//si la nouvelle position est sur un chemin, on la confirme
				if(level.isOnPath(position[0], position[1])){
					x = position[0];
					y = position[1];
					orientation = orientSuivante;
				}else{
					//si la nouvelle orientation ne fonctionne pas, on fait le calcul pour la direction actuelle
					position[0] = x;
					position[1] = y;
					calculerDeplacement(position, orientation);
					if(level.isOnPath(position[0], position[1])){
						x = position[0];
						y = position[1];
					}else{
						//sinon encore on s'arrete
						enMouvement = false;
					}
				}
				//on gere les gains obtenus à cette position
				gain(level.manger(x, y));
			}
		}else if(tempsPause > 0){
			if(--tempsPause == 0){
				pause = false;
			}
		}
	}
	
	public void calculerDeplacement(int[] position, Direction orientation){
		//on commence par calculer le deplacement normal
		switch(orientation){
		case WE:
			position[0] += DEPLACEMENT;
			break;
		case NS:
			position[1] += DEPLACEMENT;
			break;
		case EW:
			position[0] -= DEPLACEMENT;
			break;
		case SN:
			position[1] -= DEPLACEMENT;
		}
		
		//puis on regarde si le pacman est arrivé au bord de la carte pour le faire passer de l'autre côté de la map
		int limiteWidth = level.getWidth() - Level.PAS;
		int limiteHeight = level.getHeight() - Level.PAS;
		
		if(position[0] >= limiteWidth)
			position[0] -= limiteWidth;
		else if(position[0] < 0)
			position[0] += limiteWidth;
		
		if(position[1] >= limiteHeight)
			position[1] -= limiteHeight;
		else if(position[1] < 0)
			position[1] += limiteHeight;
	}
	
	public void confronter(Fantome fantome){
		int xDiff = fantome.getX() - x;
		int yDiff = fantome.getY() - y;
		if(Math.abs(xDiff) < Pacman.DEPLACEMENT*2){
			if(Math.abs(yDiff) < Pacman.DEPLACEMENT*2){
				//on mange le fantome
				if(fantome.getBurstMode()){
					if(fantome.estManger()){
						Toolkit.getDefaultToolkit().beep();
						gain(pointsFantomes);
						mangeFantome(fantome);
						pointsFantomes *= 2;
					}
				//ou on se fait bouffer
				}else if(fantome.estHabille()){
					Toolkit.getDefaultToolkit().beep();
					x = level.getXDepart();
					y = level.getYDepart();
					orientation = Direction.EW;
					estAttrape(fantome);
					if(--vies < 0)
						estMort(fantome);
				}
			}
		}
	}
	
	//gestion du score 
	private void gain(int points){
		if(points > 0){
			pause(1);
			int ancienScore = score;
			score += points;
			int multiple = 1;
			while(multiple < ancienScore/10000)
				multiple *= 2;
			if(ancienScore/10000/multiple != score/10000/multiple)
				vies++;
		}
	}
	
	//une pause pour un certain nombre de cycle ou indéfini avec -1
	private void pause(int delay){
		pause = true;
		tempsPause = delay;
	}

	public void reinitialiser() {
		x = level.getXDepart();
		y = level.getYDepart();
		vies = 2;
		score = 0;
		pointsFantomes = POINTS_FANTOMES;
		orientation = Direction.EW;
		orientSuivante = Direction.NONE;
		enMouvement = false;
		burstMode = false;
		pause = false;
		tempsPause = -1;
	}

	
	public String toString(){
		String retour = "Position: x=" + x + " ; y=" + y + "  |  ";
		retour += "Orientation=" + orientation+" ; " + "Orientation Suivante=" + orientSuivante + "  |  ";
		retour += "En mouvement=" + enMouvement + " | ";
		retour += "Vies=" + vies;
		return retour;
	}
	
	/*		GETTERS / SETTERS		*/
	public int getX(){
		return x;
	}
	
	public int getY(){
		return y;
	}
	
	public int getScore(){
		return score;
	}
	
	public Direction getOrientation(){
		return orientation;
	}

	public int getVies(){
		return vies;
	}
	
	
	
	/*		GENERATION DES EVENEMENTS		*/
	private void mangeFantome(Fantome fantome){
		PacmanEvent pe = new PacmanEvent(this, fantome, pointsFantomes);
		for(PacmanListener listener: listeners)
			listener.aMangeFantome(pe);
	}
	
	private void estAttrape(Fantome fantome){
		PacmanEvent pe = new PacmanEvent(this, fantome);
		for(PacmanListener listener: listeners){
			listener.estAttrape(pe);
		}
	}
	
	private void estMort(Fantome fantome){
		PacmanEvent pe = new PacmanEvent(this, fantome);
		for(PacmanListener listener: listeners){
			listener.estMort(pe);
		}
	}
	
	public void addPacmanListener(PacmanListener pl){
		listeners.add(pl);
	}
	
	public void removePacmanListener(PacmanListener pl){
		listeners.remove(pl);
	}

	public void setOrientationSuivante(Direction orient){
		orientSuivante = orient;
		enMouvement = true;
	}

	public void levelUp() {
		this.x = level.getXDepart();
		this.y = level.getYDepart();
		this.enMouvement = false;
		this.orientation = Direction.EW;
		pointsFantomes = POINTS_FANTOMES;
		burstMode = false;
	}

	public void beginBurstMode() {
		burstMode = true;
		pointsFantomes = POINTS_FANTOMES;
	}

	public void endBurstMode() {
		burstMode = false;
		pointsFantomes = POINTS_FANTOMES;
	}
}
