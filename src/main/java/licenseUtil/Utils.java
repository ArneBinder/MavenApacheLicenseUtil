package licenseUtil;

import org.apache.maven.cli.MavenCli;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by Arne Binder (arne.b.binder@gmail.com) on 10.09.2015.
 */
public class Utils {

    static final Logger logger = LoggerFactory.getLogger(Utils.class);

    public enum ColumnHeader {
        ARTIFACT_ID("artefactId"),
        GROUP_ID("groupId"),
        VERSION("version"),
        LIBRARY_NAME("libraryName"),
        LICENSE("license"),
        COPYRIGHT_INFORMATION("copyRightInformation");

        private final String headerValue;

        ColumnHeader(String headerValue) {
            this.headerValue = headerValue;
        }

        public String value() {
            return this.headerValue;
        }
        public static ArrayList<String> headerValues(){
            ArrayList<String> result = new ArrayList<String>();
            for(ColumnHeader header: ColumnHeader.class.getEnumConstants()){
                result.add(header.value());
            }
            return result;
        }
        @Override
        public String toString(){
            return this.headerValue;
        }
    }

    public static MavenProject readPom(File pomfile) {
        logger.info("read pom file from \""+pomfile.getPath()+"\"...");
        Model model = null;
        FileReader reader;
        MavenXpp3Reader mavenreader = new MavenXpp3Reader();
        try {
            reader = new FileReader(pomfile);
            model = mavenreader.read(reader);
            model.setPomFile(pomfile);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        MavenProject project = new MavenProject(model);
        return project;
    }

    public static void writeEffectivePom(File projectDirectory, String fullEffectivePomFilename){
        MavenCli cli = new MavenCli();
        cli.doMain(new String[]{"org.apache.maven.plugins:maven-help-plugin:2.2:effective-pom", "-Doutput="+fullEffectivePomFilename},projectDirectory.getAbsolutePath(), System.out, System.out);
    }

    public static void write(String content, String filename){
        logger.info("write to file \""+filename+"\"...");
        Writer out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            out.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
