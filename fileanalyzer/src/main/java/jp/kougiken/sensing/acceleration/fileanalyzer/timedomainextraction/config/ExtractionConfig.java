package jp.kougiken.sensing.acceleration.fileanalyzer.timedomainextraction.config;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.ImmutableNode;

import jp.kougiken.sensing.acceleration.common.AppMessages;
import jp.kougiken.sensing.acceleration.common.Constants;

/**
 *  Time specified extraction condition - 時間指定の抽出条件
 */
public class ExtractionConfig {
	/** 定義順序(0～) */
	private int sequenceIndex;
	/** 抽出基準日時 */
	private LocalDateTime extractBaseTime;
	/** 抽出期間(s) */
	private long extractPeriod;
	/** 抽出期間の前後の余裕時間(s) */
	private long extractPeriodMargin;

	/** コンストラクタ */
	public ExtractionConfig(){

	}

	/**
	 * Configfileの指定されたサブノード(extraction.extractbasetime)から読み取り
	 * */
	public void readConfig(int index, HierarchicalConfiguration<ImmutableNode> subNode)
			throws ConfigurationException {

		// 定義順序
		sequenceIndex = index;

		// 抽出基準日時
		String sTime = subNode.getString(".");
		try {
			extractBaseTime = LocalDateTime.parse(sTime, Constants.baseFormater);
		} catch (DateTimeParseException e) {
			throw new ConfigurationException(String.format(AppMessages.ERROR.get("ERR004"), sTime));
		}

		/// 抽出期間(s)
		extractPeriod = subNode.getInt(".[@period]");
		if (extractPeriod < 0){
			throw new ConfigurationException(AppMessages.ERROR.get("ERR007"));
		}

		// 抽出期間の前後の余裕時間(s)
		extractPeriodMargin = subNode.getInt(".[@margin]");
		if (extractPeriodMargin < 0){
			throw new ConfigurationException(AppMessages.ERROR.get("ERR008"));
		}

	}

	/**
	 * 定義順序
	 * @return 0～の連番
	 */
	public int getSequenceIndex() {
		return sequenceIndex;
	}

	/**
	 * 抽出開始日時
	 * @return extractdatetime
	 */
	public LocalDateTime getExtractBaseTime() {
		return extractBaseTime;
	}

	/**
	 * 抽出期間(s)
	 * @return extractPeriod
	 */
	public long getExtractPeriod() {
		return extractPeriod;
	}

	/**
	 * 抽出期間の前後の余裕時間(s)
	 * @return extractPeriodMargin
	 */
	public long getExtractPeriodMargin() {
		return extractPeriodMargin;
	}

	/**
	 * 余裕時間を極力考慮した抽出開始日時
	 * @param measureStartTime 計測開始時刻
	 * @return 抽出開始日時
	 */
	public LocalDateTime getExtractStartTime(LocalDateTime measureStartTime){
		LocalDateTime extractStartTime = extractBaseTime;
		// 余裕時間を全て考慮できる場合
		if (measureStartTime.compareTo(extractBaseTime.minusSeconds(extractPeriodMargin)) < 0){
			extractStartTime = extractBaseTime.minusSeconds(extractPeriodMargin);
		}
		// 余裕時間を一部考慮できる場合
		else if (measureStartTime.compareTo(extractBaseTime) < 0){
			extractStartTime = measureStartTime;
		}

		return extractStartTime;
	}

	/**
	 * 抽出期間と余裕時間を考慮した抽出終了日時
	 * @return 抽出終了日時
	 */
	public LocalDateTime getExtractEndTime(){
		return extractBaseTime.plusSeconds(extractPeriod + extractPeriodMargin);
	}

	/**
	 * 抽出条件を文字列で返す
	 *
	 */
	public String toString(){
		String ret = String.format("Index[%d]、抽出基準(=開始)日時:[%s]、期間(period(s)):[%d]、抽出前後の余裕時間(margin(s))[%d]",
				sequenceIndex, extractBaseTime, extractPeriod, extractPeriodMargin);
		return ret;
	}
}
