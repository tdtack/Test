package Wasan;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import Wasan.NewCanvas.ModuleComparator;

//Scanグラフ理論的発想
//延長degree1は排除(グラフ理論の実装は数学会後でも良し)
//長い線分

public class Scan {
	Hough hough;

	// Point[] getPoint;// 検出した点の仮保存
	// Line[] getLine;// 検出した線分の仮保存-
	// Circle[] getCircle;// 検出した円の仮保存
	// Module[] getModule;

	ArrayList<Point> getPoint;// 検出した点の仮保存
	ArrayList<Line> getLine;// 検出した線分の仮保存-
	ArrayList<Circle> getCircle;// 検出した円の仮保存
	ArrayList<Module> getModule;

	// Point[] relPoint = new Point[0];// あるモジュールが関連性を持つ点モジュールを格納する配列
	// Line[] relLine = new Line[0];// あるモジュールが関連性を持つ線分モジュールを格納する配列
	// Circle[] relCircle = new Circle[0];// あるモジュールが関連性を持つ円モジュールを格納する配列
	// Module[] relModule = new Module[0];

	ArrayList<Point> relPoint = new ArrayList<Point>();
	ArrayList<Line> relLine = new ArrayList<Line>();
	ArrayList<Circle> relCircle = new ArrayList<Circle>();
	ArrayList<Module> relModule = new ArrayList<Module>();

	boolean input = true;// モジュール化した認識データを整理する際の条件分岐に利用

	Scan() {
		//
	}

	Scan(Hough _hough) {
		hough = _hough;

		// getPoint = new Point[0];
		// getLine = new Line[0];
		// getCircle = new Circle[0];
		// getModule = new Module[0];

		getPoint = new ArrayList<Point>();
		getLine = new ArrayList<Line>();
		getCircle = new ArrayList<Circle>();
		getModule = new ArrayList<Module>();
	}

	void scanPoint() {// 検出した線分と円から交点を総当たりで検出
		double w = hough.maxWidth;
		double h = hough.maxHeight;

		for (int i = 0; i < getLine.size(); i++) {
			// 線分同士の交点の検出
			for (int j = i + 1; j < getLine.size(); j++) {
				Point p = new Point().Inter1(getLine.get(i), getLine.get(j), -1);
				try {
					if ((0 < p.x && p.x < w) && (0 < p.y && p.y < h)) {
						// getPoint = (Point[]) append(getPoint, p);
						getPoint.add(p);
					}
				} catch (NullPointerException e) {
					//
				}
			}

			// 線分と円の交点の検出
			for (int j = 0; j < getCircle.size(); j++) {
				Point p1 = new Point().Inter2(getLine.get(i), getCircle.get(j), 1, -1);
				Point p2 = new Point().Inter2(getLine.get(i), getCircle.get(j), 2, -1);
				try {
					if ((0 < p1.x && p1.x < w) && (0 < p1.y && p1.y < h)) {
						// getPoint = (Point[]) append(getPoint, p1);
						getPoint.add(p1);
					}
					if ((0 < p2.x && p2.x < w) && (0 < p2.y && p2.y < h)) {
						// getPoint = (Point[]) append(getPoint, p2);
						getPoint.add(p2);
					}
				} catch (NullPointerException e) {
					//
				}
			}

			// 線分の端点の検出
			for (int j = 0; j < 2; j++) {
				Point p = (j == 0 ? getLine.get(i).start : getLine.get(i).end);
				if ((0 < p.x && p.x < w) && (0 < p.y && p.y < h)) {
					// getPoint = (Point[]) append(getPoint, p);
					getPoint.add(p);
				}
			}
		}

		// 被りがある点の排除
		for (int i = 0; i < getPoint.size(); i++) {
			getPoint = orgPoint(getPoint, getLine, getCircle, i, i + 1);
		}
	}

	// 線分の繋ぎは難しい？
	// そもそも交点などが整理されてから図形認識に入った方が安全？
	ArrayList<Point> orgPoint(ArrayList<Point> pArray, ArrayList<Line> lArray, ArrayList<Circle> cArray, int own,
			int comp) {// 検出した点に関する整理
		for (int j = comp; j < pArray.size(); j++) {
			if (dist(pArray.get(own).x, pArray.get(own).y, pArray.get(j).x, pArray.get(j).y) < 20) {// ある2点が被ると判断した場合
				// 線分の端点であった場合は連結
				for (int k = 0; k < lArray.size(); k++) {
					if (lArray.get(k).start == pArray.get(j)) {
						lArray.get(k).start = pArray.get(own);
					} else if (lArray.get(k).end == pArray.get(j)) {
						lArray.get(k).end = pArray.get(own);
					}
				}

				// 円の中心や円上の点であった場合は連結
				for (int k = 0; k < cArray.size(); k++) {
					if (cArray.get(k).center == pArray.get(j)) {
						cArray.get(k).center = pArray.get(own);
					} else if (cArray.get(k).circum == pArray.get(j)) {
						cArray.get(k).circum = pArray.get(own);
					}
				}

				// データを1つ詰め、削除してから再帰
				// for (int k = j; k < pArray.length - 1; k++) {
				// pArray[k] = pArray[k + 1];
				// }
				// return orgPoint((Point[]) shorten(pArray), lArray, cArray,
				// own, j);

				pArray.remove(j);
				return orgPoint(pArray, lArray, cArray, own, j);
			}
		}
		return pArray;
	}

