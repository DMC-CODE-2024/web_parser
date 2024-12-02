package com.glocks.web_parser.validator;


import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class Validation {

    Pattern onlyNumberPattern = Pattern.compile("^[0-9]*$");
    Pattern onlyAlphaNumberPattern = Pattern.compile("^[a-zA-Z0-9_]*$");

    public boolean isNumeric(String number) {
        return onlyNumberPattern.matcher(number.trim()).matches();
    }

    public boolean isAlphaNumeric(String number) {
        return onlyAlphaNumberPattern.matcher(number.trim()).matches();
    }

    public boolean isLengthEqual(String number, int length) {
        return number.trim().length() == length;
    }

    public boolean isLengthLess(String number, int length) {
        return number.trim().length() < length;
    }


    public boolean isLengthMore(String number, int length) {
        return number.trim().length() > length;
    }
    public boolean isPrefix(String number, String prefix) {
        return number.trim().startsWith(prefix);
    }
    public boolean isEmptyAndNull(String number) {
        return number == null || number.trim().isEmpty() || number.trim().isBlank();
    }

}
