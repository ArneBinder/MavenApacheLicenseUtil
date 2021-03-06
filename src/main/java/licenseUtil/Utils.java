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


import licenseUtil.aether.Booter;
import org.apache.maven.cli.MavenCli;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Arne Binder (arne.b.binder@gmail.com) on 10.09.2015.
 */
public class Utils {

    static final Logger logger = LoggerFactory.getLogger(Utils.class);

    public static MavenProject readPom(File pomfile) throws IOException, XmlPullParserException {
        logger.info("read pom file from \""+pomfile.getPath()+"\"...");
        Model model = null;
        FileReader reader;
        MavenXpp3Reader mavenreader = new MavenXpp3Reader();

        reader = new FileReader(pomfile);
        model = mavenreader.read(reader, false);
        model.setPomFile(pomfile);
        reader.close();

        MavenProject project = new MavenProject(model);
        return project;
    }

    public static void writeEffectivePom(File projectDirectory, String fullEffectivePomFilename){
        MavenCli cli = new MavenCli();
        logger.info("Write effective-pom to \""+fullEffectivePomFilename+"\"");
        cli.doMain(new String[]{"org.apache.maven.plugins:maven-help-plugin:2.2:effective-pom", "-Doutput=" + fullEffectivePomFilename}, projectDirectory.getAbsolutePath(), new PrintStream(new OutputStream(){
            public void write(int b) {
                //NO-OP
            }}), System.err);
    }
    
    public static void write(String content, File file){
        logger.info("write to file \""+file.getPath()+"\"");
        Writer out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
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

    public static void commitAndPushRepository(String gitDir, String message) {
        try {
            // Open an existing repository
            Repository existingRepo = new FileRepositoryBuilder()
                    .setGitDir(new File(gitDir + "/.git"))
                    .build();
            Git git = new Git(existingRepo);

            logger.info("commit & push to repo ("+gitDir+"): "+message);
            git.commit().setMessage(message).call();
            git.push().call();
        } catch (GitAPIException e) {
            logger.warn("Could not commit and push local repository: \""+gitDir+"\" with message: \""+message+"\" because of "+e.getMessage());
        } catch (IOException e){
            logger.warn("Could not open local git repository directory: \""+gitDir+"\"");
        }

    }

    public static void addToRepository(String gitDir, String newFN) {
        try {
            // Open an existing repository
            Repository existingRepo = new FileRepositoryBuilder()
                    .setGitDir(new File(gitDir + "/.git"))
                    .build();
            Git git = new Git(existingRepo);
            File newFile = new File(gitDir+File.separator+newFN);
            newFile.createNewFile();
            git.add().addFilepattern(newFile.getName()).call();
        } catch (GitAPIException e) {
            logger.warn("Could not the file \""+newFN+"\" to local repository: \""+gitDir+".");
        } catch (IOException e){
            logger.warn("Could not open local git repository directory: \""+gitDir+"\"");
        }

    }

    public static MavenProject resolveArtifact(MavenProject project, Artifact artifact) throws ArtifactResolutionException, IOException, XmlPullParserException {
        logger.debug( "resolve artifact " + artifact.toString() );
        
        RepositorySystem system = Booter.newRepositorySystem();
        RepositorySystemSession session = Booter.newRepositorySystemSession( system );
        
        ArtifactRequest artifactRequest = new ArtifactRequest();
        artifactRequest.setArtifact( artifact );

        List<RemoteRepository> remotes = new LinkedList<>();
        for( org.apache.maven.model.Repository repository: project.getModel().getRepositories()){
            remotes.add(new RemoteRepository.Builder(repository.getId(), "default", repository.getUrl()).build());
        }
        remotes.add(Booter.newCentralRepository());
        artifactRequest.setRepositories( remotes );

        ArtifactResult artifactResult = system.resolveArtifact( session, artifactRequest );
        artifact = artifactResult.getArtifact();
        logger.info( "resolved artifact \""+ artifact + "\" to \"" + artifact.getFile() + "\"" );

        return Utils.readPom(artifact.getFile());
    }
    
    static public String getValue(Map<String, String> map, String key){
        if(map.containsKey(key))
            return map.get(key);

        if(key==null)
            return null;
        for(String k: map.keySet()){
            if(k.contains(key) || key.contains(k))
                return map.get(k);
        }

        return null;
    }
}
