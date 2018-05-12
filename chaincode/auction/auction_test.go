package main

import (
	"testing"
	"github.com/hyperledger/fabric/bccsp/factory"
	"github.com/hyperledger/fabric/core/chaincode/shim"
	"encoding/binary"
	"log"
	"encoding/json"
	"time"
	"github.com/stretchr/testify/assert"
)

func checkInit(t *testing.T, stub *shim.MockStub) {
	res := stub.MockInit("tx", [][]byte{[]byte("init"), []byte("60"), []byte("fireflyc")})
	assert.Equal(t, res.Status, int32(shim.OK))
	times, err := stub.GetState(KEY_TIMES)
	assert.NoError(t, err)

	beneficiary, err := stub.GetState(KEY_BENEFICIARY_ID)
	assert.NoError(t, err)
	endTime, err := stub.GetState(KEY_END_TIME)
	assert.NoError(t, err)

	t.Logf("%s %s %d", times, beneficiary, binary.LittleEndian.Uint64(endTime))
}

func TestAuction_Init(t *testing.T) {
	factory.InitFactories(nil)
	cc := new(Auction)
	stub := shim.NewMockStub("auctioncc", cc)
	checkInit(t, stub)
}

func checkQueryHighest(t *testing.T, stub *shim.MockStub, username string, amount uint64, isEnd bool) {
	res := stub.MockInvoke("tx", [][]byte{[]byte("query_highest")})
	assert.Equal(t, res.Status, int32(shim.OK))
	status := AuctionStatus{}
	err := json.Unmarshal(res.Payload, &status)
	assert.NoError(t, err)
	assert.Equal(t, status.HighestBidder.Username, username)
	assert.Equal(t, status.HighestBidder.Amount, amount)
	assert.Equal(t, status.IsEnd, isEnd)
}

func checkHistory(t *testing.T, stub *shim.MockStub, username string, amounts [][]byte) {
	res := stub.MockInvoke("tx", [][]byte{[]byte("query_history"), []byte(username)})
	assert.Equal(t, res.Status, int32(shim.OK))
	bidderHistory := BidderHistory{}
	err := json.Unmarshal(res.Payload, &bidderHistory)
	assert.NoError(t, err)
	assert.Equal(t, bidderHistory.Username, username)
}

func TestAuction_Invoke(t *testing.T) {
	factory.InitFactories(nil)
	cc := new(Auction)
	stub := shim.NewMockStub("auctioncc", cc)

	//check init
	checkInit(t, stub)

	res := stub.MockInvoke("tx", [][]byte{[]byte("echo"), []byte("你好")})
	assert.Equal(t, res.Status, int32(shim.OK))

	t.Logf("echo response %s", res.Payload)
	const testUser1 = "test1"
	var testUser1Amount = [][]byte{[]byte("100"), []byte("100"), []byte("99"), []byte("120"), []byte("130")}
	res = stub.MockInvoke("tx", [][]byte{[]byte("bid"), []byte(testUser1), testUser1Amount[0]}) //1
	assert.Equal(t, res.Status, int32(shim.OK))

	res = stub.MockInvoke("tx", [][]byte{[]byte("bid"), []byte(testUser1), testUser1Amount[1]}) //2
	res = stub.MockInvoke("tx", [][]byte{[]byte("bid"), []byte(testUser1), testUser1Amount[2]})
	//不允许比历史出价低
	log.Printf(res.Message)
	assert.Equal(t, res.Status, int32(shim.OK))

	res = stub.MockInvoke("tx", [][]byte{[]byte("bid"), []byte(testUser1), testUser1Amount[3]}) //3
	res = stub.MockInvoke("tx", [][]byte{[]byte("bid"), []byte(testUser1), testUser1Amount[4]}) //4 error
	log.Printf(res.Message)
	//超过竞拍次数
	assert.Equal(t, res.Status, int32(shim.OK))

	highestByte, err := stub.GetState(KEY_HIGHEST)
	assert.NoError(t, err)

	highestBidder := HighestBidder{}
	err = json.Unmarshal(highestByte, &highestBidder)
	assert.NoError(t, err)
	assert.Equal(t, highestBidder.Username, testUser1)
	assert.Equal(t, highestBidder.Amount, uint64(120))

	const testUser2 = "test2"

	checkQueryHighest(t, stub, testUser1, 120, false)
	checkHistory(t, stub, testUser1, testUser1Amount)
	res = stub.MockInvoke("tx", [][]byte{[]byte("bid"), []byte(testUser2), []byte("100")}) //user2 bid 1
	checkQueryHighest(t, stub, testUser1, 120, false)
	res = stub.MockInvoke("tx", [][]byte{[]byte("bid"), []byte(testUser2), []byte("140")}) //user2 bid 1
	checkQueryHighest(t, stub, testUser2, 140, false)
	//wait 测试竞拍结束
	time.Sleep(60 * time.Second)
	checkQueryHighest(t, stub, testUser2, 140, true)
}
