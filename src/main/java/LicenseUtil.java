import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.invoker.MavenInvocationException;

import java.io.*;


/**
 * Created by Arne Binder (arne.b.binder@gmail.com) on 05.08.2015.
 */
public class LicenseUtil {

    public static void main(String[] args) throws IOException {
        if(args[0].equals("addPomToTsv")){
            MavenProject project = Utils.readPom(new File(args[1]));
            LicensingList licensingList = new LicensingList();
            File f = new File(args[2]);
            if (f.exists() && !f.isDirectory()) {
                licensingList.readFromSpreadsheet(args[2]);
            }
            //System.out.println("CSV READING FINISHED");
            licensingList.addMavenProject(project);
            licensingList.writeToSpreadsheet(args.length>3?args[3]:args[2]);
        }else if(args[0].equals("repo")){
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
        }else if(args[0].equals("buildEffectivePom")){

            try {
                Utils.buildEffectivePom(new File(args[1]));
            } catch (MavenInvocationException e) {
                e.printStackTrace();
            }
        }

    }


}
