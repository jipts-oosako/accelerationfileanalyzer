package jp.kougiken.sensing.acceleration.fileanalyzer.gui;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jp.kougiken.sensing.acceleration.common.CmnFunc;
import jp.kougiken.sensing.acceleration.common.SystemConfigurations;
import jp.kougiken.sensing.acceleration.fileanalyzer.featurepointsearch.logic.FeaturePointSearch;
import jp.kougiken.sensing.acceleration.fileanalyzer.timedomainextraction.logic.TimeDomainExtraction;

public class MainFrame extends JFrame {
	private static final Logger logger = LogManager.getLogger();

	private static final String frameTitle = "加速度データの検索・抽出ツール";
	private static final String fileChooseTitleTDE = "設定ファイル選択ダイアログ（時間指定の領域抽出）";
	private static final String fileChooseTitleFPS = "設定ファイル選択ダイアログ（特徴領域検索）";
	private static final String filterTDE = "extractconfig.xml";
	private static final String filterFPS = "searchconfig.xml";

	private static final int frameLeft = 50;
	private static final int frameTop = 50;

	private JPanel contentPane;
	private JTextField txtSettingFileTDE;
	private JTextField txtSettingFileFPS;
	private JProgressBar progressBarTDE;
	private JProgressBar progressBarFPS;
	private JTextField textpgTDE;
	private JTextField textpgFPS;
	private JTextPane textInfomation;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Throwable e) {
			e.printStackTrace();
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
	                // EDTに例外ハンドラーを設定し、ErrorとRuntimeException を受け取る
	                Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
	                    public void uncaughtException(Thread t, Throwable e) {
	                        ShowDialog.showErrorDialog(e);
	                        System.exit(1);
	                    }
	                });
	                MainFrame frame = new MainFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MainFrame() {

		logger.info("SystemInfomation read START.");
		logger.info(CmnFunc.getSystemInfomation());
		logger.info("SystemInfomation read END.\n");

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle(frameTitle);
		setBounds(frameLeft, frameTop, 650, 650);

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		String settingfileTDE = SystemConfigurations.getInstance().getSettingfiletde();
		String settingfileFPS = SystemConfigurations.getInstance().getSettingfilefps();

		JPanel panelTDE = new JPanel();
		panelTDE.setBounds(12, 267, 610, 220);
		panelTDE.setBorder(new TitledBorder("時間指定の領域抽出"));
		contentPane.add(panelTDE);

		txtSettingFileTDE = new JTextField(settingfileTDE);
		txtSettingFileTDE.setBounds(80, 89, 445, 19);
		txtSettingFileTDE.setEditable(false);
		txtSettingFileTDE.setColumns(10);

		JButton btnSettingSelectTDE = new JButton(new FileChooseBtnAction(fileChooseTitleTDE, filterTDE, txtSettingFileTDE));
		btnSettingSelectTDE.setBounds(528, 86, 64, 24);

		JLabel lblSettingFileTDE = new JLabel("設定ファイル");
		lblSettingFileTDE.setBounds(12, 92, 57, 13);

		JButton btnRunTDE = new JButton("抽出実行");
		btnRunTDE.setBounds(263, 118, 105, 27);
		btnRunTDE.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try{
					textInfomation.setText("");

					TimeDomainExtraction swTDE = new TimeDomainExtraction(txtSettingFileTDE.getText(), progressBarTDE, textpgTDE, btnRunTDE);
					swTDE.execute();
				}catch(ConfigurationException ex){
					logger.error("時間指定の領域抽出 - 設定ファイル読み込みエラー",  ex);
					textInfomation.setText("時間指定の領域抽出 - 設定ファイル読み込みエラー");
				}catch(Exception ex){
					logger.error("時間指定の領域抽出 - 抽出実行イベント",  ex);
				}
			}
		});
		btnRunTDE.setFont(new Font("MS UI Gothic", Font.PLAIN, 18));
		panelTDE.setLayout(null);
		panelTDE.add(lblSettingFileTDE);
		panelTDE.add(txtSettingFileTDE);
		panelTDE.add(btnSettingSelectTDE);
		panelTDE.add(btnRunTDE);

		progressBarTDE = new JProgressBar();
		progressBarTDE.setBounds(80, 155, 512, 14);
		panelTDE.add(progressBarTDE);

		JLabel labelpgTDE = new JLabel("進捗状況");
		labelpgTDE.setBounds(12, 152, 50, 13);
		panelTDE.add(labelpgTDE);

		textpgTDE = new JTextField();
		textpgTDE.setEditable(false);
		textpgTDE.setBounds(12, 178, 580, 36);
		panelTDE.add(textpgTDE);
		textpgTDE.setColumns(10);

		JTextPane txtpnextractconfigxml = new JTextPane();
		txtpnextractconfigxml.setText("計測ファイルから、指定した時間帯のデータを抽出します。\r\n複数の計測ファイルを対象にした場合、全計測ファイルから、同じ時間帯のデータを抽出できます。\r\nデータ抽出は、「extractconfig.xml」ファイルに記載した抽出条件を元に行います。");
		txtpnextractconfigxml.setBackground(SystemColor.controlHighlight);
		txtpnextractconfigxml.setEditable(false);
		txtpnextractconfigxml.setBounds(12, 16, 580, 65);
		panelTDE.add(txtpnextractconfigxml);

		JPanel panelFPS = new JPanel();
		panelFPS.setBounds(12, 40, 610, 220);
		panelFPS.setBorder(new TitledBorder("特徴領域の検索"));
		contentPane.add(panelFPS);

		JLabel lblSettingFileFPS = new JLabel("設定ファイル");
		lblSettingFileFPS.setBounds(12, 92, 57, 13);

		txtSettingFileFPS = new JTextField(settingfileFPS);
		txtSettingFileFPS.setBounds(80, 89, 445, 19);
		txtSettingFileFPS.setEditable(false);
		txtSettingFileFPS.setColumns(10);

		JButton btnSettingSelectFPS = new JButton(new FileChooseBtnAction(fileChooseTitleFPS, filterFPS, txtSettingFileFPS));
		btnSettingSelectFPS.setBounds(528, 86, 64, 24);

		JButton btnRunFPS = new JButton("検索実行");
		btnRunFPS.setBounds(264, 118, 105, 27);
		btnRunFPS.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try{
					textInfomation.setText("");

					FeaturePointSearch swFPS = new FeaturePointSearch(txtSettingFileFPS.getText(), progressBarFPS, textpgFPS, btnRunFPS);
					swFPS.execute();
				}catch(ConfigurationException ex){
					logger.error("特徴領域の検索 - 設定ファイル読み込みエラー",  ex);
					textInfomation.setText("特徴領域の検索 - 設定ファイル読み込みエラー");
				}catch(Exception ex){
					logger.error("特徴領域の検索 - 検索実行イベント",  ex);
				}
			}
		});
		btnRunFPS.setFont(new Font("MS UI Gothic", Font.PLAIN, 18));
		panelFPS.setLayout(null);
		panelFPS.add(lblSettingFileFPS);
		panelFPS.add(txtSettingFileFPS);
		panelFPS.add(btnSettingSelectFPS);
		panelFPS.add(btnRunFPS);

		JLabel labelpgFPS = new JLabel("進捗状況");
		labelpgFPS.setBounds(12, 156, 50, 13);
		panelFPS.add(labelpgFPS);

		progressBarFPS = new JProgressBar();
		progressBarFPS.setBounds(80, 155, 512, 14);
		panelFPS.add(progressBarFPS);

		textpgFPS = new JTextField();
		textpgFPS.setEditable(false);
		textpgFPS.setBounds(12, 178, 580, 36);
		panelFPS.add(textpgFPS);
		textpgFPS.setColumns(10);

		JTextPane txtpnA = new JTextPane();
		txtpnA.setEditable(false);
		txtpnA.setBackground(SystemColor.controlHighlight);
		txtpnA.setText("加速度データの計測ファイルから、境界値として設定した加速度を超過した部分”(特徴領域)”を検索します。\r\nデータ検索は、「searchconfig.xml」ファイルに記載した検索条件を元に行います。");
		txtpnA.setBounds(12, 16, 580, 65);
		panelFPS.add(txtpnA);

		textInfomation = new JTextPane();
		textInfomation.setBackground(SystemColor.info);
		textInfomation.setEditable(false);
		textInfomation.setBounds(13, 488, 610, 89);
		JScrollPane scrollpane = new JScrollPane(textInfomation);
		scrollpane.setBounds(13, 518, 610, 89);
		contentPane.add(scrollpane);

		JLabel labelInfomation = new JLabel("情報通知");
		labelInfomation.setBounds(12, 497, 91, 13);
		contentPane.add(labelInfomation);

		JButton btnEnvCheck = new JButton("環境確認(Debug用)");
		btnEnvCheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textInfomation.setText(CmnFunc.getSystemInfomation());
			}
		});
		btnEnvCheck.setBounds(484, 493, 138, 21);
		contentPane.add(btnEnvCheck);

		JLabel lblCopyright = new JLabel("鋼橋技術研究会／センシング技術を用いた構造評価に関する研究部会 WG3");
		lblCopyright.setHorizontalAlignment(SwingConstants.RIGHT);
		lblCopyright.setBounds(230, 10, 392, 13);
		contentPane.add(lblCopyright);

		addWindowListener(new CloseListener(this));
	}

	/**
	 * 「×」ボタンが押された時のイベント
	 */
	private class CloseListener extends WindowAdapter {
		@SuppressWarnings("unused")
		private Component parentComponent;

		private CloseListener(Component parentComponent) {
			this.parentComponent = parentComponent;
		}

		public void windowClosing(WindowEvent e) {
			try {
				SystemConfigurations config = SystemConfigurations.getInstance();
				if (config != null){
					textInfomation.setText(config.toString());
					config.setSettingfiletde(txtSettingFileTDE.getText());
					config.setSettingfilefps(txtSettingFileFPS.getText());
					config.write();
				}

			} catch (ConfigurationException e1) {
				logger.error("windowClosing", e1);
				e1.printStackTrace();
			}
		}
	}

	/**
	 * ファイル選択ダイアログ表示アクション
	 */
	private class FileChooseBtnAction extends AbstractAction {
		private String dialogTitle = "ファイル選択ダイアログ";
		private String fileFilter = ".xml";
		private JTextField txtField = null;

		private FileChooseBtnAction(String dialogTitle, String fileFilter, JTextField txtField) {
			super("選択...");
			this.dialogTitle = dialogTitle;
			this.fileFilter = fileFilter;
			this.txtField = txtField;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			String pathname = txtField.getText();
			if (pathname.isEmpty()) pathname = System.getProperty("user.dir");

			JFileChooser chooser = new JFileChooser(new File(pathname).getParent());
			chooser.setFileFilter(new TextFilter(fileFilter));
			chooser.setDialogTitle(dialogTitle);
			int answer = chooser.showOpenDialog(null);
			if (answer == JFileChooser.APPROVE_OPTION && txtField != null) {
				txtField.setText(chooser.getSelectedFile().getAbsolutePath());
			}
		}
	}

	/**
	 * ファイル選択ダイアログのフィルター
	 */
	private class TextFilter extends FileFilter {
		private String fileFilter = ".xml";

		private TextFilter(String fileFilter) {
			this.fileFilter = fileFilter;
		}

		@Override
		public boolean accept(File file) {
			if (file.isDirectory() || file.getName().endsWith(fileFilter)) {
				return true;
			}

			return false;
		}

		@Override
		public String getDescription() {
			return "設定ファイル("+fileFilter+")";
		}
	}

	class AnalyzeTask extends SwingWorker<String, Object> {

		public AnalyzeTask(){

		}

		@Override
		public String doInBackground() {
			return "Done";
		}

		@Override
		protected void done() {
			try {
			} catch (Exception ignore) {
			}
		}
	}
}
