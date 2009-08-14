package pacman;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Random;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.border.BevelBorder;

public class Launcher extends JFrame {

	public static final long serialVersionUID = 1L;
	
	public Launcher() throws IOException{
		super();

		this.setSize(600, 800);
		int w = Toolkit.getDefaultToolkit().getScreenSize().width;
		int h = Toolkit.getDefaultToolkit().getScreenSize().height;
		this.setLocation((w - this.getWidth())/2, (h - this.getHeight())/2);
		
		this.getContentPane().add(new ZoneJeu(this));
		
		this.setResizable(false);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setVisible(true);
		
		JPanel jp = new JPanel();
		jp.setBackground(Color.BLACK);
		jp.setBounds(20, 20, 40, 40);
		
		this.getLayeredPane().add(jp, JLayeredPane.DEFAULT_LAYER);
	}

	public static void main(String[] args) throws IOException {
		new Launcher();
	}

	

	
	
	
	
	//	SOUS CLASSE POUR LA ZONE DE JEU
	private class ZoneJeu extends JPanel implements ActionListener, KeyListener, LevelListener, PacmanListener{
		private static final long serialVersionUID = 1L;

		/*		CONSTANTES		*/
		private static final int TAILLE_BASE_GOMMES = 3,
			ECHELLE = 2,
			TEMPS_POINTS = 30,//temps d'affichage du score quand on mange un fantome
			FREQUENCE_JEU = 50,
			TEMPO_DEBUT = 3,//temporisation au début en secondes
			TEMPS_MESSAGE = 40,
			TEMPS_CLIGNOTEMENT = 5;
		private final boolean DEBUG = false;
		
		/*		VARIABLES		*/
		private JFrame parent;
		private GestionScores gestionScores = null;
		private Pacman pacman;
		private Vector<Fantome> fantomes;
		private Level level;

		private Vector<int[]> pointsFantomes = new Vector<int[]>();
		private String message = "";
		private int tempsMessage = -1;
		private int tailleCase;
		
		private Timer tJeu, tDebutPartie, tClignotant;

		//pour la bouche du pacman
		private int modifAngle = 0;
		private int valModifAngle = -10;
		
		//gameplay
		private boolean pause = false,
			clignotant = true;
		private int tempsPause = -1;
		
		//messages
		private Vector<String> messages = new Vector<String>();
	
		
		
		public ZoneJeu(JFrame parent) throws IOException{
			super();
			this.setBackground(Color.BLACK);
			
			this.parent = parent;

			tJeu = new Timer(FREQUENCE_JEU, this);
			tDebutPartie = new Timer(TEMPO_DEBUT * 1000, this);
			tClignotant = new Timer(TEMPS_CLIGNOTEMENT, this);
			
			messages.add("<nom> n'a plus que ses yeux pour pleurer =D");
			messages.add("Un petit bout de <nom> t'es resté entre les dents =/");
			messages.add("<nom> a un petit goût de guimauve :P");
			messages.add("<nom> retourne se rabiller xD");
		
			initialiser();
		}
		
		private void initialiser(){
			level = new Level();
			level.addLevelListener(this);

			parent.setSize(level.getWidth()*ECHELLE+10, level.getHeight()*ECHELLE+30);
			
			pacman = new Pacman(level);
			pacman.addPacmanListener(this);

			parent.addKeyListener(pacman);
			parent.addKeyListener(this);
			
			fantomes = new Vector<Fantome>();
			fantomes.add(new Fantome(level, Fantome.NIVEAU1));
			fantomes.add(new Fantome(level, Fantome.NIVEAU2));
			fantomes.add(new Fantome(level, Fantome.NIVEAU3));
			fantomes.add(new Fantome(level, Fantome.NIVEAU4));
			pacman.addPacmanListener(fantomes.get(0));
			pacman.addPacmanListener(fantomes.get(1));
			pacman.addPacmanListener(fantomes.get(2));
			pacman.addPacmanListener(fantomes.get(3));
			
			tailleCase = ECHELLE * Level.PAS;
			
			tDebutPartie.start();			
		}
		
