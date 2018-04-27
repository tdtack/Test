package Wasan;
//Aspose.OCR for Javaの利用?

import java.awt.AWTEvent;

//機械学習はDeepLearning4j?

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import javax.imageio.ImageIO;

public class NewCanvas extends Canvas implements MouseListener, MouseMotionListener {// implementsの追加
	ImagePro imgPro;// 画像処理に関する変数
	Hough hough;// Hough変換に関する変数
	Scan scan;// Hough変換に基づいた検出に関する変数

	ArrayList<Point> detectPoint;
	ArrayList<Line> detectLine;
	ArrayList<Circle> detectCircle;
	ArrayList<Module> detectModule;

	int mouseX, mouseY;

	NewCanvas() {
		addMouseListener(this);
		addMouseMotionListener(this);

		imgPro = new ImagePro();
		hough = new Hough(imgPro);
		scan = new Scan(hough);

		// 円に関する投票、検出、幾何要素の除去(文字との分類のため)
		hough.voteFieldCircle(imgPro.textImage);
		scan.scanCircle();
		imgPro.removeCircle(scan.getCircle);

		// 直線に関する投票、検出、幾何要素の除去(文字との分類のため)
		hough.voteFieldLine(imgPro.textImage);
		scan.scanLine();
		imgPro.removeLine(scan.getLine);

		// ★★★ここから

		// 点の検出、幾何要素の除去(文字との分類のため)
		scan.scanPoint();
		imgPro.removePoint(scan.getPoint);

		// ★Module定義(degree算出に近い?)
		// ここで端点などの補正処理を行う?
		scan.scanModule(scan.getPoint, scan.getLine, scan.getCircle);// RelatedTotalが算出
		// ★★★ここまででデータの不具合があると思われる

		// 幾何要素と文字の分類
		imgPro.removeText();

		// printResult(scan);// 幾何要素の関連性を一旦表示する

		// ここで三角形とか四角形の図形認識できる？
		// 三角形、四角形と区分けして考える？

		// ArrayListへの追加
		detectPoint = new ArrayList<Point>();
		detectLine = new ArrayList<Line>();
		detectCircle = new ArrayList<Circle>();
		detectModule = new ArrayList<Module>();

		for (int i = 0; i < scan.getModule.size(); i++) {// scan.getModule.length
			if (scan.getModule.get(i).p != null) {// scan.getModule[i].p
				detectPoint.add(scan.getModule.get(i).p);// scan.getModule[i].p
			} else if (scan.getModule.get(i).l != null) {// scan.getModule[i].l
				detectLine.add(scan.getModule.get(i).l);// scan.getModule[i].l
			} else if (scan.getModule.get(i).c != null) {// scan.getModule[i].c
				detectCircle.add(scan.getModule.get(i).c);// scan.getModule[i].c
			}
			detectModule.add(scan.getModule.get(i));// scan.getModule[i]
		}

		// ★★★//任意の1点に対してPoint[]型でreturnしたい
		// ここで取得できた点同士の関連は「線で繋がっている」ということが重要→Point型での管理がしたい？
		// 取り敢えず、点と点の関連付けは以下

		// for (int i = 0; i < detectModule.size(); i++) {
		// Module pMod = detectModule.get(i);
		// if (pMod.p != null) {// Moduleの内、点を取得
		// for (int j = 0; j < pMod.relLine.size(); j++) {
		// Module lMod = getModule(pMod.relLine.get(j));//
		// detectModule.get(i).relLine[j]
		// for (int k = 0; k < lMod.relPoint.size(); k++) {
		// if (pMod != getModule(lMod.relPoint.get(k))) {
		// pMod.relModule.add(getModule(lMod.relPoint.get(k)));//
		// (Module[])append(pMod.relModule,getModule(lMod.relPoint[k]));
		// }
		// }
		// }
		// }
		// }

		// for (int i = 0; i < detectModule.size(); i++) {
		// if (detectModule.get(i).relModule.size() > 0) {
		// Collections.sort(detectModule.get(i).relModule, new
		// ModuleComparator());// detectModule.get(i).relModule.sort(c);
		// }
		// }

		// データ取得確認
		for (int i = 0; i < detectModule.size(); i++) {
			Module pMod = detectModule.get(i);
			try {
				// System.out.print("p[" + pMod.p.getIndex(detectPoint) + "]
				// connects to");
				for (int j = 0; j < pMod.relModule.size(); j++) {// このmoduleは点
					Module pMod2 = pMod.relModule.get(j);// relModuleは今のところ点に該当するものだけ
					try {
						System.out.print("p[" + pMod2.p.getIndex(detectPoint) + "],");
					} catch (Exception e) {
						//
					}
				}
				// System.out.println();
			} catch (Exception e) {
				//
			}
		}
		// System.out.println();

		// ★★★三角形認識
		Polygon[] tri = new Polygon[0];
		for (int i = 0; i < detectModule.size(); i++) {
			Module m = detectModule.get(i);
			try {// if (m.p != null)
				Point[] v = getVertex(m);// mに繋がっている点のモジュールのList
				for (int j = 0; j < v.length; j++) {
					for (int k = j + 1; k < v.length; k++) {
						if (relatePoint(v[j], v[k])) {
							int[] num = { m.p.getIndex(detectPoint), v[j].getIndex(detectPoint),
									v[k].getIndex(detectPoint) };
							Arrays.sort(num);// インデックスを昇順に並び替える

							Point[] p = new Point[num.length];// num.length=3のはず
							for (int l = 0; l < p.length; l++) {
								p[l] = detectPoint.get(num[l]);
							}

							// 2個の条件を1つのboolean関数にしても良い？
							boolean check = true;
							for (int l = 0; l < tri.length; l++) {
								if ((tri[l].p[0] == p[0] && (tri[l].p[1] == p[1]) && (tri[l].p[2] == p[2]))) {
									check = false;
								}
							}

							if (check && !collinear(tri, p[0], p[1], p[2])) {// 論理的な三角形（グラフ理論的には3角形としてよい）
								tri = new Polygon().append(tri, new Polygon(p[0], p[1], p[2]));// この時点で辺も決定したい
								Polygon t = tri[tri.length - 1];
								t.l = t.getLine(detectLine);// ★★構成する辺を取得
							}
						}
					}
				}
			} catch (Exception e) {
				//
			}
		}

		/*
		 * for (int i = 0; i < tri.length; i++) {// ★★★円に接する三角形の点や辺の情報 //
		 * tri[i].pとtri[i].lに情報は入っている for (int j = 0; j < tri[i].p.length; j++)
		 * { System.out.print("P[" + tri[i].p[j].getIndex(detectPoint) + "], ");
		 * } System.out.print("- "); for (int j = 0; j < tri[i].l.length; j++) {
		 * System.out.print("L[" + tri[i].l[j].getIndex(detectLine) + "], "); }
		 * System.out.println(); }
		 */

		// ★★★四角形認識
		Polygon[] quad = new Polygon[0];
		for (int i = 0; i < detectModule.size(); i++) {
			for (int j = i + 1; j < detectModule.size(); j++) {// 異なる2点の発見
				Module m1 = detectModule.get(i);
				Module m2 = detectModule.get(j);
				try {// if (m1.p != null && m2.p != null)
					Point[] v = sharePoints(getVertex(m1), getVertex(m2));
					// if (v.length == 2) {// 3以上の場合も//より細かな条件の検討が必要
					for (int k = 0; k < v.length; k++) {
						for (int l = k + 1; l < v.length; l++) {
							int[] asc = { m1.p.getIndex(detectPoint), m2.p.getIndex(detectPoint),
									v[k].getIndex(detectPoint), v[l].getIndex(detectPoint) };
							int[] rot = { m1.p.getIndex(detectPoint), v[k].getIndex(detectPoint),
									m2.p.getIndex(detectPoint), v[l].getIndex(detectPoint) };
							Arrays.sort(asc);

							Point[][] p = new Point[2][asc.length];
							for (int m = 0; m < asc.length; m++) {
								p[0][m] = detectPoint.get(asc[m]);
								p[1][m] = detectPoint.get(rot[m]);
							}

							boolean check = true;
							for (int m = 0; m < quad.length; m++) {
								// quad[m].p[0]自身も並び替えが必要
								int[] num = { quad[m].p[0].getIndex(detectPoint), quad[m].p[1].getIndex(detectPoint),
										quad[m].p[2].getIndex(detectPoint), quad[m].p[3].getIndex(detectPoint) };
								Arrays.sort(num);

								boolean test = (detectPoint.get(num[0]) == p[0][0]
										&& (detectPoint.get(num[1]) == p[0][1]) && (detectPoint.get(num[2]) == p[0][2]))
										&& (detectPoint.get(num[3]) == p[0][3]);
								if (test) {
									check = false;
								}
							}

							if (check && !collinear(quad, p[0][0], p[0][1], p[0][2], p[0][3])) {
								quad = new Polygon().append(quad, new Polygon(p[1][0], p[1][1], p[1][2], p[1][3]));// 順に入ってはいる？
								Polygon q = quad[quad.length - 1];
								q.l = q.getLine(detectLine);// ★★
							}
						}
					}

					// }
				} catch (Exception e) {
					//
				}
			}
		}
		
		//★★★n角形認識

		for (int i = 0; i < quad.length; i++) {// ★★★円に接する三角形の点や辺の情報
												// //tri[i].pとtri[i].lに情報は入っている
			for (int j = 0; j < quad[i].p.length; j++) {
				// System.out.print("P[" + quad[i].p[j].getIndex(detectPoint) +
				// "], ");
			}
			// System.out.print("- ");
			for (int j = 0; j < quad[i].l.length; j++) {
				// System.out.print("L[" + quad[i].l[j].getIndex(detectLine) +
				// "], ");
			}
			// System.out.println();
		}

		if (tri.length > 0) {
			System.out.println("三角形 × " + tri.length);

			ArrayList<String> type = new ArrayList<String>();
			for (int i = 0; i < tri.length; i++) {
				for (int j = 0; j < tri[i].p.length; j++) {
					// System.out.print("P[" + tri[i].p[j].getIndex(detectPoint)
					// + "], ");
				}
				if (tri[i].checkTriangle() != "三角形") {
					boolean check = true;
					for (int j = 0; j < type.size(); j++) {
						if (type.get(j) == tri[i].checkTriangle()) {
							check = false;
						}
					}
					if (check) {
						type.add(tri[i].checkTriangle());
					}
				}
			}

			for (int i = 0; i < type.size(); i++) {
				// System.out.println(type.get(i));
			}
		}

		// System.out.println();

		if (quad.length > 0) {
			System.out.println("四角形 × " + quad.length);

			ArrayList<String> type = new ArrayList<String>();
			for (int i = 0; i < quad.length; i++) {
				for (int j = 0; j < quad[i].p.length; j++) {
					// System.out.print("P[" +
					// quad[i].p[j].getIndex(detectPoint) + "], ");
				}
				if (quad[i].checkQuadrangle() != "四角形") {
					boolean check = true;
					for (int j = 0; j < type.size(); j++) {
						if (type.get(j) == quad[i].checkQuadrangle()) {
							check = false;
						}
					}

					if (check) {
						type.add(quad[i].checkQuadrangle());
					}
				}
			}

			for (int i = 0; i < type.size(); i++) {
				System.out.println(type.get(i));
			}
		}

		System.out.println();

		if (detectCircle.size() > 0) {
			System.out.println("円 × " + detectCircle.size());
		}

		System.out.println();

		// for (int i = 0; i < tri.length; i++) {// 全ての3角形
		// System.out.println(tri.length);
		for (int j = 0; j < detectCircle.size(); j++) {// 全ての円
			int count = 0;
			for (int i = 0; i < tri.length; i++) {// 全ての3角形
				if (tri[i].tangentGeo(detectCircle.get(j))) {// true
					System.out.print("C[" + detectCircle.get(j).getIndex(detectCircle) + "]は三角形");
					for (int k = 0; k < tri[i].p.length; k++) {
						System.out.print("P[" + tri[i].p[k].getIndex(detectPoint) + "]");
					}

					count++;
					if (count <= 1) {
						// System.out.println("に接する");
					} else {
						// System.out.print("に接する");
					}

					System.out.print("に接する");
				}
			}
			System.out.println();
		}
		// 直線の接し方で出力させるか？

		// 端点情報の
		for (int i = 0; i < tri.length; i++) {//
			for (int j = 0; j < quad.length; j++) {//
				boolean flag1 = true;
				for (int l = 0; l < quad[j].p.length; l++) {
					boolean flag2 = false;
					for (int k = 0; k < tri[i].l.length; k++) {
						if (scan.isNeighbor(quad[j].p[l], tri[i].l[k])) {
							flag2 = true;
						} else {
							// 継続
						}
					}
					if (!flag2) {
						flag1 = false;
						break;
					}
				}
				if (flag1) {
					// System.out.println(i + "," + j);
				}
			}
		}

		// ★★★
		// 他のタグをつけたい
		// ①四角形に内接する円
		// ②多角形同士の内接?
		for (int j = 0; j < detectCircle.size(); j++) {// 全ての円
			int count = 0;
			for (int i = 0; i < quad.length; i++) {// 全ての3角形
				if (quad[i].tangentGeo(detectCircle.get(j))) {// true
					System.out.print("C[" + detectCircle.get(j).getIndex(detectCircle) + "]は四角形");
					for (int k = 0; k < quad[i].p.length; k++) {
						System.out.print("P[" + quad[i].p[k].getIndex(detectPoint) + "]");
					}
					System.out.print("に接する");
				}
			}
			System.out.println();
		}

		// ここら辺から要素同士の関連性を出力したい

		// for (int i = 0; i < tri.length; i++) {
		// if (tri[i].checkTriangle() != "三角形") {
		// System.out.println(tri[i].checkTriangle() + " : " + "p[" +
		// tri[i].p[0].getIndex(detectPoint) + "] p["
		// + tri[i].p[1].getIndex(detectPoint) + "] p[" +
		// tri[i].p[2].getIndex(detectPoint) + "]");
		// }
		// }
		// System.out.println();
		//
		// for (int i = 0; i < quad.length; i++) {
		// if (quad[i].checkQuadrangle() != "四角形") {
		// System.out.println(quad[i].checkQuadrangle() + " : " + "p[" +
		// quad[i].p[0].getIndex(detectPoint)
		// + "] p[" + quad[i].p[1].getIndex(detectPoint) + "] p[" +
		// quad[i].p[2].getIndex(detectPoint)
		// + "] p[" + quad[i].p[3].getIndex(detectPoint) + "]");
		// }
		// }
		// System.out.println();
		//
		// for (int i = 0; i < detectModule.size(); i++) {
		// if (detectModule.get(i).c != null) {
		// System.out.println("円 : " + "c[" +
		// detectModule.get(i).c.getIndex(detectCircle) + "]");
		// }
		// }
		// System.out.println();

		// printResult();

		// ここで四角形などのタグ付けに関する処理？
		// scan.tagImage(detectPoint, detectLine, detectCircle);
		// for (int i = 0; i < imgPro.tag.length; i++) {
		// System.out.println("OK");
		// }

		// Robot robot=null;
		try {
			// robot=new Robot();
			// BufferedImage image=robot.createScreenCapture(new
			// Rectangle(getX(),getY(),getWidth(),getHeight()));
			// ImageIO.write(imgPro.textImage, "png", new File("dat/output/文字画像"
			// + imgPro.i + ".png"));
			ImageIO.write(imgPro.geomImage, "png", new File("dat/output/幾何画像" + imgPro.i + ".png"));
			// ImageIO.write(image, "png", new File("dat/output/認識結果" + imgPro.i
			// + ".png"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			// File file = new File("dat/vector/vector.csv");
			// FileWriter filewriter = new FileWriter("dat/vector/vector.csv",
			// false);
			// PrintWriter p = new PrintWriter(new BufferedWriter(filewriter));

			File file = new File("dat/vector/vector.csv");
			FileWriter filewriter = new FileWriter(file, true);
			PrintWriter printwriter = new PrintWriter(new BufferedWriter(filewriter));

			printwriter.write("Name");
			printwriter.write("Num of C");
			printwriter.write("Num of L");

			printwriter.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		try {

			// File file = new File("dat/vector/vector3.txt");
			// FileWriter filewriter = new FileWriter(file, true);
			//
			// filewriter.write(imgPro.i + ".PNG,");
			//
			// filewriter.write("三角形" + (tri.length > 0 ? "●" : "○") + ",");
			// filewriter.write("三角形" + tri.length + ",");
			// boolean[] hasTri = new boolean[4];
			// for (int i = 0; i < tri.length; i++) {
			// hasTri[i] = false;
			// }
			// for (int i = 0; i < tri.length; i++) {
			// switch (tri[i].checkTriangle()) {
			// case "正三角形":
			// hasTri[0] = true;
			// break;
			// case "直角三角形":
			// hasTri[1] = true;
			// break;
			// case "二等辺三角形":
			// hasTri[2] = true;
			// break;
			// default:
			// break;
			// }
			// }
			// filewriter.write("正三角" + (hasTri[0] ? "●" : "○") + ",");
			// filewriter.write("直角三角" + (hasTri[1] ? "●" : "○") + ",");
			// filewriter.write("二等辺" + (hasTri[2] ? "●" : "○") + ",");
			// filewriter.write("\r\n");
			//
			// filewriter.write(" " + ",");
			// filewriter.write("四角形" + (quad.length > 0 ? "●" : "○") + ",");
			// filewriter.write("四角形" + quad.length + ",");
			// boolean[] hasQuad = new boolean[4];
			// for (int i = 0; i < quad.length; i++) {
			// hasQuad[i] = false;
			// }
			// for (int i = 0; i < quad.length; i++) {
			// switch (quad[i].checkQuadrangle()) {
			// case "正方形":
			// hasQuad[0] = true;
			// break;
			// case "長方形":
			// hasQuad[1] = true;
			// break;
			// case "菱形":
			// hasQuad[2] = true;
			// break;
			// case "等脚台形":
			// hasQuad[3] = true;
			// break;
			// default:
			// break;
			// }
			// }
			// filewriter.write("正方形" + (hasQuad[0] ? "●" : "○") + ",");
			// filewriter.write("長方形" + (hasQuad[1] ? "●" : "○") + ",");
			// filewriter.write("菱形" + (hasQuad[2] ? "●" : "○") + ",");
			// filewriter.write("等脚台形" + (hasQuad[3] ? "●" : "○") + ",");
			// filewriter.write("\r\n");
			//
			// filewriter.write(" " + ",");
			// filewriter.write("円" + (detectCircle.size() > 0 ? "●" : "○") +
			// ",");
			// filewriter.write("円" + detectCircle.size() + ",");
			//
			// filewriter.write("\r\n");
			//
			// filewriter.close();

		} catch (Exception e) {
			// File file = new File("dat/vector/vector.txt");
			// FileWriter filewriter = new FileWriter(file, true);
			//
			// filewriter.write(imgPro.i + ".PNG,");
			//
			// for (int i = 0; i < 5; i++) {
			// filewriter.write("null" + ",");
			// }
			// filewriter.write("\r\n");
			//
			// filewriter.write(" " + ",");
			// for (int i = 0; i < 6; i++) {
			// filewriter.write("null" + ",");
			// }
			// filewriter.write("\r\n");
			//
			// filewriter.write(" " + ",");
			// for (int i = 0; i < 2; i++) {
			// filewriter.write("null" + ",");
			// }
			//
			// filewriter.write("\r\n");
			//
			// filewriter.close();
			// e.printStackTrace();
			System.out.println(e);
		}

		// printResult();

		enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK);
	}

