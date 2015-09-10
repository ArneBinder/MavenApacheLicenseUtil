import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;

import java.io.*;
import java.util.*;
import Utils.ColumnHeader;

/**
 * Created by Arne Binder (arne.b.binder@gmail.com) on 10.09.2015.
 */
public class LicensingList extends ArrayList<LicensingObject> {

    static final char columnDelimiter = '\t';
    static final String excludedScope = "test";
    static final CharSequence libraryListPlaceholder = "#librarylist";
    static final String licenseTextDirectory = "src/main/resources/templates/";
    static final Boolean aggregateByLibrary = false;

    void readFromSpreadsheet(String spreadsheetFN) throws IOException {
        InputStreamReader inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(new FileInputStream(spreadsheetFN), "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        CSVParser parser = new CSVParser(
                bufferedReader,
                CSVFormat.DEFAULT.withHeader().withDelimiter(columnDelimiter));

        for (CSVRecord record : parser) {
            add(new LicensingObject(record));
        }
    }

    void writeToSpreadsheet(String spreadsheetFN) throws IOException {
        FileWriter fileWriter = null;
        CSVPrinter csvFilePrinter = null;

        //Create the CSVFormat object with "\n" as a record delimiter
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withDelimiter(columnDelimiter);

        try {
            //initialize FileWriter object
            fileWriter = new FileWriter(spreadsheetFN);

            //initialize CSVPrinter object
            csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);

            ArrayList<String> headers = new ArrayList<>();
            headers.addAll(ColumnHeader.headerValues());
            headers.addAll(getNonFixedHeaders());
            //Create CSV file header
            csvFilePrinter.printRecord(headers);
            for (LicensingObject licensingObject : this) {

                String version = licensingObject.get(ColumnHeader.VERSION.value());
                if(version!=null && !version.startsWith("'") && !version.endsWith("'") ){
                    licensingObject.put(ColumnHeader.VERSION.value(), "'"+version+"'");
                }
                csvFilePrinter.printRecord(licensingObject.getRecord(headers));
            }

            System.out.println("CSV file was created successfully !!!");

        } catch (Exception e) {
            System.out.println("Error in CsvFileWriter !!!");
            e.printStackTrace();
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
                csvFilePrinter.close();
            } catch (IOException e) {
                System.out.println("Error while flushing/closing fileWriter/csvPrinter !!!");
                e.printStackTrace();
            }
        }
    }

    public String getRepoLicensesForModule(String moduleName) throws IOException{
        String result ="3rd party license information for \""+moduleName+"\"\n";
        HashMap<String, HashSet<String>> licenseList = new HashMap<>();
        for(LicensingObject licensingObject: this){

            licensingObject.clean();

            //HashSet<String> libStrings = new HashSet<>();
            if(licensingObject.containsKey(moduleName)){
                HashSet<String> licenseElement;
                //String licenseKey = licensingObject.get(ColumnHeader.LICENSE_SHORT.value())+columnDelimiter+licensingObject.get(ColumnHeader.LICENSE.value());
                String licenseKey = licensingObject.get(ColumnHeader.LICENSE.value());
                if(!licenseList.containsKey(licenseKey)){
                    licenseElement = new HashSet<>();
                }else {
                    licenseElement = licenseList.get(licenseKey);
                }
                String libString = licensingObject.get(ColumnHeader.LIBRARY_NAME.value());
                if(libString==null){
                    libString = "";
                }else{
                    libString += " - ";
                }

                if(libString.trim().equals("") || !aggregateByLibrary){
                    libString += licensingObject.get(ColumnHeader.ARTIFACT_ID.value());
                    String version = licensingObject.get(ColumnHeader.VERSION.value());
                    if(version!=null){
                        if(version.startsWith("'"))
                            version = version.substring(1, version.length());
                        if(version.endsWith("'"))
                            version = version.substring(0, version.length()-1);
                        libString +=":"+version;
                    }
                }
                if(licensingObject.containsKey(ColumnHeader.COPYRIGHT_INFORMATION.value())){

                    libString += ", Copyright "+licensingObject.get(ColumnHeader.COPYRIGHT_INFORMATION.value());
                }
                //if(!libStrings.contains(libString)) {
                licenseElement.add(libString);
                //  libStrings.add(libString);
                //}
                licenseList.put(licenseKey,licenseElement);
            }
        }
        ArrayList<String> sortedLicenseNames = new ArrayList<>(licenseList.keySet());
        sortedLicenseNames.remove(null);
        Collections.sort(sortedLicenseNames);
        for(String key: sortedLicenseNames){
            File f = new File(licenseTextDirectory+key);
            if (f.exists() && !f.isDirectory()) {
                InputStreamReader inputStreamReader = null;
                try {
                    inputStreamReader = new InputStreamReader(new FileInputStream(f), "UTF-8");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line = null;
                result += "\n-----------------------------------------------------------------------------\n\n";
                while ((line = bufferedReader.readLine()) != null) {
                    if(line.toLowerCase().contains(libraryListPlaceholder)){
                        //result += licenseList.get(key);
                        result += "applies to:\n";
                        ArrayList<String> sortedLibraryInfos = new ArrayList<>(licenseList.get(key));
                        Collections.sort(sortedLibraryInfos);
                        for(String libraryInfo: sortedLibraryInfos){
                            result += "\t- "+ libraryInfo+"\n";
                        }
                        result += "\n-----------------------------------------------------------------------------\n";
                    }else{
                        result += line+"\n";
                    }
                }
                bufferedReader.close();
            }else{
                result += "\n-----------------------------------------------------------------------------\n";
                result += "\nCOULD NOT FIND LICENSE TEMPLATE FILE \""+f.getPath()+"\"\n";
                result += "\n-----------------------------------------------------------------------------\n";
                result += "applies to:\n";
                ArrayList<String> sortedLibraryInfos = new ArrayList<>(licenseList.get(key));
                Collections.sort(sortedLibraryInfos);
                for(String libraryInfo: sortedLibraryInfos){
                    result += "\t- "+ libraryInfo+"\n";
                }
            }
        }
        return result;
    }


    public void addMavenProject(MavenProject project) {
        List<Dependency> dependencies = project.getDependencies();

        for (Dependency dependency : dependencies) {
            if(dependency.getScope()==null || !dependency.getScope().equals(excludedScope)) {
                LicensingObject licensingObject = new LicensingObject(dependency, project.getArtifactId());
                if (!add(licensingObject)) {
                    //TODO
                }
            }
        }

        List<Plugin> plugins = project.getBuild().getPlugins();
        for (Plugin plugin : plugins) {
            LicensingObject licensingObject = new LicensingObject(plugin, project.getArtifactId());
            if (!add(licensingObject)) {
                //TODO
            }
        }
    }

    public HashSet<String> getNonFixedHeaders() {
        HashSet<String> result = new HashSet<>();
        for (LicensingObject licensingObject : this) {
            result.addAll(licensingObject.getNonFixedHeaders());
        }
        return result;
    }

    @Override
    public boolean add(LicensingObject newLicensingObject) {
        //System.out.println("add\t" + newLicensingObject.toString());
        //System.out.println("to \t"+toString());
        int index = indexOf(newLicensingObject);
        //System.out.println("index: "+index);
        if (index == -1) {
            return super.add(newLicensingObject);
        } else {
            LicensingObject inList = get(index);
            remove(index);
            //System.out.println("after removing:\t" + toString());
            if (indexOf(newLicensingObject) != -1) {
                // or throw exception?
                System.out.println("could not add newLicensingObject:\t" + newLicensingObject.toString());
                super.add(inList);
                System.out.println("current List:\t" + toString());
                return false;
            } else {
                inList.update(newLicensingObject);
                return super.add(inList);
            }

        }
    }
}
