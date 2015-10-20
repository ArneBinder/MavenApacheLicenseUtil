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

import org.apache.maven.cli.MavenCli;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by Arne Binder (arne.b.binder@gmail.com) on 10.09.2015.
 */
public class Utils {

    static final Logger logger = LoggerFactory.getLogger(Utils.class);



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

    public static void updateRepository(String gitDir) {
        try {
            // Open an existing repository
            Repository existingRepo = new FileRepositoryBuilder()
                    .setGitDir(new File(gitDir + "/.git"))
                    .build();
            Git git = new Git(existingRepo);

            git.pull().call();
        } catch (GitAPIException e) {
            logger.warn("Could not update local repository: \""+gitDir+"\" with git pull.");
        } catch (IOException e){
            logger.warn("Could not open local git repository directory: \""+gitDir+"\"");
        }

    }
}
