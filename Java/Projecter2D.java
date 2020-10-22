import java.awt.event.*;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.image.BufferedImage;

import java.awt.*;

public class Projecter2D extends JFrame {
	private Dimension dim;
	private BufferedImage monBuf; // buffer d’affichage
	private Point cameraP = new Point(0, 0, 0);

	private double cameraTheta = 0;
	private double cameraPhi = 0;

	private double alphaMax = Math.PI / 2.0;
	private double focalDistance;

	private int space = 20;
	private Grid grid = new Grid(space);

	private Object3D obj;

	public Projecter2D(Object3D obj) {
		super("3D ENGINE");
		this.obj = obj;
		dim = Toolkit.getDefaultToolkit().getScreenSize();

		this.setExtendedState(JFrame.MAXIMIZED_BOTH); //full screen
		this.setUndecorated(true);//enleve la bande du haut

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(null);
		mainPanel.setBackground(Color.green);
		this.setContentPane(mainPanel);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		addKeyListener(new MoveKeyListener());
		addMouseMotionListener(new CameraMouseListener());

		Timer t = new Timer(5000, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				repaint();
			}
		});
		t.start();

		setVisible(true);

		monBuf = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_RGB);

		focalDistance = dim.getHeight() / (2.0 * Math.tan(alphaMax / 2.0));
	}

	public void drawLine(Point a, Point b) {
		a.getPointNewBase(cameraP, cameraTheta, cameraPhi);
		b.getPointNewBase(cameraP, cameraTheta, cameraPhi);

		int x1 = (int)((a.getX() * focalDistance ) / (a.getY() + focalDistance) - dim.getWidth()/2);
		int y1 = (int)((a.getZ() * focalDistance ) / (a.getY() + focalDistance) - dim.getWidth()/2);

		int x2 = (int)((b.getX() * focalDistance ) / (b.getY() + focalDistance) - dim.getWidth()/2);
		int y2 = (int)((b.getZ() * focalDistance ) / (b.getY() + focalDistance) - dim.getWidth()/2);

		System.out.println("a = (" + x1 + ", " + y1 + ")");
		System.out.println("b = (" + x2 + ", " + y2 + ")");

		monBuf.getGraphics().drawLine(x1, y1, x2, y2);
	}

	public void drawTriangle(Triangle tri) {
		for (int i = 0; i < 3; i++) {
			drawLine(tri.getPoints()[i], tri.getPoints()[(i+1)%3]);
			System.out.println("\n");
		}
	} 

	public void drawObject(Object3D obj) {
		for (Triangle tri : obj.getFaces()) {
			drawTriangle(tri);
			System.out.println("----------------\n");
		}
	}

	public void drawGrid() {
		for (Point point : grid.getPoints()) {
			Point p = point.getPointNewBase(cameraP, cameraTheta, cameraPhi);
			Point topPoint = new Point(point.getX(), point.getY(), point.getZ() + space).getPointNewBase(cameraP, cameraTheta, cameraPhi);
			Point backPoint = new Point(point.getX(), point.getY() + space, point.getZ()).getPointNewBase(cameraP, cameraTheta, cameraPhi);
			Point rightPoint = new Point(point.getX() + space, point.getY(), point.getZ()).getPointNewBase(cameraP, cameraTheta, cameraPhi);

			drawLine(p, topPoint);
			drawLine(p, backPoint);
			drawLine(p, rightPoint);
		}
	}

	public void paint(Graphics g) {
        Prepaint(monBuf.getGraphics());
        g.drawImage(monBuf, 0, 0, null);
	}
	
	public void Prepaint(Graphics g) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, (int)dim.getWidth(), (int)dim.getHeight());

		g.setColor(Color.WHITE);
		drawObject(obj);
		//drawGrid();
	}

	public class MoveKeyListener implements KeyListener {
		private double move = 2;

		public void keyPressed(KeyEvent e) {
			char key = e.getKeyChar();
			if (key == 'z') {
				cameraP.addX(move);
			} else if (key == 's') {
				cameraP.addX(-move);
			} else if (key == 'q') {
				cameraP.addY(-move);
			} else if (key == 'd') {
				cameraP.addY(move);
			}
		}
		public void keyReleased(KeyEvent e) {
		}
		public void keyTyped(KeyEvent e) {
		}
	}

	public class CameraMouseListener implements MouseMotionListener {
		private int mouseX = (int)(dim.getWidth() / 2.0);
		private int mouseY = (int)(dim.getHeight() / 2.0);

		public void mouseDragged(MouseEvent e) {
		}
		public void mouseMoved(MouseEvent e) {
			cameraTheta = (e.getXOnScreen() - mouseX) * 2.0 * Math.PI/dim.getWidth();
			cameraPhi   = (e.getYOnScreen() - mouseY) * 2.0 * Math.PI/dim.getHeight();
			mouseX = e.getXOnScreen();
			mouseY = e.getYOnScreen();
	
			//cameraV.set
		}
	}
}
