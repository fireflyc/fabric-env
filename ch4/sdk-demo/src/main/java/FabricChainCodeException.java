import org.hyperledger.fabric.sdk.ChaincodeResponse;

public class FabricChainCodeException extends RuntimeException {
    private final String message;
    private final ChaincodeResponse.Status status;

    public FabricChainCodeException(ChaincodeResponse.Status status, String message) {
        this.status = status;
        this.message = message;
    }

    @Override
    public String toString() {
        return String.format("status=%s %s", this.status, this.message);
    }
}