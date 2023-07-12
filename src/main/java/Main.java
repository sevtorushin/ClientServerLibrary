import clients.LocalTransferClient;
import clients.TransferClient;
import entity.SIBParameter;
import entity.WITSPackageDirectional;
import entity.WITSPackageMwdEvaluation;
import entity.WITSPackageTimeBased;
import exceptions.BuildObjectException;
import servers.*;
import service.CacheReader;
import service.SIBConverter;
import service.WITSConverter;
import utils.ArrayUtils;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException, BuildObjectException {
        //Трансляция клиентом дампа SR из бинарного файла----------------------------------------
//        try(TransferClient client = new TransferClient("localhost", 5000)) {
//            SIBStreamEmulator emulator = new SIBStreamEmulator(new File("E:\\Documents\\Java_Projects\\DrillingDataReceiver\\src\\main\\resources\\dumps\\sibDump.bin"));
//            OutputStream os = client.getOutputStreamToServer();
//            byte[] b;
//            for (int i = 0; i < 5; i++) {
//                b = emulator.buildBinaryObject();
//                os.write(b);
//                try {
//                    Thread.sleep(3000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//----------------------------------------------------------------------------------------------
        //Трансляция сервером дампа WITS из бинарного файла
//        WITSStreamEmulator emulator = new WITSStreamEmulator(new File("E:\\Documents\\Java_Projects\\DrillingDataReceiver\\src\\main\\resources\\dumps\\witsDump.bin"));
//        Socket socket = null;
//        OutputStream os = null;
//        while (true) {
//            try (ServerSocket serverSocket = new ServerSocket(5000)) {
//                socket = serverSocket.accept();
//                os = socket.getOutputStream();
//                byte[] b;
//                while (true) {
//                    try {
//                        b = emulator.buildBinaryObject();
//                        os.write(b);
//                        try {
//                            Thread.sleep(10);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    } catch (BuildObjectException e) {
//                        System.err.println(e.getMessage());
//                        break;
//                    }
//                }
//            } finally {
//                if (os != null)
//                    os.close();
//                if (socket != null)
//                    socket.close();
//            }
//        }

//----------------------------------------------------------------------------------------------
//        Сборка объекта пакета №1 WITS
//        WITSReceivingClient client = new WITSReceivingClient("192.168.0.100", 6000);
//        WITSConverter converter = new WITSConverter();
////        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("e:\\witsObjectData.bin"));
////        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("e:\\witsBinaryData.bin"));
//        byte[] bytes;
//        while (true) {
//            bytes = client.receiveBytes();
//            WITSPackageTimeBased packageTimeBased = (WITSPackageTimeBased) converter.convert(bytes, WITSPackageTimeBased.class);
////            System.out.println(Arrays.toString(bytes));
//            System.out.println(packageTimeBased);
////            oos.writeObject(packageTimeBased);
////            oos.flush();
////            bos.write(bytes);
////            bos.flush();
//        }
//----------------------------------------------------------------------------------------------
        //Чтение объектов WITS из файла
//        ObjectInputStream ois = new ObjectInputStream(new FileInputStream("e:\\witsObjectData.bin"));
//        try {
//            while (true) {
//                System.out.println(ois.readObject());
//            }
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }

//----------------------------------------------------------------------------------------------
        //Чтение байтов WITS из файла, конвертация в объекты и печать в консоль
//        WITSStreamEmulator emulator = new WITSStreamEmulator(new File("E:\\Documents\\Java_Projects\\DrillingDataReceiver\\src\\main\\resources\\dumps\\witsDump.bin"));
//        WITSConverter converter = new WITSConverter();
//        byte[] b;
//        WITSPackage witsPackage;
//        while (true) {
//            try {
//                b = emulator.buildBinaryObject();
//            } catch (BuildObjectException e) {
//                System.err.println(e.getMessage());
//                break;
//            }
//            witsPackage = converter.convert(b, WITSPackageTimeBased.class);
//            System.out.println(witsPackage);
//        }
//----------------------------------------------------------------------------------------------
        //Прием данных из SR, конвертация в объект и печать в консоль