	class ModuleComparator implements Comparator<Module> {

		@Override
		public int compare(Module m1, Module m2) {
			try {
				int idx1 = m1.p.getIndex(detectPoint);
				int idx2 = m2.p.getIndex(detectPoint);
				return idx1 < idx2 ? -1 : 1;
			} catch (Exception e) {
				return 0;
			}
		}
	}

	boolean collinear(Polygon[] poly, Point... p) {// 3点以上が同一直線状にあるかどうかを判定
		// relPointとpの内、大きい方をn、小さい方をmとすると、nCm通り？
		// (relPointもpも昇順であることから全通り調べる必要はない)
		// relPointとpのサイズが一致するときと一致しない時の場合分けが必要

		for (int i = 0; i < scan.getLine.size(); i++) {
			Module m = getModule(scan.getLine.get(i));// 1本の直線

			int count = 0;
			for (int j = 0; j < m.relPoint.size(); j++) {// relPointが4で
				for (int k = 0; k < p.length; k++) {// pが3の時
					if (m.relPoint.get(j) == p[k]) {
						count++;
					}
				}
			}

			// if (m.relPoint.size() != p.length) {
			// for (int j = 0; j < m.relPoint.size(); j++) {// relPointが4で
			// for (int k = 0; k < p.length; k++) {// pが3の時
			// if (m.relPoint.get(j) == p[k]) {
			// count++;
			// }
			// }
			// }
			// } else {// 1通り
			// for (int j = 0; j < m.relPoint.size(); j++) {// relPointが4で
			// for (int k = 0; k < p.length; k++) {// pが3の時
			// if (m.relPoint.get(j) == p[k]) {
			// count++;
			// }
			// }
			// }
			// }

			if (count >= 3) {// count==3
				return true;
			} else {
				//
			}
		}

		return false;
	}

