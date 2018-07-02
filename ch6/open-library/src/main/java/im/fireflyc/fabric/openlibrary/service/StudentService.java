package im.fireflyc.fabric.openlibrary.service;

import im.fireflyc.fabric.openlibrary.common.JooqUtils;
import im.fireflyc.fabric.openlibrary.entity.Tables;
import im.fireflyc.fabric.openlibrary.entity.tables.pojos.TbBook;
import im.fireflyc.fabric.openlibrary.entity.tables.pojos.TbStudent;
import im.fireflyc.fabric.openlibrary.entity.tables.records.TbBookRecord;
import im.fireflyc.fabric.openlibrary.entity.tables.records.TbStudentRecord;
import im.fireflyc.fabric.openlibrary.form.EditBookForm;
import im.fireflyc.fabric.openlibrary.form.EditStudentForm;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static im.fireflyc.fabric.openlibrary.entity.Tables.TB_STUDENT;
import static im.fireflyc.fabric.openlibrary.entity.tables.TbBook.TB_BOOK;

@Service
@Transactional
public class StudentService {
    @Autowired
    private DSLContext context;

    public Page<TbStudent> getStudent(Pageable pageRequest) {
        Condition where = DSL.trueCondition();
        return JooqUtils.executePaging(context, TB_STUDENT, pageRequest, TbStudent.class, where);
    }

    public TbStudent getStudent(Long id) {
        return context.fetchOne(TB_STUDENT, TB_STUDENT.ID.eq(id)).into(TbStudent.class);
    }

    public void saveOrUpdateBook(EditStudentForm form) {
        TbStudentRecord record = context.newRecord(TB_STUDENT);
        if (form.getId() != null) {
            record = context.fetchOne(TB_STUDENT, TB_STUDENT.ID.eq(form.getId()));
        }
        record.setSno(form.getSno());
        record.setName(form.getName());
        if (record.getId() == null) {
            context.insertInto(TB_STUDENT)
                    .set(record)
                    .execute();
        } else {
            context.update(TB_STUDENT)
                    .set(record)
                    .where(TB_STUDENT.ID.eq(record.getId()))
                    .execute();
        }
    }

    public void deleteStudent(Long id) {
        context.deleteFrom(Tables.TB_STUDENT)
                .where(TB_STUDENT.ID.eq(id))
                .execute();
    }
}
