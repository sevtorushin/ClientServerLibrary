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
import test.StartConsoleAPI;
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

    public static void main(String[] args) {
        StartConsoleAPI.main(args);
    }
}

