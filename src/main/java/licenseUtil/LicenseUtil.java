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

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingException;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.resolution.ArtifactDescriptorException;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.jgit.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;


/**
 * Created by Arne Binder (arne.b.binder@gmail.com) on 05.08.2015.
 */
public class LicenseUtil {

    static final Logger logger = LoggerFactory.getLogger(LicenseUtil.class);

    static final String LICENSE_3RD_PARTY_FN = "LICENSE-3RD-PARTY";
    static final String EFFECTIVE_POM_FN = "effective-pom.xml";
    static final String POM_FN = "pom.xml";
    static final String README_PATH = "README.md";

    public static void main(String[] args) throws IOException {
        if(args.length==0){
            logger.error("Missing parameters. Use --help to get a list of the possible options.");
        }else if(args[0].equals("--addPomToTsv")){
            if(args.length<4)
                logger.error("Missing arguments for option --addPomToTsv. Please specify <pomFileName> <licenses.stub.tsv> <currentVersion> or use the option --help for further information.");
            String pomFN = args[1];
            String spreadSheetFN = args[2];
            String currentVersion = args[3];

            MavenProject project = null;
            try {
                project = Utils.readPom(new File(pomFN));
            } catch (XmlPullParserException e) {
                logger.error("Could not parse pom file: \""+pomFN+"\"");
            }
            LicensingList licensingList = new LicensingList();
            File f = new File(spreadSheetFN);
            if (f.exists() && !f.isDirectory()) {
                licensingList.readFromSpreadsheet(spreadSheetFN, currentVersion);
            }

            licensingList.addMavenProject(project, currentVersion);
            licensingList.writeToSpreadsheet(spreadSheetFN);
        }else if(args[0].equals("--writeLicense3rdParty")){
            if(args.length<4)
                logger.error("Missing arguments for option --writeLicense3rdParty. Please provide <licenses.enhanced.tsv> <processModule> <currentVersion> [and <targetDir>] or use the option --help for further information.");
            String spreadSheetFN = args[1];
            String processModule = args[2];
            String currentVersion = args[3];


            HashMap<String, String> targetDirs = new HashMap<>();
            if(args.length > 4){
                File targetDir = new File(args[4]);
                File[] subdirs = targetDir.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
                for(File subdir: subdirs){
                    String pomFN = subdir.getPath()+File.separator+POM_FN;
                    MavenProject mavenProject;
                    try {
                        mavenProject = Utils.readPom(new File(pomFN));
                    } catch (Exception e) {
                        logger.warn("Could not read from pom file: \""+pomFN+"\" because of "+e.getMessage());
                        continue;
                    }
                    targetDirs.put(mavenProject.getModel().getArtifactId(), subdir.getAbsolutePath());
                }
            }

            LicensingList licensingList = new LicensingList();
            licensingList.readFromSpreadsheet(spreadSheetFN, currentVersion);
            if(processModule.toUpperCase().equals("ALL")){
                for(String module: licensingList.getNonFixedHeaders()){
                    writeLicense3rdPartyFile(module,licensingList,currentVersion,targetDirs.get(module));
                }
            }else {
                writeLicense3rdPartyFile(processModule,licensingList,currentVersion,targetDirs.get(processModule));
            }
        }else if(args[0].equals("--buildEffectivePom")){
                Utils.writeEffectivePom(new File(args[1]), (new File(EFFECTIVE_POM_FN)).getAbsolutePath());
        }else if(args[0].equals("--processProjectsInFolder")){
            if(args.length<4)
                logger.error("Missing arguments for option --processProjectsInFolder. Please provide <superDirectory> <licenses.stub.tsv> and <currentVersion> or use the option --help for further information.");
            File directory = new File(args[1]);
            String spreadSheetFN = args[2];
            String currentVersion = args[3];
            LicensingList licensingList = new LicensingList();
            licensingList.addAll(processProjectsInFolder(directory, spreadSheetFN, currentVersion,false));
            licensingList.writeToSpreadsheet(spreadSheetFN);

        }else if(args[0].equals("--purgeTsv")){
            if(args.length<3)
                logger.error("Missing arguments for option --purgeTsv. Please provide <spreadSheetIN.tsv>, <spreadSheetOUT.tsv> and <currentVersion> or use the option --help for further information.");
            String spreadSheetIN = args[1];
            String spreadSheetOUT = args[2];
            String currentVersion = args[3];

            LicensingList licensingList = new LicensingList();
            licensingList.readFromSpreadsheet(spreadSheetIN, currentVersion);
            licensingList.purge(currentVersion);
            licensingList.writeToSpreadsheet(spreadSheetOUT);

        }else if(args[0].equals("--help")){
            InputStream in =  LicenseUtil.class.getClassLoader().getResourceAsStream(README_PATH);
            Utils utils = new Utils();
            BufferedReader reader =new BufferedReader(new InputStreamReader(in));
            String line;
            while((line = reader.readLine()) !=null){
                System.out.println(line);
            }
        }else if(args[0].equals("--test")){
            String pomFN = args[1];

            LicensingList licensingList = new LicensingList();
            /*MavenProject project = null;
            try {
                project = Utils.readPom(new File(pomFN));

            } catch (XmlPullParserException e) {
                logger.error("Could not parse pom file: \""+pomFN+"\"");
            }*/

            /*try {
                Utils.testResolveArtefact(project);
            } catch (ArtifactResolutionException e) {
                e.printStackTrace();
            }*/

            /*try {
                Utils.test(project);
            } catch (ArtifactDescriptorException | XmlPullParserException | ArtifactResolutionException e) {
                e.printStackTrace();
            }*/
            //try {
                //SortedMap<String, MavenProject> licenses = Utils.loadProjectDependencies(project);
            //} catch (ProjectBuildingException e) {
            //   e.printStackTrace();
            // }

        }else{
            logger.error("Unknown parameter: " + args[0] + ". Use --help to get a list of the possible options.");
        }
    }


