import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeMap;
import java.awt.Graphics;
import java.awt.*;

public abstract class View {
	//dimension variables
	protected int width;
	protected int height;
	protected int centerX;
	protected int centerY;

	//images and their graphics
	protected BufferedImage depthBuffer;
	protected Graphics gDepthBuffer;

	//pre initiated variables shade computing and bounds computing
	protected Point replaceablePoint = new Point();
	protected Vector replaceableVector = new Vector();

	//pixels to draw
	protected int[] projection = new int[2];
	protected int[][] vertices = new int[3][2];
	protected int[][] coordDiffs = new int[3][2];
	protected int[] coordYSteps = new int[3];
	protected int[] barycentricCoord = new int[3];

	//angles
	protected double theta = 0;
	protected double phi = 0;

	//calculated sinuses
	protected double cosTheta;
	protected double sinTheta;
	protected double cosPhi;
	protected double sinPhi;

	//2D projection variables
	protected double fieldOfView;
	protected double cosFieldOfView;
	protected double focalDistance;

	//maps for general purpose
	protected HashMap<Point, int[]> tempPointsInChunk = new HashMap<Point, int[]>(128);
	protected TreeMap<Double, Triangle> trianglesInChunk = new TreeMap<Double, Triangle>(Collections.reverseOrder());
	protected ArrayList<TreeMap<Double, Chunk>> sortedChunksByChunkLevel = new ArrayList<TreeMap<Double, Chunk>>();

	//view point
	protected Point viewPoint;

	//debug
	private int displayMode = 0;
	private ArrayList<Chunk> debugChunksToDraw = new ArrayList<Chunk>();

	public View(Point viewPoint, int maxChunkLevel, double fieldOfView, int width, int height) {
		this.viewPoint = viewPoint;
		this.width = width;
		this.height = height;
		this.fieldOfView = fieldOfView;
		cosFieldOfView = Math.cos(fieldOfView);

		focalDistance = height / (2 * Math.tan(fieldOfView / 2.0));
		centerX = width/2;
		centerY = height/2;

		depthBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		gDepthBuffer = depthBuffer.getGraphics();

		for (int i = 0; i < maxChunkLevel + 1; i++) {
			sortedChunksByChunkLevel.add(i, new TreeMap<Double, Chunk>());
		}

		addDirection(0, 0);
	}

	/** determines recursively which chunks to draw */
	public void sortChunksAndDraw(TreeMap<Point, Surface> chunks, int originChunkLevel, int debugChunkLevel) {
		// TreeMap<Double, Chunk> sortedChunks = sortedChunksByChunkLevel.get(originChunkLevel - 1);
		TreeMap<Double, Chunk> sortedChunks = new TreeMap<Double, Chunk>();

		sortedChunks.clear();
		
		for (Surface surface : chunks.values()) {
			Chunk chunk = (Chunk)surface;
			replaceableVector.setVector(chunk.getCenter(), viewPoint);
			sortedChunks.put(replaceableVector.getNorm(), chunk);
		}

		for (Chunk chunk : sortedChunks.values()) {
			//conditions : not be hidden behind other chunks + be in screen space
			if (isChunkVisible(chunk)) {
				if (chunk.getChunkLevel() == 0) {
					sortTrianglesInChunk(chunk);
					drawTrianglesInChunk();
				} else {
					sortChunksAndDraw(chunk.getSmallerChunks(), chunk.getChunkLevel(), debugChunkLevel);
				}

				//debug modes
				if (displayMode == 1) {
					if (chunk.getChunkLevel() == debugChunkLevel) {
						debugChunksToDraw.add(chunk);
					}
				} else if (displayMode == 2) {
					if (originChunkLevel > debugChunkLevel && chunk.getChunkLevel() <= debugChunkLevel) {
						debugChunksToDraw.add(chunk);
					}	
				}
			}
		}
	}

	/** returns a list of visible triangles sorted by distance to camera */
	public void sortTrianglesInChunk(Chunk chunk) {
		tempPointsInChunk.clear();
		trianglesInChunk.clear();

		for (Surface surface : chunk.getSmallerChunks().values()) {
			Triangle tri = (Triangle)surface;
			replaceableVector.setVector(tri.getCenterOfGravity(), viewPoint);

			// Surface must be facing towards the camera
			if (tri.getNormal().getScalarProduct(replaceableVector) > 0) {
				trianglesInChunk.put(replaceableVector.getNorm(), tri);

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
					replaceablePoint.replace(pt.getX() + i * chunk.getChunkSize(), pt.getY() + j * chunk.getChunkSize(), pt.getZ() + k * chunk.getChunkSize());
					compute2DProjection(replaceablePoint);

					xMin = Math.min(xMin, projection[0]);
					xMax = Math.max(xMax, projection[0]);
					yMin = Math.min(yMin, projection[1]);
					yMax = Math.max(yMax, projection[1]);
				}
			}
		}

