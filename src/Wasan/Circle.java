package Wasan;

import java.util.ArrayList;

public class Circle extends Module {// extends Module 追加
	Point center, circum;// 円の中心にあたる点、円上にある点
	double b, c, d;// 円の方程式における係数
	int n;// 円に付与する番号

	double radius;
	
	boolean isHalf;//半円かどうかを決める?

	Circle() {
		//
	}

	Circle(Point _center, Point _circum, double _b, double _c, double _d, int _n) {// コンストラクタ②
		this.center = _center;
		this.circum = _circum;
		this.b = _b;
		this.c = _c;
		this.d = _d;
		this.n = _n;
		//コンストラクタに
	}
	
	//TODO return時に半円情報が付けられるように
	//boolean程度でOKか？

	Circle(Point _center, double _radius) {
		this.center = _center;
		this.radius = _radius;
	}
	
	int getIndex(Circle[] cList) {
		for (int i = 0; i < cList.length; i++) {
			if (this == cList[i]) {
				return i;
			}
		}
		return -1;
	}

	int getIndex(ArrayList<Circle> cList) {
		for (int i = 0; i < cList.size(); i++) {
			if (this == cList.get(i)) {
				return i;
			}
		}
		return -1;
	}

	Circle Normal(Point p1, Point p2, int n) {// 通常の円を取得するメソッド
		double b = -2 * p1.x;
		double c = -2 * p1.y;
		double d = p2.x * (2 * p1.x - p2.x) + p2.y * (2 * p1.y - p2.y);
		
		//ここで半円情報を加えるor半円情報を登録するメソッドの作成
		return new Circle(p1, p2, b, c, d, n);
	}

	Circle ThreePoint(Point p1, Point p2, Point p3, int n) {// 3点からなる円を取得するメソッド
		double A = (p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y);
		double B = (p2.x - p3.x) * (p2.x - p3.x) + (p2.y - p3.y) * (p2.y - p3.y);
		double C = (p3.x - p1.x) * (p3.x - p1.x) + (p3.y - p1.y) * (p3.y - p1.y);

		double x = (A * (B + C - A) * p3.x + B * (C + A - B) * p1.x + C * (A + B - C) * p2.x)
				/ (A * (B + C - A) + B * (C + A - B) + C * (A + B - C));
		double y = (A * (B + C - A) * p3.y + B * (C + A - B) * p1.y + C * (A + B - C) * p2.y)
				/ (A * (B + C - A) + B * (C + A - B) + C * (A + B - C));

		double b = -2 * x;
		double c = -2 * y;
		double d = p2.x * (2 * x - p2.x) + p2.y * (2 * y - p2.y);

		return new Circle(new Point(x, y, -1), p2, b, c, d, n);
	}

	Circle Compass(Point p1, Point p2, Point p3, int n) {// コンパスによる円を取得するメソッド
		double x = p3.x + Math.abs(p2.x - p1.x);
		double y = p3.y + Math.abs(p2.y - p1.y);

		double b = -2 * p3.x;
		double c = -2 * p3.y;
		double d = x * (2 * p3.x - x) + y * (2 * p3.y - y);

		return new Circle(p3, new Point(x, y, -1), b, c, d, n);
	}

	Circle Tangent1(Point p1, Line l1, int n) {// 直線に接する円を取得するメソッド
		double coeff = (l1.a * p1.x + l1.b * p1.y + l1.c) / (l1.a * l1.a + l1.b * l1.b);

		double x = p1.x - coeff * l1.a;
		double y = p1.y - coeff * l1.b;

		double b = -2 * p1.x;
		double c = -2 * p1.y;
		double d = x * (2 * p1.x - x) + y * (2 * p1.y - y);

		return new Circle(p1, new Point(x, y, -1), b, c, d, n);
	}

	Circle Tangent2(Point p1, Circle c1, int ident, int n) {// 円に接する円を取得するメソッド
		double A = c1.center.y - p1.y;
		double B = p1.x - c1.center.x;
		double C = c1.center.x * p1.y - p1.x * c1.center.y;

		double D = 4 * C * (-C + A * c1.b + B * c1.c) + (B * c1.b - A * c1.c) * (B * c1.b - A * c1.c)
				- 4 * (A * A + B * B) * c1.d;

		double x = (-(2 * A * C + B * B * c1.b - A * B * c1.c)) / (2 * (A * A + B * B));
		double y = (-(2 * B * C + A * A * c1.c - A * B * c1.b)) / (2 * (A * A + B * B));

		switch (ident) {
		case 1:
			x = (-(2 * A * C + B * B * c1.b - A * B * c1.c) + B * Math.sqrt(D)) / (2 * (A * A + B * B));
			y = (-(2 * B * C + A * A * c1.c - A * B * c1.b) - A * Math.sqrt(D)) / (2 * (A * A + B * B));
			break;
		case 2:
			x = (-(2 * A * C + B * B * c1.b - A * B * c1.c) - B * Math.sqrt(D)) / (2 * (A * A + B * B));
			y = (-(2 * B * C + A * A * c1.c - A * B * c1.b) + A * Math.sqrt(D)) / (2 * (A * A + B * B));
			break;
		default:
		}

		double b = -2 * p1.x;
		double c = -2 * p1.y;
		double d = x * (2 * p1.x - x) + y * (2 * p1.y - y);

		return new Circle(p1, new Point(x, y, -1), b, c, d, n);
	}

	double distance(int x, int y) {
		// return Math.sqrt(Math.pow((x - center.x), 2) + Math.pow((y -
		// center.y), 2)) - radius;
		// return Math.hypot((x - center.x),(y - center.y)) - radius;
		return Math.abs(
				Math.hypot((x - center.x), (y - center.y)) - Math.hypot((circum.x - center.x), (circum.y - center.y)));
	}
}
