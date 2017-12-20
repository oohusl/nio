package com.nio;

import java.io.*;
import java.net.Socket;

/**
 * Created by hsl on 2017/12/17.
 */
public class SocketClient {
    public static void main1(String[] args){
        BufferedReader sin=new BufferedReader(new InputStreamReader(System.in));

        String readline = null; //从系统标准输入读入一字符串
        try {
            readline = sin.readLine();
            while(!readline.equals("bye")){
                System.out.println(readline);
                readline = sin.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public static void main(String[] args) {
        try {
            Socket socket=new Socket("127.0.0.1",3000);
            BufferedReader reader=new BufferedReader(new InputStreamReader(System.in));
            BufferedWriter os =new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader is=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            new Thread(new ClientHandler(is)).start();
            String readline =reader.readLine(); //从系统标准输入读入一字符串
            while(!readline.equals("bye")){
                //若从标准输入读入的字符串为 "bye"则停止循环
                System.out.println("输入: " + readline);
                os.write(readline);
                os.newLine();
                os.flush();

                //刷新输出流，使Server马上收到该字符串
                //System.out.println("Server:"+is.readLine());

                //从Server读入一字符串，并打印到标准输出上

                readline=reader.readLine(); //从系统标准输入读入一字符串

            } //继续循环

            os.close(); //关闭Socket输出流

            is.close(); //关闭Socket输入流

            socket.close(); //关闭Socket


        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    static class ClientHandler implements Runnable {
        final BufferedReader reader;
        public ClientHandler(BufferedReader reader){
            this.reader = reader;
        }

        public void run(){
            try {
                while(!Thread.interrupted()){
                    String line = reader.readLine();
                    System.out.println("From server:" + line);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
