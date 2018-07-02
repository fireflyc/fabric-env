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