	void scanLine() {// 線分の検出
		for (int i = 0; i < 15; i++) {
			Line l = hough.getFieldLine();
			// System.out.println(i+"-"+"("+l.theta + "," + l.rho+")");//★★★★★
			if (hough.restoreLine(l.theta, l.rho).getLength() > 0) {// ★//l.theta
																	// != 0 ||
																	// l.rho !=
																	// 0
				// getLine = (Line[]) append(getLine, hough.restoreLine(l.theta,
				// l.rho));
				getLine.add(hough.restoreLine(l.theta, l.rho));
			} else {
				break;
			}
		}
		getLine = orgLine();
	}

	ArrayList<Line> orgLine() {// 検出した線分に関する整理
		for (int i = 0; i < getLine.size(); i++) {
			// 線分上の点に近い場合は置き換え
			for (int j = i + 1; j < getLine.size(); j++) {
				Point p = null;
				if (isNeighbor(getLine.get(i).start, getLine.get(j))) {
					p = getLine.get(i).start;
				} else if (isNeighbor(getLine.get(i).end, getLine.get(j))) {
					p = getLine.get(i).end;
				}

				try {
					double denom = getLine.get(j).a * getLine.get(j).a + getLine.get(j).b * getLine.get(j).b;
					double numer = getLine.get(j).b * p.x - getLine.get(j).a * p.y;

					p.x = (-getLine.get(j).a * getLine.get(j).c + getLine.get(j).b * numer) / denom;
					p.y = (-getLine.get(j).b * getLine.get(j).c - getLine.get(j).a * numer) / denom;
				} catch (Exception e) {
					//
				}
			}

			// 円上の点に近い場合は置き換え
			for (int j = 0; j < getCircle.size(); j++) {
				Point p = null;
				if (isNeighbor(getLine.get(i).start, getCircle.get(j))) {
					p = getLine.get(i).start;
				} else if (isNeighbor(getLine.get(i).end, getCircle.get(j))) {
					p = getLine.get(i).end;
				}

				try {
					Line l = new Line().Normal(p, getCircle.get(j).center, -1);
					Point p1 = new Point().Inter2(l, getCircle.get(j), 1, -1);
					Point p2 = new Point().Inter2(l, getCircle.get(j), 2, -1);
					boolean isClose = dist(p.x, p.y, p1.x, p1.y) < dist(p.x, p.y, p2.x, p2.y);

					p.x = isClose ? p1.x : p2.x;
					p.y = isClose ? p1.y : p2.y;
				} catch (Exception e) {
					//
				}
			}
		}
		return getLine;
	}

	void scanCircle() {// 円の検出
		for (int i = 0; i < 10; i++) {
			Circle c = hough.getFieldCircle();
			if (c.center.x != 0 || c.center.y != 0 || c.radius != 0) {
				// getCircle = (Circle[]) append(getCircle,
				// hough.restoreCircle(c.center, c.radius));
				getCircle.add(hough.restoreCircle(c.center, c.radius));
			} else {
				break;
			}
		}
	}

	Line endPointLine(Point p, Line[] lArray) {
		for (int i = 0; i < lArray.length; i++) {
			if (p == lArray[i].start || p == lArray[i].end) {
				return lArray[i];
			}
		}
		return null;
	}

	double getVector(Point p, Line[] lArray) {
		for (int i = 0; i < lArray.length; i++) {
			if (p == lArray[i].start) {
				// return lArray[i];
			} else if (p == lArray[i].end) {
				//
			}
		}
		return 0;
	}

	boolean isNeighbor() {
		return false;
	}

