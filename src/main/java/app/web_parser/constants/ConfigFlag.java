package app.web_parser.constants;

public enum ConfigFlag {

    msgForAlreadyExistsInBlackList,
    msgForAlreadyExistsInExceptionList,
    msgForAlreadyExistsInBlockedTac,

    msgForNotExistsInBlackList,

    msgForNotExistsInExceptionList,

    msgForNotExistsInBlockedTac,

    msgForAddedInBlackList,
    msgForAddedInExceptionList,
    msgForAddedInBlockedTac,

    msgForDeletedInBlackList,

    msgForDeletedInExceptionList,

    msgForDeletedInBlockedTac,

    msgForRemarksForInternalErrorInTA,

    msgForRemarksForRecordsCountErrorInTA,
    msgForRemarksForDataFormatErrorInTA,

    msgForRemarksForSuccessInTA,
    msgForRemarksForInternalErrorInQA,
    msgForRemarksForRecordsCountErrorInQA,
    msgForRemarksForDataFormatErrorInQA,
    msgForRemarksForSuccessInQA,
    msgForRemarksForInternalErrorInLM,
    msgForRemarksForRecordsCountErrorInLM,
    msgForRemarksForDataFormatErrorInLM,
    msgForRemarksForSuccessInLM,
    msgForStatusOkInLM,
    msgForStatusNotOkInLM,
    msgForReasonRecordErrorInLM,
    msgForReasonIMEIFailInLM,

    msgForReasonIMEIDuplicateInLM,
    msgForReasonSuccessInLM,
    msgForReasonNationalWhitelistInLM,
    msgForReasonDuplicateDBInLM,
    msgForReasonMDRDBInLM,
    msgForReasonTypeApprovedInLM,

    msgForReasonLostDBInLM,
    msgForReasonLocalManufacturerDBInLM,
    numberOfRecordsSubject,
    numberOfRecordsMessage,
    invalidDataFormatSubject,
    invalidDataFormatMessage,
    fileProcessSuccessSubject,
    fileProcessSuccessMessage,

    msgForLengthValidationIMEIInList,
    msgForNonNumericIMEIInList,
    msgForNonNumericIMSIInList,
    msgForPrefixIMSIInList,
    msgForNonNumericMSISDNInList,
    msgForPrefixMSISDNInList,
    msgForNonNumericTACInList,
    msgForLengthValidationTACInList,
    msgForNullTACInList,
    msgForNullIMEIIMSIMSISDNInList,
    msgForCompliantBulkImei,
    msgForNonCompliantBulkImei,
    msgForLengthValidationMSISDNInList

}
