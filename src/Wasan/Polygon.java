package Wasan;

import java.util.ArrayList;

public class Polygon extends Scan {
	Point[] p = new Point[0];// 点
	Line[] l = new Line[0];// 辺(辺の一部である場合は条件を加える)

	// 関係性認識
	// No.16～20の線分(直線)も判断基準に入れるかどうか
	// 閉じた図形の内部に点が存在するかを判定するメソッド(よくある方法？)
	// →点周りを調べる
	// あとは周上に回る関数とか
	// (補足 : 内接云々で迷うようなら自分達の意見として1つ持っておく)

	Polygon(Point... _p) {// 多角形に対応？
		Point vp = new Point();
		for (int i = 0; i < _p.length; i++) {
			p = vp.append(p, _p[i]); // p[i] = _p[i];・・
		}

		Line vl = new Line();
		try {
			// System.out.println(getLine.size());
		} catch (Exception e) {
			// System.out.println(e);
		}
		/*
		 * for (int i = 0; i < getLine.size(); i++) { for (int j = 0; j <
		 * this.p.length; j++) {// 自身の点データ for (int k = 0; k < this.p.length;
		 * k++) { if (j != k) { if (getLine.get(i).start == p[j] &&
		 * getLine.get(i).end == p[k]) { l = vl.append(l, getLine.get(i)); }
		 * else if (isNeighbor(p[k], getLine.get(i))) {// 片方が辺上にある if
		 * (getLine.get(i).start == p[j] || getLine.get(i).end == p[j]) { l =
		 * vl.append(l, getLine.get(i)); } } } } } }
		 */
	}

	Line[] getLine(ArrayList<Line> list) {
		Line vl = new Line();
		for (int i = 0; i < list.size(); i++) {
			for (int j = 0; j < this.p.length; j++) {// 自身の点データ
				for (int k = 0; k < this.p.length; k++) {
					if (j != k) {
						if (list.get(i).start == p[j] && list.get(i).end == p[k]) {
							l = vl.append(l, list.get(i));
						} else if (isNeighbor(p[k], list.get(i))) {// 片方が辺上にある
							if (list.get(i).start == p[j] || list.get(i).end == p[j]) {
								l = vl.append(l, list.get(i));
							}
						}
					}
				}
			}
		}
		return l;
	}

	boolean isNeighbor(Point p, Line l) {// 点が線分上にあるかどうかの判定
		if (l.start == p || l.end == p) {
			return false;
		}

		double dotA = (l.start.x - l.end.x) * (l.start.x - p.x) + (l.start.y - l.end.y) * (l.start.y - p.y);
		double dotB = (l.end.x - l.start.x) * (l.end.x - p.x) + (l.end.y - l.start.y) * (l.end.y - p.y);
		if (dotA < 0 || dotB < 0) {
			return false;
		}

		double dist = Math.abs(l.a * p.x + l.b * p.y + l.c) / Math.sqrt(l.a * l.a + l.b * l.b);

		return dist < 10 ? true : false;
	}

	Polygon[] append(Polygon[] array, Polygon value) {
		Polygon[] newArray = new Polygon[array.length + 1];
		for (int i = 0; i < array.length; i++) {
			newArray[i] = array[i];
		}
		newArray[array.length] = value;

		return newArray;
	}

	// 評価関数
	// 課題①...構成する点だけじゃなくて線分の情報もあった方がいい？→そういうメソッド作るか、コンストラクタの中で生成するか
	// 課題②...
	double dist(Point p1, Point p2) {
		// return Math.hypo((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) *
		// (p1.y - p2.y));
		return Math.hypot((p1.x - p2.x), (p1.y - p2.y));
	}

