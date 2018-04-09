package jp.kougiken.sensing.acceleration.fileanalyzer.timedomainextraction.config;

import java.time.format.DateTimeParseException;
import java.util.List;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.ImmutableNode;

import jp.kougiken.sensing.acceleration.common.AppMessages;

/**
 * 計測データの特徴
 */
public class CharacteristicConfig {

	/** ゼロ点からのずれの平均値(g) */
	double zerocorrections[] = {0.0, 0.0, 0.0};

	/**
	 * コンストラクタ
	 */
	public CharacteristicConfig(){

	}

	/**
	 * Configfileの指定されたノード(characteristic)から読み取り
	 * */
	public void readConfig(HierarchicalConfiguration<ImmutableNode> node)
			throws ConfigurationException {

		if (node == null) return;

		// ゼロ点からのずれの平均値(g)
		try {
			List<HierarchicalConfiguration<ImmutableNode>> subNode = node.configurationsAt("zerocorrection");
			for (HierarchicalConfiguration<ImmutableNode> item : subNode){
				double value = item.getDouble(".");
				int axis = item.getInt(".[@axis]");
				this.zerocorrections[axis] = value;
			}
		} catch (DateTimeParseException e) {
			throw new ConfigurationException(String.format(AppMessages.ERROR.get("ERR009")));
		}
	}
	/**
	 * 指定軸における、ゼロ点からのずれの平均値を取得する
	 * @return ゼロ点からのずれの平均値(g)[0:X軸, 1:Y軸, 2:Z軸]
	 */
	public double[] getZerocorrections() {
		return zerocorrections;
	}
}
