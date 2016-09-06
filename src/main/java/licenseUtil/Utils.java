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
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.List;

import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyFilter;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.resolution.DependencyRequest;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.JavaScopes;
import org.sonatype.aether.util.filter.DependencyFilterUtils;


import java.io.*;

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

    public static void write(String content, String filename){
        logger.info("write to file \""+filename+"\"");
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

    public void test() throws DependencyResolutionException, org.eclipse.aether.resolution.DependencyResolutionException {

        System.out.println( "------------------------------------------------------------" );
        //System.out.println( ResolveTransitiveDependencies.class.getSimpleName() );

        RepositorySystem system = Booter.newRepositorySystem();

        RepositorySystemSession session = Booter.newRepositorySystemSession( system );

        Artifact artifact = new DefaultArtifact( "org.eclipse.aether:aether-impl:1.0.0.v20140518" );

        DependencyFilter classpathFlter = DependencyFilterUtils.classpathFilter( JavaScopes.COMPILE );

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot( new Dependency( artifact, JavaScopes.COMPILE ) );
        collectRequest.setRepositories( Booter.newRepositories( system, session ) );

        DependencyRequest dependencyRequest = new DependencyRequest( collectRequest, classpathFlter );

        List<ArtifactResult> artifactResults =
                system.resolveDependencies( session, dependencyRequest ).getArtifactResults();

        for ( ArtifactResult artifactResult : artifactResults )
        {
            System.out.println( artifactResult.getArtifact() + " resolved to " + artifactResult.getArtifact().getFile() );
        }

    }

    /*public static SortedMap<String, MavenProject> loadProjectDependencies(MavenProject project
                                                                   //,ArtifactRepository localRepository,
                                                                   //,List<ArtifactRepository> remoteRepositories
    ) throws ProjectBuildingException {

        MavenProjectBuilder mavenProjectBuilder = new DefaultMavenProjectBuilder();

        Artifact artifact = new DefaultArtifact( "org.sonatype.aether:aether-util:1.9" );

        //List<ArtifactRepository> remoteRepositories =  project.getRemoteArtifactRepositories();

        //ArtifactRepository localRepository = project.getArtifact().getRepository();

        Set<?> depArtifacts;

        //if ( configuration.isIncludeTransitiveDependencies() )
        //{
            // All project dependencies
            depArtifacts = project.getArtifacts();
        //}
        //else
        //{
            // Only direct project dependencies
        //    depArtifacts = project.getDependencyArtifacts();
        //}

//        List<String> includedScopes = configuration.getIncludedScopes();
//        List<String> excludeScopes = configuration.getExcludedScopes();
//
//        boolean verbose = configuration.isVerbose();


        SortedMap<String, MavenProject> result = new TreeMap<String, MavenProject>();

        for ( Object o : depArtifacts )
        {
            Artifact artifact = (Artifact) o;


            String scope = artifact.getScope();


            //org.codehaus.plexus.logging.Logger log = getLogger();

            String id = artifact.getGroupId()+":"+artifact.getArtifactId()+":"+artifact.getVersion();

            MavenProject depMavenProject = null;


                // build project

                //try
                //{
                    //depMavenProject =
                      //      mavenProjectBuilder.buildFromRepository( artifact, remoteRepositories, localRepository, true );
                    //depMavenProject.getArtifact().setScope( artifact.getScope() );
                //}
                //catch ( ProjectBuildingException e )
                //{
                    //log.warn( "Unable to obtain POM for artifact : " + artifact, e );
                //    continue;
                //}




            // keep the project
            result.put( id, depMavenProject );
        }
        return result;
    }*/







}
