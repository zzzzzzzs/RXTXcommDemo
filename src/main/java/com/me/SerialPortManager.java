package com.me;

import com.me.utils.ArrayUtils;
import com.me.utils.ShowUtils;
import gnu.io.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.TooManyListenersException;


public class SerialPortManager {

    /**
     * search the available serial port
     *
     * @auther: zzzzzzzs
     * @date: 2021/2/18 14:10
     * @return: return the available serial port
     */
    public static final ArrayList<String> searchPorts() {
        // get now all can use port
        Enumeration<CommPortIdentifier> ports = CommPortIdentifier.getPortIdentifiers();
        ArrayList<String> portNameList = new ArrayList<>();
        // Adds the available serial port names to the List and returns the List
        while (ports.hasMoreElements()) {
            String name = ports.nextElement().getName();
            portNameList.add(name);
        }
        return portNameList;
    }

    /**
     * open serial port
     *
     * @auther: zzzzzzzs
     * @date: 2021/2/18 14:36
     * @param: portName serial port name
     * @return: baudrate
     */
    public static final SerialPort openPort(String portName, int baudrate) throws PortInUseException {
        try {
            //identify port name
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
            // open port and set a timeout
            CommPort commPort = portIdentifier.open(portName, 2000);
            // judge whether it is a serial port
            if (commPort instanceof SerialPort) {
                SerialPort serialPort = (SerialPort) commPort;
                try {
                    // set serial baudrate
                    // data bits:8
                    // stop bits:1
                    // parity:none
                    serialPort.setSerialPortParams(baudrate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
                } catch (UnsupportedCommOperationException e) {
                    e.printStackTrace();
                }
                return serialPort;
            }
        } catch (NoSuchPortException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * close serial port
     *
     * @param serialPort close serial object
     */
    public static void closePort(SerialPort serialPort) {
        if (serialPort != null) {
            serialPort.close();
        }
    }


    /**
     * 往串口发送数据
     *
     * @param serialPort
     *            串口对象
     * @param order
     *            待发送数据
     */

    /**
     * @Description:
     * @Param: serialPort: object
     * order: data to be sent
     * @return:
     * @auther: zzzzzzzs
     * @Date: 2021/2/20
     */
    public static void sendToPort(SerialPort serialPort, byte[] order) {
        OutputStream out = null;
        try {
            out = serialPort.getOutputStream();
            out.write(order);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                    out = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    // TODO

    /**
     * read data from serial port
     *
     * @param serialPort : The SerialPort object to which the connection is currently established
     * @return Retrieved data
     */
    public static byte[] readFromPort(SerialPort serialPort) {
        InputStream in = null;
        byte[] bytes = {};
        try {
            in = serialPort.getInputStream();
            // Buffer size is one byte
            byte[] readBuffer = new byte[1];
            int bytesNum = in.read(readBuffer);
            while (bytesNum > 0) {
                bytes = ArrayUtils.concat(bytes, readBuffer);
                bytesNum = in.read(readBuffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                    in = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bytes;
    }

    /**
     * add serial open listener
     *
     * @param serialPort : serial port object
     * @param listener   :
     *                   串口存在有效数据监听
     */
    public static void addListener(SerialPort serialPort, SerialReceData listener) {
        try {
            // 给串口添加监听器
            serialPort.addEventListener(new SerialPortListener(listener));
            // 设置当有数据到达时唤醒监听接收线程
            serialPort.notifyOnDataAvailable(true);
            // 设置当通信中断时唤醒中断线程
            serialPort.notifyOnBreakInterrupt(true);
        } catch (TooManyListenersException e) {
            e.printStackTrace();
        }
    }

    /**
     * serial port listener
     */
    public static class SerialPortListener implements SerialPortEventListener {

        private SerialReceData serialReceData;

        public SerialPortListener(SerialReceData serialReceData) {
            this.serialReceData = serialReceData;
        }
        @Override
        public void serialEvent(SerialPortEvent serialPortEvent) {
            switch (serialPortEvent.getEventType()) {
                case SerialPortEvent.DATA_AVAILABLE: // 1.串口存在有效数据
                    if (serialReceData != null) {
                        serialReceData.receEvent();
                    }
                    break;

                case SerialPortEvent.OUTPUT_BUFFER_EMPTY: // 2.输出缓冲区已清空
                    break;

                case SerialPortEvent.CTS: // 3.清除待发送数据
                    break;

                case SerialPortEvent.DSR: // 4.待发送数据准备好了
                    break;

                case SerialPortEvent.RI: // 5.振铃指示
                    break;

                case SerialPortEvent.CD: // 6.载波检测
                    break;

                case SerialPortEvent.OE: // 7.溢位（溢出）错误
                    break;

                case SerialPortEvent.PE: // 8.奇偶校验错误
                    break;

                case SerialPortEvent.FE: // 9.帧错误
                    break;

                case SerialPortEvent.BI: // 10.通讯中断
                    System.out.println("与串口设备通讯中断");
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * serial receive data
     */
    public interface SerialReceData {
        void receEvent();
    }
}