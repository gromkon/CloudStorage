import java.io.*;
import java.net.Socket;

public class ClientHandler {

    private Server server;
    private Socket socket;
    private DataInputStream is;
    private DataOutputStream os;

    private int id;

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.is = new DataInputStream(socket.getInputStream());
            this.os = new DataOutputStream(socket.getOutputStream());
            this.id = 0;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    msgHandling();
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void msgHandling() {
        try {
            while (true) {
                String msg = is.readUTF();
                System.out.println("Пользователь" + id + " прислал сообщение:\n" + msg);
                if (msg.startsWith(Commands.GET_INFO.command)) {
                    sendMsg("Содержимое вашей папки:\n" + server.info("user" + id));
                } else if (msg.startsWith(Commands.DOWNLOAD.command)) {
                    String filePath = msg.split("\"")[1];
                    server.download(socket,"user" + id + "/" + filePath);

//                    // Без этого в файл добавляется фраза "Файл скачен", не могу разобраться почему
//                    try {
//                        Thread.sleep(100);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }

//                    sendMsg("Файл скачен");
                } else if (msg.startsWith(Commands.UPLOAD.command)) {
                    String fileName = msg.substring(msg.lastIndexOf("/") + 1, msg.length() - 1);
                    String length = is.readUTF();
                    sendMsg(server.upload(socket,"user" + id + "/" + fileName, Long.parseLong(length)));
                } else if (msg.startsWith(Commands.AUTH.command)){
                    id = Integer.parseInt(msg.substring(Commands.AUTH.command.length() + 1));
                    sendMsg("Вы авторизованы как пользователь" + id);
                    server.createDirectory("user" + id);
                } else if (msg.startsWith(Commands.STOP.command)) {
                    disconnect();
                    sendMsg("Вы отключены от сервера");
                    break;
                } else {
                    sendMsg("Я не понимаю, что вы хотите. Доступные команды:\n" +
                            "/info - получить инофрмацию о файлах в хранилище\n" +
                            "/d \"путь до файла на сервере\" \"в какую директорию скачать\"- скачать файл из хранилища\n" +
                            "/u \"путь до файла\" - загрузить файл в хранилище\n" +
                            "/stop - отключиться");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMsg(String msg) {
        try {
            os.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void disconnect() {
        server.disconnectUser(id);
    }

    public int getId() {
        return id;
    }
}
