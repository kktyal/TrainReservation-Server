package project.server.enums.member;

import project.server.enums.interfaces.IResult;

public enum SessionAuthorizedResult implements IResult {
    NO_SESSION,
    SESSION_EXPIRED
}
