import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ChainCodeService {
    private final User user;
    private final ChaincodeID chaincodeID;
    private final String channelName;
    private HFClient client;
    private Channel channel;

    public ChainCodeService(User user, String channelName, ChaincodeID chaincodeID) {
        this.user = user;
        this.channelName = channelName;
        this.chaincodeID = chaincodeID;
    }

    public void start() {
        try {
            client = HFClient.createNewInstance();
            client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
            client.setUserContext(user);

            channel = client.newChannel(this.channelName);
            Peer org1Peer0 = client.newPeer("peer0.org1.fireflyc.im", "grpc://localhost:7051");
            Peer org1Peer1 = client.newPeer("peer1.org1.fireflyc.im", "grpc://localhost:8051");
            Orderer orderer = client.newOrderer("orderer.fireflyc.im", "grpc://localhost:7050");

            channel.addOrderer(orderer);
            channel.addPeer(org1Peer0);
            channel.addPeer(org1Peer1);
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