	boolean collinear2(Point... p) {// 3点以上が同一直線状にあるかどうかを判定
		if (p.length == 3) {
			double a = p[0].y - p[1].y;
			double b = p[1].x - p[0].x;
			double c = p[0].x * p[1].y - p[0].y * p[1].x;

			return (a * p[2].x + b * p[2].y + c) < 200 ? false : true;
		}
		return false;
	}

	boolean relatePoint(Point p1, Point p2) {// 三角形判定に利用
		Module m = getModule(p1);
		// Module m2 = getModule(p2);
		// System.out.println("size:"+getModule(p1).relPoint.size());
		for (int i = 0; i < m.relPoint.size(); i++) {
			if (m.relPoint.get(i) == p2) {
				return true;
			}
		}
		return false;
	}

	Point[] getVertex(Module m) {// 関連性のある点のModuleのリストを返す
		// Module[] mList = new Module[0];
		// for (int i = 0; i < m.relModule.size(); i++) {
		// mList = (Module[]) append(mList, m.relModule.get(i));
		// }
		// return mList;

		Point[] pList = new Point[0];
		// System.out.println(m.relPoint.size());
		for (int i = 0; i < m.relPoint.size(); i++) {
			pList = (Point[]) append(pList, m.relPoint.get(i));
		}
		return pList;
	}

