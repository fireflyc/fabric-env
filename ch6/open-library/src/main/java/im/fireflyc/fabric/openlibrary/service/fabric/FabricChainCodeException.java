package im.fireflyc.fabric.openlibrary.service.fabric;

import org.hyperledger.fabric.sdk.ChaincodeResponse;

public class FabricChainCodeException extends RuntimeException {
    private final ChaincodeResponse.Status status;

    public FabricChainCodeException(ChaincodeResponse.Status status, String message) {
        super(message);
        this.status = status;
    }

    @Override
    public String toString() {
        return String.format("status=%s %s", this.status, this.getMessage());
    }
}