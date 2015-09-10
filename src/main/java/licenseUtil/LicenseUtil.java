package licenseUtil;

import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;


/**
 * Created by Arne Binder (arne.b.binder@gmail.com) on 05.08.2015.
 */
public class LicenseUtil {

    static final Logger logger = LoggerFactory.getLogger(LicenseUtil.class);

    public static void main(String[] args) throws IOException {
        if(args.length==0){
            logger.error("missing parameters");
        }else if(args[0].equals("--addPomToTsv")){
            MavenProject project = Utils.readPom(new File(args[1]));
            LicensingList licensingList = new LicensingList();
            File f = new File(args[2]);
            if (f.exists() && !f.isDirectory()) {
                licensingList.readFromSpreadsheet(args[2]);
            }
            //System.out.println("CSV READING FINISHED");
            licensingList.addMavenProject(project);
            licensingList.writeToSpreadsheet(args.length > 3 ? args[3] : args[2]);
        }else if(args[0].equals("--writeLicense3rdParty")){
            LicensingList licensingList = new LicensingList();
            licensingList.readFromSpreadsheet(args[2]);
            if(args[1].equals("ALL")){
                for(String module: licensingList.getNonFixedHeaders()){
                    String moduleLicenseText = licensingList.getRepoLicensesForModule(module);
                    Writer out = new BufferedWriter(new OutputStreamWriter(
                            new FileOutputStream("LICENSE-3RD-PARTY."+module), "UTF-8"));
                    try {
                        out.write(moduleLicenseText);
                    } finally {
                        out.close();
                    }
                }
            }else {
                System.out.println(licensingList.getRepoLicensesForModule(args[1]));
            }
        }else if(args[0].equals("--buildEffectivePom")){
                Utils.writeEffectivePom(new File(args[1]), (new File("effective-pom.xml")).getAbsolutePath());
        }else if(args[0].equals("--help")){

        }else{
            logger.error("unknown parameters");
        }
    }


}
