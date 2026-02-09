package PacMan;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.HashSet;
import java.util.Random;


@SuppressWarnings("serial")
public class PacMan extends JPanel implements ActionListener, KeyListener {
	
	class Block{
		int x;
		int y;
		int width;
		int height;
		Image image;
		Image normalImage;

		int startX;
		int startY;
		
		char direction = 'U';
		int velocityX = 0;
		int velocityY = 0;
		
		int hitboxMargin = 6;
		
		boolean up, down, left, right;
		
		boolean mouthOpen = true;
		
		Block(Image image, int x, int y, int width, int height) {
				this.image = image;
				this.normalImage = image;
				this.x = x;
				this.y = y;
				this.width = width;
				this.height = height;
				this.startX = x;
				this.startY = y;
		}
		
		void updateDirection(char direction) {
			char prevDirection = this.direction;

		    this.direction = direction;
		    updateVelocity();

		    int testX = this.x + this.velocityX;
		    int testY = this.y + this.velocityY;

		    boolean blocked = false;
		    for (Block wall : PacMan.this.walls) {
		        int originalX = this.x;
		        int originalY = this.y;
		        this.x = testX;
		        this.y = testY;

		        if (collision(this, wall)) {
		            blocked = true;
		        }

		        // Revert
		        this.x = originalX;
		        this.y = originalY;

		        if (blocked) break;
		    }

		    if (blocked) {
		        this.direction = prevDirection;
		        updateVelocity();		    
		    }
		}
				
		void updateVelocity() {
			if (this.direction == 'U') {
				this.velocityX = 0;
				this.velocityY = -tileS/4;
			}
			else if (this.direction == 'D') {
				this.velocityX = 0;
				this.velocityY = tileS/4;
			}
			else if (this.direction == 'L') {
				this.velocityX = -tileS/4;
				this.velocityY = 0;
			}
			else if (this.direction == 'R') {
				this.velocityX = tileS/4;
				this.velocityY = 0;
			}
		}
		
		void reset() {
			this.x = this.startX;
			this.y = this.startY;
		}
	}
	
	private int rowC = 21;
	private int columnC = 19;
	private int tileS = 32;
	private int boardW = columnC * tileS;
	private int boardH = rowC * tileS;

	private Image wallImage;
	private Image blueGhostImage;
	private Image pinkGhostImage;
	private Image orangeGhostImage;
	private Image redGhostImage;
	private Image scaredGhostImage;

	private Image pacmanUpImage;
	private Image pacmanDownImage;
	private Image pacmanLeftImage;
	private Image pacmanRightImage;
	private Image pacmanCloseImage;
	
	private Image fruitImage;
	
	private Image powerImage;

	private String[] tileMap = {
        "XXXXXXXXXXXXXXXXXXX",
        "Xqr     fXf     oqX",
        "X XX XXX X XXX XX X",
        "X                 X",
        "X XX X XXXXX X XX X",
        "X    X       X    X",
        "XXXX XXXX XXXX XXXX",
        "OOOX X       X XOOO",
        "XXXX X XX XX X XXXX",
        "X                 X",
        "XXXX X XXXXX X XXXX",
        "OOOX X f   f X XOOO",
        "XXXX X XXXXX X XXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X  X     P     X  X",
        "XX X X XXXXX X X XX",
        "Xf   X   X   X   fX",
        "X XXXXXX X XXXXXX X",
        "Xqb             pqX",
        "XXXXXXXXXXXXXXXXXXX" 
    };

	HashSet<Block> walls;
	HashSet<Block> foods;
	HashSet<Block> ghosts;
	HashSet<Block> fruits;
	HashSet<Block> powers;
	Block pacman;
	
	Timer gameLoop;
	
	char[] directions = {'U', 'D', 'L', 'R'};
	Random random = new Random();
	
	int score = 0;
	int lives = 3;
	boolean gameOver = false;
	
	boolean up, down, left, right;
	
	long lastMouthUpdate = System.currentTimeMillis();
	long mouthAnimationDelay = 150;
	
	boolean poweredUp = false;
	long poweredUpTime = 0;
	static final long poweredUpDuration = 7000;

	PacMan() {
		setPreferredSize(new Dimension(boardW, boardH));
		setBackground(Color.BLACK);
		addKeyListener(this);
		setFocusable(true);
		
		wallImage = new ImageIcon(getClass().getResource("wall.png")).getImage();
		blueGhostImage = new ImageIcon(getClass().getResource("blueGhost.png")).getImage();
		pinkGhostImage = new ImageIcon(getClass().getResource("pinkGhost.png")).getImage();
		orangeGhostImage = new ImageIcon(getClass().getResource("orangeGhost.png")).getImage();
		redGhostImage = new ImageIcon(getClass().getResource("redGhost.png")).getImage();
		scaredGhostImage = new ImageIcon(getClass().getResource("scaredGhost.png")).getImage();
		
		pacmanUpImage = new ImageIcon(getClass().getResource("pacmanUp.png")).getImage();
		pacmanDownImage = new ImageIcon(getClass().getResource("pacmanDown.png")).getImage();
		pacmanLeftImage = new ImageIcon(getClass().getResource("pacmanLeft.png")).getImage();
		pacmanRightImage = new ImageIcon(getClass().getResource("pacmanRight.png")).getImage();
		pacmanCloseImage = new ImageIcon(getClass().getResource("pacmanClose.png")).getImage();
		
		fruitImage = new ImageIcon(getClass().getResource("cherry.png")).getImage();
		
		powerImage = new ImageIcon(getClass().getResource("powerFood.png")).getImage();
				
		loadMap();
		for (Block ghost : ghosts) {
			char newDirection = directions[random.nextInt(4)];
			ghost.updateDirection(newDirection);
		}
		
		gameLoop = new Timer(50, this);
		gameLoop.start();

	}

