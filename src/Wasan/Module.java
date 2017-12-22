package Wasan;

import java.util.Comparator;

public class Module extends Scan{//extends Scan 追加
	Point p1, p2, p3, p;// 点によるインプット(p1～p3)、アウトプット(p)
	Line l1, l2, l;// 線分によるインプット(l1、l2)、アウトプット(l)
	Circle c1, c2, c;// 円によるインプット(c1、c2)、アウトプット(c)
	int n;// モジュールに付与する番号

	int type = 0;
	int ident = 0;

	Module() {// コンストラクタ①
		//
	}

	Module(Point _p1, Point _p2, Line _l1, Line _l2, Circle _c1, Circle _c2, Point _p, int _n) {// コンストラクタ②
		this.p1 = _p1;
		this.p2 = _p2;
		this.l1 = _l1;
		this.l2 = _l2;
		this.c1 = _c1;
		this.c2 = _c2;
		this.p = _p;

		this.n = _n;
	}

	Module(Point _p1, Point _p2, Line _l1, Line _l2, Circle _c1, Line _l, int _n) {// コンストラクタ③
		this.p1 = _p1;
		this.p2 = _p2;
		this.l1 = _l1;
		this.l2 = _l2;
		this.c1 = _c1;
		this.l = _l;

		this.n = _n;
	}

	Module(Point _p1, Point _p2, Point _p3, Line _l1, Circle _c1, Circle _c, int _n) {// コンストラクタ④
		this.p1 = _p1;
		this.p2 = _p2;
		this.p3 = _p3;
		this.l1 = _l1;
		this.c1 = _c1;
		this.c = _c;

		this.n = _n;
	}

	void Set(int _type, int _ident) {// モジュールにtypeとidentを設定するメソッド
		this.type = _type;
		this.ident = _ident;
	}
}
