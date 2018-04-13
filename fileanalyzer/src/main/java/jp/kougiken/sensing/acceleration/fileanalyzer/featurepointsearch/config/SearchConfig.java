package jp.kougiken.sensing.acceleration.fileanalyzer.featurepointsearch.config;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration2.ConfigurationUtils;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jp.kougiken.sensing.acceleration.common.AppMessages;
import jp.kougiken.sensing.acceleration.common.Constants;

/**
 * Search condition - 検索条件の設定
 */
public class SearchConfig {
    private static final Logger logger = LogManager.getLogger();

	/** 計測ファイルの条件 */
	/** 計測データの格納ディレクトリ */
	private Path measurementdatadir;

	/** 対象とする計測ファイルのファイルパターン */
	private String filepattern;

	/** 計測開始時刻 */
	private LocalDateTime starttime;

	/** 計測周波数(Hz) */
	private double frequency;

	/** 特徴領域の検出条件 */
	private DetectionConfig detectionConfig = new DetectionConfig();
//	private List<DetectionConfig> detectionConfig = new ArrayList<DetectionConfig>();

	/** 検出結果の出力ディレクトリ(計測データの格納ディレクトリを基準とした相対パス) */
	private Path outputExtractdir;

    /**
	 * コンストラクタ
	 * @throws ConfigurationException
	 */
	public SearchConfig() {
	}

	/**
	 * Configfile読み込み
	 * @throws Exception
	 */
	public void readConfig(String configfile) throws ConfigurationException {
		XMLConfiguration config = new Configurations().xml(configfile);
		logger.info(String.format("configfile read [%s].", configfile));
		logger.info("\n"+ConfigurationUtils.toString(config));

		// データ読み込みと整合性確認
		String sdir = config.getString("measurement.datadir");
		if (sdir.isEmpty()) sdir = Paths.get(configfile).toFile().getParent();
		measurementdatadir = Paths.get(sdir).toAbsolutePath();
		if (!(Files.exists(measurementdatadir) && Files.isWritable(measurementdatadir))){
			throw new ConfigurationException(AppMessages.ERROR.get("ERR001"));
		}

		// 対象とする計測ファイルのファイルパターン
		filepattern = config.getString("measurement.filepattern");
		if (filepattern.isEmpty()){
			throw new ConfigurationException(AppMessages.ERROR.get("ERR002"));
		}

		// 計測開始時刻
		String sTime = config.getString("measurement.starttime");
		try {
			starttime = LocalDateTime.parse(sTime, Constants.baseFormaterMs);
		} catch (DateTimeParseException e) {
			throw e;
		}
		// 計測周波数(Hz)
		frequency = config.getDouble("measurement.frequency");

		// 特徴領域の検出条件
		detectionConfig.readConfig(config.configurationAt("detection"));

		// 検出結果の出力ディレクトリ(計測データの格納ディレクトリを基準とした相対パス)
		String sOutDir = config.getString("output.dirname");
		if (sOutDir.isEmpty()){
			throw new ConfigurationException(AppMessages.ERROR.get("ERR005"));
		}
		outputExtractdir = measurementdatadir.resolve(sOutDir);

		logger.info("configfile read ok.");

	}

	/**
	 * 計測データの格納ディレクトリ
	 * @return measurementdatadir
	 */
	public Path getMeasurementdatadir() {
		return measurementdatadir;
	}

	/**
	 * 対象とする計測ファイルのファイルパターン
	 * @return filepattern
	 */
	public String getFilepattern() {
		return filepattern;
	}

	/**
	 * 計測開始時刻
	 * @return starttime
	 */
	public LocalDateTime getStarttime() {
		return starttime;
	}

	/**
	 * 計測周波数(Hz)
	 * @return frequency
	 */
	public double getFrequency() {
		return frequency;
	}

	/**
	 * 特徴領域の検出条件
	 * @return detectionConfig
	 */
	public DetectionConfig getDetectionConfig() {
		return detectionConfig;
	}

	/**
	 * 検出結果の出力ディレクトリ(計測データの格納ディレクトリに生成し、結果を出力する)
	 * @return extractdir
	 */
	public Path getOutputExtractdir() {
		return outputExtractdir;
	}

	/**
	 * 計測データの格納ディレクトリから、対象とする計測ファイルのファイルパターンに
	 * 合致するファイルの一覧を取得する
	 * @return filelist
	 */
	public List<File> getTargetDataList() {
    	Pattern ptn = Pattern.compile(filepattern);


		File[] files = measurementdatadir.toFile().listFiles(
				new FilenameFilter() {
				    public boolean accept(File dir, String name) {
				    	Matcher m = ptn.matcher(name);
				        if (m.matches()) {
				            return true;
				        } else {
				            return false;
				        }
				    }
		});

		return Arrays.asList(files);
	}
}
