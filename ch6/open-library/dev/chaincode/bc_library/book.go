package main

import (
	"github.com/hyperledger/fabric/core/chaincode/shim"
	"encoding/json"
	"time"
	"github.com/hyperledger/fabric/protos/peer"
	"fmt"
)

//图书信息
type Book struct {
	ISBN       string    `json:"isbn"`
	BorrowTime time.Time `json:"borrow_time"`
	Borrower   string    `json:"borrower"`
}

type BookHistory struct {
	TxId  string `json:"txId"`
	Value Book   `json:"value"`
}

func getBook(stub shim.ChaincodeStubInterface, isbn string) (*Book, error) {
	bookJson, err := stub.GetState(BOOK_KEY_PREFIX + isbn)
	if err != nil {
		return nil, err
	}
	book := Book{}
	if bookJson == nil {
		return &book, nil
	}
	err = json.Unmarshal(bookJson, &book)
	if err != nil {
		return nil, err
	}
	return &book, nil
}

func saveBook(stub shim.ChaincodeStubInterface, book *Book) error {
	bookJson, err := json.Marshal(book)
	if err != nil {
		return err
	}
	err = stub.PutState(BOOK_KEY_PREFIX+book.ISBN, bookJson)
	if err != nil {
		return err
	}
	return nil
}

func Borrow(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 2 {
		return shim.Error("请提供借阅者ID和图书ID")
	}
	borrower := args[0]
	isbn := args[1]
	book, err := getBook(stub, isbn)
	if err != nil {
		return shim.Error(err.Error())
	}
	if book.Borrower != "" {
		return shim.Error(fmt.Sprintf("图书%s已经被%s借阅尚未归还", book.ISBN, book.Borrower))
	}
	book.ISBN = isbn
	book.Borrower = borrower
	book.BorrowTime = time.Now()
	err = saveBook(stub, book)
	if err != nil {
		return shim.Error(err.Error())
	}
	return shim.Success(nil)
}

func GiveBack(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 2 {
		return shim.Error("请提供借阅者ID和图书ID")
	}
	borrower := args[0]
	isbn := args[1]
	book, err := getBook(stub, isbn)
	if err != nil {
		return shim.Error(err.Error())
	}
	if book.Borrower == "" {
		return shim.Error(fmt.Sprintf("图书%s尚未借出", book.ISBN))
	}
	if book.Borrower != borrower {
		return shim.Error(fmt.Sprintf("图书%s借阅人不是%s", isbn, borrower))
	}
	book.Borrower = ""
	book.BorrowTime = time.Now()
	saveBook(stub, book)
	return shim.Success(nil)
}

func QueryBook(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 1 {
		return shim.Error("图书ID")
	}
	isbn := args[0]
	book, err := getBook(stub, isbn)
	if err != nil {
		return shim.Error(err.Error())
	}
	if book.ISBN == "" {
		return shim.Success(nil)
	}
	bookAsBytes, err := json.Marshal(book)
	if err != nil {
		return shim.Error(err.Error())
	}
	return shim.Success(bookAsBytes)
}

func QueryBookHistory(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 1 {
		return shim.Error("请提供图书ID")
	}
	isbn := args[0]
	iter, err := stub.GetHistoryForKey(BOOK_KEY_PREFIX + isbn)
	if err != nil {
		return shim.Error(err.Error())
	}

	var history []BookHistory;
	defer iter.Close()

	for iter.HasNext() {
		historyData, err := iter.Next()
		if err != nil {
			return shim.Error(err.Error())
		}
		var book Book
		var tx BookHistory
		tx.TxId = historyData.TxId
		if historyData.Value == nil {
			var emptyBook Book
			tx.Value = emptyBook
		} else {
			json.Unmarshal(historyData.Value, &book)
			tx.Value = book
		}
		history = append(history, tx)
	}
	historyAsBytes, err := json.Marshal(history)
	if err != nil {
		return shim.Error(err.Error())
	}
	return shim.Success(historyAsBytes)
}