	public void loadMap() {
		walls = new HashSet<Block>();
		foods = new HashSet<Block>();
		ghosts = new HashSet<Block>();
		fruits = new HashSet<Block>();
		powers = new HashSet<Block>();

		for (int r = 0; r < rowC; r++) {
			for (int c = 0; c < columnC; c++) {
				String row = tileMap[r];
				char tileMapChar = row.charAt(c);

				int x = c*tileS;
				int y = r*tileS;

				if (tileMapChar == 'X') {
					Block wall = new Block(wallImage, x, y, tileS, tileS);
					walls.add(wall);
				}
				else if (tileMapChar == 'b') {
					Block ghost = new Block(blueGhostImage, x, y, tileS, tileS);
					ghosts.add(ghost);
				}
				else if (tileMapChar == 'o') {
						Block ghost = new Block(orangeGhostImage, x, y, tileS, tileS);
						ghosts.add(ghost);
				}
				else if (tileMapChar == 'r') {
					Block ghost = new Block(redGhostImage, x, y, tileS, tileS);
					ghosts.add(ghost);
				}
				else if (tileMapChar == 'p') {
					Block ghost = new Block(pinkGhostImage, x, y, tileS, tileS);
					ghosts.add(ghost);
				}
				else if (tileMapChar == 'P') {
					pacman = new Block(pacmanRightImage, x, y, tileS, tileS);
				}
				else if (tileMapChar == 'f') {
					Block fruit = new Block(fruitImage, x, y, tileS, tileS);
					fruits.add(fruit);
				}
				else if (tileMapChar == ' ') {
					Block food = new Block(null, x + 14, y + 14, 4, 4);
					foods.add(food);
				}
				else if (tileMapChar == 'q') {
					Block power = new Block(powerImage, x, y, tileS, tileS);
					powers.add(power);
				}
			}
		}
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		draw(g);
	}

	public void draw(Graphics g) {
		g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);
		
		for (Block fruit : fruits) {
			g.drawImage(fruit.image, fruit.x, fruit.y, fruit.width, fruit.height, null);
		}
		
		for (Block ghost : ghosts) {
			g.drawImage(ghost.image,  ghost.x,  ghost.y, ghost.width,  ghost.height, null);
		}
		
		for (Block wall : walls) {
			g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
		}
		
		for (Block power : powers) {
			g.drawImage(power.image, power.x + 8, power.y + 8, 12, 12, null);
		}
		
		g.setColor(Color.WHITE);
		for (Block food : foods) {
			g.fillRect(food.x, food.y, food.width, food.height);
		}
				
		g.setFont(new Font("Arial", Font.PLAIN, 18));
		if (gameOver) {
			g.drawString("Game Over: " + String.valueOf(score), tileS/2, tileS/2);
		}
		
