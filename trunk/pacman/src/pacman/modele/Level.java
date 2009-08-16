/*
 * 	Un niveau de jeu.
 * 	Un niveau de jeu est essentiellement un tableau à 2 dimensions contenant les informations relatives à chaque case.
 * 	Cette matrice est générée à partir d'une image dans laquelle chaque case est représentée par un pixel dont la couleur indique la nature de la case.
 * 	
 * 	La matrice contient plusieurs informations binaires par case. Pour se faire, on utilise des constantes qui contiennent chacune
 * 	un entier dont la valeur correspond à un bit, des masques binaires. Grace aux operateur binaires, on peut superposer les informations et les retrouver.
 * 
 * 
 * */



package pacman.modele;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.swing.Timer;
import java.util.Vector;

import javax.imageio.ImageIO;

import pacman.modele.lang.LevelListener;
import pacman.modele.lang.PacmanEvent;
import pacman.modele.lang.PacmanListener;



public class Level implements ActionListener, PacmanListener{
	//	CONSTANTES
	//masques binaires
	static public final int RIEN = 0,
	PATH = 1,//le chemin que peuvent emprunter le pacman et les fantomes
	MUR = 2,//inutilisée
	SUPER = 4,//une super gomme
	NORMALE = 8,//une gomme normale
	GOMME = SUPER | NORMALE,//une gomme, peut importe laquelle
	DEPART = 16,//le depart du pacman - inutilisée
	DRESSING_FANTOME = 32,//la zone reservee aux fantomes
	PORTE_DRESSING = 64,//le point d'entree et de sortie des fantomes
	
	//les differents types de murs
	GAUCHE = 128,
	DROITE = 256,
	HAUT = 512,
	BAS = 1024,
	BAS_GAUCHE = BAS | GAUCHE,
	BAS_DROITE = BAS | DROITE,
	HAUT_GAUCHE = HAUT | GAUCHE,
	HAUT_DROITE = HAUT | DROITE,
	
	//couleurs
	VERT = -16711936,//un chemin avec une super gomme (0, 255, 0)
	BLANC = -1,//un chemin avec des gommes normales (255, 255, 255)
	GRIS = -3618616,//un chemin sans gomme (200, 200, 200)
	JAUNE = -256,//point d'entree et de sortie du dressing (255, 255, 0)
	ROUGE = -65536,//la zone de depart des fantomes (le dressing ;) )(255, 0, 0)
	BLEU = -16776961,//position de depart du pacman (0, 0, 255)
	ORANGE = -551907,//la position de depart de clyde (247, 148, 29)
	VIN = -10223616,//LIE DE VIN - point de depart de Blinky (100, 0, 0)
	CYAN = -16732433,//point de depart d'Inky (0, 174, 239)
	ROSE = -65281,//position de depart de Pinky (255, 0, 255)
	
	//constantes de réglages du niveau
	PAS = Pacman.DEPLACEMENT * 3,
	MARGE = 3,
	DUREE_BURST_MODE = 10;

	//	VARIABLES
	private BufferedImage image;
	private Vector<LevelListener> listeners = new Vector<LevelListener>();
	
	private int[][] path;
	
	private int xDepart, yDepart, 
		xDressing, yDressing,
		xDepartClyde, yDepartClyde,
		xDepartBlinky, yDepartBlinky,
		xDepartInky, yDepartInky,
		xDepartPinky, yDepartPinky;
	
	private int width, height, 
		gommes = 0,
		level = 0;
	
	private Timer tBurstMode = new Timer(DUREE_BURST_MODE*1000, this);
	
	
	public Level(){
		initialiser();
	}
	
