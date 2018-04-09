package jp.kougiken.sensing.acceleration.fileanalyzer.featurepointsearch.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.ImmutableNode;

import jp.kougiken.sensing.acceleration.common.Constants;

/**
 * 特徴領域の検出条件
 */
public class DetectionConfig {
	/** 0と見なす値(gal)(正負共通) */
	private double nearzero;
	/** 検出終了条件(0と見なす値の継続時間(s))) */
	private double endduration;
	/** 検出を行う際の検出時間の下限値(s) */
	private double minDuration;
	/** 検出を行う際の検出時間の上限値(s) */
	private double maxDuration;
	/** 検出直前に割り込ませる余裕時間(s) */
	private double prebuffer;

	/** 各軸の検出境界値 */
	private List<BoundaryConfig> axisBoundarys = Collections.synchronizedList(new ArrayList<BoundaryConfig>());
	/** コンストラクタ */
	public DetectionConfig(){

	}

	/**
	 * Configfileの指定されたサブノード(detection)から読み取り
	 * */
	public void readConfig(HierarchicalConfiguration<ImmutableNode> subNode)
			throws ConfigurationException {

		// 0と見なす値(gal)(正負共通)
		nearzero = Math.abs(subNode.getDouble("nearzero"));

		// 検出終了条件(0と見なす値の継続時間(s)))
		endduration = Math.abs(subNode.getDouble("endduration"));

		// 検出を行う際の検出時間の下限値(s)
		minDuration = Math.abs(subNode.getDouble("minduration"));

		// 検出を行う際の検出時間の上限値(s)
		maxDuration = Math.abs(subNode.getDouble("maxduration"));

		// 検出直前に割り込ませる余裕時間(s)
		prebuffer = Math.abs(subNode.getDouble("prebuffer"));

		// 検出境界値(gal)(正負共通)
		axisBoundarys.add(new BoundaryConfig(Constants.AXIS_X, subNode.getDouble("boundary[@axis_x]")));
		axisBoundarys.add(new BoundaryConfig(Constants.AXIS_Y, subNode.getDouble("boundary[@axis_y]")));
		axisBoundarys.add(new BoundaryConfig(Constants.AXIS_Z, subNode.getDouble("boundary[@axis_z]")));
	}

	/**
	 * 0と見なす値(gal)(正負共通)
	 * @return nearzero
	 */
	public double getNearzero() {
		return nearzero;
	}

	/**
	 * 検出終了条件(0と見なす値の継続時間(s))
	 * @return endduration
	 */
	public double getEndduration() {
		return endduration;
	}

	/**
	 * 検出を行う際の検出時間の下限値(s)
	 * @return 最低検出時間(s)
	 */
	public double getMinDuration() {
		return minDuration;
	}

	/**
	 * 検出を行う際の検出時間の上限値(s)
	 * @return 最長検出時間(s)
	 */
	public double getMaxDuration() {
		return maxDuration;
	}

	/**
	 * 検出直前に割り込ませる余裕時間(s)
	 * @return 検出前余裕時間(s)
	 */
	public double getPrebuffer() {
		return prebuffer;
	}

	/**
	 * 各軸の検出境界値をListとして取得
	 * @return 各軸の検出境界値(gal)
	 */
	public List<BoundaryConfig> getAxisBoundarys() {
		return axisBoundarys;
	}

	/**
	 * 条件を文字列で返す
	 *
	 */
	public String toString(){

		String boudaryStr = axisBoundarys.stream().map(x -> x.toString()).collect(Collectors.joining(", "));
		String ret = String.format("0と見なす値:[%.6f(gal)]、検出終了の0継続時間:[%.3f(s)]、検出下限・上限時間:[%.3f～%.3f(s)]、検出前余裕時間:[%.3f(s)]、各軸の検出境界値:[%s]",
				nearzero, endduration, minDuration, maxDuration, prebuffer, boudaryStr);
		return ret;
	}
}
