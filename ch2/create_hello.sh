#step 1生成证书
docker run --rm -v `pwd`:/data  hyperledger/fabric-tools:x86_64-1.1.0 \
        cryptogen generate --config=/data/crypto-config.yaml --output=/data/crypto-config

#step 2生成创世块
mkdir configtx && docker run -v `pwd`:/data -e FABRIC_CFG_PATH=/data --rm  hyperledger/fabric-tools:x86_64-1.1.0 \
        configtxgen -profile TwoOrgsOrdererGenesis -outputBlock /data/configtx/genesis.block

#step 3生成Channel
#3.1 生成Channel的配置数据块
docker run -v `pwd`:/data -e FABRIC_CFG_PATH=/data --rm  hyperledger/fabric-tools:x86_64-1.1.0 \
        configtxgen -profile TwoOrgsChannel -outputCreateChannelTx /data/configtx/hello.tx -channelID hello
#3.2 生成Anchor Peer的配置数据块
docker run -v `pwd`:/data -e FABRIC_CFG_PATH=/data --rm  hyperledger/fabric-tools:x86_64-1.1.0 \
        configtxgen -profile TwoOrgsChannel -outputAnchorPeersUpdate /data/configtx/Org1MSPanchors_hello.tx -channelID hello -asOrg Org1MSP
docker run -v `pwd`:/data -e FABRIC_CFG_PATH=/data --rm  hyperledger/fabric-tools:x86_64-1.1.0 \
        configtxgen -profile TwoOrgsChannel -outputAnchorPeersUpdate /data/configtx/Org2MSPanchors_hello.tx -channelID hello -asOrg Org2MSP



#step 4
#step 4.1附加Channel
docker exec -e CORE_PEER_MSPCONFIGPATH=/opt/crypto-config/peerOrganizations/org1.fireflyc.im/users/Admin@org1.fireflyc.im/msp \
            -e CORE_PEER_LOCALMSPID="Org1MSP" \
            cli peer channel create -o orderer.fireflyc.im:7050 -c hello -f /opt/configtx/hello.tx

#step 4.2移动Channel数据块
docker exec cli mv /hello.block /opt/configtx/
#step 4.3更新Channel——把org1的两个节点加入到Channel
docker exec -e CORE_PEER_MSPCONFIGPATH=/opt/crypto-config/peerOrganizations/org1.fireflyc.im/users/Admin@org1.fireflyc.im/msp \
            -e CORE_PEER_LOCALMSPID="Org1MSP" \
            -e CORE_PEER_ADDRESS=peer0.org1.fireflyc.im:7051 \
            cli peer channel join -b /opt/configtx/hello.block
docker exec -e CORE_PEER_MSPCONFIGPATH=/opt/crypto-config/peerOrganizations/org1.fireflyc.im/users/Admin@org1.fireflyc.im/msp \
            -e CORE_PEER_LOCALMSPID="Org1MSP" \
            -e CORE_PEER_ADDRESS=peer1.org1.fireflyc.im:7051 \
            cli peer channel join -b /opt/configtx/hello.block
#step 4.4更新Channel——把org2的两个节点加入到Channel
docker exec -e CORE_PEER_MSPCONFIGPATH=/opt/crypto-config/peerOrganizations/org2.fireflyc.im/users/Admin@org2.fireflyc.im/msp \
            -e CORE_PEER_LOCALMSPID="Org2MSP" \
            -e CORE_PEER_ADDRESS=peer0.org2.fireflyc.im:7051 \
            cli peer channel join -b /opt/configtx/hello.block
docker exec -e CORE_PEER_MSPCONFIGPATH=/opt/crypto-config/peerOrganizations/org2.fireflyc.im/users/Admin@org2.fireflyc.im/msp \
            -e CORE_PEER_LOCALMSPID="Org2MSP" \
            -e CORE_PEER_ADDRESS=peer1.org2.fireflyc.im:7051 \
            cli peer channel join -b /opt/configtx/hello.block
#step 4.5更新Channel——更新Anchor Peer
docker exec -e CORE_PEER_MSPCONFIGPATH=/opt/crypto-config/peerOrganizations/org1.fireflyc.im/users/Admin@org1.fireflyc.im/msp \
            -e CORE_PEER_LOCALMSPID="Org1MSP" \
            -e CORE_PEER_ADDRESS=peer0.org1.fireflyc.im:7051 \
            cli peer channel update -o orderer.fireflyc.im:7050 -c hello -f /opt/configtx/Org1MSPanchors_hello.tx
docker exec -e CORE_PEER_MSPCONFIGPATH=/opt/crypto-config/peerOrganizations/org2.fireflyc.im/users/Admin@org2.fireflyc.im/msp \
            -e CORE_PEER_LOCALMSPID="Org2MSP" \
            -e CORE_PEER_ADDRESS=peer0.org2.fireflyc.im:7051 \
            cli peer channel update -o orderer.fireflyc.im:7050 -c hello -f /opt/configtx/Org2MSPanchors_hello.tx


#step5安装Chaincode
#step5.1 在org1上安装chaincode
docker exec -e CORE_PEER_MSPCONFIGPATH=/opt/crypto-config/peerOrganizations/org1.fireflyc.im/users/Admin@org1.fireflyc.im/msp \
            -e CORE_PEER_LOCALMSPID="Org1MSP" \
            -e CORE_PEER_ADDRESS=peer0.org1.fireflyc.im:7051 \
            cli peer chaincode install -n demo1 -v 1.0 -p github.com/demo1

#step5.2 在org1上实例化chaincode
docker exec -e CORE_PEER_MSPCONFIGPATH=/opt/crypto-config/peerOrganizations/org1.fireflyc.im/users/Admin@org1.fireflyc.im/msp \
            -e CORE_PEER_LOCALMSPID="Org1MSP" \
            -e CORE_PEER_ADDRESS=peer0.org1.fireflyc.im:7051 \
            cli peer chaincode instantiate -o orderer.fireflyc.im:7050 -C hello -n demo1 -v 1.0 -c '{"Args":["init", "a", "100", "b", "200"]}'

#step5.3 测试一下例子正常工作
docker exec -e CORE_PEER_MSPCONFIGPATH=/opt/crypto-config/peerOrganizations/org1.fireflyc.im/users/Admin@org1.fireflyc.im/msp \
            -e CORE_PEER_LOCALMSPID="Org1MSP" \
            -e CORE_PEER_ADDRESS=peer0.org1.fireflyc.im:7051 \
            cli peer chaincode invoke -C hello -n demo1 -v 1.0 -c '{"Args":["invoke", "10"]}'