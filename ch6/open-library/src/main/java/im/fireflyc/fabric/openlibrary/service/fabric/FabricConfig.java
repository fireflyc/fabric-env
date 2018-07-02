package im.fireflyc.fabric.openlibrary.service.fabric;


import org.hyperledger.fabric.sdk.ChaincodeID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@ConfigurationProperties(prefix = "fabric")
public class FabricConfig {
    @Autowired
    private ResourceLoader resourceLoader;

    private String channelName;
    private String chainCodeName;
    private String chainCodeVersion;
    private String mspId;

    private List<PeerConfig> peers;
    private List<PeerConfig> orderer;
    private String certificateFile;
    private String privateKeyFile;

    public ChaincodeID buildChaincodeID() {
        return ChaincodeID.newBuilder()
                .setVersion(this.getChainCodeVersion())
                .setName(this.getChainCodeName())
                .build();
    }

    public String getMspId() {
        return mspId;
    }

    public void setMspId(String mspId) {
        this.mspId = mspId;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getChainCodeName() {
        return chainCodeName;
    }

    public void setChainCodeName(String chainCodeName) {
        this.chainCodeName = chainCodeName;
    }

    public String getChainCodeVersion() {
        return chainCodeVersion;
    }

    public void setChainCodeVersion(String chainCodeVersion) {
        this.chainCodeVersion = chainCodeVersion;
    }

    public List<PeerConfig> getPeers() {
        return peers;
    }

    public void setPeers(List<PeerConfig> peers) {
        this.peers = peers;
    }

    public List<PeerConfig> getOrderer() {
        return orderer;
    }

    public void setOrderer(List<PeerConfig> orderer) {
        this.orderer = orderer;
    }

    public Resource getCertificateFile() {
        return resourceLoader.getResource(certificateFile);
    }

    public void setCertificateFile(String certificateFile) {
        this.certificateFile = certificateFile;
    }

    public Resource getPrivateKeyFile() {
        return resourceLoader.getResource(privateKeyFile);
    }

    public void setPrivateKeyFile(String privateKeyFile) {
        this.privateKeyFile = privateKeyFile;
    }
}
