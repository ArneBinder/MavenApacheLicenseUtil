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
                logger.error("Missing arguments for option --addPomToTsv. Please specify <pomFileName> <licenses.stub.tsv> <currentVersion> or use the option --help for further information");
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
                logger.error("Missing arguments for option --writeLicense3rdParty. Please provide <licenses.enhanced.tsv> <processModule> and <currentVersion> or use the option --help for further information");
            String spreadSheetFN = args[1];
            String processModule = args[2];
            String currentVersion = args[3];

            LicensingList licensingList = new LicensingList();
            licensingList.readFromSpreadsheet(spreadSheetFN, currentVersion);
            if(processModule.equals("ALL")){
                for(String module: licensingList.getNonFixedHeaders()){
                    Utils.write(licensingList.getRepoLicensesForModule(module), "LICENSE-3RD-PARTY."+module);
                }
            }else {
                Utils.write(licensingList.getRepoLicensesForModule(processModule), "LICENSE-3RD-PARTY."+processModule);
            }
        }else if(args[0].equals("--buildEffectivePom")){
                Utils.writeEffectivePom(new File(args[1]), (new File("effective-pom.xml")).getAbsolutePath());
        }else if(args[0].equals("--processProjectsInFolder")){
            if(args.length<4)
                logger.error("Missing arguments for option --processProjectsInFolder. Please provide <superDirectory> <licenses.stub.tsv> and <currentVersion> or use the option --help for further information");
            File directory = new File(args[1]);
            String licenseStubFN = args[2];
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
                File f = new File(licenseStubFN);
                if (f.exists() && !f.isDirectory()) {
                    licensingList.readFromSpreadsheet(licenseStubFN, currentVersion);
                }
                licensingList.addMavenProject(project, currentVersion);
                licensingList.writeToSpreadsheet(licenseStubFN);
            }
        }else if(args[0].equals("--help")){
            logger.info(
                    "\nusage: maven-license-util <option> [parameters...]\n"
                            + "\n"
                            + "possible options:\n"
                            + "--buildEffectivePom <MavenProjectDirectory>"
                            + "\t\t\tGenerates an effective-pom file (\"effective-pom.xml\") in the current folder.\n"
                            + "\t<MavenProjectDirectory>\t\tthe maven project directory containing the pom file\n"
                            + "\n"
                            + "--addPomToTsv <pomFile> <tsvFile> [<tsvOutput>]\t\tAnalyzes a maven pom file and generates a table stub, \n" +
                            "\t\t\t\t\t\t\t\t\t\t\t\t\twhich contains each referenced (as dependency or plugin) library of the project.\n" +
                            "\t\t\t\t\t\t\t\t\t\t\t\t\tYou should use an EFFECTIVE-POM to get all information.\n" +
                            "\t\t\t\t\t\t\t\t\t\t\t\t\tDependencies with scope \"test\" are not considered.\n"
                            + "\t<pomFile>\t\tthe pom file embedding the 3rd-party libraries \n"
                            + "\t<tsvFile>\t\tThe analyzed pom information is written to this file. If it exists already, the content is merged.\n"
                            + "\t<tsvOutput>\t\tif set, the generated table stub is written to this file instead of <tsvFile> (OPTIONAL)\n"
                            + "\n"
                            + "--writeLicense3rdParty <tsvFile> (ALL|<project>)"
                            + "\tUse the information of the <tsvFile> and generate LICENSE-3RD-PARTY files via templates from the resources/templates folder.\n"
                            + "\t<tsvFile>\t\tThe enhanced (by you) tsv table stub\n"
                            + "\t<project>\t\tIf you just want to have the LICENSE-3RD-PARTY file of a certain project, use the maven artifactId.\n"
                            + "\t\t\t\t\t\"ALL\" creates the LICENSE-3RD-PARTY files for all projects appearing in the <tsvFile>.\n"
                            + "\n"
                            + "The workflow is as follows:\n"
                            + "\t1. Generate an effective-pom file for the project you want to add a LICENSE-3RD-PARTY file:\n" +
                            "\t\t\tmaven-license-util --buildEffectivePom <MavenProjectDirectory>\n"
                            + "\t2. Add it to the tsv-file (which doesnt exists in the first run):\n" +
                            "\t\t\tmaven-license-util --addPomToTsv effective-pom.xml licenses.stub.tsv\n"
                            + "\t3. Repeat step 1 and 2 until all projects you want to have LICENSE-3RD-PARTY files for are added\n"
                            + "\t4. Enhance the licenses.stub.tsv by yourself:\n" +
                            "\t\t\tEspecially fill the \"license\" column according to the filenames of the license templates \n" +
                            "\t\t\tin resources/templates (APACHE2, BSD, CDDL, EPLV1, GPLV2. GPLV3, H2, JSON, LGPLV3, MIT).\n" +
                            "\t\t\tFurthermore, fill the column \"bundle\" for better readability and \"copyRightInformation\",\n" +
                            "\t\t\tif this information is available.\n"
                            + "\t5. Create the LICENSE-3RD-PARTY files by\n" +
                            "\t\t\t--writeLicense3rdParty licenses.enhanced.tsv ALL"

            );
        }else{
            logger.error("Unknown parameter: "+args[0]+". Use --help to get a list of the possible options.");
        }
    }


}
