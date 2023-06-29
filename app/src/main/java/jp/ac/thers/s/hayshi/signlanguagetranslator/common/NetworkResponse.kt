/*==================================================================================================
sealed class
インタフェースのようにクラスが保持するフィールド変数やメソッドを定義する
インタフェースとの違いとしてサブクラス(sealed classを継承したクラス)を
同じファイルに記述しなければならない
そうすることで、サブクラスの管理がしやすくなる

class Success<T>(data: T) : NetworkResponse<T>(data = data2)
NetworkResponseを継承するSuccessクラスを作成して、コンストラクタも定義する(data: T)
(data = data)はNetworkResponseのコンストラクタを使用して、dataの値を初期化している
右辺のdata2はSuccessコンストラクタが受け取った値

このクラスを使用してAPIをたたいたときの通信状態を管理する
==================================================================================================*/

package jp.ac.thers.s.hayshi.signlanguagetranslator.common

sealed class NetworkResponse<T> (
    val data: T? = null,
    val error: String? = null,
) {
    class Success<T>(data: T) : NetworkResponse<T>(data = data)
    class Failure<T>(error: String) : NetworkResponse<T>(error = error)
    class Loading<T> : NetworkResponse<T>()
}
