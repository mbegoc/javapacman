/**
 * 
 */
package pacman.modele;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Random;
import java.util.Vector;

import javax.swing.Timer;

import pacman.controlleur.Controlleur;
import pacman.modele.lang.LevelListener;
import pacman.modele.lang.PacmanEvent;
import pacman.modele.lang.PacmanListener;


public class Fantome implements LevelListener, PacmanListener, ActionListener{
	
	//CONSTANTES
	public final static int
		WE = 1,
		NS = 2,
		EW = 3,
		SN = 4;
	/*ces valeurs doivent impérativement 
	être un diviseur du pas du jeu, sinon les fantomes se decallent 
	dans la grille du jeu et ne peuvent plus circuler normalement*/
	private final static int VITESSE_NORMALE = 3,
		VITESSE_LENTE = 1;
	
	public final static int NIVEAU1 = 1,
	NIVEAU2 = 2,
	NIVEAU3 = 3,
	NIVEAU4 = 4;
	
	//
	private int x, y, 
		deplacement = VITESSE_NORMALE,
		difficulte;
	private int orientation = SN,
		orientSuivante = 0;
	private boolean libre = false,//peut sortir du dressing
		habille = true,
		burstMode = false;
	private Color couleur;
	private String nom = "",
		message = "";

	private Level level;
	private Pacman pacman;
	//le temps pour chaque fantome avant qu'il ne sorte du dressing
	private Timer peaceTime;

	
	
	
	public Fantome(Level level, Pacman pacman, int difficulte){
		this.difficulte = difficulte;
		this.level = level;
		this.pacman = pacman;
		level.addLevelListener(this);
		
		if(difficulte == NIVEAU4){
			couleur = Color.RED;
			nom = "Blinky";
			message = "Blinky t'as eu ! =P";
			peaceTime = new Timer((Controlleur.TEMPO_DEBUT + 6)*1000, this);
		}else if(difficulte == NIVEAU3){
			couleur = Color.PINK;
			nom = "Pinky";
			message = "Pinky n'est pas pire xD";
			peaceTime = new Timer((Controlleur.TEMPO_DEBUT + 4)*1000, this);
		}else if(difficulte == NIVEAU2){
			couleur = Color.CYAN;
			nom = "Inky";
			message = "Inky t'as effacé ;)";
			peaceTime = new Timer((Controlleur.TEMPO_DEBUT + 2)*1000, this);
		}else if(difficulte == NIVEAU1){
			couleur = Color.ORANGE;
			nom = "Clyde";
			message = "Erf, même Clyde est capable de t'avoir =D";
			peaceTime = new Timer(Controlleur.TEMPO_DEBUT * 1000, this);
		}
		this.x = level.getXDepart(this);
		this.y = level.getYDepart(this);
		peaceTime.start();
	}
	
	public void liberer(){
		peaceTime.stop();
		libre = true;
	}

	public void deplacer(){
		int[] position = new int[2];
		position[0] = x;
		position[1] = y;
		//si on est libre et sur le chemin, on bouge
		if(!habille && x == level.getXDressing() && y == level.getYDressing()){
			allerDepart();
		}else if((level.isInDressing(x, y) || level.estPorteDressing(x, y)) && !habille){
			allerDepart();
		}else if(libre 
					&& (level.isInDressing(position[0], position[1]) || level.estPorteDressing(position[0], position[1])) 
					&& (position[0] != level.getXDressing() || position[1] != level.getYDressing())){ 
			allerPorteDressing();		
		}else if(!habille){ 
			allerPorteDressing();
		//sinon on cherche une nouvelle direction
		}else{
			orienter(5);
			calculerDeplacement(position);
			x = position[0];
			y = position[1];
		}
		
		//on en profite pour rabiller notre fantome s'il est arrivé dans sa penderie
		if(level.getXDepart(this) == x && level.getYDepart(this) == y && habille == false){
			reinitialiser(false);
		}

	}
	
