package project.server.vos.train;

import lombok.Data;

@Data
public class InquiryVo {
    private String departStationName;
    private String arriveStationName;
    private String fullDate;
    private int adult;
    private int kid;
    private int old;
}
