package com.cisco.cmxmobile.model;

import java.util.Collection;
import java.util.HashMap;

import com.cisco.cmxmobile.cacheService.client.annotations.Key;

/**
 * The <class>AssociationMap</class> will contain Mappings between entities.
 * 
 * @author amhakoo
 */
public class AssociationMap {
    @Key(index = 1)
    private String id;

    private HashMap<String, String> pair;

    public static final String ID = "id";

    public AssociationMap() {
        //Empty constructor
    }

    public AssociationMap(String id) {
        this.id = id;

        pair = new HashMap<String, String>();
    }

    public String getId() {
        return id;
    }

    public void addAssociation(String entity1, String entity2) {
        pair.put(entity1, entity2);
    }

    public void removeAssociation(String entity1) {
        pair.remove(entity1);
    }

    public String getAssociatedEntity(String entity1) {
        return pair.get(entity1);
    }
    
    public Collection<String> values()
    {
        return pair.values();
    }

    public int getSize() {
        return pair.size();
    }
}
