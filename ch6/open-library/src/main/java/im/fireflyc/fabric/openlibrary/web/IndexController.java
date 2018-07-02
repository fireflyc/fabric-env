package im.fireflyc.fabric.openlibrary.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RequestMapping(OpenLibraryController.PREFIX)
@Controller
public class IndexController extends OpenLibraryController {
    @RequestMapping("index")
    public void index() {
        System.out.println("here");
    }
}
