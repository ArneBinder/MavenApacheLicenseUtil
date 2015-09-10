/**
 * Copyright (C) 2015 Arne Binder (arne.b.binder@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package licenseUtil;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

/**
 * Created by Arne Binder (arne.b.binder@gmail.com) on 10.09.2015.
 */
public class LicensingList extends ArrayList<LicensingObject> {

    final Logger logger = LoggerFactory.getLogger(LicensingList.class);

    static final char columnDelimiter = '\t';
    static final String excludedScope = "test";
    static final CharSequence libraryListPlaceholder = "#librarylist";
    static final String licenseTextDirectory = "src/main/resources/templates/";
    static final Boolean aggregateByLibrary = false;

    public void readFromSpreadsheet(String spreadsheetFN) throws IOException {
        logger.info("read spreadsheet from \"" + spreadsheetFN + "\"...");
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

    public void writeToSpreadsheet(String spreadsheetFN) throws IOException {
        logger.info("write spreadsheet to \"" + spreadsheetFN + "\"...");
        FileWriter fileWriter = null;
        CSVPrinter csvFilePrinter = null;

        //Create the CSVFormat object with "\n" as a record delimiter
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withDelimiter(columnDelimiter);

        try {
            //initialize FileWriter object
            fileWriter = new FileWriter(spreadsheetFN);

            //initialize CSVPrinter object
            csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);

            ArrayList<String> headers = new ArrayList<String>();
            headers.addAll(Utils.ColumnHeader.headerValues());
            headers.addAll(getNonFixedHeaders());
            //Create CSV file header
            csvFilePrinter.printRecord(headers);
            for (LicensingObject licensingObject : this) {

                String version = licensingObject.get(Utils.ColumnHeader.VERSION.value());
                if(version!=null && !version.startsWith("'") && !version.endsWith("'") ){
                    licensingObject.put(Utils.ColumnHeader.VERSION.value(), "'"+version+"'");
                }
                csvFilePrinter.printRecord(licensingObject.getRecord(headers));
            }
            logger.info("CSV file was created successfully");

        } catch (Exception e) {
            logger.error("Error in CsvFileWriter");
            e.printStackTrace();
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
                csvFilePrinter.close();
            } catch (IOException e) {
                logger.error("Error while flushing/closing fileWriter/csvPrinter !!!");
                e.printStackTrace();
            }
        }
    }

    public String getRepoLicensesForModule(String moduleName) throws IOException{
        String result ="3rd party license information for \""+moduleName+"\"\n";
        HashMap<String, HashSet<String>> licenseList = new HashMap<String, HashSet<String>>();
        for(LicensingObject licensingObject: this){

            licensingObject.clean();

            if(licensingObject.containsKey(moduleName)){
                HashSet<String> licenseElement;
                String licenseKey = licensingObject.get(Utils.ColumnHeader.LICENSE.value());
                if(!licenseList.containsKey(licenseKey)){
                    licenseElement = new HashSet<String>();
                }else {
                    licenseElement = licenseList.get(licenseKey);
                }
                String libString = licensingObject.get(Utils.ColumnHeader.LIBRARY_NAME.value());
                if(libString==null){
                    libString = "";
                }else{
                    libString += " - ";
                }

                if(libString.trim().equals("") || !aggregateByLibrary){
                    libString += licensingObject.get(Utils.ColumnHeader.ARTIFACT_ID.value());
                    String version = licensingObject.get(Utils.ColumnHeader.VERSION.value());
                    if(version!=null){
                        if(version.startsWith("'"))
                            version = version.substring(1, version.length());
                        if(version.endsWith("'"))
                            version = version.substring(0, version.length()-1);
                        libString +=":"+version;
                    }
                }
                if(licensingObject.containsKey(Utils.ColumnHeader.COPYRIGHT_INFORMATION.value())){

                    libString += ", Copyright "+licensingObject.get(Utils.ColumnHeader.COPYRIGHT_INFORMATION.value());
                }
                //if(!libStrings.contains(libString)) {
                licenseElement.add(libString);
                //  libStrings.add(libString);
                //}
                licenseList.put(licenseKey,licenseElement);
            }
        }
        ArrayList<String> sortedLicenseNames = new ArrayList<String>(licenseList.keySet());
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
                        result += "applies to:\n";
                        ArrayList<String> sortedLibraryInfos = new ArrayList<String>(licenseList.get(key));
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
                ArrayList<String> sortedLibraryInfos = new ArrayList<String>(licenseList.get(key));
                Collections.sort(sortedLibraryInfos);
                for(String libraryInfo: sortedLibraryInfos){
                    result += "\t- "+ libraryInfo+"\n";
                }
            }
        }
        return result;
    }


    public void addMavenProject(MavenProject project) {
        logger.debug("add pom content to current list...");
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
        HashSet<String> result = new HashSet<String>();
        for (LicensingObject licensingObject : this) {
            result.addAll(licensingObject.getNonFixedHeaders());
        }
        return result;
    }

    @Override
    public boolean add(LicensingObject newLicensingObject) {
         int index = indexOf(newLicensingObject);
        if (index == -1) {
            return super.add(newLicensingObject);
        } else {
            LicensingObject inList = get(index);
            remove(index);
            if (indexOf(newLicensingObject) != -1) {
                // or throw exception?
                logger.warn("could not add newLicensingObject:\t" + newLicensingObject.toString());
                super.add(inList);
                logger.debug("current List:\t" + toString());
                return false;
            } else {
                inList.update(newLicensingObject);
                return super.add(inList);
            }

        }
    }
}
