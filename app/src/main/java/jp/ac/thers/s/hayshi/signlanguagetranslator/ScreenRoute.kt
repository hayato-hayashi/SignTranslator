/*=====================================================================
サブクラスがclassではなくobjectの理由
classとobjectを利用するときはインスタンスを生成する必要がある
objectはclassと異なりインスタンスが1つしか生成されない

Car Lexus = new Car()
Car BMW= new Car()

class Carの場合                object Carの場合
LexusとBMWは全く違うもの         LexusとBMWは同じものが生成されている

objectを使用すると常に同じインスタンスが返されることになる
====================================================================*/

package jp.ac.thers.s.hayshi.signlanguagetranslator

sealed class ScreenRoute(val route: String) {
    object TranslationScreen : ScreenRoute("translation_screen")
    object LogScreen : ScreenRoute("log_screen")
}