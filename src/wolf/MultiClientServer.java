package wolf;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Server to accept client request.
 */
public class MultiClientServer {
    private int port;
    private  String outputDirectory;
    private int maxThreads;

    public MultiClientServer(String configFilePath) throws IOException {
        loadConfig(configFilePath);
    }

    /**
     * Load config detail from config file.
     * @param configFilePath
     * @throws IOException
     */
    private void loadConfig(String configFilePath) throws IOException {
        Properties config = new Properties();
        try (FileInputStream fis = new FileInputStream(configFilePath)) {
            config.load(fis);
            port = Integer.parseInt(config.getProperty("serverPort"));
            outputDirectory = config.getProperty("directoryPath");
            maxThreads = Integer.parseInt(config.getProperty("maxThreads"));
        }

        File outputDir = new File(outputDirectory);
        if (!outputDir.exists()) {
            if (!outputDir.mkdirs()) {
                throw new IOException("Failed to create output directory: " + outputDirectory);
            }
        }
    }

    public void start() {
        ExecutorService executorService = Executors.newFixedThreadPool(maxThreads);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                executorService.submit(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        } finally {
            executorService.shutdown();
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java MultiClientServer <config-file-path>");
            return;
        }
        try {
            MultiClientServer server = new MultiClientServer(args[0]);
            server.start();
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
        }
    }

    /**
     *  Connects with client.
     */
    private static class ClientHandler implements Runnable {
        private Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
                // Set up input and output streams for communication with the client
                DataInputStream input = new DataInputStream(clientSocket.getInputStream());

                // Read the file name (first data sent by client)
                String fileName = input.readUTF();
                System.out.println("Received file name from client: " + fileName);

                // Read the data sent by the client
                StringBuilder fileData = new StringBuilder();
                String line;
                while (!(line = input.readUTF()).equals("EOF")) {
                    fileData.append(line).append("\n");
                }

                // Write the data to a new file
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
                    writer.write(fileData.toString());
                    System.out.println("Data written to file: " + fileName);
                } catch (IOException e) {
                    System.out.println("Error writing to file: " + e.getMessage());
                }

                // Close the client connection
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

