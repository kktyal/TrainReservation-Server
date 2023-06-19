package project.server.validators.member;

import project.server.validators.Validator;


public enum MemberValidator implements Validator {
    ID("^\\d{9}$"),
    EMAIL("^(?=.{8,50}$)([\\da-zA-Z][\\da-zA-Z\\-_.]+[\\da-zA-Z])@([\\da-z][\\da-z\\-]*[\\da-z]\\.)?([\\da-z][\\da-z\\-]*[\\da-z])\\.([a-z]{2,15})(\\.[a-z]{2})?$"),
    PHONE("^(01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4})$"),
    NAME("^([가-힣]{2,5})$"),
    GENDER("^(남|여)$"),
    BIRTH("^(19\\d{2}|20\\d{2})(0[0-9]|1[0-2])(0[1-9]|[1-2][0-9]|3[0-1])$"),
    AUTH("^(.{8})$");


    private final String exp;

    MemberValidator(String exp) {
        this.exp = exp;
    }



    @Override
    public boolean matches(String input) {
        return input != null && input.matches(this.exp);
    }

}
