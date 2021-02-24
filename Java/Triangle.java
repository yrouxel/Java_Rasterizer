import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.awt.Color;
import java.awt.image.BufferedImage;

public class Triangle extends Surface{
	private Point points[] = new Point[3];
	private Vector normal;

	private BufferedImage texture;
	private TexturePoint texturePoints[];
	private float[] colorModifiers = new float[3];
	private boolean needsUpdating = true;
	private Color defaultColor;

	private HashMap<LightView, Integer> visibleLights = new HashMap<LightView, Integer>();

	public Triangle(Point a, Point b, Point c, Color defaultColor) {
		this.defaultColor = defaultColor;
		points[0] = a;
		points[1] = b;
		points[2] = c;
		normal = new Vector(points[1], points[0]).getCrossProduct(new Vector(points[2], points[1]));
		normal.normalize();
	}

	public Triangle(Point a, Point b, Point c) {
		this(a, b, c, Color.GRAY);
	}

	public void addTextures(TexturePoint a, TexturePoint b, TexturePoint c, BufferedImage texture) {
		this.texture = texture;
		texturePoints = new TexturePoint[3];
		texturePoints[0] = a;
		texturePoints[1] = b;
		texturePoints[2] = c;
	}

	public boolean contains(Point p) {
		for (Point pt : points) {
			if (pt.equals(p)) {
				return true;
			}
		}
		return false;
	}

	public Boolean shareEdge(Triangle triangle) {
		int commonPoints = 0;
		for (int i = 0; i < points.length; i++) {
			for (int j = 0; j < triangle.getPoints().length; j++) {
				if (points[i].equals(triangle.getPoints()[j])) {
					commonPoints++;
				}
			}
		}
		return commonPoints == 2;
	}

	public void replacePoint(Point a, Point b) {
		for (int i = 0; i < 3; i++) {
			if (points[i].equals(a)) {
				points[i].setPoint(b);
				break;
			}
		}
	}

	public void computeColorModifier() {
		float totalRedLight   = 0;
		float totalGreenLight = 0;
		float totalBlueLight  = 0;

		int redLight   = 0;
		int greenLight = 0;
		int blueLight  = 0;

		float max = 255;

		for (Map.Entry<LightView, Integer> entry : visibleLights.entrySet()) {
			if (entry.getValue() == null) {
				LightView lightView = entry.getKey();
				Vector triangleToLight = new Vector(getCenterOfGravity(), lightView.getViewPoint());
				double triangleToLightNorm = triangleToLight.getNorm();
				double shade = normal.getScalarProduct(triangleToLight) / (normal.getNorm() * triangleToLightNorm);
				if (lightView.isDeclining()) {
					shade /= triangleToLightNorm;
				}

				int rgb = lightView.getRGB();

				redLight   = (int)(shade*((rgb >> 16) & 0x000000FF));
				greenLight = (int)(shade*((rgb >> 8 ) & 0x000000FF));
				blueLight  = (int)(shade*((rgb      ) & 0x000000FF));

				rgb = redLight << 16 | greenLight << 8 | blueLight;

				entry.setValue(rgb);
			} else {
				redLight   = (entry.getValue() >> 16) & 0x000000FF;
				greenLight = (entry.getValue() >> 8 ) & 0x000000FF;
				blueLight  = (entry.getValue()      ) & 0x000000FF;
			}
			totalRedLight   += redLight;
			totalGreenLight += greenLight;
			totalBlueLight  += blueLight;
		}
		max = Math.max(max, totalRedLight);
		max = Math.max(max, totalGreenLight);
		max = Math.max(max, totalBlueLight);

		colorModifiers[0] = totalRedLight / max;
		colorModifiers[1] = totalGreenLight / max;
		colorModifiers[2] = totalBlueLight / max;

		needsUpdating = false;

		for (float colorModifier : colorModifiers) {
			if (colorModifier != 0) {
				return;
			}	
		}
		colorModifiers[0] = -1;
	}

	//---GETTERS---

	public Vector getNormal() {
		return normal;
	}

	public double getTotalSurface() {
		return normal.getNorm() / 2;
	}

	public Point getCenterOfGravity() {
		Point g = new Point();
		g.add(points[0]);
		g.add(points[1]);
		g.add(points[2]);
		g.multiply(1.0/3.0);
		return g;
	}

	public void getCenterOfGravity(Point g) {
		g.setPoint(points[0]);
		g.add(points[1]);
		g.add(points[2]);
		g.multiply(1.0/3.0);
	}

	public Point[] getPoints() {
		return points;
	}

	public TexturePoint[] getTexturePoints() {
		return texturePoints;
	}

	public Color getDefaultColor() {
		return defaultColor;
	}

	public float[] getColorModifiers() {
		if (needsUpdating) {
			computeColorModifier();
		}
		return colorModifiers;
	}

	public BufferedImage getTexture() {
		return texture;
	}

	public HashMap<LightView, Integer> getVisibleLights() {
		return visibleLights;
	}

	//---SETTERS---

	public void setNeedsUpdating() {
		needsUpdating = true;
	}

	public void setDefaultColor(Color defaultColor) {
		this.defaultColor = defaultColor;
	}

	@Override
	public String toString() {
		return "Triangle [points=" + Arrays.toString(points) + "]";
	}
}
