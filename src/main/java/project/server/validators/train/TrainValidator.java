package project.server.validators.train;

import project.server.validators.Validator;

public enum TrainValidator implements Validator {
    DATE("^(20\\d{2})(0[0-9]|1[0-2])(0[1-9]|[1-2][0-9]|3[0-1])$"),
    CNT("^(?:[0-9]|10)$"),
    TIME("^(0[0-9]|1[0-9]|2[0-3])(0[1-9]|[0-5][0-9])(0[1-9]|[0-5][0-9])$"),
    CARRIAGE("^[12]$"),
    SEAT("^\\d[A-Z]"),
    RESERVATIONID("^.{16}$");



    private final String exp;

    TrainValidator(String exp) {
        this.exp = exp;
    }

    @Override


    public boolean matches(String input) {
        return input != null && input.matches(this.exp);
    }
}
