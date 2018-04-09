package jp.kougiken.sensing.acceleration.common;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.nio.file.Path;

import org.apache.commons.codec.digest.DigestUtils;

public class CmnFunc {

	/**
	 * 秒->ナノ秒 ※ミリ秒で丸める
	 * @param second
	 * @return
	 */
	public static double secondToNano(double second){return Math.round(second*1000.0)*1000000.0;};
	/**
	 * ナノ秒->秒 ※ミリ秒で丸める
	 * @param nano
	 * @return
	 */
	public static double nanoToSecond(double nano){return Math.round(nano/1000000.0)/1000.0;};

	/**
	 * 同値判定
	 * @param lhs
	 * @param rhs
	 * @return
	 */
	public static boolean equals(double lhs, double rhs){return Math.abs(rhs - lhs) < Constants.DEPS;};

	/**
	 * 出力ディレクトリを開く
	 * @param outputDir
	 */
	public static void openOutputDir(Path outputDir) {
		try {
			Runtime rt = Runtime.getRuntime();
			String cmd = "explorer " + outputDir.toString();
			rt.exec(cmd);
		} catch (IOException e) {
		}
//		ProcessBuilder pb = new ProcessBuilder("C:\\Program Files\\Internet Explorer\\iexplore.exe");
//		pb.start();
	}

	/**
	 * ファイルのMD5ハッシュコードを取得する
	 * @param filepath
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static String getFileHashcode(Path filepath) throws FileNotFoundException, IOException {
		String ret = "";
		try (FileInputStream fis = new FileInputStream(filepath.toFile())){
			ret = DigestUtils.md5Hex(fis);
		}
		return ret;

	}
	/**
	 * システム状態を取得する
	 */
	public static String getSystemInfomation() {
		String msg = "";
        String progVersion = CmnFunc.class.getPackage().getImplementationVersion();
		msg += String.format("ProgVersion:%s\n", progVersion);
		msg += String.format("JavaVersion:%s\n", System.getProperty("java.version"));
		msg += String.format("OSVersion  :%s\n", System.getProperty("os.name"));
		msg += String.format("OSArchitect:%s\n", System.getProperty("os.arch"));
		msg += String.format("CPUCore    :%d\n", Runtime.getRuntime().availableProcessors());

		MemoryUsage memoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
		long memInit = memoryUsage.getInit()/1024/1024;			// 起動中にJava仮想マシンがオペレーティング・システムから要求するメモリー管理のための初期メモリー量
		long memUsed = memoryUsage.getUsed()/1024/1024;			// 現在使用されているメモリーの量(バイト単位)を表す
		long memComm = memoryUsage.getCommitted()/1024/1024;	// Java仮想マシンが使用できることが保証されているメモリーの量
		long memMax = memoryUsage.getMax()/1024/1024;			// メモリー管理に使用できる最大メモリー量
		msg += String.format("MemoryForJVM(Init/Used/Committed/Max):%d/%d/%d/%d (MB)\n", memInit, memUsed, memComm, memMax);


		SystemConfigurations sysconfig = SystemConfigurations.getInstance();
		if (sysconfig != null){
			msg += String.format("System設定:\n%s", sysconfig.toString());
		}else{
			msg += "ERROR システム設定ファイルが見つかりません。";
		}
		return msg;
	}
}
