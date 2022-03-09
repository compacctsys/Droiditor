package com.cambridge.utils;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

public class DataOffloader {

    private String separator = "#SEP#";

    private Queue<DataOffloaderMessage> ZMQMessageQueue = new LinkedList<>();

    public DataOffloader() {
        pub.start();
    }

    public void send(String topic, String data) {
        ZMQMessageQueue.add(new DataOffloaderMessage(topic, topic + separator + data));
    }

    private Thread pub = new Thread(new Runnable() {
        @Override
        public void run() {

            try (ZContext context = new ZContext()) {
                ZMQ.Socket socket = context.createSocket(SocketType.PUB);
                socket.bind("tcp://*:5556");

                while (true) {
                    if (ZMQMessageQueue.isEmpty() == false) {
                        DataOffloaderMessage zmqMessage = ZMQMessageQueue.remove();
                        socket.send(zmqMessage.data);
                    }
                    Thread.sleep(1);
                }
            } catch (NoSuchElementException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    });
}
