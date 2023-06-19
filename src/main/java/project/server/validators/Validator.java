package project.server.validators;

import org.springframework.lang.Nullable;

public interface Validator {
    boolean matches(String input);


//    static boolean isValidInteger(@Nullable Integer integer) {
//        return isValidInteger(integer, Integer.MIN_VALUE, Integer.MAX_VALUE);
//    }
//
//    static boolean isValidInteger(@Nullable Integer integer, @Nullable Integer min, @Nullable Integer max) {
//        if (integer == null) {
//            return false;
//        }
//        min = min != null ? min : Integer.MIN_VALUE;
//        max = max != null ? max : Integer.MAX_VALUE;
//        return integer > min && integer < max;
//    }
//
//    static boolean isValidString(@Nullable String string) {
//        return string != null && string.length() != 0;
//    }


}