		else {
			g.drawString("x" + String.valueOf(lives) + " Score: " + String.valueOf(score), tileS/2, tileS/2);
		}
	}

	public void move() {
		if (up) pacman.updateDirection('U');
		else if (down) pacman.updateDirection('D');
		else if (left) pacman.updateDirection('L');
		else if (right) pacman.updateDirection('R');
		
		pacman.x += pacman.velocityX;
		pacman.y += pacman.velocityY;
		
		for (Block wall : walls) {
			if (collision(pacman, wall)) {
				pacman.x -= pacman.velocityX;
				pacman.y -= pacman.velocityY;
				break;
			}
		}
		
		long now = System.currentTimeMillis();
	    if (now - lastMouthUpdate > mouthAnimationDelay) {
	        pacman.mouthOpen = !pacman.mouthOpen;
	        lastMouthUpdate = now;
	    }
		
		switch (pacman.direction) {
        case 'U': pacman.image = pacman.mouthOpen ? pacmanUpImage : pacmanCloseImage; break;
        case 'D': pacman.image = pacman.mouthOpen ? pacmanDownImage : pacmanCloseImage; break;
        case 'L': pacman.image = pacman.mouthOpen ? pacmanLeftImage : pacmanCloseImage; break;
        case 'R': pacman.image = pacman.mouthOpen ? pacmanRightImage : pacmanCloseImage; break;
		}
		
		for (Block ghost : ghosts) {
			if (collision(ghost, pacman)) {
				if (poweredUp) {
					ghost.reset();
					score += 300;
				} else {
					lives -= 1;
					if (lives == 0) {
						gameOver = true;
						return;
					}
				resetPositions();
				}
			}
			
			if (ghost.x == tileS*9 && ghost.direction != 'U' && ghost.direction != 'D') {
				ghost.updateDirection('U');
			}
			ghost.x += ghost.velocityX;
			ghost.y += ghost.velocityY;
			for (Block wall : walls) {
				if (collision(ghost, wall)) {
					ghost.x -= ghost.velocityX;
					ghost.y -= ghost.velocityY;
					pathfinding(ghost);
				}
			}
		}
		
		Block foodEaten = null;
		for (Block food : foods) {
			if (collision(pacman, food)) {
				foodEaten = food;
				score += 10;
			}
		}
		foods.remove(foodEaten);
		
		if (foods.isEmpty()) {
			loadMap();
			resetPositions();
		}
		
		Block fruitEaten = null;
		for (Block fruit : fruits) {
			if (collision(pacman, fruit)) {
				fruitEaten = fruit;
				score += 200;
			}
		}
		fruits.remove(fruitEaten);
		
		Block powerEaten = null;
		for (Block power : powers) {
			if (collision(pacman, power)) {
				powerEaten = power;
				for (Block ghost : ghosts) { 
					ghost.image = scaredGhostImage; 
				}
				poweredUp = true;
				poweredUpTime = System.currentTimeMillis() + poweredUpDuration;
			}
		}
		powers.remove(powerEaten);		
		
		if (poweredUp && System.currentTimeMillis() > poweredUpTime) {
			poweredUp = false;
			for (Block ghost : ghosts) {
				ghost.image = ghost.normalImage; 				
			}
		}
	}
	
	public boolean collision(Block a, Block b) {
		 	int ax = a.x + a.hitboxMargin;
		    int ay = a.y + a.hitboxMargin;
		    int aw = a.width - a.hitboxMargin * 2;
		    int ah = a.height - a.hitboxMargin * 2;

		    int bx = b.x;
		    int by = b.y;
		    int bw = b.width;
		    int bh = b.height;

		    return ax < bx + bw &&
		           ax + aw > bx &&
		           ay < by + bh &&
		           ay + ah > by;
	}
	
	public void resetPositions() {
		pacman.reset();
		pacman.velocityX = 0;
		pacman.velocityY = 0;
		for (Block ghost : ghosts) {
			ghost.reset();
			char newDirection = directions[random.nextInt(4)];
			ghost.updateDirection(newDirection);
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		move();
		repaint();	
		if (gameOver == true) {
			gameLoop.stop();
		}
	}
	
	void pathfinding(Block ghost) {
		double bestDist = Double.MAX_VALUE;
		char bestDir = ghost.direction;
		
		char reverseDir = ' ';
	    switch(ghost.direction) {
	        case 'U': reverseDir = 'D'; break;
	        case 'D': reverseDir = 'U'; break;
	        case 'L': reverseDir = 'R'; break;
	        case 'R': reverseDir = 'L'; break;
	    }

	    boolean foundValid = false;

	    for (char d : directions) {
	        if (d == reverseDir) continue; 

	        ghost.direction = d;
	        ghost.updateVelocity();

	        int nextX = ghost.x + ghost.velocityX;
	        int nextY = ghost.y + ghost.velocityY;

	        boolean blocked = false;
	        for (Block wall : walls) {
	            int originalX = ghost.x;
	            int originalY = ghost.y;

	            ghost.x = nextX;
	            ghost.y = nextY;

	            if (collision(ghost, wall)) {
	                blocked = true;
	            }

	            ghost.x = originalX;
	            ghost.y = originalY;

	            if (blocked) break;
	        }

	        if (!blocked) {
	            foundValid = true;
	            int dx = nextX - pacman.x;
	            int dy = nextY - pacman.y;
	            double distance = Math.abs(dx) + Math.abs(dy);

	            if (distance < bestDist) {
	                bestDist = distance;
	                bestDir = d;
	            }
	        }
	    }

	    if (!foundValid) bestDir = reverseDir;

	    ghost.updateDirection(bestDir);
	}

	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_UP) up = true;
	    if (e.getKeyCode() == KeyEvent.VK_DOWN) down = true;
	    if (e.getKeyCode() == KeyEvent.VK_LEFT) left = true;
	    if (e.getKeyCode() == KeyEvent.VK_RIGHT) right = true;
	    
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if(gameOver) {
			loadMap();
			resetPositions();
			lives = 3;
			score = 0;
			gameOver = false;
			gameLoop.start();
		}
		//System.out.println("Key event: " + e.getKeyCode());
		if (e.getKeyCode() == KeyEvent.VK_UP) up = false;
	    if (e.getKeyCode() == KeyEvent.VK_DOWN) down = false;
	    if (e.getKeyCode() == KeyEvent.VK_LEFT) left = false;
	    if (e.getKeyCode() == KeyEvent.VK_RIGHT) right = false;
		
	}
}
