package jp.kougiken.sensing.acceleration.common;

import org.kohsuke.args4j.Option;

public class Args {
	// name		:パラメータ名称
	// aliases	:別名
    // metaVar	:usageの表示文字
	// required	:必須(=true)
	// usage	:説明

	/**
	 * コマンドライン引数 - 抽出設定ファイルの絶対パス
	 */
    @Option(name = "-C", aliases = {"--config" }, metaVar = "ConfigFilePath", required = true, usage = "抽出設定ファイルの絶対パス")
    private String configPath;

    public String getConfigPath() {
		return configPath;
	}
}
