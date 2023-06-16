package project.server.enums;

import project.server.enums.interfaces.IResult;

public enum CommonResult implements IResult {
    FAILURE, //실패
    SUCCESS, //성공
    INPUT_ERROR
}
