package app.web_parser.service.fileCopy;

import app.web_parser.config.AppConfig;
import app.web_parser.constants.CopyStatus;
import app.web_parser.constants.FileType;
import app.web_parser.constants.ListType;
import app.web_parser.model.app.ListFileManagement;
import app.web_parser.repository.app.ListFileManagementRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ListFileManagementService {
    private final Logger log = LogManager.getLogger(this.getClass());

    @Autowired
    private ListFileManagementRepository listFileManagementRepository;

    @Autowired
    AppConfig appConfig;

    @Autowired
    SftpFileService sftpFileService;

    public ListFileManagement save(ListFileManagement listFileManagement) {
        log.info("Going to Save listFileManagement {}", listFileManagement);
        ListFileManagement savedListFileManagement = listFileManagementRepository.save(listFileManagement);
        log.info("Saved listFileManagement {}", savedListFileManagement);
        return savedListFileManagement;
    }

//    public void saveListManagementEntity(String transactionId, ListType listType, FileType fileType,
//                                                              String sourceFilePath, String sourceFileName,
//                                                              Long totalCount) {
//        String sourceServerName = appConfig.getSourceServerName();
//        List<String> copyDestinationServerName = appConfig.getCopyDestinationServerName();
//        List<String> copyDestinationServerPath = appConfig.getCopyDestinationServerPath();
//        Integer noOfDestination = copyDestinationServerName.size();
//        for (int i = 0; i < noOfDestination; i++) {
//            String destServerName = copyDestinationServerName.get(i);
//            String destFilePath = copyDestinationServerPath.get(i);
//            ListFileManagement listFileManagement = new ListFileManagement();
//            listFileManagement.setFileType(fileType.getIndex());
//            listFileManagement.setFileState(1);
//            listFileManagement.setFileName(sourceFileName);
//            listFileManagement.setFilePath(sourceFilePath);
//            listFileManagement.setListType(listType);
//            listFileManagement.setCopyStatus(CopyStatus.NEW.getIndex());
//            listFileManagement.setCreatedOn(LocalDateTime.now());
//            listFileManagement.setDestinationPath(destFilePath + "/" + transactionId);
//            listFileManagement.setModifiedOn(LocalDateTime.now());
//            listFileManagement.setDestinationServer(destServerName);
//            listFileManagement.setOperatorName("ALL");
//            listFileManagement.setRecordCount(totalCount);
//            listFileManagement.setSourceServer(sourceServerName);
//            sftpFileService.sendCopyFileInfo(listFileManagement);
//        }
//        return ;
//    }

    public void saveListManagementEntity(String transactionId, ListType listType, FileType fileType,
                                         String sourceFilePath, String sourceFileName,
                                         Long totalCount) {
        String sourceServerName = appConfig.getSourceServerName();
        List<String> copyDestinationServerName = appConfig.getCopyDestinationServerName();
        List<String> copyDestinationServerPath = appConfig.getCopyDestinationServerPath();
        boolean fileCopyApiStatus = sftpFileService.sendCopyFileInfo(transactionId, listType, fileType, sourceFilePath, sourceFileName, totalCount,
                copyDestinationServerName, copyDestinationServerPath);
        Integer noOfDestination = copyDestinationServerName.size();
        if (!fileCopyApiStatus) {


            for (int i = 0; i < noOfDestination; i++) {
                String destServerName = copyDestinationServerName.get(i);
                String destFilePath = copyDestinationServerPath.get(i);
                ListFileManagement listFileManagement = new ListFileManagement();
                listFileManagement.setFileType(fileType.getIndex());
                listFileManagement.setFileState(1);
                listFileManagement.setFileName(sourceFileName);
                listFileManagement.setFilePath(sourceFilePath);
                listFileManagement.setListType(listType);
                listFileManagement.setCopyStatus(CopyStatus.NEW.getIndex());
                listFileManagement.setCreatedOn(LocalDateTime.now());
                listFileManagement.setDestinationPath(destFilePath + "/" + transactionId);
                listFileManagement.setModifiedOn(LocalDateTime.now());
                listFileManagement.setDestinationServer(destServerName);
                listFileManagement.setOperatorName("ALL");
                listFileManagement.setRecordCount(totalCount);
                listFileManagement.setSourceServer(sourceServerName);
                //            sftpFileService.sendCopyFileInfo(listFileManagement);
                save(listFileManagement);
            }
            return;
        }
    }

}
