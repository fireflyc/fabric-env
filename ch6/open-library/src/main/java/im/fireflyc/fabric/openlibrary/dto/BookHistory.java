package im.fireflyc.fabric.openlibrary.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class BookHistory {
    private String txId;
    private BookHistoryItem value;

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public BookHistoryItem getValue() {
        return value;
    }

    public void setValue(BookHistoryItem book) {
        this.value = book;
    }

    public class BookHistoryItem {
        private String isbn;
        @JsonProperty(value = "borrow_time")
        private Date borrowTime;
        private String borrower;

        public String getIsbn() {
            return isbn;
        }

        public void setIsbn(String isbn) {
            this.isbn = isbn;
        }

        public Date getBorrowTime() {
            return borrowTime;
        }

        public void setBorrowTime(Date borrowTime) {
            this.borrowTime = borrowTime;
        }

        public String getBorrower() {
            return borrower;
        }

        public void setBorrower(String borrower) {
            this.borrower = borrower;
        }
    }
}
