/*
 * Une interface que doivent implementer les objets qui veulent écouter les événements du pacman
 */
package pacman.modele.lang;


public interface PacmanListener {
	public void estAttrape(PacmanEvent pe);
	public void estMort(PacmanEvent pe);
	public void aMangeFantome(PacmanEvent pe);
}
