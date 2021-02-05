import java.awt.event.*;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.awt.*;

public class Projecter2D extends JFrame {
	private Dimension dim;
	private int centerX;
	private int centerY;

	private BufferedImage imageBuffer;// buffer d’affichage
	private BufferedImage depthBuffer; // buffer d’affichage

	private Graphics gImageBuffer;
	private Graphics gDepthBuffer;

	//pixels to draw
	private int[] xs = new int[3];
	private int[] ys = new int[3];

	//debug variables
	private boolean drawingImage = true;
	private boolean displayingDebug = false;
	private int debugMode = 0;
	private int debugChunkLevel;
	private Chunk lastDisplayedChunk;
	private Chunk nextDisplayedChunk;
	private ArrayList<Chunk> debugChunksToDraw = new ArrayList<Chunk>();

	//use for doom object
	// private Point cameraP = new Point(-63, 202, 10);
	private Point cameraP = new Point(0, 0, 10);
	private Point lightingP = new Point(0, 0, 10);

	private double cameraTheta = 0;
	private double cameraPhi = 0;

	private double cosTheta;
	private double sinTheta;
	private double cosPhi;
	private double sinPhi;

	private double alphaMax = Math.PI / 2;
	private double focalDistance;

	private World world;

	private Robot robot;
	private Boolean mouseLocked = false;

	private long startTime;

	private Vector cameraToTriangle = new Vector();
	private HashMap<Point, int[]> tempPointsInChunk = new HashMap<Point, int[]>();
	private TreeMap<Double, Triangle> trianglesInChunk = new TreeMap<Double, Triangle>(Collections.reverseOrder());

