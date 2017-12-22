package Wasan;

import java.awt.image.BufferedImage;

public class TextRecognition {
	NewCanvas canvas;
	BufferedImage TextScanImage;
	int w,h;

	TextRecognition(NewCanvas _canvas){
		canvas = _canvas;
		w = canvas.imgPro.image.getWidth();
		h = canvas.imgPro.image.getHeight();
	}

	public void scan1(int ss){
		int scan_size = ss;
		TextScanImage = new BufferedImage(w-scan_size,h-scan_size,BufferedImage.TYPE_INT_RGB);
		for(int y = 0 ; y<h-scan_size; y++){
			for(int x = 0 ; x<w-scan_size; x++){
				int count = 0;
				for(int v = 0; v<scan_size; v++){
					for(int u = 0; u<scan_size; u++){
						// if(canvas.geomImage.getRGB(x+u,
						// y+v)==canvas.rgb(0,0,0)){
						// count ++;
						// }
					}
				}
				if(count >scan_size*scan_size*0.5){
					//TextScanImage.setRGB(x, y, canvas.rgb(0,0,0));
				} else {
					//TextScanImage.setRGB(x, y, canvas.rgb(255,255,255));
				}
			}
		}
	}
}
