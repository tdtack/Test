package Wasan;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

//(主に改良をお願いしたい箇所には★マークをつけました。)
public class Hough {
	// NewCanvas canvas;
	ImagePro imgPro;

	/**
	 *  直線(線分)に関する投票数(int型)
	 */
	int[][] fieldLine;//
	int maxWidth, maxHeight;

	double cos[], sin[];
	int maxRho, maxTheta;

	/**
	 *  円に関する投票数(int型)
	 */
	int[][][] fieldCircle;// 円に関する投票数
	double maxRadius;
	double[][] radius;

	int[][] subArray;
	int[] subsubArray;
	int[] sub2subArray;
	int[] sub3subArray;
	int[] sub4subArray;
	int[] sub5subArray;

	ArrayList<Line>  houghLine;

	class parameters{
		/**
		 * getFieldLine前半アルゴリズム選択<br>
		 * 0: rho の前後のデータを見て，山形になっているかどうかを見る。
		 */
		int gFD1method=0;
		/**
		 * getFieldLine前半パラメータ１<br>
		 * method=0のとき： para1: rhoを動かす幅<br>
		 */
		int gFD1para1=10;
		/**
		 * getFieldLine前半パラメータ２<br>
		 * method=0のとき： para2: 山の高さ（割合）<br>
		 */
		int gFD1para2=2;
		/**
		 * getFieldLine後半アルゴリズム選択<br>
		 * 0: 山を見つけたら長方形に切り取る<br>
		 * 1: 山を見つけたら蝶の形に切り取る
		 */
		int gFD2method=0;
		/**
		 * getFieldLine後半パラメータ１<br>
		 * method=0のとき： para1: 長方形の幅（theta方向）<br>
		 * method=1のとき： para1: theta方向の範囲
		 */
		int gFD2para1=10;
		/**
		 * getFieldLine後半パラメータ２<br>
		 * method=0のとき： para2: 長方形の高さ（rho方向)<br>
		 */
		int gFD2para2=45;
		/**
		 * メソッドrestoreLineの　変数connectの値
		 */
		int restoreLineConnect = 60;
		/**
		 *
		 */
		int fillGapSubArraySize = 7;

		parameters(){
		};
	} ;

	parameters Para;

	Hough() {
		//
	}

	Hough(ImagePro _imgPro) {
		imgPro = _imgPro;

		maxWidth = imgPro.image.getWidth();
		maxHeight = imgPro.image.getHeight();

		maxRho = (int) Math.floor(Math.sqrt(maxWidth * maxWidth + maxHeight * maxHeight));
		maxTheta = 180;
		maxRadius = Math.min(maxWidth, maxHeight) / 2;

		int lengTheta = convTheta(maxTheta);
		int lengRho = convRho(maxRho);

		fieldLine = new int[lengTheta][lengRho];

		cos = new double[lengTheta];
		sin = new double[lengTheta];

		for (int t = 0; t < maxTheta; t ++) {
			cos[convTheta(t)] = Math.cos(Math.PI * t / maxTheta);
			sin[convTheta(t)] = Math.sin(Math.PI * t / maxTheta);
		}

		fieldCircle = new int[(int) maxWidth][(int) maxHeight][(int) maxRadius + 1];
		radius = new double[(int) maxWidth][(int) maxHeight];
		for (int x = 0; x < maxWidth; x++) {
			for (int y = 0; y < maxHeight; y++) {
				radius[x][y] = Math.hypot(x, y);
			}
		}

		houghLine = new ArrayList<Line>();

		Para = new parameters();
	}

	/**
	 * 変数thetaを配列番号へ変換
	 * @param theta
	 * @return 配列番号
	 */
	int convTheta(int theta) {// 実数θを配列番号へ拡張
		assert(0<=theta && theta<maxTheta) : "convTheta: out of bounds error";
		return theta;
	}
	/**
	 * 変数rhoを配列番号へ変換
	 * @param theta
	 * @return 配列番号
	 */
	int convRho(int rho) {// 実数ρを配列番号へ拡張
		assert(-maxRho<=rho && rho<maxRho) : "convRho: out of bounds error";
		return rho + maxRho;
	}


