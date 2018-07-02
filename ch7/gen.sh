#step 1生成证书
docker run --rm -v `pwd`:/data  hyperledger/fabric-tools:x86_64-1.1.0 \
        cryptogen generate --config=/data/crypto-config.yaml --output=/data/crypto-config

#step 2生成创世块
mkdir configtx && docker run -v `pwd`:/data -e FABRIC_CFG_PATH=/data --rm  hyperledger/fabric-tools:x86_64-1.1.0 \
        configtxgen -profile TwoOrgsOrdererGenesis -outputBlock /data/configtx/genesis.block

#step 3生成Channel
#3.1 生成Channel的配置数据块
docker run -v `pwd`:/data -e FABRIC_CFG_PATH=/data --rm  hyperledger/fabric-tools:x86_64-1.1.0 \
        configtxgen -profile TwoOrgsChannel -outputCreateChannelTx /data/configtx/openlibrary.tx -channelID openlibrary
#3.2 生成Anchor Peer的配置数据块
docker run -v `pwd`:/data -e FABRIC_CFG_PATH=/data --rm  hyperledger/fabric-tools:x86_64-1.1.0 \
        configtxgen -profile TwoOrgsChannel -outputAnchorPeersUpdate /data/configtx/Org1MSPanchors_openlibrary.tx -channelID openlibrary -asOrg Org1MSP
docker run -v `pwd`:/data -e FABRIC_CFG_PATH=/data --rm  hyperledger/fabric-tools:x86_64-1.1.0 \
        configtxgen -profile TwoOrgsChannel -outputAnchorPeersUpdate /data/configtx/Org2MSPanchors_openlibrary.tx -channelID openlibrary -asOrg Org2MSP



#step 4
#step 4.1附加Channel
docker exec -e CORE_PEER_MSPCONFIGPATH=/opt/crypto-config/peerOrganizations/org1.fireflyc.im/users/Admin@org1.fireflyc.im/msp \
            -e CORE_PEER_LOCALMSPID="Org1MSP" \
            cli peer channel create -o orderer.fireflyc.im:7050 -c openlibrary -f /opt/configtx/openlibrary.tx

#step 4.2移动Channel数据块
docker exec cli mv /openlibrary.block /opt/configtx/
#step 4.3更新Channel——把org1的两个节点加入到Channel
docker exec -e CORE_PEER_MSPCONFIGPATH=/opt/crypto-config/peerOrganizations/org1.fireflyc.im/users/Admin@org1.fireflyc.im/msp \
            -e CORE_PEER_LOCALMSPID="Org1MSP" \
            -e CORE_PEER_ADDRESS=peer0:7051 \
            cli peer channel join -b /opt/configtx/openlibrary.block
docker exec -e CORE_PEER_MSPCONFIGPATH=/opt/crypto-config/peerOrganizations/org1.fireflyc.im/users/Admin@org1.fireflyc.im/msp \
            -e CORE_PEER_LOCALMSPID="Org1MSP" \
            -e CORE_PEER_ADDRESS=peer1:7051 \
            cli peer channel join -b /opt/configtx/openlibrary.block
#step 4.4更新Channel——把org2的两个节点加入到Channel
docker exec -e CORE_PEER_MSPCONFIGPATH=/opt/crypto-config/peerOrganizations/org2.fireflyc.im/users/Admin@org2.fireflyc.im/msp \
            -e CORE_PEER_LOCALMSPID="Org2MSP" \
            -e CORE_PEER_ADDRESS=peer0:7051 \
            cli peer channel join -b /opt/configtx/openlibrary.block
docker exec -e CORE_PEER_MSPCONFIGPATH=/opt/crypto-config/peerOrganizations/org2.fireflyc.im/users/Admin@org2.fireflyc.im/msp \
            -e CORE_PEER_LOCALMSPID="Org2MSP" \
            -e CORE_PEER_ADDRESS=peer1:7051 \
            cli peer channel join -b /opt/configtx/openlibrary.block
