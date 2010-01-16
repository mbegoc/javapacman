package pacman.vue;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Random;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicGraphicsUtils;

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
	private String bigMessage = "";
	
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

		int xCenter = level.getWidth() * Vue.ECHELLE / 2;
		int yCenter = level.getHeight() * Vue.ECHELLE / 2;
		
		g2.setColor(Color.WHITE);
		g2.drawString("Score: " + pacman.getScore(), 10, 10);
		
		g2.setColor(Color.YELLOW);
		int xVies = 120;
		for(int i = 0 ; i < pacman.getVies() ; i++){
			xVies += 3*parent.getTailleCase()/4;
			g2.fillOval(xVies, 5, parent.getTailleCase()/2, parent.getTailleCase()/2);
		}

		g2.setColor(Color.WHITE);
		Rectangle2D bounds0 = g2.getFontMetrics().getStringBounds("r = recommencer / p = pause", g2);
		g2.drawString("r = recommencer / p = pause", xCenter - (int)bounds0.getCenterX(), 10);
		
		String strOthers = "Appuyer sur 1, 2 ou 3 pour charger d'autres niveaux";
		Rectangle2D bounds1 = g2.getFontMetrics().getStringBounds(strOthers, g2);
		g2.drawString(strOthers, level.getWidth() * Vue.ECHELLE - (int)bounds1.getWidth() - 3, level.getHeight()*Vue.ECHELLE - 3);
		
		String strOriginal = "Appuyer sur o pour recharger le niveau original";
		Rectangle2D bounds2 = g2.getFontMetrics().getStringBounds(strOriginal, g2);
		g2.drawString(strOriginal, level.getWidth() * Vue.ECHELLE - (int)bounds2.getWidth() - 3, level.getHeight()*Vue.ECHELLE - 3 - (int)bounds1.getHeight());
		
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
			Font font = g2.getFont();
			g2.setColor(Color.LIGHT_GRAY);
			Font newFont = font.deriveFont(Font.BOLD, 30);
			g2.setFont(newFont);
			Rectangle2D shape = newFont.getStringBounds(bigMessage, g2.getFontRenderContext());
			g2.drawString(bigMessage, getWidth() / 2 - (int)shape.getCenterX(), getHeight() / 2 - 15);
			g2.setFont(font);
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
	
	public void setBigMessage(String message){
		bigMessage = message;
	}
	
	public void clearBigMessage(){
		bigMessage = "";
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
