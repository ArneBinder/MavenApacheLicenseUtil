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
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.nibor.autolink.LinkExtractor;
import org.nibor.autolink.LinkSpan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Created by Arne Binder (arne.b.binder@gmail.com) on 10.09.2015.
 */
public class LicensingList extends ArrayList<LicensingObject> {

    final static Logger logger = LoggerFactory.getLogger(LicensingList.class);

    static final char columnDelimiter = '\t';
    static final String excludedScope = "test";
    static final CharSequence libraryListPlaceholder = "#librarylist";
    static final String licenseTextDirectory = "templates/";
    static final Boolean aggregateByBundle = false;
    static final String forceAddingLibraryKeyword = "KEEP";

    private static Map<String, String> licenseUrlMappings = constructLicenseUrlFileMapping();

    public void readFromSpreadsheet(String spreadsheetFN, String currentVersion) throws IOException {
        logger.info("read spreadsheet from \"" + spreadsheetFN + "\"");
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
        logger.info("write spreadsheet to \"" + spreadsheetFN + "\"");
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
            headers.addAll(LicensingObject.ColumnHeader.headerValues());
            headers.addAll(getNonFixedHeaders());
            //Create CSV file header
            csvFilePrinter.printRecord(headers);
            for (LicensingObject licensingObject : this) {
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

    public String getRepoLicensesForModule(String moduleName, String version) throws IOException{
        String result ="3rd party license information for \""+moduleName+"\"\n";
        HashMap<String, HashSet<String>> licenseList = new HashMap<>();
        for(LicensingObject licensingObject: this){

            licensingObject.clean();

            if(licensingObject.containsKey(moduleName)){
                String versionString = licensingObject.get(moduleName).toUpperCase();
                if(versionString.equals(version.toUpperCase()) || versionString.equals(forceAddingLibraryKeyword.toUpperCase())) {
                    HashSet<String> licenseElement;
                    String licenseKey = licensingObject.get(LicensingObject.ColumnHeader.LICENSE.value());
                    if (!licenseList.containsKey(licenseKey)) {
                        licenseElement = new HashSet<>();
                    } else {
                        licenseElement = licenseList.get(licenseKey);
                    }

                    //if(!libStrings.contains(libString)) {
                    licenseElement.add(licensingObject.getStringForModule(moduleName, aggregateByBundle));
                    //  libStrings.add(libString);
                    //}
                    licenseList.put(licenseKey, licenseElement);
                }
            }
        }
        ArrayList<String> sortedLicenseNames = new ArrayList<>(licenseList.keySet());
        sortedLicenseNames.remove(null);
        Collections.sort(sortedLicenseNames);
        for(String key: sortedLicenseNames){
            InputStream inputStream = null;
            String fn = "/"+licenseTextDirectory + key;
            inputStream = getClass().getResourceAsStream(fn);
            if(inputStream!=null){
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line = null;
                result += "\n-----------------------------------------------------------------------------\n\n";
                while ((line = bufferedReader.readLine()) != null) {
                    if(line.toLowerCase().contains(libraryListPlaceholder)){
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
                logger.warn("COULD NOT FIND LICENSE TEMPLATE FILE: "+fn);
                result += "\n-----------------------------------------------------------------------------\n";
                result += "\nCOULD NOT FIND LICENSE TEMPLATE FILE \""+fn+"\"\n";
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


    public void addMavenProject(MavenProject project, String version) {
        logger.debug("add pom content to current list");
        List<Dependency> dependencies = project.getDependencies();

        for (Dependency dependency : dependencies) {
            if(dependency.getScope()==null || !dependency.getScope().equals(excludedScope)) {
                LicensingObject licensingObject;
                Artifact depArtefact = new DefaultArtifact(dependency.getGroupId(),dependency.getArtifactId(),"pom",dependency.getVersion());
                try {
                    MavenProject depProject = Utils.resolveArtefact(project, depArtefact);
                    licensingObject = new LicensingObject(depProject, project.getArtifactId(), version, licenseUrlMappings);
                } catch (ArtifactResolutionException | IOException | XmlPullParserException e) {
                    logger.error("Could not resolve Artefact; "+depArtefact.toString());
                    licensingObject = new LicensingObject(dependency, project.getArtifactId(), version);
                }
                add(licensingObject);
            }
        }

        List<Plugin> plugins = project.getBuild().getPlugins();
        for (Plugin plugin : plugins) {
            //LicensingObject licensingObject = new LicensingObject(plugin, project.getArtifactId(), version);
            LicensingObject licensingObject;
            Artifact depArtefact = new DefaultArtifact(plugin.getGroupId(),plugin.getArtifactId(),"pom",plugin.getVersion());
            try {
                MavenProject depProject = Utils.resolveArtefact(project, depArtefact);
                licensingObject = new LicensingObject(depProject, project.getArtifactId(), version, licenseUrlMappings);
            } catch (ArtifactResolutionException | IOException | XmlPullParserException e) {
                logger.error("Could not resolve Artefact; "+depArtefact.toString());
                licensingObject = new LicensingObject(plugin, project.getArtifactId(), version);
            }
            add(licensingObject);
        }
    }

    public HashSet<String> getNonFixedHeaders() {
        HashSet<String> result = new HashSet<>();
        for (LicensingObject licensingObject : this) {
            result.addAll(licensingObject.getNonFixedHeaders());
        }
        return result;
    }

    public void purge(String version){
        logger.info("purge licensing list: just keep elements with version=\""+version+"\" or labeled with \""+forceAddingLibraryKeyword+"\"");
        Iterator<LicensingObject> i = this.iterator();
        while (i.hasNext()) {
            LicensingObject licensingObject = i.next(); // must be called before you can call i.remove()
            if(licensingObject.purgedEmpty(version.toUpperCase())){
                i.remove();
            }
        }
    }

    public static Map<String, String> constructLicenseUrlFileMapping() {

        Map<String, String> result = new HashMap<>();
        try {
            List<String> files = IOUtils.readLines(LicensingList.class.getClassLoader()
                    .getResourceAsStream(licenseTextDirectory), StandardCharsets.UTF_8);

            LinkExtractor linkExtractor = LinkExtractor.builder().build();
            for (String licenseFN : files) {
                InputStream inputStream;
                String fn = "/" + licenseTextDirectory + licenseFN;
                inputStream = LicensingList.class.getResourceAsStream(fn);

                String link;
                if (inputStream != null) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    boolean foundUrl = false;
                    boolean foundListPlaceholder = false;
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        if (line.toLowerCase().contains(libraryListPlaceholder)) {
                            foundListPlaceholder = true;
                            break;
                        }
                        //Iterable<LinkSpan> links = linkExtractor.extractLinks(line);
                        for (LinkSpan linkSpan : linkExtractor.extractLinks(line)) {
                            // get substring containing the link and prune protocol
                            link = line.substring(linkSpan.getBeginIndex(), linkSpan.getEndIndex()).replaceFirst("^[^:]+://", "");
                            result.put(link, licenseFN);
                            foundUrl = true;
                        }
                    }
                    bufferedReader.close();

                    if (!foundUrl)
                        logger.warn("No license URL found in " + licenseFN + " template file.");
                    if (!foundListPlaceholder) {
                        String msg = "Library list placeholder (" + libraryListPlaceholder + ") not found in " + licenseFN + " template file.";
                        logger.error(msg);
                        throw new RuntimeException(msg);
                    }
                }
            }
        }catch(final IOException ex){
            throw new RuntimeException(ex);
        }
        return result;
    }

    @Override
    public boolean addAll(Collection<? extends LicensingObject> c) {
        boolean result = true;
        for(LicensingObject licensingObject: c){
            result &= this.add(licensingObject);
        }
        return result;
    }

    @Override
    public boolean add(LicensingObject newLicensingObject) {
         int index = indexOf(newLicensingObject);
        if (index == -1) {
            return super.add(newLicensingObject);
        } else {
            LicensingObject inList = remove(index);
            int newIndex = indexOf(newLicensingObject);
            int newIndex2 = indexOf(inList);
            if (newIndex != -1) {
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
