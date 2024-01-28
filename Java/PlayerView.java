import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.TreeMap;
import java.awt.Color;

public class PlayerView extends View {
	private BufferedImage imageBuffer;
	private Graphics gImageBuffer;
	private BufferedImage imageExtendedBuffer;
	private Graphics gImageExtendedBuffer;

	private int imageWidth;
	private int imageHeight;

	private int resolution;
	private int resolutionSquare;

	public PlayerView(Point viewPoint, int maxChunkLevel, double alphaMax, int width, int height, int resolution) {
		// super(viewPoint, maxChunkLevel, alphaMax, width, height);
		super(viewPoint, maxChunkLevel, alphaMax, width * resolution, height * resolution);

		this.resolution = resolution;
		resolutionSquare = resolution * resolution;

		imageWidth = width;
		imageHeight = height;

		imageExtendedBuffer = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB);
		gImageExtendedBuffer = imageExtendedBuffer.getGraphics();
		imageBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		gImageBuffer = imageBuffer.getGraphics();


	}

	@Override
	public void drawFunction(Triangle tri, int xMin, int xMax, int yMin, int yMax, int areaTri) {
		float[] colorModifiers = tri.getColorModifiers();
		if (colorModifiers[0] != -1) {
			BufferedImage texture = tri.getTexture();
			TexturePoint[] texturePoints = tri.getTexturePoints();
	
			int redTexture   = tri.getDefaultColor().getRed();
			int greenTexture = tri.getDefaultColor().getGreen();
			int blueTexture  = tri.getDefaultColor().getBlue();
	
			int rgb;
			double textX, textY;

			boolean firstPixelFound;
			for (int y = yMin; y < yMax; y++) {
				for (int i = 0; i < 2; i++) {
					coordYSteps[i] = coordDiffs[i][0] * (y - vertices[i][1]);
				}
				firstPixelFound = false;
				for (int x = xMin; x < xMax; x++) {
					if (computeBarycentricCoords(x, areaTri)) {
						firstPixelFound = true;
						if (depthBuffer.getRGB(x, y) == -16777216) {
							depthBuffer.setRGB(x, y, -1);

							// textX = 0;
							// textY = 0;
							// for (int i = 0; i < 3; i++) {
							// 	textX += texturePoints[i].getX() * barycentricCoord[i];
							// 	textY += texturePoints[i].getY() * barycentricCoord[i];
							// }

							// rgb = texture.getRGB((int)(textX / areaTri), (int)(textY / areaTri));
							// redTexture   = (rgb >> 16) & 0x000000FF;
							// blueTexture  = (rgb >> 8 ) & 0x000000FF;
							// greenTexture = (rgb      ) & 0x000000FF;

							//hologramme
							rgb = (int)((1.0 - colorModifiers[0]) * (float)redTexture) << 16 | (int)((1.0 - colorModifiers[1]) * (float)greenTexture) << 8 | (int)((1.0 - colorModifiers[2]) * (float)blueTexture);
							//normal
							// rgb = (int)(colorModifiers[0] * (float)redTexture) << 16 | (int)(colorModifiers[1] * (float)greenTexture) << 8 | (int)(colorModifiers[2] * (float)blueTexture);
							// imageExtendedBuffer.setRGB(x, y, rgb);
							imageBuffer.setRGB(x, y, rgb);
						}
					} else if (firstPixelFound) {
						break;
					}
				}
			}
		} else {
			boolean firstPixelFound;
			for (int y = yMin; y < yMax; y++) {
				for (int i = 0; i < 2; i++) {
					coordYSteps[i] = coordDiffs[i][0] * (y - vertices[i][1]);
				}
				firstPixelFound = false;
				for (int x = xMin; x < xMax; x++) {
					if (computeBarycentricCoords(x, areaTri)) {
						firstPixelFound = true;
						if (depthBuffer.getRGB(x, y) == -16777216) {
							depthBuffer.setRGB(x, y, -1);
						}
					} else if (firstPixelFound) {
						break;
					}
				}
			}
		}
	}

	
	/** lowers down resolution to screen size to smoothen edges */
	public void downSampleImage() {
		int totalRed;
		int totalGreen;
		int totalBlue;
		int rgb;
		for (int x = 0; x < imageWidth; x++) {
			for (int y = 0; y < imageHeight; y++) {
				totalRed = 0;
				totalGreen = 0;
				totalBlue = 0;
				for (int i = 0; i < resolution; i++) {
					for (int j = 0; j < resolution; j++) {
						rgb = imageExtendedBuffer.getRGB(x * resolution + i, y * resolution + j);
						totalRed   += (rgb >> 16) & 0x000000FF;
						totalBlue  += (rgb >> 8 ) & 0x000000FF;
						totalGreen += (rgb      ) & 0x000000FF;
					}
				}
				totalRed   /= resolutionSquare;
				totalBlue  /= resolutionSquare;
				totalGreen /= resolutionSquare;

				rgb = totalRed << 16 | totalGreen << 8 | totalBlue;
				imageBuffer.setRGB(x, y, rgb);
			}
		}
	}

	@Override
	public void computeView(TreeMap<Point, Object> chunks, int originChunkLevel, int debugChunkLevel) {
		// gImageExtendedBuffer.setColor(Color.BLACK);
		// gImageExtendedBuffer.fillRect(0, 0, width, height);
		gImageBuffer.setColor(Color.BLACK);
		gImageBuffer.fillRect(0, 0, width, height);
		super.computeView(chunks, originChunkLevel, debugChunkLevel);
		// downSampleImage();
	}

	//---GETTERS

	public BufferedImage getImageBuffer() {
		return imageBuffer;
	}
}
