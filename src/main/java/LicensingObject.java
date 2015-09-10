import org.apache.commons.csv.CSVRecord;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import Utils.ColumnHeader;

import java.util.*;

/**
 * Created by Arne Binder (arne.b.binder@gmail.com) on 10.09.2015.
 */
public class LicensingObject extends HashMap<String, String> {



    static final HashSet<String> keyHeaders = new HashSet<>(Arrays.asList(ColumnHeader.ARTIFACT_ID.value(), ColumnHeader.GROUP_ID.value(), ColumnHeader.VERSION.value()));



    LicensingObject(Dependency dependency, String includingProject) {
        super();
        put(ColumnHeader.ARTIFACT_ID.value(), dependency.getArtifactId());
        put(ColumnHeader.GROUP_ID.value(), dependency.getGroupId());
        put(ColumnHeader.VERSION.value(), dependency.getVersion());
        put(includingProject, "x");
        clean();
    }

    LicensingObject(Plugin plugin, String includingProject) {
        super();
        put(ColumnHeader.ARTIFACT_ID.value(), plugin.getArtifactId());
        put(ColumnHeader.GROUP_ID.value(), plugin.getGroupId());
        put(ColumnHeader.VERSION.value(), plugin.getVersion());
        put(includingProject, "x");
        clean();
    }

    LicensingObject(CSVRecord record) {
        super();
        Map<String, String> recordMap = record.toMap();
        for (String key : recordMap.keySet()) {
            String value = recordMap.get(key);
            if (value != null && !value.equals(""))
                put(key, recordMap.get(key));
        }
    }

    public ArrayList<String> getRecord(ArrayList<String> headers) {
        ArrayList<String> result = new ArrayList<>();
        for (String key : headers) {
            if (get(key) != null)
                result.add(get(key));
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


        return ((this.get(ColumnHeader.ARTIFACT_ID.value()) == null || that.get(ColumnHeader.ARTIFACT_ID.value()) == null || this.get(ColumnHeader.ARTIFACT_ID.value()).equals(that.get(ColumnHeader.ARTIFACT_ID.value()))) &&
                (this.get(ColumnHeader.GROUP_ID.value()) == null || that.get(ColumnHeader.GROUP_ID.value()) == null || this.get(ColumnHeader.GROUP_ID.value()).equals(that.get(ColumnHeader.GROUP_ID.value()))) &&
                (this.get(ColumnHeader.VERSION.value()) == null || that.get(ColumnHeader.VERSION.value()) == null || this.get(ColumnHeader.VERSION.value()).equals(that.get(ColumnHeader.VERSION.value()))));
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

        /*@Override
        public String toString() {
            return get("groupId")+":"+get("artefactId")+":"+get("version")+"#"+get("libraryName")+"#"+licenseName+"#"+copyRightInformation+"#"+includingProjects.toString();
        }*/
}
