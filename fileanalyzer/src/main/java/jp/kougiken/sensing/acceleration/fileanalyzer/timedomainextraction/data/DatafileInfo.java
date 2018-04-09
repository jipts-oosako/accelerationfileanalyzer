package jp.kougiken.sensing.acceleration.fileanalyzer.timedomainextraction.data;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jp.kougiken.sensing.acceleration.common.AppMessages;
import jp.kougiken.sensing.acceleration.common.CmnFunc;
import jp.kougiken.sensing.acceleration.common.MeasurementData;

/**
 * 計測データファイルの情報
 */
public class DatafileInfo {
    private static final Logger logger = LogManager.getLogger();

	/** データファイルパス */
    private Path datafilePath;
	/** 全行数 */
	private long allLinenum = 0;
	/** 先頭行の経過時間(s) */
	private double firstElapsedTime = 0.0;
	/** 末端行の経過時間(s) */
	private double lastElapsedTime = 0.0;
	/** 計測周波数(Hz) */
	private double frequency = 0.0;


	/**
	 * コンストラクタ
	 */
	public DatafileInfo(){
		this(null, 0, 0.0, 0.0, 0.0);
	}

	/**
	 * コンストラクタ
	 */
	public DatafileInfo(Path datafilePath, long allLinenum, double firstElapsedTime, double lastElapsedTime, double frequency){
		this.datafilePath = datafilePath;
		this.allLinenum = allLinenum;
		this.firstElapsedTime = firstElapsedTime;
		this.lastElapsedTime = lastElapsedTime;
		this.frequency = frequency;
	}

	/**
	 * データファイルパスを設定する
	 * @param datafilePath データファイルパス
	 */
	public void setDatafilePath(Path datafilePath){
		this.datafilePath = datafilePath;
	}

	/**
	 * 全行数を設定する
	 * @param allLinenum 全行数
	 */
	public void setAllLinenum(long allLinenum) {
		this.allLinenum = allLinenum;
	}

	/**
	 * 先頭行の経過時間(s)を設定する
	 * @param firstElapsedTime 先頭行の経過時間(s)
	 */
	public void setFirstElapsedTime(double firstElapsedTime) {
		this.firstElapsedTime = firstElapsedTime;
	}

	/**
	 * 末端行の経過時間(s)を設定する
	 * @param lastElapsedTime 末端行の経過時間(s)
	 */
	public void setLastElapsedTime(double lastElapsedTime) {
		this.lastElapsedTime = lastElapsedTime;
	}

	/**
	 * 計測周波数(Hz)を設定する
	 * @param frequency 計測周波数(Hz)
	 */
	public void setFrequency(double frequency) {
		this.frequency = frequency;
	}


	/**
	 * データファイルパスを設定する
	 * @return datafilePath データファイルパス
	 */
	public Path getDatafilePath(){
		return datafilePath;
	}

	/**
	 * 全行数を取得する
	 * @return allLinenum
	 */
	public long getAllLinenum() {
		return allLinenum;
	}

	/**
	 * 先頭行の経過時間(s)を取得する
	 * @return firstElapsedTime
	 */
	public double getFirstElapsedTime() {
		return firstElapsedTime;
	}

	/**
	 * 末端行の経過時間(s)を取得する
	 * @return lastElapsedTime
	 */
	public double getLastElapsedTime() {
		return lastElapsedTime;
	}

	/**
	 * 計測周波数(Hz)を設定する
	 * @return frequency
	 */
	public double getFrequency() {
		return frequency;
	}

	/**
	 * 計測データ情報を生成し取得する
	 * @param datafilePath データファイルパス
	 * @param frequency    計測周波数(Hz)
	 * @param separator    CSVファイルの区切り文字
	 * @return null：取得失敗
	 */
	public static DatafileInfo getDatafileInfo(Path datafilePath, double frequency, String separator) {
		DatafileInfo ret = null;

		try {
			double firstElapsedTime = MeasurementData.getFirstData(datafilePath, separator).getElapsedTime();
			double lastElapsedTime = MeasurementData.getLastData(datafilePath, separator).getElapsedTime();
		    if (lastElapsedTime < 0.0){
				logger.error(String.format(AppMessages.ERROR.get("ERR104")) );
		    }else{
				// ファイルの全行数を算出
		    	long allLinenum = (long)Math.ceil(lastElapsedTime * frequency) - (long)Math.ceil(firstElapsedTime * frequency) + 1;
				ret = new DatafileInfo(datafilePath, allLinenum, firstElapsedTime, lastElapsedTime, frequency);
		    }

		} catch (IOException e) {
			logger.error(String.format(AppMessages.ERROR.get("ERR104")) );
		}

		return ret;
	}

	/**
	 * 先頭行の日時を取得する
	 * @param measureStartTime 計測開始日時
	 * @return 先頭行の日時
	 */
	public LocalDateTime getRecordStartTime(LocalDateTime measureStartTime){
		return measureStartTime.plusNanos((long)CmnFunc.secondToNano(firstElapsedTime));
	}

	/**
	 * 末端行の日時を取得する
	 * @param measureEndTime 計測開始日時
	 * @return 末端行の日時
	 */
	public LocalDateTime getRecordEndTime(LocalDateTime measureStartTime){
		return measureStartTime.plusNanos((long)CmnFunc.secondToNano(lastElapsedTime));
	}
}
