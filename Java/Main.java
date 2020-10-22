import java.awt.event.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.*;

public class Main extends JFrame implements ActionListener, KeyListener, MouseMotionListener {
	private Dimension dim;
	private JPanel mainPanel;
	private BufferedImage monBuf; // buffer d’affichage

	//timer
	private Timer t;
	private int dt = 500;

	private Object3D obj;
	private int space = 20;
	private int move = 2;
	private Grid grid = new Grid(space);

	private Point cameraP = new Point(0, 0, 0);
	private Vector cameraV = new Vector(1, 0, 0);
	private double alphaMax = Math.PI / 2.0;

	private int mouseX, mouseY;

	public Main() {
		super("3D ENGINE");
		dim = Toolkit.getDefaultToolkit().getScreenSize();

		this.setExtendedState(JFrame.MAXIMIZED_BOTH); //full screen
		this.setUndecorated(true);//enleve la bande du haut

        //Jpanel principal (content pane)
        mainPanel = new JPanel();
        mainPanel.setLayout(null);
		mainPanel.setBackground(Color.green);
		this.setContentPane(mainPanel);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addKeyListener(this);
		addMouseMotionListener(this);
		setVisible(true);

		t = new Timer(dt, this);
		t.start();

		monBuf = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_RGB);

		repaint();
	}

	public void paint(Graphics g) {
        Prepaint(monBuf.getGraphics());
        g.drawImage(monBuf, 0, 0, null);
	}
	
	public void Prepaint(Graphics g) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, (int)dim.getWidth(), (int)dim.getHeight());

		g.setColor(Color.WHITE);
		//obj.get2dProjections(cameraV, cameraP, alphaMax);

		for (Point point : grid.getPoints()) {
			point.get2dProjections(cameraV, cameraP, alphaMax);

			Point topPoint = new Point(point.getX(), point.getY(), point.getZ() + space);
			Point backPoint = new Point(point.getX(), point.getY() + space, point.getZ());
			Point rightPoint = new Point(point.getX() + space, point.getY(), point.getZ());

			topPoint.get2dProjections(cameraV, cameraP, alphaMax);
			backPoint.get2dProjections(cameraV, cameraP, alphaMax);
			rightPoint.get2dProjections(cameraV, cameraP, alphaMax);

			int x1 = (int)(point.getX2D() * dim.getWidth()/2 + dim.getWidth()/2);
			int y1 = (int)(-point.getY2D() * dim.getHeight()/2 + dim.getHeight()/2);
			int x2 = (int)(topPoint.getX2D() * dim.getWidth()/2 + dim.getWidth()/2);
			int y2 = (int)(-topPoint.getY2D() * dim.getHeight()/2 + dim.getHeight()/2);
			g.drawLine(x1, y1, x2, y2);

			x2 = (int)(backPoint.getX2D() * dim.getWidth()/2 + dim.getWidth()/2);
			y2 = (int)(-backPoint.getY2D() * dim.getHeight()/2 + dim.getHeight()/2);
			g.drawLine(x1, y1, x2, y2);

			x2 = (int)(rightPoint.getX2D() * dim.getWidth()/2 + dim.getWidth()/2);
			y2 = (int)(-rightPoint.getY2D() * dim.getHeight()/2 + dim.getHeight()/2);
			g.drawLine(x1, y1, x2, y2);
		}

		/*for (Triangle tri : obj.getFaces()) {
			for (int i = 0; i < tri.getPoints().length; i++) {
				int x1 = (int)(tri.getPoints()[i].getX2D() * dim.getWidth()/2 + dim.getWidth()/2);
				int y1 = (int)(-tri.getPoints()[i].getY2D() * dim.getHeight()/2 + dim.getHeight()/2);
				int x2 = (int)(tri.getPoints()[(i+1)%3].getX2D() * dim.getWidth()/2 + dim.getWidth()/2);
				int y2 = (int)(-tri.getPoints()[(i+1)%3].getY2D() * dim.getHeight()/2 + dim.getHeight()/2);
				g.drawLine(x1, y1, x2, y2);
			}
		}*/
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == t) {
			//move();
			repaint();
		}
	}

	public void move() {
		int coord = (int)(Math.random() * 3.0);
		for (Triangle tri : obj.getFaces()) {
			for (int i = 0; i < tri.getPoints().length; i++) {
				if (coord == 0) {
					tri.getPoints()[i].setX(2);
				} else if (coord == 1) {
					tri.getPoints()[i].setY(2);
				} else if (coord == 2) {
					tri.getPoints()[i].setZ(2);
				}
			}
		}
	}

	public void keyPressed(KeyEvent e) {
		char key = e.getKeyChar();
		if (key == 'z') {
			cameraP.setX(move);
			repaint();
		} else if (key == 's') {
			cameraP.setX(-move);
			repaint();
		} else if (key == 'q') {
			cameraP.setY(-move);
			repaint();
		} else if (key == 'd') {
			cameraP.setY(move);
			repaint();
		}
	}
	public void keyReleased(KeyEvent e) {
	}
	public void keyTyped(KeyEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
	}
	public void mouseMoved(MouseEvent e) {
		double diffX = (e.getXOnScreen() - mouseX) * Math.PI/dim.getWidth();
		double diffY = (e.getYOnScreen() - mouseY) * Math.PI/dim.getHeight();
		mouseX = e.getXOnScreen();
		mouseY = e.getYOnScreen();

		//cameraV.set
	}

	public static void main(String[] args) {
		Point a = new Point(10, 10, 10);
		Point b = new Point(10, 10, 20);
		Point c = new Point(20, 10, 10);
		Point d = new Point(20, 10, 20);

		Point e = new Point(10, 20, 10);
		Point f = new Point(10, 20, 20);
		Point g = new Point(20, 20, 10);
		Point h = new Point(20, 20, 20);

		Triangle t1 = new Triangle(a, b, c);
		Triangle t2 = new Triangle(b, c, d);

		Triangle t3 = new Triangle(e, f, g);
		Triangle t4 = new Triangle(f, g, h);

		Triangle t5 = new Triangle(a, c, e);
		Triangle t6 = new Triangle(e, g, c);

		Triangle t7 = new Triangle(b, d, f);
		Triangle t8 = new Triangle(d, f, h);

		Triangle t9 = new Triangle(a, b, e);
		Triangle t10 = new Triangle(b, e, f);

		Triangle t11 = new Triangle(c, d, g);
		Triangle t12 = new Triangle(d, g, h);

		ArrayList<Triangle> list = new ArrayList<Triangle>();
		list.add(t1);
		list.add(t2);
		list.add(t3);
		list.add(t4);
		list.add(t5);
		list.add(t6);
		list.add(t7);
		list.add(t8);
		list.add(t9);
		list.add(t10);
		list.add(t11);
		list.add(t12);

		Object3D obj = new Object3D(list);

		new Main();
	}
}
