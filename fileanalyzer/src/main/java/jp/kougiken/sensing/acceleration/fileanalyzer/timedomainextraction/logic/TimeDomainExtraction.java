package jp.kougiken.sensing.acceleration.fileanalyzer.timedomainextraction.logic;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import com.google.gson.JsonIOException;
import com.google.gson.stream.JsonWriter;

import jp.kougiken.sensing.acceleration.common.AppMessages;
import jp.kougiken.sensing.acceleration.common.Args;
import jp.kougiken.sensing.acceleration.common.CmnFunc;
import jp.kougiken.sensing.acceleration.common.Constants;
import jp.kougiken.sensing.acceleration.common.MeasurementData;
import jp.kougiken.sensing.acceleration.common.SystemConfigurations;
import jp.kougiken.sensing.acceleration.common.UpdateListener;
import jp.kougiken.sensing.acceleration.fileanalyzer.featurepointsearch.data.MinMaxData;
import jp.kougiken.sensing.acceleration.fileanalyzer.timedomainextraction.config.ExtractionConfig;
import jp.kougiken.sensing.acceleration.fileanalyzer.timedomainextraction.config.MeasurementConfig;
import jp.kougiken.sensing.acceleration.fileanalyzer.timedomainextraction.data.CollaborationData;
import jp.kougiken.sensing.acceleration.fileanalyzer.timedomainextraction.data.DatafileInfo;

/**
 * Time specified region extraction - 時間指定の領域抽出
 *
 */
public class TimeDomainExtraction extends SwingWorker<String, String> implements UpdateListener {

	private static final Logger logger = LogManager.getLogger();

	private StopWatch stopWatch = new StopWatch();

	private JProgressBar progressBar = null;
	private JTextField textfield = null;
	private JButton btnOnOff = null;

	private MeasurementConfig measurementConfig = null;

	/** 計測ファイルの区切り文字 */
	private String sysSeparator = "";

	/** 並列処理の実施可否 */
	private boolean sysParallelProcessing = true;

	/**
	 * コンストラクタ
	 * @param configPath 設定ファイルのパス
	 * @throws ConfigurationException
	 */
	public TimeDomainExtraction(String configPath) throws ConfigurationException{
		this(configPath, null, null, null);
	}

