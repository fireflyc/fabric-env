#step 1生成证书
docker run --rm -v `pwd`:/data  hyperledger/fabric-tools:x86_64-1.1.0 \
        cryptogen generate --config=/data/crypto-config.yaml --output=/data/crypto-config

docker run --rm -v `pwd`/crypto-config/peerOrganizations/org1.fireflyc.im/ca/:/etc/hyperledger/fabric-ca-server-config \
        -v `pwd`/var:/etc/hyperledger/fabric-ca-server \
        -p 7054:7054 \
         hyperledger/fabric-ca:x86_64-1.1.0 \
         sh -c 'fabric-ca-server start --ca.certfile /etc/hyperledger/fabric-ca-server-config/ca.org1.fireflyc.im-cert.pem --ca.keyfile /etc/hyperledger/fabric-ca-server-config/67331a2bd2f1ab6f8a67e689bad3ade80769b17c0ad1a503cce23e6f3ba55f20_sk -b admin:adminpw -d'