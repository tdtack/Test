//今は何もしなくて良い

package Wasan;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
/*
ウィンドウ・イベントを受け取るための抽象アダプタ・クラスです。このクラス内のメソッドは空です。このクラスは、リスナー・オブジェクトの作成を容易にするためのものです。 
WindowEventリスナーを作成し、目的のイベントのためのメソッドをオーバーライドするには、このクラスを拡張します。
(WindowListenerインタフェースを実装する場合には、その中のすべてのメソッドを定義する必要があります。
この抽象クラスはそれらすべてのnullメソッドを定義しているので、必要なイベント用のメソッドを定義するだけで済みます。) 

拡張したクラスを使ってリスナー・オブジェクトを作成してから、ウィンドウのaddWindowListenerメソッドを使ってWindowに登録します。
ウィンドウの状態が変更されると(開かれる、閉じられる、アクティブ化される、非アクティブ化される、アイコン化される、または非アイコン化される)、
リスナー・オブジェクトの関連するメソッドが呼び出され、WindowEventが渡されます。
*/

public class NewAdapter extends WindowAdapter {
	public void windowClosing(WindowEvent e) {
		System.exit(0);
	}
}
