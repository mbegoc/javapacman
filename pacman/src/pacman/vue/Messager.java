package pacman.vue;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Random;
import java.util.Vector;

import javax.swing.JPanel;

import pacman.modele.Fantome;
import pacman.modele.Level;
import pacman.modele.Pacman;

public class Messager extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private Vue parent;

	//messages
	private Vector<String> messages = new Vector<String>();
	private Vector<String> debugMessages = new Vector<String>();
	private Vector<int[]> pointsFantomes = new Vector<int[]>();
	private boolean debug = false;
	private int tempsMessage = -1;
	private String message = "";
	
	public Messager(Vue parent){
		this.parent = parent;
		
		this.setOpaque(false);
		setSize(parent.getGame().getLevel().getWidth()*Vue.ECHELLE, parent.getGame().getLevel().getHeight()*Vue.ECHELLE);
		
		messages.add("<nom> n'a plus que ses yeux pour pleurer =D");
		messages.add("Un petit bout de <nom> t'es resté entre les dents =/");
		messages.add("<nom> a un petit goût de guimauve :P");
		messages.add("<nom> peut retourner se rabiller xD");	
	}

	public Messager(Vue parent, boolean debug){
		this(parent);
		
		this.debug = debug;
	}

	public void paintComponent(Graphics g){
		Date debut = new Date();
		
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		Pacman pacman = parent.getGame().getPacman();
		Level level = parent.getGame().getLevel();
		
		g2.setColor(Color.WHITE);
		g2.drawString("Score: " + pacman.getScore(), 10, 10);
		
		g2.setColor(Color.YELLOW);
		int xVies = 120;
		for(int i = 0 ; i < pacman.getVies() ; i++){
			xVies += 3*parent.getTailleCase()/4;
			g2.fillOval(xVies, 5, parent.getTailleCase()/2, parent.getTailleCase()/2);
		}

		g2.setColor(Color.WHITE);
		g2.drawString("r = recommencer / p = pause", 250, 10);
		
		//affichage des points
		g2.setColor(Color.WHITE);
		try{
			for(int[] point: pointsFantomes){
				g2.drawString(Integer.toString(point[0]), point[1]*Vue.ECHELLE, point[2]*Vue.ECHELLE);
				if(point[3]-- == 0)
					pointsFantomes.remove(point);
			}
		}catch(ConcurrentModificationException cme){
			//cette erreur se produit lorsque le thread du timer et celui qui affiche essaient d'accéder en même temps au vecteur de points
			//on ne fait rien, tant pis si le score n'est pas affiché pour cette fois, ce n'est pas tres grave
		}
		
		if(debug){
			paintDebug(g2);
		}else{
			try{
				g2.drawString(message, 10, level.getHeight()*Vue.ECHELLE - 3);
				if(tempsMessage-- == 0)
					message = "";
			}catch(ConcurrentModificationException cme){
				//cette erreur se produit lorsque le thread du timer et celui qui affiche essaient d'accéder en même temps au vecteur de points
				//on ne fait rien, tant pis si le score n'est pas affiché pour cette fois, ce n'est pas tres grave
			}				
		}		

//		System.err.println((new Date()).getTime() - debut.getTime());
	}
	
	private void paintDebug(Graphics g2){
//		paintAnchorPath(g2);
		for(int i = 0 ; i < debugMessages.size() ; i++)
			g2.drawString(debugMessages.elementAt(i), 10, parent.getGame().getLevel().getHeight()*Vue.ECHELLE);
	}
	
	private void paintAnchorPath(Graphics g2){
		Level level = parent.getGame().getLevel();
		for(int i = 0 ; i < level.getWidth() ; i++){
			for(int j = 0 ; j < level.getHeight() ; j++){
				g2.setColor(Color.ORANGE);
				if(level.isOnPath(i, j))
					g2.fillRect(i*Vue.ECHELLE, j*Vue.ECHELLE, 1, 1);
				
				g2.setColor(Color.YELLOW);
				if(level.estPorteDressing(i, j))
					g2.drawRect(i*Vue.ECHELLE, j*Vue.ECHELLE, parent.getTailleCase(), parent.getTailleCase());
				if(level.isInDressing(i, j))
					g2.drawRect(i*Vue.ECHELLE, j*Vue.ECHELLE, parent.getTailleCase(), parent.getTailleCase());

				g2.setColor(Color.RED);
				if(level.getXDressing() == i && level.getYDressing() == j){
					g2.drawRect(i*Vue.ECHELLE, j*Vue.ECHELLE, parent.getTailleCase(), parent.getTailleCase());
					g2.drawString(""+level.getYDressing(), i*Vue.ECHELLE, j*Vue.ECHELLE);
				}
			}
		}			
	}
	
	public void setMessage(String message, int temps){
		this.message = message;
		tempsMessage = temps;
	}

	public void setMessage(Fantome fantome, int temps){
		Random r = new Random();
		int i = r.nextInt(messages.size());
		message = messages.elementAt(i).replace("<nom>", fantome.getNom());
		tempsMessage = temps;
	}

	public void addPointsFantomes(int[] pointsFantomes) {
		this.pointsFantomes.add(pointsFantomes);
	}
	
	public void initPointsFantomes(){
		pointsFantomes = new Vector<int[]>();
	}

}