#step 4.5更新Channel——更新Anchor Peer
docker exec -e CORE_PEER_MSPCONFIGPATH=/opt/crypto-config/peerOrganizations/org1.fireflyc.im/users/Admin@org1.fireflyc.im/msp \
            -e CORE_PEER_LOCALMSPID="Org1MSP" \
            -e CORE_PEER_ADDRESS=peer0:7051 \
            cli peer channel update -o orderer.fireflyc.im:7050 -c openlibrary -f /opt/configtx/Org1MSPanchors_openlibrary.tx
docker exec -e CORE_PEER_MSPCONFIGPATH=/opt/crypto-config/peerOrganizations/org2.fireflyc.im/users/Admin@org2.fireflyc.im/msp \
            -e CORE_PEER_LOCALMSPID="Org2MSP" \
            -e CORE_PEER_ADDRESS=peer0:7051 \
            cli peer channel update -o orderer.fireflyc.im:7050 -c openlibrary -f /opt/configtx/Org2MSPanchors_openlibrary.tx


#step5安装Chaincode
#step5.1 在org1上安装chaincode
docker exec -e CORE_PEER_MSPCONFIGPATH=/opt/crypto-config/peerOrganizations/org1.fireflyc.im/users/Admin@org1.fireflyc.im/msp \
            -e CORE_PEER_LOCALMSPID="Org1MSP" \
            -e CORE_PEER_ADDRESS=peer0:7051 \
            cli peer chaincode install -n demo2 -v 1.0 -p github.com/demo2

#step5.2 在org1上实例化chaincode
docker exec -e CORE_PEER_MSPCONFIGPATH=/opt/crypto-config/peerOrganizations/org1.fireflyc.im/users/Admin@org1.fireflyc.im/msp \
            -e CORE_PEER_LOCALMSPID="Org1MSP" \
            -e CORE_PEER_ADDRESS=peer0:7051 \
            cli peer chaincode instantiate -o orderer.fireflyc.im:7050 -C openlibrary -n demo2 -v 1.0 -c '{"Args":["init", "a", "100", "b", "200"]}'

#step5.3 测试一下例子正常工作
docker exec -e CORE_PEER_MSPCONFIGPATH=/opt/crypto-config/peerOrganizations/org1.fireflyc.im/users/Admin@org1.fireflyc.im/msp \
            -e CORE_PEER_LOCALMSPID="Org1MSP" \
            -e CORE_PEER_ADDRESS=peer0:7051 \
            cli peer chaincode invoke -C openlibrary -n demo2 -v 1.0 -c '{"Args":["invoke", "a", "b", "20"]}'

docker exec -e CORE_PEER_MSPCONFIGPATH=/opt/crypto-config/peerOrganizations/org1.fireflyc.im/users/Admin@org1.fireflyc.im/msp \
            -e CORE_PEER_LOCALMSPID="Org1MSP" \
            -e CORE_PEER_ADDRESS=peer0:7051 \
            cli peer chaincode invoke -C openlibrary -n demo2 -v 1.0 -c '{"Args":["query", "b"]}'

##org2不需要实例化只需要install
docker exec -e CORE_PEER_MSPCONFIGPATH=/opt/crypto-config/peerOrganizations/org2.fireflyc.im/users/Admin@org2.fireflyc.im/msp \
            -e CORE_PEER_LOCALMSPID="Org2MSP" \
            -e CORE_PEER_ADDRESS=peer0:7051 \
            cli peer chaincode install -n demo2 -v 1.0 -p github.com/demo2

docker exec -e CORE_PEER_MSPCONFIGPATH=/opt/crypto-config/peerOrganizations/org2.fireflyc.im/users/Admin@org2.fireflyc.im/msp \
            -e CORE_PEER_LOCALMSPID="Org2MSP" \
            -e CORE_PEER_ADDRESS=peer0:7051 \
            cli peer chaincode invoke -C openlibrary -n demo2 -v 1.0 -c '{"Args":["invoke", "a", "b", "10"]}'

docker exec -e CORE_PEER_MSPCONFIGPATH=/opt/crypto-config/peerOrganizations/org2.fireflyc.im/users/Admin@org2.fireflyc.im/msp \
            -e CORE_PEER_LOCALMSPID="Org2MSP" \
            -e CORE_PEER_ADDRESS=peer0:7051 \
            cli peer chaincode invoke -C openlibrary -n demo2 -v 1.0 -c '{"Args":["query", "b"]}'