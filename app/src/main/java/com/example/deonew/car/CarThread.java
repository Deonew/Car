package com.example.deonew.car;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static java.lang.Thread.sleep;


/**
 * Created by deonew on 17-2-24.
 */


public class CarThread implements Runnable {

    @Override
    public void run() {

        Socket socket = null;
        try {
            //创建一个流套接字并将其连接到指定主机上的指定端口号
            socket = new Socket("192.168.0.105", 12345);
            sleep(1000);
            //读取服务器端数据
            //DataInputStream input = new DataInputStream(socket.getInputStream());
            //向服务器端发送数据
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            // System.out.print("请输入: \t");
            // String str = new BufferedReader(new InputStreamReader(System.in)).readLine();
            String str = "nihao";
            out.writeUTF(str);

            //String ret = input.readUTF();
            //System.out.println("服务器端返回过来的是: " + ret);
            // 如接收到 "OK" 则断开连接
            //if ("OK".equals(ret)) {
              //  System.out.println("客户端将关闭连接");
                //Thread.sleep(500);
//                break;
            //}

            out.close();
            //input.close();
        } catch (Exception e) {
            System.out.println("客户端异常:" + e.getMessage());
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    socket = null;
                    System.out.println("客户端 finally 异常:" + e.getMessage());
                }
            }
        }
    }
}
