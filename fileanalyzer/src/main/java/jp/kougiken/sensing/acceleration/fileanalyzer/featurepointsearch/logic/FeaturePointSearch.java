package jp.kougiken.sensing.acceleration.fileanalyzer.featurepointsearch.logic;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
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
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.util.DoubleArray;
import org.apache.commons.math3.util.ResizableDoubleArray;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import jp.kougiken.sensing.acceleration.common.AppMessages;
import jp.kougiken.sensing.acceleration.common.Args;
import jp.kougiken.sensing.acceleration.common.CmnFunc;
import jp.kougiken.sensing.acceleration.common.Constants;
import jp.kougiken.sensing.acceleration.common.MeasurementData;
import jp.kougiken.sensing.acceleration.common.SystemConfigurations;
import jp.kougiken.sensing.acceleration.common.UpdateListener;
import jp.kougiken.sensing.acceleration.fileanalyzer.featurepointsearch.config.BoundaryConfig;
import jp.kougiken.sensing.acceleration.fileanalyzer.featurepointsearch.config.DetectionConfig;
import jp.kougiken.sensing.acceleration.fileanalyzer.featurepointsearch.config.SearchConfig;
import jp.kougiken.sensing.acceleration.fileanalyzer.featurepointsearch.data.RegionData;

/**
 * Search of the feature point area - 特徴領域の検索
 *
 */
public class FeaturePointSearch extends SwingWorker<String, String> implements UpdateListener {

	private static final Logger logger = LogManager.getLogger();

	// 進捗情報更新の頻度
	private final int progressUpdateRate = 100;

	private StopWatch stopWatch = new StopWatch();

	private JProgressBar progressBar = null;
	private JTextField textfield = null;
	private JButton btnOnOff = null;

	private SearchConfig searchConfig = null;

	// 計測ファイルの区切り文字
	private String sysSeparator = "";
	// 並列処理の実施可否
	private boolean sysParallelProcessing = true;

	/**
	 * コンストラクタ
	 * @param configPath 設定ファイルのパス
	 * @throws ConfigurationException
	 */
	public FeaturePointSearch(String configPath) throws ConfigurationException{
		this(configPath, null, null, null);
	}

	/**
	 * コンストラクタ
	 * @param configPath 設定ファイルのパス
	 * @throws ConfigurationException
	 */
	public FeaturePointSearch(String configPath, JProgressBar progressBar, JTextField textfield, JButton btnOnOff) throws ConfigurationException{
	    //システム設定ファイルの設定値取得
		sysSeparator = SystemConfigurations.getInstance().getSeparator();
		sysParallelProcessing = SystemConfigurations.getInstance().getParallelProcessing();

		//設定ファイルの処理
		searchConfig = new SearchConfig();
		searchConfig.readConfig(configPath);

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

		// 特徴領域の検索処理
		try {
			FeaturePointSearch fp = new FeaturePointSearch(cmdargs.getConfigPath());
			fp.doAction();

		} catch (ConfigurationException e) {
			logger.error("Error Occurred!", e);
		}
	}

	/**
	 * 処理実行
	 * @throws ConfigurationException
	 */
	public void doAction() throws ConfigurationException {
		if (preprocessing(progressBar != null && false)){
			mainprocess();
			postprocessing();
		}
	}

