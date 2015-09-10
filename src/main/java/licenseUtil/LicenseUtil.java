/**
 * Copyright (C) ${project.inceptionYear} Arne Binder (arne.b.binder@gmail.com)
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
            MavenProject project = Utils.readPom(new File(args[1]));
            LicensingList licensingList = new LicensingList();
            File f = new File(args[2]);
            if (f.exists() && !f.isDirectory()) {
                licensingList.readFromSpreadsheet(args[2]);
            }
            licensingList.addMavenProject(project);
            licensingList.writeToSpreadsheet(args.length > 3 ? args[3] : args[2]);
        }else if(args[0].equals("--writeLicense3rdParty")){
            LicensingList licensingList = new LicensingList();
            licensingList.readFromSpreadsheet(args[1]);
            if(args[2].equals("ALL")){
                for(String module: licensingList.getNonFixedHeaders()){
                    Utils.write(licensingList.getRepoLicensesForModule(module), "LICENSE-3RD-PARTY."+module);
                }
            }else {
                Utils.write(licensingList.getRepoLicensesForModule(args[2]), "LICENSE-3RD-PARTY."+args[2]);
            }
        }else if(args[0].equals("--buildEffectivePom")){
                Utils.writeEffectivePom(new File(args[1]), (new File("effective-pom.xml")).getAbsolutePath());
        }else if(args[0].equals("--help")){
            logger.info(
                    "\nusage: maven-license-util <option> [parameters...]\n"
                    + "\n"
                    + "possible options:\n"
                    + "--buildEffectivePom <MavenProjectDirectory>"
                    + "\t\t\tGenerates an effective-pom file (\"effective-pom.xml\") in the current folder.\n"
                    + "\t<MavenProjectDirectory>\t\tthe maven project directory containing the pom file\n"
                    + "\n"
                    + "--addPomToTsv <pomFile> <tsvFile> [<tsvOutput>]"
                    + "\t\tAnalyzes a maven pom file and generates a table stub, \n" +
                        "\t\t\t\t\t\t\t\t\t\t\t\t\twhich contains each referenced (as dependency or plugin) library of the project.\n" +
                        "\t\t\t\t\t\t\t\t\t\t\t\t\tYou should use an EFFECTIVE-POM to get all information.\n" +
                        "\t\t\t\t\t\t\t\t\t\t\t\t\tIf the <tsvInput> parameter is set, the library informations will be added to the given tsv file.\n"
                    + "\t<pomFile>\t\tthe pom file to add their referenced libraries from\n"
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
