package im.fireflyc.fabric.openlibrary;

import im.fireflyc.fabric.openlibrary.config.ApplicationConfig;
import im.fireflyc.fabric.openlibrary.config.WebConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import java.net.URL;

import static org.hyperledger.fabric.sdk.helper.Config.ORG_HYPERLEDGER_FABRIC_SDK_CONFIGURATION;

@SpringBootApplication
@Import({ApplicationConfig.class, WebConfig.class})
public class OpenLibraryApplication {

    public static void main(String[] args) {
        URL url = OpenLibraryApplication.class.getResource("/fabric-sdk-config.properties");
        System.setProperty(ORG_HYPERLEDGER_FABRIC_SDK_CONFIGURATION, url.getFile());
        SpringApplication.run(OpenLibraryApplication.class, args);
    }
}
