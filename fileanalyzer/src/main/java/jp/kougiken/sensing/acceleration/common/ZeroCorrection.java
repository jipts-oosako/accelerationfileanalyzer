package jp.kougiken.sensing.acceleration.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * ファイル全体のゼロ補正値を取得する
 */
public class ZeroCorrection {

	private static final Logger logger = LogManager.getLogger();

	// 進捗情報更新の頻度
	private final int progressUpdateRate = 100;

	private Path datafile = null;
	private UpdateListener listener = null;
	private int allLineNums;

	/**
	 * コンストラクタ
	 * @param datafile
	 *        検査対象のファイルパス
	 * @param allLineNums
	 *        検査対象のファイル全行数
	 * @param listener
	 *        プログレスバーの更新通知リスナー
	 */
	public ZeroCorrection(Path datafile, int allLineNums, UpdateListener listener){

		this.datafile = datafile;
		this.allLineNums = allLineNums;
		this.listener = listener;
	}

	/**
	 * 全軸のゼロ補正値（＝平均値）を取得する
	 * @return 全軸のゼロ補正値（平均値）(g)[0:X, 1:Y, 2:Z]
	 * @throws ConfigurationException
	 * @throws IOException
	 */
	public double[] getZeroCorrectionValues() throws  IOException{
		// 処理対象のファイル名
		String filename = datafile.toFile().getName();
		// 計測ファイルの区切り文字
		String separator = SystemConfigurations.getInstance().getSeparator();

		// X,Y,Z毎の平均値を取得する
		double[] average = {0.0, 0.0, 0.0};	// 0:X, 1:Y, 2:Z
		try(Stream<String> lines =  Files.lines(datafile)){
			Iterator<String> iterator = lines.iterator();

			String nodeNo = "";

			int linecount = 0;
			while(iterator.hasNext()) {
				if (linecount % progressUpdateRate == 0){
					listener.update(String.format("file:%s - ゼロ補正処理(平均値取得)...[%10d / %10d]", filename, linecount, allLineNums));
				}

				// データ取得
			    String line = (String)iterator.next();
			    MeasurementData mdata = MeasurementData.parse(line, separator);
			    for (int idx = Constants.AXIS_X; idx < Constants.AXIS_NUM; idx++){
			    	average[idx] = (linecount * average[idx] + mdata.getMeasure(idx)) / (double)(linecount + 1);
			    }
				linecount++;
			}
			logger.info(String.format("File:%s, NodeNo:%s, Average[X, Y, Z] = [%10.6f, %10.6f, %10.6f]"
					, datafile.getFileName(), nodeNo, average[Constants.AXIS_X], average[Constants.AXIS_Y], average[Constants.AXIS_Z]));
		}
		return average;
	}
}
