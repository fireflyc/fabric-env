package main

import (
	"fmt"
	"github.com/hyperledger/fabric/core/chaincode/shim"
	"github.com/hyperledger/fabric/protos/peer"
	"log"
)

const BOOK_KEY_PREFIX = "book_"

type OpenLibrary struct {
}

func (t *OpenLibrary) Init(stub shim.ChaincodeStubInterface) peer.Response {
	return shim.Success(nil)
}

func (t *OpenLibrary) Invoke(stub shim.ChaincodeStubInterface) peer.Response {
	function, args := stub.GetFunctionAndParameters()
	log.Printf("function=%s %s", function, args)
	if function == "echo" {
		return shim.Success([]byte(args[0]))
	} else if function == "borrow" {
		return Borrow(stub, args)
	} else if function == "giveback" {
		return GiveBack(stub, args)
	} else if function == "query_book" {
		return QueryBook(stub, args)
	} else if function == "query_book_history" {
		return QueryBookHistory(stub, args)
	}
	return shim.Error("无法识别调用的函数")
}

func main() {
	err := shim.Start(new(OpenLibrary))
	if err != nil {
		fmt.Printf("Error starting Auction chaincode: %s", err)
	}
}
