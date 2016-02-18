import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class Sender1a {

    private int packetSize;
    private int port;
    private int delay;
    private String hostname;
    private FileInputStream file;

    /**
     * @param port       The number of the port we're working with
     * @param packetSize The number of bytes per packet that's being sent
     * @param filepath   The name of the file we we're sending
     * @param hostname   Generally localhost
     * @param delay      How long to wait between sending packets (in ms)
     */
    public Sender1a(int port, int packetSize, String filepath, String hostname, int delay) throws FileNotFoundException {
        this.file = new FileInputStream(filepath);
        this.port = port;
        this.packetSize = packetSize;
        this.hostname = hostname;
        this.delay = delay;
    }

    /**
     * Starts sending a file
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public void send() throws IOException, InterruptedException {

        InetAddress ip = InetAddress.getByName(hostname);
        DatagramSocket socket = new DatagramSocket();

        int packetNumber = 0;
        boolean finishedTransfer = false;

        while (!finishedTransfer) {
            // available data is the data we haven't already "read"
            int availableData = file.available();

            // the amount of bytes we have per packet for actual data (i.e not header data)
            int packetDataSize = packetSize - 3;

            // if we have more data available than our packet size, then we only read
            // then we only read the amount of data we can store in our packet (minus 3
            // that we'll use to store header information)
            int dataLength;
            if (availableData >= packetDataSize) {
                dataLength = packetDataSize;
            } else {
                dataLength = availableData;
            }

            // if this is the final packet, update boolean to end loop
            finishedTransfer = availableData <= packetDataSize;

            // prepare data; the extra 3 bytes contain the packet number and EOF data
            byte packetData[] = new byte[dataLength + 3];

            // the two bytes store the packet number
            packetData[0] = (byte) (packetNumber >>> 8);
            packetData[1] = (byte) (packetNumber);

            // If it's the end of file, we send a 1, otherwise we send a 0
            packetData[2] = (byte) (finishedTransfer ? 1 : 0);

            // we read data from the file, offset by 3 (the 3 bytes we've added)
            // we only read the amount available and no more
            file.read(packetData, 3, dataLength);

            // send the data
            DatagramPacket sendPacket = new DatagramPacket(packetData, packetData.length, ip, port);
            socket.send(sendPacket);

            packetNumber++;

            // sleep for 10ms before sending another packet
            Thread.sleep(delay);

        }

        socket.close();
    }

    /*
        java Sender1a localhost <Port> <Filename>
     */
    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        int packetSize = 1024;
        String filepath = args[2];
        String hostname = "localhost";

        int delay = 10;

        try {
            Sender1a sender = new Sender1a(port, packetSize, filepath, hostname, delay);
            sender.send();
            System.out.println("File has been sent successfully");

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            System.out.println("That file doesn't exist!");
        } catch (IOException e) {
            System.out.println("Something went wrong :( " + e.getMessage());
        }
    }

}