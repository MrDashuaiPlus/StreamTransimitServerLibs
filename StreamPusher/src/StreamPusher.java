import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;

public class StreamPusher {
	static Process process;
	static String logTag;
	static String bgMusicPath;
	static String bgMusicListPath;
	static String bgMusicMD5Code;
	static String OutputAudioFormat;

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				// TODO Auto-generated method stub
				System.out.print("被杀了！");
				if (process != null && process.isAlive()) {
					process.destroy();
				}
			}
		});

		String command = args[0];
		if (args.length > 1)
			logTag = args[1];
		if (args.length > 2)
			bgMusicPath = args[2];
		if(args.length>3)
			OutputAudioFormat= args[3];
		createBgMusicListFile();
		int index1 = command.indexOf("exe");
		if (index1 == -1) {
			int index2 = command.indexOf("ffmpeg");
			if (index2 != -1) {
				System.out.println("index2:" + index2);
				if(OutputAudioFormat!=null){
					command = command.substring(0, index2 + 6)
							+ " -f concat -i " + bgMusicListPath + " -c:a "+OutputAudioFormat+" -an "
							+ command.substring(index2 + 6);
				}else {
					command = command.substring(0, index2 + 6)
							+ " -f concat -i " + bgMusicListPath + " "
							+ command.substring(index2 + 6);
				}
			}
		} else {
			if(OutputAudioFormat!=null){
				command = command.substring(0, index1 + 3) + " -f concat -i "
						+ bgMusicListPath + " -c:a "+OutputAudioFormat+" -an " + command.substring(index1 + 3);
			}else {
				command = command.substring(0, index1 + 3) + " -f concat -i "
						+ bgMusicListPath + " " + command.substring(index1 + 3);
			}
		}

		process(command);

		process.destroy();
	}

	public static void process(String command) {
		try {
		
			System.out.println("ffmpeg命令为：" + command);

			process = Runtime.getRuntime().exec(command);
			System.out.println("process:" + process.toString());

			// 获取进程的标准输入流
			final InputStream is1 = process.getInputStream();
			// 获取进城的错误流
			final InputStream is2 = process.getErrorStream();
			// 启动两个线程，一个线程负责读标准输出流，另一个负责读标准错误流
			new Thread() {
				public void run() {
					BufferedReader br1 = new BufferedReader(
							new InputStreamReader(is1));
					try {
						String line1 = null;
						while ((line1 = br1.readLine()) != null) {
							if (line1 != null) {
								System.out.println(logTag + " : " + line1);
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						try {
							is1.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}.start();

			new Thread() {
				public void run() {
					BufferedReader br2 = new BufferedReader(
							new InputStreamReader(is2));
					try {
						String line2 = null;
						while ((line2 = br2.readLine()) != null) {
							if (line2 != null) {
								System.out.println(logTag + " : " + line2);
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						try {
							is2.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}.start();
			process.waitFor();
			process.destroyForcibly();
			System.out.println("视频流断了...");
			process(command);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.toString());

			try {
				process.getErrorStream().close();
				process.getInputStream().close();
				process.getOutputStream().close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

	}

	public static void createBgMusicListFile() throws FileNotFoundException {
		String classPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
		String rootPath = classPath.substring(1,classPath.indexOf("StreamTransimitServer")+22);
		rootPath=rootPath.replace("/", "\\");
		File file = new File(rootPath+"MusicLinks");
		if (!file.exists() && !file.isDirectory()) {
			file.mkdir();
			File file1 = new File(bgMusicPath);
			if (file1.exists()) {
				bgMusicMD5Code = getMd5ByFile(file1);
				System.out.println("MD5为：" + bgMusicMD5Code);
				bgMusicListPath = rootPath+"MusicLinks\\" + bgMusicMD5Code + ".txt";
				File listFile = new File(bgMusicListPath);
				if (!listFile.exists()) {
					writerMusicListTxt();
				}
			}
		} else {
			File file1 = new File(bgMusicPath);
			if (file1.exists()) {
				bgMusicMD5Code = getMd5ByFile(file1);
				System.out.println("MD5为：" + bgMusicMD5Code);
				bgMusicListPath = rootPath+"MusicLinks\\" + bgMusicMD5Code + ".txt";
				File listFile = new File(bgMusicListPath);
				if (!listFile.exists()) {
					writerMusicListTxt();
				}
			}
		}
	}

	public static void writerMusicListTxt() {
		BufferedWriter fw = null;
		try {
			File file = new File(bgMusicListPath);
			fw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file, true), "UTF-8")); // 指定编码格式，以免读取时中文字符异常
			fw.append("file '" + bgMusicPath + "'");
			for (int i = 0; i < 10000; i++) {
				fw.newLine();
				fw.append("file '" + bgMusicPath + "'");
			}
			fw.flush(); // 全部写入缓存中的内容
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void writerNewPidListTxt(String pidfilePath,int pid) {
		BufferedWriter fw = null;
		try {
			File file = new File(pidfilePath);
			fw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file, true), "UTF-8")); // 指定编码格式，以免读取时中文字符异常
			fw.append(pid+";");
			fw.flush(); // 全部写入缓存中的内容
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static String getMd5ByFile(File file) throws FileNotFoundException {
		String value = null;
		FileInputStream in = new FileInputStream(file);
		try {
			MappedByteBuffer byteBuffer = in.getChannel().map(
					FileChannel.MapMode.READ_ONLY, 0, file.length());
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(byteBuffer);
			BigInteger bi = new BigInteger(1, md5.digest());
			value = bi.toString(16);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != in) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return value;
	}


}
