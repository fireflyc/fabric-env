package im.fireflyc.fabric.openlibrary.service.fabric;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;

import java.io.*;
import java.security.PrivateKey;
import java.security.Security;
import java.util.Set;

public class LocalFileUser implements User {
    private final String certificate;
    private final PrivateKey privateKey;
    private final String mspId;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }


    public LocalFileUser(File certificateFile, File privateKeyFile, String mspId) throws IOException {
        this.certificate = new String(IOUtils.toByteArray(new FileInputStream(certificateFile)), "UTF-8");
        this.privateKey = this.loadPrivateKeyFromBytes(IOUtils.toByteArray(new FileInputStream(privateKeyFile)));
        this.mspId = mspId;
    }

    public String getName() {
        return "admin";
    }

    public Set<String> getRoles() {
        return null;
    }

    public String getAccount() {
        return null;
    }

    public String getAffiliation() {
        return null;
    }


    public String getMspId() {
        return this.mspId;
    }

    public Enrollment getEnrollment() {
        return new LocalFileEnrollment(this.certificate, this.privateKey);
    }

    protected PrivateKey loadPrivateKeyFromBytes(byte[] data) throws IOException {
        final Reader pemReader = new StringReader(new String(data));
        final PrivateKeyInfo pemPair;
        try (PEMParser pemParser = new PEMParser(pemReader)) {
            pemPair = (PrivateKeyInfo) pemParser.readObject();
        }
        return new JcaPEMKeyConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME).getPrivateKey(pemPair);
    }

    class LocalFileEnrollment implements Enrollment {
        private final PrivateKey privateKey;
        private final String certificate;

        LocalFileEnrollment(String certificate, PrivateKey privateKey) {
            this.certificate = certificate;
            this.privateKey = privateKey;
        }

        @Override
        public PrivateKey getKey() {
            return this.privateKey;
        }

        @Override
        public String getCert() {
            return this.certificate;
        }
    }
}