	// Module[] defineModule(Point[] pArray, Line[] lArray, Circle[] cArray) {//
	// 検出した点、線分、円をモジュール化し、要素間の関連性を定義するメソッド
	// Module[] mArray = new Module[0];
	//
	// // 点のモジュール化、関連性定義
	// for (int i = 0; i < pArray.length; i++) {
	// Module m = new Module(null, null, null, null, null, null, pArray[i], -1);
	//
	// // 線分との関連性定義(点が通る線分)
	// for (int j = 0; j < lArray.length; j++) {
	// if (isNeighbor(pArray[i], lArray[j])) {
	// m.relLine = (Line[]) append(m.relLine, lArray[j]);
	// }
	// }
	//
	// // 円との関連性定義(点が通る円)
	// for (int j = 0; j < cArray.length; j++) {
	// if (isNeighbor(pArray[i], cArray[j])) {
	// m.relCircle = (Circle[]) append(m.relCircle, cArray[j]);
	// }
	// }
	//
	// // 他の点との関連性定義(中点)
	// for (int j = 0; j < pArray.length; j++) {
	// for (int k = j + 1; k < pArray.length; k++) {
	// if (((i != j) && (i != k)) && (pArray[j].input && pArray[k].input) &&
	// m.relLine.length < 3) {
	// Point p = new Point().Middle(pArray[j], pArray[k], -1);
	// if (dist(p.x, p.y, pArray[i].x, pArray[i].y) < 10) {
	// for (int l = 0; l < 2; l++) {
	// m.relPoint = (Point[]) append(m.relPoint, (l == 0 ? pArray[j] :
	// pArray[k]));
	// }
	// }
	// }
	// }
	// }
	//
	// // 関連性からして、作図上不要と思われるものを排除し、そうでないものは保存する
	// // (Point[] ScanPointで交点を総当たりで検出し、交点でしか存在しないものを排除する)
	// int relatedTotal = m.relPoint.length + m.relLine.length +
	// m.relCircle.length;
	// if (relatedTotal == 1 && isEndPoint(m.p, lArray)) {// degreeが1でそれが端点ならば、
	// // Line l = endPointLine(m.p, lArray);// それを端点とする線分を特定
	// //
	// // if (m.p == l.start) {
	// // double t1 = (l.start.x) / (l.start.x - l.end.x);// x=0の場合
	// // double t2 = (l.start.x - hough.maxWidth) / (l.start.x -
	// // l.end.x);// x=widthの場合
	// //
	// // double t = (t1 < 0) ? t1 : t2;
	// // Line l1 = new Line().Normal(new Point((l.start.x + t *
	// // (l.end.x - l.start.x)),
	// // (l.start.y + t * (l.end.y - l.start.y)), -1), l.end, -1);//
	// // 延長した仮想線分
	// //
	// // for (int j = 0; j < pArray.length; j++) {
	// // if (isNeighbor(pArray[j], l1) && l.start != pArray[j]) {
	// // l.start = pArray[j];
	// // }
	// // }
	// // } else if (m.p == l.end) {
	// // double t1 = (l.end.x) / (l.end.x - l.start.x);// x=0の場合
	// // double t2 = (l.end.x - hough.maxWidth) / (l.end.x -
	// // l.start.x);// x=widthの場合
	// //
	// // double t = (t1 < 0) ? t1 : t2;
	// // Line l1 = new Line().Normal(l.start,
	// // new Point((l.end.x + t * (l.start.x - l.end.x)), (l.end.y + t
	// // * (l.start.y - l.end.y)), -1),
	// // -1);// 延長した仮想線分
	// //
	// // for (int j = 0; j < pArray.length; j++) {
	// // if (isNeighbor(pArray[j], l1) && l.end != pArray[j]) {
	// // l.end = pArray[j];
	// // }
	// // }
	// // }
	// //
	// // m.p.input = false;
	// // mArray = excludeModule(mArray, m.p, 0);
	// } else if (relatedTotal == 2 && !isEndPoint(m.p, lArray)) {
	// m.p.input = false;
	// mArray = excludeModule(mArray, m.p, 0);
	// } else {
	// mArray = (Module[]) append(mArray, m);
	// }
	//
	// // degree1について
	// //
	// もし、自身が端点のstartであった場合は(start-end)ベクトル、endであった場合は(end-start)ベクトルで点が存在するか調べ
	// // あればその点に端点を置き換え
	// // その後、見つからなければ、逆ベクトル方向に近い点に置き換え
	// }
	//
	// // 線分のモジュール化、関連性定義
	// for (int i = 0; i < lArray.length; i++) {
	// Module m = new Module(null, null, null, null, null, lArray[i], -1);
	//
	// // 他の線分との関連性定義(平行線、垂線、角の2等分線)
	// for (int j = 0; j < lArray.length; j++) {
	// if ((i != j)) {
	// if (isParallel(lArray[i], lArray[j])) {
	// m.relLine = (Line[]) append(m.relLine, lArray[j]);
	// } else if (isPerpendicular(lArray[i], lArray[j])) {
	// m.relLine = (Line[]) append(m.relLine, lArray[j]);
	// }
	// for (int k = j + 1; k < lArray.length; k++) {
	// if (isBisector(lArray[i], lArray[j], lArray[k])) {
	// for (int l = 0; l < 2; l++) {
	// m.relLine = (Line[]) append(m.relLine, (l == 0 ? lArray[j] : lArray[k]));
	// }
	// }
	// }
	// }
	// }
	//
	// // 円との関連性定義(接円)
	// for (int j = 0; j < cArray.length; j++) {
	// if (isTangent(lArray[i], cArray[j])) {
	// m.relCircle = (Circle[]) append(m.relCircle, cArray[j]);
	// }
	// }
	//
	// // 点との関連性定義(線分上の点)
	// for (int j = 0; j < pArray.length; j++) {
	// if (pArray[j].input) {
	// if (isNeighbor(pArray[j], lArray[i])) {
	// m.relPoint = (Point[]) append(m.relPoint, pArray[j]);
	// }
	// }
	// }
	//
	// // ★ここで近しい線分は消したいところ?
	// // m.l.start,m.l.end
	// mArray = (Module[]) append(mArray, m);
	// }
	//
	// // 円のモジュール化、関連性定義
	// for (int i = 0; i < cArray.length; i++) {
	// Module m = new Module(null, null, null, null, null, cArray[i], -1);
	//
	// // 線分との関連性定義(接する線分)
	// for (int j = 0; j < lArray.length; j++) {
	// if (isTangent(lArray[j], cArray[i])) {
	// m.relLine = (Line[]) append(m.relLine, lArray[j]);
	// }
	// }
	//
	// // 他の円との関連性定義(コンパスによる同じ大きさの円、接円)
	// for (int j = 0; j < cArray.length; j++) {
	// if (i != j) {
	// if (isCompass(cArray[i], cArray[j])) {
	// // m.relCircle = (Circle[]) append(m.relCircle,
	// // cArray[j]);
	// } else if (isTangent(cArray[i], cArray[j])) {
	// m.relCircle = (Circle[]) append(m.relCircle, cArray[j]);
	// }
	// }
	// }
	//
	// // 点との関連性定義(円上の点)
	// for (int j = 0; j < pArray.length; j++) {
	// if (pArray[j].input) {
	// if (isNeighbor(pArray[j], cArray[i])) {
	// m.relPoint = (Point[]) append(m.relPoint, pArray[j]);
	// }
	// }
	// }
	//
	// mArray = (Module[]) append(mArray, m);
	// }
	//
	// return mArray;
	// }
	//
	// // Point[] ScanPointで交点を総当たりで検出し、Module[]
	// // DefineModuleの関連性定義において、作図に必要ないと判断された点モジュールを再帰を利用して排除するメソッド
	// Module[] excludeModule(Module[] mArray, Point p, int comp) {
	// for (int j = comp; j < mArray.length; j++) {
	// for (int k = 0; k < mArray[j].relPoint.length; k++) {
	// if (p == mArray[j].relPoint[k] && !p.input) {// pが既に作図上不要と判断されている場合
	// // データを2つ詰め、削除する(この場合は中点の関連性として、不要なデータが2つ含まれているため)
	// for (int l = (k % 2 == 0 ? k : k - 1); l < mArray[j].relPoint.length - 2;
	// l++) {
	// mArray[j].relPoint[l] = mArray[j].relPoint[l + 2];
	// }
	// for (int l = 0; l < 2; l++) {
	// mArray[j].relPoint = (Point[]) shorten(mArray[j].relPoint);
	// }
	//
	// if (mArray[j].relPoint.length + mArray[j].relLine.length +
	// mArray[j].relCircle.length == 2) {// 関連性からして、交点でしかないと判断された場合
	// mArray[j].p.input = false;
	// for (int l = j; l < mArray.length - 1; l++) {
	// mArray[l] = mArray[l + 1];
	// }
	// return excludeModule((Module[]) shorten(mArray), p, j);//
	// データを1つ詰め、削除してから再帰する
	// }
	// }
	// }
	// }
	// return mArray;
	// }

