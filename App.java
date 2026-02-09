package PacMan;

import javax.swing.JFrame;

public class App {

	public static void main(String[] args) {
		int rowC = 21;
		int columnC = 19;
		int tileS = 32;
		int boardW = columnC * tileS;
		int boardH = rowC * tileS;

		JFrame frame = new JFrame("Pac-Man");
		frame.setVisible(true);
		frame.setSize(boardW, boardH);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		PacMan pacmanGame = new PacMan();
		frame.add(pacmanGame);
		frame.pack();
		pacmanGame.requestFocus();
	}

}
