package jp.kougiken.sensing.acceleration.fileanalyzer.gui;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/*
 * Error message dialog
 */
 class ShowDialog {
    static void showErrorDialog(final Throwable t) {
        try {
            t.printStackTrace();
            // EDT以外で呼ばれる事も想定し、EDTで実行する。
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                	StringWriter stringWriter = new StringWriter();
                    PrintWriter printWriter = new PrintWriter( stringWriter );
                    t.printStackTrace( printWriter );

                    JOptionPane.showMessageDialog(
                            null,
                            stringWriter.toString(),
                            "ERROR",
                            JOptionPane.ERROR_MESSAGE);
                }
            });
        } catch (Throwable e) {
            // ダイアログ表示時はエラーは全て破棄
        }
    }
}