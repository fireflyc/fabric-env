
#step5安装Chaincode
#step5.1 在org1上安装chaincode
docker exec -e CORE_PEER_MSPCONFIGPATH=/opt/crypto-config/peerOrganizations/org1.fireflyc.im/users/Admin@org1.fireflyc.im/msp \
            -e CORE_PEER_LOCALMSPID="Org1MSP" \
            -e CORE_PEER_ADDRESS=peer0.org1.fireflyc.im:7051 \
            cli peer chaincode install -n bc_library -v 1.0 -p fireflyc.im/bc_library

docker exec -e CORE_PEER_MSPCONFIGPATH=/opt/crypto-config/peerOrganizations/org1.fireflyc.im/users/Admin@org1.fireflyc.im/msp \
            -e CORE_PEER_LOCALMSPID="Org1MSP" \
            -e CORE_PEER_ADDRESS=peer0.org1.fireflyc.im:7051 \
            cli peer chaincode instantiate -o orderer.fireflyc.im:7050 -C hello -n bc_library -v 1.0 -c '{"Args":["init"]}'

docker exec -e CORE_PEER_MSPCONFIGPATH=/opt/crypto-config/peerOrganizations/org1.fireflyc.im/users/Admin@org1.fireflyc.im/msp \
            -e CORE_PEER_LOCALMSPID="Org1MSP" \
            -e CORE_PEER_ADDRESS=peer1.org1.fireflyc.im:7051 \
            cli peer chaincode install -n bc_library -v 1.0 -p fireflyc.im/bc_library

docker exec -e CORE_PEER_MSPCONFIGPATH=/opt/crypto-config/peerOrganizations/org1.fireflyc.im/users/Admin@org1.fireflyc.im/msp \
            -e CORE_PEER_LOCALMSPID="Org1MSP" \
            -e CORE_PEER_ADDRESS=peer0.org1.fireflyc.im:7051 \
            cli peer chaincode invoke -C hello -n bc_library -v 1.0 -c '{"Args":["echo", "hello"]}'

docker exec -e CORE_PEER_MSPCONFIGPATH=/opt/crypto-config/peerOrganizations/org1.fireflyc.im/users/Admin@org1.fireflyc.im/msp \
            -e CORE_PEER_LOCALMSPID="Org1MSP" \
            -e CORE_PEER_ADDRESS=peer1.org1.fireflyc.im:7051 \
            cli peer chaincode invoke -C hello -n bc_library -v 1.0 -c '{"Args":["echo", "hello"]}'

#org2
docker exec -e CORE_PEER_MSPCONFIGPATH=/opt/crypto-config/peerOrganizations/org2.fireflyc.im/users/Admin@org2.fireflyc.im/msp \
            -e CORE_PEER_LOCALMSPID="Org2MSP" \
            -e CORE_PEER_ADDRESS=peer0.org2.fireflyc.im:7051 \
            cli peer chaincode install -n bc_library -v 1.0 -p fireflyc.im/bc_library

docker exec -e CORE_PEER_MSPCONFIGPATH=/opt/crypto-config/peerOrganizations/org2.fireflyc.im/users/Admin@org2.fireflyc.im/msp \
            -e CORE_PEER_LOCALMSPID="Org2MSP" \
            -e CORE_PEER_ADDRESS=peer1.org2.fireflyc.im:7051 \
            cli peer chaincode install -n bc_library -v 1.0 -p fireflyc.im/bc_library


docker exec -e CORE_PEER_MSPCONFIGPATH=/opt/crypto-config/peerOrganizations/org2.fireflyc.im/users/Admin@org2.fireflyc.im/msp \
            -e CORE_PEER_LOCALMSPID="Org2MSP" \
            -e CORE_PEER_ADDRESS=peer0.org2.fireflyc.im:7051 \
            cli peer chaincode invoke -C hello -n bc_library -v 1.0 -c '{"Args":["echo", "hello"]}'

docker exec -e CORE_PEER_MSPCONFIGPATH=/opt/crypto-config/peerOrganizations/org2.fireflyc.im/users/Admin@org2.fireflyc.im/msp \
            -e CORE_PEER_LOCALMSPID="Org2MSP" \
            -e CORE_PEER_ADDRESS=peer1.org2.fireflyc.im:7051 \
            cli peer chaincode invoke -C hello -n bc_library -v 1.0 -c '{"Args":["echo", "hello"]}'