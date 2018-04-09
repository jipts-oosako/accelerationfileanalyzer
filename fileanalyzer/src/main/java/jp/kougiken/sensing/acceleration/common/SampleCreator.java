package jp.kougiken.sensing.acceleration.common;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Random;

public class SampleCreator {
	public static void main(String[] args) {
		Create();
	}

	public static void Create(){
		String TEST_DATAFILE =  "E:/workspaceKgk/accelerationfileanalyzer/data/create_sample.dat";
		Random rnd = new Random();
		int maxlen = 100000000;

		// 書き込み専用のFileChannelオブジェクト
		try (FileOutputStream outStream = new FileOutputStream(TEST_DATAFILE)){
			try (FileChannel outCh = outStream.getChannel()){
				for (int i=0; i<maxlen; i++){
					double dx = rnd.nextDouble();
					outCh.write(ByteBuffer.wrap(String.format("%d %.2f %.6f %.6f %.6f\n", 3, 0.01*i, -dx, -(dx * 1.5), -(dx * 2.0)).getBytes()));
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
