import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * Created by Arne Binder (arne.b.binder@gmail.com) on 10.09.2015.
 */
public class Utils {

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
            ArrayList<String> result = new ArrayList<>();
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
}
