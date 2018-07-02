package im.fireflyc.fabric.openlibrary.common.message;

/**
 * Created by xingsen
 */
public class WebMessage {
    private WebMessageLevel level;
    private String message;


    public WebMessage(String message, WebMessageLevel level) {
        this.message = message;
        this.level = level;
    }

    public WebMessageLevel getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }
}