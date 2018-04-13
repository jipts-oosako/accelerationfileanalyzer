package jp.kougiken.sensing.acceleration.fileanalyzer.featurepointsearch.config;

import jp.kougiken.sensing.acceleration.common.Constants;

/**
 * Detection boundary value for each axis. - 軸毎の検出境界値
 */
public class BoundaryConfig {
	/** 軸のIndex値 */
	private int axisIndex;
	/** 軸の検出境界値(gal)(正負共通)*/
	private double boundary;

	/**
	 * コンストラクタ
	 */
	public BoundaryConfig(){

	}

	/**
	 * コンストラクタ
	 */
	public BoundaryConfig(int axisIndex, double boundary){
		// 軸のIndex値
		this.axisIndex = axisIndex;
		// 検出境界値(gal)(正負共通)
		this.boundary = boundary;
	}

	/**
	 * 設定軸のIndex
	 * @return axisIndex
	 */
	public int getAxisIndex() {
		return axisIndex;
	}

	/**
	 * 設定軸の文字列表現
	 * @return X軸=AXIS_StrX, Y軸=AXIS_StrY, Z軸=AXIS_StrZ
	 */
	public String getAxisString() {
		String ret = "";
		switch(axisIndex){
		case Constants.AXIS_X: ret = Constants.AXIS_StrX;break;
		case Constants.AXIS_Y: ret = Constants.AXIS_StrY;break;
		case Constants.AXIS_Z: ret = Constants.AXIS_StrZ;break;
		}
		return ret;
	}

	/**
	 * 検出境界値(gal)(正負共通)
	 * @return boundary
	 */
	public double getBoundary() {
		return boundary;
	}

	/**
	 * 条件を文字列で返す
	 *
	 */
	public String toString(){
		String ret = String.format("%s:[%.6f]", getAxisString(), boundary);
		return ret;
	}
}
