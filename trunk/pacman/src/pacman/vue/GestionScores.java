package pacman.vue;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;

public class GestionScores extends JPanel implements ActionListener, Runnable{

	private static final long serialVersionUID = 1L;

	private final String URL = "jdbc:mysql://localhost:3306/pacman",
	USER = "pacman",
	PASSWD = "p4cm4n",
	CREATE_TABLE = "CREATE TABLE `pacman`.`scores` (`date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,`nom` VARCHAR( 100 ) NOT NULL ,`score` INT NOT NULL) ENGINE = MYISAM ;",
	GET_SCORES = "select * from scores order by score desc",
	INSERT_SCORE = "insert into scores (nom, score) values (?, ?)";

	
	
	private Vue parent;
	private JLabel lNom;
	private JTextField nom;
	private JButton envoyer;
	private int score;
	private Connection connexion;
	private JScrollPane jspScores = new JScrollPane();
	private JTextArea jtaScores = new JTextArea();
	
	public GestionScores(Vue parent, int score) {
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
