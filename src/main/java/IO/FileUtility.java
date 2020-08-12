package IO;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class FileUtility {

    public static void createDirectory(String dirName) {
        File file = new File(dirName);
        if (!file.exists()) {
            file.mkdir();
        }
    }

    public static void createFile(String fileName) throws IOException {
        File file = new File(fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
    }

    public static void move(File dir, File file) throws IOException {
        String path = dir.getAbsolutePath() + "/" +  file.getName();
        createFile(path);
        InputStream is = new FileInputStream(file);
        try (OutputStream os = new FileOutputStream(new File(path))) {
            byte[] buffer = new byte[8192];
            while (is.available() > 0) {
                int readBytes = is.read(buffer);
                os.write(buffer, 0 , readBytes);
            }
        }
    }

    public static void sendFile(Socket socket, File file) throws IOException {
        InputStream is = new FileInputStream(file);
        long size = file.length();
        int count = (int) (size / 8192) / 10;
        int readBuckets = 0;
        try (DataOutputStream os = new DataOutputStream(socket.getOutputStream())) {
            byte[] buffer = new byte[8192];
            os.writeUTF(file.getName());
            while (is.available() > 0) {
                int readBytes = is.read(buffer);
                readBuckets++;
                if (readBuckets % count == 0) {
                    System.out.print("=");
                }
                os.write(buffer, 0 , readBytes);
            }
        }
    }

    public static void main(String[] args) throws IOException {
//        createFile("./1.txt");
//        createDirectory("./dir1");
//        move(new File("./dir1"), new File("./1.txt"));
        sendFile(new Socket("localhost", 8189), new File("./Common/checklist.jpg"));
//        sendFile(new Socket("localhost", 8189), new File("./Common/video.mp4"));
    }

}