	public Projecter2D(World world) {
		super("3D ENGINE");
		this.world = world;
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

		Timer t = new Timer(20, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				repaint();
			}
		});
		t.start();

		focalDistance = dim.getHeight() / (2 * Math.tan(alphaMax / 2.0));
		centerX = (int)dim.getWidth()/2;
		centerY = (int)dim.getHeight()/2;

		imageBuffer = new BufferedImage((int)dim.getWidth(), (int)dim.getHeight(), BufferedImage.TYPE_INT_RGB);
		depthBuffer = new BufferedImage((int)dim.getWidth(), (int)dim.getHeight(), BufferedImage.TYPE_INT_RGB);

		gImageBuffer = imageBuffer.getGraphics();
		gDepthBuffer = depthBuffer.getGraphics();

		robot.mouseMove(centerX, centerY);
	}

	/** computes and draws the 2D screen projection of the line between 2 points */
	public void drawLine(Graphics g, Point a, Point b) {
		a = a.getPointNewBaseOptimized(cameraP, cosTheta, sinTheta, cosPhi, sinPhi);		
		b = b.getPointNewBaseOptimized(cameraP, cosTheta, sinTheta, cosPhi, sinPhi);

		if (a.getY() > 0 && b.getY() > 0) {

			int x1 = a.get2DXTransformation(centerX, focalDistance);
			int x2 = b.get2DXTransformation(centerX, focalDistance);

			int y1 = a.get2DYTransformation(centerY, focalDistance);
			int y2 = b.get2DYTransformation(centerY, focalDistance);

			g.drawLine(x1, y1, x2, y2);
		}
	}

	/** determines recursively which chunks to draw */
	public void sortChunksAndDrawDepthBuffer(TreeMap<Point, Surface> chunks, double chunkSize, double biggerChunkSize, int origineChunkLevel) {
		//first condition : be in screen space
		TreeMap<Double, Tuple<Chunk, Rectangle>> sortedChunks = sortChunks(chunks, chunkSize, biggerChunkSize);

		//second condition : not be hidden behind other chunks
		for (Map.Entry<Double, Tuple<Chunk, Rectangle>> entry : sortedChunks.entrySet()) {
			if (isChunkVisible(entry.getValue().getY())) {
				Chunk chunk = entry.getValue().getX();

				if (chunk.getChunkLevel() == 1) {
					sortTrianglesInChunk(entry.getValue().getX());
		
					for (Map.Entry<Double, Triangle> entry2 : trianglesInChunk.entrySet()) {
						Triangle tri = entry2.getValue();
				
						for (int i = 0; i < 3; i++) {
							int[] projection = tempPointsInChunk.get(tri.getPoints()[i]);
							
							xs[i] = projection[0];
							ys[i] = projection[1];
						}
						
						gDepthBuffer.setColor(Color.WHITE);
						gDepthBuffer.fillPolygon(xs, ys, 3);

						gImageBuffer.setColor(getTriangleShade(tri));
						gImageBuffer.fillPolygon(xs, ys, 3);
					}
				} else {
					sortChunksAndDrawDepthBuffer(chunk.getSmallerChunks(), chunkSize, biggerChunkSize, chunk.getChunkLevel());
				}
				
				//debug modes
				if (debugMode == 1) {
					if (chunk.getChunkLevel() == debugChunkLevel) {
						debugChunksToDraw.add(chunk);
					}
				} else if (debugMode == 2) {
					if (origineChunkLevel > debugChunkLevel && chunk.getChunkLevel() <= debugChunkLevel) {
						debugChunksToDraw.add(chunk);
					}	
				}
			}
		}
	}

	/** returns a list of visible chunks sorted by distance to camera */
	public TreeMap<Double, Tuple<Chunk, Rectangle>> sortChunks(TreeMap<Point, Surface> chunks, double baseChunkSize, double biggerChunkSize) {
		TreeMap<Double, Tuple<Chunk, Rectangle>> depthChunks = new TreeMap<Double, Tuple<Chunk, Rectangle>>();
		
		for (Map.Entry<Point, Surface> entry : chunks.entrySet()) {
			Chunk chunk = (Chunk)entry.getValue();
			double chunkSize = baseChunkSize * Math.pow(biggerChunkSize, chunk.getChunkLevel()-1);
			Rectangle rect = getChunkProjectionBounds(chunk, chunkSize);

			//if rect is in screen space
			if (rect != null) {
				depthChunks.put(new Vector(chunk.getCenter(chunkSize), cameraP).getNorm(), new Tuple<Chunk, Rectangle>(chunk, rect));
			}
		}
		return depthChunks;
	}

	/** returns a list of visible triangles sorted by distance to camera */
	public void sortTrianglesInChunk(Chunk chunk) {
		tempPointsInChunk.clear();
		trianglesInChunk.clear();

		for (Map.Entry<Point, Surface> entry : chunk.getSmallerChunks().entrySet()) {
			Triangle tri = (Triangle)entry.getValue();
			cameraToTriangle.recreate(tri.getCenterOfGravity(), cameraP);

			// Surface must be facing towards the camera
			if (tri.getNormal().getScalarProduct(cameraToTriangle) < 0) {
				trianglesInChunk.put(cameraToTriangle.getNorm(), tri);

				for (Point p : tri.getPoints()) {
					if (!tempPointsInChunk.containsKey(p)) {
						tempPointsInChunk.put(p, compute2DProjection(p));
					}
				}
			}
		}
	}

	/** returns a box containing the 2D view of the chunk if it is in screen space, null otherwise */
	public Rectangle getChunkProjectionBounds(Chunk chunk, double chunkSize) {
		int xMin = (int)dim.getWidth() + 1;
		int xMax = -1;
		int yMin = (int)dim.getHeight() + 1;
		int yMax = -1;

		for (Point pt : chunk.getPoints(chunkSize)) {
			pt = pt.getPointNewBaseOptimized(cameraP, cosTheta, sinTheta, cosPhi, sinPhi);
			int x = pt.get2DXTransformation(centerX, focalDistance);
			int y = pt.get2DYTransformation(centerY, focalDistance);

			xMin = Math.min(xMin, x);
			xMax = Math.max(xMax, x);
			yMin = Math.min(yMin, y);
			yMax = Math.max(yMax, y);
		}

		if (xMax < 0 || xMin > dim.getWidth() || yMax < 0 || yMin > dim.getHeight()) {
			return null;
		}

		xMin = Math.max(0, xMin);
		xMax = Math.min((int)dim.getWidth(), xMax);
		yMin = Math.max(0, yMin);
		yMax = Math.min((int)dim.getHeight(), yMax);

		return new Rectangle(xMin, xMax, yMin, yMax);
	}

	/** returns true if any pixel of the box isn't black */
	public Boolean isChunkVisible(Rectangle rect) {
		for (int i = rect.getxMin(); i < rect.getxMax(); i++) {
			for (int j = rect.getyMin(); j < rect.getyMax(); j++) {
				if (new Color(depthBuffer.getRGB(i, j)).equals(Color.BLACK)) {
					return true;
				}
			}
		}
		return false;
	}

	/** draws debug optionsin the list */
	public void drawDebugChunks() {
		// debug options
		gImageBuffer.setColor(Color.WHITE);
		for (Chunk chunk : debugChunksToDraw) {
			drawBoundaries(gImageBuffer, chunk.getPoints(world.getChunkSize() * Math.pow(world.getBiggerChunkSize(), chunk.getChunkLevel() - 1)));
		}

		if (debugMode == 3) {
			gImageBuffer.setColor(Color.GREEN);
			drawBoundaries(gImageBuffer, nextDisplayedChunk.getPoints(world.getChunkSize() * Math.pow(world.getBiggerChunkSize(), nextDisplayedChunk.getChunkLevel() - 1)));

			if (lastDisplayedChunk != null) {
				gImageBuffer.setColor(Color.RED);
				drawBoundaries(gImageBuffer, lastDisplayedChunk.getPoints(world.getChunkSize() * Math.pow(world.getBiggerChunkSize(), lastDisplayedChunk.getChunkLevel() - 1)));
			}
		}
	}

	public int[] compute2DProjection(Point p) {
		int[] pixelCoords = new int[2];

        double x = p.getX() - cameraP.getX();
        double y = p.getY() - cameraP.getY();
        double z = p.getZ() - cameraP.getZ();

        double xBefore = x;

        //theta rotation
        x = xBefore * cosTheta + y * sinTheta;
        double yBefore = y*cosTheta - xBefore*sinTheta;

        //phi rotation
        y = yBefore*cosPhi + z*sinPhi;
		z = z*cosPhi - yBefore*sinPhi;
		
		//2D Projection
		pixelCoords[0] = (int)(centerX + x * focalDistance / y);
		pixelCoords[1] = (int)(centerY - z * focalDistance / y);

		return pixelCoords;
    }

	/** draws a triangle with shade computed with direction of light */
	public Color getTriangleShade(Triangle tri) {
		Vector normal = tri.getNormal();
		Vector lightToTriangle = new Vector(tri.getCenterOfGravity(), lightingP);
		normal.normalize();
		lightToTriangle.normalize();

		double shade = Math.max(-2.0 * normal.getScalarProduct(lightToTriangle), 0);
		return new Color(Math.min(255, (int)(shade*tri.getColor().getRed())), Math.min(255, (int)(shade*tri.getColor().getGreen())), Math.min(255, (int)(shade*tri.getColor().getBlue())));
	} 

	/** draws a texture linearly on a triangle */
	/*
	public void drawTriangleWithTexture(Graphics g, BufferedImage texture, Triangle tri) {
		Vector normal = tri.getNormal();
		Vector lightToTriangle = new Vector(tri.getCenterOfGravity(), lightingP);
		normal.normalize();
		lightToTriangle.normalize();

		double shade = Math.max(-2.0 * normal.getScalarProduct(lightToTriangle), 0);

		int[] xs = new int[3];
		int[] ys = new int[3];

		int xMin = (int)dim.getWidth() + 1;
		int xMax = -1;
		int yMin = (int)dim.getHeight() + 1;
		int yMax = -1;

		int ixMin = 0;
		int ixMax = 0;
		int iyMin = 0;
		int iyMax = 0;

		for (int i = 0; i < 3; i++) {
			Point ptNewBase = tri.getPoints()[i].getPointNewBaseOptimized(cameraP, cosTheta, sinTheta, cosPhi, sinPhi);
			xs[i] = ptNewBase.get2DXTransformation(centerX, focalDistance);
			ys[i] = ptNewBase.get2DYTransformation(centerY, focalDistance);

			if (xs[i] < xMin) {
				xMin = xs[i];
				ixMin = i;
			}
			if (xs[i] > xMax) {
				xMax = xs[i];
				ixMax = i;
			}

			if (ys[i] < yMin) {
				yMin = ys[i];
				iyMin = i;
			}
			if (ys[i] > yMax) {
				yMax = ys[i];
				iyMax = i;
			}
		}

		double xDiff = ((double)tri.getTexturePoints()[ixMax].getX() - (double)tri.getTexturePoints()[ixMin].getX()) / ((double)xMax - (double)xMin);
		double yDiff = ((double)tri.getTexturePoints()[iyMax].getY() - (double)tri.getTexturePoints()[iyMin].getY()) / ((double)yMax - (double)yMin);

		for (int i = xMin; i < xMax; i++) {
			for (int j = yMin; j < yMax; j++) {
				if (IsPointInTriangle(i, j, xs[1], ys[1], xs[2], ys[2], xs[3], ys[3])) {
					int x = (int)((i - xMin) * xDiff + tri.getTexturePoints()[ixMin].getX());
					int y = (int)((j - yMin) * yDiff + tri.getTexturePoints()[iyMin].getY());
					Color color = new Color(texture.getRGB(x, y));
					g.setColor(new Color(Math.min(255, (int)(shade*color.getRed())), Math.min(255, (int)(shade*color.getGreen())), Math.min(255, (int)(shade*tri.getColor().getBlue()))));
				}
			}
		}
	}

	/** returns the sign of the cross product */
	/*
	public Boolean getSign(int x1, int y1, int x2, int y2, int x3, int y3) {
		  return (x1 - x3) * (y2 - y3) < (x2 - x3) * (y1 - y3);
	}

	/** checks if a given point is a triangle by a methode of cross products */
	/*
	public boolean IsPointInTriangle(int xPt, int yPt, int x1, int y1, int x2, int y2, int x3, int y3){
		return (getSign(xPt, yPt, x1, y1, x2, y2) && getSign(xPt, yPt, x2, y2, x3, y3) && getSign(xPt, yPt, x3, y3, x1, y1));
	}
	

	/** draws the box surrounding the chunk */
	public void drawBoundaries(Graphics g, Point[] points) {
		drawLine(g, points[0], points[1]);
		drawLine(g, points[2], points[3]);
		drawLine(g, points[4], points[5]);
		drawLine(g, points[6], points[7]);

		drawLine(g, points[0], points[2]);
		drawLine(g, points[1], points[3]);
		drawLine(g, points[4], points[6]);
		drawLine(g, points[5], points[7]);

		drawLine(g, points[0], points[4]);
		drawLine(g, points[1], points[5]);
		drawLine(g, points[2], points[6]);
		drawLine(g, points[3], points[7]);
	}

	public void paint(Graphics g) {
		Prepaint();
		if (drawingImage) {
			g.drawImage(imageBuffer, 0, 0, null);
		} else {
			g.drawImage(depthBuffer, 0, 0, null);
		}
	}
	
	public void Prepaint() {
		gDepthBuffer.setColor(Color.BLACK);
		gDepthBuffer.fillRect(0, 0, (int)dim.getWidth(), (int)dim.getHeight());
		gDepthBuffer.setColor(Color.WHITE);

		gImageBuffer.setColor(Color.BLACK);
		gImageBuffer.fillRect(0, 0, (int)dim.getWidth(), (int)dim.getHeight());

		cosTheta = Math.cos(cameraTheta);
		sinTheta = Math.sin(cameraTheta);
		cosPhi = Math.cos(cameraPhi);
		sinPhi = Math.sin(cameraPhi);

		if (debugMode != 3 && debugMode != 0) {
			debugChunksToDraw.clear();
		}

		startTime = System.currentTimeMillis();
		sortChunksAndDrawDepthBuffer(world.getChunks(), world.getChunkSize(), world.getBiggerChunkSize(), world.getChunkLevel() + 1);

		if (debugMode != 0) {
			drawDebugChunks();
		}
		
		if (displayingDebug) {
			if (drawingImage) {
				displayDebug(gImageBuffer);
			} else {
				displayDebug(gDepthBuffer);
			}
		}
	}

	public void displayDebug(Graphics g) {
		g.setColor(Color.WHITE);
		g.drawString("(" + (int)cameraP.getX() + ", " + (int)cameraP.getY() + ", " + (int)cameraP.getZ() + ")", 20, 20);

		Point movement = new Point(0, 100, 0);
		movement.rotate(-cameraTheta, -cameraPhi);
		g.drawString("(" + ((double)(int)movement.getX()/100) + ", " + ((double)(int)movement.getY()/100) + ", " + ((double)(int)movement.getZ()/100) + ")", 20, 40);
		g.drawString("REFRESH TIME : " + (System.currentTimeMillis() - startTime) + " ms", 20, 60);
		g.drawString("DISPLAYING : " + debugChunksToDraw.size() + " chunks\n", 20, 80);
		g.drawString("CHUNK LEVEL : " + debugChunkLevel, 20, 100);
		g.drawString("DISPLAY MODE : " + debugMode, 20, 120);
		if (debugMode == 3) {
			Point p = nextDisplayedChunk.getCoord();
			g.drawString("CHUNK INFO : " + p, 20, 140);
			g.drawString("CHUNK POINT : " + world.computeChunkPoint(p, world.getChunkSize() * Math.pow(world.getBiggerChunkSize(), nextDisplayedChunk.getChunkLevel())), 20, 160);
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
			} else if (key == 't') {
				move *= 2;
			} else if (key == 'g') {
				move /= 2;
			} else if (key == 'y') {
				debugChunkLevel += 1;
				if (debugChunkLevel > world.getChunkLevel()) {
					debugChunkLevel = 1;
				}
			} else if (key == 'h') {
				debugChunkLevel -= 1;
				if (debugChunkLevel == 0) {
					debugChunkLevel = world.getChunkLevel();
				}
			} else if (key == 'u') {
				mouseLocked = !mouseLocked;
			} else if (key == 'i') {
				drawingImage = !drawingImage;
			} else if (key == 'o') {
				debugMode++;
				if (debugMode == 3) {
					lastDisplayedChunk = null;
					for (Map.Entry<Point, Surface> entry : world.getChunks().entrySet()) {
						debugChunksToDraw.add((Chunk)entry.getValue());
					}
					nextDisplayedChunk = debugChunksToDraw.get(0);
				} else if (debugMode == 4) {
					debugMode = 0;
				}
			} else if (key == 'p') {
				displayingDebug = !displayingDebug;
			} else if (key == 'n') {
				int index = debugChunksToDraw.indexOf(nextDisplayedChunk) + 1;
				if (index >= debugChunksToDraw.size()) {
					index = 0;
				}
				nextDisplayedChunk = debugChunksToDraw.get(index);
			} else if (key == 'b') {
				lastDisplayedChunk = nextDisplayedChunk;

				debugChunksToDraw.clear();
				for (Map.Entry<Point, Surface> entry : lastDisplayedChunk.getSmallerChunks().entrySet()) {
					debugChunksToDraw.add((Chunk)entry.getValue());
				}				
				nextDisplayedChunk = debugChunksToDraw.get(0);
			} else if (key == 'v') {
				lastDisplayedChunk = null;

				debugChunksToDraw.clear();
				for (Map.Entry<Point, Surface> entry : world.getChunks().entrySet()) {
					debugChunksToDraw.add((Chunk)entry.getValue());
				}
				nextDisplayedChunk = debugChunksToDraw.get(0);
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
		public void mouseDragged(MouseEvent e) {
		}
		public void mouseMoved(MouseEvent e) {
			if (!mouseLocked) {
				cameraTheta -= (e.getXOnScreen() - centerX) * 2.0 * Math.PI/dim.getWidth();
				cameraPhi   -= (e.getYOnScreen() - centerY) * 2.0 * Math.PI/dim.getHeight();
				robot.mouseMove(centerX, centerY);
			}
		}
	}
}
