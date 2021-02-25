import java.util.ArrayList;
import java.util.TreeMap;

public class LightView extends View {
	private int rgb;
	private boolean declining;
	private ArrayList<Triangle> lastVisibleTriangles = new ArrayList<Triangle>();
	private ArrayList<Triangle> nextVisibleTriangles = new ArrayList<Triangle>();

	public LightView(Point viewPoint, int maxChunkLevel, double alphaMax, int width, int height, int rgb, boolean declining) {
		super(viewPoint, maxChunkLevel, alphaMax, width, height);
		this.rgb = rgb;
		this.declining = declining;
	}

	@Override
	//iterate through it, compare with depth buffer color, check if point is inside the triangle, paint it
	public void drawFunction(Triangle tri, int xMin, int xMax, int yMin, int yMax, int areaTri) {
		boolean triangleVisible = false;
		boolean firstPixelFound;
		for (int y = yMin; y < yMax; y++) {
			for (int i = 0; i < 3; i++) {
				coordYSteps[i] = coordDiffs[i][0] * (y - vertices[i][1]);
			}
			firstPixelFound = false;
			for (int x = xMin; x < xMax; x++) {
				if (depthBuffer.getRGB(x, y) == -16777216) {
					computeBarycentricCoords(x);
					if (barycentricCoord[0] != -1) {
						depthBuffer.setRGB(x, y, -1);
						firstPixelFound = true;
						triangleVisible = true;
					} else if (firstPixelFound) {
						break;
					}
				}
			}
		}

		if (triangleVisible) {
			nextVisibleTriangles.add(tri);
		}		
	}

	public void updateLight(boolean simpleRotation) {
		if (simpleRotation) {
			for (Triangle tri : nextVisibleTriangles) {
				if (!lastVisibleTriangles.contains(tri)) {
					tri.getVisibleLights().put(this, null);
					tri.setNeedsUpdating();
				} else {
					lastVisibleTriangles.remove(tri);
				}
			}
	
			for (Triangle tri : lastVisibleTriangles) {
				if (!nextVisibleTriangles.contains(tri)) {
					tri.getVisibleLights().remove(this);
					lastVisibleTriangles.remove(tri);
					tri.setNeedsUpdating();
				}
			}
	
			lastVisibleTriangles.addAll(nextVisibleTriangles);
			nextVisibleTriangles.clear();
		} else {
			for (Triangle tri : lastVisibleTriangles) {
				tri.getVisibleLights().remove(this);
				tri.setNeedsUpdating();
			}
			for (Triangle tri : nextVisibleTriangles) {
				tri.getVisibleLights().put(this, null);
				tri.setNeedsUpdating();
			}
			lastVisibleTriangles.clear();
			lastVisibleTriangles.addAll(nextVisibleTriangles);
			nextVisibleTriangles.clear();
		}
	}

	@Override
	public void computeView(TreeMap<Point, Object> chunks, int originChunkLevel, int debugChunkLevel) {
		super.computeView(chunks, originChunkLevel, debugChunkLevel);
		updateLight(false);
	}

	//---GETTERS---

	public int getRGB() {
		return rgb;
	}

	public boolean isDeclining() {
		return declining;
	}
}
