import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String args[]) throws IOException, ExecutionException, InterruptedException {

        File keyRoot = new File(Main.class.getResource("/").getFile());
        LocalFileUser user = new LocalFileUser(new File(keyRoot, "Admin@org1.fireflyc.im-cert.pem").getPath(),
                new File(keyRoot, "4db30e7a4485750ea0603fc42e5196b34905d34070789eeac1d3c6a51f6d7bdd_sk").getPath());

        ChaincodeID chaincodeID = ChaincodeID.newBuilder().setName("aucthion").setVersion("1.0").build();
        ChainCodeService service = new ChainCodeService(user, "hello", chaincodeID);

        service.start();
        try {
            String user1 = "user1";
            String user2 = "user2";

            ProposalResponse response = service.invokeReadOrThrow("echo", "你好");
            System.out.println(response.getProposalResponse().getResponse().getPayload().toStringUtf8());

            BlockEvent.TransactionEvent event = service.invokeWrite("reinit", "28800", "fireflyc").get();
            if (!event.isValid()) {
                System.out.println("排序失败");
                return;
            }

            BlockEvent.TransactionEvent event1 = service.invokeWrite("bid", user1, "100").get();
            BlockEvent.TransactionEvent event2 = service.invokeWrite("bid", user2, "120").get();

            if (!event1.isValid()) {
                System.out.println("用户1竞拍失败");
            }
            if (!event2.isValid()) {
                System.out.println("用户2竞拍失败");
            }

            //最高出价
            ProposalResponse queryHighestResp = service.invokeReadOrThrow("query_highest");
            System.out.println(queryHighestResp.getProposalResponse().getResponse().getPayload().toStringUtf8());

            //两个用户的出价历史
            ProposalResponse queryUser1History = service.invokeReadOrThrow("query_history", user1);
            System.out.println(queryUser1History.getProposalResponse().getResponse().getPayload().toStringUtf8());

            ProposalResponse queryUser2History = service.invokeReadOrThrow("query_history", user2);
            System.out.println(queryUser2History.getProposalResponse().getResponse().getPayload().toStringUtf8());
        } finally {
            service.close();
        }
    }

}