    public static LicensingList processProjectsInFolder(File directory, String spreadSheetFN, String currentVersion, Boolean mavenProjectsOnly) throws IOException {

        LicensingList result = new LicensingList();
        File f = new File(spreadSheetFN);
        if (f.exists() && !f.isDirectory()) {
            result.readFromSpreadsheet(spreadSheetFN, currentVersion);
        }

        File[] subdirs = directory.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
        File gitDir = new File(directory.getPath()+File.separator+".git");
        File pomFile = new File(directory.getPath()+File.separator+"pom.xml");

        if(mavenProjectsOnly){
            // check pom.xml
            if(!pomFile.exists()){
                return result;
            }
        }else{
            // check git and update
            if(gitDir.exists()) {
                logger.info("update local repository");
                Utils.updateRepository(directory.getPath());
            }
            for (File dir : subdirs) {
                result.addAll(processProjectsInFolder(dir, spreadSheetFN, currentVersion, true));
            }
            //return result;
        }
        logger.info("process directory: " + directory.getName());

        // check git and update
        if(gitDir.exists()) {
            logger.info("update local repository");
            Utils.updateRepository(directory.getPath());
        }

        logger.info("build effective-pom");
        File pom = new File(directory.getPath()+File.separator+EFFECTIVE_POM_FN);
        Utils.writeEffectivePom(new File(directory.getPath()), pom.getAbsolutePath());
        MavenProject project = null;
        try {
            project = Utils.readPom(pom);
        } catch (Exception e) {
            logger.warn("Could not read from pom file: \"" + pom.getPath() + "\" because of "+e.getMessage());
            return result;
        }
        FileUtils.delete(pom);

        // death first
        for (String module : project.getModules()) {
            File subdirectory = new File(directory + File.separator + module);
            result.addAll(processProjectsInFolder(subdirectory, spreadSheetFN, currentVersion, true));
        }

        /*File f = new File(spreadSheetFN);
        if (f.exists() && !f.isDirectory()) {
            result.readFromSpreadsheet(spreadSheetFN, currentVersion);
        }*/
        result.addMavenProject(project, currentVersion);
        //result.writeToSpreadsheet(spreadSheetFN);

        return result;

        /*for (File dir : subdirs) {
            // check if pom.xml is available
            File pomFile = new File(dir.getPath()+File.separator+"pom.xml");
            if(!pomFile.exists()){
                logger.debug("skip directory: "+dir.getName()+", no pom.xml available");
                continue;
            }

            logger.info("process directory: " + dir.getName());
            File gitDir = new File(dir.getPath()+File.separator+".git");
            if(gitDir.exists()) {
                logger.info("update local repository");
                Utils.updateRepository(dir.getPath());
            }

            LicensingList licensingList = new LicensingList();
            if(dir.isDirectory()){
                licensingList.addAll(processProjectsInFolder(dir,spreadSheetFN,currentVersion));
            }

            logger.info("build effective-pom");
            File pom = new File(dir.getPath()+File.separator+EFFECTIVE_POM_FN);
            Utils.writeEffectivePom(new File(dir.getPath()), pom.getAbsolutePath());
            logger.info("add pom content to tsv");

            MavenProject project = null;
            try {
                project = Utils.readPom(pom);
            } catch (Exception e) {
                logger.warn("Could not read from pom file: \"" + pom.getPath() + "\" because of "+e.getMessage());
                continue;
            }
            FileUtils.delete(pom);

            File f = new File(spreadSheetFN);
            if (f.exists() && !f.isDirectory()) {
                licensingList.readFromSpreadsheet(spreadSheetFN, currentVersion);
            }
            licensingList.addMavenProject(project, currentVersion);
            licensingList.writeToSpreadsheet(spreadSheetFN);
            all.addAll(licensingList);
        }
        return all;*/
    }

    public static void writeLicense3rdPartyFile(String module, LicensingList licensingList, String currentVersion, String targetDir) throws IOException {
        if(targetDir!=null){
            File new3rdPartyLicenseFile = new File(targetDir+File.separator + LICENSE_3RD_PARTY_FN);
            Utils.updateRepository(targetDir);
            boolean exists = new3rdPartyLicenseFile.exists();
            Utils.write(licensingList.getRepoLicensesForModule(module, currentVersion), new3rdPartyLicenseFile);
            if(!exists) {
                Utils.addToRepository(targetDir, LICENSE_3RD_PARTY_FN);
                Utils.commitAndPushRepository(targetDir, "added file: "+ LICENSE_3RD_PARTY_FN);
            }else
                Utils.commitAndPushRepository(targetDir, "updated file: "+ LICENSE_3RD_PARTY_FN);
        }else
            Utils.write(licensingList.getRepoLicensesForModule(module, currentVersion), LICENSE_3RD_PARTY_FN +"."+module);
    }

}
