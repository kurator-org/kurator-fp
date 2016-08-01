package org.filteredpush.kuration;

import java.text.MessageFormat;

/**
 * Created by lowery on 7/29/16.
 */
public class StringUtils {

    public static boolean isNumeric(String str) {
        try {
            double d = Double.parseDouble(str);
        } catch(NumberFormatException e){
            return false;
        }

        return true;
    }

    public static String comment(String message, Object... args) {
        return MessageFormat.format(message, args);
    }

}
