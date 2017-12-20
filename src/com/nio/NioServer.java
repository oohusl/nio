package com.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by hsl on 2017/12/19.
 */
public class NioServer {



    public void startServer() {
        try {
            new Thread(new Reactor(3000)).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new NioServer().startServer();
    }

    class Reactor implements Runnable {

        final Set<SocketChannel> sockets = new HashSet<SocketChannel>();
        final Selector selector;
        final ServerSocketChannel serverSocket;

        Reactor(int port) throws IOException {
            selector = Selector.open();
            serverSocket = ServerSocketChannel.open();
            serverSocket.socket().bind(new InetSocketAddress(port));
            serverSocket.configureBlocking(false);
            SelectionKey sk = serverSocket.register(selector, SelectionKey.OP_ACCEPT);
            sk.attach(new Acceptor());
        }

        @Override
        public void run() {
            try {
                while (!Thread.interrupted()) {
                    selector.select();
                    System.out.println("selected");
                    Set selected = selector.selectedKeys();
                    Iterator<SelectionKey> it = selected.iterator();
                    while (it.hasNext()) {
                        SelectionKey key = it.next();
                        System.out.println(key.interestOps() + "," + key.attachment().getClass().getCanonicalName());
                        dispatch(key);
                    }
                    selected.clear();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        void dispatch(SelectionKey k) {
            Runnable r = (Runnable) k.attachment();
            if (r != null) {
                r.run();
            }
        }


        class Acceptor implements Runnable {
            public void run() {
                try {
                    SocketChannel c = serverSocket.accept();

                    if (c != null) {
                        System.out.println(c.getRemoteAddress() + "连接成功...");
                        new Handler(selector, c);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        }

        final class Handler implements Runnable {

            final SocketChannel socket;
            final SelectionKey sk;

            ByteBuffer input = ByteBuffer.allocate(10);
            ByteBuffer output = ByteBuffer.allocate(10);
            static final int READING = 0, SENDING = 1;
            int state = READING;

            Handler(Selector sel, SocketChannel c) throws IOException {
                socket = c;
                c.configureBlocking(false);
                sk = socket.register(sel, SelectionKey.OP_READ);
                sk.attach(this);
                sel.wakeup();
            }

            boolean inputIsComplete() {
                input.flip();
                byte[] content = new byte[input.limit()];
                input.get(content);
                System.out.println("收到:" + new String(content));
                return true;
            }

            boolean outputIsComplete() {
                return true;
            }

            void process() {

            }

            @Override
            public void run() {
                try {
                    input.clear();
                    int len = 0;
                    while((len = socket.read(input)) > 0){
                        input.flip();
                        byte[] content = new byte[input.limit()];
                        input.get(content);
                        input.clear();
                        System.out.println("收到:" + new String(content));
                    }
                    System.out.println("end read");

//                    if (inputIsComplete()) {
//                        process();
////                        sk.attach(new Sender());
////                        sk.interestOps(SelectionKey.OP_WRITE);
////                        sk.selector().wakeup();
//                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }



            class Sender implements Runnable {
                public void run() {
                    try {
                        output.put("haha back".getBytes());
                        socket.write(output);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (outputIsComplete()) sk.cancel();
                }
            }
        }
    }


}
