package im.fireflyc.fabric.openlibrary.web;


import im.fireflyc.fabric.openlibrary.dto.BookHistory;
import im.fireflyc.fabric.openlibrary.entity.tables.pojos.TbBook;
import im.fireflyc.fabric.openlibrary.form.EditBookForm;
import im.fireflyc.fabric.openlibrary.service.BookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping(OpenLibraryController.PREFIX + "book")
public class BookController extends OpenLibraryController {
    private Logger LOG = LoggerFactory.getLogger(BookController.class);

    @Autowired
    private BookService bookService;

    @RequestMapping("index")
    public void index(Pageable pageRequest, Model model) {
        Page<TbBook> page = bookService.getBooks(pageRequest);
        model.addAttribute("page", page);
    }

    @RequestMapping("edit")
    public void edit(Long id, Model model) {
        TbBook book = new TbBook();
        if (id != null) {
            book = bookService.getBook(id);
        }
        model.addAttribute("book", book);
    }

    @RequestMapping(value = "save", method = RequestMethod.POST)
    public String save(EditBookForm form, RedirectAttributes model) {
        try {
            bookService.saveOrUpdateBook(form);
        } catch (RuntimeException e) {
            saveError(model, e.getMessage());
            LOG.error(e.getMessage(), e);
        }
        return redirect("book/index");
    }

    @RequestMapping("delete")
    public String delete(Long id, RedirectAttributes model) {
        bookService.deleteBook(id);
        saveSuccess(model, "删除成功");
        return redirect("student/index");
    }

    @RequestMapping(value = "borrow", method = RequestMethod.GET)
    public void borrow() {

    }

    @RequestMapping(value = "borrow", method = RequestMethod.POST)
    public String doBorrow(String isbn, String sno, RedirectAttributes model) {
        try {
            bookService.borrow(isbn, sno);
            saveSuccess(model, "借阅成功");
        } catch (RuntimeException e) {
            LOG.error(e.getMessage(), e);
            saveError(model, e.getMessage());
        }
        return redirect("book/borrow");
    }


    @RequestMapping("query_detail")
    @ResponseBody
    public TbBook queryBook(Long id) {
        return bookService.getBook(id);
    }

    @RequestMapping(value = "give_back", method = RequestMethod.GET)
    public void giveBack() {
    }

    @RequestMapping(value = "give_back", method = RequestMethod.POST)
    public String doGiveBack(String isbn, String sno, RedirectAttributes model) {
        try {
            bookService.giveback(isbn, sno);
            saveSuccess(model, "归还成功");
        } catch (RuntimeException e) {
            LOG.error(e.getMessage(), e);
            saveError(model, e.getMessage());
        }
        return redirect("book/give_back");
    }

    @RequestMapping("query_history")
    public void queryHistory(String isbn, Model model){
        List<BookHistory>  histories = bookService.queryBookHistory(isbn);
        model.addAttribute("histories", histories);
    }
}
