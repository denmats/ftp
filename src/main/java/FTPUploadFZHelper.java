import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FTPUploadFZHelper {

    public static void main(String[] args) throws IOException {

        //Walk through directory and store all file into set
        Set<String> listOfFilesForArchiving = listFilesUsingJavaIO();
        listOfFilesForArchiving.stream().forEach(System.out::println);

        //adds date format to distinguish archive files on the server
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");
        String firstRemoteFile = "archive_"+formatter.format(date)+".zip";


        //Zip files from the directory to archive
        try {
            FileOutputStream fos = new FileOutputStream("C:\\Users\\matsuied\\Desktop\\ftp\\"+firstRemoteFile);
            ZipOutputStream zipOut = new ZipOutputStream(fos);
            for (String srcFile : listOfFilesForArchiving) {
                File fileToZip = new File("C:\\Users\\matsuied\\Desktop\\ftp\\"+srcFile);
                FileInputStream fis = new FileInputStream(fileToZip);
                ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
                zipOut.putNextEntry(zipEntry);

                byte[] bytes = new byte[1024];
                int length;
                while((length = fis.read(bytes)) >= 0) {
                    zipOut.write(bytes, 0, length);
                }
                fis.close();
            }
            zipOut.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Zip folder is created.");

        //uploading to remote server
        String server = "192.168.0.101";
        int port = 21;
        String user = "denys1";
        String pass = "1234";

        FTPClient ftpClient = new FTPClient();
        try {

            ftpClient.connect(server, port);
            ftpClient.login(user, pass);
            ftpClient.enterLocalPassiveMode();

            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);


        // uploads zip archive an InputStream
            File dirForArchiving = new File("C:\\Users\\matsuied\\Desktop\\ftp\\"+firstRemoteFile);

            InputStream inputStream = new FileInputStream(dirForArchiving);

            System.out.println("Start uploading files...");
            boolean done = ftpClient.storeFile(firstRemoteFile, inputStream);
            inputStream.close();
            if (done) {
                System.out.println("The archive is uploaded successfully.");
            }

        } catch (IOException ex) {
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        //Remove all files
        removeAllFilesAfterUploadingToRemoteServer();

    }

    private static Set<String> listFilesUsingJavaIO() {
        return Stream.of(Objects.requireNonNull(new File("C:\\Users\\matsuied\\Desktop\\ftp").listFiles()))
                .filter(file -> !file.isDirectory())
                .map(File::getName)
                .collect(Collectors.toSet());
    }

    private static void removeAllFilesAfterUploadingToRemoteServer() throws IOException {
        FileUtils.cleanDirectory(new File("C:\\Users\\matsuied\\Desktop\\ftp"));
        System.out.println("All files are removed");
    }

}