	/**
	 * fieldLine の内容を csvファイルに出力する。
	 * ファイル名はimgPro.iに依存してつける。
	 */
	void exportCSV(int x) {
		try {
			File file = new File("file/fieldLine" + imgPro.i  + "-"+x+".csv");
			FileOutputStream fos = new FileOutputStream(file);
			OutputStreamWriter osw = new OutputStreamWriter(fos, "SJIS");
			BufferedWriter bw = new BufferedWriter(osw);
			PrintWriter pw = new PrintWriter(bw);

			for (int r = -maxRho; r < maxRho; r ++) {
				for (int t = 0; t < maxTheta; t ++) {
					pw.print(fieldLine[convTheta(t)][convRho(r)] + ",");
				}
				pw.println();
			}
			pw.close();
		} catch (Exception e) {
			//
		}
	}
	/**
	 *　線分に関する投票を行う。 NewCanvas.javaで使用する。
	 * @param image
	 */
	void voteFieldLine(BufferedImage image) {//
		for (int x = 0; x < maxWidth; x++) {
			for (int y = 0; y < maxHeight; y++) {
				if ((image.getRGB(x, y) >> 16 & 0xFF) < 128) {
					for (int t = 0; t < maxTheta; t ++) {
						int rho = (int)Math.floor(cos[convTheta(t)] * x + sin[convTheta(t)] * y);
						fieldLine[convTheta(t)][convRho(rho)]++;
					}
				}
			}
		}
		//exportCSV(0);
	}

	int gFLcount=1;

	Line getFieldLine() {// 投票に基づく(θ,ρ)の取得
		int voteNum = 0;

		int theta = 0;
		int rho = 0;

		if(Para.gFD1method==0){
			int Dr = Para.gFD1para1;
			int seads = Para.gFD1para2;
			for (int t = 0; t < maxTheta; t ++) {
				for (int r = -maxRho + Dr; r < maxRho - Dr; r ++) {
					if (voteNum < fieldLine[convTheta(t)][convRho(r)]) {//★★★★★
						
						int t1 = fieldLine[convTheta(t)][convRho(r - Dr)]
								+ fieldLine[convTheta(t)][convRho(r - Dr + 1)]
								+ fieldLine[convTheta(t)][convRho(r - Dr + 2)];
						int t2 = fieldLine[convTheta(t)][convRho(r - 1)]
								+ fieldLine[convTheta(t)][convRho(r)]
								+ fieldLine[convTheta(t)][convRho(r + 1)];
						int t3 = fieldLine[convTheta(t)][convRho(r + Dr)]
								+ fieldLine[convTheta(t)][convRho(r + Dr - 1)]
								+ fieldLine[convTheta(t)][convRho(r + Dr - 2)];

						boolean condition = ((t1*seads < t2) && (t3*seads < t2));
						if (condition) {
							voteNum = fieldLine[convTheta(t)][convRho(r)];
							theta = t;
							rho = r;
						}
					}
				}
			}
		}

		if(Para.gFD2method==0){
			int et = Para.gFD2para1;//20;//長方形を切り取ってよいか。
			int er = Para.gFD2para2;//50;
			for (int dt = -et; dt <= et; dt ++) {
				for (int dr = -er; dr <= er; dr ++) {
					int thetaD = theta + dt;
					int rhoD = rho + dr;

					if (inRange(thetaD, rhoD)) {
						fieldLine[convTheta(thetaD)][convRho(rhoD)] = 0;
					}

					if (theta < et || maxTheta - et < theta) {
						thetaD = (maxTheta - theta) + dt;
						rhoD = -rho + dr;
						if (inRange(thetaD, rhoD)) {
							fieldLine[convTheta(thetaD)][convRho(rhoD)] = 0;
						}
					}
				}
			}
		} else if(Para.gFD2method==1){
			int et = Para.gFD2para1;//20;//長方形を切り取ってよいか。
			double maxT = 0;
			if(theta < 90){
				maxT = Math.max(maxHeight*cos[convTheta(theta)], maxWidth*sin[convTheta(theta)]);
			} else {
				int pi4 = (int)Math.round(Math.atan2(maxHeight, maxWidth)*180/Math.PI);
				maxT = Math.hypot(maxWidth, maxHeight)*sin[theta-pi4];
			}
			for (int dt = -et; dt <= et; dt ++) {
				int thetaD = theta+dt;
				int rhoMin = (int)Math.floor(rho*cos[Math.abs(dt)] - maxT*sin[Math.abs(dt)]);
				int rhoMax = (int)Math.ceil(rho*cos[Math.abs(dt)] + maxT*sin[Math.abs(dt)]);
				for (int rhoD = rhoMin; rhoD <= rhoMax; rhoD ++) {
					if (inRange(thetaD, rhoD)) {
						fieldLine[convTheta(thetaD)][convRho(rhoD)] = 0;
					}

					if (theta < et || maxTheta - et < theta) {
						thetaD = (maxTheta - theta) + dt;
						if (inRange(thetaD, -rhoD)) {
							fieldLine[convTheta(thetaD)][convRho(-rhoD)] = 0;
						}
					}
				}
			}
		}
		//exportCSV(gFLcount);
		//gFLcount ++ ;
		Line thisLine = new Line(theta, rho);
		houghLine.add(thisLine);
		return thisLine;
	}

