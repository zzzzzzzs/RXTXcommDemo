package com.me;

import gnu.io.PortInUseException;
import gnu.io.SerialPort;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


@SuppressWarnings("all")
public class MainRXTX {

    // 串口列表
    private List<String> mCommList = null;
    // 串口对象
    private SerialPort mSerialport;

    // 获取波特率，默认为9600
    private int baudrate = 9600;
    // auto get name
    private String commName;


    private static final ReentrantLock LOCK = new ReentrantLock();
    private static final Condition STOP = LOCK.newCondition();

    public MainRXTX() {
        openSerialPort();
    }

    /**
     * open serial
     *
     * @param:
     * @return:
     * @auther: zzzzzzzs
     * @date: 2021/2/18 13:54
     */
    private void openSerialPort() {
        try {
            ArrayList<String> ports = SerialPortManager.searchPorts();
            commName = ports.get(0);
            mSerialport = SerialPortManager.openPort(commName, baudrate);

            if (mSerialport != null) {
                System.out.printf("串口已打开\r\n");
            }
        } catch (PortInUseException e) {
            System.out.println("串口已被占用！");
        }

        // add serial listener
        SerialPortManager.addListener(mSerialport, new SerialPortManager.SerialReceData() {

            @Override
            public void receEvent() {
                byte[] data = null;
                try {
                    if (mSerialport == null) {
                        System.out.println("串口对象为空，监听失败！");
                    } else {
                        // read serial data
                        data = SerialPortManager.readFromPort(mSerialport);
                        System.out.printf(new String(data));
//                        System.out.println(ByteUtils.byteArrayToHexString(data) + "\r\n");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // 发生读取错误时显示错误信息后退出系统
                    System.exit(0);
                }
            }
        });

    }


    public static void block() {
        //主线程阻塞等待，守护线程释放锁后退出
        try {
            LOCK.lock();
            STOP.await();
        } catch (InterruptedException e) {
//            logger.warn(" service   stopped, interrupted by other thread!", e);
            System.out.println(" service   stopped, interrupted by other thread!");
        } finally {
            LOCK.unlock();
        }
    }

    public static void main(String[] args) {

        Runnable r = () -> {
            new MainRXTX();
        };
        Thread t1 = new Thread(r, "线程1");
        // 并行执行的，不一定谁先执行
        t1.start();


        block();

    }
}