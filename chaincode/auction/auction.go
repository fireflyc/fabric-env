package main

import (
	"github.com/hyperledger/fabric/core/chaincode/shim"
	"github.com/hyperledger/fabric/protos/peer"
	"fmt"
	"strconv"
	"encoding/binary"
	"time"
	"strings"
	"encoding/json"
)

type Auction struct {
}

const SYS_KEY_PREFIX = "sys_"
const KEY_TIMES = SYS_KEY_PREFIX + "times"
const KEY_BENEFICIARY_ID = SYS_KEY_PREFIX + "beneficiary_id"
const KEY_END_TIME = SYS_KEY_PREFIX + "end_time"
const KEY_HIGHEST = SYS_KEY_PREFIX + "highest"

type BidderLog struct {
	Id     int    `json:"id"`
	Amount uint64 `json:"amount"`
}

type BidderHistory struct {
	Username string      `json:"username"`
	Logs     []BidderLog `json:"logs"`
}

type HighestBidder struct {
	Username string `json:"username"`
	Amount   uint64 `json:"amount"`
}

type AuctionStatus struct {
	HighestBidder HighestBidder `json:"highestBidder"`
	IsEnd         bool          `json:"isEnd"`
}

func (t *Auction) Init(stub shim.ChaincodeStubInterface) peer.Response {
	_, args := stub.GetFunctionAndParameters()
	if len(args) != 2 {
		return shim.Error("请提供竞拍持续时间和受益人ID")
	}

	times, err := strconv.Atoi(args[0])
	if err != nil {
		return shim.Error(fmt.Sprintf("竞拍时间转换失败不对 %s", err.Error()))
	}
	beneficiary := args[1]
	fmt.Printf("竞拍时间 = %d, 受益人 = %s\n", times, beneficiary)

	//把本次竞拍的时间和受益人写到状态数据库
	err = stub.PutState(KEY_TIMES, []byte(strconv.Itoa(times)))
	if err != nil {
		return shim.Error(fmt.Sprintf("写入竞拍时间失败 %s", err.Error()))
	}

	err = stub.PutState(KEY_BENEFICIARY_ID, []byte(beneficiary))
	if err != nil {
		return shim.Error(fmt.Sprintf("写入受益人失败 %s", err.Error()))
	}

	//计算结束时间写到状态数据库
	timesBytes := make([]byte, 8)
	binary.LittleEndian.PutUint64(timesBytes, uint64(time.Now().Unix())+uint64(times))
	err = stub.PutState(KEY_END_TIME, timesBytes)
	if err != nil {
		return shim.Error(fmt.Sprintf("写入结束时间 %s", err.Error()))
	}
	return shim.Success(nil)
}

func (t *Auction) Invoke(stub shim.ChaincodeStubInterface) peer.Response {
	function, args := stub.GetFunctionAndParameters()
	if function == "echo" {
		return shim.Success([]byte(args[0]))
	} else if function == "bid" {
		return t.bid(stub, args)
	} else if function == "query_highest" {
		return t.queryHighest(stub, args)
	} else if function == "query_history" {
		return t.queryHistory(stub, args)
	}else if function=="reinit"{
		return t.Init(stub)
	}
	return shim.Error("无法识别调用的函数")
}

func (t *Auction) isEnd(stub shim.ChaincodeStubInterface) (bool, error) {
	now := uint64(time.Now().Unix())
	timesBytes, err := stub.GetState(KEY_END_TIME)
	if err != nil {
		return false, err
	}
	endTime := binary.LittleEndian.Uint64(timesBytes)
	if now < endTime {
		return false, nil
	}
	return true, nil
}

func (t *Auction) bid(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 2 {
		return shim.Error("需要两个参数")
	}
	isEnd, err := t.isEnd(stub)
	if err != nil {
		return shim.Error(err.Error())
	}
	if isEnd {
		return shim.Error("竞拍已结束")
	}
	username := args[0]
	if strings.HasPrefix(username, KEY_END_TIME) {
		return shim.Error("用户名不合法")
	}
	amount, err := strconv.ParseUint(args[1], 10, 0)
	if err != nil {
		return shim.Error(err.Error())
	}
	userInfoByte, err := stub.GetState(username)
	if err != nil {
		return shim.Error(err.Error())
	}
	var bidder BidderHistory
	if userInfoByte == nil {
		bidder = BidderHistory{Username: username}
	} else {
		json.Unmarshal(userInfoByte, &bidder)
	}
	if len(bidder.Logs) > 0 && bidder.Logs[len(bidder.Logs)-1].Amount > amount {
		return shim.Error("不能比历史出价低")
	}
	if len(bidder.Logs) >= 3 {
		return shim.Error("超过竞拍次数")
	}
	bidder.Logs = append(bidder.Logs, BidderLog{Id: len(bidder.Logs), Amount: amount})
	newBidder, err := json.Marshal(bidder)
	if err != nil {
		return shim.Error(err.Error())
	}
	err = stub.PutState(username, []byte(newBidder))
	if err != nil {
		return shim.Error(err.Error())
	}
	highestByte, err := stub.GetState(KEY_HIGHEST)
	if err != nil {
		return shim.Error(err.Error())
	}
	highestBidder := HighestBidder{Amount: 0}
	if highestByte != nil {
		err = json.Unmarshal(highestByte, &highestBidder)
		if err != nil {
			return shim.Error(err.Error())
		}
	}
	if highestBidder.Amount < amount {
		highestBidder.Amount = amount
		highestBidder.Username = username
		highestByte, err = json.Marshal(highestBidder)
		if err != nil {
			return shim.Error(err.Error())
		}
		err = stub.PutState(KEY_HIGHEST, highestByte)
		if err != nil {
			return shim.Error(err.Error())
		}
	}
	return shim.Success(nil)
}

func (t *Auction) queryHighest(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	highestByte, err := stub.GetState(KEY_HIGHEST)
	if err != nil {
		return shim.Error(err.Error())
	}
	if highestByte == nil {
		return shim.Success(nil)
	}

	status := AuctionStatus{}
	err = json.Unmarshal(highestByte, &status.HighestBidder)
	if err != nil {
		return shim.Error(fmt.Sprintf("反序列化%s失败", err.Error()))
	}
	status.IsEnd, err = t.isEnd(stub)
	if err != nil {
		return shim.Error(err.Error())
	}
	statusBytes, err := json.Marshal(status)
	return shim.Success(statusBytes)
}

func (t *Auction) queryHistory(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 1 {
		return shim.Error("必须给出一个参数")
	}
	username := args[0]
	if strings.HasPrefix(username, SYS_KEY_PREFIX) {
		return shim.Error("用户名不合法")
	}
	userInfoByte, err := stub.GetState(username)
	if err != nil {
		return shim.Error(fmt.Sprintf("反序列化%s失败", err.Error()))
	}
	return shim.Success(userInfoByte)
}

func main() {
	err := shim.Start(new(Auction))
	if err != nil {
		fmt.Printf("Error starting Auction chaincode: %s", err)
	}
}
