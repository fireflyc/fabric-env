docker exec -e CORE_PEER_MSPCONFIGPATH=/opt/crypto-config/peerOrganizations/org1.fireflyc.im/users/Admin@org1.fireflyc.im/msp \
            -e CORE_PEER_LOCALMSPID="Org1MSP" \
            -e CORE_PEER_ADDRESS=peer0.org1.fireflyc.im:7051 \
            cli peer chaincode install -n aucthion -v 1.0 -p github.com/auction

#把auction和Channel做绑定（实例化）
docker exec -e CORE_PEER_MSPCONFIGPATH=/opt/crypto-config/peerOrganizations/org1.fireflyc.im/users/Admin@org1.fireflyc.im/msp \
            -e CORE_PEER_LOCALMSPID="Org1MSP" \
            -e CORE_PEER_ADDRESS=peer0.org1.fireflyc.im:7051 \
            cli peer chaincode instantiate -o orderer.fireflyc.im:7050 -C hello -n aucthion -v 1.0 -c '{"Args":["init", "120", "fireflyc"]}'

#测试echo
docker exec -e CORE_PEER_MSPCONFIGPATH=/opt/crypto-config/peerOrganizations/org1.fireflyc.im/users/Admin@org1.fireflyc.im/msp \
            -e CORE_PEER_LOCALMSPID="Org1MSP" \
            -e CORE_PEER_ADDRESS=peer0.org1.fireflyc.im:7051 \
            cli peer chaincode invoke -C hello -n aucthion -v 1.0 -c '{"Args":["echo", "hello"]}'

#测试bid
docker exec -e CORE_PEER_MSPCONFIGPATH=/opt/crypto-config/peerOrganizations/org1.fireflyc.im/users/Admin@org1.fireflyc.im/msp \
            -e CORE_PEER_LOCALMSPID="Org1MSP" \
            -e CORE_PEER_ADDRESS=peer0.org1.fireflyc.im:7051 \
            cli peer chaincode invoke -C hello -n aucthion -v 1.0 -c '{"Args":["bid", "test1", "100"]}'

docker exec -e CORE_PEER_MSPCONFIGPATH=/opt/crypto-config/peerOrganizations/org1.fireflyc.im/users/Admin@org1.fireflyc.im/msp \
            -e CORE_PEER_LOCALMSPID="Org1MSP" \
            -e CORE_PEER_ADDRESS=peer0.org1.fireflyc.im:7051 \
            cli peer chaincode invoke -C hello -n aucthion -v 1.0 -c '{"Args":["bid", "test2", "120"]}'

#查看最高出价人
docker exec -e CORE_PEER_MSPCONFIGPATH=/opt/crypto-config/peerOrganizations/org1.fireflyc.im/users/Admin@org1.fireflyc.im/msp \
            -e CORE_PEER_LOCALMSPID="Org1MSP" \
            -e CORE_PEER_ADDRESS=peer0.org1.fireflyc.im:7051 \
            cli peer chaincode invoke -C hello -n aucthion -v 1.0 -c '{"Args":["query_highest"]}'
#查看出价历史
docker exec -e CORE_PEER_MSPCONFIGPATH=/opt/crypto-config/peerOrganizations/org1.fireflyc.im/users/Admin@org1.fireflyc.im/msp \
            -e CORE_PEER_LOCALMSPID="Org1MSP" \
            -e CORE_PEER_ADDRESS=peer0.org1.fireflyc.im:7051 \
            cli peer chaincode invoke -C hello -n aucthion -v 1.0 -c '{"Args":["query_history", "test1"]}'


#升级
docker exec -e CORE_PEER_MSPCONFIGPATH=/opt/crypto-config/peerOrganizations/org1.fireflyc.im/users/Admin@org1.fireflyc.im/msp \
            -e CORE_PEER_LOCALMSPID="Org1MSP" \
            -e CORE_PEER_ADDRESS=peer0.org1.fireflyc.im:7051 \
            cli peer chaincode install -n aucthion -v 1.1 -p github.com/auction

docker exec -e CORE_PEER_MSPCONFIGPATH=/opt/crypto-config/peerOrganizations/org1.fireflyc.im/users/Admin@org1.fireflyc.im/msp \
            -e CORE_PEER_LOCALMSPID="Org1MSP" \
            -e CORE_PEER_ADDRESS=peer0.org1.fireflyc.im:7051 \
            cli peer chaincode instantiate -C hello -n aucthion -v 1.0 -c '{"Args":["init", "120", "abc"]}'

#删除链码
docker rm -f <链码容器ID>
docker exec peer0.org1.fireflyc.im rm /var/hyperledger/production/chaincodes/<名称>:<版本>