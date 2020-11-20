import java.awt.event.*;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.awt.*;

public class Projecter2D extends JFrame {
	private Dimension dim;
	private BufferedImage monBuf; // buffer d’affichage
	private Point cameraP = new Point(0, 0, 10);
	private Point lightingP = new Point(0, 0, 10);

	private double cameraTheta = 0;
	private double cameraPhi = 0;

	private double alphaMax = Math.PI / 2;
	private double focalDistance;

	private int space = 20;
	private Grid grid = new Grid(space);

	private Object3D obj;

	private Robot robot;
	private Boolean mouseLocked = false;

	TreeMap<Double, Triangle> depthTriangles = new TreeMap<Double, Triangle>(Collections.reverseOrder());

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
		a = a.getPointNewBaseOptimized(cameraP, cameraTheta, cameraPhi);		
		b = b.getPointNewBaseOptimized(cameraP, cameraTheta, cameraPhi);

		if (a.getY() > 0 && b.getY() > 0) {

			int x1 = a.get2DXTransformation(dim.getWidth()/2, focalDistance);
			int x2 = b.get2DXTransformation(dim.getWidth()/2, focalDistance);

			int y1 = a.get2DYTransformation(dim.getHeight()/2, focalDistance);
			int y2 = b.get2DYTransformation(dim.getHeight()/2, focalDistance);

			g.drawLine(x1, y1, x2, y2);
			// g.drawString((int)a.getX() + ", " + (int)a.getY() + ", " + (int)a.getZ(), x1-10, y1-10);
			// g.drawString((int)a.getX() + ", " + (int)a.getY() + ", " + (int)a.getZ(), x2-10, y2-10);
		}
	}

	public void drawTriangles(Graphics g) {
		for (Map.Entry<Double, Triangle> entry : depthTriangles.entrySet()) {
			drawTriangle(g, entry.getValue());
		}
	}

	public void drawTriangle(Graphics g, Triangle tri) {
		Vector normal = tri.getNormal();
		Vector lightToTriangle = new Vector(tri.getCenterOfGravity(), lightingP);
		normal.normalize();
		lightToTriangle.normalize();
		double shade = Math.max(-2.0 * normal.getScalarProduct(lightToTriangle), 0);

		Point a = tri.getPoints()[0].getPointNewBaseOptimized(cameraP, cameraTheta, cameraPhi);
		Point b = tri.getPoints()[1].getPointNewBaseOptimized(cameraP, cameraTheta, cameraPhi);
		Point c = tri.getPoints()[2].getPointNewBaseOptimized(cameraP, cameraTheta, cameraPhi);

		int[] xs = new int[3];
		int[] ys = new int[3];

		xs[0] = a.get2DXTransformation(dim.getWidth()/2, focalDistance);
		xs[1] = b.get2DXTransformation(dim.getWidth()/2, focalDistance);
		xs[2] = c.get2DXTransformation(dim.getWidth()/2, focalDistance);

		ys[0] = a.get2DYTransformation(dim.getHeight()/2, focalDistance);
		ys[1] = b.get2DYTransformation(dim.getHeight()/2, focalDistance);
		ys[2] = c.get2DYTransformation(dim.getHeight()/2, focalDistance);

		g.setColor(new Color(Math.min(255, (int)(shade*tri.getColor().getRed())), Math.min(255, (int)(shade*tri.getColor().getGreen())), Math.min(255, (int)(shade*tri.getColor().getBlue()))));
		g.fillPolygon(xs, ys, 3);
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

		sortTriangles();
		drawTriangles(g);

		// displayComments(g);
		// g.setColor(Color.WHITE);
		// drawGrid(g);
	}

	public void displayComments(Graphics g) {
		g.setColor(Color.WHITE);
		g.drawString("(" + cameraP.getX() + ", " + cameraP.getY() + ", " + cameraP.getZ() + ")", 20, 20);

		Point movement = new Point(0, 100, 0);
		movement.rotate(-cameraTheta, -cameraPhi);
		g.drawString("(" + (int)movement.getX() + ", " + (int)movement.getY() + ", " + (int)movement.getZ() + ")", 20, 40);
	}

	public void sortTriangles() {
		depthTriangles.clear();
		for (Triangle tri : obj.getFaces()) {
			Point center = tri.getCenterOfGravity();
			Vector cameraToTriangle = new Vector(center, cameraP);
			center = center.getPointNewBaseOptimized(cameraP, cameraTheta, cameraPhi);
			// triangles must be facing towards the camera and in the fov
			//triangles facing us should be the last condition in a real world, but here i'm displaying only one object
			if (tri.getNormal().getScalarProduct(cameraToTriangle) < 0 && Math.abs(center.get2DYTransformation(0, focalDistance)) < dim.getHeight() && Math.abs(center.get2DXTransformation(0, focalDistance)) < dim.getWidth()) {
				depthTriangles.put(cameraToTriangle.getNorm(), tri);
			}
		}
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
				mouseLocked = !mouseLocked;
			} else if (key == 't') {
				move *= 2;
			} else if (key == 'g') {
				move /= 2;
			}

			movement.rotate(-cameraTheta, -cameraPhi);
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
			if (!mouseLocked) {
				cameraTheta -= (e.getXOnScreen() - mouseX) * 2.0 * Math.PI/dim.getWidth();
				cameraPhi   -= (e.getYOnScreen() - mouseY) * 2.0 * Math.PI/dim.getHeight();
				robot.mouseMove(mouseX, mouseY);
			}
		}
	}
}
