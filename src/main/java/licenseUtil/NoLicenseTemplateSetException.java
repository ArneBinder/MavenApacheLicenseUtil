package licenseUtil;

/**
 * Created by Arne on 08.09.2016.
 */
public class NoLicenseTemplateSetException extends Exception {
    final LicensingObject licensingObject;
    public NoLicenseTemplateSetException(LicensingObject licensingObject) {
        super(licensingObject.toString());
        this.licensingObject = licensingObject;
    }

    public LicensingObject getLicensingObject() {
        return licensingObject;
    }
}