	public void initialiser(){
		gommes = 0;
		
		//il va falloir charger une image et évaluer les deplacements valides a partir de cette image
		File file = new File("bin/pacman/maps/level1.gif");
		try{
			image = ImageIO.read(file);
		}catch(IOException ioe){
			System.out.println("Impossible de lire le niveau "+level+": "+ioe.getMessage());
			System.exit(1);
		}

		width = image.getWidth();
		height = image.getHeight();
		
		//on veut une grille plus grande que l'image pour éviter que le pacman ne se deplace d'une case à la fois
		path = new int[width*PAS][height*PAS];
		
		for(int i = 0 ; i < width ; i++){
			for(int j = 0 ; j < height ; j++){
				//on commence par remplir le tableau avec le contenu du fichier, sans oublier de tenir du pas
				path[i*PAS][j*PAS] = genererCase(i, j);
				if((path[i*PAS][j*PAS] & GOMME) != 0)
					gommes++;
				
				if((path[i*PAS][j*PAS] & PATH) != 0){
					//ensuite, on vérifie si la case en dessous (si elle existe) est un chemin
					//si c'est le cas, il faut remplir les case vides du pas avec un chemin
					if(j < height-1 && (genererCase(i, j+1) & PATH) != 0){
						for(int jPas = j*PAS ; jPas < (j+1)*PAS ; jPas++){
							path[i*PAS][jPas] |= PATH;
						}
					}
					//on fait la meme chose pour la case a droite
					if(i < width-1 && (genererCase(i+1, j) & PATH) != 0){
						for(int iPas = i*PAS ; iPas < (i+1)*PAS ; iPas++){
							path[iPas][j*PAS] |= PATH;
						}
					}
				}else{
					path[i*PAS][j*PAS] |= calculerTypeMur(i, j);
				}
					
				//on fait la meme chose pour le dressing pour que les fantomes puissent s'y deplacer
				if((path[i*PAS][j*PAS] & DRESSING_FANTOME) != 0){
					//si c'est le cas, il faut remplir les case vides du pas avec un chemin
					if(j < height-1 && (genererCase(i, j+1) & DRESSING_FANTOME) != 0){
						for(int jPas = j*PAS ; jPas < (j+1)*PAS ; jPas++){
							path[i*PAS][jPas] |= DRESSING_FANTOME;
						}
					}
					//on fait la meme chose pour la case a droite
					if(i < width-1 && (genererCase(i+1, j) & DRESSING_FANTOME) != 0){
						for(int iPas = i*PAS ; iPas < (i+1)*PAS ; iPas++){
							path[iPas][j*PAS] |= DRESSING_FANTOME;
						}
					}
				}
				//on fait encore la meme chose pour la porte de dressing, il doit y avoir un couloir de sortie
				if((path[i*PAS][j*PAS] & PORTE_DRESSING) != 0){
					//si c'est le cas, il faut remplir les case vides du pas avec un chemin
					if(j < height-1 && (genererCase(i, j+1) & DRESSING_FANTOME) != 0){
						for(int jPas = j*PAS ; jPas < (j+1)*PAS ; jPas++){
							path[i*PAS][jPas] |= PORTE_DRESSING;
						}
					}
					//on fait la meme chose pour la case a droite
					if(i < width-1 && (genererCase(i+1, j) & DRESSING_FANTOME) != 0){
						for(int iPas = i*PAS ; iPas < (i+1)*PAS ; iPas++){
							path[iPas][j*PAS] |= PORTE_DRESSING;
						}
					}
				}
			}
			tBurstMode.stop();
		}
		
		//les murs ne sont pas bien finalisés, il manque quelques angles qu'on ne peut calculer que maintenant
		for(int i = 0 ; i < width-1 ; i++){
			for(int j = 0 ; j < height-1 ; j++){
				if(i > 0 && j > 0 && i < width && j < height){
					if((path[(i-1)*PAS][j*PAS] & DROITE) != 0 && (path[i*PAS][(j-1)*PAS] & BAS) != 0 ){
						path[(i-1)*PAS][j*PAS] ^= DROITE;
						path[i*PAS][(j-1)*PAS] ^= BAS;
						path[i*PAS][j*PAS] |= HAUT_GAUCHE;
					}
					if((path[(i-1)*PAS][j*PAS] & DROITE) != 0 && (path[i*PAS][(j+1)*PAS] & HAUT) != 0 ){
						path[(i-1)*PAS][j*PAS] ^= DROITE;
						path[i*PAS][(j+1)*PAS] ^= HAUT;
						path[i*PAS][j*PAS] |= BAS_GAUCHE;
					}
					if((path[(i+1)*PAS][j*PAS] & GAUCHE) != 0 && (path[i*PAS][(j-1)*PAS] & BAS) != 0 ){
						path[(i+1)*PAS][j*PAS] ^= GAUCHE;
						path[i*PAS][(j-1)*PAS] ^= BAS;
						path[i*PAS][j*PAS] |= HAUT_DROITE;
					}
					if((path[(i+1)*PAS][j*PAS] & GAUCHE) != 0 && (path[i*PAS][(j+1)*PAS] & HAUT) != 0 ){
						path[(i+1)*PAS][j*PAS] ^= GAUCHE;
						path[i*PAS][(j+1)*PAS] ^= HAUT;
						path[i*PAS][j*PAS] |= BAS_DROITE;
					}
				}
			}
		}
		
		levelUp();
	}
	
