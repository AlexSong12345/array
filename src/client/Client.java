package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

import common.Const;
import common.DataX;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;


public class Client implements Runnable {

    private final String data;
    private final Socket socket;
    private final static String FILE_NAME = "C:\\Temp\\srcDataFile.txt";

    public Client(String data) throws IOException {

        String addr = Const.Server;
        this.data = data;
        socket = new Socket(addr, Const.Port);
    }

    public static void main(String[] args) throws IOException {
        ExecutorService service = Executors.newCachedThreadPool();//Executors.newFixedThreadPool(8);
        Stream<String> lines = Files.lines(Paths.get(FILE_NAME), StandardCharsets.UTF_8);
        lines.forEach(line -> {
            try {
                service.submit(new Client(line));
//            try {
//                new Thread(new Client(line)).start();
//            } catch (IOException ex) {
//                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
//            }
            } catch (IOException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        service.shutdown();
    }

    @Override
    public void run() {
        String prefix = "[" + Thread.currentThread().getName() + "]";
        System.out.println(prefix + "Connected to: " + socket.getInetAddress());
        System.out.println(prefix + "Source data <<" + this.data + ">>");

        try (Scanner resInput = new Scanner(new InputStreamReader(socket.getInputStream()));
                Scanner srcInput = new Scanner(new StringReader(this.data));) {

            srcInput.useDelimiter("[;\\s]+");
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

            DataX dataSend = new DataX();
            dataSend.str = srcInput.nextLine();

            System.out.println(prefix + "Отправка на cервер...");
            out.writeObject(dataSend);
            System.out.println(prefix + "ОК");

            System.out.println(prefix + "Прием данных...");
            String result = resInput.nextLine();
            System.out.println(prefix + "ОК");
            System.out.println(prefix + "Ответ: ");
            System.out.println(result);

        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