	Point[] sharePoints(Point[] list1, Point[] list2) {// 共有点を探すメソッド
		Point[] pList = new Point[0];

		for (int i = 0; i < list1.length; i++) {
			for (int j = 0; j < list2.length; j++) {
				if (list1[i] == list2[j]) {
					pList = (Point[]) append(pList, list1[i]);
				}
			}
		}

		if (pList.length != 2) {
			// return new Module[0];
		}
		return pList;
	}

	Point[] append(Point[] array, Point value) {
		Point[] newArray = new Point[array.length + 1];
		for (int i = 0; i < array.length; i++) {
			newArray[i] = array[i];
		}
		newArray[array.length] = value;

		return newArray;
	}

	Line[] append(Line[] array, Line value) {
		Line[] newArray = new Line[array.length + 1];
		for (int i = 0; i < array.length; i++) {
			newArray[i] = array[i];
		}
		newArray[array.length] = value;

		return newArray;
	}

	Module[] append(Module[] array, Module value) {
		Module[] newArray = new Module[array.length + 1];
		for (int i = 0; i < array.length; i++) {
			newArray[i] = array[i];
		}
		newArray[array.length] = value;

		return newArray;
	}

	Module getModule(Object o) {// ★★★
		for (int i = 0; i < scan.getModule.size(); i++) {
			boolean condition = false;
			if (o instanceof Point) {
				condition = (o == scan.getModule.get(i).p);
			} else if (o instanceof Line) {
				condition = (o == scan.getModule.get(i).l);
			} else if (o instanceof Circle) {
				condition = (o == scan.getModule.get(i).c);
			}
			if (condition) {
				return scan.getModule.get(i);
			}
		}
		return null;
	}

