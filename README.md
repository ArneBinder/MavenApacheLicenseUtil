#Maven License Util

##The workflow is as follows:
	1. Generate an effective-pom file for the project you want to add a LICENSE-3RD-PARTY file:
			maven-license-util --buildEffectivePom \<MavenProjectDirectory\>
	2. Add it to the tsv-file (which doesnt exists in the first run):
			maven-license-util --addPomToTsv effective-pom.xml licenses.stub.tsv
	3. Repeat step 1 and 2 until all projects you want to have LICENSE-3RD-PARTY files for are added
	4. Enhance the licenses.stub.tsv by yourself:
			Especially fill the "license" column according to the filenames of the license templates 
			in resources/templates (APACHE2, BSD, CDDL, EPLV1, GPLV2. GPLV3, H2, JSON, LGPLV3, MIT).
			Furthermore, fill the column libraryName for better readability and copyRightInformation,
			if this information is available.
	5. Create the LICENSE-3RD-PARTY files by
			--writeLicense3rdParty licenses.enhanced.tsv ALL

##Usage

maven-license-util \<option\> [parameters...]

##Options:
```
--buildEffectivePom <MavenProjectDirectory>			Generates an effective-pom file ("effective-pom.xml") in the current folder.
	<MavenProjectDirectory>		the maven project directory containing the pom file

--addPomToTsv <pomFile> <tsvFile> [<tsvOutput>]		Analyzes a maven pom file and generates a table stub, 
													which contains each referenced (as dependency or plugin) library of the project.
													You should use an EFFECTIVE-POM to get all information.
													If the <tsvInput> parameter is set, the library informations will be added to the given tsv file.
	<pomFile>		the pom file to add their referenced libraries from
	<tsvFile>		The analyzed pom information is written to this file. If it exists already, the content is merged.
	<tsvOutput>		if set, the generated table stub is written to this file instead of <tsvFile> (OPTIONAL)

--writeLicense3rdParty <tsvFile> (ALL|<project>)	Use the information of the <tsvFile> and generate LICENSE-3RD-PARTY files via templates from the resources/templates folder.
	<tsvFile>		The enhanced (by you) tsv table stub
	<project>		If you just want to have the LICENSE-3RD-PARTY file of a certain project, use the maven artifactId.
					"ALL" creates the LICENSE-3RD-PARTY files for all projects appearing in the <tsvFile>.
````
