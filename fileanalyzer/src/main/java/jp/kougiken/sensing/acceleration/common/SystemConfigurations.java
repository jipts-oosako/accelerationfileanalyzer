package jp.kougiken.sensing.acceleration.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * システム コンフィグファイルの読み込み
 */
public class SystemConfigurations {
	private static SystemConfigurations singleton = null;

	/**
	 * インスタンス取得
	 * @throws ConfigurationException
	 */
    public static SystemConfigurations getInstance(){
    	if (singleton == null){
    		singleton = new SystemConfigurations();
    	}
        return singleton;
    }

    private static final Logger logger = LogManager.getLogger();

	private static final String DEFAULT_CONFIGFILE = "systemconfig.xml";

	// システム設定ファイル
	private File configfile;
	// システム設定ファイルの内容
	private XMLConfiguration config;

	/** 計測ファイルの項目の区切り文字 */
	private String separator;

	/** 並列処理の実行可否 */
	private boolean parallelProcessing;

	/** 時間指定の領域抽出の設定ファイル */
	private String settingfiletde = "";

	/** 特徴領域の検索の設定ファイル */
	private String settingfilefps = "";

	/**
	 * コンストラクタ
	 */
	private SystemConfigurations() {
		this(DEFAULT_CONFIGFILE);
	}

	/**
	 * コンストラクタ
	 * @throws ConfigurationException
	 */
	protected SystemConfigurations(String configfile) {
		this.configfile = new File(configfile);

		try{
			logger.info("configfile loading:{} exist:{}", this.configfile.getAbsolutePath(), this.configfile.exists());

			config = new Configurations().xml(this.configfile);
			// 計測ファイルの項目の区切り文字
			separator = config.getString("system.measurementfile.separator");
			if (separator.isEmpty()){
				throw new ConfigurationException(AppMessages.SYSTEM.get("SYS001"));
			}

			// 並列処理の実行可否
			parallelProcessing = config.getBoolean("system.processing.parallel");

			// 時間指定の領域抽出の設定ファイル
			settingfiletde = config.getString("system.guisetting.settingfiletde");

			// 特徴領域の検索の設定ファイル
			settingfilefps = config.getString("system.guisetting.settingfilefps");

			logger.info("configfile read OK.");

			logger.info("Acceleration File Analyzer Start.");
		} catch (ConfigurationException e) {
			logger.error(e.toString());
			logger.error("configfile read NG.");
		}
	}

	/**
	 * 設定を書き込む
	 */
	public void write() throws ConfigurationException {
		try {
			// 時間指定の領域抽出の設定ファイル
			config.setProperty("system.guisetting.settingfiletde", settingfiletde);

			// 特徴領域の検索の設定ファイル
			config.setProperty("system.guisetting.settingfilefps", settingfilefps);

			//文字コードの指定ができない！
//			FileWriter fw = new FileWriter(this.configfile);

			try(FileOutputStream fo = new FileOutputStream(this.configfile);
				OutputStreamWriter ow = new OutputStreamWriter(fo, "UTF-8");){
				if (ow!=null) config.write(ow);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 計測ファイルの項目の区切り文字
	 * @return separator
	 */
	public String getSeparator() {
		return separator;
	}

	/**
	 *  並列処理の実行可否
	 *  @return 並列処理実行:true
	 */
	public boolean getParallelProcessing(){
		return parallelProcessing;
	}

	/**
	 * 時間指定の領域抽出の設定ファイル
	 * @return 設定ファイルの絶対パス
	 */
	public String getSettingfiletde() {
		return settingfiletde;
	}

	/**
	 * 特徴領域の検索の設定ファイル
	 * @return 設定ファイルの絶対パス
	 */
	public String getSettingfilefps() {
		return settingfilefps;
	}

	/**
	 * 時間指定の領域抽出の設定ファイルの絶対パスを設定する
	 * @param settingfiletde 設定ファイルの絶対パス
	 */
	public void setSettingfiletde(String settingfiletde) {
		this.settingfiletde = settingfiletde;
	}

	/**
	 * 特徴領域の検索の設定ファイルの絶対パスを設定する
	 * @param settingfilefps 設定ファイルの絶対パス
	 */
	public void setSettingfilefps(String settingfilefps) {
		this.settingfilefps = settingfilefps;
	}

	public String toString() {
		String ret = String.format("システム設定ファイル:[%s]\n"
								+  "区切り文字:[%s]\n"
								+  "並列処理:[%s]\n"
									, configfile.getAbsolutePath()
									, (separator.equals(" "))? "(半角スペース)" : separator
									, ((parallelProcessing)? "する": "しない"));
		return ret;
	}
}