	void printResult(Scan s) {
		for (int i = 0; i < s.getModule.size(); i++) {
			Module m = s.getModule.get(i);// Moduleを取得

			// そのModuleが点であるか、線であるか、円であるか
			if (m.p != null) {
				System.out.print("p[" + m.p.getIndex(s.getPoint) + "] → ");
			} else if (m.l != null) {
				System.out.print("l[" + m.l.getIndex(s.getLine) + "] → ");
			} else if (m.c != null) {
				System.out.print("c[" + m.c.getIndex(s.getCircle) + "] → ");
			}

			// 関連した要素を表示する
			for (int j = 0; j < m.relPoint.size(); j++) {
				System.out.print("p[" + m.relPoint.get(j).getIndex(s.getPoint) + "], ");
			}
			for (int j = 0; j < m.relLine.size(); j++) {
				System.out.print("l[" + m.relLine.get(j).getIndex(s.getLine) + "], ");
			}
			for (int j = 0; j < m.relCircle.size(); j++) {
				System.out.print("c[" + m.relCircle.get(j).getIndex(s.getCircle) + "], ");
			}

			// System.out.print("RelatedTotal=" + RelatedTotal);
			System.out.println();
		}
	}

	void printResult() {
		// for (int i = 0; i < detectPoint.size(); i++) {
		// Point p=detectPoint.get(i);
		// System.out.print("p[" + p.getIndex(detectPoint)+ "] → ");
		//
		// for (int j = 0; j < p.relPoint.size(); j++) {
		// System.out.print("p[" + p.relPoint.get(j).getIndex(detectPoint) +
		// "]");
		// }
		// for (int j = 0; j < p.relLine.size(); j++) {
		// System.out.print("l[" + p.relLine.get(j).getIndex(detectLine) + "]");
		// }
		// for (int j = 0; j < p.relCircle.size(); j++) {
		// System.out.print("c[" + p.relCircle.get(j).getIndex(detectCircle) +
		// "]");
		// }
		// System.out.println();
		// }
		//
		// for (int i = 0; i < detectLine.size(); i++) {
		// Line l=detectLine.get(i);
		// System.out.print("l[" + l.getIndex(detectLine) + "] → ");
		// for (int j = 0; j < l.relPoint.size(); j++) {
		// System.out.print("p[" + l.relPoint.get(j).getIndex(detectPoint) +
		// "]");
		// }
		// for (int j = 0; j < l.relLine.size(); j++) {
		// System.out.print("l[" + l.relLine.get(j).getIndex(detectLine) + "]");
		// }
		// for (int j = 0; j < l.relCircle.size(); j++) {
		// System.out.print("c[" + l.relCircle.get(j).getIndex(detectCircle) +
		// "]");
		// }
		// System.out.println();
		// }
		//
		// for (int i = 0; i < detectCircle.size(); i++) {
		// Circle c=detectCircle.get(i);
		// System.out.print("c[" + c.getIndex(detectCircle) + "] → ");
		// for (int j = 0; j < c.relPoint.size(); j++) {
		// System.out.print("p[" + c.relPoint.get(j).getIndex(detectPoint) +
		// "]");
		// }
		// for (int j = 0; j < c.relLine.size(); j++) {
		// System.out.print("l[" + c.relLine.get(j).getIndex(detectLine) + "]");
		// }
		// for (int j = 0; j < c.relCircle.size(); j++) {
		// System.out.print("c[" + c.relCircle.get(j).getIndex(detectCircle) +
		// "]");
		// }
		// System.out.println();
		// }

		for (int i = 0; i < detectModule.size(); i++) {
			int RelatedTotal = detectModule.get(i).relPoint.size() + detectModule.get(i).relLine.size()
					+ detectModule.get(i).relCircle.size();
			if (detectModule.get(i).p != null) {
				System.out.print("p[" + detectModule.get(i).p.getIndex(detectPoint) + "] → ");
			} else if (detectModule.get(i).l != null) {
				System.out.print("l[" + detectModule.get(i).l.getIndex(detectLine) + "] → ");
			} else if (detectModule.get(i).c != null) {
				System.out.print("c[" + detectModule.get(i).c.getIndex(detectCircle) + "] → ");
			}

			for (int j = 0; j < detectModule.get(i).relPoint.size(); j++) {
				System.out.print("p[" + detectModule.get(i).relPoint.get(j).getIndex(detectPoint) + "], ");
			}
			for (int j = 0; j < detectModule.get(i).relLine.size(); j++) {
				System.out.print("l[" + detectModule.get(i).relLine.get(j).getIndex(detectLine) + "], ");
			}
			for (int j = 0; j < detectModule.get(i).relCircle.size(); j++) {
				System.out.print("c[" + detectModule.get(i).relCircle.get(j).getIndex(detectCircle) + "], ");
			}

			// System.out.print("RelatedTotal=" + RelatedTotal);
			System.out.println();
		}
	}

