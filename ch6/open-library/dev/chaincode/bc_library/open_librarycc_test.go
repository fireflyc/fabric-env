package main

import (
	"testing"
	"github.com/hyperledger/fabric/bccsp/factory"
	"github.com/hyperledger/fabric/core/chaincode/shim"
	"github.com/stretchr/testify/assert"
	"encoding/json"
	"log"
)

func checkInit(t *testing.T, stub *shim.MockStub) {
	res := stub.MockInit("tx", [][]byte{[]byte("init")})
	assert.Equal(t, res.Status, int32(shim.OK))

	res = stub.MockInvoke("tx", [][]byte{[]byte("echo"), []byte("hello")})
	assert.Equal(t, res.Status, int32(shim.OK))
	assert.Equal(t, res.Payload, []byte("hello"))
}

func testQueryBook(t *testing.T, stub *shim.MockStub, isbn string) Book {
	res := stub.MockInvoke("tx", [][]byte{[]byte("query_book"), []byte(isbn)})
	assert.Equal(t, res.Status, int32(shim.OK))
	var book Book
	err := json.Unmarshal(res.Payload, &book)
	assert.NoError(t, err)
	log.Print(book)
	assert.Equal(t, book.ISBN, isbn)
	return book
}

func testBook(t *testing.T, stub *shim.MockStub) {
	//借阅
	res := stub.MockInvoke("tx", [][]byte{[]byte("borrow"), []byte("fireflyc"), []byte("9787115297655")}) //计算机体系结构量化研究方法
	assert.Equal(t, res.Status, int32(shim.OK))
	res = stub.MockInvoke("tx", [][]byte{[]byte("borrow"), []byte("fireflyc"), []byte("9787115293800")}) //算法
	assert.Equal(t, res.Status, int32(shim.OK))
	book := testQueryBook(t, stub, "9787115297655")
	assert.Equal(t, book.Borrower, "fireflyc")
	book = testQueryBook(t, stub, "9787115293800")
	assert.Equal(t, book.Borrower, "fireflyc")

	//借还
	res = stub.MockInvoke("tx", [][]byte{[]byte("giveback"), []byte("fireflyc"), []byte("9787115297655")})
	assert.Equal(t, res.Status, int32(shim.OK))
	book = testQueryBook(t, stub, "9787115297655")
	assert.Equal(t, book.Borrower, "")

	book = testQueryBook(t, stub, "9787115293800")
	assert.Equal(t, book.Borrower, "fireflyc")

	//https://jira.hyperledger.org/browse/FAB-5507
	//历史查询 尚未实现
	/*res = stub.MockInvoke("tx", [][]byte{[]byte("query_book_history"), []byte("9787115297655")})
	assert.Equal(t, res.Status, int32(shim.OK))
	var history []BookHistory
	err := json.Unmarshal(res.Payload, &history)
	assert.NoError(t, err)
	for _, book := range history {
		assert.Equal(t, book.Value.ISBN, "9787115297655")
		log.Print(book)
	}*/
}

func TestOpenLibrary_Init(t *testing.T) {
	factory.InitFactories(nil)
	cc := new(OpenLibrary)
	stub := shim.NewMockStub("open_libcc", cc)
	checkInit(t, stub)

	testBook(t, stub)
}

func TestOpenLibrary_Invoke(t *testing.T) {
	factory.InitFactories(nil)
	cc := new(OpenLibrary)
	stub := shim.NewMockStub("open_libcc", cc)
	checkInit(t, stub)

	testBook(t, stub)
}