	ArrayList<Module> defineModule2(ArrayList<Point> pArray, ArrayList<Line> lArray, ArrayList<Circle> cArray) {// 検出した点、線分、円をモジュール化し、要素間の関連性を定義するメソッド
		ArrayList<Module> mArray = new ArrayList<Module>();

		// 点のモジュール化、関連性定義
		for (int i = 0; i < pArray.size(); i++) {
			Module m = new Module(null, null, null, null, null, null, pArray.get(i), -1);

			// 線分との関連性定義(点が通る線分)
			for (int j = 0; j < lArray.size(); j++) {
				if (isNeighbor(pArray.get(i), lArray.get(j))) {
					m.relLine.add(lArray.get(j));
				}
			}

			// 円との関連性定義(点が通る円)
			for (int j = 0; j < cArray.size(); j++) {
				if (isNeighbor(pArray.get(i), cArray.get(j))) {
					m.relCircle.add(cArray.get(j));
				}
			}

			// 他の点との関連性定義(中点)
			for (int j = 0; j < pArray.size(); j++) {
				for (int k = j + 1; k < pArray.size(); k++) {
					if (((i != j) && (i != k)) && (pArray.get(j).input && pArray.get(k).input)
							&& m.relLine.size() < 3) {
						Point p = new Point().Middle(pArray.get(j), pArray.get(k), -1);
						if (dist(p.x, p.y, pArray.get(i).x, pArray.get(i).y) < 10) {
							for (int l = 0; l < 2; l++) {
								m.relPoint.add((l == 0 ? pArray.get(j) : pArray.get(k)));
							}
						}
					}
				}
			}

			// 関連性からして、作図上不要と思われるものを排除し、そうでないものは保存する
			// (Point[] ScanPointで交点を総当たりで検出し、交点でしか存在しないものを排除する)
			int relatedTotal = m.relPoint.size() + m.relLine.size() + m.relCircle.size();
			// if (relatedTotal == 1 && isEndPoint(m.p, lArray)) {//
			// degreeが1でそれが端点ならば、
			// // degree1について
			// //
			// もし、自身が端点のstartであった場合は(start-end)ベクトル、endであった場合は(end-start)ベクトルで点が存在するか調べ
			// // あればその点に端点を置き換え
			// // その後、見つからなければ、逆ベクトル方向に近い点に置き換え
			// } else

			if (relatedTotal == 2 && !isEndPoint(m.p, lArray)) {
				m.p.input = false;
				mArray = excludeModule2(mArray, m.p, 0);
			} else {
				mArray.add(m);
			}
		}

		// 線分のモジュール化、関連性定義
		for (int i = 0; i < lArray.size(); i++) {
			Module m = new Module(null, null, null, null, null, lArray.get(i), -1);

			// 他の線分との関連性定義(平行線、垂線、角の2等分線)
			for (int j = 0; j < lArray.size(); j++) {
				if ((i != j)) {
					if (isParallel(lArray.get(i), lArray.get(j))) {
						m.relLine.add(lArray.get(j));
					} else if (isPerpendicular(lArray.get(i), lArray.get(j))) {
						m.relLine.add(lArray.get(j));
					}
					for (int k = j + 1; k < lArray.size(); k++) {
						if (isBisector(lArray.get(i), lArray.get(j), lArray.get(k))) {
							for (int l = 0; l < 2; l++) {
								m.relLine.add((l == 0 ? lArray.get(j) : lArray.get(k)));
							}
						}
					}
				}
			}

			// 円との関連性定義(接円)
			for (int j = 0; j < cArray.size(); j++) {
				if (isTangent(lArray.get(i), cArray.get(j))) {
					m.relCircle.add(cArray.get(j));
				}
			}

			// 点との関連性定義(線分上の点)
			for (int j = 0; j < pArray.size(); j++) {
				if (pArray.get(j).input) {
					if (isNeighbor(pArray.get(j), lArray.get(i))) {
						m.relPoint.add(pArray.get(j));
					}
				}
			}

			// ★ここで近しい線分は消したいところ?
			// m.l.start,m.l.end
			mArray.add(m);
		}

		// 円のモジュール化、関連性定義
		for (int i = 0; i < cArray.size(); i++) {
			Module m = new Module(null, null, null, null, null, cArray.get(i), -1);

			// 線分との関連性定義(接する線分)
			for (int j = 0; j < lArray.size(); j++) {
				if (isTangent(lArray.get(j), cArray.get(i))) {
					m.relLine.add(lArray.get(j));
				}
			}

			// 他の円との関連性定義(コンパスによる同じ大きさの円、接円)
			for (int j = 0; j < cArray.size(); j++) {
				if (i != j) {
					if (isCompass(cArray.get(i), cArray.get(j))) {
						// m.relCircle = (Circle[]) append(m.relCircle,
						// cArray[j]);
					} else if (isTangent(cArray.get(i), cArray.get(j))) {
						m.relCircle.add(cArray.get(j));
					}
				}
			}

			// 点との関連性定義(円上の点)
			for (int j = 0; j < pArray.size(); j++) {
				if (pArray.get(j).input) {
					if (isNeighbor(pArray.get(j), cArray.get(i))) {
						m.relPoint.add(pArray.get(j));
					}
				}
			}

			mArray.add(m);
		}

		// ★★★

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

		// ★★★問題
		// mArray→点5線6円1(0,1,2,3,4)(5,6,7,8,9,10)(11)
		for (int i = 0; i < mArray.size(); i++) {
			if (mArray.get(i).p != null) {
				for (int j = 0; j < mArray.get(i).relLine.size(); j++) {
					for (int k = 0; k < mArray.size(); k++) {// (mArray.size() -
																// cArray.size())
						if (mArray.get(k).l != null) {
							if (mArray.get(i).relLine.get(j) == mArray.get(k).l) {
								for (int l = 0; l < mArray.get(k).relPoint.size(); l++) {
									if (mArray.get(i).p != mArray.get(k).relPoint.get(l)) {
										//mArray.get(i).relPoint.add(mArray.get(k).relPoint.get(l));
										Point p=getPoint(pArray,mArray.get(k).relPoint.get(l));
										mArray.get(i).relPoint.add(p);
									}
								}
							}
						}
					}
				}
			} else {
				// break;
			}
		}

		/*
		 * for (int i = 0; i < mArray.size(); i++) { if (mArray.get(i).p !=
		 * null) { for (int j = 0; j < mArray.get(i).relLine.size(); j++) { for
		 * (int k = pArray.size(); k < (pArray.size() + lArray.size()); k++)
		 * {//(mArray.size() - cArray.size()) if (mArray.get(i).relLine.get(j)
		 * == mArray.get(k).l) { for (int l = 0; l <
		 * mArray.get(k).relPoint.size(); l++) { if (mArray.get(i).p !=
		 * mArray.get(k).relPoint.get(l)) {
		 * mArray.get(i).relPoint.add(mArray.get(k).relPoint.get(l)); } } } } }
		 * } else { break; } }
		 */

		for (int i = 0; i < mArray.size(); i++) {
			if (mArray.get(i).relPoint.size() > 0) {
				Collections.sort(mArray.get(i).relPoint, new PointComparator());// detectModule.get(i).relModule.sort(c);
			}
		}

		return mArray;
	}

