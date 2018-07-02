package im.fireflyc.fabric.openlibrary.web;

import im.fireflyc.fabric.openlibrary.entity.tables.pojos.TbStudent;
import im.fireflyc.fabric.openlibrary.form.EditStudentForm;
import im.fireflyc.fabric.openlibrary.service.StudentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping(OpenLibraryController.PREFIX + "student")
public class StudentController extends OpenLibraryController {
    private Logger LOG = LoggerFactory.getLogger(StudentController.class);

    @Autowired
    private StudentService studentService;

    @RequestMapping("index")
    public void index(Pageable pageRequest, Model model) {
        Page<TbStudent> page = studentService.getStudent(pageRequest);
        model.addAttribute("page", page);
    }

    @RequestMapping("edit")
    public void edit(Long id, Model model) {
        TbStudent student = new TbStudent();
        if (id != null) {
            student = studentService.getStudent(id);
        }
        model.addAttribute("student", student);
    }

    @RequestMapping(value = "save", method = RequestMethod.POST)
    public String save(EditStudentForm form, RedirectAttributes model) {
        try {
            studentService.saveOrUpdateBook(form);
        } catch (RuntimeException e) {
            saveError(model, e.getMessage());
            LOG.error(e.getMessage(), e);
        }
        return redirect("student/index");
    }

    @RequestMapping("delete")
    public String delete(Long id, RedirectAttributes model) {
        studentService.deleteStudent(id);
        saveSuccess(model, "删除成功");
        return redirect("student/index");
    }


}