		/*		METHODES GRAPHIQUES		*/
		//methode fourre-tout d'affichage de debuggage
		private void paintDebug(Graphics g2){
//			for(int i = 0 ; i < level.getWidth() ; i++){
//				for(int j = 0 ; j < level.getHeight() ; j++){
//					g2.setColor(Color.YELLOW);
//					if(level.estPorteDressing(i, j))
//						g2.drawRect(i*ECHELLE, j*ECHELLE, tailleCase, tailleCase);
//					g2.setColor(Color.RED);
//					if(level.getXDressing() == i && level.getYDressing() == j){
//						g2.drawRect(i*ECHELLE, j*ECHELLE, tailleCase, tailleCase);
//						g2.drawString(""+level.getYDressing(), i*ECHELLE, j*ECHELLE);
//					}
//					g2.setColor(Color.YELLOW);
//					if(level.isInDressing(i, j))
//						g2.drawRect(i*ECHELLE, j*ECHELLE, tailleCase, tailleCase);
//				}
//			}			

			//debuggage
			g2.setColor(Color.WHITE);
			String debug;
			debug = pacman.toString();
//			debug = "BurstMode="+burstMode;
			g2.drawString(debug, 10, level.getHeight()*ECHELLE);
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
					dessinerGommes(i, j, g2);
				}
			}
			
			dessinerPacman(g2);
			dessinerFantomes(g2);
			
			//infos de jeu
			g2.setColor(Color.WHITE);
			g2.drawString("Score: "+pacman.getScore(), 10, 10);
			
			g2.setColor(Color.YELLOW);
			int xVies = 120;
			for(int i = 0 ; i < pacman.getVies() ; i++){
				xVies += 3*tailleCase/4;
				g2.fillOval(xVies, 5, tailleCase/2, tailleCase/2);
			}

			g2.setColor(Color.WHITE);
			g2.drawString("r = recommencer / p = pause", 250, 10);
			
			//affichage des points
			g2.setColor(Color.WHITE);
			try{
				for(int[] point: pointsFantomes){
					g2.drawString(Integer.toString(point[0]), point[1]*ECHELLE, point[2]*ECHELLE);
					if(point[3]-- == 0)
						pointsFantomes.remove(point);
				}
			}catch(ConcurrentModificationException cme){
				//cette erreur se produit lorsque le thread du timer et celui qui affiche essaient d'accéder en même temps au vecteur de points
				//on ne fait rien, tant pis si le score n'est pas affiché pour cette fois, ce n'est pas tres grave
			}
			