	// タグ付けの結果をcsvやtxtファイルで書き出せるとよい？

	public void paint(Graphics g) {// void setup
		int imageWidth = imgPro.image.getWidth();
		int imageHeight = imgPro.image.getHeight();

		g.drawImage(imgPro.textImage, 0, 0, imageWidth, imageHeight, this);
		// g.drawImage(null, 0, 0, imageWidth, imageHeight, this);
		g.drawImage(imgPro.geomImage, imageWidth, 0, imageWidth, imageHeight, this);
	}

	public void update(Graphics g) {// void draw (repaintで自動呼び出し)
		// 検出した幾何要素の描画
		// int imageWidth = imgPro.image.getWidth();
		// int imageHeight = imgPro.image.getHeight();
		//
		// g.drawImage(imgPro.textImage, 0, 0, imageWidth, imageHeight, this);
		// g.drawImage(imgPro.geomImage, imageWidth, 0, imageWidth, imageHeight,
		// this);
		// 白い長方形で塗りつぶすとか？

		try {
			int size = 16;
			g.setFont(new Font("Arial", Font.PLAIN, size));

			g.setColor(new Color(255, 0, 0));
			for (int i = 0; i < detectCircle.size(); i++) {
				Circle c = detectCircle.get(i);
				double radius = Math.hypot((c.center.x - c.circum.x), (c.center.y - c.circum.y));
				if (c.distance(mouseX, mouseY) < 5) {
					g.setColor(new Color(0, 0, 255));
				} else {
					g.setColor(new Color(255, 0, 0));
				}
				g.drawOval((int) (c.center.x - radius), (int) (c.center.y - radius), (int) (2 * radius),
						(int) (2 * radius));

				g.drawString("c[" + i + "]", (int) c.center.x + size, (int) c.center.y);
				// g.drawString("c[" + i + "]", (int) c.circum.x, (int)
				// c.circum.y);
			}
			for (int i = 0; i < detectLine.size(); i++) {
				Line l = detectLine.get(i);
				if (l.distance(mouseX, mouseY) < 5) {
					g.setColor(new Color(0, 0, 255));
				} else {
					g.setColor(new Color(255, 0, 0));
				}
				g.drawLine((int) l.start.x, (int) l.start.y, (int) l.end.x, (int) l.end.y);

				g.drawString("l[" + i + "]", (int) ((l.start.x + l.end.x) / 2 - size / 2),
						(int) ((l.start.y + l.end.y) / 2 + size / 2));
				// g.drawString("l[" + i + "]", (int) ((l.start.x + 2 * l.end.x)
				// / 3),
				// (int) ((l.start.y + 2 * l.end.y) / 3));
			}

			g.setColor(new Color(0, 255, 0));
			for (int i = 0; i < detectPoint.size(); i++) {
				Point p = detectPoint.get(i);
				g.fillOval((int) (p.x - 3), (int) (p.y - 3), 6, 6);
				g.drawOval((int) (p.x - 3), (int) (p.y - 3), 6, 6);

				g.drawString("p[" + i + "]", (int) p.x - size / 2, (int) p.y + size);
			}
		} catch (Exception e) {
			//
		}

		// g.setColor(new Color(255, 255, 255));
		// g.drawRect(0, 0, imageWidth, imageHeight);
	}

