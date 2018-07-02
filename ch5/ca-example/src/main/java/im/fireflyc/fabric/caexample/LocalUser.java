package im.fireflyc.fabric.caexample;

import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;

import java.util.Set;

public class LocalUser implements User {
    private final Enrollment enrollment;

    public LocalUser(Enrollment enrollment) {
        this.enrollment = enrollment;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Set<String> getRoles() {
        return null;
    }

    @Override
    public String getAccount() {
        return null;
    }

    @Override
    public String getAffiliation() {
        return null;
    }

    @Override
    public Enrollment getEnrollment() {
        return enrollment;
    }

    @Override
    public String getMspId() {
        return null;
    }
}
