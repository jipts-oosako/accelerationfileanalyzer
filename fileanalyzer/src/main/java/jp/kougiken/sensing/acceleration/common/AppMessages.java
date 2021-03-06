package jp.kougiken.sensing.acceleration.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Message definition - メッセージ定義クラス
 */
public final class AppMessages {
    public static final Map<String ,String> SYSTEM = new HashMap<String ,String>() {
    	{put("SYS001", "計測ファイルの項目の区切り文字(セパレータ)が指定されていません。");}
    	{put("SYS002", "ゼロ補正を行う方法の指定が不正です。");}
	};

    public static final Map<String ,String> ERROR = new HashMap<String ,String>() {
    	// 時間指定の設定ファイル読み込み
    	{put("ERR001", "計測データの格納ディレクトリが存在しない、または、書き込みができません。");}
    	{put("ERR002", "対象とする計測ファイルのファイルパターンが指定されていません。");}
    	{put("ERR003", "");}
    	{put("ERR004", "抽出開始時刻の指定が正しくありません。[%s]");}
    	{put("ERR005", "抽出結果の出力ディレクトリが指定されていません。");}
    	{put("ERR006", "ディレクトリの作成に失敗しました。");}
    	{put("ERR007", "抽出期間は、0(s)以上で設定してください。");}
    	{put("ERR008", "抽出期間の前後の余裕時間は、0(s)以上で設定してください。");}
    	{put("ERR009", "ゼロ点からのずれの平均値の指定が正しくありません。");}
    	// 特徴点領域の検出条件の設定ファイル読み込み
    	{put("ERR021", "設定軸は、X, Y, Z のいずれかを指定してください。");}
    	// 処理共通
    	{put("ERR050", "処理対象の計測データファイルが見つかりません。\nデータの保存状態やfilepatternを確認してください。");}
    	{put("ERR051", "filepatternの正規表現に誤りがあります。\n[%s]");}
    	{put("ERR060", "処理対象の計測データファイルのフォーマットが不正です。(%s)[%s]");}
    	// 時間指定抽出
    	{put("ERR101", "抽出終了時刻[%s]が抽出開始時刻[%s]より過去の日時になっているため、データ抽出することができません。");}
    	{put("ERR102", "抽出ファイルが保存できません。[%s]");}
    	{put("ERR103", "ファイルの入力、または、出力処理で、エラーが発生しました。[%s]");}
    	{put("ERR104", "計測データから、最終の経過時間が取得できませんでした。");}
    	{put("ERR105", "抽出開始日時[%s]が計測開始日時[%s]より過去の日時になっているため、データ抽出することができません。");}
	};
}
