package jp.kougiken.sensing.acceleration.fileanalyzer.featurepointsearch.data;

import java.math.BigDecimal;

/**
 * Minimum value, maximum value pair - 最小値、最大値のペア
 */
public class MinMaxData {

	private double min = Double.POSITIVE_INFINITY;
	private double max = Double.NEGATIVE_INFINITY;

	public MinMaxData(){
	}

	public MinMaxData(double min, double max){
		set(min, max);
	}

	/**
	 * 最小値、最大値を設定する
	 * @param min 最小値
	 * @param max 最大値
	 */
	public void set(double min, double max){
		this.min = min;
		this.max = max;
	}

	/**
	 * 指定値の最小、最大を判断して設定する
	 * @param value 最小値、最大値となりうる値
	 */
	public void set(double value){
		this.min = Math.min(this.min, value);
		this.max = Math.max(this.max, value);
	}

	public double getMin(){
		return min;
	}

	public double getMax(){
		return max;
	}

	/**
	 * Min-Maxの平均値を取得
	 * @return Min-Maxの平均値(小数点以下6桁に四捨五入)
	 */
	public double getMinMaxAverage(){
		return getMinMaxAverage(true, 6);
	}
	/**
	 * Min-Maxの平均値を取得
	 * @param round 四捨五入する場合true
	 * @param newScale 四捨五入後に丸めた桁数
	 * @return Min-Maxの平均値
	 */
	public double getMinMaxAverage(boolean round, int newScale){
		double ret = (max + min) / 2.0;
		if (round){
			ret = new BigDecimal(ret).setScale(newScale, BigDecimal.ROUND_HALF_UP).doubleValue();
		}
		return ret;
	}

	public boolean in(double value){
		return (min < value && value <= max);
	}

	public String toString(){
		return String.format("%.6f～%.6f", min, max);
	}
}
