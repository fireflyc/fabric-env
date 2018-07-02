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
