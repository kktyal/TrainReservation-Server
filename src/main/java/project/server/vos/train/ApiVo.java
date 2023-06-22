package project.server.vos.train;

import lombok.Data;

@Data
public class ApiVo {
    private int adultcharge;
    private String arrplacename;
    private String arrplandtime;
    private String depplacename;
    private String depplandtime;
    private String traingradename;
    private int trainno;
    private String date;
    private boolean standard;
    private boolean premium;
}
