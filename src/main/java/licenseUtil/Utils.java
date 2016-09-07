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
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.*;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

import java.io.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

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

    /*public static void test2(MavenProject project) throws DependencyResolutionException {
        File local = new File("/tmp/local-repository");
        Collection<RemoteRepository> remotes = new LinkedList<>();
        for( org.apache.maven.model.Repository repository: project.getModel().getRepositories()){
            remotes.add(new RemoteRepository(repository.getId(), "default", repository.getUrl()));
        }

        DefaultArtifact root = new DefaultArtifact(project.getGroupId(),project.getArtifactId(),"",project.getPackaging(),project.getVersion());

        Collection<Artifact> deps = new Aether(remotes, local).resolve(root,
                "runtime"
        );
        System.out.println("asd");

    }*/

    public static void testResolveArtefact(MavenProject project) throws ArtifactResolutionException {

        System.out.println( "------------------------------------------------------------" );
        //System.out.println( ResolveArtifact.class.getSimpleName() );

        RepositorySystem system = Booter.newRepositorySystem();

        RepositorySystemSession session = Booter.newRepositorySystemSession( system );

        Artifact artifact = new DefaultArtifact(project.getGroupId(),project.getArtifactId(),project.getPackaging(),project.getVersion()); //( "org.eclipse.aether:aether-impl:1.0.0.v20140518" );

        //Artifact artifact = new DefaultArtifact( "org.eclipse.aether:aether-util:1.0.0.v20140518" );

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

        System.out.println( artifact + " resolved to  " + artifact.getFile() );

    }

    public static void test(MavenProject project) throws ArtifactDescriptorException {
        System.out.println( "------------------------------------------------------------" );
        //System.out.println( GetDirectDependencies.class.getSimpleName() );

        RepositorySystem system = Booter.newRepositorySystem();

        RepositorySystemSession session = Booter.newRepositorySystemSession( system );

        Artifact artifact = new DefaultArtifact(project.getGroupId(),project.getArtifactId(),project.getPackaging(),project.getVersion()); //( "org.eclipse.aether:aether-impl:1.0.0.v20140518" );

        ArtifactDescriptorRequest descriptorRequest = new ArtifactDescriptorRequest();
        descriptorRequest.setArtifact( artifact );
        List<RemoteRepository> remotes = new LinkedList<>();
        for( org.apache.maven.model.Repository repository: project.getModel().getRepositories()){
            remotes.add(new RemoteRepository.Builder(repository.getId(), "default", repository.getUrl()).build());
        }
        remotes.add(Booter.newCentralRepository());
        descriptorRequest.setRepositories( remotes );

        ArtifactDescriptorResult descriptorResult = system.readArtifactDescriptor( session, descriptorRequest );

        for ( Dependency dependency : descriptorResult.getDependencies() )
        {
            System.out.println( dependency );
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
