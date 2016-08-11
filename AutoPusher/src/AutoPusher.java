import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class AutoPusher {

	/**
	 * @param args
	 * @throws IOException
	 * @throws InterruptedException
	 */
	// static Process process;
	static String commBgMusic, serverPath;
	static ArrayList<HashMap<String, String>> cameraList = new ArrayList<HashMap<String, String>>();
	static ArrayList<Process> processes = new ArrayList<Process>();

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				// TODO Auto-generated method stub
				for (int i = 0; i < processes.size(); i++) {
					Process process = processes.get(i);
					process.destroyForcibly();
				}
				System.out.print("关闭了！");

			}
		});
		String classPath = Thread.currentThread().getContextClassLoader()
				.getResource("").getPath();
		String rootPath = classPath.substring(1,
				classPath.indexOf("StreamTransimitServer") + 22);
		File musicLinksDir =new File(rootPath + "MusicLinks");
		if(musicLinksDir.exists()&&musicLinksDir.isDirectory()){
			deleteDir(musicLinksDir);
		}
		String HLSServerPath = classPath.substring(1,
				classPath.indexOf("HLSServer") + 10);
		writeShutDownBat();
		parseXml();
//		File serverDir = new File(serverPath);
//		deleteDir(serverDir);
		for (int i = 0; i < cameraList.size(); i++) {
			HashMap<String, String> cameraHashMap = cameraList.get(i);
			String subCommand;
			String URL = cameraHashMap.get("URL");
			if (URL == null || URL.isEmpty()) {
				throw new Exception("Camera的URL不能为空！");
			}
			String OutputVideoFormat = cameraHashMap.get("OutputVideoFormat");
			if (OutputVideoFormat != null && !OutputVideoFormat.isEmpty()) {
				subCommand = "java -jar "
						+ HLSServerPath
						+ "core.jar "
						+ "\""
						+ rootPath
						+ "ffmpeg.exe -loglevel info -stimeout 5000000 -rtsp_transport tcp -re -i "
						+ URL
						+ " -c:v "
						+ OutputVideoFormat
						+ " -f hls -shortest -hls_time 5.0 -hls_list_size 5 -hls_wrap 5 ";
				// + serverPath.replace("/", "\\")+"";
			} else {
				subCommand = "java -jar "
						+ HLSServerPath
						+ "core.jar "
						+ "\""
						+ rootPath
						+ "ffmpeg.exe -loglevel info -stimeout 5000000 -rtsp_transport tcp -re -i "
						+ URL
						+ " -c:v copy -f hls -shortest -hls_time 5.0 -hls_list_size 5 -hls_wrap 5 ";
			}
			String name = cameraHashMap.get("name");
			String dirName = cameraHashMap.get("dirName");
			if (name != null && !name.isEmpty()) {
				File file;
				if (dirName != null && !dirName.isEmpty()) {
					file = new File(serverPath + dirName);
					subCommand = subCommand + serverPath.replace("/", "\\")
							+ dirName + "\\" + name + ".m3u8\"";
				} else {
					file = new File(serverPath + name);
					subCommand = subCommand + serverPath.replace("/", "\\")
							+ name + "\\" + name + ".m3u8\"";
				}

				System.out.println("subCommand：" + subCommand);
				if (!file.exists() || !file.isDirectory()) {
					file.mkdirs();
				}

			} else {
				throw new Exception("name不能为空");
			}
			String LogTag = cameraHashMap.get("LogTag");
			if (LogTag != null && !LogTag.isEmpty()) {
				subCommand = subCommand + " " + LogTag;
			} else {
				subCommand = subCommand + " 未定义";
			}
			String BgMusic = cameraHashMap.get("BgMusic");
			if (BgMusic != null && !BgMusic.isEmpty()) {
				String musicPath = rootPath + "MusicResource/" + BgMusic;
				subCommand = subCommand + " " + musicPath;
			} else if (commBgMusic != null && !commBgMusic.isEmpty()) {
				String musicPath = rootPath + "MusicResource/" + commBgMusic;
				subCommand = subCommand + " " + musicPath;
			} else {
				throw new Exception("没有指定背景音乐！");
			}
			String OutputAudioFormat = cameraHashMap.get("OutputAudioFormat");
			if (OutputAudioFormat != null && !OutputAudioFormat.isEmpty()) {
				subCommand = subCommand + " " + OutputAudioFormat;
			}

			subCommand = "cmd /k start " + subCommand;
			System.out.println("subCommand:" + subCommand);

			Process process = Runtime.getRuntime().exec(subCommand);
			processes.add(process);

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
								System.out.println(line1);
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
								System.out.println(line2);
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

		}

		// String teString
		// ="cmd /k start echo hahah&cmd /k start echo hahah&cmd /k start echo hahah";
		// command = command +" -encoding utf-8";
		// System.out.println("commandString:" + command);
		// writeCoreBat(command);
		// process = Runtime.getRuntime().exec(HLSServerPath + "core.bat");

	}

	private static void writeShutDownBat() throws IOException {
		int pid = getPid();
		String classPath = Thread.currentThread().getContextClassLoader()
				.getResource("").getPath();
		String HLSServerPath = classPath.substring(1,
				classPath.indexOf("HLSServer") + 10);
		HLSServerPath = HLSServerPath.replace("/", "\\");
		File file = new File(HLSServerPath + "shut_down.bat");
		if (file.exists()) {
			file.delete();
			writerNewShutDownTxt(HLSServerPath + "shut_down.bat", pid);
		} else {
			writerNewShutDownTxt(HLSServerPath + "shut_down.bat", pid);
		}
	}

	private static void writeCoreBat(String coreText) throws IOException {
		int pid = getPid();
		String classPath = Thread.currentThread().getContextClassLoader()
				.getResource("").getPath();
		String HLSServerPath = classPath.substring(1,
				classPath.indexOf("HLSServer") + 10);
		HLSServerPath = HLSServerPath.replace("/", "\\");
		File file = new File(HLSServerPath + "core.bat");
		if (file.exists()) {
			file.delete();
			writerNewCoreTxt(HLSServerPath + "core.bat", coreText);
		} else {
			writerNewCoreTxt(HLSServerPath + "core.bat", coreText);
		}
	}

	private static int getPid() {
		RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
		String name = runtime.getName(); // format: "pid@hostname"
		try {
			return Integer.parseInt(name.substring(0, name.indexOf('@')));
		} catch (Exception e) {
			return -1;
		}
	}

	public static void writerNewShutDownTxt(String pidfilePath, int pid) {
		BufferedWriter fw = null;
		try {
			File file = new File(pidfilePath);
			fw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file, true), "UTF-8")); // 指定编码格式，以免读取时中文字符异常
			fw.append("taskkill /f /T /pid " + pid);
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

	public static void writerNewCoreTxt(String pidfilePath, String coreText) {
		BufferedWriter fw = null;
		try {
			File file = new File(pidfilePath);
			fw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file, true), "GBK")); // 指定编码格式，以免读取时中文字符异常
			fw.append(coreText);
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

	private static void parseXml() throws Exception {
		// 1.读取XML文件,获得document对象
		String classPath = Thread.currentThread().getContextClassLoader()
				.getResource("").getPath();
		String HLSServerPath = classPath.substring(1,
				classPath.indexOf("HLSServer") + 10);
		SAXReader reader = new SAXReader();
		Document document = reader.read(new File(HLSServerPath + "config.xml"));
		Element root = document.getRootElement();
		commBgMusic = root.elementText("CommBgMusic");
		serverPath = root.elementText("ServerPath");
		if (serverPath == null && serverPath.isEmpty()) {
			throw new Exception("serverPath不能为空");
		}

		Element cameraListElement = root.element("CameraList");
		cameraList.clear();
		for (Iterator it = cameraListElement.elementIterator(); it.hasNext();) {
			Element camera = (Element) it.next();
			HashMap<String, String> cameraHashMap = new HashMap<String, String>();
			cameraHashMap.put("name", camera.elementText("name"));
			cameraHashMap.put("dirName", camera.elementText("dirName"));
			cameraHashMap.put("URL", camera.elementText("URL"));
			cameraHashMap.put("OutputVideoFormat",
					camera.elementText("OutputVideoFormat"));
			cameraHashMap.put("OutputAudioFormat",
					camera.elementText("OutputAudioFormat"));
			cameraHashMap.put("BgMusic", camera.elementText("BgMusic"));
			cameraHashMap.put("LogTag", camera.elementText("LogTag"));
			cameraList.add(cameraHashMap);
		}
	}

	private static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			// 递归删除目录中的子目录下
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		// 目录此时为空，可以删除
		return dir.delete();
	}
}
