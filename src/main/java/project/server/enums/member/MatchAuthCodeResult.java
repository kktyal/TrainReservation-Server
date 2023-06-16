package project.server.enums.member;

import project.server.enums.interfaces.IResult;

public enum MatchAuthCodeResult implements IResult {
    EMAIL_NO_MATCH,
    AUTH_CODE_EXPIRED
}