	// public void processKeyEvent(MouseEvent e) {
	// repaint();
	// }

	// void processCircle() {
	//
	// }

	public void mouseClicked(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
	}

	public void mouseReleased(MouseEvent e) {
		for (int i = 0; i < detectLine.size(); i++) {
			if (detectLine.get(i).distance(e.getX(), e.getY()) < 5) {
				for (int j = 0; j < detectPoint.size(); j++) {
					if (detectLine.get(i).start == detectPoint.get(j)) {
						detectPoint.remove(j);
					}
					if (detectLine.get(i).end == detectPoint.get(j)) {
						detectPoint.remove(j);
					}
				}
				detectLine.remove(i);
			}
		}

		for (int i = 0; i < detectCircle.size(); i++) {
			if (detectCircle.get(i).distance(e.getX(), e.getY()) < 5) {
				for (int j = 0; j < detectPoint.size(); j++) {
					if (detectCircle.get(i).center == detectPoint.get(j)) {
						detectPoint.remove(j);
					}
					if (detectCircle.get(i).circum == detectPoint.get(j)) {
						detectPoint.remove(j);
					}
				}
				detectCircle.remove(i);
			}
		}
		repaint();
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
		for (int i = 0; i < detectPoint.size(); i++) {
			if (detectPoint.get(i).distance(e.getX(), e.getY()) < 10) {
				detectPoint.get(i).x = e.getX();
				detectPoint.get(i).y = e.getY();
			}
		}
		// スレッドのキューとか
		repaint();
	}

	public void mouseMoved(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
		repaint();
	}
}
