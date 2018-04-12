package jp.kougiken.sensing.acceleration.fileanalyzer.timedomainextraction.data;

/**
 * 軸毎の情報
 */
public class AxisData {
	/** ゼロ補正前の平均値（G）*/
	private double originalAverage;
	/** 軸毎の平均値（G）*/
	private double average;
	/** 軸毎の最小値（G）*/
	private double minvalue;
	/** 軸毎の最大値（G）*/
	private double maxvalue;

	/**
	 * コンストラクタ
	 */
	public AxisData(){

	}

	/**
	 * ゼロ補正前の平均値（G）
	 * @param originalAverage セットする originalAverage
	 */
	public void setOriginalAverage(double originalAverage) {
		this.originalAverage = originalAverage;
	}
	/**
	 * 軸毎の平均値（G）
	 * @param averages セットする average
	 */
	public void setAverage(double average) {
		this.average = average;
	}
	/**
	/** 軸毎の最小値（G）
	 * @param minvalues セットする minvalue
	 */
	public void setMinvalue(double minvalue) {
		this.minvalue = minvalue;
	}
	/**
	/** 軸毎の最大値（G）
	 * @param maxvalues セットする maxvalue
	 */
	public void setMaxvalue(double maxvalue) {
		this.maxvalue = maxvalue;
	}


	/**
	 * 全計測データの平均値（G）
	 * @return globalAverage
	 */
	public double getGlobalaverage() {
		return originalAverage;
	}
	/**
	 * 軸毎の平均値（G）
	 * @return average
	 */
	public double getAverage() {
		return average;
	}
	/**
	/** 軸毎の最小値（G）
	 * @return minvalue
	 */
	public double getMinvalue() {
		return minvalue;
	}
	/**
	/** 軸毎の最大値（G）
	 * @return maxvalue
	 */
	public double getMaxvalue() {
		return maxvalue;
	}
	/**
	/** 軸毎のピークピーク値（G）
	 * @return maxvalue
	 */
	public double getPeakToPeak() {
		return maxvalue - minvalue;
	}
}