	/**
	 * 前処理
	 * @param configPath 設定ファイルのパス
	 * @param searchConfig 起動時に指定された設定
	 */
	protected boolean preprocessing(boolean confirmeOutput) {
		logger.info("Welcome, FeaturePointSearch!");
		logger.trace("preprocessing");

		// 検出結果の出力ディレクトリの作成
		Path outputDir = searchConfig.getOutputExtractdir();
		if (Files.exists(outputDir)){
			if (confirmeOutput){
				int option = JOptionPane.showConfirmDialog(null, "検出結果の出力ディレクトリが既に存在しています。\n検出結果が上書きされますが、処理を続行しますか？");
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
		String msg = String.format("処理完了 - 処理時間:%.3f(s)\n", stopWatch.getTime() / 1000.0);
		logger.info(msg);
		publish(msg);

		// 抽出結果の出力ディレクトリを開く
		if (progressBar != null){
			int option = JOptionPane.showConfirmDialog(null, "抽出結果の出力ディレクトリを開きますか？");
			if (option == JOptionPane.YES_OPTION){
				CmnFunc.openOutputDir(searchConfig.getOutputExtractdir());
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
	 * @param bar プログレスバーコントロール
	 * @throws ConfigurationException
	 */
	protected void mainprocess() {
		logger.trace("mainprocess");

		// 複数ノードの計測データから、特徴領域の時間帯を抽出する
		// 複数ノードの計測データのファイルリスト
		List<File> datalists = null;
		try{
			datalists = searchConfig.getTargetDataList();

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


		int progressMax = datalists.size() * searchConfig.getDetectionConfig().getAxisBoundarys().size() + 1;
		if (this.progressBar != null){
			this.progressBar.setMinimum(0);
			this.progressBar.setMaximum(progressMax);
			setProgress(0);
		}

		// 処理開始
		AtomicInteger progressCounter = new AtomicInteger();
		progressCounter.set(1);
		setProgress((int)(progressCounter.get() /(double)progressMax * 100));
		if (sysParallelProcessing){
			// 軸毎の特徴点抽出 - 並列処理
			datalists.parallelStream().forEach(file->{
				List<BoundaryConfig> axislist = searchConfig.getDetectionConfig().getAxisBoundarys();
				axislist.parallelStream().forEach(boundary -> {
					fpsearch(file.toPath(), searchConfig, boundary);
				    progressCounter.incrementAndGet();
					setProgress((int)(progressCounter.get() /(double)progressMax * 100));
				});
			});
		}else{
			// 軸毎の特徴点抽出 - 順次処理
			for (File file: datalists) {
				for (BoundaryConfig boundary: searchConfig.getDetectionConfig().getAxisBoundarys()){
					fpsearch(file.toPath(), searchConfig, boundary);
				    progressCounter.incrementAndGet();
					setProgress((int)(progressCounter.get() /(double)progressMax * 100));
				}
			}
		}
		setProgress(100);
	}

	/**
	 * 特徴領域の検出（ファイル毎-軸毎）
	 * @param datafile
	 *		  計測データファイル
	 * @param searchConfig
	 * 		  検出条件
	 * @param boundary
	 * 		  軸毎の検出境界
	 */
	protected void fpsearch(Path datafile, SearchConfig searchConfig, BoundaryConfig boundary){

		// 処理対象のファイル名
		String filename = datafile.toFile().getName();

		// 時間指定抽出
		long time = stopWatch.getTime();
		logger.trace("検出処理開始[{}-{}]", filename, boundary.getAxisString());

		// 計測開始時刻、計測周波数(Hz)
		LocalDateTime measureStartTime = searchConfig.getStarttime();
		double frequency = searchConfig.getFrequency();

		// 抽出結果の出力ディレクトリ
		Path outputDir = searchConfig.getOutputExtractdir();

		//// 特徴領域の検出
		List<RegionData> featurePointRegions = null;
		try {
			// ファイル末尾の経過時間から全行数を推測する
			double firstElapsedTime = MeasurementData.getFirstData(datafile, sysSeparator).getElapsedTime();
			double lastElapsedTime = MeasurementData.getLastData(datafile, sysSeparator).getElapsedTime();
			int allLineNums = (int)((lastElapsedTime-firstElapsedTime) * 100) + 1;

			// 特徴領域を検出する
			featurePointRegions = searchFeaturePointRegions(datafile, measureStartTime, frequency, searchConfig, boundary, allLineNums);

			logger.trace(String.format("検出処理終了[%s-%s](処理時間[.3f(s)])", filename, boundary.getAxisString(), (stopWatch.getTime() - time) / 1000.0));
			logger.trace("");

		} catch (IOException e) {
			logger.error("invalid files[" + datafile + "]", e);
		}

		// 結果が得られなかった場合でも、ファイル出力は行う
		if (featurePointRegions != null){

			//// 検出データの出力
			// 検出データ出力ディレクトリの作成（ファイル名と同じディレクトリ名）
			Path outputSearchdir = outputDir.resolve(filename);
			if (Files.notExists(outputSearchdir)) outputSearchdir.toFile().mkdir();

			// 検出データ出力ディレクトリに、軸毎の検出結果を出力する
			String axisString = boundary.getAxisString();
			Path outputPath = outputSearchdir.resolve(String.format("axis_%s.txt", axisString));
			try (PrintWriter pw = new PrintWriter(outputPath.toString())){
				// 特徴領域の情報を出力
				pw.println("\t<extraction>");
				for (int idx = 0; idx < featurePointRegions.size(); idx++) {
					RegionData pd = featurePointRegions.get(idx);
					pw.println(String.format("\t\t<extractbasetime period=\"%d\" margin=\"%d\">%s</extractbasetime>"
							, (long)pd.getPeriod(), 0, pd.getSearchStart().format(Constants.baseFormater)));
					logger.info(String.format("axis, %s, extractbasetime period, %4d, margin, %3d, elapsedtime, %12.2f, extractbasetime, %s"
							, axisString, (long)pd.getPeriod(), 0, pd.getStartTime(), pd.getSearchStart().format(Constants.baseFormater)));
				}
				pw.println("\t</extraction>");
			} catch (FileNotFoundException e) {
				logger.error(String.format(AppMessages.ERROR.get("ERR102"), filename), e);
			} catch (Exception e2) {
				logger.error(String.format("error"), e2);
			}
		}
	}

	/**
	 * 計測開始日時と周波数の情報から、指定日時の行情報を取得
	 *
	 * @param datafile
	 *        データファイルパス
	 * @param measureStartTime
	 *        計測開始日時
	 * @param frequency
	 *        計測周波数(Hz)
	 * @param searchConfig
	 *        検索条件
	 * @param boundary
	 *        軸毎の検出境界
	 * @param allLineNums
	 *        全行数(推測値)
	 * @param zeroCorrection
	 */
	protected List<RegionData> searchFeaturePointRegions(Path datafile, LocalDateTime measureStartTime, double frequency, SearchConfig searchConfig, BoundaryConfig boundary, int allLineNums) {

		// 処理対象のファイル名
		String filename = datafile.toFile().getName();
		// 検出対象の計測データの軸Index
		int axisIndex = boundary.getAxisIndex();
		// 検出対象の計測データの軸名称
		String targetAxisString = boundary.getAxisString();

		// 検出条件
		DetectionConfig detectionConfig = searchConfig.getDetectionConfig();

		double startTime = -1.0;
		int enddurationNum = (int)(detectionConfig.getEndduration() * 100);	// 終了判定期間(s)に相当するデータ数
		double peakjudgeSecond = 0.5;			// ピーク判定に用いる時間
		int peakjudgeNum = (int)(peakjudgeSecond * 100);	// ピーク判定に用いるデータ数

	    MeasurementData mdata = null;	// 計測データ
		List<String> measurementDataList = null;

		// 検出結果
		publish(String.format("file:%s - %s軸 - searching start", filename, targetAxisString));
		List<RegionData> retValue = new ArrayList<RegionData>();
		try(Stream<String> lines =  Files.lines(datafile)){
			Iterator<String> iterator = lines.iterator();
			int linecount = 0;
			// ピーク判定を行うためのデータ蓄積
			DoubleArray peakToPeak = new ResizableDoubleArray();
			// 収束判定を行うためのデータ蓄積
			DoubleArray zeroranges = new ResizableDoubleArray();
			while(iterator.hasNext()) {
				linecount++;
				if (linecount % progressUpdateRate == 0){
					publish(String.format("file:%s - %s軸 - searching...[%10d / %10d]", filename, targetAxisString, linecount, allLineNums));
				}

				// データ取得
			    String line = (String)iterator.next();
			    mdata = MeasurementData.parse(line, sysSeparator);
			    if (mdata==null) continue;

			    double value = mdata.getMeasure(axisIndex);

				// ピーク判定の更新(FIFO)
				if (peakToPeak.getNumElements() > peakjudgeNum){
					peakToPeak.addElementRolling(value);
				}else{
					peakToPeak.addElement(value);
				}

				// 検出中でない場合
				if (startTime < 0.0){
					// ピーク判定に必要なデータが蓄積されている場合
					if (peakToPeak.getNumElements() >= peakjudgeNum){
						double min = StatUtils.min(peakToPeak.getElements());
						double max = StatUtils.max(peakToPeak.getElements());

						if (Math.abs(max-min) > boundary.getBoundary()){
							startTime = mdata.getElapsedTime() - peakjudgeSecond;	//ピーク判定の開始時間
							measurementDataList = new ArrayList<String>();
							measurementDataList.add(line);
							zeroranges.clear();
							zeroranges.addElement(value);
							logger.trace(String.format("  start [%s] Time:%.3f min:%.6f max:%.6f abs(max-min):%.6f boundary:%.6f", targetAxisString, startTime, min, max, Math.abs(max-min), boundary.getBoundary()));
						}
					}
				}
				// 検出中の場合
				else{
					measurementDataList.add(line);
					// 終了判定期間の更新(FIFO)
					if (zeroranges.getNumElements() > enddurationNum){
						zeroranges.addElementRolling(value);
					}else{
						zeroranges.addElement(value);
					}

					// 検出時間の下限値を上回っていれば、終了判定を行う
					if ((mdata.getElapsedTime() - startTime) > detectionConfig.getMinDuration()){
						// 検出終了フラグ
						boolean bEnd = false;
						double rms = 0.0;
						double mean = 0.0;
						double stdev = 0.0;

						// 検出を行う際の検出時間の上限値を超えていたら、1件の検出処理を終了する
						if ((mdata.getElapsedTime() - startTime) + Constants.DEPS > detectionConfig.getMaxDuration()){
							bEnd = true;
						}
						// 終了判定期間に達している場合、終了判定を行う
						else if (zeroranges.getNumElements() >= enddurationNum){
							rms = Math.sqrt(StatUtils.sumSq(zeroranges.getElements()) / (zeroranges.getNumElements()));	// 二乗平均平方根＝SQRT(二乗和/データ個数)
							mean = Math.abs(StatUtils.mean(zeroranges.getElements()));									// 平均
//							stdev = Math.sqrt(rms*rms-mean*mean);														// 標準偏差＝SQRT(二乗平均平方根^2 - 平均^2)
//							stdev = Math.sqrt(StatUtils.populationVariance(zeroranges.getElements()));					// 標準偏差＝SQRT(母分散)
							stdev = Math.sqrt(StatUtils.variance(zeroranges.getElements()));							// 標本標準偏差＝SQRT(標本分散)

							// ほぼZeroとみなせる場合、1件の検出処理を終了する
							if (stdev < detectionConfig.getNearzero()){
								bEnd = true;
							}
						}

						if (bEnd){
							logger.trace(String.format("  end   [%s] Time:%.3f Duration:%.3f(s) RMS:%.8f MEAN:%.8f STDEV:%.8f", targetAxisString, mdata.getElapsedTime(), mdata.getElapsedTime()-startTime, rms, mean, stdev));
							retValue.add(new RegionData(targetAxisString, measureStartTime, mdata.getNodeNo(), startTime, mdata.getElapsedTime(), detectionConfig.getPrebuffer()));
							startTime = -1.0;
							peakToPeak.clear();
							zeroranges.clear();
							measurementDataList = null;
						}
					}
				}
			}

			// ファイル終了時に検出中となっていた場合、検出対象とする
			if (startTime > 0.0 && (mdata.getElapsedTime() - startTime) > detectionConfig.getMinDuration()){
				retValue.add(new RegionData(targetAxisString, measureStartTime, mdata.getNodeNo(), startTime, mdata.getElapsedTime(), detectionConfig.getPrebuffer()));
				startTime = -1.0;
				measurementDataList = null;
			}
		} catch (IOException e) {
			logger.error("IOException",  e);
		}

		return retValue;
	}
}
