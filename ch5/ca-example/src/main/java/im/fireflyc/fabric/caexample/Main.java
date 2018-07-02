package im.fireflyc.fabric.caexample;

import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class Main {
    public static void main(String args[]) throws Exception {

        Properties properties = new Properties();
        HFCAClient hfcaClient = HFCAClient.createNewInstance("http://localhost:7054", properties);
        hfcaClient.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());

        String userName = "fireflyc";
        Enrollment enrollment = hfcaClient.enroll("admin", "adminpw");
        System.out.println(enrollment);
        //创建普通用户
        RegistrationRequest regUser = new RegistrationRequest(userName);
        LocalUser user = new LocalUser(enrollment);
        String userId = hfcaClient.register(regUser, user);
        System.out.println(userId);
        Enrollment userEnrollment = hfcaClient.enroll("fireflyc", userId);
        Files.write(Paths.get("./fireflyc.pem"), userEnrollment.getCert().getBytes());
    }
}
