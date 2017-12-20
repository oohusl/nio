package com.nio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by hsl on 2017/12/17.
 */
public class NioClient {

    final SocketChannel socket;
    final ByteBuffer bufferWriter = ByteBuffer.allocate(1024);
    final ByteBuffer bufferReader = ByteBuffer.allocate(1024);

    final Selector selector;

    NioClient(String host, int port) throws IOException {
        selector = Selector.open();
        socket = SocketChannel.open();
        socket.connect(new InetSocketAddress(host, port));
        socket.configureBlocking(false);
        SelectionKey skey = socket.register(selector, SelectionKey.OP_READ);
        skey.attach(new Handler());

        while(!Thread.interrupted()){
            selector.select();

            Set<SelectionKey> selected = selector.selectedKeys();

            Iterator<SelectionKey> it = selected.iterator();
            while (it.hasNext()) {
                SelectionKey key = it.next();
                System.out.println(key.interestOps() + "," + key.attachment().getClass().getCanonicalName());
                dispatch(key);
            }
            selected.clear();
        }
        new Thread(new ChatInput()).start();
    }

    void dispatch(SelectionKey k) {
        Runnable r = (Runnable) k.attachment();
        if (r != null) {
            r.run();
        }
    }

    class ChatInput implements Runnable {
        @Override
        public void run() {
            try {
                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
                while (!Thread.interrupted()) {
                    String line = inReader.readLine();
                    bufferWriter.clear();
                    bufferWriter.put(line.getBytes());
                    bufferWriter.flip();
                    socket.write(bufferWriter);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new NioClient("127.0.0.1", 3000);
    }


    class Handler implements Runnable{
        @Override
        public void run(){
            try {
                bufferReader.clear();
                int len = 0;
                while((len = socket.read(bufferReader)) > 0){
                    bufferReader.flip();
                    byte[] content = new byte[bufferReader.limit()];
                    System.out.println("Receive from server:" + new String(content));
                    bufferReader.clear();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
