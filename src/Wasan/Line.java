package Wasan;

import java.util.ArrayList;

public class Line extends Module {// extends Module 追加
	Point start, end;// 線分の始点、終点
	/**
	 * 線分の方程式ax+by+c=0における<br>
	 * xの係数
	 */
	double a;//
	double b;// 線分の方程式における係数
	double c;// 線分の方程式における係数
	// ★係数の正規化が必要(ベクトルabc)
	int n;// 線分に付与する番号

	Hough h = new Hough();

	double theta, rho;

	Line() {
		//
	}

	Line(Point _start, Point _end, double _a, double _b, double _c, int _n) {
		this.start = _start;
		this.end = _end;
		this.a = _a;
		this.b = _b;
		this.c = _c;
		this.n = _n;
	}

	Line(double _theta, double _rho) {// a=cos ,b=sin, c=-ρ
		theta = _theta;// _theta;
		rho = _rho;// _rho;
	}

	/*
	 * Line(Point _start, Point _end, double _a, double _b, double _c, double
	 * _theta, double _rho, int _n) {// コンストラクタにtheta,rhoを追加? this.start =
	 * _start; this.end = _end; this.a = _a; this.b = _b; this.c = _c;
	 * 
	 * this.theta = _theta; this.rho = _rho;
	 * 
	 * this.n = _n; }
	 */
	
	int getIndex(Line[] lList) {
		for (int i = 0; i < lList.length; i++) {
			if (this == lList[i]) {
				return i;
			}
		}
		return -1;
	}

	int getIndex(ArrayList<Line> lList) {
		for (int i = 0; i < lList.size(); i++) {
			if (this == lList.get(i)) {
				return i;
			}
		}
		return -1;
	}

	Line Normal(Point p1, Point p2, int n) {// 通常の線分の取得
		double a = p1.y - p2.y;
		double b = p2.x - p1.x;
		double c = p1.x * p2.y - p2.x * p1.y;

		return new Line(p1, p2, a, b, c, n);
	}

	Line Parallel(Point p1, Line l1, int n) {// 平行な線分の取得
		double a = l1.a;
		double b = l1.b;
		double c = -l1.a * p1.x - l1.b * p1.y;

		Line L = new Line(p1, null, a, b, c, -1);
		Circle C = new Circle().Normal(p1, new Point(p1.x + 100, p1.y, -1), -1);

		return new Line(p1, new Point().Inter2(L, C, 1, -1), a, b, c, n);
	}

	Line Perpendicular(Point p1, Line l1, int n) {// 垂直な線分の取得
		double a = l1.b;
		double b = -l1.a;
		double c = -l1.b * p1.x + l1.a * p1.y;

		Line L = new Line(p1, null, a, b, c, -1);
		Circle C = new Circle().Normal(p1, new Point(p1.x + 100, p1.y, -1), -1);

		return new Line(p1, new Point().Inter2(L, C, 1, -1), a, b, c, n);
	}

	Line Tangent(Point p1, Circle c1, int ident, int n) {// 円に接する線分の取得
		double x = p1.x - c1.center.x;
		double y = p1.y - c1.center.y;
		double r = dist(c1.center.x, c1.center.y, c1.circum.x, c1.circum.y);

		double a = x;
		double b = y;
		double c = -c1.center.x * a - c1.center.y * b - (a * a + b * b);

		switch (ident) {
		case 1:
			a = ((x * r - y * Math.sqrt(x * x + y * y - r * r)) * r) / (x * x + y * y);
			b = ((y * r + x * Math.sqrt(x * x + y * y - r * r)) * r) / (x * x + y * y);
			c = -a * (a + c1.center.x) - b * (b + c1.center.y);
			break;
		case 2:
			a = ((x * r + y * Math.sqrt(x * x + y * y - r * r)) * r) / (x * x + y * y);
			b = ((y * r - x * Math.sqrt(x * x + y * y - r * r)) * r) / (x * x + y * y);
			c = -a * (a + c1.center.x) - b * (b + c1.center.y);
			break;
		default:
		}

		Line L = new Line(p1, null, a, b, c, -1);
		Circle C = new Circle().Normal(p1, new Point(p1.x + 100, p1.y, -1), -1);

		return new Line(p1, new Point().Inter2(L, C, 1, -1), a, b, c, n);
	}

	Line Bisector(Line l1, Line l2, int ident, int n) {// 角の2等分線の取得
		double a = Math.hypot(l1.a, l1.b) * l2.a; // Math.sqrt(l1.a * l1.a +
													// l1.b * l1.b) * l2.a;
		double b = Math.hypot(l1.a, l1.b) * l2.b; // Math.sqrt(l1.a * l1.a +
													// l1.b * l1.b) * l2.b;
		double c = Math.hypot(l1.a, l1.b) * l2.c; // Math.sqrt(l1.a * l1.a +
													// l1.b * l1.b) * l2.c;

		switch (ident) {
		case 1:
			a = (Math.hypot(l1.a, l1.b) * l2.a) + (Math.hypot(l2.a, l2.b) * l1.a);
			b = (Math.hypot(l1.a, l1.b) * l2.b) + (Math.hypot(l2.a, l2.b) * l1.b);
			c = (Math.hypot(l1.a, l1.b) * l2.c) + (Math.hypot(l2.a, l2.b) * l1.c);
			break;
		case 2:
			a = (Math.hypot(l1.a, l1.b) * l2.a) - (Math.hypot(l2.a, l2.b) * l1.a);
			b = (Math.hypot(l1.a, l1.b) * l2.b) - (Math.hypot(l2.a, l2.b) * l1.b);
			c = (Math.hypot(l1.a, l1.b) * l2.c) - (Math.hypot(l2.a, l2.b) * l1.c);
			break;
		default:
		}

		double val1 = l1.b * l2.c - l2.b * l1.c;
		double val2 = l1.c * l2.a - l2.c * l1.a;
		double div = l1.a * l2.b - l2.a * l1.b;

		Line L = new Line(new Point(val1 / div, val2 / div, -1), null, a, b, c, -1);
		Circle C = new Circle().Normal(new Point(val1 / div, val2 / div, -1),
				new Point(val1 / div + 100, val2 / div, -1), -1);

		return new Line(new Point(val1 / div, val2 / div, -1), new Point().Inter2(L, C, 1, -1), a, b, c, n);
	}

	double dist(double sx, double sy, double ex, double ey) {
		return Math.hypot((sx - ex), (sy - ey));
	}

	double distance(int x, int y) {
		try {
			double a = end.y - start.y;
			double b = start.x - end.x;
			double c = start.y * end.x - start.x * end.y;

			double dotA = b * (start.x - x) - a * (start.y - y);
			double dotB = a * (end.y - y) - b * (end.x - x);

			if (dotA < 0 || dotB < 0) {
				return Double.MAX_VALUE;
			}

			return Math.abs(a * x + b * y + c) / Math.hypot(a, b);
		} catch (Exception e) {
			double convTheta = theta;// theta / h.constTheta;
			double convRho = rho;

			return Math.cos(Math.toRadians(convTheta)) * x + Math.sin(Math.toRadians(convTheta)) * y - convRho;
		}

		// double convTheta = theta;// theta / h.constTheta;
		// double convRho = rho;
		// return Math.cos(Math.toRadians(convTheta)) * x +
		// Math.sin(Math.toRadians(convTheta)) * y - convRho;
	}

	double getLength() {
		return Math.hypot(Math.abs(start.x - end.x), Math.abs(start.y - end.y));
	}
}