	public void calculerDeplacement(int[] position){
		//on commence par calculer le deplacement normal
		switch(orientation){
		case WE:
			position[0] += deplacement;
			break;
		case NS:
			position[1] += deplacement;
			break;
		case EW:
			position[0] -= deplacement;
			break;
		case SN:
			position[1] -= deplacement;
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
	
	private void orienter(int chance){
		Vector<Integer> positionsPossibles = new Vector<Integer>();
		int[] position = new int[2];
		position[0] = x;
		position[1] = y;
		calculerDeplacement(position);
		boolean enAvant = level.isOnPath(position[0], position[1]);
			
		//si il est possible de continuer sur la même route, il y a plus de chance que cette route soit choisie
		
		if(enAvant)
			for(int i = 0 ; i < chance ; i++)
				positionsPossibles.add(orientation);
		
		if(libre){
			if(level.isOnPath(x-1, y) && orientation != WE){
				setDifficulte(positionsPossibles, EW);
			}
			if(level.isOnPath(x+1, y) && orientation != EW){
				setDifficulte(positionsPossibles, WE);
			}
			if(level.isOnPath(x, y+1) && orientation != SN){
				setDifficulte(positionsPossibles, NS);
			}
			if(level.isOnPath(x, y-1) && orientation != NS){
				setDifficulte(positionsPossibles, SN);
			}
		}else{
			if(level.isInDressing(x-1, y) && orientation != WE){
				positionsPossibles.add(EW);
			}
			if(level.isInDressing(x+1, y) && orientation != EW){
				positionsPossibles.add(WE);
			}
			if(level.isInDressing(x, y+1) && orientation != SN){
				positionsPossibles.add(NS);
			}
			if(level.isInDressing(x, y-1) && orientation != NS){
				positionsPossibles.add(SN);
			}
			/*dans le parcours normal, il n'est pas possible de ne 
			pas trouver de chemin, mais dans le dressing, Ã§a peut arriver au niveau de la sortie, 
			à ce moment, il faut revenir sur ses pas*/
			if(positionsPossibles.size() == 0){
				orientation = orientationInverse(orientation);
			}
		}
		if(positionsPossibles.size() != 0){
			Random r = new Random();
			orientation = positionsPossibles.get(r.nextInt(positionsPossibles.size()));
		}
	}
	
	private void setDifficulte(Vector<Integer> positionsPossibles, int direction){
		//on commence par déterminer si cette direction est avantageuse pour attraper le Pacman
		boolean avantageux = false;
		if(direction == SN && (y - pacman.getY() > 0)){
			avantageux = true;
		}else if(direction == NS && (y - pacman.getY() < 0)){
			avantageux = true;
		}else if(direction == EW && (x - pacman.getX() > 0)){
			avantageux = true;
		}else if(direction == WE && (x - pacman.getX() < 0)){
			avantageux = true;
		}
		
		//ensuite, on va ajouter plus ou moins de fois la position pour influencer les 
		//chances que cette direction soit choisie en fonction du couple niveau fantome / avantage
		int qte = 1;
		if(habille){
			if(difficulte == NIVEAU4){
				if(avantageux && !burstMode)
					qte = 100;
				else if(!avantageux && burstMode)
					qte = 10;
			}
	
			if(difficulte == NIVEAU3){
				if(avantageux && !burstMode)
					qte = 10;
				else if(!avantageux && burstMode)
					qte = 5;
			}
	
			if(difficulte == NIVEAU1){
				if(!avantageux && !burstMode)
					qte = 5;
				else if(avantageux && burstMode)
					qte = 5;
			}
		}

		ajouterChances(positionsPossibles, direction, qte);
	}
	
	private int orientationInverse(int direction){
		if(direction == WE)
			direction = EW;
		if(direction == EW)
			direction = WE;
		if(direction == NS)
			direction = SN;
		if(direction == SN)
			direction = NS;
		return direction;
	}
	
	private void ajouterChances(Vector<Integer> positionsPossibles, int direction, int quantite){
		for(int i = 0 ; i < quantite ; i++)
			positionsPossibles.add(direction);
	}
	
	private void allerPorteDressing(){
		if(level.isInDressing(x, y) || level.estPorteDressing(x, y)){
			if(x < level.getXDressing() && (level.isInDressing(x+deplacement, y) || level.estPorteDressing(x+deplacement, y)))
				x += deplacement;
			else if(x > level.getXDressing() && (level.isInDressing(x-deplacement, y) || level.estPorteDressing(x-deplacement, y)))
				x -= deplacement;
			
			if(y < level.getYDressing() && (level.isInDressing(x, y+deplacement) || level.estPorteDressing(x, y+deplacement)))
				y += deplacement;
			else if(y > level.getYDressing() && (level.isInDressing(x, y-deplacement) || level.estPorteDressing(x, y-deplacement)))
				y -= deplacement;

		}else if(level.isOnPath(x, y)){
			//on commence par identifier les chemins possibles
			Vector<Integer> positionsPossibles = new Vector<Integer>();
			if(level.isOnPath(x-1, y) && orientation != WE){
				positionsPossibles.add(EW);
			}
			if(level.isOnPath(x+1, y) && orientation != EW){
				positionsPossibles.add(WE);
			}
			if(level.isOnPath(x, y+1) && orientation != SN){
				positionsPossibles.add(NS);
			}
			if(level.isOnPath(x, y-1) && orientation != NS){
				positionsPossibles.add(SN);
			}
			if(level.getXDressing() - x >= 0)
				if(positionsPossibles.indexOf(EW) != -1 && positionsPossibles.size() > 1){
					positionsPossibles.remove(positionsPossibles.indexOf(EW));
				}
			if(level.getXDressing() - x <= 0)
				if(positionsPossibles.indexOf(WE) != -1 && positionsPossibles.size() > 1){
					positionsPossibles.remove(positionsPossibles.indexOf(WE));
				}
			if(level.getYDressing() - y >= 0)
				if(positionsPossibles.indexOf(SN) != -1 && positionsPossibles.size() > 1){
					positionsPossibles.remove(positionsPossibles.indexOf(SN));
				}
			if(level.getYDressing() - y <= 0)
				if(positionsPossibles.indexOf(NS) != -1 && positionsPossibles.size() > 1){
					positionsPossibles.remove(positionsPossibles.indexOf(NS));
				}
			if(positionsPossibles.size() != 0){
				Random r = new Random();
				orientation = positionsPossibles.get(r.nextInt(positionsPossibles.size()));
			}

			int[] position = new int[2];
			position[0] = x;
			position[1] = y;
			calculerDeplacement(position);

			x = position[0];
			y = position[1];
		}

		if(Math.abs(level.getXDressing() - x) < deplacement)
			x = level.getXDressing();
		if(Math.abs(level.getYDressing() - y) < deplacement)
			y = level.getYDressing();

	}
	
	private void allerDepart(){
		if(x < level.getXDepart(this) && (level.isInDressing(x+deplacement, y) || level.estPorteDressing(x+deplacement, y)))
			x += deplacement;
		else if(x > level.getXDepart(this) && (level.isInDressing(x-deplacement, y) || level.estPorteDressing(x-deplacement, y)))
			x -= deplacement;
		
		if(y < level.getYDepart(this) && (level.isInDressing(x, y+deplacement) || level.estPorteDressing(x, y+deplacement)))
			y += deplacement;
		else if(y > level.getYDepart(this) && (level.isInDressing(x, y-deplacement) || level.estPorteDressing(x, y-deplacement)))
			y -= deplacement;

		if(Math.abs(level.getXDepart(this) - x) < deplacement)
			x = level.getXDepart(this);
		if(Math.abs(level.getYDepart(this) - y) < deplacement)
			y = level.getYDepart(this);
	}
	//le fantome se fait mangé
	public boolean estManger(){
		if(habille){
			habille = false;
			deplacement = VITESSE_NORMALE;
			resetPosition();
			return true;
		}else{
			return false;
		}
	}
	
	//cette fonction sert à  repositionner proprement le fantome dans la grille quand il change de vitesse
	//en effet, quand il passe d'une vitesse lente (probablement 1 case de déplacement) à  une autre vitesse,
	//sa position peut ne plus être en phase avec les intersections de chemins
	public void resetPosition(){
		x = (x/Level.PAS)*Level.PAS;
		y = (y/Level.PAS)*Level.PAS;		
	}
	
	public String toString(){
		String retour = "Position: x=" + x + " ; y=" + y + "  |  ";
		retour += "Orientation=" + orientation+" ; " + "Orientation Suivante=" + orientSuivante;
		return retour;
	}
	
	
	
	/*		GETTERS / SETTERS		*/
	public int getX(){
		return x;
	}
	
	public int getY(){
		return y;
	}
	
	public int getOrientation(){
		return orientation;
	}

	public Color getColor(){
		return couleur;
	}

	public String getNom(){
		return nom;
	}

	public String getMessage(){
		return message;
	}
	
	public boolean estHabille(){
		return habille;
	}
	
	public boolean getBurstMode(){
		return burstMode;
	}
	
	public void reinitialiser(boolean delay){
		burstMode = false;
		habille = true;
		deplacement = VITESSE_NORMALE;
		this.x = level.getXDepart(this);
		this.y = level.getYDepart(this);
		if(delay){
			peaceTime.start();
			libre = false;
		}else{
			libre = true;
		}
		this.orientation = SN;
		this.orientSuivante = SN;
	}
	
	
	
	/*		METHODES EVENEMENTIELLES		*/
	//swing
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == peaceTime){
			libre = true;
			peaceTime.stop();
		}
	}

	//level
	public void levelUp() {
		reinitialiser(true);
	}

	public void beginBurstMode() {
		if(habille){
			deplacement = VITESSE_LENTE;//attention à  cette valeur, elle doit etre cohérente avec la grille de jeu
			burstMode = true;
			if(orientation == SN && (y - pacman.getY() > 0)){
				orientation = NS;
			}else if(orientation == NS && (y - pacman.getY() < 0)){
				orientation = SN;
			}else if(orientation == EW && (x - pacman.getX() > 0)){
				orientation = WE;
			}else if(orientation == WE && (x - pacman.getX() < 0)){
				orientation = EW;
			}
		}
	}

	public void endBurstMode() {
		burstMode = false;
		deplacement = VITESSE_NORMALE;
		resetPosition();
	}

	//pacman
	public void estAttrape(PacmanEvent pe) {
		reinitialiser(true);
	}

	//inutiles
	public void keyReleased(KeyEvent arg0) {}
	public void keyTyped(KeyEvent arg0) {}
	public void estMort(PacmanEvent pe) {}
	public void aMangeFantome(PacmanEvent pe) {}

}
