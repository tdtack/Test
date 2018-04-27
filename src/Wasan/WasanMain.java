package Wasan;

import java.applet.Applet;
import java.io.IOException;

public class WasanMain extends Applet {// extends Applet
	public static void main(String[] args) throws IOException {
		NewFrame frame = new NewFrame();// フレーム作成
		NewCanvas canvas = new NewCanvas();// キャンバス作成

		frame.add(canvas);// フレームにキャンバス追加
		frame.addWindowListener(new NewAdapter());// アダプタ追加
		// frame.setUndecorated(true);

		int canvasWidth = canvas.imgPro.image.getWidth() + 10;
		int canvasHeight = canvas.imgPro.image.getHeight() + 50;
		// frame.setSize(canvasWidth * 2, canvasHeight);
		frame.setSize(canvasWidth * 2, canvasHeight);
		frame.setVisible(true);

		System.out.println();
		System.out.println("complete");

		// System.exit(0);
	}
}
