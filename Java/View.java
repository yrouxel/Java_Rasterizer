import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeMap;
import java.awt.Graphics;
import java.awt.*;

public abstract class View {
	//provides an ordering without duplicate
	protected final Comparator<Double> doubleComparator = new Comparator<Double>() {
		public int compare(Double d1, Double d2) {	
			if (d1 < d2) {
				return -1;
			} else {
				return 1;
			}
		}
	};

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
	protected int[][] coordDiffs = new int[2][2];
	protected int[] coordYSteps = new int[2];
	protected int[] barycentricCoord = new int[3];
	protected double[][][] multipliedCoord = new double[2][][];
	protected double[] coords = new double[3];
	protected int[] bounds = new int[4];

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

	//reusable objects
	protected int indexNextReusableProjection = 0;
	protected int indexNextReusableMap = 0;
	protected ArrayList<int[]> reusableProjections = new ArrayList<int[]>();
	protected ArrayList<TreeMap<Double, Chunk>> sortedChunksByChunkLevel = new ArrayList<TreeMap<Double, Chunk>>();
	protected LinkedList<TreeMap<Point, int[]>> reusableChunkProjections  = new LinkedList<TreeMap<Point, int[]>>();

	//maps for general purpose
	protected HashMap<Point, int[]> tempPointsInChunk = new HashMap<Point, int[]>(128);
	protected TreeMap<Double, Triangle> trianglesInChunk = new TreeMap<Double, Triangle>(doubleComparator);

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
			sortedChunksByChunkLevel.add(new TreeMap<Double, Chunk>(doubleComparator));
			reusableChunkProjections.add(new TreeMap<Point, int[]>());
		}

		reusableChunkProjections.add(new TreeMap<Point, int[]>());

		for (int i = 0; i < 30; i++) {
			reusableProjections.add(new int[2]);
		}

		addDirection(0, 0);

		for (int i = 0; i < 2; i++) {
			multipliedCoord[i] = new double[3][];
			for (int j = 0; j < 3; j++) {
				multipliedCoord[i][j] = new double[2];
			}
		}
	}

	/** determines recursively which chunks to draw */
	// public void sortChunksAndDraw(TreeMap<Point, Object> chunks, TreeMap<Point, int[]> biggerChunkPoints, int biggerChunkXMin, int originChunkLevel, int debugChunkLevel) {
	public void sortChunksAndDraw(TreeMap<Point, Object> chunks, int biggerChunkXMin, int originChunkLevel, int debugChunkLevel) {
		// TreeMap<Double, Chunk> sortedChunks = new TreeMap<Double, Chunk>(doubleComparator);
		TreeMap<Double, Chunk> sortedChunks = sortedChunksByChunkLevel.get(indexNextReusableMap);
		indexNextReusableMap++;
		// TreeMap<Point, int[]> currentChunkPoints = reusableChunkProjections.removeFirst();
		sortedChunks.clear();
		
		for (Object object : chunks.values()) {
			Chunk chunk = (Chunk)object;
			chunk.computeCenter(replaceablePoint);
			replaceableVector.setVector(replaceablePoint, viewPoint);
			sortedChunks.put(replaceableVector.getSquareNorm(), chunk);
		}

		for (Chunk chunk : sortedChunks.values()) {
			//conditions : not be hidden behind other chunks + be in screen space
			// currentChunkPoints.clear();
			// int currentChunkXMin = isChunkVisible(chunk, biggerChunkXMin, biggerChunkPoints, currentChunkPoints);
			int currentChunkXMin = isChunkVisible(chunk, biggerChunkXMin);
			if (currentChunkXMin != -1) {
				if (chunk.getChunkLevel() == 0) {
					sortTrianglesInChunk(chunk);
					drawTrianglesInChunk();
				} else {
					// sortChunksAndDraw(chunk.getSmallerChunks(), currentChunkPoints, biggerChunkXMin, chunk.getChunkLevel(), debugChunkLevel);
					sortChunksAndDraw(chunk.getSmallerChunks(), currentChunkXMin, chunk.getChunkLevel(), debugChunkLevel);

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

		indexNextReusableMap--;
		// sortedChunksByChunkLevel.add(sortedChunks);
		// reusableChunkProjections.add(currentChunkPoints);
	}

	/** returns a list of visible triangles sorted by distance to camera */
	public void sortTrianglesInChunk(Chunk chunk) {
		tempPointsInChunk.clear();
		trianglesInChunk.clear();
		indexNextReusableProjection = 0;

		for (Object object : chunk.getSmallerChunks().values()) {
			Triangle tri = (Triangle)object;
			tri.getCenterOfGravity(replaceablePoint);
			replaceableVector.setVector(replaceablePoint, viewPoint);

			// Object must be facing towards the camera
			if (tri.getNormal().getScalarProduct(replaceableVector) > 0) {
				// here points 2D projection can be added even if all 3 points aren't visible
				// boolean triangleVisible = true;
				for (Point p : tri.getPoints()) {
					if (!tempPointsInChunk.containsKey(p)) {
						int[] proj = getReusableProjection();
						// int[] proj = new int[2];
						compute2DProjectionTriangle(p, proj);
						// if (proj == null) {
						// 	triangleVisible = false;
						// 	break;
						// }
						tempPointsInChunk.put(p, proj);
						indexNextReusableProjection++;
					}
				}

				// if (triangleVisible) {
					trianglesInChunk.put(replaceableVector.getSquareNorm(), tri);
				// }
			}
		}
	}

	/** returns a box containing the 2D view of the chunk if it is in screen space and contains at least one visible pixel, null otherwise */
	// public int isChunkVisible(Chunk chunk, int biggerChunkXMin, TreeMap<Point, int[]> biggerChunkPoints, TreeMap<Point, int[]> currentChunkPoints) {
	public int isChunkVisible(Chunk chunk, int biggerChunkXMin) {
		bounds[0] = Integer.MAX_VALUE;
		bounds[1] = Integer.MIN_VALUE;
		bounds[2] = Integer.MAX_VALUE;
		bounds[3] = Integer.MIN_VALUE;

		// boolean alreadyComputed = false;
		// for (int i = 0; i < 8; i++) {
		// 	Point p = chunk.getPoints()[i];
		// 	int[] proj = biggerChunkPoints.get(p);
		// 	if (proj == null) {
		// 		//need computation of bounds
		// 		if (!alreadyComputed) {
		// 			alreadyComputed = true;
		// 			prepare2DProjectionChunk(chunk.getCoord(), chunk.getChunkSize());
		// 		}
		// 		proj = new int[2];
		// 		compute2DProjectionChunk(proj, i);
		// 	} 

		// 	bounds[0] = Math.min(bounds[0], proj[0]);
		// 	bounds[1] = Math.max(bounds[1], proj[0]);
		// 	bounds[2] = Math.min(bounds[2], proj[1]);
		// 	bounds[3] = Math.max(bounds[3], proj[1]);

		// 	biggerChunkPoints.putIfAbsent(p, proj);
		// 	currentChunkPoints.put(p, proj);
		// }

		compute2DProjectionChunk(chunk.getCoord(), chunk.getChunkSize());

		//if the chunk is in front of the player and if the box is out of screen space
		if ((bounds[2] += centerY) >= height || (bounds[3] += centerY) < 0 
		|| (bounds[0] += centerX) >= width || (bounds[1] += centerX) < biggerChunkXMin) {
			return -1;
		}

		bounds[2] = Math.max(0, bounds[2]);
		bounds[3] = Math.min(height - 1, bounds[3]);

		if (bounds[2] == bounds[3]) {
			return -1;
		}

		bounds[0] = Math.max(biggerChunkXMin, bounds[0]);
		bounds[1] = Math.min(width - 1, bounds[1]);

		// if at least one pixel is visible (black) in depth buffer
		for (int i = bounds[0]; i < bounds[1]; i++) {
			for (int j = bounds[2]; j < bounds[3]; j++) {
				if (depthBuffer.getRGB(i, j) == -16777216) {
					return i;
				}
			}
		}

		return -1;
	}

	/* returns a useable array for projection from a pool, extends the pool if necessary */
	public int[] getReusableProjection() {
		if (indexNextReusableProjection == reusableProjections.size()) {
			for (int i = 0; i < 5; i++) {
				reusableProjections.add(new int[2]);
			}
		}
		return reusableProjections.get(indexNextReusableProjection);
	}

	public void compute2DProjectionChunk(Point p, double chunkSize) {
		coords[0] = p.getX() - viewPoint.getX();
        coords[1] = p.getY() - viewPoint.getY();
        coords[2] = p.getZ() - viewPoint.getZ();
		
		//compute all multiplied cosinus/sinuses before translation
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 3; j++) {	
				if (j < 2) {
					multipliedCoord[i][j][0] = coords[j] * cosTheta;
					multipliedCoord[i][j][1] = coords[j] * sinTheta;
				} else {
					multipliedCoord[i][j][0] = coords[j] * cosPhi;
					multipliedCoord[i][j][1] = coords[j] * sinPhi;
				}
			}

			if (i == 0) {
				for (int j = 0; j < 3; j++) {
					coords[j] += chunkSize;
				}
			}
		}

		double x, y, yBefore, z;

		//compute x, y, z and projections
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {	
				//x isn't function of z
				x = multipliedCoord[i][0][0] + multipliedCoord[j][1][1];
				yBefore = multipliedCoord[j][1][0] - multipliedCoord[i][0][1];
				for (int k = 0; k < 2; k++) {	
					y = yBefore * cosPhi + multipliedCoord[k][2][1];
					z = multipliedCoord[k][2][0] - yBefore * sinPhi;

					//project
					projection[0] = (int)(x * focalDistance / y);
					projection[1] = - (int)(z * focalDistance / y);

					bounds[0] = Math.min(bounds[0], projection[0]);
					bounds[1] = Math.max(bounds[1], projection[0]);
					bounds[2] = Math.min(bounds[2], projection[1]);
					bounds[3] = Math.max(bounds[3], projection[1]);
				}
			}
		}
	}

	public void prepare2DProjectionChunk(Point p, double chunkSize) {
		coords[0] = p.getX() - viewPoint.getX();
        coords[1] = p.getY() - viewPoint.getY();
        coords[2] = p.getZ() - viewPoint.getZ();
		
		//compute all multiplied cosinus/sinuses before translation
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 3; j++) {	
				if (j < 2) {
					multipliedCoord[i][j][0] = coords[j] * cosTheta;
					multipliedCoord[i][j][1] = coords[j] * sinTheta;
				} else {
					multipliedCoord[i][j][0] = coords[j] * cosPhi;
					multipliedCoord[i][j][1] = coords[j] * sinPhi;
				}
			}

			if (i == 0) {
				for (int j = 0; j < 3; j++) {
					coords[j] += chunkSize;
				}
			}
		}
	}

	public void compute2DProjectionChunk(int[] proj, int index) {
		double x, y, yBefore, z;

		int i = index & 1;
		int j = (index >> 1) & 1;
		int k = (index >> 2) & 1;

		//compute x, y, z and projections
		x = multipliedCoord[i][0][0] + multipliedCoord[j][1][1];
		yBefore = multipliedCoord[j][1][0] - multipliedCoord[i][0][1];
		y = yBefore * cosPhi + multipliedCoord[k][2][1];
		z = multipliedCoord[k][2][0] - yBefore * sinPhi;

		//project
		proj[0] = (int)(x * focalDistance / y);
		proj[1] = - (int)(z * focalDistance / y);
	}

	/** computes the coordinates of the point in the new base, then computes its 2D projection */
	public boolean compute2DProjectionTriangle(Point p, int[] projection) {
		//translation
        double x = p.getX() - viewPoint.getX();
        double y = p.getY() - viewPoint.getY();
        double z = p.getZ() - viewPoint.getZ();

        double xBefore = x;

        //theta rotation
        x = xBefore * cosTheta + y * sinTheta;
        double yBefore = y*cosTheta - xBefore*sinTheta;

        //phi rotation
        y = yBefore*cosPhi + z*sinPhi;
		z = z*cosPhi - yBefore*sinPhi;

		// if (z < cosFieldOfView && x < cosFieldOfView * width) {
		// 	return false;
		// }
		
		//2D Projection
		projection[0] = centerX + (int)(x * focalDistance / y);
		projection[1] = centerY - (int)(z * focalDistance / y);

		return true;
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
				for (int i = 0; i < 2; i++) {
					int iLastVert = (i+2)%3;
					coordDiffs[i][0] = vertices[iLastVert][0] - vertices[i][0];
					coordDiffs[i][1] = vertices[iLastVert][1] - vertices[i][1];
				}
				
				// if triangle contains at least 1 pixel
				int areaTri = coordDiffs[0][0] * coordDiffs[1][1] - coordDiffs[1][0] * coordDiffs[0][1];
				if (areaTri != 0) {
					xMin = Math.max(0, xMin);
					xMax = Math.min(width-1, xMax);
					yMin = Math.max(0, yMin);
					yMax = Math.min(height-1, yMax);

					drawFunction(tri, xMin, xMax, yMin, yMax, areaTri);
				}
			}
		}
	}

	public abstract void drawFunction(Triangle tri, int xMin, int xMax, int yMin, int yMax, int areaTri);

	/** checks if a given point is a triangle by a methode of cross products */
	public boolean computeBarycentricCoords(int ptX, int areaTri){
		for (int i = 0; i < 2; i++) {
			//(x1-x3)(y2-y3) - (x2-x3)(y1-y3)
			barycentricCoord[i] = (ptX - vertices[i][0]) * coordDiffs[i][1] - coordYSteps[i];
			if (barycentricCoord[i] < 0) {
				return false;
			}
		}
		if ((barycentricCoord[2] = areaTri - barycentricCoord[0] - barycentricCoord[1]) < 0) {
			return false;
		}
		return true;
	}

	public void computeView(TreeMap<Point, Object> chunks, int originChunkLevel, int debugChunkLevel) {
		gDepthBuffer.setColor(Color.BLACK);
		gDepthBuffer.fillRect(0, 0, width, height);
		
		debugChunksToDraw.clear();

		// TreeMap<Point, int[]> biggerChunkProjections = reusableChunkProjections.removeFirst();
		// biggerChunkProjections.clear();
		// sortChunksAndDraw(chunks, biggerChunkProjections, 0, originChunkLevel, debugChunkLevel);
		// reusableChunkProjections.add(biggerChunkProjections);
		sortChunksAndDraw(chunks, 0, originChunkLevel, debugChunkLevel);
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
		viewPointPlus.rotate(cosTheta, -sinTheta, cosPhi, -sinPhi);
		viewPoint.add(viewPointPlus);
	}

	//---SETTERS---

	public void setDisplayMode(int displayMode) {
		this.displayMode = displayMode;
	}
}
