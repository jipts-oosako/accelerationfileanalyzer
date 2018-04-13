package jp.kougiken.sensing.acceleration.fileanalyzer.featurepointsearch.data;

import java.time.LocalDateTime;

import jp.kougiken.sensing.acceleration.common.CmnFunc;

/**
 * Data of extraction area - 抽出領域のデータ
 */
public class RegionData {

	/** 検出軸 */
	private String axis;
	/** ノードNo */
	private String nodeNo;
	/** 計測開始日時 */
	private LocalDateTime measureStartTime;
	/** 検出開始時の計測開始からの経過時間(s) */
	private double startTime;
	/** 検出終了時の計測開始からの経過時間(s) */
	private double endTime;

	/**
	 * コンストラクタ
	 * @param  axis
	 *         検出軸
	 * @param  measureStartTime
	 *         計測開始日時
	 * @param  lineNo
	 *         先頭の行を1行目とする行番号
	 * @param  nodeNo
	 *         ノードNo
	 * @param  startTime
	 *         計測開始から検出開始時までの経過時間(s)(0.0～)
	 * @param  endTime
	 *         計測開始から検出終了時までの経過時間(s)(0.0～)
	 * @param  prebuffer
	 *         検出直前に割り込ませる余裕時間(s)
	 */
	protected RegionData(){

	}

	public RegionData(String axis, LocalDateTime measureStartTime, String nodeNo, double startTime, double endTime, double prebuffer){
		this.axis = axis;
		this.measureStartTime = measureStartTime;
		this.nodeNo = nodeNo;
		// 開始時刻は切り捨て、終了時刻は切り上げとする
		// 検出直前に余裕時間を割り込ませられる場合、余裕時間を含める
		this.startTime = Math.floor((startTime - prebuffer >= 0.0)? (startTime-prebuffer) : startTime);
		this.endTime = Math.ceil(endTime);
	}

	/**
	 * 検出軸を取得
	 * @return 検出軸（文字列）
	 */
	public String getAxis() {
		return axis;
	}

	/**
	 * 検出開始日時を取得
	 * @return 検出開始日時
	 */
	public LocalDateTime getSearchStart() {
		return measureStartTime.plusNanos((long)CmnFunc.secondToNano(startTime));
	}

	/**
	 * 検出時間(検出開始時から検出終了時までの時間)を取得
	 * @return 検出時間(s)
	 */
	public double getPeriod() {
		return endTime - startTime;
	}
	/**
	 * 検出開始時の計測開始からの経過時間(s)
	 */
	public double getStartTime(){
		return startTime;
	}

	/**
	 * 検出終了時の計測開始からの経過時間(s)
	 */
	public double getEndTime(){
		return endTime;
	}

	/**
	 * 内包データを文字列で返す
	 * @return 文字列表現した内包データ
	 */
	public String toString(){
		String ret = String.format("ノードNo:%s(%s軸), 計測日時:%s～%s(%.3f(s))",
				nodeNo, axis, measureStartTime.plusNanos((long)CmnFunc.secondToNano(startTime))
				            , measureStartTime.plusNanos((long)CmnFunc.secondToNano(endTime))
				            , (endTime - startTime));
		return ret;
	}
}
