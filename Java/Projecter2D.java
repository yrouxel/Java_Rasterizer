import java.awt.event.*;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeMap;
import java.awt.*;

/** handles 2D projection of a world and movement in the world */
public class Projecter2D extends JFrame {
	//dimension variables
	private Dimension dim;
	private int centerX;
	private int centerY;

	//images and their graphics
	private BufferedImage imageBuffer;
	private BufferedImage depthBuffer;
	private Graphics gImageBuffer;
	private Graphics gDepthBuffer;

	//pre initiated variables shade computing and bounds computing
	private Point replaceablePointForChunks = new Point();
	private Vector lightToTriangle = new Vector();
	private Vector cameraToTriangle = new Vector();
	private Vector cameraToVisibilityCone = new Vector();

	//pixels to draw
	private int[] projection = new int[2];
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
	private Point cameraP = new Point(-300, -1500, 1500);
	private Point lightingP = new Point(-300, -1500, 1500);
	private Point directionP = new Point(0, 1, 0);
	// private Point lightingP = cameraP;

	//camera rotations
	private double cameraTheta = 0;
	private double cameraPhi = 0;

	//calculated sinus
	private double cosTheta;
	private double sinTheta;
	private double cosPhi;
	private double sinPhi;

	//2D projection variables
	private double alphaMax = Math.PI / 2;
	private double focalDistance;

	private World world;

	//mouse variables
	private Robot robot;
	private Boolean mouseLocked = false;

	//timer
	private long startTime;

	//maps for general purpose
	private HashMap<Point, int[]> tempPointsInChunk = new HashMap<Point, int[]>(128);
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

