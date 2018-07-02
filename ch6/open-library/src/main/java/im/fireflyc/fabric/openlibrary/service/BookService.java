package im.fireflyc.fabric.openlibrary.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import im.fireflyc.fabric.openlibrary.common.JooqUtils;
import im.fireflyc.fabric.openlibrary.dto.BookHistory;
import im.fireflyc.fabric.openlibrary.entity.Tables;
import im.fireflyc.fabric.openlibrary.entity.tables.pojos.TbBook;
import im.fireflyc.fabric.openlibrary.entity.tables.records.TbBookRecord;
import im.fireflyc.fabric.openlibrary.form.EditBookForm;
import im.fireflyc.fabric.openlibrary.service.fabric.ChainCodeService;
import im.fireflyc.fabric.openlibrary.service.storage.FileSystemStorageService;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static im.fireflyc.fabric.openlibrary.entity.tables.TbBook.TB_BOOK;

@Service
@Transactional
public class BookService {
    @Autowired
    private DSLContext context;

    @Autowired
    private FileSystemStorageService storageService;
    @Autowired
    private ChainCodeService chainCodeService;
    @Autowired
    private ObjectMapper objectMapper;

    public void borrow(String isbn, String sno) {
        try {
            BlockEvent.TransactionEvent e = chainCodeService.invokeWrite("borrow", sno, isbn).get();
            if (!e.isValid()) {
                throw new RuntimeException("提交失败");
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void giveback(String isbn, String sno) {
        try {
            BlockEvent.TransactionEvent e = chainCodeService.invokeWrite("giveback", sno, isbn).get();
            if (!e.isValid()) {
                throw new RuntimeException("提交失败");
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public List<BookHistory> queryBookHistory(String isbn) {
        ProposalResponse response = chainCodeService.invokeReadOrThrow("query_book_history", isbn);
        String json = response.getProposalResponse().getResponse().getPayload().toStringUtf8();
        try {
            return objectMapper.readValue(json, new TypeReference<List<BookHistory>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Page<TbBook> getBooks(Pageable pageRequest) {
        Condition where = DSL.trueCondition();
        return JooqUtils.executePaging(context, TB_BOOK, pageRequest, TbBook.class, where);
    }

    public TbBook getBook(Long id) {
        return context.fetchOne(TB_BOOK, TB_BOOK.ID.eq(id)).into(TbBook.class);
    }

    public void saveOrUpdateBook(EditBookForm form) {
        TbBookRecord record = context.newRecord(TB_BOOK);
        if (form.getId() != null) {
            record = context.fetchOne(TB_BOOK, TB_BOOK.ID.eq(form.getId()));
            if (form.getPic() != null) {
                storageService.delete(record.getPic());
            }
        }
        if (form.getPic() != null && !form.getPic().isEmpty()) {
            storageService.delete(record.getPic());
            String path = storageService.store(form.getPic());
            record.setPic(path);
        }
        record.setIsbn(form.getIsbn());
        record.setAuthor(form.getAuthor());
        record.setTitle(form.getTitle());
        record.setPress(form.getPress());
        if (record.getId() == null) {
            context.insertInto(TB_BOOK)
                    .set(record)
                    .execute();
        } else {
            context.update(TB_BOOK)
                    .set(record)
                    .where(TB_BOOK.ID.eq(record.getId()))
                    .execute();
        }
    }

    public void deleteBook(Long id) {
        context.deleteFrom(Tables.TB_BOOK)
                .where(TB_BOOK.ID.eq(id))
                .execute();
    }

}
