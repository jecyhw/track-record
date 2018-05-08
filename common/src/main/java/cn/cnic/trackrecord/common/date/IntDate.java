package cn.cnic.trackrecord.common.date;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.Date;

@Data
public abstract class IntDate {
    /**
     * 用整数表示日期，精确到s
     */
    @ApiModelProperty(hidden = true)
    private int value = toInt(new Date().getTime());

    abstract SimpleDateFormat getDateFormat();

    @ApiModelProperty(hidden = true)
    public String getString() {
        if (getValue() == 0) {
            return "";
        }
        return getDateFormat().format(new Date(this.getTimeMillis()));
    }

    @ApiModelProperty(hidden = true)
    public long getTimeMillis() {
        return getValue() * 1000L;
    }

    public Date toDate() {
        return new Date(getTimeMillis());
    }

    static int toInt(long timeMillis) {
        return (int)(timeMillis / 1000);
    }
}