//        try(SIBReceiverServer srv = new SIBReceiverServer(5111)) {
//            SIBConverter converter = new SIBConverter();
//            while (true) {
//                byte[] data = srv.receiveBytes();
//                    SIBParameter parameter = converter.convert(data, SIBParameter.class);
//                    System.out.println(parameter);
//            }
//        } catch (DisconnectedException e){
//            System.out.println(e.getMessage());
//        }
//        catch (IOException e){
//            e.printStackTrace();
//        }
//---------------------------------------------------------------------------------------------
        //Прием данных из SR и запись байтов в бинарный файл
//        SIBReceiverServer srv = new SIBReceiverServer(5111);
//        TransferClient.transferFromTo(srv.getInpStrm(), new FileOutputStream(
//                "E:\\Documents\\Java_Projects\\WITSServer\\src\\main\\resources\\dumps\\slbDump.bin"));
//---------------------------------------------------------------------------------------------
        //Прием данных из SR, конвертация в объект и запись в бинарный файл
//        SIBReceiverServer srv = new SIBReceiverServer(5111);
//        SIBConverter converter = new SIBConverter();
//        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("e:\\sibData.bin"));
//        while (true) {
//            byte[] b = srv.receiveBytes();
//            SIBParameter parameter = converter.convert(b);
//            oos.writeObject(parameter);
//            oos.flush();
//    }
//---------------------------------------------------------------------------------------------
        //Чтение объектов SR из файла
//        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("e:\\sibDump.bin"))){
//            while (true) {
//                System.out.println(ois.readObject());
//            }
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }

//----------------------------------------------------------------------------------------------

        //Эмуляция приема данных от SR. Чтение из файла и печать объектов в консоль
//        SIBStreamEmulator emulator = new SIBStreamEmulator(new File(
//                "E:\\Documents\\Java_Projects\\WITSServer\\src\\main\\resources\\dumps\\sibDump.bin"));
//        SIBConverter converter = new SIBConverter();
//        byte[] data;
//        for (int i = 0; i < 89; i++) {
//            System.out.println(converter.convert(emulator.buildBinaryObject(), SIBParameter.class));
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//--------------------------------------------------------------------------------------------

        //Прием данных из SR по протоколу GeoServer и печать байтов в консоль
//        SIBReceiverServer srv = new SIBReceiverServer(1011);
//        FileOutputStream fos = new FileOutputStream("e:\\geoSrv.bin");
//        while (true) {
//            byte[] b = srv.receiveBytes();
////            if (b[1] == 23)
//            fos.write(b);
//            System.out.println(Arrays.toString(b));
//
//        }
//--------------------------------------------------------------------------------------------

        //Генерация пакета №1 WITS и печать в консоль
//        WITSRandomGenerator generator = new WITSRandomGenerator("01");
//        while (true) {
//            System.out.println(generator.getWITSPackage());
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }

//// -----------------WITS server--------------------------------
//        System.out.println("В аргументах ввести сначала порт, затем интервал");
//        WITSRandomGenerator generator = new WITSRandomGenerator("01");
//        ServerSocket serverSocket = new ServerSocket(Integer.parseInt(args[0]));
//        System.out.println("Server started on port " + args[0]);
//        new Thread(() -> {
//            String s = generator.getWITSPackage();
//            Socket socket;
//            OutputStream os = null;
//            while (true) {
//                try {
//                    socket = serverSocket.accept();
//                    System.out.println("Client " + socket.getInetAddress() + " connected");
//                    os = socket.getOutputStream();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                while (true) {
//                    try {
//                        os.write(s.getBytes());
//                        System.out.println(s);
//                        Thread.sleep(Integer.parseInt(args[1]));
//                        s = generator.getWITSPackage();
//                    } catch (InterruptedException | IOException e) {
//                        System.out.println("\r\n" + "Client connection reset. Waiting new client...");
//                        break;
//                    }
//                }
//            }
//        }).start();
    }
}