	Point getPoint(ArrayList<Point> pArray, Point p) {
		for (int i = 0; i < pArray.size(); i++) {
			if(pArray.get(i)==p){
				return pArray.get(i);
			}
		}
		return p;
	}

	void scanPolygon() {
		//
	}

	class PointComparator implements Comparator<Point> {

		@Override
		public int compare(Point p1, Point p2) {
			try {
				int idx1 = p1.getIndex(getPoint);
				int idx2 = p2.getIndex(getPoint);
				return idx1 < idx2 ? -1 : 1;
			} catch (Exception e) {
				return 0;
			}
		}
	}

	// Module getModule(Object o) {
	// for (int i = 0; i < detectModule.size(); i++) {
	// boolean condition = false;
	// if (o instanceof Point) {
	// condition = (o == detectModule.get(i).p);
	// } else if (o instanceof Line) {
	// condition = (o == detectModule.get(i).l);
	// } else if (o instanceof Circle) {
	// condition = (o == detectModule.get(i).c);
	// }
	// if (condition) {
	// return detectModule.get(i);
	// }
	// }
	// return null;
	// }

	// Point[] ScanPointで交点を総当たりで検出し、Module[]
	// DefineModuleの関連性定義において、作図に必要ないと判断された点モジュールを再帰を利用して排除するメソッド
	ArrayList<Module> excludeModule2(ArrayList<Module> mArray, Point p, int comp) {
		for (int j = comp; j < mArray.size(); j++) {
			for (int k = 0; k < mArray.get(j).relPoint.size(); k++) {
				if (p == mArray.get(j).relPoint.get(k) && !p.input) {// pが既に作図上不要と判断されている場合
					// データを2つ詰め、削除する(この場合は中点の関連性として、不要なデータが2つ含まれているため)
					// for (int l = (k % 2 == 0 ? k : k - 1); l <
					// mArray.get(j).relPoint.size() - 2; l++) {
					// mArray[j].relPoint[l] = mArray[j].relPoint[l + 2];
					// }
					// for (int l = 0; l < 2; l++) {
					// mArray[j].relPoint = (Point[])
					// shorten(mArray[j].relPoint);
					// }

					if (k % 2 == 0) {
						for (int l = 0; l < 2; l++) {
							mArray.get(j).relPoint.remove(k);
						}
					} else {
						for (int l = 0; l < 2; l++) {
							mArray.get(j).relPoint.remove(k - 1);
						}
					}

					if (mArray.get(j).relPoint.size() + mArray.get(j).relLine.size()
							+ mArray.get(j).relCircle.size() == 2) {// 関連性からして、交点でしかないと判断された場合
						mArray.get(j).p.input = false;

						// for (int l = j; l < mArray.length - 1; l++) {//j番目の消去
						// mArray[l] = mArray[l + 1];
						// }
						// return excludeModule((Module[]) shorten(mArray), p,
						// j);// データを1つ詰め、削除してから再帰する

						mArray.remove(j);
						return excludeModule2(mArray, p, j);
					}
				}
			}
		}
		return mArray;
	}

