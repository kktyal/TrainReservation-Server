package project.server.enums;

import project.server.enums.interfaces.IResult;

public enum SessionAuthorizedResult implements IResult {
    NO_SESSION,
    CNT_NO_SEESION,
    MEMBER_NO_SESSION,
    MEMBER_SESSION_EXPIRED,
    CNT_SESSION_EXPIRED
}
