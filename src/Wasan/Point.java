package Wasan;

import java.util.ArrayList;

public class Point extends Module {// extends Module 追加
	double x, y;// 点のx座標、y座標
	int n;// 点に付与する番号
	
	Point[] links=new Point[0];

	Point() {
		//
	}

	Point(double _x, double _y, int _n) {
		this.x = _x;
		this.y = _y;
		this.n = _n;
	}
	
	int getIndex(Point[] pList) {
		for (int i = 0; i < pList.length; i++) {
			if (this == pList[i]) {
				return i;
			}
		}
		return -1;
	}

	int getIndex(ArrayList<Point> pList) {
		for (int i = 0; i < pList.size(); i++) {
			if (this == pList.get(i)) {
				return i;
			}
		}
		return -1;
	}

	Point Inter1(Line l1, Line l2, int n) {// 線分同士の交点の取得
		double crossA = (l1.start.x - l1.end.x) * (l1.start.y - l2.start.y)
				- (l1.start.y - l1.end.y) * (l1.start.x - l2.start.x);
		double crossB = (l1.start.x - l1.end.x) * (l1.start.y - l2.end.y)
				- (l1.start.y - l1.end.y) * (l1.start.x - l2.end.x);
		double crossC = (l2.start.x - l2.end.x) * (l2.start.y - l1.start.y)
				- (l2.start.y - l2.end.y) * (l2.start.x - l1.start.x);
		double crossD = (l2.start.x - l2.end.x) * (l2.start.y - l1.end.y)
				- (l2.start.y - l2.end.y) * (l2.start.x - l1.end.x);

		if (crossA * crossB >= 0 || crossC * crossD >= 0) {
			return null;
		}

		double x = (l1.b * l2.c - l2.b * l1.c) / (l1.a * l2.b - l2.a * l1.b);
		double y = (l1.c * l2.a - l2.c * l1.a) / (l1.a * l2.b - l2.a * l1.b);

		return new Point(x, y, n);
	}

	Point Inter2(Line l1, Circle c1, int ident, int n) {// 線分と円の交点の取得
		double radius = dist(c1.center.x, c1.center.y, c1.circum.x, c1.circum.y);
		double distA = dist(c1.center.x, c1.center.y, l1.start.x, l1.start.y);
		double distB = dist(c1.center.x, c1.center.y, l1.end.x, l1.end.y);

		if (distA < radius) {
			return null;
		} else if (distB < radius) {
			return null;
		}

		double D = 4 * l1.c * (-l1.c + l1.a * c1.b + l1.b * c1.c)
				+ (l1.b * c1.b - l1.a * c1.c) * (l1.b * c1.b - l1.a * c1.c) - 4 * (l1.a * l1.a + l1.b * l1.b) * c1.d;

		double x = (-(2 * l1.a * l1.c + l1.b * l1.b * c1.b - l1.a * l1.b * c1.c)) / (2 * (l1.a * l1.a + l1.b * l1.b));
		double y = (-(2 * l1.b * l1.c + l1.a * l1.a * c1.c - l1.a * l1.b * c1.b)) / (2 * (l1.a * l1.a + l1.b * l1.b));

		if (Math.abs(D) < 1000 * radius) {
			D = 0;
		}

		if (D != 0) {
			switch (ident) {
			case 1:
				x = (-(2 * l1.a * l1.c + l1.b * l1.b * c1.b - l1.a * l1.b * c1.c) + l1.b * Math.sqrt(D))
						/ (2 * (l1.a * l1.a + l1.b * l1.b));
				y = (-(2 * l1.b * l1.c + l1.a * l1.a * c1.c - l1.a * l1.b * c1.b) - l1.a * Math.sqrt(D))
						/ (2 * (l1.a * l1.a + l1.b * l1.b));
				break;
			case 2:
				x = (-(2 * l1.a * l1.c + l1.b * l1.b * c1.b - l1.a * l1.b * c1.c) - l1.b * Math.sqrt(D))
						/ (2 * (l1.a * l1.a + l1.b * l1.b));
				y = (-(2 * l1.b * l1.c + l1.a * l1.a * c1.c - l1.a * l1.b * c1.b) + l1.a * Math.sqrt(D))
						/ (2 * (l1.a * l1.a + l1.b * l1.b));
				break;
			default:
			}
		}
		return new Point(x, y, n);
	}