	void scanModule(ArrayList<Point> pArray, ArrayList<Line> lArray, ArrayList<Circle> cArray) {
		// Point[] pArray, Line[] lArray, Circle[] cArray
		// getModule = defineModule(pArray, lArray, cArray);
		getModule = defineModule2(pArray, lArray, cArray);
	}

	double dist(double sx, double sy, double ex, double ey) {
		return Math.hypot((sx - ex), (sy - ey));
	}

	boolean isEndPoint(Point p, Line[] lArray) {// 点が線分の端点かどうかの判定
		for (int i = 0; i < lArray.length; i++) {
			if (p == lArray[i].start || p == lArray[i].end) {
				return true;
			}
		}
		return false;
	}

	boolean isEndPoint(Point p, ArrayList<Line> lArray) {// 点が線分の端点かどうかの判定
		for (int i = 0; i < lArray.size(); i++) {
			if (p == lArray.get(i).start || p == lArray.get(i).end) {
				return true;
			}
		}
		return false;
	}

	boolean isNeighbor(Point p, Line l) {// 点が線分上にあるかどうかの判定
		double dotA = (l.start.x - l.end.x) * (l.start.x - p.x) + (l.start.y - l.end.y) * (l.start.y - p.y);
		double dotB = (l.end.x - l.start.x) * (l.end.x - p.x) + (l.end.y - l.start.y) * (l.end.y - p.y);
		if (dotA < 0 || dotB < 0) {
			return false;
		}

		double dist = Math.abs(l.a * p.x + l.b * p.y + l.c) / Math.sqrt(l.a * l.a + l.b * l.b);

		return dist < 10 ? true : false;
	}

