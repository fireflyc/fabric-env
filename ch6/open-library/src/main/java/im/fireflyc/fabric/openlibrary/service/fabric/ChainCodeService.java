package im.fireflyc.fabric.openlibrary.service.fabric;

import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class ChainCodeService {
    private Logger LOG = LoggerFactory.getLogger(ChainCodeService.class);

    @Autowired
    private FabricConfig fabricConfig;

    private User user;
    private ChaincodeID chaincodeID;
    private HFClient client;
    private Channel channel;

    @PostConstruct
    public void init() throws IOException {
        this.user = new LocalFileUser(fabricConfig.getCertificateFile().getFile(), fabricConfig.getPrivateKeyFile().getFile(), fabricConfig.getMspId());
        this.chaincodeID = fabricConfig.buildChaincodeID();
        start();
        LOG.info("连接Fabric");
    }

    public void start() {
        try {
            client = HFClient.createNewInstance();
            client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
            client.setUserContext(user);

            channel = client.newChannel(fabricConfig.getChannelName());
            for (PeerConfig peerConfig : fabricConfig.getPeers()) {
                Peer peer = client.newPeer(peerConfig.getName(), peerConfig.getUrl());
                channel.addPeer(peer);
            }
            for (PeerConfig orderConfig : fabricConfig.getOrderer()) {
                Orderer orderer = client.newOrderer(orderConfig.getName(), orderConfig.getUrl());
                channel.addOrderer(orderer);
            }
            channel.initialize();
        } catch (IllegalAccessException | InstantiationException | CryptoException
                | InvocationTargetException | InvalidArgumentException | ClassNotFoundException
                | NoSuchMethodException | TransactionException e) {
            throw new RuntimeException(e);
        }
    }


    public Collection<ProposalResponse> invokeRead(String fnc, String... args) {
        return this.proposal(false, fnc, args);
    }

    public ProposalResponse invokeReadOrThrow(String fnc, String... args) {
        Collection<ProposalResponse> proposalResponses = this.invokeRead(fnc, args);
        Optional<ProposalResponse> optional = proposalResponses.stream()
                .filter(response -> response.getStatus().equals(ChaincodeResponse.Status.SUCCESS))
                .findFirst();
        if (optional == null || optional.orElse(null) == null) {
            //找到第一个错误
            Optional<ProposalResponse> error = proposalResponses.stream()
                    .filter(response -> !response.getStatus().equals(ChaincodeResponse.Status.SUCCESS))
                    .findFirst();
            ProposalResponse response = error.get();
            throw new FabricChainCodeException(response.getStatus(),
                    String.format("peer=%s message=%s", response.getPeer(), response.getMessage()));
        }
        return optional.get();
    }

    public CompletableFuture<BlockEvent.TransactionEvent> invokeWrite(String fcn, String... args) {
        Collection<ProposalResponse> proposalResponses = this.proposal(true, fcn, args);
        return channel.sendTransaction(proposalResponses);
    }

    public Collection<ProposalResponse> proposal(boolean allReady, String fcn, String... args) {
        try {
            TransactionProposalRequest request = client.newTransactionProposalRequest();
            request.setChaincodeEndorsementPolicy(new ChaincodeEndorsementPolicy());
            request.setFcn(fcn);
            request.setArgs(args);
            request.setChaincodeID(chaincodeID);
            Collection<ProposalResponse> responses = channel.sendTransactionProposal(request);
            for (ProposalResponse response : responses) {
                if (allReady && !response.getStatus().equals(ChaincodeResponse.Status.SUCCESS)) {
                    throw new FabricChainCodeException(response.getStatus(),
                            String.format("peer=%s message=%s", response.getPeer(), response.getMessage()));
                }
            }
            return responses;
        } catch (InvalidArgumentException | ProposalException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown(false);
        }
    }
}