			if(DEBUG){
				paintDebug(g2);
			}else{
				try{
					g2.drawString(message, 10, level.getHeight()*ECHELLE);
					if(tempsMessage-- == 0)
						message = "";
				}catch(ConcurrentModificationException cme){
					//cette erreur se produit lorsque le thread du timer et celui qui affiche essaient d'accéder en même temps au vecteur de points
					//on ne fait rien, tant pis si le score n'est pas affiché pour cette fois, ce n'est pas tres grave
				}				
			}
			System.out.println((new Date()).getTime() - debut.getTime());
		}
		
		private void dessinerPacman(Graphics2D g2){
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
			g2.fillArc(pacman.getX()*ECHELLE, pacman.getY()*ECHELLE, tailleCase, tailleCase, angleDepart, angle);			
		}
		
		private void dessinerFantomes(Graphics2D g2){
			for(Fantome fantome: fantomes){
				if(fantome.getBurstMode())
					if(clignotant)
						g2.setColor(Color.BLUE);
					else
						g2.setColor(Color.WHITE);
				else
					g2.setColor(fantome.getColor());
				
				int x = fantome.getX()*ECHELLE;
				int y = fantome.getY()*ECHELLE;
				int demi = tailleCase/2;
				int tiers = tailleCase/3;
				int quart = tailleCase/4;
				int troisQuart = 3*quart;
				//taille des yeux
				int blanc = tailleCase/6;
				int pupille = tailleCase/12;
				
				if(fantome.estHabille()){
					//haut
					g2.fillArc(x, y, tailleCase, tailleCase, 0, 180);
					g2.fillRect(x, y+demi, tailleCase, tiers);
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
		
		private void dessinerMur(int x, int y, Graphics2D g){
			int type = level.getCase(x, y);
			int marge = Level.MARGE;
			
			//on inverse la marge si le mur se trouve sur un chemin, car ce ne sera plus un retrait dans le même sens
			if(level.isOnPath(x, y))
				marge *= -1;
			
			x*=ECHELLE;
			y*=ECHELLE;
			
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
		
		private void dessinerGommes(int x, int y, Graphics2D g){
			int type = level.getCase(x, y);
			
			x*=ECHELLE;
			y*=ECHELLE;
			
			Color c = g.getColor();
			g.setColor(Color.WHITE);
			
			if((type & Level.NORMALE) != 0){
				int tailleGomme = TAILLE_BASE_GOMMES * ECHELLE;
				int decallage = (tailleCase - tailleGomme) / 2;
				g.fillOval(x+decallage, y+decallage, tailleGomme, tailleGomme);
			}else if((type & Level.SUPER) != 0){
				int tailleGomme = TAILLE_BASE_GOMMES * ECHELLE * 2;
				int decallage = (tailleCase - tailleGomme) / 2;
				g.fillOval(x+decallage, y+decallage, tailleGomme, tailleGomme);
			}
			
			g.setColor(c);
		}
		
		//pause pour delay cycle / -1 = temps indetermine
		private void pause(int delay){
			pause = true;
			tempsPause = delay;
		}


		
		/*		METHODES EVENEMENTIELLES		*/
		//swing
		public void actionPerformed(ActionEvent ae) {
			if(ae.getSource() == tJeu){
				if(!pause){
					Date debut = new Date();
					
					if(modifAngle <= 0 || modifAngle >= Pacman.ANGLE_BOUCHE)
						valModifAngle *= -1;
					modifAngle += valModifAngle;
					
					pacman.deplacer();
					for(Fantome fantome: fantomes){
						fantome.deplacer();
						pacman.confronter(fantome);
					}
					//repaint(pacman.getX() - 5, pacman.getY() - 5 , 30, 30);
					repaint();
					
					System.out.println((new Date()).getTime() - debut.getTime());
				}else if(tempsPause >= 0){
					if(tempsPause-- == 0){
						pause = false;
					}
				}
			}else if(ae.getSource() == tDebutPartie){
				tDebutPartie.stop();
				tJeu.start();
			}else if(ae.getSource() == tClignotant){
				if(tClignotant.getDelay() != TEMPS_CLIGNOTEMENT*100)
					tClignotant.setDelay(TEMPS_CLIGNOTEMENT*100);
				else
					clignotant = !clignotant;
			}
		}

		public void keyTyped(KeyEvent ke) {
			if(ke.getKeyChar() == 'p'){
				pause = !pause;
			}else if(ke.getKeyChar() == 'r'){
				if(gestionScores != null){
					setVisible(true);
					parent.remove(gestionScores);
					gestionScores = null;
				}
				level.initialiser();
				for(Fantome fantome: fantomes)
					fantome.reinitialiser(true);
				pacman.reinitialiser();
				pointsFantomes = new Vector<int[]>();
				message = "";
				tempsMessage = -1;
			}	
		}

		//level
		public void beginBurstMode() {
			tClignotant.setDelay(3000*Level.DUREE_BURST_MODE/4);
			tClignotant.start();
			clignotant = true;
		}

		public void endBurstMode() {
			tClignotant.stop();
		}

		public void levelUp() {
			tJeu.stop();
			tDebutPartie.start();
			
			pointsFantomes = new Vector<int[]>();
			message = "";
			tempsMessage = -1;
		}

		//pacman
		public void estAttrape(PacmanEvent pe) {
			modifAngle = 0;
			valModifAngle = -10;
			
			tJeu.stop();
			tDebutPartie.restart();
			message = pe.getFantome().getMessage();
			tempsMessage = TEMPS_MESSAGE;
		}

		public void aMangeFantome(PacmanEvent pe) {
			int[] point = new int[4];
			point[0] = pe.getPoint();
			point[1] = pe.getFantome().getX();
			point[2] = pe.getFantome().getY();
			point[3] = TEMPS_POINTS;
			pointsFantomes.add(point);
			pause(10);
			Random r = new Random();
			int i = r.nextInt(messages.size());
			message = messages.elementAt(i).replace("<nom>", pe.getFantome().getNom());
			tempsMessage = TEMPS_MESSAGE;
		}
		
		//gestion de la fin de partie quand le pacman est mort
		public void estMort(PacmanEvent pe) {
			tJeu.stop();
			tDebutPartie.stop();

			this.setVisible(false);
			gestionScores = new GestionScores(this.parent, pacman.getScore());
			this.parent.getContentPane().add(gestionScores);
		}

		//inutilisées
		public void keyPressed(KeyEvent arg0) {}
		public void keyReleased(KeyEvent arg0) {}
		
	}

	
	
	
	
	
	
	private class GestionScores extends JPanel implements ActionListener, Runnable{
		private final String URL = "jdbc:mysql://localhost:3306/pacman",
		USER = "pacman",
		PASSWD = "p4cm4n",
		CREATE_TABLE = "CREATE TABLE `pacman`.`scores` (`date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,`nom` VARCHAR( 100 ) NOT NULL ,`score` INT NOT NULL) ENGINE = MYISAM ;",
		GET_SCORES = "select * from scores order by score desc",
		INSERT_SCORE = "insert into scores (nom, score) values (?, ?)";
	
		
		
		private JFrame parent;
		private JLabel lNom;
		private JTextField nom;
		private JButton envoyer;
		private int score;
		private Connection connexion;
		private JScrollPane jspScores = new JScrollPane();
		private JTextArea jtaScores = new JTextArea();
		
		public GestionScores(JFrame parent, int score) {
			int marge = (int)(parent.getWidth()*0.2);
			this.parent = parent;
			this.score = score;
			this.setBackground(new Color(0,0,30,155));
			
			this.setLayout(null);
			this.setBounds(0, 0, parent.getWidth(), parent.getHeight());
			
			lNom = new JLabel("Appuyez sur r pour commencer une nouvelle partie");
			lNom.setForeground(Color.WHITE);
			lNom.setBounds(10, 10, parent.getWidth()-20, 20);
			this.add(lNom);
			
			lNom = new JLabel("Score: "+this.score+". Saisir votre nom:");
			lNom.setForeground(Color.WHITE);
			lNom.setBounds(marge, 30, parent.getWidth() - marge*2, 20);
			this.add(lNom);
			
			nom = new JTextField();
			nom.setBounds(marge, 50, parent.getWidth() - marge*2, 20);
			this.add(nom);
			
			marge = (int)(marge * 1.5);
			
			envoyer = new JButton("Envoyer");
			envoyer.setBounds(marge, 80, parent.getWidth() - marge*2, 25);
			envoyer.addActionListener(this);
			this.add(envoyer);
			
			nom.requestFocus();
			
			majScores();
		}
		
		//on thread l'accès à la base de données car c'est une methode bloquante et que la connexion peut prendre jusqu'a plusieurs secondes
		public void majScores(){
			Thread t = new Thread(this);
			t.start();
		}
		
		private void afficherScores(){
			PreparedStatement query = null;
			ResultSet scores = null;
				
			try {
				query = connexion.prepareStatement(GET_SCORES);
				scores = query.executeQuery();
				try {
					jtaScores = new JTextArea();
					jtaScores.setEditable(false);
					jtaScores.setFocusable(false);
					jtaScores.setBackground(new Color(0,0,0,50));
					while(scores.next()){
						jtaScores.append(scores.getDate(1)+"\t\t"+scores.getString(2)+"\t\t"+scores.getInt(3)+"\n");
					}
					jspScores.setViewportView(jtaScores);
					jspScores.setBounds(10, 120, this.parent.getWidth()-30, this.parent.getHeight() - 150);
					jspScores.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
					jspScores.setBackground(new Color(0,0,0,50));
					this.add(jspScores);
				} catch (SQLException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(this, "Affichage des scores impossible");
				}
			} catch (SQLException e) {
				try {
					query = connexion.prepareStatement(CREATE_TABLE);
					query.execute();
					JOptionPane.showMessageDialog(this, "La table n'existait pas et a été créée");
				} catch (SQLException e1) {
					JOptionPane.showMessageDialog(this, "Impossible de configurer la base de données correctement");
					e1.printStackTrace();
					nom.setVisible(false);
					envoyer.setVisible(false);
					lNom.setText("Erreur de base de données. Gestion des scores impossible.");
				}
			}
			//les scores ne sont pas raffraichis - validate provoque le rafraichissement
			jspScores.validate();
		}

		public void actionPerformed(ActionEvent ae) {
			if(!nom.getText().equals("")){
				try {
					PreparedStatement query = connexion.prepareStatement(INSERT_SCORE);
					query.setString(1, nom.getText());
					query.setInt(2, score);
					if(query.executeUpdate() == 1){
						JOptionPane.showMessageDialog(this, "Votre score a été enregistré");
						nom.setEnabled(false);
						envoyer.setEnabled(false);
					}else{
						JOptionPane.showMessageDialog(this, "Une erreur est survenue lors de l'enregistrement de votre score");
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				afficherScores();
			}else{
				JOptionPane.showMessageDialog(this, "Votre nom doit être renseigné");
			}
		}
		
		public void finalize(){
			try {
				connexion.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		//la methode runnable appelée lorsqu'on thread l'objet
		public void run() {
			try {
				this.connexion = DriverManager.getConnection(URL, USER, PASSWD);

				afficherScores();
			} catch (SQLException sqle) {
				sqle.printStackTrace();
				
				lNom = new JLabel("Connexion à la base de données impossible");
				this.add(lNom);
			}
		}
	}
}