	/**
	 *　Hough ペアに対して，線分を抽出する。
	 * @param _theta
	 * @param _rho
	 * @return
	 */
	Line restoreLine(double _theta, double _rho) {// 線分の復元
		//System.out.println("(θ,ρ)=(" + _theta + "," + _rho + ")");//★★★

		Point start = new Point(0, 0, -1);// 線分の始点
		Point end = new Point(0, 0, -1);// 線分の終点

		int theta = (int)Math.floor(_theta+0.001);
		int rho = (int)Math.floor(_rho+0.001);


		for(Line line1 : houghLine){
			if(line1.theta == _theta && line1.rho == _rho){
				double rho1 = line1.rho;
				double theta1 = line1.theta;
				for(Line line2 : houghLine){
					double rho2 = line2.rho;
					double theta2 = line2.theta;
					if(theta1 != theta2 && (Math.abs(theta1 - theta2)<=7.01 || Math.abs(theta1 - theta2)>=172.99)){
						double numX = rho1 * Math.sin(Math.toRadians(theta2)) - rho2 * Math.sin(Math.toRadians(theta1));
						double numY = -rho1 * Math.cos(Math.toRadians(theta2)) + rho2 * Math.cos(Math.toRadians(theta1));
						double den = Math.sin(Math.toRadians(theta2-theta1));
						double intersectX = numX / den;
						double intersectY = numY / den;
						if(0 <= intersectX && intersectX < maxWidth && 0<= intersectY && intersectY < maxHeight){
							houghLine.remove(line1);
							return new Line().Normal(start, end, -1);
						}
					}

				}
			}
		}


		boolean condition = (maxTheta / 4 < theta && theta <= 3 * maxTheta / 4);
		int[] data = Bright(condition, theta, rho);

		int div = 1;
		int connect = Para.restoreLineConnect;// 起点から繋がっているピクセル数

		// 線分の始点をint配列から探索。 start(Line型)を得る。
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < connect; j++) {
				if (i + j < data.length && data[i + j] > 0) {
					if (j == connect - 1) {
						int s = i * div;
						start.x = condition ? s : (rho - s * sin[convTheta(theta)]) / cos[convTheta(theta)];
						start.y = condition ? (rho - s * cos[convTheta(theta)]) / sin[convTheta(theta)] : s;
					} else {
						//
					}
				} else {
					break;
				}
			}
			if (start.x > 0 && start.y > 0) {
				break;
			}
		}

		// 線分の終点をint配列から探索。end(Line型)を得る。
		for (int i = data.length - 1; i >= 0; i--) {
			for (int j = 0; j < connect; j++) {
				if (i - j>=0 && data[i - j] > 0) {
					if (j == connect - 1) {
						int e = i * div;
						end.x = condition ? e : (rho - e * sin[convTheta(theta)]) / cos[convTheta(theta)];
						end.y = condition ? (rho - e * cos[convTheta(theta)]) / sin[convTheta(theta)] : e;
					} else {
						//
					}
				} else {
					break;
				}
			}
			if (end.x > 0 && end.y > 0) {
				break;
			}
		}

		//System.out.println("("+start.x+","+start.y+")-("+end.x+","+end.y+")");//★★★

		try {
			File file = new File("file/Array" + imgPro.i  + ".txt");
			FileOutputStream fos = new FileOutputStream(file, true);
			OutputStreamWriter osw = new OutputStreamWriter(fos, "SJIS");
			BufferedWriter bw = new BufferedWriter(osw);
			PrintWriter pw = new PrintWriter(bw);
			pw.println("("+start.x+","+start.y+")-("+end.x+","+end.y+")");
			pw.println("");
			pw.close();
		} catch (Exception e) {
			//
		}


		return new Line().Normal(start, end, -1);
	}

	/**
	 * 線分に関する配列の整理方法
	 * @param isW
	 * <br>true:x軸に投射，false:y軸に投射
	 * @param theta
	 * @param rho
	 * @return
	 */
	int[] Bright(boolean isW, double theta, double rho) {
		if(isW) return BrightX(theta,rho);
		else return BrightY(theta,rho);
	}

	int[] BrightX(double _theta, double _rho) {
		int thetaD=(int)Math.floor(_theta);
		double origRho = _rho;
		int drMax = 6;
		int maxDr = drMax*2+1;
		double[] dr = new double[maxDr];

		for (int i = 0; i < maxDr; i++) {
			dr[i] = 0.5 * (i- drMax);
		}

		int[] Array = new int[maxWidth];// 水平垂直方向の切り替え
		subArray = new int[maxDr][maxWidth];
		subsubArray = new int[maxWidth];
		sub2subArray = new int[maxWidth];
		sub3subArray = new int[maxWidth];
		sub4subArray = new int[maxWidth];
		sub5subArray = new int[maxWidth];
		for (int x = 0; x < maxWidth; x++) {
			Array[x] = 0;
			for (int r = 0; r < maxDr; r++) {
				subArray[r][x] = 0;
			}
			subsubArray[x]=0;
			sub2subArray[x]=0;
			sub3subArray[x]=0;
			sub4subArray[x]=0;
			sub5subArray[x]=0;
		}
		for(int x=0; x< maxWidth; x++){
			for (int r = 0; r < maxDr; r++) {
				double rhoD = origRho + dr[r];
				int y = (int)Math.floor ((rhoD - x * cos[convTheta(thetaD)]) / sin[convTheta(thetaD)]);
				if(0<= y && y <maxHeight){
					if ((imgPro.image.getRGB(x, y) >> 16 & 0xFF) < 128) {
						subArray[r][x] = 1;
					}
				}
			}
		}
		extractSegment(maxDr, maxWidth,_theta, _rho);
		for(int x=0; x< maxWidth; x++){
			for (int r = 0; r < maxDr; r++) {
				if(sub4subArray[x]==9){
					Array[x]=1;
				}
			}
		}
		return Array;
	}

	int[] BrightY(double _theta, double _rho) {
		int thetaD=(int)Math.floor(_theta);
		double origRho = _rho;
		int drMax = 7;
		int maxDr = drMax * 2 + 1;
		double[] dr = new double[maxDr];
		for (int i = 0; i < maxDr; i++) {
			dr[i] = 0.5 * (i- drMax);
		}
		int[] Array = new int[maxHeight];//
		subArray = new int[maxDr][maxHeight];
		subsubArray = new int[maxHeight];
		sub2subArray = new int[maxHeight];
		sub3subArray = new int[maxHeight];
		sub4subArray = new int[maxHeight];
		sub5subArray = new int[maxHeight];
		for (int y = 0; y < maxHeight; y++) {
			Array[y] = 0;
			for (int r = 0; r < maxDr; r++) {
				subArray[r][y] = 0;
			}
			subsubArray[y]=0;
			sub2subArray[y]=0;
			sub3subArray[y]=0;
			sub4subArray[y]=0;
			sub5subArray[y]=0;
		}
		for(int y=0; y< maxHeight; y++){
			for (int r = 0; r < maxDr; r++) {
				double rhoD = origRho + dr[r];
				int x = (int)Math.floor ((rhoD - y * sin[convTheta(thetaD)]) / cos[convTheta(thetaD)]);
				if(0<= x && x <maxWidth){
					if ((imgPro.image.getRGB(x, y) >> 16 & 0xFF) < 128) {
						subArray[r][y] = 1;
					}
				}
			}
		}
		extractSegment(maxDr, maxHeight,_theta,_rho);
		for(int y=0; y< maxHeight; y++){
			for (int r = 0; r < maxDr; r++) {
				if(sub4subArray[y]==9){
					Array[y]=1;
				}
			}
		}
		return Array;
	}


	boolean inRange(double theta, double rho) {
		return (0 <= theta && theta < maxTheta) && (-maxRho <= rho && rho < maxRho);
	}

	/**
	 * 2重数列から線分要素を判別する。
	 * @param _array
	 */
	void extractSegment(int lenR, int lenX, double _theta, double _rho){

		fillGapSubArray(lenR, lenX);
		make_subData(lenR,lenX);
		deleteArcSlash(lenR,lenX);
		exportArrayToTxt(lenR,lenX,_theta,_rho);
	}

	/**
	 * subArrayの隙間があれば埋める。
	 * @param lenR
	 * @param lenX
	 */
	void fillGapSubArray(int lenR, int lenX){
		for (int r = 0; r < lenR; r++) {
			int loopCount=0;
			boolean cont=false;
			do{
				cont=false;
				int cnt0=1;
				int cnt1=-1;
				int cnt2=-1;
				int idx1=-1;
				for(int x=0; x<lenX; x++){
					if(x<lenX-1 && subArray[r][x] == subArray[r][x+1]){
						cnt0++;
					} else {
						if(cnt1>0 && cnt1>0 && cnt1<Para.fillGapSubArraySize && ((cnt0 > cnt1+1 && cnt1+1 < cnt2) || cnt0 > cnt1*2 || cnt1*2 < cnt2)){
							for(int k=idx1; k>idx1-cnt1 && k>=0 ; k--){
								subArray[r][k] = 1-subArray[r][k];
							}
							cont=true;
							break;
						}
						cnt2 = cnt1;
						cnt1 = cnt0;
						cnt0 = 1;
						idx1 = x;
					}
				}
				loopCount++;
			} while (cont && loopCount<100);
		}
	}

	/**
	 * subArrayの断面の位置をsubに，断面の大きさをsub2に，記録する。
	 * @param lenR
	 * @param lenX
	 */
	void make_subData(int lenR, int lenX){
		for(int x=0; x<lenX; x++){
			subsubArray[x]=0;
			sub2subArray[x]=0;
			sub3subArray[x]=0;
			for (int r = 0; r < lenR; r++) {
				subsubArray[x] += (r+1)*subArray[r][x];
				sub2subArray[x]+=subArray[r][x];
			}
			subsubArray[x] = (int)Math.ceil(1.0*subsubArray[x]/sub2subArray[x]);
		}
	}


	/**
	 * subsubArrayの連結成分を見て，断面の位置が大きく変化しているものは消去する。<br>
	 * 継続的に増加、継続的に減少している部分を取り出してその全体からの割合を調べる。
	 * @return
	 */
	boolean deleteArcSlash(int lenR, int lenX){
		boolean arrayOn = false;
		int xOn = -1;// かたまり始まりの座標
		int xIncr = -1; //増加始まりの座標
		int rIncr = 0; // 増加の量
		int dxIncr = 0; // 増加の座標幅

		int xDecr = -1; //減少始まりの座標
		int rDecr = 0; // 減少の量
		int dxDecr = 0; // 減少の座標幅

		int dxStay = 0;//

		int countIncrDecr = 0;// 増加または減少部分のxの個数
		int countSub2=0;// 幅いっぱいの部分のxの個数を数える。
		for(int x=0; x<lenX; x++){
			if(subsubArray[x] != 0){
				if(!arrayOn){//かたまりはじめ
					arrayOn = true;
					xOn = x;
					xIncr = xDecr = -1;
					countSub2=0;
				} else {//かたまり途中
					if(Math.abs(subsubArray[x] - subsubArray[xOn])*10 > (x-xOn)){
						sub3subArray[x] = 1;
					}
					if(sub2subArray[x]>=lenR-1){
						countSub2++;
					}
					if(xIncr == -1 && subsubArray[x-1] == subsubArray[x]-1) {//増加始まり
						xIncr = x;
						sub5subArray[x]=1;
						dxStay = 0;
					} else if(xIncr >0) {
						if(x < lenX-1 && subsubArray[x] != subsubArray[x+1] && subsubArray[x] != subsubArray[x+1]-1) {
							rIncr = subsubArray[x]-subsubArray[xIncr];
							dxIncr = x-xIncr;
							//System.out.println("xIncr, rIncr, dxIncr = "+xIncr+","+rIncr+","+dxIncr);
							if((rIncr>6 && rIncr*20 > dxIncr) || (rIncr>4 && rIncr*15 > dxIncr)) {//断続的に増加している条件
								countIncrDecr += dxIncr;
								//System.out.println("OK: countIncrDecr = "+countIncrDecr);
								for(int xx=xIncr; xx<x; xx++){
									sub5subArray[xx]=1;
								}
							}
							xIncr = -1;
							rIncr = dxIncr = 0;
						} else if(subsubArray[x] == subsubArray[x+1] ){
							dxStay ++;
							if(dxStay>30){// もはや増加とは認められず
								for(int xx=xIncr; xx<x; xx++){
									sub3subArray[xx]=0;
								}
								xIncr = -1;
								rIncr = dxIncr = 0;
							}
						} else if(subsubArray[x] == subsubArray[x+1]-1){
							dxStay = 0;
						}
					}
					if(xDecr == -1 && subsubArray[x-1] == subsubArray[x]+1) {//減少始まり
						xDecr = x;
						sub5subArray[x]=1;
						dxStay = 0;
					} else if(xDecr >0) {
						if(x < lenX-1 && subsubArray[x] != subsubArray[x+1] && subsubArray[x] != subsubArray[x+1]+1) {
							rDecr = subsubArray[xDecr]-subsubArray[x];
							dxDecr = x-xDecr;
							//System.out.println("xDecr, rDecr, dxDecr = "+xDecr+","+rDecr+","+dxDecr);
							if((rDecr>6  && rDecr*20 > dxDecr) || (rDecr>4  && rDecr*15 > dxDecr)) {//断続的に減少している条件
								countIncrDecr += dxDecr;
								//System.out.println("OK: countIncrDecr = "+countIncrDecr);
								for(int xx=xDecr; xx<x; xx++){
									sub5subArray[xx]=1;
								}
							}
							xDecr = -1;
							rDecr = dxDecr = 0;
						} else if(subsubArray[x] == subsubArray[x+1] ){
							dxStay ++;
							if(dxStay>30){// もはや減少とは認められず
								for(int xx=xDecr; xx<x; xx++){
									sub3subArray[xx]=0;
								}
								xDecr = -1;
								rDecr = dxDecr = 0;
							}
						} else if(subsubArray[x] == subsubArray[x+1]+1){
							dxStay = 0;
						}
					}
				}
			} else {
				if(arrayOn){// かたまり終わり
					if(xIncr >0) {
							rIncr = subsubArray[x]-subsubArray[xIncr];
							dxIncr = x-xIncr;
							//System.out.println("xIncr, rIncr, dxIncr = "+xIncr+","+rIncr+","+dxIncr);
							if((rIncr>6 && rIncr*20 > dxIncr) || (rIncr>4 && rIncr*15 > dxIncr)) {//断続的に増加している条件
								countIncrDecr += dxIncr;
								//System.out.println("OK: countIncrDecr = "+countIncrDecr);
								for(int xx=xIncr; xx<x; xx++){
									sub5subArray[xx]=1;
								}
							}
							xIncr = -1;
							rIncr = dxIncr = 0;
					}
					if(xDecr >0) {
							rDecr = subsubArray[xDecr]-subsubArray[x];
							dxDecr = x-xDecr;
							//System.out.println("xDecr, rDecr, dxDecr = "+xDecr+","+rDecr+","+dxDecr);
							if((rDecr>6  && rDecr*20 > dxDecr) || (rDecr>4  && rDecr*15 > dxDecr)) {//断続的に増加している条件
								countIncrDecr += dxDecr;
								//System.out.println("OK: countIncrDecr = "+countIncrDecr);
								for(int xx=xDecr; xx<x; xx++){
									sub5subArray[xx]=1;
								}
							}
							xDecr = -1;
							rDecr = dxDecr = 0;
					}
					int sum=0;
					for(int xx=xOn; xx<x; xx++){
						sum += sub3subArray[xx];
					}
					if(x-xOn<sum*2 || (x-xOn < 50 && x-xOn < countSub2 * 2) || x-xOn < countIncrDecr*2){
						for(int xx=xOn; xx<x; xx++){
							sub4subArray[xx]=1;
						}
					} else {
						for(int xx=xOn; xx<x; xx++){
							sub4subArray[xx]=9;
						}
					}
				}
				arrayOn = false;
			}
		}
		return false;
	}

	String[] subsub={"0","1","2","3","4","5","6","7","8","9","a","b","c","d","e","f","g","h",};

	void exportArrayToTxt(int lenR, int lenX, double _theta, double _rho){
		try {
			File file = new File("file/Array" + imgPro.i  + ".txt");
			FileOutputStream fos = new FileOutputStream(file, true);
			OutputStreamWriter osw = new OutputStreamWriter(fos, "SJIS");
			BufferedWriter bw = new BufferedWriter(osw);
			PrintWriter pw = new PrintWriter(bw);

			pw.println("-----------------------theta = "+_theta+", rho = "+_rho);
			for (int r = 0; r < lenR; r++) {
				for(int x = 0; x < lenX; x++){
					pw.print(subArray[r][x]);
				}
				pw.println();
			}
			for(int x = 0; x < lenX; x++){
				pw.print(subsub[subsubArray[x]]);
			}
			pw.println();
			for(int x = 0; x < lenX; x++){
				pw.print(subsub[sub2subArray[x]]);
			}
			pw.println();
			for(int x = 0; x < lenX; x++){
				pw.print(sub3subArray[x]);
			}
			pw.println();
			for(int x = 0; x < lenX; x++){
				pw.print(sub4subArray[x]);
			}
			pw.println();
			for(int x = 0; x < lenX; x++){
				pw.print(sub5subArray[x]);
			}
			pw.println();
			pw.close();
		} catch (Exception e) {
			//
		}


	}



	void voteFieldCircle(BufferedImage image) {// 円に関する投票
		int d = 2;
		for (int x = 0; x < maxWidth; x += d) {
			for (int y = 0; y < maxHeight; y += d) {
				if ((image.getRGB(x, y) >> 16 & 0xFF) < 128) {
					for (int cx = 0; cx < maxWidth; cx += d) {
						for (int cy = 0; cy < maxHeight; cy += d) {
							double cr = radius[Math.abs(cx - x)][Math.abs(cy - y)];
							// int cr = (int)radius[Math.abs(cx - x)][Math.abs(cy - y)];
							if (cr < maxRadius) {// maxRadius-1でもよし
								fieldCircle[cx][cy][(int) cr]++;
								// fieldCircle[cx][cy][cr]++;
							}
						}
					}
				}
			}
		}
	}

	Circle getFieldCircle() {// 投票に基づく(x,y,r)の取得
		int voteNum = 0;
		int X = 0;
		int Y = 0;
		int R = 0;

		int d = 2;
		//int thresh = 200;

		int Dr = 10;//Para.circleDr=10;
		int seads = 3;//Para.circleSeads=3;
		int minR = 30;//Para.circleMinR = 30
		double vr=0.75;//Para.circleVr=0.75;
		for (int x = 0; x < maxWidth; x += d) {
			for (int y = 0; y < maxHeight; y += d) {
				for (int r = minR+Dr; r < maxRadius-Dr; r ++) {
					if (voteNum < fieldCircle[x][y][r]) {

						int c1 = fieldCircle[x][y][r-Dr]+fieldCircle[x][y][r-Dr+1]+fieldCircle[x][y][r-Dr+2];
						int c2 = fieldCircle[x][y][r-1]+fieldCircle[x][y][r]+fieldCircle[x][y][r+1];
						int c3 = fieldCircle[x][y][r+Dr-2]+fieldCircle[x][y][r+Dr-1]+fieldCircle[x][y][r+Dr];
						boolean condition = (c1*seads<c2 && c3*seads<c2 && fieldCircle[x][y][r]>r*vr);
						if (condition) {
							X = x;
							Y = y;
							R = r;
							voteNum = fieldCircle[x][y][r];
						} else {
							//break;
						}
					}
				}
			}
		}
		//System.out.println("(x="+X+", y="+Y+", r="+R+") fC="+(1.0*voteNum/R));//★★★

		int pxE = 20;
		int pyE = 20;
		int radiusE = 20;
		for (int dx = -pxE; dx <= pxE; dx++) {
			for (int dy = -pyE; dy <= pyE; dy++) {
				for (int dr = -radiusE; dr <= radiusE; dr++) {
					double pxD = X + dx;
					double pyD = Y + dy;
					double radiusD = R + dr;
					if (inRange(pxD, pyD, radiusD)) {
						fieldCircle[(int) pxD][(int) pyD][(int) radiusD] = 0;
					}
				}
			}
		}

		return new Circle(new Point(X, Y, -1), R);
	}

	Circle restoreCircle(Point p, double r) {// 円の復元
		//TODO return時に半円情報が付けられるように
		//Circle c=new Circle().Normal(p, new Point(p.x + r / Math.sqrt(2), p.y + r / Math.sqrt(2), -1), -1);
		//c.boolean=...
		return new Circle().Normal(p, new Point(p.x + r / Math.sqrt(2), p.y + r / Math.sqrt(2), -1), -1);
	}

	boolean inRange(double x, double y, double r) {
		return (0 <= x && x < maxWidth) && (0 <= y && y < maxHeight) && (0 < r && r < maxRadius);
	}
}


