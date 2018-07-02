package im.fireflyc.fabric.openlibrary.common;

import im.fireflyc.fabric.openlibrary.common.message.WebMessage;
import im.fireflyc.fabric.openlibrary.common.message.WebMessageLevel;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

public abstract class UIController {

    public void saveError(RedirectAttributes model, String error) {
        model.addFlashAttribute("global_error", error);
    }

    public void saveMessage(RedirectAttributes model, String message) {
        model.addFlashAttribute("global_message", message);
    }

    public void saveSuccess(RedirectAttributes model, String message) {
        model.addFlashAttribute("global_success", message);
    }

    protected void doInitBinder(HttpServletRequest request, ServletRequestDataBinder binder) {
    }

    public abstract String getPrefix();

    protected String redirect(String path) {
        String portalName = getPrefix();
        if (portalName.startsWith("/")) {
            return "redirect:" + portalName + path;
        }
        return "redirect:/" + portalName + path;

    }

    protected String forward(String path) {
        String portalName = getPrefix();
        if (portalName.startsWith("/")) {
            return "forward:" + portalName + path;
        }
        return "forward:/" + portalName + path;
    }

}
