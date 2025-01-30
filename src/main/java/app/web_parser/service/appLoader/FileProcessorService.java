package app.web_parser.service.appLoader;

import app.web_parser.alert.AlertService;
import app.web_parser.config.AppConfig;
import app.web_parser.model.app.WebActionDb;
import app.web_parser.model.aud.AppAnalyticsEntity;
import app.web_parser.model.rep.*;
import app.web_parser.repository.rep.*;
import app.web_parser.repository.app.WebActionDbRepository;
import app.web_parser.repository.aud.AppAnalyticsUploaderRepository;
import app.web_parser.service.fileOperations.FileOperations;
import app.web_parser.service.parser.moi.utility.ConfigurableParameter;
import app.web_parser.service.parser.moi.utility.MOIService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FileProcessorService {
    private final Logger logger = LogManager.getLogger(this.getClass());
    @Autowired
    private AndroidDailyActiveDevicesRepository androidDailyActiveDevicesRepository;
    @Autowired
    private AndroidUninstallEventsRepository androidUninstallEventsRepository;
    @Autowired
    private AndroidDeviceStoreListingImpressionsRepository androidDeviceStoreListingImpressionsRepository;
    @Autowired
    private AndroidTotalInstallationsByAppVersionRepository androidTotalInstallationsByAppVersionRepository;
    @Autowired
    private IosActiveDevicesByAppVersionRepository iosActiveDevicesByAppVersionRepository;
    @Autowired
    private IosActiveDevicesByDeviceRepository iosActiveDevicesByDeviceRepository;
    @Autowired
    private IosDeletionsByAppVersionRepository iosDeletionsByAppVersionRepository;
    @Autowired
    private IosDeletionsByDeviceRepository iosDeletionsByDeviceRepository;
    @Autowired
    private IosTotalDownloadsByDeviceRepository iosTotalDownloadsByDeviceRepository;
    @Autowired
    private IosTotalInstallationsByDeviceRepository iosTotalInstallationsByDeviceRepository;
    @Autowired
    private IosTotalInstallationsByAppVersionRepository iosTotalInstallationsByAppVersionRepository;
    @Autowired
    private IosDeviceStoreListingImpressionsByDeviceRepository iosDeviceStoreListingImpressionsByDeviceRepository;
    @Autowired
    private AppConfig appConfig;
    @Autowired
    private FileOperations fileOperations;
    /*@Autowired
    private AppAnalyticsUploaderRepository appAnalyticsUploaderRepository;*/
    @Autowired
    private WebActionDbRepository webActionDbRepository;
    @Autowired
    private MOIService moiService;
    @Autowired
    private AlertService alertService;

    private final AppAnalyticsUploaderRepository appAnalyticsUploaderRepository;

    public FileProcessorService(AppAnalyticsUploaderRepository appAnalyticsUploaderRepository) {
        this.appAnalyticsUploaderRepository = appAnalyticsUploaderRepository;
    }

    @Transactional
    public int processFile(WebActionDb wb, String reportType) throws Exception {
        String filePath = null;
        //String uploadedFileName = "Daily Active Devices.csv";
        String transactionId = wb.getTxnId();
        logger.info("Looking for transactionId: {}", transactionId);
        AppAnalyticsEntity uploader = appAnalyticsUploaderRepository.findByTransactionId(transactionId);
        if (uploader == null) {
            webActionDbRepository.updateWebActionStatus(5, wb.getId());
            appAnalyticsUploaderRepository.updateCountOfRecordsandStatus(transactionId, 0, "Fail");
            throw new IllegalArgumentException("No record found for transaction ID: " + transactionId);
        }
        String uploadedFileName = uploader.getSourceFileName();
        String moiFilePath = appConfig.getMoiFilePath();
        String uploadedFilePath = moiFilePath + "/" + transactionId + "/" + uploadedFileName;
        logger.info("Uploaded file path is {}", uploadedFilePath);
        if (!fileOperations.checkFileExists(uploadedFilePath)) {
            logger.error("Uploaded file does not exists in path {} for transactionId {}", uploadedFilePath, transactionId);
            boolean commonStorage = appConfig.isCommonStorage();
            logger.info("isCommonStorage available : {}", commonStorage);
            if (commonStorage && Objects.nonNull(wb.getTxnId())) {
                webActionDbRepository.updateWebActionStatus(5, wb.getId());
                appAnalyticsUploaderRepository.updateCountOfRecordsandStatus(transactionId, 0, "Fail");
            }
            alertService.raiseAnAlert(transactionId, ConfigurableParameter.FILE_MISSING_ALERT.getValue(), wb.getSubFeature(), transactionId, 0);
            return -1;
        }
        String normalizedReportType = reportType.trim().toLowerCase();
        logger.info("Normalized Report Type: " + normalizedReportType);
        int insertCount = 0;
        switch (normalizedReportType) {
            case "android_daily_active_devices":
                insertCount = processAndroidDailyActiveDevices(uploadedFilePath, wb);
                break;
            case "android_uninstall_events":
                insertCount = processAndroidUninstallEvents(uploadedFilePath, wb);
                break;
            case "android_device_store_listing_impressions":
                insertCount = processAndroidDeviceStoreListingImpressions(uploadedFilePath, wb);
                break;
            case "android_total_installations_by_app_version":
                insertCount = processAndroidTotalInstallationsByAppVersion(uploadedFilePath, wb);
                break;
            case "ios_active_devices_by_app_version":
                insertCount = processIosActiveDevicesByAppVersion(uploadedFilePath, wb);
                break;
            case "ios_active_devices_by_device":
                insertCount = processIosActiveDevicesByDevice(uploadedFilePath, wb);
                break;
            case "ios_deletions_by_app_version":
                insertCount = processIosDeletionsByAppVersion(uploadedFilePath, wb);
                break;
            case "ios_deletions_by_device":
                insertCount = processIosDeletionsByDevice(uploadedFilePath, wb);
                break;
            case "ios_total_downloads_by_device":
                insertCount = processIosTotalDownloadsByDevice(uploadedFilePath, wb);
                break;
            case "ios_total_installations_by_device":
                insertCount = processIosTotalInstallationsByDevice(uploadedFilePath, wb);
                break;
            case "ios_total_installations_by_app_version":
                insertCount = processIosTotalInstallationsByAppVersion(uploadedFilePath, wb);
                break;
            case "ios_device_store_listing_impressions_by_device":
                insertCount = processIosImpressionsByDevice(uploadedFilePath, wb);
                break;
            default:
                throw new IllegalArgumentException("Unsupported report type: " + reportType);
        }
        logger.info("number of records inserted: {}", insertCount);
        int currentState = webActionDbRepository.getWebActionState(wb.getId());
        if (currentState == 5) {
            logger.warn("State is 5 for ID {}: Skipping WebActionDb update and marking AppAnalytics as 'Fail'.", wb.getId());
            appAnalyticsUploaderRepository.updateCountOfRecordsandStatus(transactionId, insertCount, "Fail");
            return insertCount;
        }

        appAnalyticsUploaderRepository.updateCountOfRecordsandStatus(transactionId, insertCount, "Done");
        webActionDbRepository.updateWebActionStatus(4, wb.getId());
        return insertCount;
    }


    @Transactional
    public int processAndroidDailyActiveDevices(String filePath, WebActionDb wb) {
        List<AndroidDailyActiveDevices> recordList = new ArrayList<>(); // List to store valid records
        Map<AndroidDailyActiveDevices, Integer> recordLineMap = new HashMap<>(); // Map to track line numbers for records
        int failureCount = 0; // Counter to track failures
        List<String> failureReasons = new ArrayList<>(); // List to store failure reasons

        List<String> lines = readFileLines(filePath, wb); // Read file lines
        if (lines.isEmpty()) {
            String reason = "File is empty or missing rows!";
            logger.error(reason);
            webActionDbRepository.updateWebActionStatus(5, wb.getId());
            appAnalyticsUploaderRepository.updateCountOfRecordsandStatusandReason(wb.getTxnId(), 0, "Fail", reason);
            return 0; // No records to process
        }

        lines.remove(0); // Remove header row
        int notesMaxLength = 1000;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int lineNumber = i + 2; // Add 2 to account for the header row and 0-based index
            try (Scanner scanner = new Scanner(line)) {
                scanner.useDelimiter(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"); // Regex for splitting CSV

                // Parse fields
                String dateString = scanner.hasNext() ? scanner.next().trim().replaceAll("\"", "") : null;
                String activeDevices = scanner.hasNext() ? scanner.next().trim() : "";
                String notes = scanner.hasNext() ? scanner.next().trim() : "";

                // Truncate notes if its length exceeds the maximum limit
                if (notes.length() > notesMaxLength) {
                    notes = notes.substring(0, notesMaxLength);
                }

                if (dateString == null || activeDevices == null) {
                    throw new IllegalArgumentException("Missing required fields");
                }

                // Parse date
                LocalDate activeDate;
                try {
                    DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);
                    DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                    try {
                        activeDate = LocalDate.parse(dateString, formatter1);
                    } catch (DateTimeParseException e1) {
                        activeDate = LocalDate.parse(dateString, formatter2);
                    }
                } catch (DateTimeParseException e) {
                    failureCount++;
                    String reason = String.format("Incorrect date format (%s) at line %d", dateString, lineNumber);
                    failureReasons.add(reason);
                    throw new IllegalArgumentException(reason, e);
                }

                // Parse active devices count
                int activeDeviceCount;
                if (activeDevices == null || activeDevices.isEmpty()) {
                    failureCount++;
                    String reason = String.format("Missing active devices count at line %d", lineNumber);
                    failureReasons.add(reason);
                    throw new IllegalArgumentException(reason);
                } else if (activeDevices.equals("-")) {
                    activeDeviceCount = 0;
                } else {
                    try {
                        activeDeviceCount = Integer.parseInt(activeDevices);
                    } catch (NumberFormatException e) {
                        failureCount++;
                        String reason = String.format("Invalid active devices count (%s) at line %d", activeDevices, lineNumber);
                        failureReasons.add(reason);
                        throw new IllegalArgumentException(reason, e);
                    }
                }

                // Create and add record to the list
                AndroidDailyActiveDevices record = new AndroidDailyActiveDevices();
                record.setActiveDate(activeDate);
                record.setActiveDevicesCount(activeDeviceCount);
                record.setNotes(notes);

                recordList.add(record);
                recordLineMap.put(record, lineNumber); // Track the line number for this record

            } catch (Exception e) {
                logger.error("Error processing row: {}", line, e);

                // If more than 1 failure, mark as failed and stop processing
                if (failureCount > 0) {
                    logger.error("Multiple failures occurred. Aborting processing.");
                    String allReasons = String.join("; ", failureReasons);
                    webActionDbRepository.updateWebActionStatus(5, wb.getId());
                    appAnalyticsUploaderRepository.updateCountOfRecordsandStatusandReason(wb.getTxnId(), 0, "Fail", allReasons);
                    return 0;
                }
            }
        }

        if (!recordList.isEmpty()) {
            try {
                androidDailyActiveDevicesRepository.saveAll(recordList);
            } catch (Exception e) {
                logger.error("SQL operation failed", e);

                // Extract detailed error message
                String detailedReason = "Database operation failed during save.";
                Throwable rootCause = e.getCause();
                if (rootCause != null && rootCause.getMessage() != null) {
                    detailedReason += " Root cause: " + rootCause.getMessage();
                }

                // Check for specific duplicate entry error
                if (rootCause != null && rootCause.getMessage().contains("Duplicate entry")) {
                    String duplicateDate = rootCause.getMessage().split("'")[1];

                    // Find the line number of the duplicate record
                    Optional<Integer> duplicateLineNumber = recordList.stream().filter(record -> record.getActiveDate().toString().equals(duplicateDate)).map(recordLineMap::get).findFirst();

                    if (duplicateLineNumber.isPresent()) {
                        detailedReason = String.format("Duplicate Date %s at line number %d", duplicateDate, duplicateLineNumber.get());
                    }
                }

                // Update the databases with the failure reason
                webActionDbRepository.updateWebActionStatus(5, wb.getId());
                appAnalyticsUploaderRepository.updateCountOfRecordsandStatusandReason(wb.getTxnId(), 0, "Fail", detailedReason);

                return 0;
            }
        } else {
            logger.warn("No valid records to save.");
        }

        return recordList.size(); // Return the count of successfully processed records
    }


    @Transactional
    public int processAndroidUninstallEvents(String filePath, WebActionDb wb) {
        List<AndroidUninstallEvents> recordList = new ArrayList<>(); // List to store valid records
        Map<AndroidUninstallEvents, Integer> recordLineMap = new HashMap<>(); // Map to track line numbers for records
        int failureCount = 0; // Counter to track failures
        List<String> failureReasons = new ArrayList<>(); // List to store failure reasons

        List<String> lines = readFileLines(filePath, wb); // Read file lines
        if (lines.isEmpty()) {
            logger.error("File is empty or missing rows!");
            webActionDbRepository.updateWebActionStatus(5, wb.getId());
            appAnalyticsUploaderRepository.updateCountOfRecordsandStatus(wb.getTxnId(), 0, "Fail");
            return 0; // No records to process
        }

        lines.remove(0); // Remove header row
        int notesMaxLength = 1000;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int lineNumber = i + 2; // Add 2 to account for the header row and 0-based index
            try (Scanner scanner = new Scanner(line)) {
                scanner.useDelimiter(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"); // Regex for splitting CSV

                // Read and clean fields
                String dateString = scanner.hasNext() ? scanner.next().trim().replaceAll("\"", "") : null;
                String uninstallEventsString = scanner.hasNext() ? scanner.next().trim() : "";
                String notes = scanner.hasNext() ? scanner.next().trim() : "";

                // Truncate notes if its length exceeds the maximum limit
                if (notes.length() > notesMaxLength) {
                    notes = notes.substring(0, notesMaxLength);
                }
                if (dateString == null || uninstallEventsString == null) {
                    failureCount++;
                    String reason = String.format("Missing required fields at line %d", lineNumber);
                    failureReasons.add(reason);
                    throw new IllegalArgumentException(reason);
                }

                // Parse date
                LocalDate uninstallDate;
                try {
                    DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH);
                    DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);
                    if (dateString.contains("-")) {
                        uninstallDate = LocalDate.parse(dateString, formatter1);
                    } else if (dateString.contains(",")) {
                        uninstallDate = LocalDate.parse(dateString, formatter2);
                    } else {
                        failureCount++;
                        String reason = String.format("Unsupported date format (%s) at line %d", dateString, lineNumber);
                        failureReasons.add(reason);
                        throw new DateTimeParseException("Unsupported date format", dateString, 0);
                    }
                } catch (DateTimeParseException e) {
                    failureCount++;
                    String reason = String.format("Invalid date format (%s) at line %d", dateString, lineNumber);
                    failureReasons.add(reason);
                    throw new IllegalArgumentException(reason, e);
                }

                int uninstallEventsCount;

                if (uninstallEventsString == null || uninstallEventsString.trim().isEmpty()) {
                    failureCount++;
                    String reason = String.format("Missing uninstall events count at line %d", lineNumber);
                    failureReasons.add(reason);
                    throw new IllegalArgumentException(reason);
                }

                if (uninstallEventsString.equals("-")) {
                    uninstallEventsCount = 0;
                } else {
                    try {
                        uninstallEventsCount = Integer.parseInt(uninstallEventsString);
                    } catch (NumberFormatException e) {
                        failureCount++;
                        String reason = String.format("Invalid uninstall events count (%s) at line %d", uninstallEventsString, lineNumber);
                        failureReasons.add(reason);
                        throw new IllegalArgumentException(reason, e);
                    }
                }

                // Create and add record to the list
                AndroidUninstallEvents record = new AndroidUninstallEvents();
                record.setUninstallDate(uninstallDate);
                record.setUninstallEvents(uninstallEventsCount);
                record.setNotes(notes);

                recordList.add(record);
                recordLineMap.put(record, lineNumber); // Track the line number for this record

            } catch (Exception e) {
                failureCount++;
                logger.error("Error processing row: {} at line {}", line, lineNumber, e);

                // If more than 1 failure, mark as failed and stop processing
                if (failureCount > 1) {
                    logger.error("Multiple failures occurred. Aborting processing.");
                    String allReasons = String.join("; ", failureReasons);
                    webActionDbRepository.updateWebActionStatus(5, wb.getId());
                    appAnalyticsUploaderRepository.updateCountOfRecordsandStatusandReason(wb.getTxnId(), 0, "Fail", allReasons);
                    return 0;
                }
            }
        }

        if (!recordList.isEmpty()) {
            try {
                androidUninstallEventsRepository.saveAll(recordList);
            } catch (Exception e) {
                logger.error("SQL operation failed", e);

                // Extract detailed error message
                String detailedReason = "Database operation failed during save.";
                Throwable rootCause = e.getCause();
                if (rootCause != null && rootCause.getMessage() != null) {
                    detailedReason += " Root cause: " + rootCause.getMessage();
                }

                // Check for specific duplicate entry error
                if (rootCause != null && rootCause.getMessage().contains("Duplicate entry")) {
                    String duplicateDate = rootCause.getMessage().split("'")[1];

                    // Find the line number of the duplicate record
                    Optional<Integer> duplicateLineNumber = recordList.stream().filter(record -> record.getUninstallDate().toString().equals(duplicateDate)).map(recordLineMap::get).findFirst();

                    if (duplicateLineNumber.isPresent()) {
                        detailedReason = String.format("Duplicate Date %s at line number %d", duplicateDate, duplicateLineNumber.get());
                    }
                }

                // Update the databases with the failure reason
                webActionDbRepository.updateWebActionStatus(5, wb.getId());
                appAnalyticsUploaderRepository.updateCountOfRecordsandStatusandReason(wb.getTxnId(), 0, "Fail", detailedReason);

                return 0;
            }
        } else {
            logger.warn("No valid records to save.");
        }

        return recordList.size(); // Return the count of successfully processed records
    }


    @Transactional
    public int processAndroidDeviceStoreListingImpressions(String filePath, WebActionDb wb) {
        List<AndroidDeviceStoreListingImpressions> recordList = new ArrayList<>(); // List to store valid records
        Map<AndroidDeviceStoreListingImpressions, Integer> recordLineMap = new HashMap<>(); // Map to track line numbers for records
        int failureCount = 0; // Counter to track failures
        List<String> failureReasons = new ArrayList<>(); // List to store failure reasons

        List<String> lines = readFileLines(filePath, wb); // Read file lines
        if (lines.isEmpty()) {
            logger.error("File is empty or missing rows!");
            webActionDbRepository.updateWebActionStatus(5, wb.getId());
            appAnalyticsUploaderRepository.updateCountOfRecordsandStatus(wb.getTxnId(), 0, "Fail");
            return 0; // No records to process
        }

        lines.remove(0); // Remove header row
        int notesMaxLength = 1000;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int lineNumber = i + 2; // Add 2 to account for the header row and 0-based index
            try (Scanner scanner = new Scanner(line)) {
                scanner.useDelimiter(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"); // Regex for splitting CSV

                // Read and clean fields
                String dateString = scanner.hasNext() ? scanner.next().trim().replaceAll("\"", "") : null;
                String devicesListingCountString = scanner.hasNext() ? scanner.next().trim() : "";
                String notes = scanner.hasNext() ? scanner.next().trim() : "";

                // Truncate notes if its length exceeds the maximum limit
                if (notes.length() > notesMaxLength) {
                    notes = notes.substring(0, notesMaxLength);
                }
                if (dateString == null || devicesListingCountString == null) {
                    failureCount++;
                    String reason = String.format("Missing required fields at line %d", lineNumber);
                    failureReasons.add(reason);
                    throw new IllegalArgumentException(reason);
                }

                // Parse date
                LocalDate listingDate;
                try {
                    DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH);
                    DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);
                    if (dateString.contains("-")) {
                        listingDate = LocalDate.parse(dateString, formatter1);
                    } else if (dateString.contains(",")) {
                        listingDate = LocalDate.parse(dateString, formatter2);
                    } else {
                        failureCount++;
                        String reason = String.format("Unsupported date format (%s) at line %d", dateString, lineNumber);
                        failureReasons.add(reason);
                        throw new DateTimeParseException("Unsupported date format", dateString, 0);
                    }
                } catch (DateTimeParseException e) {
                    failureCount++;
                    String reason = String.format("Invalid date format (%s) at line %d", dateString, lineNumber);
                    failureReasons.add(reason);
                    throw new IllegalArgumentException(reason, e);
                }

                // Parse devices listing count
                int devicesListingCount;

                if (devicesListingCountString == null || devicesListingCountString.trim().isEmpty()) {
                    failureCount++;
                    String reason = String.format("Missing devices listing count at line %d", lineNumber);
                    failureReasons.add(reason);
                    throw new IllegalArgumentException(reason);
                }

                if (devicesListingCountString.equals("-")) {
                    devicesListingCount = 0;
                } else {
                    try {
                        devicesListingCount = Integer.parseInt(devicesListingCountString);
                    } catch (NumberFormatException e) {
                        failureCount++;
                        String reason = String.format("Invalid devices listing count (%s) at line %d", devicesListingCountString, lineNumber);
                        failureReasons.add(reason);
                        throw new IllegalArgumentException(reason, e);
                    }
                }


                // Create and add record to the list
                AndroidDeviceStoreListingImpressions record = new AndroidDeviceStoreListingImpressions();
                record.setListingDate(listingDate);
                record.setDevicesListingCount(devicesListingCount);
                record.setNotes(notes);

                recordList.add(record);
                recordLineMap.put(record, lineNumber); // Track the line number for this record

            } catch (Exception e) {
                failureCount++;
                logger.error("Error processing row: {} at line {}", line, lineNumber, e);

                // If more than 1 failure, mark as failed and stop processing
                if (failureCount > 1) {
                    logger.error("Multiple failures occurred. Aborting processing.");
                    String allReasons = String.join("; ", failureReasons);
                    webActionDbRepository.updateWebActionStatus(5, wb.getId());
                    appAnalyticsUploaderRepository.updateCountOfRecordsandStatusandReason(wb.getTxnId(), 0, "Fail", allReasons);
                    return 0;
                }
            }
        }

        if (!recordList.isEmpty()) {
            try {
                androidDeviceStoreListingImpressionsRepository.saveAll(recordList);
            } catch (Exception e) {
                logger.error("SQL operation failed", e);

                // Extract detailed error message
                String detailedReason = "Database operation failed during save.";
                Throwable rootCause = e.getCause();
                if (rootCause != null && rootCause.getMessage() != null) {
                    detailedReason += " Root cause: " + rootCause.getMessage();
                }

                // Check for specific duplicate entry error
                if (rootCause != null && rootCause.getMessage().contains("Duplicate entry")) {
                    String duplicateDate = rootCause.getMessage().split("'")[1];

                    // Find the line number of the duplicate record
                    Optional<Integer> duplicateLineNumber = recordList.stream().filter(record -> record.getListingDate().toString().equals(duplicateDate)).map(recordLineMap::get).findFirst();

                    if (duplicateLineNumber.isPresent()) {
                        detailedReason = String.format("Duplicate Date %s at line number %d", duplicateDate, duplicateLineNumber.get());
                    }
                }

                // Update the databases with the failure reason
                webActionDbRepository.updateWebActionStatus(5, wb.getId());
                appAnalyticsUploaderRepository.updateCountOfRecordsandStatusandReason(wb.getTxnId(), 0, "Fail", detailedReason);

                return 0;
            }
        } else {
            logger.warn("No valid records to save.");
        }

        return recordList.size(); // Return the count of successfully processed records
    }


    @Transactional
    public int processAndroidTotalInstallationsByAppVersion(String filePath, WebActionDb wb) {
        List<AndroidTotalInstallationsByAppVersion> recordList = new ArrayList<>(); // List to store valid records
        Map<AndroidTotalInstallationsByAppVersion, Integer> recordLineMap = new HashMap<>(); // Map to track line numbers for records
        int failureCount = 0; // Counter to track failures
        List<String> failureReasons = new ArrayList<>(); // List to store failure reasons

        List<String> lines = readFileLines(filePath, wb);
        if (lines.isEmpty()) {
            logger.error("File is empty or missing rows!");
            webActionDbRepository.updateWebActionStatus(5, wb.getId());
            appAnalyticsUploaderRepository.updateCountOfRecordsandStatus(wb.getTxnId(), 0, "Fail");
            return 0; // No records to process
        }

        int insertCount = 0;

        // Extract column headers for version numbers
        String headerLine = lines.remove(0);
        String[] headers = headerLine.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        Map<Integer, String> versionMap = new HashMap<>();

        for (int i = 1; i < headers.length - 1; i++) { // Exclude "Date" (0) and "Notes" (last column)
            String header = headers[i].trim().replaceAll("\"", "");
            String versionPart = header.contains(":") ? header.substring(header.indexOf(":") + 1).trim() : header;
            logger.debug("Extracted version for header " + i + ": " + versionPart);
            versionMap.put(i, versionPart);
        }

        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH);
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);

        // Process rows
        for (int lineNumber = 0; lineNumber < lines.size(); lineNumber++) {
            String line = lines.get(lineNumber);
            try {
                // Normalize row: Ensure it matches the header length by adding missing fields
                String[] columns = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                // Add empty values for missing columns
                if (columns.length < headers.length) {
                    columns = Arrays.copyOf(columns, headers.length);
                    Arrays.fill(columns, columns.length - 1, headers.length, ""); // Fill missing values with empty strings
                }

                // Validate column count
                if (columns.length != headers.length) {
                    failureCount++;
                    String reason = String.format("Row has incorrect number of columns at line %d", lineNumber + 2);
                    failureReasons.add(reason);
                    logger.error(reason);
                    continue; // Skip invalid rows and continue with the next one
                }

                // Parse date
                String dateString = columns[0].trim().replaceAll("\"", "");
                LocalDate installDate;
                try {
                    if (dateString.contains("-")) {
                        installDate = LocalDate.parse(dateString, formatter1);
                    } else if (dateString.contains(",")) {
                        installDate = LocalDate.parse(dateString, formatter2);
                    } else {
                        failureCount++;
                        String reason = String.format("Unsupported date format (%s) at line %d", dateString, lineNumber + 2);
                        failureReasons.add(reason);
                        throw new DateTimeParseException("Unsupported date format", dateString, 0);
                    }
                } catch (DateTimeParseException e) {
                    failureCount++;
                    String reason = String.format("Invalid date format (%s) at line %d", dateString, lineNumber + 2);
                    failureReasons.add(reason);
                    logger.error(reason, e);
                    continue; // Skip invalid date rows and continue with the next one
                }

                // Extract notes (allow empty value)
                String notes = columns[columns.length - 1].trim().replaceAll("\"", "");

                int notesMaxLength = 1000;
                if (notes.length() > notesMaxLength) {
                    notes = notes.substring(0, notesMaxLength);
                }
                // Process version data
                // Process version data
                for (int i = 1; i < columns.length - 1; i++) { // Exclude "Date" and "Notes"
                    String value = columns[i].trim();
                    String versionName = versionMap.getOrDefault(i, "Unknown");
                    int installCount;

                    // Check if the value is null or empty, mark as failure
                    if (value == null || value.isEmpty()) {
                        failureCount++;
                        String reason = String.format("Missing install count for version %s at line %d, Date: %s", versionName, lineNumber + 2, columns[0].trim());
                        failureReasons.add(reason);
                        logger.error(reason);
                        continue; // Skip invalid version data rows and continue with the next one
                    }

                    // Handle "-" as zero
                    if (value.equals("-")) {
                        installCount = 0;
                    } else {
                        try {
                            installCount = Integer.parseInt(value);
                        } catch (NumberFormatException e) {
                            failureCount++;
                            String reason = String.format("Invalid number format for value: %s for version %s at line %d, Date: %s", value, versionName, lineNumber + 2, columns[0].trim());
                            failureReasons.add(reason);
                            logger.error(reason, e);
                            continue; // Skip invalid version data rows and continue with the next one
                        }
                    }


                    // Create record and add to list
                    AndroidTotalInstallationsByAppVersion record = new AndroidTotalInstallationsByAppVersion();
                    record.setInstallDate(installDate);
                    record.setInstallVersion(versionName);
                    record.setInstallCount(installCount);
                    record.setNotes(notes); // Handle empty notes gracefully
                    recordList.add(record);
                    recordLineMap.put(record, lineNumber + 2); // Track the line number of the record
                }

            } catch (Exception e) {
                failureCount++;
                String reason = String.format("Error processing row: %s at line %d", line, lineNumber + 2);
                failureReasons.add(reason);
                logger.error(reason, e);
            }
        }

        // After processing all rows, check if there were any failures
        if (failureCount > 0) {
            String allReasons = String.join("; ", failureReasons);
            webActionDbRepository.updateWebActionStatus(5, wb.getId());
            appAnalyticsUploaderRepository.updateCountOfRecordsandStatusandReason(wb.getTxnId(), 0, "Fail", allReasons);
            return 0; // Abort and return 0 if there were any failures
        }

        if (!recordList.isEmpty()) {
            try {
                // Only insert if there were no errors
                androidTotalInstallationsByAppVersionRepository.saveAll(recordList);
            } catch (Exception e) {
                logger.error("SQL operation failed", e);

                String reason = "Unknown error occurred"; // Default reason

                // Log the specific SQL error with the duplicate entry message
                String errorMessage = e.getMessage();
                if (errorMessage.contains("Duplicate entry")) {
                    String[] parts = errorMessage.split("'");
                    if (parts.length >= 2) {
                        String duplicateKey = parts[1];  // Extracting the duplicate key (e.g., 'All app versions-2023-10-06')
                        reason = String.format("Duplicate entry found for entry '%s' at line %d", duplicateKey, lines.size() + 1);
                        logger.error("Duplicate entry error: " + reason);
                    }
                }

                // Update the status to failed
                webActionDbRepository.updateWebActionStatus(5, wb.getId());
                appAnalyticsUploaderRepository.updateCountOfRecordsandStatusandReason(wb.getTxnId(), 0, "Fail", reason);
                return 0;
            }
        } else {
            logger.warn("No valid records to save.");
        }

        return recordList.size(); // Return the count of successfully processed records
    }


    @Transactional
    public int processIosActiveDevicesByAppVersion(String filePath, WebActionDb wb) {
        List<IosActiveDevicesByAppVersion> recordList = new ArrayList<>(); // List to store valid records
        int failureCount = 0; // Counter to track failures
        List<String> failureReasons = new ArrayList<>(); // List to store failure reasons

        List<String> lines = readFileLines(filePath, wb); // Read file lines
        if (lines.size() <= 4) {
            String reason = "File does not contain sufficient data rows!";
            failureReasons.add(reason);
            logger.error(reason);
            webActionDbRepository.updateWebActionStatus(5, wb.getId());
            appAnalyticsUploaderRepository.updateCountOfRecordsandStatusandReason(wb.getTxnId(), 0, "Fail", String.join("; ", failureReasons));
            return 0; // No records to process
        }

        // Extract header line and map versions
        String headerLine = lines.get(4);
        String[] headers = headerLine.split(",");

        List<String> versions = Arrays.stream(headers).skip(1) // Skip the date column
                .map(header -> header.split("\\s")[0].trim()) // Extract version names
                .collect(Collectors.toList());

        // Remove the header and metadata rows
        lines.subList(0, 5).clear();

        // Define possible date formats
        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("M/d/yy");
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("MM-dd-yyyy");

        for (int lineNumber = 0; lineNumber < lines.size(); lineNumber++) {
            String line = lines.get(lineNumber);
            try {
                String[] values = line.split(",", -1); // -1 includes trailing empty fields
                String dateString = values[0].trim();

                // Parse event date
                LocalDate eventDate;
                try {
                    if (dateString.contains("/")) {
                        eventDate = LocalDate.parse(dateString, formatter1);
                    } else if (dateString.contains("-")) {
                        eventDate = LocalDate.parse(dateString, formatter2);
                    } else {
                        throw new DateTimeParseException("Unknown date format", dateString, 0);
                    }
                } catch (DateTimeParseException e) {
                    failureCount++;
                    String reason = String.format("Invalid date format: %s at line %d", dateString, lineNumber + 6);
                    failureReasons.add(reason);
                    logger.error(reason, e);
                    continue; // Skip this line and continue with the next one
                }

                // Process version data
                for (int i = 1; i < values.length; i++) {
                    String value = values[i].trim();
                    String versionName = versions.get(i - 1); // Retrieve the version name

                    // Handle missing or empty values
                    if (value.isEmpty()) {
                        failureCount++;
                        String reason = String.format("Missing value for version: %s at line %d", versionName, lineNumber + 6);
                        failureReasons.add(reason);
                        logger.warn(reason + " | Line: " + line);
                        continue; // Skip to the next value
                    }

                    // Parse the value or handle "-" as zero
                    int eventTotal;
                    if (value.equals("-")) {
                        eventTotal = 0;
                    } else {
                        try {
                            eventTotal = (int) Double.parseDouble(value);
                        } catch (NumberFormatException e) {
                            failureCount++;
                            String reason = String.format("Invalid active devices count: %s for version %s at line %d", value, versionName, lineNumber + 6);
                            failureReasons.add(reason);
                            logger.error(reason, e);
                            continue; // Skip this version entry and move to the next one
                        }
                    }

                    // Create and add record to the list
                    IosActiveDevicesByAppVersion record = new IosActiveDevicesByAppVersion();
                    record.setEventDate(eventDate);
                    record.setVersion(versions.get(i - 1));
                    record.setEventTotal(eventTotal);

                    recordList.add(record);
                }

            } catch (Exception e) {
                failureCount++;
                String reason = String.format("Error processing row: %s at line %d", line, lineNumber + 5);
                failureReasons.add(reason);
                logger.error(reason, e);


            }
        }
        if (failureCount > 0) {
            logger.error("Multiple failures occurred. Aborting processing.");
            webActionDbRepository.updateWebActionStatus(5, wb.getId());
            appAnalyticsUploaderRepository.updateCountOfRecordsandStatusandReason(wb.getTxnId(), 0, "Fail", String.join("; ", failureReasons));
            return 0;
        }

        if (!recordList.isEmpty()) {
            try {
                iosActiveDevicesByAppVersionRepository.saveAll(recordList);
            } catch (Exception e) {
                logger.error("SQL operation failed", e);

                String reason = "Unknown error occurred"; // Default reason

                // Log the specific SQL error with the duplicate entry message
                String errorMessage = e.getMessage();
                if (errorMessage.contains("Duplicate entry")) {
                    String[] parts = errorMessage.split("'");

                    if (parts.length >= 2) {
                        String duplicateKey = parts[1];  // Extracting the duplicate key (e.g., 'All app versions-2023-10-06')
                        reason = String.format("Duplicate entry found for entry '%s' at line %d.", duplicateKey, lines.size() + 6);
                        logger.error("Duplicate entry error: " + reason);
                    }
                }

                // Update the status to failed
                webActionDbRepository.updateWebActionStatus(5, wb.getId());
                appAnalyticsUploaderRepository.updateCountOfRecordsandStatusandReason(wb.getTxnId(), 0, "Fail", reason);
                return 0;
            }
        } else {
            logger.warn("No valid records to save.");
        }

        return recordList.size(); // Return the count of successfully processed records
    }


    private int processIosActiveDevicesByDevice(String filePath, WebActionDb wb) throws Exception {
        List<IosActiveDevicesByDevice> recordList = new ArrayList<>(); // List to store valid records
        int failureCount = 0; // Counter to track failures
        List<String> failureReasons = new ArrayList<>(); // List to store failure reasons
        List<String> lines = readFileLines(filePath, wb);
        int insertCount = 0;

        // Extract header line and device names
        String headerLine = lines.get(4);
        String[] headerValues = headerLine.split(",");
        List<String> deviceNames = new ArrayList<>();
        for (int i = 1; i < headerValues.length; i++) {
            deviceNames.add(headerValues[i].trim().replace(" Active Devices", ""));
        }

        lines.subList(0, 5).clear(); // Remove header and metadata rows

        // Define date format
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M/d/yy");

        // Process each data row
        for (int lineNumber = 0; lineNumber < lines.size(); lineNumber++) {
            String line = lines.get(lineNumber);
            String[] values = line.split(",");
            String date = values[0].trim();

            try {
                LocalDate eventDate = LocalDate.parse(date, dateFormatter);

                // Process version data (starting from column 1)
                for (int i = 1; i < values.length; i++) {
                    String value = values[i].trim();
                    String deviceName = deviceNames.get(i - 1); // Retrieve the device name

                    // Treat "-" as 0 and handle invalid or empty values
                    int eventTotal;
                    if (value.equals("-")) {
                        eventTotal = 0;
                    } else if (value.isEmpty()) {
                        failureCount++;
                        String reason = String.format("Empty value for device %s at line %d", deviceName, lineNumber + 6);
                        failureReasons.add(reason);
                        logger.error(reason);
                        continue; // Skip this device entry and move to the next
                    } else {
                        try {
                            eventTotal = (int) Double.parseDouble(value);
                        } catch (NumberFormatException e) {
                            failureCount++;
                            String reason = String.format("Invalid number format: %s for device %s at line %d", value, deviceName, lineNumber + 6);
                            failureReasons.add(reason);
                            logger.error(reason, e);
                            continue; // Skip this device entry and move to the next
                        }
                    }

                    // Create and save the record
                    IosActiveDevicesByDevice record = new IosActiveDevicesByDevice();
                    record.setEventDate(eventDate);
                    record.setDeviceName(deviceNames.get(i - 1));
                    record.setEventTotal(eventTotal);

                    recordList.add(record);
                    insertCount++;
                }
            } catch (DateTimeParseException e) {
                failureCount++;
                String reason = String.format("Invalid date format: %s at line %d", date, lineNumber + 6);
                failureReasons.add(reason);
                logger.error(reason, e);


            }
        }
// If more than 1 failure, mark as failed and stop processing
        if (failureCount > 1) {
            logger.error("Multiple failures occurred. Aborting processing.");
            webActionDbRepository.updateWebActionStatus(5, wb.getId());
            appAnalyticsUploaderRepository.updateCountOfRecordsandStatusandReason(wb.getTxnId(), 0, "Fail", String.join("; ", failureReasons));
            return 0;
        }
        if (!recordList.isEmpty()) {
            try {
                iosActiveDevicesByDeviceRepository.saveAll(recordList);
            } catch (Exception e) {
                logger.error("SQL operation failed", e);

                String reason = "Unknown error occurred"; // Default reason

                // Log the specific SQL error with the duplicate entry message
                String errorMessage = e.getMessage();
                if (errorMessage.contains("Duplicate entry")) {
                    String[] parts = errorMessage.split("'");

                    if (parts.length >= 2) {
                        String duplicateKey = parts[1];  // Extracting the duplicate key (e.g., 'All app versions-2023-10-06')
                        reason = String.format("Duplicate entry found for key '%s' at line %d.", duplicateKey, lines.size() + 6);
                        logger.error("Duplicate entry error: " + reason);
                    }
                }

                // Update the status to failed
                webActionDbRepository.updateWebActionStatus(5, wb.getId());
                appAnalyticsUploaderRepository.updateCountOfRecordsandStatusandReason(wb.getTxnId(), 0, "Fail", reason);
                return 0;
            }
        } else {
            logger.warn("No valid records to save.");
        }

        return recordList.size(); // Return the count of successfully processed records
    }


    private int processIosDeletionsByAppVersion(String filePath, WebActionDb wb) throws Exception {
        List<IosDeletionsByAppVersion> recordList = new ArrayList<>(); // List to store valid records
        int failureCount = 0;
        List<String> failureReasons = new ArrayList<>(); // List to store failure reasons
        List<String> lines = readFileLines(filePath, wb);
        int insertCount = 0;

        // Extract header line and versions
        String headerLine = lines.get(4);
        String[] headerValues = headerLine.split(",");
        List<String> versions = new ArrayList<>();
        for (int i = 1; i < headerValues.length; i++) {
            versions.add(headerValues[i].trim().replace(" (iOS) Deletions", ""));
        }

        lines.subList(0, 5).clear(); // Remove header and metadata rows

        // Define date format
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M/d/yy");

        // Process each data row
        for (int lineNumber = 0; lineNumber < lines.size(); lineNumber++) {
            String line = lines.get(lineNumber);
            String[] values = line.split(",");
            String date = values[0].trim();

            try {
                LocalDate eventDate = LocalDate.parse(date, dateFormatter);

                // Process version data (starting from column 1)
                for (int i = 1; i < values.length; i++) {
                    String value = values[i].trim();
                    String versionName = versions.get(i - 1); // Retrieve the version name

                    // Treat "-" as 0 and handle invalid or empty values
                    int eventTotal;
                    if (value.equals("-")) {
                        eventTotal = 0;
                    } else if (value.isEmpty()) {
                        failureCount++;
                        String reason = String.format("Empty value for version %s at line %d", versionName, lineNumber + 6);
                        failureReasons.add(reason);
                        logger.error(reason);
                        continue; // Skip this version entry and move to the next
                    } else {
                        try {
                            eventTotal = Integer.parseInt(value);
                        } catch (NumberFormatException e) {
                            failureCount++;
                            String reason = String.format("Invalid number format: %s for version %s at line %d", value, versionName, lineNumber + 6);
                            failureReasons.add(reason);
                            logger.error(reason, e);
                            continue; // Skip this version entry and move to the next
                        }
                    }

                    // Create and save the record
                    IosDeletionsByAppVersion record = new IosDeletionsByAppVersion();
                    record.setEventDate(eventDate);
                    record.setVersion(versions.get(i - 1));
                    record.setEventTotal(eventTotal);

                    recordList.add(record);
                    insertCount++;
                }
            } catch (DateTimeParseException e) {
                failureCount++;
                String reason = String.format("Invalid date format: %s at line %d", date, lineNumber + 6);
                failureReasons.add(reason);
                logger.error(reason, e);


            }
        }
// If more than 1 failure, mark as failed and stop processing
        if (failureCount > 1) {
            logger.error("Multiple failures occurred. Aborting processing.");
            webActionDbRepository.updateWebActionStatus(5, wb.getId());
            appAnalyticsUploaderRepository.updateCountOfRecordsandStatusandReason(wb.getTxnId(), 0, "Fail", String.join("; ", failureReasons));
            return 0;
        }
        // Save records if any are valid
        if (!recordList.isEmpty()) {
            try {
                iosDeletionsByAppVersionRepository.saveAll(recordList);
            } catch (Exception e) {
                logger.error("SQL operation failed", e);

                String reason = "Unknown error occurred"; // Default reason

                // Log the specific SQL error with the duplicate entry message
                String errorMessage = e.getMessage();
                if (errorMessage.contains("Duplicate entry")) {
                    String[] parts = errorMessage.split("'");

                    if (parts.length >= 2) {
                        String duplicateKey = parts[1];  // Extracting the duplicate key (e.g., 'All app versions-2023-10-06')
                        reason = String.format("Duplicate entry found for key '%s' at line %d.", duplicateKey, lines.size() + 6);
                        logger.error("Duplicate entry error: " + reason);
                    }
                }

                // Update the status to failed
                webActionDbRepository.updateWebActionStatus(5, wb.getId());
                appAnalyticsUploaderRepository.updateCountOfRecordsandStatusandReason(wb.getTxnId(), 0, "Fail", reason);
                return 0;
            }
        } else {
            logger.warn("No valid records to save.");
        }

        return recordList.size(); // Return the count of successfully processed records
    }

    private int processIosDeletionsByDevice(String filePath, WebActionDb wb) throws Exception {
        List<IosDeletionsByDevice> recordList = new ArrayList<>(); // List to store valid records
        int failureCount = 0;
        List<String> failureReasons = new ArrayList<>(); // List to store failure reasons
        List<String> lines = readFileLines(filePath, wb);
        int insertCount = 0;

        // Extract header line and devices
        String headerLine = lines.get(4);
        String[] headerValues = headerLine.split(",");
        List<String> devices = new ArrayList<>();
        for (int i = 1; i < headerValues.length; i++) {
            devices.add(headerValues[i].trim().replace(" Deletions", ""));
        }

        lines.subList(0, 5).clear(); // Remove header and metadata rows

        // Define date format
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M/d/yy");

        // Process each data row
        for (int lineNumber = 0; lineNumber < lines.size(); lineNumber++) {
            String line = lines.get(lineNumber);
            String[] values = line.split(",");
            String date = values[0].trim();

            try {
                LocalDate eventDate = LocalDate.parse(date, dateFormatter);

                // Process device data (starting from column 1)
                for (int i = 1; i < values.length; i++) {
                    String value = values[i].trim();
                    String deviceName = devices.get(i - 1); // Retrieve the device name

                    // Treat "-" or empty as 0
                    int eventTotal;
                    if (value.equals("-")) {
                        eventTotal = 0;
                    } else if (value.isEmpty()) {
                        failureCount++;
                        String reason = String.format("Empty value for device %s at line %d", deviceName, lineNumber + 6);
                        failureReasons.add(reason);
                        logger.error(reason);
                        continue; // Skip this device entry and move to the next
                    } else {
                        try {
                            eventTotal = (int) Double.parseDouble(value);
                        } catch (NumberFormatException e) {
                            failureCount++;
                            String reason = String.format("Invalid number format: %s for device %s at line %d", value, deviceName, lineNumber + 6);
                            failureReasons.add(reason);
                            logger.error(reason, e);
                            continue; // Skip this device entry and move to the next
                        }
                    }

                    // Create and save the record
                    IosDeletionsByDevice record = new IosDeletionsByDevice();
                    record.setEventDate(eventDate);
                    record.setDeviceName(devices.get(i - 1));
                    record.setEventTotal(eventTotal);

                    recordList.add(record);
                    insertCount++;
                }
            } catch (DateTimeParseException e) {
                failureCount++;
                String reason = String.format("Invalid date format: %s at line %d", date, lineNumber + 6);
                failureReasons.add(reason);
                logger.error(reason, e);


            }
        }
// If more than 1 failure, mark as failed and stop processing
        if (failureCount > 1) {
            logger.error("Multiple failures occurred. Aborting processing.");
            webActionDbRepository.updateWebActionStatus(5, wb.getId());
            appAnalyticsUploaderRepository.updateCountOfRecordsandStatusandReason(wb.getTxnId(), 0, "Fail", String.join("; ", failureReasons));
            return 0;
        }
        // Save records if any are valid
        if (!recordList.isEmpty()) {
            try {
                iosDeletionsByDeviceRepository.saveAll(recordList);
            } catch (Exception e) {
                logger.error("SQL operation failed", e);

                String reason = "Unknown error occurred"; // Default reason

                // Log the specific SQL error with the duplicate entry message
                String errorMessage = e.getMessage();
                if (errorMessage.contains("Duplicate entry")) {
                    String[] parts = errorMessage.split("'");

                    if (parts.length >= 2) {
                        String duplicateKey = parts[1];  // Extracting the duplicate key (e.g., 'All app versions-2023-10-06')
                        reason = String.format("Duplicate entry found for key '%s' at line %d.", duplicateKey, lines.size() + 6);
                        logger.error("Duplicate entry error: " + reason);
                    }
                }

                // Update the status to failed
                webActionDbRepository.updateWebActionStatus(5, wb.getId());
                appAnalyticsUploaderRepository.updateCountOfRecordsandStatusandReason(wb.getTxnId(), 0, "Fail", reason);
                return 0;
            }
        } else {
            logger.warn("No valid records to save.");
        }

        return recordList.size(); // Return the count of successfully processed records
    }

    private int processIosTotalDownloadsByDevice(String filePath, WebActionDb wb) throws Exception {
        List<IosTotalDownloadsByDevice> recordList = new ArrayList<>(); // List to store valid records
        int failureCount = 0;
        List<String> failureReasons = new ArrayList<>(); // List to store failure reasons
        List<String> lines = readFileLines(filePath, wb);
        int insertCount = 0;

        // Extract header line and devices
        String headerLine = lines.get(4);
        String[] headerValues = headerLine.split(",");
        List<String> devices = new ArrayList<>();
        for (int i = 1; i < headerValues.length; i++) {
            devices.add(headerValues[i].trim().replace(" Total Downloads", ""));
        }

        lines.subList(0, 5).clear(); // Remove header and metadata rows

        // Define date format
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M/d/yy");

        // Process each data row
        for (int lineNumber = 0; lineNumber < lines.size(); lineNumber++) {
            String line = lines.get(lineNumber);
            String[] values = line.split(",");
            String date = values[0].trim();

            try {
                LocalDate eventDate = LocalDate.parse(date, dateFormatter);

                // Process device data (starting from column 1)
                for (int i = 1; i < values.length; i++) {
                    String value = values[i].trim();
                    String deviceName = devices.get(i - 1); // Retrieve the device name

                    // Treat "-" or empty as 0
                    int eventTotal;
                    if (value.equals("-")) {
                        eventTotal = 0;
                    } else if (value.isEmpty()) {
                        failureCount++;
                        String reason = String.format("Empty value for device %s at line %d", deviceName, lineNumber + 6);
                        failureReasons.add(reason);
                        logger.error(reason);
                        continue; // Skip this device entry and move to the next
                    } else {
                        try {
                            eventTotal = (int) Double.parseDouble(value);
                        } catch (NumberFormatException e) {
                            failureCount++;
                            String reason = String.format("Invalid number format: %s for device %s at line %d", value, deviceName, lineNumber + 6);
                            failureReasons.add(reason);
                            logger.error(reason, e);
                            continue; // Skip this device entry and move to the next
                        }
                    }

                    // Create and save the record
                    IosTotalDownloadsByDevice record = new IosTotalDownloadsByDevice();
                    record.setEventDate(eventDate);
                    record.setDeviceName(devices.get(i - 1));
                    record.setEventTotal(eventTotal);

                    recordList.add(record);
                    insertCount++;
                }
            } catch (DateTimeParseException e) {
                failureCount++;
                String reason = String.format("Invalid date format: %s at line %d", date, lineNumber + 6);
                failureReasons.add(reason);
                logger.error(reason, e);


            }
        }
// If more than 1 failure, mark as failed and stop processing
        if (failureCount > 1) {
            logger.error("Multiple failures occurred. Aborting processing.");
            webActionDbRepository.updateWebActionStatus(5, wb.getId());
            appAnalyticsUploaderRepository.updateCountOfRecordsandStatusandReason(wb.getTxnId(), 0, "Fail", String.join("; ", failureReasons));
            return 0;
        }
        // Save records if any are valid
        if (!recordList.isEmpty()) {
            try {
                iosTotalDownloadsByDeviceRepository.saveAll(recordList);
            } catch (Exception e) {
                logger.error("SQL operation failed", e);

                String reason = "Unknown error occurred"; // Default reason

                // Log the specific SQL error with the duplicate entry message
                String errorMessage = e.getMessage();
                if (errorMessage.contains("Duplicate entry")) {
                    String[] parts = errorMessage.split("'");

                    if (parts.length >= 2) {
                        String duplicateKey = parts[1];  // Extracting the duplicate key (e.g., 'All app versions-2023-10-06')
                        reason = String.format("Duplicate entry found for key '%s' at line %d.", duplicateKey, lines.size() + 6);
                        logger.error("Duplicate entry error: " + reason);
                    }
                }

                // Update the status to failed
                webActionDbRepository.updateWebActionStatus(5, wb.getId());
                appAnalyticsUploaderRepository.updateCountOfRecordsandStatusandReason(wb.getTxnId(), 0, "Fail", reason);
                return 0;
            }
        } else {
            logger.warn("No valid records to save.");
        }

        return recordList.size(); // Return the count of successfully processed records
    }

    private int processIosTotalInstallationsByDevice(String filePath, WebActionDb wb) throws Exception {
        List<IosTotalInstallationsByDevice> recordList = new ArrayList<>(); // List to store valid records
        int failureCount = 0;
        List<String> failureReasons = new ArrayList<>(); // List to store failure reasons
        List<String> lines = readFileLines(filePath, wb);
        int insertCount = 0;

        // Extract header line and devices
        String headerLine = lines.get(4);
        String[] headerValues = headerLine.split(",");
        List<String> devices = new ArrayList<>();
        for (int i = 1; i < headerValues.length; i++) {
            devices.add(headerValues[i].trim().replace(" Installations", ""));
        }

        lines.subList(0, 5).clear(); // Remove header and metadata rows

        // Define date format
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M/d/yy");

        // Process each data row
        for (int lineNumber = 0; lineNumber < lines.size(); lineNumber++) {
            String line = lines.get(lineNumber);
            String[] values = line.split(",");
            String date = values[0].trim();

            try {
                LocalDate eventDate = LocalDate.parse(date, dateFormatter);

                // Process device data (starting from column 1)
                for (int i = 1; i < values.length; i++) {
                    String value = values[i].trim();
                    String deviceName = devices.get(i - 1); // Retrieve the device name

                    // Treat "-" or empty as 0
                    int eventTotal;
                    if (value.equals("-")) {
                        eventTotal = 0;
                    } else if (value.isEmpty()) {
                        failureCount++;
                        String reason = String.format("Empty value for device %s at line %d", deviceName, lineNumber + 6);
                        failureReasons.add(reason);
                        logger.error(reason);
                        continue; // Skip this device entry and move to next
                    } else {
                        try {
                            eventTotal = (int) Double.parseDouble(value);
                        } catch (NumberFormatException e) {
                            failureCount++;
                            String reason = String.format("Invalid number format: %s for device %s at line %d", value, deviceName, lineNumber + 6);
                            failureReasons.add(reason);
                            logger.error(reason, e);
                            continue; // Skip this device entry and move to the next
                        }
                    }
                    // Create and save the record
                    IosTotalInstallationsByDevice record = new IosTotalInstallationsByDevice();
                    record.setEventDate(eventDate);
                    record.setDeviceName(devices.get(i - 1));
                    record.setEventTotal(eventTotal);

                    recordList.add(record);
                    insertCount++;
                }
            } catch (DateTimeParseException e) {
                failureCount++;
                String reason = String.format("Invalid date format: %s at line %d", date, lineNumber + 6);
                failureReasons.add(reason);
                logger.error(reason, e);


            }
        }
// If more than 1 failure, mark as failed and stop processing
        if (failureCount > 1) {
            logger.error("Multiple failures occurred. Aborting processing.");
            webActionDbRepository.updateWebActionStatus(5, wb.getId());
            appAnalyticsUploaderRepository.updateCountOfRecordsandStatusandReason(wb.getTxnId(), 0, "Fail", String.join("; ", failureReasons));
            return 0;
        }
        // Save records if any are valid
        if (!recordList.isEmpty()) {
            try {
                iosTotalInstallationsByDeviceRepository.saveAll(recordList);
            } catch (Exception e) {
                logger.error("SQL operation failed", e);

                String reason = "Unknown error occurred"; // Default reason

                // Log the specific SQL error with the duplicate entry message
                String errorMessage = e.getMessage();
                if (errorMessage.contains("Duplicate entry")) {
                    String[] parts = errorMessage.split("'");

                    if (parts.length >= 2) {
                        String duplicateKey = parts[1];  // Extracting the duplicate key (e.g., 'All app versions-2023-10-06')
                        reason = String.format("Duplicate entry found for key '%s' at line %d.", duplicateKey, lines.size() + 6);
                        logger.error("Duplicate entry error: " + reason);
                    }
                }

                // Update the status to failed
                webActionDbRepository.updateWebActionStatus(5, wb.getId());
                appAnalyticsUploaderRepository.updateCountOfRecordsandStatusandReason(wb.getTxnId(), 0, "Fail", reason);
                return 0;
            }
        } else {
            logger.warn("No valid records to save.");
        }

        return recordList.size(); // Return the count of successfully processed records
    }

    private int processIosTotalInstallationsByAppVersion(String filePath, WebActionDb wb) throws Exception {
        List<IosTotalInstallationsByAppVersion> recordList = new ArrayList<>(); // List to store valid records
        int failureCount = 0;
        List<String> failureReasons = new ArrayList<>(); // List to store failure reasons
        List<String> lines = readFileLines(filePath, wb);
        int insertCount = 0;

        // Extract header line and versions
        String headerLine = lines.get(4);
        String[] headerValues = headerLine.split(",");
        List<String> versions = new ArrayList<>();
        for (int i = 1; i < headerValues.length; i++) {
            String version = headerValues[i].trim();
            version = version.replaceAll(" \\(.*\\)", "").replaceAll(" Installations", "").trim();
            versions.add(version);
        }

        lines.subList(0, 5).clear(); // Remove header and metadata rows

        // Define date format
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M/d/yy");

        // Process each data row
        for (int lineNumber = 0; lineNumber < lines.size(); lineNumber++) {
            String line = lines.get(lineNumber);
            String[] values = line.split(",");
            String date = values[0].trim();

            try {
                LocalDate eventDate = LocalDate.parse(date, dateFormatter);

                // Process version data (starting from column 1)
                for (int i = 1; i < values.length; i++) {
                    String value = values[i].trim();
                    String versionName = versions.get(i - 1); // Retrieve the version name

                    // Treat "-" or empty as 0
                    int eventTotal;
                    if (value.equals("-")) {
                        eventTotal = 0;
                    } else if (value.isEmpty()) {
                        failureCount++;
                        String reason = String.format("Empty value for version %s at line %d", versionName, lineNumber + 6);
                        failureReasons.add(reason);
                        logger.error(reason);
                        continue; // Skip this version entry and move to next
                    } else {
                        try {
                            eventTotal = (int) Double.parseDouble(value);
                        } catch (NumberFormatException e) {
                            failureCount++;
                            String reason = String.format("Invalid number format: %s for version %s at line %d", value, versionName, lineNumber + 6);
                            failureReasons.add(reason);
                            logger.error(reason, e);
                            continue; // Skip this version entry and move to next
                        }
                    }

                    // Create and save the record
                    IosTotalInstallationsByAppVersion record = new IosTotalInstallationsByAppVersion();
                    record.setEventDate(eventDate);
                    record.setVersion(versions.get(i - 1));
                    record.setEventTotal(eventTotal);

                    recordList.add(record);
                    insertCount++;
                }
            } catch (Exception e) {
                failureCount++;
                String reason = String.format("Error processing line: %s at line %d", line, lineNumber + 6);
                failureReasons.add(reason);
                logger.error(reason, e);


            }
        }
// If more than 1 failure, mark as failed and stop processing
        if (failureCount > 1) {
            logger.error("Multiple failures occurred. Aborting processing.");
            webActionDbRepository.updateWebActionStatus(5, wb.getId());
            appAnalyticsUploaderRepository.updateCountOfRecordsandStatusandReason(wb.getTxnId(), 0, "Fail", String.join("; ", failureReasons));
            return 0;
        }
        // Save records if any are valid
        if (!recordList.isEmpty()) {
            try {
                iosTotalInstallationsByAppVersionRepository.saveAll(recordList);
            } catch (Exception e) {
                logger.error("SQL operation failed", e);

                String reason = "Unknown error occurred"; // Default reason

                // Log the specific SQL error with the duplicate entry message
                String errorMessage = e.getMessage();
                if (errorMessage.contains("Duplicate entry")) {
                    String[] parts = errorMessage.split("'");

                    if (parts.length >= 2) {
                        String duplicateKey = parts[1];  // Extracting the duplicate key (e.g., 'All app versions-2023-10-06')
                        reason = String.format("Duplicate entry found for key '%s' at line %d.", duplicateKey, lines.size() + 6);
                        logger.error("Duplicate entry error: " + reason);
                    }
                }

                // Update the status to failed
                webActionDbRepository.updateWebActionStatus(5, wb.getId());
                appAnalyticsUploaderRepository.updateCountOfRecordsandStatusandReason(wb.getTxnId(), 0, "Fail", reason);
                return 0;
            }
        } else {
            logger.warn("No valid records to save.");
        }

        return recordList.size(); // Return the count of successfully processed records
    }

    private int processIosImpressionsByDevice(String filePath, WebActionDb wb) throws Exception {
        List<IosDeviceStoreListingImpressionsByDevice> recordList = new ArrayList<>(); // List to store valid records
        int failureCount = 0;
        List<String> failureReasons = new ArrayList<>(); // List to store failure reasons
        List<String> lines = readFileLines(filePath, wb);
        int insertCount = 0;

        // Extract header line and devices
        String headerLine = lines.get(4);
        String[] headerValues = headerLine.split(",");
        List<String> devices = new ArrayList<>();
        for (int i = 1; i < headerValues.length; i++) {
            String device = headerValues[i].trim();
            device = device.replaceAll(" \\(Unique Devices\\)", "").trim();
            devices.add(device);
        }

        lines.subList(0, 5).clear(); // Remove header and metadata rows

        // Define date format
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M/d/yy");

        // Process each data row
        for (int lineNumber = 0; lineNumber < lines.size(); lineNumber++) {
            String line = lines.get(lineNumber);
            String[] values = line.split(",");
            String date = values[0].trim();

            try {
                LocalDate eventDate = LocalDate.parse(date, dateFormatter);

                // Process device data (starting from column 1)
                for (int i = 1; i < values.length; i++) {
                    String value = values[i].trim();
                    String deviceName = devices.get(i - 1); // Retrieve the device name

                    // Treat "-" or empty as 0
                    int eventTotal;
                    if (value.equals("-")) {
                        eventTotal = 0;
                    } else if (value.isEmpty()) {
                        failureCount++;
                        String reason = String.format("Empty value for device %s at line %d", deviceName, lineNumber + 6);
                        failureReasons.add(reason);
                        logger.error(reason);
                        continue; // Skip this device entry and move to next
                    } else {
                        try {
                            eventTotal = (int) Double.parseDouble(value);
                        } catch (NumberFormatException e) {
                            failureCount++;
                            String reason = String.format("Invalid number format: %s for device %s at line %d", value, deviceName, lineNumber + 6);
                            failureReasons.add(reason);
                            logger.error(reason, e);
                            continue; // Skip this device entry and move to next
                        }
                    }

                    // Create and save the record
                    IosDeviceStoreListingImpressionsByDevice record = new IosDeviceStoreListingImpressionsByDevice();
                    record.setEventDate(eventDate);
                    record.setDeviceName(devices.get(i - 1));
                    record.setEventTotal(eventTotal);

                    recordList.add(record);
                    insertCount++;
                }
            } catch (Exception e) {
                failureCount++;
                String reason = String.format("Error processing line: %s at line %d", line, lineNumber + 6);
                failureReasons.add(reason);
                logger.error(reason, e);


            }
        }
// If more than 1 failure, mark as failed and stop processing
        if (failureCount > 1) {
            logger.error("Multiple failures occurred. Aborting processing.");
            webActionDbRepository.updateWebActionStatus(5, wb.getId());
            appAnalyticsUploaderRepository.updateCountOfRecordsandStatusandReason(wb.getTxnId(), 0, "Fail", String.join("; ", failureReasons));
            return 0;
        }
        // Save records if any are valid
        if (!recordList.isEmpty()) {
            try {
                iosDeviceStoreListingImpressionsByDeviceRepository.saveAll(recordList);
            } catch (Exception e) {
                logger.error("SQL operation failed", e);

                String reason = "Unknown error occurred"; // Default reason

                // Log the specific SQL error with the duplicate entry message
                String errorMessage = e.getMessage();
                if (errorMessage.contains("Duplicate entry")) {
                    String[] parts = errorMessage.split("'");

                    if (parts.length >= 2) {
                        String duplicateKey = parts[1];  // Extracting the duplicate key (e.g., 'All app versions-2023-10-06')
                        reason = String.format("Duplicate entry found for key '%s' at line %d.", duplicateKey, lines.size() + 6);
                        logger.error("Duplicate entry error: " + reason);
                    }
                }

                // Update the status to failed
                webActionDbRepository.updateWebActionStatus(5, wb.getId());
                appAnalyticsUploaderRepository.updateCountOfRecordsandStatusandReason(wb.getTxnId(), 0, "Fail", reason);
                return 0;
            }
        } else {
            logger.warn("No valid records to save.");
        }

        return recordList.size(); // Return the count of successfully processed records
    }

    private List<String> readFileLines(String filePath, WebActionDb wb) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            webActionDbRepository.updateWebActionStatus(5, wb.getId());
            appAnalyticsUploaderRepository.updateCountOfRecordsandStatus(wb.getTxnId(), 0, "Fail");
            logger.error("Error reading file: " + filePath, e);
        }
        return lines;
    }

}