	boolean isNeighbor(Point p, Circle c) {// 点が円上にあるかどうかの判定
		double radius = dist(c.center.x, c.center.y, c.circum.x, c.circum.y);
		double dist = dist(c.center.x, c.center.y, p.x, p.y);

		return Math.abs(radius - dist) < 10 ? true : false;
	}

	boolean isParallel(Line l1, Line l2) {// 線分が他の線分と平行の関係にあるかどうかの判定
		double crossA = (l1.start.x - l1.end.x) * (l1.start.y - l2.start.y)
				- (l1.start.y - l1.end.y) * (l1.start.x - l2.start.x);
		double crossB = (l1.start.x - l1.end.x) * (l1.start.y - l2.end.y)
				- (l1.start.y - l1.end.y) * (l1.start.x - l2.end.x);
		double crossC = (l2.start.x - l2.end.x) * (l2.start.y - l1.start.y)
				- (l2.start.y - l2.end.y) * (l2.start.x - l1.start.x);
		double crossD = (l2.start.x - l2.end.x) * (l2.start.y - l1.end.y)
				- (l2.start.y - l2.end.y) * (l2.start.x - l1.end.x);

		if (crossA * crossB < 0 && crossC * crossD < 0) {
			return false;
		}
		return Math.abs(l1.a * l2.b - l2.a * l1.b) < 10000 ? true : false;
	}

	boolean isPerpendicular(Line l1, Line l2) {// 線分が他の線分と垂直の関係にあるかどうかの判定
		double crossA = (l1.start.x - l1.end.x) * (l1.start.y - l2.start.y)
				- (l1.start.y - l1.end.y) * (l1.start.x - l2.start.x);
		double crossB = (l1.start.x - l1.end.x) * (l1.start.y - l2.end.y)
				- (l1.start.y - l1.end.y) * (l1.start.x - l2.end.x);
		double crossC = (l2.start.x - l2.end.x) * (l2.start.y - l1.start.y)
				- (l2.start.y - l2.end.y) * (l2.start.x - l1.start.x);
		double crossD = (l2.start.x - l2.end.x) * (l2.start.y - l1.end.y)
				- (l2.start.y - l2.end.y) * (l2.start.x - l1.end.x);

		if (crossA * crossB >= 0 || crossC * crossD >= 0) {
			// return false;//★★★
		}
		return Math.abs(l1.a * l2.a + l1.b * l2.b) < 10000 ? true : false;
	}

	boolean isBisector(Line l1, Line l2, Line l3) {// 線分が他の2本の線分に対して角の2等分線であるかどうかの判定
		double crossA = l2.b * l3.c - l3.b * l2.c;
		double crossB = l2.c * l3.a - l3.c * l2.a;
		double crossC = l2.a * l3.b - l3.a * l2.b;

		boolean condA = dist(l2.start.x, l2.start.y, crossA / crossC, crossB / crossC) < 5;
		boolean condB = dist(l3.start.x, l3.start.y, crossA / crossC, crossB / crossC) < 5;

		Point vectorA = new Point();
		Point vectorB = new Point();
		vectorA.x = condA ? (l2.end.x - l2.start.x) : (l2.start.x - l2.end.x);
		vectorA.y = condA ? (l2.end.y - l2.start.y) : (l2.start.y - l2.end.y);
		vectorB.x = condB ? (l3.end.x - l3.start.x) : (l3.start.x - l3.end.x);
		vectorB.y = condB ? (l3.end.y - l3.start.y) : (l3.start.y - l3.end.y);

		double scalarA = dist(l2.start.x, l2.start.y, l2.end.x, l2.end.y);
		double scalarB = dist(l3.start.x, l3.start.y, l3.end.x, l3.end.y);
		Point vectorP = new Point((scalarB * vectorA.x + scalarA * vectorB.x) / (scalarA + scalarB),
				(scalarB * vectorA.y + scalarA * vectorB.y) / (scalarA + scalarB), -1);

		return isNeighbor(vectorP, l1) ? true : false;
	}

	boolean isCompass(Circle c1, Circle c2) {// 円が他の円と近い大きさであるかどうかの判定
		double radiusA = dist(c1.center.x, c1.center.y, c1.circum.x, c1.circum.y);
		double radiusB = dist(c2.center.x, c2.center.y, c2.circum.x, c2.circum.y);

		return Math.abs(radiusA - radiusB) < 10 ? true : false;// 5
	}

