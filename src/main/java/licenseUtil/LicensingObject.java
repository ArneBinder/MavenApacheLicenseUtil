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

    static final char textMarker = '"';
    private final static Set<String> MARKED_AS_TEXT = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(Utils.ColumnHeader.VERSION.value())));

    final Logger logger = LoggerFactory.getLogger(LicensingObject.class);
    static final HashSet<String> keyHeaders = new HashSet<String>(Arrays.asList(Utils.ColumnHeader.ARTIFACT_ID.value(), Utils.ColumnHeader.GROUP_ID.value(), Utils.ColumnHeader.VERSION.value()));

    LicensingObject(Dependency dependency, String includingProject, String version) {
        super();
        put(Utils.ColumnHeader.ARTIFACT_ID.value(), dependency.getArtifactId());
        put(Utils.ColumnHeader.GROUP_ID.value(), dependency.getGroupId());
        put(Utils.ColumnHeader.VERSION.value(), dependency.getVersion());
        put(includingProject, version);
        clean();
    }

    LicensingObject(Plugin plugin, String includingProject, String version) {
        super();
        put(Utils.ColumnHeader.ARTIFACT_ID.value(), plugin.getArtifactId());
        put(Utils.ColumnHeader.GROUP_ID.value(), plugin.getGroupId());
        put(Utils.ColumnHeader.VERSION.value(), plugin.getVersion());
        put(includingProject, version);
        clean();
    }

    LicensingObject(CSVRecord record) {
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
    }

    public ArrayList<String> getRecord(ArrayList<String> headers) {
        ArrayList<String> result = new ArrayList<String>();
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

    @Override
    public boolean equals(Object aThat) {
        if (this == aThat) return true;
        if (!(aThat instanceof LicensingObject)) return false;
        LicensingObject that = (LicensingObject) aThat;
        boolean result = false;
        /*if((this.get(Utils.ColumnHeader.ARTIFACT_ID.value()) != null) && (this.get(Utils.ColumnHeader.ARTIFACT_ID.value()).equals("common"))
                //&&(this.get(Utils.ColumnHeader.GROUP_ID.value()) == null) && (this.get(Utils.ColumnHeader.GROUP_ID.value()).equals("eu.freme"))
                //&&(that.get(Utils.ColumnHeader.ARTIFACT_ID.value()) != null) && (that.get(Utils.ColumnHeader.ARTIFACT_ID.value()).equals("common"))
                &&(that.get(Utils.ColumnHeader.GROUP_ID.value()) != null) && (that.get(Utils.ColumnHeader.GROUP_ID.value()).equals("eu.freme"))
                ){
            System.out.println();
        }*/

        result = ((this.get(Utils.ColumnHeader.ARTIFACT_ID.value()) == null || that.get(Utils.ColumnHeader.ARTIFACT_ID.value()) == null || this.get(Utils.ColumnHeader.ARTIFACT_ID.value()).equals(that.get(Utils.ColumnHeader.ARTIFACT_ID.value()))) &&
                (this.get(Utils.ColumnHeader.GROUP_ID.value()) == null || that.get(Utils.ColumnHeader.GROUP_ID.value()) == null || this.get(Utils.ColumnHeader.GROUP_ID.value()).equals(that.get(Utils.ColumnHeader.GROUP_ID.value()))) &&
                (this.get(Utils.ColumnHeader.VERSION.value()) == null || that.get(Utils.ColumnHeader.VERSION.value()) == null || this.get(Utils.ColumnHeader.VERSION.value()).equals(that.get(Utils.ColumnHeader.VERSION.value()))));
        return result;
    }

    public int hashCode() {
        return get(Utils.ColumnHeader.ARTIFACT_ID.value()).hashCode() *
                get(Utils.ColumnHeader.GROUP_ID.value()).hashCode() *
                get(Utils.ColumnHeader.VERSION.value()).hashCode();
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
        HashSet<String> result = new HashSet<String>();
        result.addAll(keySet());
        result.removeAll(new HashSet<String>(Utils.ColumnHeader.headerValues()));
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
