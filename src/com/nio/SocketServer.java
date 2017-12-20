package com.nio;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by hsl on 2017/12/17.
 */
public class SocketServer {
    public static void main(String[] args) {
        new Thread(new Server()).start();
    }

    static class Server implements Runnable {
        public void run(){
            try {
                ServerSocket ss = new ServerSocket(3000);
                while(!Thread.currentThread().isInterrupted()){
                    new Thread(new Handler(ss.accept())).start();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        static class Handler implements Runnable {
            final Socket socket;

            public Handler(Socket s){
                this.socket = s;
                System.out.println(s.getRemoteSocketAddress() + "连接成功");
            }
            int i = 0;
            public void run(){
                try {

                    BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    BufferedWriter os =new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    while(true){
                        String thisLine = br.readLine();
                        System.out.println("Message From client: " + thisLine);
                        ++i;
                        os.write("第" + i + "条消息");
                        os.newLine();
                        os.flush();
                    }

                    //while (true) {


                    //}


                } catch (IOException ex) { /* ... */ }
            }
        }
    }
}



