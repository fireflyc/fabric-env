package im.fireflyc.fabric.openlibrary.common;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.LOWER_UNDERSCORE;

public class JooqUtils {

    public static <R extends Record, T> PageImpl<T> executePaging(DSLContext dslContext, Table<R> table, Pageable pageable, Class<T> clazz,
                                                                  Condition where) {
        SelectQuery<R> sqlQuery = JooqUtils.getQuery(dslContext, table, pageable, where);
        Long total = dslContext.selectCount()
                .from(table)
                .where(where)
                .fetchOne(0, Long.class);
        return new PageImpl<T>(sqlQuery.fetch().into(clazz), pageable, total);
    }

    public static <R extends Record> SelectQuery<R> getQuery(DSLContext dslContext, Table<R> table, Sort sort) {
        SelectQuery<R> query = dslContext.selectFrom(table).getQuery();
        if (sort == null) {
            return query;
        }
        for (Sort.Order order : sort) {
            Field<?> field = table.field(DSL.name(LOWER_CAMEL.to(LOWER_UNDERSCORE, order.getProperty())));
            if (field == null) {
                continue;
            }
            SortField<?> sortField;
            if (order.getDirection() == Sort.Direction.ASC) {
                sortField = field.asc();
            } else {
                sortField = field.desc();
            }
            query.addOrderBy(sortField);
        }
        return query;
    }

    public static <R extends Record> SelectQuery<R> getQuery(DSLContext dslContext, Table<R> table, Pageable pageable,
                                                             Condition where) {
        SelectQuery<R> query = getQuery(dslContext, table, pageable.getSort());
        query.addLimit((int) pageable.getOffset(), pageable.getPageSize());
        query.addConditions(where);
        return query;
    }
}