	/**
	 * コンストラクタ
	 * @param configPath 設定ファイルのパス
	 * @throws ConfigurationException
	 */
	public TimeDomainExtraction(String configPath, JProgressBar progressBar, JTextField textfield, JButton btnOnOff) throws ConfigurationException{
	    //システム設定ファイルの設定値取得
		sysSeparator = SystemConfigurations.getInstance().getSeparator();
		sysParallelProcessing = SystemConfigurations.getInstance().getParallelProcessing();

	    //設定ファイルの処理
		measurementConfig = new MeasurementConfig();
		measurementConfig.readConfig(configPath);

		// 進捗処理
		if (progressBar != null){
			this.progressBar = progressBar;
			this.progressBar.setStringPainted(true);
			this.progressBar.setValue(0);

			addPropertyChangeListener(new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					if("progress".equals(evt.getPropertyName())){
						int newVal = (int)evt.getNewValue() * progressBar.getMaximum() / 100;
						progressBar.setValue(newVal);
						String str = String.format("%d/%d", newVal, progressBar.getMaximum());
						progressBar.setString(String.format("処理中[%s]", str));
					}
				}
			});
		}
		if (textfield != null){
			this.textfield = textfield;
			this.textfield.setText("");
		}
		if (btnOnOff != null){
			this.btnOnOff = btnOnOff;
			this.btnOnOff.setEnabled(false);
		}
	}

    // 非同期に行われる処理
    @Override
    protected String doInBackground() {
		// 時間分割処理
        try {
        	if (progressBar != null) progressBar.setString("開始します");
			doAction();

        } catch (ConfigurationException ex1) {
			logger.error("Error Occurred!", ex1);
        }

        return null;
    }

    // メッセージ処理
    @Override
    protected void process(List<String> chunks) {
    	if (textfield != null && chunks.size() > 0){
    		textfield.setText(chunks.get(chunks.size()-1));
    	}
    }

    // 非同期処理後に実行
    @Override
    protected void done() {
    	if (progressBar != null) progressBar.setString("完了しました");
    	if (btnOnOff != null) btnOnOff.setEnabled(true);
    }

	/**
	 * コマンドライン処理用メイン関数
	 */
	public static void main(String[] args) {

		// 引数取得
		Args cmdargs = new Args();
		CmdLineParser parser = new CmdLineParser(cmdargs);

		try {
			parser.parseArgument(args);

		} catch (CmdLineException e) {
			System.out.println("usage:");	parser.printSingleLineUsage(System.out);
			System.out.println();			parser.printUsage(System.out);
			System.exit(10);
		}

		// 時間分割処理
		try {
			TimeDomainExtraction td = new TimeDomainExtraction(cmdargs.getConfigPath());
			td.doAction();

		} catch (ConfigurationException e) {
			logger.error("Error Occurred!", e);
		}
	}

	/**
	 * 処理実行
	 * @throws ConfigurationException
	 */
	protected void doAction() throws ConfigurationException{
		if (preprocessing(progressBar != null)){
			mainprocess();
			postprocessing();
		}
	}

	/**
	 * 前処理
	 */
	protected boolean preprocessing(boolean confirmeOutput) {
		logger.info("Welcome, TimeDomainExtraction!");
		logger.trace("preprocessing");

		// 抽出結果の出力ディレクトリの作成
		Path outputDir = measurementConfig.getOutputExtractdir();
		if (Files.exists(outputDir)){
			if (confirmeOutput){
				int option = JOptionPane.showConfirmDialog(null, "抽出データ出力ディレクトリが既に存在しています。\n抽出データが上書きされますが、処理を続行しますか？");
				if (option != JOptionPane.YES_OPTION){
					return false;
				}
			}
		}else{
			outputDir.toFile().mkdir();
		}

		// 計測開始
		stopWatch.start();
		return true;
	}

	/**
	 * 後処理
	 */
	protected void postprocessing() {
		logger.trace("postprocessing");

		// 計測停止
		stopWatch.stop();

		// 経過時間出力
		String msg = String.format("処理完了 - 処理時間:%.3f(s)\n", stopWatch.getTime()/1000.0);
		logger.info(msg);
		update(msg);

		// 検索結果の出力ディレクトリを開く
		if (progressBar != null){
			int option = JOptionPane.showConfirmDialog(null, "検索結果の出力ディレクトリを開きますか？");
			if (option == JOptionPane.YES_OPTION){
				CmnFunc.openOutputDir(measurementConfig.getOutputExtractdir());
			}
		}

		logger.info("Goodbye!");
	}

	/**
	 * Progressのメッセージ更新時に呼び出す
	 */
	@Override
	public void update(String msg) {
		publish(msg);
	}

	/**
	 * メイン処理
	 * @throws ConfigurationException
	 */
	protected void mainprocess(){
		logger.trace("mainprocess");

		// 複数ノードの計測データから、指定時間帯の計測データを抽出する
		// 複数ノードの計測データファイルのリスト
		List<File> datalists = null;
		try{
			datalists = measurementConfig.getTargetDataList();

			if(datalists.isEmpty()){
				String msg = String.format(AppMessages.ERROR.get("ERR050"));
				logger.error(msg);
				if (progressBar != null){
					JOptionPane.showMessageDialog(null, msg);
				}
				return;
			}

		}catch(PatternSyntaxException e){
			String msg = String.format(AppMessages.ERROR.get("ERR051"), e.toString());
			logger.error(msg);
			if (progressBar != null){
				JOptionPane.showMessageDialog(null, msg);
			}
			return;
		}

		int progressMax = datalists.size() + 1;
		if (this.progressBar != null){
			this.progressBar.setMinimum(0);
			this.progressBar.setMaximum(progressMax);
			setProgress(0);
		}

		// 処理開始
		AtomicInteger progressCounter = new AtomicInteger();
		progressCounter.set(1);
		setProgress((int)(progressCounter.get() /(double)progressMax * 100));

		// 外部連携データ
		List<CollaborationData> clbs = Collections.synchronizedList(new ArrayList<CollaborationData>());

		if (sysParallelProcessing){
			// 時間指定抽出 - 並列処理
			datalists.parallelStream().forEach(datafile->{
				// 計測データ情報を取得
				DatafileInfo datafileInfo = DatafileInfo.getDatafileInfo(datafile.toPath(), measurementConfig.getFrequency(), sysSeparator);
				if (datafileInfo!=null){
					List<ExtractionConfig> extractList = measurementConfig.getExtractionConfig();
					extractList.parallelStream().forEach(extractionConfig -> {
						CollaborationData clb = extract(measurementConfig, extractionConfig, datafileInfo);
						if (clb != null) clbs.add(clb);
						update(String.format("file:%s - 時間指定の領域抽出処理...", datafile.getName()));
					});
				    progressCounter.incrementAndGet();
					setProgress((int)(progressCounter.get() /(double)progressMax * 100));
					logger.info(String.format("file:%s finish.", datafile.getName()));
				}
			});
		}else{
			// 時間指定抽出 - 順次処理
			for (File datafile: datalists) {
				// 計測データ情報を取得
				DatafileInfo datafileInfo = DatafileInfo.getDatafileInfo(datafile.toPath(), measurementConfig.getFrequency(), sysSeparator);
				if (datafileInfo!=null){
					List<ExtractionConfig> extractList = measurementConfig.getExtractionConfig();
					for (ExtractionConfig extractionConfig: extractList){
						CollaborationData clb = extract(measurementConfig, extractionConfig, datafileInfo);
						if (clb != null) clbs.add(clb);
						update(String.format("file:%s - 時間指定の領域抽出処理...", datafile.getName()));
					}
				    progressCounter.incrementAndGet();
					setProgress((int)(progressCounter.get() /(double)progressMax * 100));
					logger.info(String.format("file:%s finish.", datafile.getName()));
				}
			}
		}

		// 外部連携データ出力
		outputjson(clbs);

		setProgress(100);
	}

	// json出力
	protected void outputjson(List<CollaborationData> clbs){
		Path outputDir = measurementConfig.getOutputExtractdir();
		String outputfile = Paths.get(outputDir.toString(), "collaboration.json").toString();

		// オブジェクトを要素毎に出力する
		String FMTF6 = "%.6f";
		try (JsonWriter writer = new JsonWriter(new FileWriter(outputfile))) {
		    writer.setIndent(" ");
			writer.beginArray();
			for (CollaborationData data: clbs) {
				writer.beginObject();
				writer.name("measurefileid").value(data.getMeasurefileid());
				writer.name("conditionid").value(data.getConditionid());
				writer.name("filepath").value(data.getFilepath());
				writer.name("nodeno").value(data.getNodeno());
				writer.name("startdatetime").value(data.getStartdatetime());
				writer.name("recordingtime").value(data.getRecordingtime());
				writer.name("recordcount").value(data.getRecordcount());

				for (int axis = Constants.AXIS_X; axis < Constants.AXIS_NUM; axis++) {
					String axisname = (axis == Constants.AXIS_X)? Constants.AXIS_StrX : (axis == Constants.AXIS_Y)? Constants.AXIS_StrY : Constants.AXIS_StrZ;
					writer.name(axisname+"_originalaverage").value(String.format(FMTF6, data.getAxisData()[axis].getGlobalaverage()));
					writer.name(axisname+"_average").value(String.format(FMTF6, data.getAxisData()[axis].getAverage()));
					writer.name(axisname+"_minvalue").value(String.format(FMTF6, data.getAxisData()[axis].getMinvalue()));
					writer.name(axisname+"_maxvalue").value(String.format(FMTF6, data.getAxisData()[axis].getMaxvalue()));
					writer.name(axisname+"_peaktopeak").value(String.format(FMTF6, data.getAxisData()[axis].getPeakToPeak()));
				}
				writer.endObject();
		    }
		    writer.endArray();
		} catch (JsonIOException e) {
			logger.error("IOException", e);
		} catch (IOException e) {
			logger.error("IOException", e);
		}
	}

	/**
	 * 時間指定抽出（ファイル毎-抽出条件毎）
	 * @param measurementConfig
	 * 		  計測条件
	 * @param extractionConfig
	 * 		  抽出条件
	 * @param datafileInfo
	 *		  計測データファイルの情報
	 * @return 外部連携出力オブジェクト
	 */
	protected CollaborationData extract(MeasurementConfig measurementConfig, ExtractionConfig extractionConfig, DatafileInfo datafileInfo) {

		// 外部連携出力オブジェクト
		CollaborationData clb = null;

		// 処理対象のファイル名
		String filename = datafileInfo.getDatafilePath().toFile().getName();

		// 計測開始時刻、計測周波数(Hz)
		LocalDateTime measureStartTime = measurementConfig.getStarttime();

		// 抽出条件 - 抽出開始日時、抽出終了日時
		LocalDateTime targetStartTime = extractionConfig.getExtractStartTime(measureStartTime);
		LocalDateTime targetEndTime = extractionConfig.getExtractEndTime();

		// 抽出条件番号
		int extractNo = extractionConfig.getSequenceIndex()+1;

		// 抽出開始日時が計測開始日時より過去の場合、出力できない
		if (targetStartTime.compareTo(measureStartTime) < 0){
			logger.error(String.format(AppMessages.ERROR.get("ERR105"), targetStartTime.toString(), measureStartTime.toString()));
			return null;
		}

		// 抽出終了日時が抽出開始日時より過去の場合、抽出できない
		if (targetEndTime.compareTo(targetStartTime) < 0){
			logger.error(String.format(AppMessages.ERROR.get("ERR101"), targetEndTime.toString(), targetStartTime.toString()));
			return null;
		}

		// 時間指定の抽出結果
		List<MeasurementData> extractData = null;
		try {
			// 時間指定抽出
			long time = stopWatch.getTime();
			logger.trace("抽出処理開始[{}-{}]", filename, extractNo);
			extractData = searchTime(datafileInfo, measureStartTime, targetStartTime, targetEndTime);
			logger.trace(String.format("抽出処理終了[%s-%s](処理時間[%.3f(s)])", filename, extractNo, (stopWatch.getTime() - time)/1000.0));
			logger.trace("");

		} catch (IOException e) {
			logger.error("invalid files[" + datafileInfo.getDatafilePath() + "]", e);
		}

		// 結果が得られなかった場合、ファイル出力しない
		if (!(extractData == null || extractData.size() == 0)){

			//// 抽出データの出力
			// 抽出結果の出力ディレクトリ
			Path outputDir = measurementConfig.getOutputExtractdir();
			// 記録開始日時
			LocalDateTime startdatetime = extractionConfig.getExtractBaseTime();
			// 記録期間
			long recordingtime = Duration.between(targetStartTime, targetEndTime).getSeconds();
			// 抽出データ出力ディレクトリの作成（例：“001_20160906_120000_5s”）
			String outputfolder = String.format("%05d_%s_%ds"
												, extractNo
												, startdatetime.format(Constants.outputFolderFormatter)
												, recordingtime);
			Path outputExtractdir = outputDir.resolve(outputfolder);
			if (Files.notExists(outputExtractdir)) outputExtractdir.toFile().mkdir();

			// 抽出データ出力ディレクトリに、計測データと同じファイル名で出力する
			Path outputPath = outputExtractdir.resolve(filename);
			try (FileOutputStream outStream = new FileOutputStream(outputPath.toString());
				 FileChannel outCh = outStream.getChannel();) {

				// 計測ファイルの区切り文字
				String separator = SystemConfigurations.getInstance().getSeparator();

				// ゼロ補正値（G）[0:X軸, 1:Y軸, 2:Z軸]
				double[] zerocorrections = {0.0, 0.0, 0.0};

				// 抽出結果の平均値でゼロ補正して出力する場合
				if (measurementConfig.getZeroHosei()){
					int recordcount = 0;
					for (MeasurementData tdd : extractData) {
						zerocorrections[Constants.AXIS_X] = (recordcount * zerocorrections[Constants.AXIS_X] + tdd.getMeasureX()) / (double)(recordcount + 1);
						zerocorrections[Constants.AXIS_Y] = (recordcount * zerocorrections[Constants.AXIS_Y] + tdd.getMeasureY()) / (double)(recordcount + 1);
						zerocorrections[Constants.AXIS_Z] = (recordcount * zerocorrections[Constants.AXIS_Z] + tdd.getMeasureZ()) / (double)(recordcount + 1);
						recordcount++;
					}
				}

				// 計測データ出力
				// 各軸の平均値、最小値、最大値
				MinMaxData[] mmvalues = {new MinMaxData(), new MinMaxData(), new MinMaxData()};
				double[] averages = {0.0, 0.0, 0.0};
				int recordcount = 0;
				for (MeasurementData tdd : extractData) {
					if (measurementConfig.getZeroHosei()) {
						tdd.applyingZerocorrections(zerocorrections);	// ゼロ補正値を適用
					}
					mmvalues[Constants.AXIS_X].set(tdd.getMeasureX());
					mmvalues[Constants.AXIS_Y].set(tdd.getMeasureY());
					mmvalues[Constants.AXIS_Z].set(tdd.getMeasureZ());
					averages[Constants.AXIS_X] = (recordcount * averages[Constants.AXIS_X] + tdd.getMeasureX()) / (double)(recordcount + 1);
					averages[Constants.AXIS_Y] = (recordcount * averages[Constants.AXIS_Y] + tdd.getMeasureY()) / (double)(recordcount + 1);
					averages[Constants.AXIS_Z] = (recordcount * averages[Constants.AXIS_Z] + tdd.getMeasureZ()) / (double)(recordcount + 1);

					byte[] bytes = String.format("%s\n", tdd.getLineData(separator)).getBytes();
					outCh.write(ByteBuffer.wrap(bytes));
					recordcount++;
				}

				// 外部連係データの作成
				clb = new CollaborationData();
				clb.setMeasurefileid(CmnFunc.getFileHashcode(outputPath));
				clb.setConditionid("");
				clb.setFilepath(Paths.get(outputfolder, filename).toString());
				clb.setNodeno(extractData.get(0).getNodeNo());
				clb.setStartdatetime(startdatetime.format(Constants.outputJsonFormatter));
				clb.setRecordingtime(recordingtime);
				clb.setRecordcount(recordcount);
				clb.setAxisData(zerocorrections,	// ゼロ補正値=ゼロ補正前の平均値
								averages,
								mmvalues);

			} catch (FileNotFoundException e) {
				logger.error(String.format(AppMessages.ERROR.get("ERR102"), filename), e);
			} catch (IOException e) {
				logger.error(String.format(AppMessages.ERROR.get("ERR103"), filename), e);
			}
		}

		return clb;
	}

	/**
	 * 計測開始日時と周波数の情報から、指定日時の行情報を取得
	 *
	 * @param  datafileInfo
	 *         計測データファイルの情報
	 * @param  measureStartTime
	 *         計測開始日時
	 * @param  targetStartTime
	 *         抽出開始日時
	 * @param  targetEndTime
	 *         抽出終了日時
	 * @throws IOException
	 * @throws ConfigurationException
	 */
	protected List<MeasurementData> searchTime(DatafileInfo datafileInfo, LocalDateTime measureStartTime, LocalDateTime targetStartTime, LocalDateTime targetEndTime) throws IOException {

		Path datafile = datafileInfo.getDatafilePath();
		double frequency = datafileInfo.getFrequency();
		long allLinenum = datafileInfo.getAllLinenum();
		double firstElapsedTime = datafileInfo.getFirstElapsedTime();

		//抽出期間が、計測データの範囲を超えている場合、抽出できない
		LocalDateTime firstRecordTime = datafileInfo.getRecordStartTime(measureStartTime);
		LocalDateTime lastRecordTime   = datafileInfo.getRecordEndTime(measureStartTime);
		if (firstRecordTime.isAfter(targetStartTime) || lastRecordTime.isBefore(targetEndTime)){
			logger.trace(String.format("	抽出期間[%s ～ %s]が、計測データの範囲[%s ～ %s]を超えているため、抽出できません。[%s]",
					targetStartTime.toString(), targetEndTime.toString(), firstRecordTime.toString(), lastRecordTime.toString(), datafile.getFileName()));
			return null;
		}

		//計測開始日時と抽出開始日時の差を取得。さらに、計測ファイルの先頭行の経過時間を減じて、スキップする期間を算定。
		Duration skipDuration = Duration.between(measureStartTime, targetStartTime).minusMillis(new Double(firstElapsedTime*1000).longValue());
		//抽出開始日時と抽出終了日時の差を取得
		Duration extractDuration = Duration.between(targetStartTime, targetEndTime);

		//周波数と日時差から、読み飛ばす行数を決定
		double skipSecond = skipDuration.getSeconds() + CmnFunc.nanoToSecond(skipDuration.getNano());
		long skipLineNum = (long)Math.ceil(skipSecond * frequency);

		//周波数と抽出開始日時と抽出終了日時の差から、抽出する行数を決定
		double durationSecond = extractDuration.getSeconds() + CmnFunc.nanoToSecond(extractDuration.getNano());
		long extractLinenum = (long)Math.ceil(durationSecond * frequency) + 1;

		logger.trace(String.format("	計測開始日時[%s], 計測周波数[%.3f(Hz)]", measureStartTime.toString(), frequency));
		logger.trace(String.format("	先頭行の日時[%s], 末端行の日時[%s], 全データ行数[%d]", firstRecordTime.toString(), lastRecordTime.toString(), allLinenum));
		logger.trace(String.format("	抽出開始日時[%s], 抽出終了日時[%s], 抽出期間[%.3f(s)]", targetStartTime.toString(), targetEndTime.toString(), durationSecond));
		logger.trace(String.format("	読飛ばし行数[%d](=計測周波数[%.3f(Hz)]×計測開始～抽出開始までの期間[%.3f(s)])", skipLineNum, frequency, skipSecond));
		logger.trace(String.format("	抽出結果行数[%d](=計測周波数[%.3f(Hz)]×抽出期間[%.3f(s)])", extractLinenum, frequency, durationSecond));

		// 対象行を読み込み、返値に設定する
		List<MeasurementData> retValue = new ArrayList<MeasurementData>();
		try(Stream<String> lines = Files.lines(datafile).skip(skipLineNum).limit(extractLinenum)){
			lines.forEachOrdered(line -> {
				retValue.add(MeasurementData.parse(line, sysSeparator));
			});
		}

		if (retValue.size() > 1){
			LocalDateTime extStart = measureStartTime.plusNanos((long)(CmnFunc.secondToNano(retValue.get(0).getElapsedTime())));
			LocalDateTime extEnd = measureStartTime.plusNanos((long)CmnFunc.secondToNano((retValue.get(retValue.size()-1).getElapsedTime())));
			Duration extResDuration = Duration.between(extStart, extEnd);
			logger.trace(String.format("	抽出データ - 抽出開始日時[%s], 抽出終了日時[%s], 抽出期間[%.3f(s)]"
					, extStart.toString()
					, extEnd.toString()
					, extResDuration.getSeconds() + CmnFunc.nanoToSecond(extResDuration.getNano())));
		}

		return retValue;
	}
}