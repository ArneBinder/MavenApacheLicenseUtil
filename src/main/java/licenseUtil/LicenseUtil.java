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
            logger.error("Missing parameters. Use --help to get a list of the possible options.");
        }else if(args[0].equals("--addPomToTsv")){
            if(args.length<4)
                logger.error("Missing arguments for option --addPomToTsv. Please specify <pomFileName> <licenses.stub.tsv> <currentVersion> or use the option --help for further information.");
            String pomFN = args[1];
            String spreadSheetFN = args[2];
            String currentVersion = args[3];

            MavenProject project = Utils.readPom(new File(pomFN));
            LicensingList licensingList = new LicensingList();
            File f = new File(spreadSheetFN);
            if (f.exists() && !f.isDirectory()) {
                licensingList.readFromSpreadsheet(spreadSheetFN, currentVersion);
            }

            licensingList.addMavenProject(project, currentVersion);
            licensingList.writeToSpreadsheet(spreadSheetFN);
        }else if(args[0].equals("--writeLicense3rdParty")){
            if(args.length<4)
                logger.error("Missing arguments for option --writeLicense3rdParty. Please provide <licenses.enhanced.tsv> <processModule> and <currentVersion> or use the option --help for further information.");
            String spreadSheetFN = args[1];
            String processModule = args[2];
            String currentVersion = args[3];

            LicensingList licensingList = new LicensingList();
            licensingList.readFromSpreadsheet(spreadSheetFN, currentVersion);
            if(processModule.toUpperCase().equals("ALL")){
                for(String module: licensingList.getNonFixedHeaders()){
                    Utils.write(licensingList.getRepoLicensesForModule(module, currentVersion), "LICENSE-3RD-PARTY."+module);
                }
            }else {
                Utils.write(licensingList.getRepoLicensesForModule(processModule, currentVersion), "LICENSE-3RD-PARTY."+processModule);
            }
        }else if(args[0].equals("--buildEffectivePom")){
                Utils.writeEffectivePom(new File(args[1]), (new File("effective-pom.xml")).getAbsolutePath());
        }else if(args[0].equals("--processProjectsInFolder")){
            if(args.length<4)
                logger.error("Missing arguments for option --processProjectsInFolder. Please provide <superDirectory> <licenses.stub.tsv> and <currentVersion> or use the option --help for further information.");
            File directory = new File(args[1]);
            String spreadSheetFN = args[2];
            String currentVersion = args[3];

            File[] subdirs = directory.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
            for (File dir : subdirs) {
                logger.info("process module: " + dir.getName());
                logger.info("update local repository");
                Utils.updateRepository(dir.getPath());
                logger.info("build effective-pom");
                File pom = new File(dir.getPath()+"/effective-pom.xml");
                Utils.writeEffectivePom(new File(dir.getPath()), pom.getAbsolutePath());
                logger.info("add pom content to tsv");

                MavenProject project = Utils.readPom(pom);
                LicensingList licensingList = new LicensingList();
                File f = new File(spreadSheetFN);
                if (f.exists() && !f.isDirectory()) {
                    licensingList.readFromSpreadsheet(spreadSheetFN, currentVersion);
                }
                licensingList.addMavenProject(project, currentVersion);
                licensingList.writeToSpreadsheet(spreadSheetFN);
            }
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
            logger.info(
                    "\nusage: maven-license-util <option> [parameters...]\n"
                            + "\n"
                            + "possible options:\n"
                            + "--processProjectsInFolder <superDirectory> <tsvFile> <currentReleaseVersion>\t\n" +
                            "\t<superDirectory>\t\tThe directory containing the project directories.\n" +
                            "\t<tsvFile>\t\t\t\tThe analyzed pom information is written to this file. If it exists already, the content is merged.\n" +
                            "\t<currentReleaseVersion>\tThis value will be written into the project columns of the tsv if the project uses the library specified by the current row\n" +
                            "\t\n" +
                            "--buildEffectivePom <MavenProjectDirectory>\t\t\tGenerates an effective-pom file (\"effective-pom.xml\") in the current folder.\n" +
                            "\t<MavenProjectDirectory>\t\tthe maven project directory containing the pom file\n" +
                            "\n" +
                            "--addPomToTsv <pomFile> <tsvFile>\t\t\t\t\tAnalyzes a maven pom file and generates a table stub, \n" +
                            "\t\t\t\t\t\t\t\t\t\t\t\t\twhich contains each referenced (as dependency or plugin) library of the project.\n" +
                            "\t\t\t\t\t\t\t\t\t\t\t\t\tYou should use an EFFECTIVE-POM to get all information.\n" +
                            "\t\t\t\t\t\t\t\t\t\t\t\t\tDependencies with scope \"test\" are not considered.\t\t\t\t\t\t\t\n" +
                            "\t<pomFile>\t\tthe pom file embedding the 3rd-party libraries \n" +
                            "\t<tsvFile>\t\tThe analyzed pom information is written to this file. If it exists already, the content is merged.\n" +
                            "\t<currentReleaseVersion>\t\twill be written into the project columns if the project uses the library specified by the current row\n" +
                            "\n" +
                            "--writeLicense3rdParty <tsvFile> (ALL|<project>)\tUse the information of the <tsvFile> and generate LICENSE-3RD-PARTY files via templates from the resources/templates folder.\n" +
                            "\t<tsvFile>\t\tThe enhanced (by you) tsv table stub\n" +
                            "\t<project>\t\tIf you just want to have the LICENSE-3RD-PARTY file of a certain project, use the maven artifactId of this one.\n" +
                            "\t\t\t\t\t\"ALL\" creates the LICENSE-3RD-PARTY files for all projects appearing in the <tsvFile>.\n" +
                            "\t<currentReleaseVersion>\t\tOnly the libraries which have the <currentReleaseVersion> or string \"KEEP\" in the <project> column are collected. \n" +
                            "\t\t\t\t\t\t\t\t\"KEEP\" can be used, if you have added a library manually to the list." +
                            "\n" +
                            "--purgeTsv <spreadSheetIN.tsv> <spreadSheetOUT.tsv> <currentReleaseVersion>\t\t\n" +
                            "\t\tDeletes all entries, which do not link the library via <currentReleaseVersion> to a project, except entries marked with \"KEEP\".\n" +
                            "\t<spreadSheetIN.tsv>\t\tThe input tsv file with licensing information\n" +
                            "\t<spreadSheetOUT.tsv>\tThe purged output tsv\n" +
                            "\t<currentReleaseVersion>\tThe entries, which should stay in the table, should be linked via this version in the project columns."

            );
        }else{
            logger.error("Unknown parameter: "+args[0]+". Use --help to get a list of the possible options.");
        }
    }


}
