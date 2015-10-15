package licenseUtil;

import licenseUtil.Utils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by Arne on 16.10.2015.
 */
public class UtilsTest {

    @Test
    public void testUpdateRepository() throws IOException, GitAPIException {
        Utils.updateRepository("../Freme/Broker");
    }
}