	Point Inter3(Circle c1, Circle c2, int ident, int n) {// 円同士の交点の取得
		double radiusA = dist(c1.center.x, c1.center.y, c1.circum.x, c1.circum.y);
		double radiusB = dist(c2.center.x, c2.center.y, c2.circum.x, c2.circum.y);
		double dist = dist(c1.center.x, c1.center.y, c2.center.x, c2.center.y);

		if (Math.abs((radiusA + radiusB) - dist) > 0) {
			return null;
		}

		double D = 4 * ((c1.b - c2.b) * (c2.b * c1.d - c1.b * c2.d) + (c1.c - c2.c) * (c2.c * c1.d - c1.c * c2.d)
				- (c1.d - c2.d) * (c1.d - c2.d)) + (c2.b * c1.c - c1.b * c2.c) * (c2.b * c1.c - c1.b * c2.c);

		double x = (-2 * (c1.b - c2.b) * (c1.d - c2.d) + (c1.c - c2.c) * ((c1.b * c2.c - c2.b * c1.c)))
				/ (2 * ((c1.b - c2.b) * (c1.b - c2.b) + (c1.c - c2.c) * (c1.c - c2.c)));
		double y = (-2 * (c1.c - c2.c) * (c1.d - c2.d) + (c1.b - c2.b) * ((c2.b * c1.c - c1.b * c2.c)))
				/ (2 * ((c1.b - c2.b) * (c1.b - c2.b) + (c1.c - c2.c) * (c1.c - c2.c)));

		if (Math.abs(D) < 1000 * radiusA) {
			D = 0;
		}

		if (D != 0) {
			switch (ident) {
			case 1:
				x = (-2 * (c1.b - c2.b) * (c1.d - c2.d) + (c1.c - c2.c) * ((c1.b * c2.c - c2.b * c1.c) + Math.sqrt(D)))
						/ (2 * ((c1.b - c2.b) * (c1.b - c2.b) + (c1.c - c2.c) * (c1.c - c2.c)));
				y = (-2 * (c1.c - c2.c) * (c1.d - c2.d) + (c1.b - c2.b) * ((c2.b * c1.c - c1.b * c2.c) - Math.sqrt(D)))
						/ (2 * ((c1.b - c2.b) * (c1.b - c2.b) + (c1.c - c2.c) * (c1.c - c2.c)));
				break;
			case 2:
				x = (-2 * (c1.b - c2.b) * (c1.d - c2.d) + (c1.c - c2.c) * ((c1.b * c2.c - c2.b * c1.c) - Math.sqrt(D)))
						/ (2 * ((c1.b - c2.b) * (c1.b - c2.b) + (c1.c - c2.c) * (c1.c - c2.c)));
				y = (-2 * (c1.c - c2.c) * (c1.d - c2.d) + (c1.b - c2.b) * ((c2.b * c1.c - c1.b * c2.c) + Math.sqrt(D)))
						/ (2 * ((c1.b - c2.b) * (c1.b - c2.b) + (c1.c - c2.c) * (c1.c - c2.c)));
				break;
			default:
			}
		}
		return new Point(x, y, n);
	}

	Point Middle(Point p1, Point p2, int n) {// 中点の取得
		double x = (p1.x + p2.x) / 2;
		double y = (p1.y + p2.y) / 2;

		return new Point(x, y, n);
	}

	double dist(double sx, double sy, double ex, double ey) {
		return Math.hypot((sx - ex), (sy - ey));
	}

	double distance(int x, int y) {
		return Math.hypot((this.x - x), (this.y - y));
	}
}
