import java.awt.event.*;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.awt.*;

/** handles 2D projection of a world and movement in the world */
public class Projecter2D extends JFrame {
	private ArrayList<View> views = new ArrayList<View>();
	private View view;

	//dimension variables
	private int width;
	private int height;
	private int centerX;
	private int centerY;

	//debug variables
	private int displayMode;
	private boolean drawingImage = true;
	private int debugChunkLevel;
	private Chunk lastDisplayedChunk;
	private Chunk nextDisplayedChunk;
	private ArrayList<Chunk> debugChunksToDraw = new ArrayList<Chunk>();
	private boolean needsRefresh = true;

	//the whole world man
	private World world;

	//mouse variables
	private Robot robot;
	private Boolean mouseLocked = false;

	//timer
	private long startTime;
	private Timer timer = new Timer(20, new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			repaint();
		}
	});

	public Projecter2D(World world) {
		super("3D ENGINE");
		this.world = world;

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		width = (int)dim.getWidth();
		height = (int)dim.getHeight();
		centerX = width / 2;
		centerY =  height / 2;

		Point p = new Point(-300, -1500, 1516);
		// Point p = new Point(0, 0, 0);
		views.add(new PlayerView(p, world.getChunkLevel(), Math.PI / 2, width, height, 1));
		view = views.get(0);
		views.add(new LightView(p, world.getChunkLevel(), Math.PI / 2, 3*width, 3*height, Color.WHITE.getRGB(), false));

		for (View view : views) {
			// view.addDirection(Math.PI, 0);
			view.computeView(world.getChunks(), world.getChunkLevel() + 1, debugChunkLevel);
		}

		//full screen
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		this.setUndecorated(true);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(null);
		mainPanel.setBackground(Color.green);
		this.setContentPane(mainPanel);

		//invisible cursor
		BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new java.awt.Point(0, 0), "blank cursor");
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

		timer.start();
		robot.mouseMove(centerX, centerY);
	}

	@Override
	public void paint(Graphics g) {
		if (needsRefresh) {
			startTime = System.currentTimeMillis();
			// for (View view : views) {
				view.computeView(world.getChunks(), world.getChunkLevel() + 1, debugChunkLevel);
			// }
			BufferedImage image;
			if (drawingImage && views.indexOf(view) == 0) {
				image = ((PlayerView)view).getImageBuffer();
			} else {
				image = view.getDepthBuffer();
			}
			Graphics g2 = image.getGraphics();

			if (displayMode != 0) {
				if (displayMode == 3) {
					drawDebugChunks(g2, debugChunksToDraw);
					drawOctrees(g2, nextDisplayedChunk, lastDisplayedChunk);
				} else if (displayMode != 4) {
					drawDebugChunks(g2, view.getDebugChunksToDraw());
				}
				displayDebug(g2);
			}

			g.drawImage(image, 0, 0, null);
			needsRefresh = false;
		}
	}

	public void displayDebug(Graphics g) {
		// Vector directionV = new Vector(0, 1, 0);
		// directionV.rotate(view.getTheta(), view.getPhi());
		Point viewPoint = view.getViewPoint();
		g.setColor(Color.WHITE);
		g.drawString("(" + (int)viewPoint.getX() + ", " + (int)viewPoint.getY() + ", " + (int)viewPoint.getZ() + ")", 20, 20);
		// g.drawString("(" + directionV.getX() + ", " + directionV.getY() + ", " + directionV.getZ() + ")", 20, 40);
		g.drawString("REFRESH TIME : " + (System.currentTimeMillis() - startTime) + " ms", 20, 60);
		g.drawString("DISPLAYING : " + debugChunksToDraw.size() + " chunks\n", 20, 80);
		g.drawString("CHUNK LEVEL : " + debugChunkLevel, 20, 100);
		g.drawString("DISPLAY MODE : " + displayMode, 20, 120);
		g.drawString("VIEW : " + view, 20, 140);
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
		int[] projA = new int[2];
		int[] projB = new int[2];
		view.compute2DProjectionTriangle(a, projA);
		view.compute2DProjectionTriangle(b, projB);

		if (a.getY() > 0 && b.getY() > 0) {
			g.drawLine(projA[0], projA[1], projB[0], projB[1]);
		}
	}

	/** draws debug options in the list */
	public void drawDebugChunks(Graphics g, ArrayList<Chunk> debugChunksToDraw) {
		g.setColor(Color.WHITE);
		for (Chunk chunk : debugChunksToDraw) {
			drawBoundaries(g, chunk.getPoints());
		}
	}
	
	/** draws an interactive octree structure*/
	public void drawOctrees(Graphics g, Chunk nextDisplayedChunk, Chunk lastDisplayedChunk) {
		drawDebugChunks(g, debugChunksToDraw);
		g.setColor(Color.GREEN);
		drawBoundaries(g, nextDisplayedChunk.getPoints());

		if (lastDisplayedChunk != null) {
			g.setColor(Color.RED);
			drawBoundaries(g, lastDisplayedChunk.getPoints());
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
				debugChunkLevel = (debugChunkLevel + 1)%(world.getChunkLevel() + 1);
			} else if (key == 'h') {
				debugChunkLevel--;
				if (debugChunkLevel == -1) {
					debugChunkLevel = world.getChunkLevel();
				}
			} else if (key == 'u') {
				mouseLocked = !mouseLocked;
			} else if (key == 'i') {
				drawingImage = !drawingImage;
			} else if (key == 'o') {
				displayMode++;
				if (displayMode == 3) {
					debugChunksToDraw.clear();
					lastDisplayedChunk = null;
					for (Object object : world.getChunks().values()) {
						debugChunksToDraw.add((Chunk)object);
					}
					nextDisplayedChunk = debugChunksToDraw.get(0);
				} else if (displayMode == 5) {
					displayMode = 0;
				}
				view.setDisplayMode(displayMode);
			} else if (key == 'p') {
				view = views.get((views.indexOf(view) + 1)%views.size());
			} else if (key == 'n') {
				int index = debugChunksToDraw.indexOf(nextDisplayedChunk) + 1;
				if (index >= debugChunksToDraw.size()) {
					index = 0;
				}
				nextDisplayedChunk = debugChunksToDraw.get(index);
			} else if (key == 'b') {
				lastDisplayedChunk = nextDisplayedChunk;

				debugChunksToDraw.clear();
				for (Object object : lastDisplayedChunk.getSmallerChunks().values()) {
					debugChunksToDraw.add((Chunk)object);
				}				
				nextDisplayedChunk = debugChunksToDraw.get(0);
			} else if (key == 'v') {
				lastDisplayedChunk = null;

				debugChunksToDraw.clear();
				for (Object object : world.getChunks().values()) {
					debugChunksToDraw.add((Chunk)object);
				}
				nextDisplayedChunk = debugChunksToDraw.get(0);
			}

			view.addViewPoint(movement);
			needsRefresh = true;
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
				// for (View view : views) {
					view.addDirection(-(e.getXOnScreen() - centerX) * 2.0 * Math.PI/width, -(e.getYOnScreen() - centerY) * 2.0 * Math.PI/height);
				// }
				robot.mouseMove(centerX, centerY);
				needsRefresh = true;
			}
		}
	}
}