	//accede à un pixel et calcul le masque binaire pour cette case
	private int genererCase(int i, int j){
		int retour = RIEN;

		if(i >= 0 && j >= 0 && i < width && j < height){
			int couleur = image.getRGB(i, j);

			if(couleur == BLANC || couleur == VERT || couleur == GRIS || couleur == BLEU || couleur == JAUNE){
				retour |= PATH;
			}
			if(couleur == BLANC){
				retour |= NORMALE;
			}
			if(couleur == VERT){
				retour |= SUPER;
			}
			if(couleur == BLEU){
				retour |= DEPART;
				xDepart = i;
				yDepart = j;
			}
			if(couleur == JAUNE){
				xDressing = i;
				yDressing = j;
				retour |= PORTE_DRESSING;
			}
			if(couleur == ROUGE || couleur == ORANGE || couleur == VIN || couleur == CYAN || couleur == ROSE){
				retour |= DRESSING_FANTOME;
			}
			if(couleur == ORANGE){
				xDepartClyde = i;
				yDepartClyde = j;
			}
			if(couleur == VIN){
				xDepartBlinky = i;
				yDepartBlinky = j;
			}
			if(couleur == CYAN){
				xDepartInky = i;
				yDepartInky = j;
			}
			if(couleur == ROSE){
				xDepartPinky = i;
				yDepartPinky = j;
			}
		}
		return retour;
	}
	
	private int calculerTypeMur(int i, int j){
		int retour = RIEN;
		
		if((genererCase(i-1, j) & PATH) != 0)
			retour |= GAUCHE;
		else if((genererCase(i+1, j) & PATH) != 0)
			retour |= DROITE;
		
		if((genererCase(i, j-1) & PATH) != 0)
			retour |= HAUT;
		else if((genererCase(i, j+1) & PATH) != 0)
			retour |= BAS;
			
		return retour;
	}
	
	public boolean isOnPath(int x, int y){
		if(x < 0 || y < 0 || x >= getWidth() || y >= getHeight()){
			return false;
		}else if((path[x][y] & PATH) != 0){
			return true;
		}else{
			return false;
		}
	}
	
	public boolean isInDressing(int x, int y){
		if(x < 0 || y < 0 || x >= getWidth() || y >= getHeight()){
			return false;
		}else if((path[x][y] & DRESSING_FANTOME) != 0){
			return true;
		}else{
			return false;
		}
	}
	
	public boolean estPorteDressing(int x, int y){
		if(x < 0 || y < 0 || x >= getWidth() || y >= getHeight()){
			return false;
		}else if((path[x][y] & PORTE_DRESSING) != 0){
			return true;
		}else{
			return false;
		}
	}
	
	//le pacman appelle cette methode pour manger l'eventuelle gomme qui se trouve sur la case
	public int manger(int x, int y){
		if((path[x][y] & SUPER) != 0){
			path[x][y] ^= SUPER;
			if(--gommes == 0)
				initialiser();
			else
				beginBurstMode();
			return 50;
		}else if((path[x][y] & NORMALE) != 0){
			path[x][y] ^= NORMALE;
			if(--gommes == 0)
				initialiser();
			return 10;
		}else{
			return 0;
		}
		
	}
	
	/*		GETTERS / SETTERS		*/
	public int getCase(int x, int y){
		return path[x][y]; 
	}
	
	public int getXDepart(){
		return xDepart*PAS;
	}
	public int getYDepart(){
		return yDepart*PAS;
	}
	public int getXDepart(Fantome fantome){
		if(fantome.getNom() == "Clyde")
			return xDepartClyde*PAS;
		else if(fantome.getNom() == "Pinky")
			return xDepartPinky*PAS;
		else if(fantome.getNom() == "Inky")
			return xDepartInky*PAS;
		else if(fantome.getNom() == "Blinky")
			return xDepartBlinky*PAS;
		return 0;
	}
	public int getYDepart(Fantome fantome){
		if(fantome.getNom() == "Clyde")
			return yDepartClyde*PAS;
		else if(fantome.getNom() == "Pinky")
			return yDepartPinky*PAS;
		else if(fantome.getNom() == "Inky")
			return yDepartInky*PAS;
		else if(fantome.getNom() == "Blinky")
			return yDepartBlinky*PAS;
		return 0;
	}
	public int getXDressing(){
		return xDressing*PAS;
	}
	public int getYDressing(){
		return yDressing*PAS;
	}
	public int getWidth(){
		return width*PAS;
	}
	public int getHeight(){
		return height*PAS;
	}
	
	/*	EVENEMENTS LEVEL	*/
	public void addLevelListener(LevelListener listener){
		listeners.add(listener);
	}
	
	public void removeLevelListener(LevelListener listener){
		listeners.remove(listener);
	}
	
	private void levelUp(){
		for(LevelListener listener: listeners){
			listener.levelUp();
		}
	}

	private void beginBurstMode(){
		tBurstMode.restart();
		for(LevelListener listener: listeners){
			listener.beginBurstMode();
		}
	}

	private void endBurstMode(){
		tBurstMode.stop();
		for(LevelListener listener: listeners){
			listener.endBurstMode();
		}
	}

	/*		METHODES EVENEMENTIELLES		*/
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == tBurstMode){
			endBurstMode();
		}
	}

	public void estAttrape(PacmanEvent pe) {}
	public void estMort(PacmanEvent pe) {}
	public void aMangeFantome(PacmanEvent pe) {}
}
