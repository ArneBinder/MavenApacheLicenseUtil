#Maven Apache License Util TEST

If you want to publish your project under the [Apache2 license](http://www.apache.org/licenses/LICENSE-2.0) and it 
includes third-party libraries you have to fulfill their re-publishing requirements. This tool follows [this recommendation](http://programmers.stackexchange.com/questions/234511/what-is-the-best-practice-for-arranging-third-party-library-licenses-paperwork) 
and helps you to create LICENSE-3RD-PARTY files. 

[These PAQs](http://www.apache.org/legal/resolved.html) give an overview about licenses which are compatible with the Apache2 license.

##The workflow is as follows:

1. Generate an effective-pom file for the project you want to add a LICENSE-3RD-PARTY file:
	- `maven-apache-license-util --buildEffectivePom <MavenProjectDirectory>`
2. Add it to the tsv-file (which doesnt exists in the first run):
	- `maven-apache-license-util --addPomToTsv effective-pom.xml licenses.stub.tsv`
3. Repeat step 1 and 2 until all projects you want to have LICENSE-3RD-PARTY files for are added
4. Enhance the licenses.stub.tsv by yourself:
	- Especially fill the "license" column according to the filenames of the license templates in resources/templates (APACHE2, BSD, CDDL, EPLV1, GPLV2. GPLV3, H2, JSON, LGPLV3, MIT).
	- Furthermore, fill the column "bundle" for better readability and "copyRightInformation", if this information is available.
5. Create the LICENSE-3RD-PARTY files by
	- `maven-apache-license-util --writeLicense3rdParty licenses.enhanced.tsv ALL`

##Usage

maven-apache-license-util \<option\> [parameters...]

##Options
```
--buildEffectivePom <MavenProjectDirectory>			Generates an effective-pom file ("effective-pom.xml") in the current folder.
	<MavenProjectDirectory>		the maven project directory containing the pom file

--addPomToTsv <pomFile> <tsvFile> [<tsvOutput>]		Analyzes a maven pom file and generates a table stub, 
													which contains each referenced (as dependency or plugin) library of the project.
													You should use an EFFECTIVE-POM to get all information.
													Dependencies with scope "test" are not considered.
													
	<pomFile>		the pom file embedding the 3rd-party libraries 
	<tsvFile>		The analyzed pom information is written to this file. If it exists already, the content is merged.
	<tsvOutput>		if set, the generated table stub is written to this file instead of <tsvFile> (OPTIONAL)

--writeLicense3rdParty <tsvFile> (ALL|<project>)	Use the information of the <tsvFile> and generate LICENSE-3RD-PARTY files via templates from the resources/templates folder.
	<tsvFile>		The enhanced (by you) tsv table stub
	<project>		If you just want to have the LICENSE-3RD-PARTY file of a certain project, use the maven artifactId.
					"ALL" creates the LICENSE-3RD-PARTY files for all projects appearing in the <tsvFile>.
````

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
