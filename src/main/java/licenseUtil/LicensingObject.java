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

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVRecord;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.License;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Arne Binder (arne.b.binder@gmail.com) on 10.09.2015.
 */
public class LicensingObject extends HashMap<String, String> {

    public enum ColumnHeader {
        GROUP_ID("groupId"),
        ARTIFACT_ID("artifactId"),
        VERSION("version"),
        LICENSE_TEMPLATE("licenseTemplate"),
        LICENSE_NAMES("licenseNames"),
        LICENSE_URLS("licenseUrls"),
        LICENSE_COMMENTS("licenseComments"),
        COPYRIGHT_INFORMATION("copyRightInformation"),
        BUNDLE("bundle"),
        LIBRARY_NAME("libraryName");

        private final String headerValue;

        ColumnHeader(String headerValue) {
            this.headerValue = headerValue;
        }

        public String value() {
            return this.headerValue;
        }

        public static final List<String> HEADER_VALUES = Arrays.stream(ColumnHeader.class.getEnumConstants()).map(ColumnHeader::value).collect(Collectors.toList());

        @Override
        public String toString(){
            return this.headerValue;
        }
    }

    static final char textMarker = '"';
    static final char multiEntriesSeparator = '|';
    private final static Set<String> MARKED_AS_TEXT = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(ColumnHeader.VERSION.value())));

    final Logger logger = LoggerFactory.getLogger(LicensingObject.class);
    static final HashSet<String> KEY_HEADERS = new HashSet<>(Arrays.asList(ColumnHeader.ARTIFACT_ID.value(), ColumnHeader.GROUP_ID.value(), ColumnHeader.VERSION.value()));

    LicensingObject(MavenProject project, String includingProject, String version, Map<String, String> licenseUrlFileMappings) {
        super();
        put(ColumnHeader.ARTIFACT_ID.value(), project.getArtifactId());
        put(ColumnHeader.GROUP_ID.value(), project.getGroupId());
        put(ColumnHeader.VERSION.value(), project.getVersion());

        if(project.getLicenses()!=null && !project.getLicenses().isEmpty()){
            String licenseNames = "";
            String licenseUrls = "";
            String licenseComments = "";
            String licenseFN = null;
            int i = 0;
            for(License license:project.getLicenses()) {
                if(license.getUrl()!=null && licenseFN == null)
                    // remove protocol and lookup license url
                    licenseFN = Utils.getValue(licenseUrlFileMappings, license.getUrl().replaceFirst("^[^:]+://", ""));
                if (i++ > 0) {
                    licenseNames += multiEntriesSeparator;
                    licenseUrls += multiEntriesSeparator;
                    licenseComments += multiEntriesSeparator;
                }
                licenseNames += license.getName();
                if (!Strings.isNullOrEmpty(license.getUrl()))
                    licenseUrls += license.getUrl();
                if (!Strings.isNullOrEmpty(license.getComments()))
                    licenseComments += license.getComments();
            }
            if(!Strings.isNullOrEmpty(licenseFN))
                put(ColumnHeader.LICENSE_TEMPLATE.value(), licenseFN);
            if(!Strings.isNullOrEmpty(licenseNames))
                put(ColumnHeader.LICENSE_NAMES.value(), licenseNames);
            if(!Strings.isNullOrEmpty(licenseUrls))
                put(ColumnHeader.LICENSE_URLS.value(), licenseUrls);
            if(!Strings.isNullOrEmpty(licenseComments))
                put(ColumnHeader.LICENSE_COMMENTS.value(), licenseComments);
        }
        put(includingProject, version);
        //clean();
    }

    LicensingObject(Dependency dependency, String includingProject, String version) {
        super();
        put(ColumnHeader.ARTIFACT_ID.value(), dependency.getArtifactId());
        put(ColumnHeader.GROUP_ID.value(), dependency.getGroupId());
        put(ColumnHeader.VERSION.value(), dependency.getVersion());
        put(includingProject, version);
        //clean();
    }

    LicensingObject(Plugin plugin, String includingProject, String version) {
        super();
        put(ColumnHeader.ARTIFACT_ID.value(), plugin.getArtifactId());
        put(ColumnHeader.GROUP_ID.value(), plugin.getGroupId());
        put(ColumnHeader.VERSION.value(), plugin.getVersion());
        put(includingProject, version);
        //clean();
    }

    LicensingObject(CSVRecord record) throws IncompleteLicenseObjectException {
        super();
        Map<String, String> recordMap = record.toMap();
        for (String key : recordMap.keySet()) {
            String value = recordMap.get(key);
            if (value != null && !value.equals("")) {
                String current = recordMap.get(key).trim();
                // remove text marker
                if (value.length() > 1 && value.charAt(0) == textMarker && value.charAt(value.length() - 1) == textMarker) {
                    put(key, current.substring(1,current.length()-1));
                } else
                    put(key, current);
            }
        }

        //check key header values
        for(String keyHeader: KEY_HEADERS){
            if(Strings.isNullOrEmpty(get(keyHeader)))
                throw new IncompleteLicenseObjectException("Missing value: \""+keyHeader+"\" in \""+this+"\"");
        }
    }

    public ArrayList<String> getRecord(ArrayList<String> headers) {
        ArrayList<String> result = new ArrayList<>();
        for (String key : headers) {
            String value = get(key);
            if (value != null)
                if (MARKED_AS_TEXT.contains(key)){
                    result.add(textMarker + value + textMarker);
                }else
                    result.add(value);
            else {
                result.add("");
            }
        }
        return result;
    }

    public String getStringForModule(String moduleName, Boolean aggregateByBundle){
        if(containsKey(moduleName)){

            String libString = get(ColumnHeader.BUNDLE.value());
            if(libString==null){
                String libName = get(ColumnHeader.LIBRARY_NAME.value());
                if(libName!=null)
                    libString = libName;
                else
                    libString = "";
            }else{
                String libName = get(ColumnHeader.LIBRARY_NAME.value());
                if(libName!=null)
                    libString += ": "+libName;
                //libString += " - ";
            }

            if(!libString.equals(""))
                libString += " - ";

            if(libString.trim().equals("") || !aggregateByBundle){
                libString += get(ColumnHeader.GROUP_ID.value());
                libString += ":";
                libString += get(ColumnHeader.ARTIFACT_ID.value());
                String version = get(ColumnHeader.VERSION.value());
                if(version!=null){
                    if(version.startsWith("'"))
                        version = version.substring(1, version.length());
                    if(version.endsWith("'"))
                        version = version.substring(0, version.length()-1);
                    libString +=":"+version;
                }
            }
            if(containsKey(ColumnHeader.COPYRIGHT_INFORMATION.value())) {

                libString += ", Copyright "+get(ColumnHeader.COPYRIGHT_INFORMATION.value());
            }

            return libString;
        }else{
            return null;
        }
    }

    public boolean purgedEmpty(String version){
        Boolean result = true;
        for(String key: getNonFixedHeaders()){
            String value = get(key).toUpperCase();
            if(value.equals(version) || value.equals(LicensingList.forceAddingLibraryKeyword)){
                result = false;
            }else{
                this.remove(key);
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object aThat) {
        if (this == aThat) return true;
        if (!(aThat instanceof LicensingObject)) return false;
        LicensingObject that = (LicensingObject) aThat;

        for(String keyHeader: KEY_HEADERS){
            if(this.get(keyHeader)==null || that.get(keyHeader)==null || !this.get(keyHeader).equals(that.get(keyHeader)))
                return false;
        }
        return true;
    }

    public int hashCode() {
        int result = 1;
        for(String keyHeader: KEY_HEADERS) {
            result *= get(keyHeader).hashCode();
        }
        return result;
    }

    public void update(LicensingObject licensingObject) {
        for (String key : licensingObject.keySet()) {
            String thisValue = get(key);
            String thatValue = licensingObject.get(key);

            // replace empty multi entry values like "||" with null
            // and update, if possible
            put(key, updateElement(thisValue == null || thisValue.matches("\\"+ multiEntriesSeparator +"*")?null:thisValue, thatValue == null || thatValue.matches("\\"+ multiEntriesSeparator +"*")?null:thatValue, !KEY_HEADERS.contains(key)));
        }
    }

    public HashSet<String> getNonFixedHeaders() {
        HashSet<String> result = new HashSet<>();
        result.addAll(keySet());
        result.removeAll(ColumnHeader.HEADER_VALUES);
        return result;
    }

    private static <T> T updateElement(T origElemt, T updateElement, Boolean override) {
        if ((origElemt == null || override) && updateElement != null)
            return updateElement;
        else
            return origElemt;
    }

    public void clean() {
        for (String key : keySet()) {
            if (get(key) == null || get(key).equals(""))
                remove(key);
        }
    }

    @Override
    public String toString() {
        return get(ColumnHeader.GROUP_ID.value())+":"+get(ColumnHeader.ARTIFACT_ID.value())+":"+get(ColumnHeader.VERSION.value());
    }
}
