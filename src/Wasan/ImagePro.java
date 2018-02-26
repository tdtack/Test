package Wasan;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class ImagePro {
	BufferedImage image;// 読込画像

	BufferedImage proImage;// 画像処理用の画像
	BufferedImage geomImage;// 幾何要素のみを検出した画像
	BufferedImage textImage;// 文字のみを検出した画像
	// BufferedImage spaceImage;

	// int i = 33;// 画像番号
	String i = "006";// 画像番号//83,46
	
	String[] tag;// 画像に対するタグ→クラス化する？
	// ？TagGroup tagGroup;
	// 画像ごとにHashMapを持つという構想があっても良い？
	// Stringを持つよりもHashMapを持つ方がいい？
	// あとでタグを変えたいとかなったらHashMapの方が対応させやすい？

	ImagePro() {
		// image = loadImage("dat/input/算額画像" + i + ".png");
		// image = loadImage("dat/input/算額画像" + i + ".png");
		//image = loadImage("dat/images/" + i + ".PNG");
		image = loadImage("dat/images2/" + i + ".PNG");
		//image = loadImage("dat/test_data/" + i + ".PNG");

		// ここでリサイズすべき
		
		// 作れ!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		image = resizeImage(image);

		System.out.println(image.getWidth() + ", " + image.getHeight());

		proImage = processingImage(true);
		geomImage = processingImage(false);
		textImage = processingImage(false);

		tag = new String[0];
	}

	BufferedImage resizeImage(BufferedImage img) {
		int width = img.getWidth();
		int height = img.getHeight();
		
		int size=500;
		int resWidth, resHeight;
		if (width >= height) {
			resWidth = size;
			resHeight = resWidth * height / width;
		} else {
			resHeight = size;
			resWidth = resHeight * width / height;
		}

		BufferedImage resize = new BufferedImage(resWidth, resHeight, img.getType());

		AffineTransform transform = AffineTransform.getScaleInstance((double) resWidth / width,
				(double) resHeight / height);
		AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BICUBIC);
		op.filter(img, resize);

		/*
		 * xform = new AffineTransformOp(
		 * AffineTransform.getScaleInstance((double) new_width / width, (double)
		 * new_height / height), AffineTransformOp.TYPE_BILINEAR); dst = new
		 * BufferedImage(new_width, new_height, src.getType());
		 * xform.filter(src, dst);
		 */

		// . 変換後のバイナリイメージを byte 配列に再格納
		/*
		 * ByteArrayOutputStream baos = new ByteArrayOutputStream();
		 * ImageIO.write(dst, "jpeg", baos); img = baos.toByteArray();
		 */

		return resize;
	}

	BufferedImage loadImage(String name) {
		try {
			BufferedImage bimage = ImageIO.read(new File(name));
			return bimage;
		} catch (IOException e) {
			System.out.println("Error : " + e);
			return null;
		}
	}

	BufferedImage processingImage(boolean type) {// 画像の前処理
		BufferedImage output = image;

		output = binarizeImage(output);

		if (type) {
			output = MorphImage(output);
			// output = cleanImage(output);
		}

		return output;
	}

	BufferedImage binarizeImage(BufferedImage input) {// 画像の二値化
		BufferedImage output = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_USHORT_GRAY);
		for (int y = 0; y < input.getHeight(); y++) {
			for (int x = 0; x < input.getWidth(); x++) {
				int value = (getBrightness(input.getRGB(x, y)) > 128) ? 255 : 0;
				output.setRGB(x, y, rgb(value, value, value));
			}
		}

		return output;
	}

	BufferedImage MorphImage(BufferedImage input) {// 画像の膨張、収縮処理
		BufferedImage output = input;

		int repeat = 3;
		for (int i = 0; i < repeat; i++) {// repeat回の膨張処理
			output = OpenClose(output, true);
		}
		for (int i = 0; i < repeat; i++) {// repeat回の収縮処理
			output = OpenClose(output, false);
		}

		return output;
	}

	BufferedImage OpenClose(BufferedImage input, boolean isOpen) {// 膨張、収縮処理の詳細
		BufferedImage output = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_RGB);

		int d = 1;// 中心のマスからの誤差(通常は1)
		int size = (d * 2 + 1);
		double[] p = new double[size * size];// フィルタの作成(通常は3×3)

		for (int y = 0; y < input.getHeight(); y++) {
			for (int x = 0; x < input.getWidth(); x++) {
				if (x < d || x >= input.getWidth() - d) {
					output.setRGB(x, y, rgb(255, 255, 255));
				} else if (y < d || y >= input.getHeight() - d) {
					output.setRGB(x, y, rgb(255, 255, 255));
				} else {
					for (int dy = -d; dy <= d; dy++) {
						for (int dx = -d; dx <= d; dx++) {
							p[(dy + d) * size + (dx + d)] = input.getRGB(x + dx, y + dy) >> 16 & 0xFF;
						}
					}
					int value = getExt(p, isOpen ? false : true);
					output.setRGB(x, y, rgb(value, value, value));
				}
			}
		}

		return output;
	}

	int getExt(double[] p, boolean isMax) {
		int ext = isMax ? Integer.MIN_VALUE : Integer.MAX_VALUE;
		for (int i = 0; i < p.length; i++) {
			ext = isMax ? Math.max(ext, (int) p[i]) : Math.min(ext, (int) p[i]);
		}
		return ext;
	}

	BufferedImage cleanImage(BufferedImage input) {// 文字と思われる部分の除去(現在は使っていない)
		int d = 10;
		BufferedImage output = input;

		for (int y = d; y < input.getHeight() - d; y++) {
			for (int x = d; x < input.getWidth() - d; x++) {
				int count = 0;
				for (int dy = -d; dy <= d; dy++) {
					for (int dx = -d; dx <= d; dx++) {
						if ((input.getRGB(x + dx, y + dy) >> 16 & 0xFF) < 128) {
							count++;
						}
					}
				}

				int thresh = ((d * 2 + 1) * (d * 2 + 1) - 1);
				if (count >= thresh) {
					for (int dy = -d; dy <= d; dy++) {
						for (int dx = -d; dx <= d; dx++) {
							output.setRGB(x + dx, y + dy, rgb(255, 255, 255));
						}
					}
				}
			}
		}

		return output;
	}

	void removePoint(ArrayList<Point> pList) {// 点要素の除去(文字との分類のため)
		for (int i = 0; i < pList.size(); i++) {
			if ((proImage.getRGB((int) pList.get(i).x, (int) pList.get(i).y) >> 16 & 0xFF) == 0) {
				textImage.setRGB((int) pList.get(i).x, (int) pList.get(i).y, rgb(255, 255, 255));
			}
		}
	}

	void removeLine(ArrayList<Line> lList) {// 線分要素の除去(文字との分類のため)
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				if ((proImage.getRGB(x, y) >> 16 & 0xFF) == 0) {
					for (int i = 0; i < lList.size(); i++) {
						if (Math.abs(lList.get(i).distance(x, y)) < 6) {
							textImage.setRGB(x, y, rgb(255, 255, 255));
							break;
						}
					}
				}
			}
		}
	}

	void removeCircle(ArrayList<Circle> cList) {// 円要素の除去(文字との分類のため)
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				if ((proImage.getRGB(x, y) >> 16 & 0xFF) == 0) {
					for (int i = 0; i < cList.size(); i++) {
						if (Math.abs(cList.get(i).distance(x, y)) < 8) {
							textImage.setRGB(x, y, rgb(255, 255, 255));
							break;
						}
					}
				}
			}
		}
	}

	void removeText() {// 幾何要素と文字の分類
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				if ((textImage.getRGB(x, y) >> 16 & 0xFF) == 0) {
					geomImage.setRGB(x, y, rgb(255, 255, 255));
				}
			}
		}
	}

	public double getBrightness(int c) {
		int R = (c >> 16) & 0xFF;
		int G = (c >> 8) & 0xFF;
		int B = (c >> 0) & 0xFF;

		// return 0.299 * R + 0.587 * G + 0.114 * B;
		return 0.2126 * R + 0.7152 * G + 0.0722 * B;
	}

	public int a(int c) {
		return c >>> 24;
	}

	public int r(int c) {
		return c >> 16 & 0xff;
	}

	public int g(int c) {
		return c >> 8 & 0xff;
	}

	public int b(int c) {
		return c & 0xff;
	}

	public int rgb(int r, int g, int b) {
		return 0xff000000 | r << 16 | g << 8 | b;
	}

	public static int argb(int a, int r, int g, int b) {
		return a << 24 | r << 16 | g << 8 | b;
	}

	String[] append(String[] array, String value) {
		String[] newArray = new String[array.length + 1];
		for (int i = 0; i < array.length; i++) {
			newArray[i] = array[i];
		}
		newArray[array.length] = value;

		return newArray;
	}
}
