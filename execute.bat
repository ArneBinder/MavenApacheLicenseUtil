@echo off 

set OLDDIR=%CD%


set LicenseUtil=-Didea.launcher.port=7555 "-Didea.launcher.bin.path=C:\Program Files (x86)\JetBrains\IntelliJ IDEA 14.1.5\bin" -Dfile.encoding=windows-1252 -classpath "%OLDDIR%\%2;C:\Program Files\Java\jdk1.7.0_79\jre\lib\charsets.jar;C:\Program Files\Java\jdk1.7.0_79\jre\lib\deploy.jar;C:\Program Files\Java\jdk1.7.0_79\jre\lib\javaws.jar;C:\Program Files\Java\jdk1.7.0_79\jre\lib\jce.jar;C:\Program Files\Java\jdk1.7.0_79\jre\lib\jfr.jar;C:\Program Files\Java\jdk1.7.0_79\jre\lib\jfxrt.jar;C:\Program Files\Java\jdk1.7.0_79\jre\lib\jsse.jar;C:\Program Files\Java\jdk1.7.0_79\jre\lib\management-agent.jar;C:\Program Files\Java\jdk1.7.0_79\jre\lib\plugin.jar;C:\Program Files\Java\jdk1.7.0_79\jre\lib\resources.jar;C:\Program Files\Java\jdk1.7.0_79\jre\lib\rt.jar;C:\Program Files\Java\jdk1.7.0_79\jre\lib\ext\access-bridge-64.jar;C:\Program Files\Java\jdk1.7.0_79\jre\lib\ext\dnsns.jar;C:\Program Files\Java\jdk1.7.0_79\jre\lib\ext\jaccess.jar;C:\Program Files\Java\jdk1.7.0_79\jre\lib\ext\localedata.jar;C:\Program Files\Java\jdk1.7.0_79\jre\lib\ext\sunec.jar;C:\Program Files\Java\jdk1.7.0_79\jre\lib\ext\sunjce_provider.jar;C:\Program Files\Java\jdk1.7.0_79\jre\lib\ext\sunmscapi.jar;C:\Program Files\Java\jdk1.7.0_79\jre\lib\ext\zipfs.jar;C:\Users\arbi01\.m2\repository\org\apache\commons\commons-csv\1.1\commons-csv-1.1.jar;C:\Users\arbi01\.m2\repository\org\apache\maven\maven-model\3.3.3\maven-model-3.3.3.jar;C:\Users\arbi01\.m2\repository\org\codehaus\plexus\plexus-utils\3.0.20\plexus-utils-3.0.20.jar;C:\Users\arbi01\.m2\repository\org\apache\maven\maven-embedder\3.1.1\maven-embedder-3.1.1.jar;C:\Users\arbi01\.m2\repository\org\apache\maven\maven-settings\3.1.1\maven-settings-3.1.1.jar;C:\Users\arbi01\.m2\repository\org\apache\maven\maven-core\3.1.1\maven-core-3.1.1.jar;C:\Users\arbi01\.m2\repository\org\apache\maven\maven-settings-builder\3.1.1\maven-settings-builder-3.1.1.jar;C:\Users\arbi01\.m2\repository\org\apache\maven\maven-repository-metadata\3.1.1\maven-repository-metadata-3.1.1.jar;C:\Users\arbi01\.m2\repository\org\apache\maven\maven-artifact\3.1.1\maven-artifact-3.1.1.jar;C:\Users\arbi01\.m2\repository\org\apache\maven\maven-aether-provider\3.1.1\maven-aether-provider-3.1.1.jar;C:\Users\arbi01\.m2\repository\org\eclipse\aether\aether-impl\0.9.0.M2\aether-impl-0.9.0.M2.jar;C:\Users\arbi01\.m2\repository\org\codehaus\plexus\plexus-interpolation\1.19\plexus-interpolation-1.19.jar;C:\Users\arbi01\.m2\repository\org\apache\maven\maven-plugin-api\3.1.1\maven-plugin-api-3.1.1.jar;C:\Users\arbi01\.m2\repository\org\apache\maven\maven-model-builder\3.1.1\maven-model-builder-3.1.1.jar;C:\Users\arbi01\.m2\repository\org\apache\maven\maven-compat\3.1.1\maven-compat-3.1.1.jar;C:\Users\arbi01\.m2\repository\org\codehaus\plexus\plexus-classworlds\2.5.1\plexus-classworlds-2.5.1.jar;C:\Users\arbi01\.m2\repository\org\eclipse\sisu\org.eclipse.sisu.plexus\0.0.0.M5\org.eclipse.sisu.plexus-0.0.0.M5.jar;C:\Users\arbi01\.m2\repository\javax\enterprise\cdi-api\1.0\cdi-api-1.0.jar;C:\Users\arbi01\.m2\repository\javax\annotation\jsr250-api\1.0\jsr250-api-1.0.jar;C:\Users\arbi01\.m2\repository\javax\inject\javax.inject\1\javax.inject-1.jar;C:\Users\arbi01\.m2\repository\com\google\guava\guava\10.0.1\guava-10.0.1.jar;C:\Users\arbi01\.m2\repository\com\google\code\findbugs\jsr305\1.3.9\jsr305-1.3.9.jar;C:\Users\arbi01\.m2\repository\org\sonatype\sisu\sisu-guice\3.1.0\sisu-guice-3.1.0-no_aop.jar;C:\Users\arbi01\.m2\repository\aopalliance\aopalliance\1.0\aopalliance-1.0.jar;C:\Users\arbi01\.m2\repository\org\eclipse\sisu\org.eclipse.sisu.inject\0.0.0.M5\org.eclipse.sisu.inject-0.0.0.M5.jar;C:\Users\arbi01\.m2\repository\org\codehaus\plexus\plexus-component-annotations\1.5.5\plexus-component-annotations-1.5.5.jar;C:\Users\arbi01\.m2\repository\org\sonatype\plexus\plexus-sec-dispatcher\1.3\plexus-sec-dispatcher-1.3.jar;C:\Users\arbi01\.m2\repository\org\sonatype\plexus\plexus-cipher\1.7\plexus-cipher-1.7.jar;C:\Users\arbi01\.m2\repository\org\slf4j\slf4j-api\1.7.5\slf4j-api-1.7.5.jar;C:\Users\arbi01\.m2\repository\commons-cli\commons-cli\1.2\commons-cli-1.2.jar;C:\Users\arbi01\.m2\repository\org\eclipse\aether\aether-connector-wagon\0.9.0.M2\aether-connector-wagon-0.9.0.M2.jar;C:\Users\arbi01\.m2\repository\org\eclipse\aether\aether-api\0.9.0.M2\aether-api-0.9.0.M2.jar;C:\Users\arbi01\.m2\repository\org\eclipse\aether\aether-spi\0.9.0.M2\aether-spi-0.9.0.M2.jar;C:\Users\arbi01\.m2\repository\org\eclipse\aether\aether-util\0.9.0.M2\aether-util-0.9.0.M2.jar;C:\Users\arbi01\.m2\repository\org\apache\maven\wagon\wagon-provider-api\1.0\wagon-provider-api-1.0.jar;C:\Users\arbi01\.m2\repository\org\apache\maven\wagon\wagon-http-lightweight\2.5\wagon-http-lightweight-2.5.jar;C:\Users\arbi01\.m2\repository\org\apache\maven\wagon\wagon-http-shared\2.5\wagon-http-shared-2.5.jar;C:\Users\arbi01\.m2\repository\org\jsoup\jsoup\1.7.2\jsoup-1.7.2.jar;C:\Users\arbi01\.m2\repository\commons-lang\commons-lang\2.6\commons-lang-2.6.jar;C:\Users\arbi01\.m2\repository\commons-io\commons-io\2.2\commons-io-2.2.jar;C:\Users\arbi01\.m2\repository\ch\qos\logback\logback-classic\1.0.13\logback-classic-1.0.13.jar;C:\Users\arbi01\.m2\repository\ch\qos\logback\logback-core\1.0.13\logback-core-1.0.13.jar;C:\Program Files (x86)\JetBrains\IntelliJ IDEA 14.1.5\lib\idea_rt.jar" com.intellij.rt.execution.application.AppMain licenseUtil.LicenseUtil

:: echo %LicenseUtil%

cd %1

FOR /D %%A IN ("*") DO (
  echo %%A
  cd %%A
  git pull  
  java %LicenseUtil% --buildEffectivePom .
  cd ..
  java %LicenseUtil% --addPomToTsv %%A/effective-pom.xml licenses.stub.tsv
)

pause

java %LicenseUtil% --writeLicense3rdParty licenses.enhanced.tsv ALL

cd %OLDDIR%