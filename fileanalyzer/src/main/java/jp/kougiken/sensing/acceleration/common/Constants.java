package jp.kougiken.sensing.acceleration.common;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

/**
 * Constant value - 定数値
 */
public class Constants {
	public static final int AXIS_X = 0;
	public static final int AXIS_Y = 1;
	public static final int AXIS_Z = 2;
	public static final int AXIS_NUM = 3;
	public static final String AXIS_StrX = "X";
	public static final String AXIS_StrY = "Y";
	public static final String AXIS_StrZ = "Z";

	// 年月日時分秒のフォーマッタ
	public static final DateTimeFormatter baseFormater = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

	// 年月日時分秒ミリ秒のフォーマッタ
	// ※ミリ秒(SSS)をパースする際、JDKのバグでエラーが生じるため、以下の方法で対応する。
	public static final DateTimeFormatter baseFormaterMs = new DateTimeFormatterBuilder()
			.appendPattern("yyyy/MM/dd HH:mm:ss ").appendValue(ChronoField.MILLI_OF_SECOND, 3).toFormatter();

	// フォルダ名称生成用のフォーマッタ
	public static final DateTimeFormatter outputFolderFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
	public static final DateTimeFormatter outputJsonFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

	// double判定用の微小値
	public static final double DEPS = 1.0E-6;
}