	/** determines recursively which chunks to draw */
	public void sortChunksAndDrawDepthBuffer(TreeMap<Point, Surface> chunks, int origineChunkLevel) {
		//first condition : be in screen space
		TreeMap<Double, Chunk> sortedChunks = sortChunks(chunks);

		for (Chunk chunk : sortedChunks.values()) {
			//second condition : not be hidden behind other chunks
			if (isChunkVisible(chunk)) {
				if (chunk.getChunkLevel() == 0) {
					sortTrianglesInChunk(chunk);
					drawTrianglesInChunk2();
				} else {
					sortChunksAndDrawDepthBuffer(chunk.getSmallerChunks(), chunk.getChunkLevel());
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

	/** draws the triangle found in trianglesInChunk */
	public void drawTrianglesInChunk() {
		for (Triangle tri : trianglesInChunk.values()) {				
			for (int i = 0; i < 3; i++) {
				int[] projection = tempPointsInChunk.get(tri.getPoints()[i]);
				
				xs[i] = projection[0];
				ys[i] = projection[1];
			}
			
			//drawing in depth buffer
			gDepthBuffer.setColor(Color.WHITE);
			gDepthBuffer.fillPolygon(xs, ys, 3);

			//drawing in actual image
			gImageBuffer.setColor(new Color(getTriangleShade(tri)));
			gImageBuffer.fillPolygon(xs, ys, 3);
		}
	}


	/** draws the triangle found in trianglesInChunk with clipping and depthbuffer drawing */
	public void drawTrianglesInChunk2() {
		for (Triangle tri : trianglesInChunk.values()) {
			int rgb = getTriangleShade(tri);

			int xMin = Integer.MAX_VALUE;
			int xMax = Integer.MIN_VALUE;
			int yMin = Integer.MAX_VALUE;
			int yMax = Integer.MIN_VALUE;
			
			//compute bounding box
			for (int i = 0; i < 3; i++) {
				int[] projection = tempPointsInChunk.get(tri.getPoints()[i]);
				
				xs[i] = projection[0];
				ys[i] = projection[1];
		
				xMin = Math.min(xMin, xs[i]);
				xMax = Math.max(xMax, xs[i]);
				yMin = Math.min(yMin, ys[i]);
				yMax = Math.max(yMax, ys[i]);
			}

			//if box is at least partially inside screen space clip it
			if (xMax >= 0 && xMin < dim.getWidth() && yMax >= 0 && yMin < dim.getHeight()) {
				xMin = Math.max(0, xMin);
				xMax = Math.min((int)dim.getWidth()-1, xMax);
				yMin = Math.max(0, yMin);
				yMax = Math.min((int)dim.getHeight()-1, yMax);

				//iterate through it, compare with depth buffer color, check point is inside the triangle, paint it
				boolean firstPixelFound;
				for (int y = yMin; y < yMax; y++) {
					firstPixelFound = false;
					for (int x = xMin; x < xMax; x++) {
						if (depthBuffer.getRGB(x, y) == -16777216) {
							if (isPointInTriangle(x + 0.5, y + 0.5, xs[0], ys[0], xs[1], ys[1], xs[2], ys[2])) {
								imageBuffer.setRGB(x, y, rgb);
								depthBuffer.setRGB(x, y, -1);
								if (!firstPixelFound) {
									firstPixelFound = true;
								}
							} else if (firstPixelFound) {
								break;
							}
						}
					}
				}
			}
		}
	}

	/** returns a list of visible chunks sorted by distance to camera */
	public TreeMap<Double, Chunk> sortChunks(TreeMap<Point, Surface> chunks) {
		TreeMap<Double, Chunk> depthChunks = new TreeMap<Double, Chunk>();
		
		for (Surface surface : chunks.values()) {
			Chunk chunk = (Chunk)surface;

			if (!chunk.isAlwaysVisible()) {
				cameraToVisibilityCone.setVector(chunk.getConeTop(), cameraP);
				double coneAngle = chunk.getNormal().getScalarProduct(cameraToVisibilityCone) / (chunk.getNormal().getNorm() * cameraToVisibilityCone.getNorm());

				if (coneAngle >= chunk.getConeAngle()) {
					depthChunks.put(new Vector(chunk.getCenter(), cameraP).getNorm(), chunk);
				}
			} else {
				depthChunks.put(new Vector(chunk.getCenter(), cameraP).getNorm(), chunk);
			}
		}
		return depthChunks;
	}

	/** returns a list of visible triangles sorted by distance to camera */
	public void sortTrianglesInChunk(Chunk chunk) {
		tempPointsInChunk.clear();
		trianglesInChunk.clear();

		for (Surface surface : chunk.getSmallerChunks().values()) {
			Triangle tri = (Triangle)surface;
			cameraToTriangle.setVector(tri.getCenterOfGravity(), cameraP);

			// Surface must be facing towards the camera
			if (tri.getNormal().getScalarProduct(cameraToTriangle) > 0) {
				trianglesInChunk.put(cameraToTriangle.getNorm(), tri);

				//maps points in the chunk to their 2D projection
				for (Point p : tri.getPoints()) {
					if (!tempPointsInChunk.containsKey(p)) {
						tempPointsInChunk.put(p, compute2DProjectionWithReturn(p));
					}
				}
			}
		}
	}

	/** returns a box containing the 2D view of the chunk if it is in screen space and contains at least one visible pixel, null otherwise */
	public boolean isChunkVisible(Chunk chunk) {
		int xMin = Integer.MAX_VALUE;
		int xMax = Integer.MIN_VALUE;
		int yMin = Integer.MAX_VALUE;
		int yMax = Integer.MIN_VALUE;

		Point pt = chunk.getCoord();
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				for (int k = 0; k < 2; k++) {
					replaceablePointForChunks.replace(pt.getX() + i * chunk.getChunkSize(), pt.getY() + j * chunk.getChunkSize(), pt.getZ() + k * chunk.getChunkSize());
					compute2DProjection(replaceablePointForChunks);

					xMin = Math.min(xMin, projection[0]);
					xMax = Math.max(xMax, projection[0]);
					yMin = Math.min(yMin, projection[1]);
					yMax = Math.max(yMax, projection[1]);
				}
			}
		}

		//if the box is out of screen space
		if (xMax < 0 || xMin >= dim.getWidth() || yMax < 0 || yMin >= dim.getHeight()) {
			return false;
		}

		xMin = Math.max(0, xMin);
		xMax = Math.min((int)dim.getWidth()-1, xMax);
		yMin = Math.max(0, yMin);
		yMax = Math.min((int)dim.getHeight()-1, yMax);

		//if at least one pixel is visible (black) in depth buffer
		for (int i = xMin; i < xMax; i++) {
			for (int j = yMin; j < yMax; j++) {
				if (depthBuffer.getRGB(i, j) == -16777216) {
					return true;
				}
			}
		}
		return false;
	}

	/** computes the coordinates of the point in the new base, then computes its 2D projection */
	public void compute2DProjection(Point p) {
		//translation
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
		projection[0] = (int)(centerX + x * focalDistance / y);
		projection[1] = (int)(centerY - z * focalDistance / y);
    }

	/** same function as above, but returns the result */
	public int[] compute2DProjectionWithReturn(Point p) {
		//translation
		int[] projection = new int[2];

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
		projection[0] = (int)(centerX + x * focalDistance / y);
		projection[1] = (int)(centerY - z * focalDistance / y);

		return projection;
    }

	/** draws a triangle with shade computed with direction of light */
	public int getTriangleShade(Triangle tri) {
		lightToTriangle.setVector(tri.getCenterOfGravity(), lightingP);
		lightToTriangle.normalize();

		// rgb = (red << 16 | green << 8 | blue)
		double shade = Math.max(0, tri.getNormal().getScalarProduct(lightToTriangle));
		return (int)(shade*tri.getColor().getRed()) << 16 | (int)(shade*tri.getColor().getGreen()) << 8 | (int)(shade*tri.getColor().getBlue());
	} 

	/** returns the sign of the cross product */
	public boolean edgeFunction(double x1, double y1, double x2, double y2, double x3, double y3) {
		return (x1 - x3) * (y2 - y3) >= (x2 - x3) * (y1 - y3);
	}

	/** checks if a given point is a triangle by a methode of cross products */
	public boolean isPointInTriangle(double xPt, double yPt, int x1, int y1, int x2, int y2, int x3, int y3){
		return (edgeFunction(xPt, yPt, x1, y1, x2, y2) && edgeFunction(xPt, yPt, x2, y2, x3, y3) && edgeFunction(xPt, yPt, x3, y3, x1, y1));
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

	/** computes and draws the 2D screen projection of the line between 2 points */
	public void drawLine(Graphics g, Point a, Point b) {
		int[] projA = compute2DProjectionWithReturn(a);
		int[] projB = compute2DProjectionWithReturn(b);

		// if (a.getY() > 0 && b.getY() > 0) {
			g.drawLine(projA[0], projA[1], projB[0], projB[1]);
		// }
	}

	/** draws debug optionsin the list */
	public void drawDebugChunks() {
		gImageBuffer.setColor(Color.WHITE);
		for (Chunk chunk : debugChunksToDraw) {
			drawBoundaries(gImageBuffer, chunk.getPoints());
		}

		if (debugMode == 3) {
			gImageBuffer.setColor(Color.GREEN);
			drawBoundaries(gImageBuffer, nextDisplayedChunk.getPoints());

			if (lastDisplayedChunk != null) {
				gImageBuffer.setColor(Color.RED);
				drawBoundaries(gImageBuffer, lastDisplayedChunk.getPoints());
			}
		}
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

		directionP.replace(0, 1, 0);
		directionP.rotate(-cameraTheta, -cameraPhi);

		if (debugMode != 3 && debugMode != 0) {
			debugChunksToDraw.clear();
		}

		startTime = System.currentTimeMillis();
		sortChunksAndDrawDepthBuffer(world.getChunks(), world.getChunkLevel() + 1);

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
		g.drawString("(" + directionP.getX() + ", " + directionP.getY() + ", " + directionP.getZ() + ")", 20, 40);
		g.drawString("REFRESH TIME : " + (System.currentTimeMillis() - startTime) + " ms", 20, 60);
		g.drawString("DISPLAYING : " + debugChunksToDraw.size() + " chunks\n", 20, 80);
		g.drawString("CHUNK LEVEL : " + debugChunkLevel, 20, 100);
		g.drawString("DISPLAY MODE : " + debugMode, 20, 120);
		if (debugMode == 3) {
			Point p = nextDisplayedChunk.getCoord();
			g.drawString("CHUNK INFO : " + p, 20, 140);
			g.drawString("CHUNK POINT : " + world.computeChunkPoint(p, world.getChunkSizes().get(0) * Math.pow(world.getBiggerChunkSize(), nextDisplayedChunk.getChunkLevel())), 20, 160);
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
				debugChunkLevel = (debugChunkLevel + 1)%world.getChunkLevel();
			} else if (key == 'h') {
				debugChunkLevel -= 1;
				if (debugChunkLevel == -1) {
					debugChunkLevel = world.getChunkLevel();
				}
			} else if (key == 'u') {
				mouseLocked = !mouseLocked;
			} else if (key == 'i') {
				drawingImage = !drawingImage;
			} else if (key == 'o') {
				debugMode++;
				if (debugMode == 3) {
					debugChunksToDraw.clear();
					lastDisplayedChunk = null;
					for (Surface surface : world.getChunks().values()) {
						debugChunksToDraw.add((Chunk)surface);
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
				for (Surface surface : lastDisplayedChunk.getSmallerChunks().values()) {
					debugChunksToDraw.add((Chunk)surface);
				}				
				nextDisplayedChunk = debugChunksToDraw.get(0);
			} else if (key == 'v') {
				lastDisplayedChunk = null;

				debugChunksToDraw.clear();
				for (Surface surface : world.getChunks().values()) {
					debugChunksToDraw.add((Chunk)surface);
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