		//if the box is out of screen space
		if (xMax < 0 || xMin >= width || yMax < 0 || yMin >= height) {
			return false;
		}

		xMin = Math.max(0, xMin);
		xMax = Math.min((int)width-1, xMax);
		yMin = Math.max(0, yMin);
		yMax = Math.min((int)height-1, yMax);

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
        double x = p.getX() - viewPoint.getX();
        double y = p.getY() - viewPoint.getY();
        double z = p.getZ() - viewPoint.getZ();

		// Vector vec = new Vector(p, viewPoint);

		// if ((vec.getScalarProduct(directionV) / vec.getNorm()) < cosfieldOfView) {
		// 	return;
		// }

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

        double x = p.getX() - viewPoint.getX();
        double y = p.getY() - viewPoint.getY();
        double z = p.getZ() - viewPoint.getZ();

		// Vector vec = new Vector(p, viewPoint);

		// if ((vec.getScalarProduct(directionV) / vec.getNorm()) < cosFieldOfView) {
		// 	return null;
		// }

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

	/** draws the triangle found in trianglesInChunk with clipping and depthbuffer drawing */
	public void drawTrianglesInChunk() {
		for (Triangle tri : trianglesInChunk.values()) {
			int xMin = Integer.MAX_VALUE;
			int xMax = Integer.MIN_VALUE;
			int yMin = Integer.MAX_VALUE;
			int yMax = Integer.MIN_VALUE;
			
			//compute bounding box
			for (int i = 0; i < 3; i++) {
				int iLastVert = (i+2)%3;
				vertices[iLastVert] = tempPointsInChunk.get(tri.getPoints()[i]);
		
				xMin = Math.min(xMin, vertices[iLastVert][0]);
				xMax = Math.max(xMax, vertices[iLastVert][0]);
				yMin = Math.min(yMin, vertices[iLastVert][1]);
				yMax = Math.max(yMax, vertices[iLastVert][1]);
			}

			//if box is at least partially inside screen space clip it
			if (xMax >= 0 && xMin < width && yMax >= 0 && yMin < height) {
				xMin = Math.max(0, xMin);
				xMax = Math.min((int)width-1, xMax);
				yMin = Math.max(0, yMin);
				yMax = Math.min((int)height-1, yMax);

				for (int i = 0; i < 3; i++) {
					int iLastVert = (i+2)%3;
					coordDiffs[i][0] = vertices[iLastVert][0] - vertices[i][0];
					coordDiffs[i][1] = vertices[iLastVert][1] - vertices[i][1];
				}

				drawFunction(tri, xMin, xMax, yMin, yMax);
			}
		}
	}

	public abstract void drawFunction(Triangle tri, int xMin, int xMax, int yMin, int yMax);

	/** checks if a given point is a triangle by a methode of cross products */
	public void computeBarycentricCoords(int ptX){
		for (int i = 0; i < 3; i++) {
			//(x1-x3)(y2-y3) - (x2-x3)(y1-y3)
			barycentricCoord[i] = (ptX - vertices[i][0]) * coordDiffs[i][1] - coordYSteps[i];
			if (barycentricCoord[i] < 0) {
				barycentricCoord[0] = -1;
				return;
			}
		}
	}

	public void computeView(TreeMap<Point, Surface> chunks, int originChunkLevel, int debugChunkLevel) {
		gDepthBuffer.setColor(Color.BLACK);
		gDepthBuffer.fillRect(0, 0, width, height);
		
		debugChunksToDraw.clear();
		sortChunksAndDraw(chunks, originChunkLevel, debugChunkLevel);
	}

	//---GETTERS---

	public BufferedImage getDepthBuffer() {
		return depthBuffer;
	}

	public Point getViewPoint() {
		return viewPoint;
	}

	public double getTheta() {
		return theta;
	}

	public double getPhi() {
		return phi;
	}

	public ArrayList<Chunk> getDebugChunksToDraw() {
		return debugChunksToDraw;
	}

	//---ADDERS---

	public void addDirection(double thetaPlus, double phiPlus) {
		theta += thetaPlus;
		phi += phiPlus;

		cosTheta = Math.cos(theta);
		sinTheta = Math.sin(theta);
		cosPhi = Math.cos(phi);
		sinPhi = Math.sin(phi);	
	}

	public void addViewPoint(Point viewPointPlus) {
		viewPointPlus.rotate(theta, phi);
		viewPoint.add(viewPointPlus);
	}

	//---SETTERS---

	public void setDisplayMode(int displayMode) {
		this.displayMode = displayMode;
	}
}
