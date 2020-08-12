import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Scanner;

public class Client {
    private final static String IP_ADDRESS = "localhost";
    private final static int PORT = 8080;

    private int id;
    private Socket socket;
    private DataInputStream is;
    private Scanner in;
    private DataOutputStream os;

    public Client(int id) {
        this.id = id;
        in = new Scanner(System.in);
        connect();
    }

    private void authorization() {
        sendMsg(Commands.AUTH.command + " " + id);
        waitAnswer();
    }

    private void sendRequests() {
        while (true) {
            String msg = in.nextLine();
            sendMsg(msg);
            if (msg.startsWith(Commands.GET_INFO.command)) {
                // ждем ответ
            } else if (msg.startsWith(Commands.UPLOAD.command)) {
                String fileName = msg.substring(msg.indexOf("\"") + 1, msg.length() - 1);
                System.out.println(fileName);
                try {
                    sendFile(new File(fileName));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (msg.startsWith(Commands.DOWNLOAD.command)) {
                String filePath = msg.split("\"")[1];
                String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
                String dirPath = msg.split("\"")[3];
                uploadFile(fileName, dirPath);
            }

            String answer = waitAnswer();
            if (answer.equals("Вы отключены от сервера")) {
                break;
            }
        }
    }

    private void connect() {
        try {
            socket = new Socket(IP_ADDRESS, PORT);
            is = new DataInputStream(socket.getInputStream());
            os = new DataOutputStream(socket.getOutputStream());

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        authorization();
                        sendRequests();
                    } finally {
                        try {
                            disconnect();
                            is.close();
                            os.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void disconnect() {
        sendMsg(Commands.STOP.command);
    }

    private void sendMsg(String msg) {
        try {
            os.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String waitAnswer() {
        try {
            String answer = is.readUTF();
            System.out.println(answer);
            return answer;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private void sendFile(File file) throws IOException {
        try (InputStream isFile = new FileInputStream(file)) {
            os.writeUTF(String.valueOf(file.length()));
            byte[] buffer = new byte[8192];
            while (isFile.available() > 0) {
                int readBytes = isFile.read(buffer);
                os.write(buffer, 0 , readBytes);
            }
        }
    }

    private void uploadFile(String fileName, String dirPath) {
        try {
            System.out.println("dirPath " + dirPath);
            System.out.println("fileName " + fileName);
            long length = Long.parseLong(is.readUTF());
            System.out.println(length);
            File file = new File(dirPath + "/" + fileName);
            try (FileOutputStream osFile = new FileOutputStream(file)) {
                byte[] buffer = new byte[8192];
                while (length > 0) {
                    int r = is.read(buffer);
                    length -= r;
                    osFile.write(buffer, 0 , r);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
