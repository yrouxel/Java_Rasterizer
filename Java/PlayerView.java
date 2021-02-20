import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.TreeMap;
import java.awt.Color;

public class PlayerView extends View {
	private BufferedImage imageBuffer;
	private Graphics gImageBuffer;

	public PlayerView(Point viewPoint, int maxChunkLevel, double alphaMax, int width, int height) {
		super(viewPoint, maxChunkLevel, alphaMax, width, height);

		imageBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		gImageBuffer = imageBuffer.getGraphics();
	}

	@Override
	public void drawFunction(Triangle tri, int xMin, int xMax, int yMin, int yMax) {
		float[] colorModifiers = tri.getColorModifiers();
		if (colorModifiers[0] != -1) {
			// BufferedImage texture = tri.getTexture();
			// TexturePoint[] texturePoints = tri.getTexturePoints();

			// double areaTri = edgeFunction(vertices[1][0], vertices[1][1], vertices[2][0], vertices[2][1], vertices[0][0], vertices[0][1]);
			// double areaTextureTri = edgeFunction(texturePoints[1].getX(), texturePoints[1].getY(), texturePoints[2].getX(), texturePoints[2].getY(), texturePoints[0].getX(), texturePoints[0].getY());
			// double areaRatio = areaTextureTri/(2.0*areaTri);
	
			int redTexture   = tri.getDefaultColor().getRed();
			int greenTexture = tri.getDefaultColor().getGreen();
			int blueTexture  = tri.getDefaultColor().getBlue();
	
			int rgb;

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

							rgb = (int)(colorModifiers[0] * (float)redTexture) << 16 | (int)(colorModifiers[1] * (float)greenTexture) << 8 | (int)(colorModifiers[2] * (float)blueTexture);
							imageBuffer.setRGB(x, y, rgb);
						} else if (firstPixelFound) {
							break;
						}
					}
				}
			}
		} else {
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

						} else if (firstPixelFound) {
							break;
						}
					}
				}
			}
		}
	}

	@Override
	public void computeView(TreeMap<Point, Surface> chunks, int originChunkLevel, int debugChunkLevel) {
		gImageBuffer.setColor(Color.BLACK);
		gImageBuffer.fillRect(0, 0, width, height);
		super.computeView(chunks, originChunkLevel, debugChunkLevel);
	}

	//---GETTERS

	public BufferedImage getImageBuffer() {
		return imageBuffer;
	}
}