	boolean isTangent(Line l, Circle c) {// 線分が円に接するかどうかの判定
		double dotA = (l.start.x - l.end.x) * (l.start.x - c.center.x)
				+ (l.start.y - l.end.y) * (l.start.y - c.center.y);
		double dotB = (l.end.x - l.start.x) * (l.end.x - c.center.x) + (l.end.y - l.start.y) * (l.end.y - c.center.y);
		if (dotA < 0 || dotB < 0) {
			return false;
		}

		double dist = Math.abs(l.a * c.center.x + l.b * c.center.y + l.c) / Math.sqrt(l.a * l.a + l.b * l.b);
		double radius = dist(c.center.x, c.center.y, c.circum.x, c.circum.y);

		return Math.abs(dist - radius) < 10 ? true : false;// 5
	}

	boolean isTangent(Circle c1, Circle c2) {// 円が他の円と接するかどうかの判定
		double dist = dist(c1.center.x, c1.center.y, c2.center.x, c2.center.y);
		double radiusA = dist(c1.center.x, c1.center.y, c1.circum.x, c1.circum.y);
		double radiusB = dist(c2.center.x, c2.center.y, c2.circum.x, c2.circum.y);

		return Math.abs((radiusA + radiusB) - dist) < 11 ? true : false;// 5
	}

	Point[] shorten(Point[] array) {
		Point[] newArray = new Point[array.length - 1];
		for (int i = 0; i < array.length - 1; i++) {
			newArray[i] = array[i];
		}

		return newArray;
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

	Circle[] append(Circle[] array, Circle value) {
		Circle[] newArray = new Circle[array.length + 1];
		for (int i = 0; i < array.length; i++) {
			newArray[i] = array[i];
		}
		newArray[array.length] = value;

		return newArray;
	}

	Module[] shorten(Module[] array) {
		Module[] newArray = new Module[array.length - 1];
		for (int i = 0; i < array.length - 1; i++) {
			newArray[i] = array[i];
		}

		return newArray;
	}

	// 自作するなら自然にソートされるタイプに
	Module[] append(Module[] array, Module value) {
		Module[] newArray = new Module[array.length + 1];
		for (int i = 0; i < array.length; i++) {
			newArray[i] = array[i];
		}
		newArray[array.length] = value;

		return newArray;
	}

	// 内接などもそうだが、条件を書き出していきたい
	// 以下は全てboolean
	// isEndPoint(端点),isNeighbor(線分,円上の点),isParallel(平行),isPerpendicular(垂直),
	// isBisector(角の二等分線),isCompass(同じ大きさの円),isTangent(接線、接円)

	boolean isSquare(ArrayList<Line> lList) {// 正方形認識
		// 平行関係を利用できる?

		Line l1 = null;
		Line l2 = null;
		try {
			for (int i = 0; i < lList.size(); i++) {
				double l1_leng = lList.get(i).dist(lList.get(i).start.x, lList.get(i).start.y, lList.get(i).end.x,
						lList.get(i).end.y);
				for (int j = i + 1; j < lList.size(); j++) {// 2垂直isPerpendicular、1平行isParallel
					double l2_leng = lList.get(j).dist(lList.get(j).start.x, lList.get(j).start.y, lList.get(j).end.x,
							lList.get(j).end.y);
					if (isParallel(l1, l2) && Math.abs(l1_leng - l2_leng) < 10) {
						// 平行かつ同じ長さの線分を取り出す
						l1 = lList.get(i);
						l2 = lList.get(j);
					} else {
						//
					}
				}
			}
		} catch (Exception e) {
			//
		}

		Line l3 = null;
		Line l4 = null;
		try {
			for (int i = 0; i < lList.size(); i++) {
				if (lList.get(i).start == l1.start && lList.get(i).end == l2.start) {
					//
				} else if (lList.get(i).start == l1.start && lList.get(i).end == l2.end) {
					//
				} else if (lList.get(i).start == l1.end && lList.get(i).end == l2.start) {
					//
				} else if (lList.get(i).start == l1.end && lList.get(i).end == l2.end) {
					//
				}
				for (int j = i + 1; j < lList.size(); j++) {
					//
				}
			}
		} catch (Exception e) {
			//
		}
		return true;
	}

	boolean isRectangle(ArrayList<Point> pList, ArrayList<Line> lList) {// 長方形認識
		return true;
	}

	boolean isCircle(ArrayList<Circle> cList) {// 円認識
		// 認識する元画像はHough.imgPro.image
		return true;
	}

	// 他に円の個数を数える関数など

	void tagImage(ArrayList<Point> pList, ArrayList<Line> lList, ArrayList<Circle> cList) {
		ImagePro ip = hough.imgPro;
	}

	void tagCSV() {// タグ付けした情報が追記されるような感じにしたい
		try {
			File file = new File("file/タグ付け/tag.csv");
			FileOutputStream fos = new FileOutputStream(file, true);// trueで追記
			OutputStreamWriter osw = new OutputStreamWriter(fos, "SJIS");
			BufferedWriter bw = new BufferedWriter(osw);
			PrintWriter pw = new PrintWriter(bw);
			pw.print("算額画像" + hough.imgPro.i + ",");
			pw.print("タグ");
			pw.println();
			pw.close();
		} catch (Exception e) {
			//
		}
	}
}