	// n角形ならばp[0]～p[n-1]までのデータ
	String checkTriangle() {
		if (p.length == 3) {
			// p[0]～p[2]まで存在
			double[] dist = new double[p.length];
			for (int i = 0; i < p.length; i++) {
				dist[i] = dist(p[i], p[(i < p.length - 1) ? i + 1 : 0]);
			}

			if (sameLength(dist[0], dist[1], dist[2])) {
				return "正三角形";
				// return "Eql-Triangle";
			}

			double delta = 0.05;
			for (int i = 0; i < p.length; i++) {
				int a = i;
				int b = (i < p.length - 1) ? i + 1 : 0;
				int c = (i < p.length - 2) ? i + 2 : i - 1;
				if (Math.abs(((dist[a] * dist[a] + dist[b] * dist[b]) - (dist[c] * dist[c]))
						/ (2 * dist[a] * dist[b])) < delta) {
					return "直角三角形";
					// return "Rgt-Triangle";
				}
			}

			// Math.abs(dist[i] - dist[j]) <20
			for (int i = 0; i < p.length; i++) {
				if (sameLength(dist[i], dist[(i < p.length - 1) ? i + 1 : 0])) {
					return "二等辺三角形";
					// return "Iso-Triangle";
				}
			}
		}
		return "三角形";
		// return "Nor-Triangle";
	}

	String checkQuadrangle() {
		if (p.length == 4) {
			double[] dist = new double[p.length];
			Line[] l = new Line[p.length];
			for (int i = 0; i < p.length; i++) {
				dist[i] = dist(p[i], p[(i < p.length - 1) ? i + 1 : 0]);// 点が迂回するように並べられているのが前提...
				l[i] = new Line().Normal(p[i], p[(i < p.length - 1) ? i + 1 : 0], -1);
			}

			boolean sameL = sameLength(dist[0], dist[1], dist[2], dist[3]);
			boolean isPer = isPerpendPair(l[0], l[1], l[2], l[3]);// 偏角の順に並べ直す?
			if (sameL && isPer) {
				return "正方形";
			} else if (!sameL && isPer) {
				return "長方形";
			} else if (sameL && !isPer) {
				return "菱形";
			}

			Scan s = new Scan();
			if (sameLength(dist[0], dist[2]) && s.isParallel(l[1], l[3])) {
				return "等脚台形";
			} else if (sameLength(dist[1], dist[3]) && s.isParallel(l[0], l[2])) {
				return "等脚台形";
			}
		}
		return "四角形";
	}

	boolean isPerpendPair(Line... l) {
		Scan s = new Scan();
		for (int i = 0; i < l.length; i++) {
			if (s.isPerpendicular(l[i], l[(i < l.length - 1) ? i + 1 : 0])) {
				if (i == l.length - 1) {
					return true;
				}
			} else {
				break;
			}
		}
		return false;
	}

	boolean tangentGeo(Circle c) {
		if (this.l.length == 3) {
			for (int i = 0; i < this.l.length; i++) {
				if (!isTangent(this.l[i], c)) {
					return false;
				}
			}
		} else if (this.l.length == 4) {
			for (int i = 0; i < this.l.length; i++) {
				if (!isTangent(this.l[i], c)) {
					return false;
				}
			}
		}
		return true;
	}

	// boolean sameLength(double dist1, double dist2, double dist3) {
	// double delta = 10;
	//
	// if (Math.abs(dist1 - dist2) < delta && Math.abs(dist2 - dist3) < delta &&
	// Math.abs(dist3 - dist1) < delta) {
	// return true;
	// }
	// return false;
	// }

	boolean sameLength(double... dist) {
		double delta = 15;

		for (int i = 0; i < dist.length; i++) {
			if (Math.abs(dist[i] - dist[(i < dist.length - 1) ? i + 1 : 0]) < delta) {
				// Math.abs(dist[i] - dist[(i < p.length - 1) ? i + 1 : 0]) <
				// delta
				if (i == dist.length - 1) {
					return true;
				}
			} else {
				break;
			}
		}
		return false;
	}
	// ○三角形
	// ①正三角形
	// ②二等辺三角形
	// ③直角三角形

	// ○四角形
	// ①正方形//同じ長さだし、垂直
	// ②長方形//同じ長さじゃないけど垂直
	// ③菱形→4辺の長さが同じor2組が平行//同じ長さだけど垂直じゃない
	// ④等脚台形→2辺の長さが同じ、残り2辺が平行

	// ここに内部関係の判定を作りたい
	boolean tangent(Polygon a, Polygon b) {
		return true;
	}

	boolean tangent(Polygon tri, Circle cir) {
		// 線分を取得する関数
		return true;
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

	double dist(double sx, double sy, double ex, double ey) {
		return Math.hypot((sx - ex), (sy - ey));
	}
}
