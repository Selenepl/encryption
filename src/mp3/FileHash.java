package mp3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class FileHash {

	static int spc_count = -1;
	// CHANGE TO DIRECTORY YOU WANT ENCRYPTED FILES *******
	static String dirLoc = "D:\\Users\\Read\\Documents\\GitHub\\MP3_small" + "\\";
	static String dirName = "";
	static String bFileName = "";
	static MP3Encryption enc = null;
	// static String encContents = "";
	static byte[] encContents;
	static String decContents = "";
	static List<String> fileList = new ArrayList<String>();
	static List<String> dirList = new ArrayList<String>();

	static void Process(File aFile) {
		spc_count++;
		String spcs = "";
		for (int i = 0; i < spc_count; i++)
			spcs += " ";
		if (aFile.isFile()) {
			System.out.println(spcs + "[FILE] " + aFile.getName());

			dirList.add(aFile.getPath());
			try {
				bFileName = getMD5CheckSum(aFile.getPath());
				String contents = readFile(aFile.getPath(),
						Charset.defaultCharset());
				// encContents = enc.encrypt(contents);
				// decContents = enc.decrypt(encContents)
				// System.out.println(contents);
				fileList.add(bFileName);
				File bFile = new File(dirLoc + dirName + "\\" + bFileName);

				// copyFile(aFile, bFile);
				PrintWriter out = new PrintWriter(bFile.getPath());
				out.println(contents);
				out.close();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		} else if (aFile.isDirectory()) {
			System.out.println(spcs + "[DIR] " + aFile.getName());
			if (aFile.getName().contains("-")) {
				dirName = aFile.getName();
				File newDir = new File(dirLoc + dirName);
				if (!newDir.exists()) {
					System.out.println("creating directory: "
							+ newDir.getName());
					newDir.mkdir();
				}
			}
			File[] listOfFiles = aFile.listFiles();
			if (listOfFiles != null) {
				for (int i = 0; i < listOfFiles.length; i++)
					Process(listOfFiles[i]);
			} else {
				System.out.println(spcs + " [ACCESS DENIED]");
			}
		}
		spc_count--;
	}

	public static void copyFile(File sourceFile, File destFile)
			throws IOException {
		if (!destFile.exists()) {
			destFile.createNewFile();
		}

		FileChannel source = null;
		FileChannel destination = null;

		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		} finally {
			if (source != null) {
				source.close();
			}
			if (destination != null) {
				destination.close();
			}
		}
	}

	public static String getMD5CheckSum(String filePath) throws Exception {
		MessageDigest md = MessageDigest.getInstance("MD5");
		FileInputStream fis = new FileInputStream(filePath);

		byte[] dataBytes = new byte[1024];

		int nread = 0;
		while ((nread = fis.read(dataBytes)) != -1) {
			md.update(dataBytes, 0, nread);
		}
		;
		byte[] mdbytes = md.digest();

		// convert the byte to hex format method 1
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < mdbytes.length; i++) {
			sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16)
					.substring(1));
		}

		// System.out.println("Digest(in hex format):: " + sb.toString());

		// convert the byte to hex format method 2
		StringBuffer hexString = new StringBuffer();
		for (int i = 0; i < mdbytes.length; i++) {
			String hex = Integer.toHexString(0xff & mdbytes[i]);
			if (hex.length() == 1)
				hexString.append('0');
			hexString.append(hex);
		}
		// System.out.println("Digest(in hex format):: " +
		// hexString.toString());
		fis.close();
		return hexString.toString();
	}

	public static String readFile(String path, Charset encoding)
			throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

	public static void addFiletoList(String dirPath) {
		File directory = new File(dirPath);
		// get all the files from a directory
		File[] fList = directory.listFiles();
		for (File file : fList) {
			if (file.isFile()) {

				fileList.add(file.getName());
				dirList.add(file.getPath());
			} else if (file.isDirectory()) {
				addFiletoList(file.getAbsolutePath());
			}
		}
	}

	FileHash(String targetDir, String key) {
		File aFile = new File(targetDir);
		enc = new MP3Encryption(key);
		//Process(aFile);
		addFiletoList(dirLoc);

		System.out.println(fileList);
		System.out.println(dirList);

		try {
            Indexer indexer = new Indexer(key);
			indexer.mapToIndexFiles(indexer.indexMessages(null, dirList,
					fileList, "D:\\Users\\Read\\Documents\\GitHub\\Searchable-File-Encryption\\seperators.txt",
					"D:\\Users\\Read\\Documents\\GitHub\\Searchable-File-Encryption\\stopwords.txt"), dirLoc);
		} catch (IOException e) { // TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
