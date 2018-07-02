package im.fireflyc.fabric.openlibrary.web;

import im.fireflyc.fabric.openlibrary.common.UIController;

public class OpenLibraryController extends UIController {
    public static final String PREFIX = "/";
    @Override
    public String getPrefix() {
        return OpenLibraryController.PREFIX;
    }
}
