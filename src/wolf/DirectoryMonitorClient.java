package wolf;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.file.*;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Client program to monitor directory.
 */
public class DirectoryMonitorClient {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("The config file is not proper");
            System.exit(1);
        }

        //get the config path
        String configFilePath = args[0];
        Properties config = new Properties();

        try (FileInputStream configInput = new FileInputStream(configFilePath)) {
            config.load(configInput);
        } catch (IOException e) {
            System.err.println("Failed to load configuration file: " + e.getMessage());
            System.exit(1);
        }

        String directoryPath = config.getProperty("directoryPath");
        String keyPattern = config.getProperty("keyPattern");
        String serverAddress = config.getProperty("serverAddress");
        int serverPort = Integer.parseInt(config.getProperty("serverPort"));

        if (directoryPath == null || keyPattern == null || serverAddress == null || serverPort <= 0) {
            System.err.println("Invalid configuration. Please check the config file.");
            System.exit(1);
        }

        Pattern pattern = Pattern.compile(keyPattern);

        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            Path path = Paths.get(directoryPath);
            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

            System.out.println("Monitoring directory: " + directoryPath);
            while (true) {
                WatchKey key = watchService.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        Path filePath = path.resolve((Path) event.context());

                        //Process the new file in directory
                        processFile(filePath, pattern, serverAddress, serverPort);

                    }
                }
                if (!key.reset()) {
                    break;
                }
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("Error monitoring directory: " + e.getMessage());
        }
    }

    /**
     * @param filePath
     * @param keyPattern
     * @param serverAddress
     * @param serverPort
     */
    private static void processFile(Path filePath, Pattern keyPattern, String serverAddress, int serverPort) {
        try (InputStream input = new FileInputStream(filePath.toFile())) {
            Properties properties = new Properties();
            properties.load(input);

            // Filter properties based on the key pattern
            Map<String, String> filteredMap = properties.entrySet().stream()
                    .filter(entry -> keyPattern.matcher((String) entry.getKey()).matches())
                    .collect(Collectors.toMap(
                            entry -> (String) entry.getKey(),
                            entry -> (String) entry.getValue()
                    ));

            String fileName = filePath.getFileName().toString();
            System.out.print("File Name:" + fileName);
            // Relay the filtered map to the server
            sendToServer(filteredMap, serverAddress, serverPort, fileName);

            // Delete the file after processing
            Files.delete(filePath);
            System.out.println("Processed and deleted file: " + filePath);

        } catch (IOException e) {
            System.err.println("Failed to process file: " + filePath + ". Error: " + e.getMessage());
        }
    }

    /**
     * @param data
     * @param serverAddress
     * @param serverPort
     */
    private static void sendToServer(Map<String, String> data, String serverAddress, int serverPort, String fileName) {
        try (Socket socket = new Socket(serverAddress, serverPort)) {

            // Set up output stream to send data to the server
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            output.writeUTF(fileName);
            for(Map.Entry<String, String> entry: data.entrySet()){

                 output.writeUTF(entry.getKey());
                 output.writeUTF(entry.getValue());
            }
            output.writeUTF("EOF");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

