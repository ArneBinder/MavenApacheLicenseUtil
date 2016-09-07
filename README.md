#Maven Apache License Util

If you want to publish your project under the [Apache2 license](http://www.apache.org/licenses/LICENSE-2.0) and it 
includes third-party libraries you have to fulfill their re-publishing requirements. This tool follows 
[this recommendation](http://programmers.stackexchange.com/questions/234511/what-is-the-best-practice-for-arranging-third-party-library-licenses-paperwork) 
and helps you to create LICENSE-3RD-PARTY files. 

[These PAQs](http://www.apache.org/legal/resolved.html) give an overview about licenses which are compatible with the Apache2 license.

##The workflow is as follows:

1. Update local repositories, build effective-poms and add pom content to `<license.stub.tsv>` for all projects, 
which are direct subfolders of `<superDirectory>`, by executing:
	- `maven-apache-license-util --processProjectsInFolder <superDirectory> <license.stub.tsv> <currentReleaseVersion>`
	
	`<currentReleaseVersion>` should be a value, which marks your current release.
	If you have an `<licenses.enhanced.tsv>` from an earlier release, use this as `<license.stub.tsv>`. 
	The former license information will be kept, just the old `<currentReleaseVersion>` value will be overwritten, 
	if the library is still in use for the current release.

	NOTE: This will overwrite the old `<licenses.enhanced.tsv>`. 
2. Enhance the licenses.stub.tsv by yourself:
   	- Especially fill the "license" column according to the filenames of the license templates in resources/templates 
   		(APACHE2, BSD, BSD3, CDDL, EPLV1, GPLV2, GPLV3, H2, JSON, LGPLV3, MIT, SLICKBSD, UNLICENSED).
   	- Furthermore, fill the column "bundle" and "libraryName" for better readability and "copyRightInformation", 
   		if this information is available.
   	- Add projects which does not use maven manually, if needed. Write "KEEP" at the position of the libraries 
   		of these models (instead of `<currentReleaseVersion>` in the auto generated columns) to avoid 
   		the need of updating these fields manually, when you want to release the next time.
  
3. Create the LICENSE-3RD-PARTY files by
	- `maven-apache-license-util --writeLicense3rdParty <licenses.enhanced.tsv> ALL <currentReleaseVersion> [<targetDir>]` 
	
	This will take only the libraries which have the `<currentReleaseVersion>` (or `KEEP`) at the project columns.
	In addition you can use the optional parameter <targetDir>. IF you do so, all direct subfolders of the <targetDir> 
	folder are considered as maven projects and if a project with the same artifactID as in the tsv column headers (projects) 
	is found, the LICENSE-3RD-PARTY file is written into the containing folder. 
	Furthermore, the local repo is updated and the changes in the LICENSE-3RD-PARTY file are committed 
	and pushed for these files, if no special access rights are needed.

##Usage

`maven-apache-license-util <option> [parameters...]`

##Options
```
--processProjectsInFolder <superDirectory> <tsvFile> <currentReleaseVersion>	
	<superDirectory>			The directory containing the project directories.
	<tsvFile>					The analyzed pom information is written to this file. 
								If it exists already, the content is merged.
	<currentReleaseVersion>		This value will be written into the project columns of the tsv 
								if the project uses the library specified by the current row
	
--buildEffectivePom <MavenProjectDirectory>			
		Generates an effective-pom file ("effective-pom.xml") in the current folder.
	<MavenProjectDirectory>		the maven project directory containing the pom file

--addPomToTsv <pomFile> <tsvFile> <currentReleaseVersion>
		Analyzes a maven pom file and generates a table stub, which contains 
		each referenced (as dependency or plugin) library of the project.
		You should use an EFFECTIVE-POM to get all information.
		Dependencies with scope "test" are not considered.							
	<pomFile>					the pom file embedding the 3rd-party libraries 
	<tsvFile>					The analyzed pom information is written to this file. 
								If it exists already, the content is merged.
	<currentReleaseVersion>		will be written into the project columns 
								if the project uses the library specified by the current row

--writeLicense3rdParty <tsvFile> (ALL|<project>) <currentReleaseVersion> [<targetDir>]	
		Use the information of the <tsvFile> and generate LICENSE-3RD-PARTY files 
		via templates from the resources/templates folder.
	<tsvFile>					The enhanced (by you) tsv table stub
	<project>					If you just want to have the LICENSE-3RD-PARTY file 
								of a certain project, use the maven artifactId of this one.
								"ALL" creates the LICENSE-3RD-PARTY files for 
								all projects appearing in the <tsvFile>.
	<currentReleaseVersion>		Only the libraries which have the <currentReleaseVersion> 
								or string "KEEP" in the <project> column are collected. 
								"KEEP" can be used, if you have added a library manually to the list.
	<targetDir>					This folder is searched for maven projects with the same 
								artifactID as in the tsv column headers (projects). 
								If found, the LICENSE-3RD-PARTY file is written into the containing folder. 
								Furthermore, the local repo is updated and the changes in 
								the LICENSE-3RD-PARTY file are committed and pushed for these files, 
								if no special access rights are needed.
--purgeTsv <spreadSheetIN.tsv> <spreadSheetOUT.tsv> <currentReleaseVersion>		
		Deletes all entries, which do not link the library via <currentReleaseVersion> to a project, 
		except entries marked with "KEEP".
	<spreadSheetIN.tsv>			The input tsv file with licensing information
	<spreadSheetOUT.tsv>		The purged output tsv
	<currentReleaseVersion>		The entries, which should stay in the table, 
								should be linked via this version in the project columns.
```

## License

Copyright (C) Arne Binder

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

This project uses 3rd party tools. You can find the list of 3rd party tools including their authors and licenses [here](LICENSE-3RD-PARTY).
