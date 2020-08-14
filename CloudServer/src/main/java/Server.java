import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {

    private ArrayList<ClientHandler> clients;
    private ServerSocket server;

    public Server(int port) {
        clients = new ArrayList<>();
        try {
            server = new ServerSocket(port);
            System.out.println("Server online");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            while (true) {
                userAuthorization();
            }
        } finally {
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void userAuthorization() {
        try {
            Socket socket = server.accept();
            clients.add(new ClientHandler(this, socket));
            System.out.println("Клиент подключился");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnectUser(int id) {
        for (ClientHandler ch: clients) {
            if (ch.getId() == id) {
                clients.remove(ch);
                System.out.println("Пользователь" + id + " отключился");
                break;
            }
        }
    }

    public void createDirectory(String dirName) {
        File dir = new File("CloudServer/users/" + dirName);
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    public void createFile(String fileName) throws IOException {
        File file = new File(fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
    }

    public String info(String dirName) {
        StringBuilder sb = new StringBuilder();
        File dir = new File("CloudServer/users/" + dirName);
        File[] files = dir.listFiles();
        for (File f: files) {
            sb.append(f.getName()).append("\n");
        }
        if (sb.length() == 0) {
            return "Папка пуста";
        } else {
            return sb.deleteCharAt(sb.length() - 1).toString();
        }
    }

    public String upload(Socket socket, String filePath, long length) {
        try {
            File file = new File("CloudServer/users/" + filePath);
            DataInputStream is = new DataInputStream(socket.getInputStream());
            file.createNewFile();
            try (FileOutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[8192];
                while (length > 0) {
                    int r = is.read(buffer);
                    length -= r;
                    System.out.println(r);
                    os.write(buffer, 0 , r);
                }
            }
            return "Файл загружен";
        } catch (IOException e) {
            e.printStackTrace();
            return "ОШИБКА";
        }
    }

    public void download(Socket socket, String filePath) throws IOException {
        DataOutputStream os = new DataOutputStream(socket.getOutputStream());
        File file = new File("CloudServer/users/" + filePath);
        try (InputStream isFile = new FileInputStream(file)) {
            long length = file.length();
            os.writeUTF(String.valueOf(length));
            byte[] buffer = new byte[8192];
            while (length > 0) {
                int readBytes = isFile.read(buffer);
                length -= readBytes;
                os.write(buffer, 0 , readBytes);
            }
        }
    }
}
