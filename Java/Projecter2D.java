import java.awt.event.*;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.image.BufferedImage;
import java.util.Comparator;
import java.util.TreeMap;
import java.awt.*;

public class Projecter2D extends JFrame {
	private Dimension dim;
	private BufferedImage monBuf; // buffer d’affichage
	private Point cameraP = new Point(0, 0, 0);

	private double cameraTheta = 0;
	private double cameraPhi = 0;

	private double alphaMax = Math.PI / 2;
	private double focalDistance;

	private int space = 20;
	private Grid grid = new Grid(space);

	private Object3D obj;

	private Robot robot;

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

		BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		// Create a new blank cursor.
		Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new java.awt.Point(0, 0), "blank cursor");
		// Set the blank cursor to the JFrame.
		mainPanel.setCursor(blankCursor);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		try {
			robot = new Robot();
		} catch (Exception e) {
			e.printStackTrace();
		}

		addKeyListener(new MoveKeyListener());
		addMouseMotionListener(new CameraMouseListener());

		setVisible(true);

		monBuf = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_RGB);

		Timer t = new Timer(20, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				repaint();
			}
		});
		t.start();

		focalDistance = dim.getHeight() / (2 * Math.tan(alphaMax / 2.0));
	}

	public void drawLine(Graphics g, Point a, Point b) {
		a = a.getPointNewBase(cameraP, cameraTheta, cameraPhi);		
		b = b.getPointNewBase(cameraP, cameraTheta, cameraPhi);

		if (a.getY() > 0 && b.getY() > 0) {

			int x1 = a.get2DXTransformation(dim.getWidth()/2, focalDistance);
			int x2 = b.get2DXTransformation(dim.getWidth()/2, focalDistance);

			int y1 = a.get2DYTransformation(dim.getHeight()/2, focalDistance);
			int y2 = b.get2DYTransformation(dim.getHeight()/2, focalDistance);

			g.drawLine(x1, y1, x2, y2);
			g.drawString((int)a.getX() + ", " + (int)a.getY() + ", " + (int)a.getZ(), x1-10, y1-10);
			g.drawString((int)a.getX() + ", " + (int)a.getY() + ", " + (int)a.getZ(), x2-10, y2-10);
		}
	}

	public void sortTriangle(Triangle tri) {
		// Comparator comp = new Comparator();
		// TreeMap<Triangle, Color> depthTriangles = new TreeMap<Triangle, Color>();
		Vector normal = tri.getNormal();
		Vector cameraToTriangle = new Vector(tri.getCenterOfGravity(), cameraP);
		normal.normalize();
		cameraToTriangle.normalize();
		double scalarProduct = normal.getScalarProduct(cameraToTriangle);

		if (scalarProduct < 0 && Math.acos(Math.abs(scalarProduct)) < alphaMax) {}
	}

	public void drawTriangle(Graphics g, Triangle tri) {
		Vector normal = tri.getNormal();
		Vector cameraToTriangle = new Vector(tri.getCenterOfGravity(), cameraP);
		normal.normalize();
		cameraToTriangle.normalize();
		double scalarProduct = normal.getScalarProduct(cameraToTriangle);

		if (scalarProduct < 0 && Math.acos(Math.abs(scalarProduct)) < alphaMax) {
			Point a = tri.getPoints()[0].getPointNewBase(cameraP, cameraTheta, cameraPhi);
			Point b = tri.getPoints()[1].getPointNewBase(cameraP, cameraTheta, cameraPhi);
			Point c = tri.getPoints()[2].getPointNewBase(cameraP, cameraTheta, cameraPhi);

			int[] xs = new int[3];
			int[] ys = new int[3];
	
			xs[0] = a.get2DXTransformation(dim.getWidth()/2, focalDistance);
			xs[1] = b.get2DXTransformation(dim.getWidth()/2, focalDistance);
			xs[2] = c.get2DXTransformation(dim.getWidth()/2, focalDistance);

			ys[0] = a.get2DYTransformation(dim.getHeight()/2, focalDistance);
			ys[1] = b.get2DYTransformation(dim.getHeight()/2, focalDistance);
			ys[2] = c.get2DYTransformation(dim.getHeight()/2, focalDistance);

			int grey = (int) (-255.0 * scalarProduct);
			Color color = new Color(grey, grey, grey);
			g.setColor(color);
			g.fillPolygon(xs, ys, 3);
		}
	} 

	public void drawObject(Graphics g, Object3D obj) {
		// add a sorting by depth mecanism
		for (Triangle tri : obj.getFaces()) {
			drawTriangle(g, tri);
		}
	}

	public void drawGrid(Graphics g) {
		g.setColor(Color.WHITE);
		for (Point point : grid.getPoints()) {
			Point topPoint   = new Point(point.getX(), point.getY(), point.getZ() + space);
			Point backPoint  = new Point(point.getX(), point.getY() + space, point.getZ());
			Point rightPoint = new Point(point.getX() + space, point.getY(), point.getZ());

			drawLine(g, point, topPoint);
			drawLine(g, point, backPoint);
			drawLine(g, point, rightPoint);
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
		g.drawString("(" + cameraP.getX() + ", " + cameraP.getY() + ", " + cameraP.getZ() + ")", 20, 20);

		Point movement = new Point(0, 100, 0);
		movement.rotate(-cameraTheta, -cameraPhi);

		g.drawString("(" + (int)movement.getX() + ", " + (int)movement.getY() + ", " + (int)movement.getZ() + ") -> NORM = " + (int)movement.getNorm(), 20, 40);

		// drawObject(g, obj);

		g.setColor(Color.WHITE);
		drawGrid(g);
	}

	public class MoveKeyListener implements KeyListener {
		private double move = 2;

		public void keyPressed(KeyEvent e) {
			char key = e.getKeyChar();
			Point movement = new Point(0, 0, 0);

			if (key == 'z') {
				movement.addY(move);
			} else if (key == 's') {
				movement.addY(-move);
			} else if (key == 'q') {
				movement.addX(-move);
			} else if (key == 'd') {
				movement.addX(move);
			} else if (key == 'r') {
				movement.addZ(move);
			} else if (key == 'f') {
				movement.addZ(-move);
			} else if (key == 'w') {
				cameraTheta += Math.PI/2;
			} else if (key == 'x') {
				cameraTheta -= Math.PI/2;
			}

			movement.applyThetaRotation(-cameraTheta);
			// System.out.println(movement);
			cameraP.add(movement);
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
			cameraTheta -= (e.getXOnScreen() - mouseX) * 2.0 * Math.PI/dim.getWidth();
			cameraPhi   -= (e.getYOnScreen() - mouseY) * 2.0 * Math.PI/dim.getHeight();
			robot.mouseMove(mouseX, mouseY);
			// mouseX = e.getXOnScreen();
			// mouseY = e.getYOnScreen();
		}
	}
}
