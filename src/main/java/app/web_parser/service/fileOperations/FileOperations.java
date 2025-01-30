package app.web_parser.service.fileOperations;

import app.web_parser.dto.FileDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;


@Service
public class FileOperations implements IFileOperations{
    private final Logger logger = LogManager.getLogger(this.getClass());
    @Override
    public boolean checkFileExists(String fileName) {
        File file = new File(fileName);
        return file.exists();
    }
    public void moveFile(String file, String newFile, String path, String newPath) {
        try {
            logger.info("Moving File:{} from {} to {}", file, path, newPath);
            Files.createDirectories(Paths.get(newPath));
            Files.move(Paths.get(path+"/"+file), Paths.get(newPath + "/" + newFile));
            logger.info("Moved File:{} from {} to {}", file, path, newPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean createDirectory(String filePath){
        try {
            Files.createDirectories(Paths.get(filePath));
            return true;
        } catch (Exception ex) {
            logger.error("Error in creating directory {}", filePath);
            return false;
        }
    }
    
    public boolean copy(FileDto fileName, String addDeltaFile, String delDeltaFile) {
        try {
            File file = new File(fileName.getFilePath() +"/" + fileName.getFileName());
            File addFile = new File(addDeltaFile);
            File delFile = new File(delDeltaFile);
            Files.copy(file.toPath(), addFile.toPath());
            Files.createFile(delFile.toPath());
            return true;
        } catch (Exception ex) {
            logger.error("File creation for delta failed {}", ex.getMessage());
            return false;
        }
    }

    public boolean sortFile(String fileName, String sortedFileName) {
        try {
            String decodedPath = URLDecoder.decode(fileName, "UTF-8");
            logger.info(decodedPath);
            File inputFile = new File(fileName);
            File sortFile = new File(sortedFileName);
            String command = "(head -n 1 \"" + inputFile.getAbsolutePath() + "\" && tail -n +2 \"" + inputFile.getAbsolutePath() + "\" | sort)";
            ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c",  command);

            processBuilder.redirectOutput(ProcessBuilder.Redirect.to(sortFile));
            Process process = processBuilder.start();
            int exitStatus = process.waitFor();
            if(exitStatus == 0 ) {
                logger.info("The file {} is sorted and stored in file {}", fileName, sortedFileName);
                return true;
            }
            InputStream errorStream = process.getErrorStream();
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream));
            String line;
            while ((line = errorReader.readLine()) != null) {
                logger.error(line);
            }
            logger.error("Sorting of the file {} failed due to reason {}", fileName, process.getErrorStream());
            return false;
        } catch (Exception ex) {
            logger.error("Sorting of the file {} failed due to reason {}", fileName, ex.getMessage());
            return false;
        }
    }

//    public boolean createDelDiff(String currentFile, String previousFile, String deltaDeleteFile) {
//        try {
//            ProcessBuilder processBuilder = new ProcessBuilder( "bash", "-c",
//                    "diff -B --changed-group-format='%<' --unchanged-group-format=''" +
//                    " <(tail -n +2 " + previousFile + ") <(tail -n +2 " +
//                    currentFile + ") | (head -n 1 " + previousFile + "; cat)");
//            processBuilder.redirectOutput(ProcessBuilder.Redirect.to(new File(deltaDeleteFile)));
//            Process process = processBuilder.start();
//            InputStream errorStream = process.getErrorStream();
//            BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream));
//            String line;
//            while ((line = errorReader.readLine()) != null) {
//                logger.error(line);
//            }
//            processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
//            int exitStatus = process.waitFor();
//            if(exitStatus == 0 ) {
//                logger.info("The delete delta file creation successful for file {} ", currentFile);
//                return true;
//            }
//            logger.error("he delete delta file creation for file {} failed due to reason {}", currentFile, process.exitValue());
//            return false;
//
//        } catch (Exception ex) {
//            logger.error(String.valueOf(ex));
//            logger.error("The delete delta file creation for file {} failed due to reason {}", currentFile, ex.getMessage());
//            return false;
//        }
//    }
//    public boolean createAddDiff(String currentFile, String previousFile, String deltaAddFile) {
//        try {
//            ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c",
//                "diff -B --changed-group-format='%>' --unchanged-group-format='' <(tail -n +2 " +
//                        previousFile + ") <(tail -n +2 " + currentFile + ") | (head -n 1 " +
//                        previousFile + "; cat)"
//            );
//            processBuilder.redirectOutput(ProcessBuilder.Redirect.to(new File(deltaAddFile)));
//            Process process = processBuilder.start();
//            InputStream errorStream = process.getErrorStream();
//            BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream));
//            String line;
//            while ((line = errorReader.readLine()) != null) {
//                logger.error(line);
//            }
//            int exitStatus = process.waitFor();
//            if(exitStatus == 0 ) {
//                logger.info("The insert delta file creation successful for file {} ", currentFile);
//                return true;
//            }
//            logger.error("The insert delta file creation for file {} failed due to reason {}", currentFile, process.getErrorStream().toString());
//            return false;
//
//        } catch (Exception ex) {
//            logger.error("The insert delta file creation for file {} failed due to reason {}", currentFile, ex.getMessage());
//            return false;
//        }
//    }

    public boolean createDiffFiles(String currentFile, String previousFile, String deltaFile, int type) {
        try {
            ProcessBuilder processBuilder = getProcessBuilder(currentFile, previousFile, type);
            processBuilder.redirectOutput(ProcessBuilder.Redirect.to(new File(deltaFile)));
            Process process = processBuilder.start();
            InputStream errorStream = process.getErrorStream();
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream));
            String line;
            while ((line = errorReader.readLine()) != null) {
                logger.error(line);
            }
            processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
            int exitStatus = process.waitFor();
            if(exitStatus == 0 ) {
                if(type == 0) 
                    logger.info("The delete delta file creation successful for file {} ", currentFile);
                else
                    logger.info("The insert delta file creation successful for file {} ", currentFile);
                return false;
            }
            if(type == 0)
                logger.error(" delete delta file creation for file {} failed due to reason {}", currentFile, process.exitValue());
            else
                logger.error("The insert delta file creation for file {} failed due to reason {}", currentFile, process.getErrorStream().toString());
            return true;

        } catch (Exception ex) {
            logger.error(String.valueOf(ex));
            if(type == 0) 
                logger.error("The delete delta file creation for file {} failed due to reason {}", currentFile, ex.getMessage());
            else
                logger.error("The insert delta file creation for file {} failed due to reason {}", currentFile, ex.getMessage());
            return true;
        }
    }

    private static ProcessBuilder getProcessBuilder(String currentFile, String previousFile, int type) {
        ProcessBuilder processBuilder;
        if(type == 1) {
            processBuilder = new ProcessBuilder( "bash", "-c",
                    "diff -B --changed-group-format='%>' --unchanged-group-format=''" +
                            " <(tail -n +2 \"" + previousFile + "\") <(tail -n +2 \"" +
                            currentFile + "\") | (head -n 1 \"" + previousFile + "\"; cat)");
        }
        else {
            processBuilder = new ProcessBuilder( "bash", "-c",
                    "diff -B --changed-group-format='%<' --unchanged-group-format=''" +
                            " <(tail -n +2 \"" + previousFile + "\") <(tail -n +2 \"" +
                            currentFile + "\") | (head -n 1 \"" + previousFile + "\"; cat)");
        }
        return processBuilder;
    }
}
