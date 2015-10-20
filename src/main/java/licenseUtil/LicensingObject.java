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
import org.apache.maven.model.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by Arne Binder (arne.b.binder@gmail.com) on 10.09.2015.
 */
public class LicensingObject extends HashMap<String, String> {

    public enum ColumnHeader {
        ARTIFACT_ID("artefactId"),
        GROUP_ID("groupId"),
        VERSION("version"),
        BUNDLE("bundle"),
        LICENSE("license"),
        COPYRIGHT_INFORMATION("copyRightInformation"),
        LIBRARY_NAME("libraryName"),
        DO_NOT_DELETE("doNotDelete");

        private final String headerValue;

        ColumnHeader(String headerValue) {
            this.headerValue = headerValue;
        }

        public String value() {
            return this.headerValue;
        }
        public static ArrayList<String> headerValues(){
            ArrayList<String> result = new ArrayList<>();
            for(ColumnHeader header: ColumnHeader.class.getEnumConstants()){
                result.add(header.value());
            }
            return result;
        }
        @Override
        public String toString(){
            return this.headerValue;
        }
    }

    static final char textMarker = '"';
    private final static Set<String> MARKED_AS_TEXT = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(ColumnHeader.VERSION.value())));

    final Logger logger = LoggerFactory.getLogger(LicensingObject.class);
    static final HashSet<String> keyHeaders = new HashSet<>(Arrays.asList(ColumnHeader.ARTIFACT_ID.value(), ColumnHeader.GROUP_ID.value(), ColumnHeader.VERSION.value()));

    LicensingObject(Dependency dependency, String includingProject, String version) {
        super();
        put(ColumnHeader.ARTIFACT_ID.value(), dependency.getArtifactId());
        put(ColumnHeader.GROUP_ID.value(), dependency.getGroupId());
        put(ColumnHeader.VERSION.value(), dependency.getVersion());
        put(includingProject, version);
        clean();
    }

    LicensingObject(Plugin plugin, String includingProject, String version) {
        super();
        put(ColumnHeader.ARTIFACT_ID.value(), plugin.getArtifactId());
        put(ColumnHeader.GROUP_ID.value(), plugin.getGroupId());
        put(ColumnHeader.VERSION.value(), plugin.getVersion());
        put(includingProject, version);
        clean();
    }

    LicensingObject(CSVRecord record, String version) {
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
        if(containsKey(ColumnHeader.DO_NOT_DELETE.value())){
            for(String key: getNonFixedHeaders()){
                if(!Strings.isNullOrEmpty(get(key))){
                    put(key, version);
                }
            }
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

            String libString = get(LicensingObject.ColumnHeader.BUNDLE.value());
            if(libString==null){
                libString = "";
            }else{
                libString += " - ";
            }

            if(libString.trim().equals("") || !aggregateByBundle){
                libString += get(LicensingObject.ColumnHeader.ARTIFACT_ID.value());
                String version = get(LicensingObject.ColumnHeader.VERSION.value());
                if(version!=null){
                    if(version.startsWith("'"))
                        version = version.substring(1, version.length());
                    if(version.endsWith("'"))
                        version = version.substring(0, version.length()-1);
                    libString +=":"+version;
                }
            }
            if(containsKey(LicensingObject.ColumnHeader.COPYRIGHT_INFORMATION.value())){

                libString += ", Copyright "+get(LicensingObject.ColumnHeader.COPYRIGHT_INFORMATION.value());
            }
            
            return libString;
        }else{
            return null;
        }


    }

    @Override
    public boolean equals(Object aThat) {
        if (this == aThat) return true;
        if (!(aThat instanceof LicensingObject)) return false;
        LicensingObject that = (LicensingObject) aThat;
        boolean result = false;
        /*if((this.get(ColumnHeader.ARTIFACT_ID.value()) != null) && (this.get(ColumnHeader.ARTIFACT_ID.value()).equals("common"))
                //&&(this.get(ColumnHeader.GROUP_ID.value()) == null) && (this.get(ColumnHeader.GROUP_ID.value()).equals("eu.freme"))
                //&&(that.get(ColumnHeader.ARTIFACT_ID.value()) != null) && (that.get(ColumnHeader.ARTIFACT_ID.value()).equals("common"))
                &&(that.get(ColumnHeader.GROUP_ID.value()) != null) && (that.get(ColumnHeader.GROUP_ID.value()).equals("eu.freme"))
                ){
            System.out.println();
        }*/

        result = ((this.get(ColumnHeader.ARTIFACT_ID.value()) == null || that.get(ColumnHeader.ARTIFACT_ID.value()) == null || this.get(ColumnHeader.ARTIFACT_ID.value()).equals(that.get(ColumnHeader.ARTIFACT_ID.value()))) &&
                (this.get(ColumnHeader.GROUP_ID.value()) == null || that.get(ColumnHeader.GROUP_ID.value()) == null || this.get(ColumnHeader.GROUP_ID.value()).equals(that.get(ColumnHeader.GROUP_ID.value()))) &&
                (this.get(ColumnHeader.VERSION.value()) == null || that.get(ColumnHeader.VERSION.value()) == null || this.get(ColumnHeader.VERSION.value()).equals(that.get(ColumnHeader.VERSION.value()))));
        return result;
    }

    public int hashCode() {
        return get(ColumnHeader.ARTIFACT_ID.value()).hashCode() *
                get(ColumnHeader.GROUP_ID.value()).hashCode() *
                get(ColumnHeader.VERSION.value()).hashCode();
    }

    public void update(LicensingObject licensingObject) {
        for (String key : keySet()) {
            put(key, updateElement(get(key), licensingObject.get(key), !keyHeaders.contains(key)));
            get(key);
        }
        for (String key : licensingObject.keySet()) {
            if (!keySet().contains(key)) {
                put(key, licensingObject.get(key));
            }
        }
    }

    public HashSet<String> getNonFixedHeaders() {
        HashSet<String> result = new HashSet<>();
        result.addAll(keySet());
        result.removeAll(new HashSet<>(ColumnHeader.headerValues()));
        return result;
    }

    private <T> T updateElement(T origElemt, T updateElement, Boolean override) {
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
}
