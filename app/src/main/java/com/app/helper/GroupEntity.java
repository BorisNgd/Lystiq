package com.app.helper;

import java.util.ArrayList;
import java.util.List;

public class GroupEntity {
    public String Name;
    public String catid;
    public int imgid;

    public List<GroupItemEntity> GroupItemCollection;

    public GroupEntity() {
        GroupItemCollection = new ArrayList<GroupItemEntity>();
    }

    public class GroupItemEntity {
        public String Name;
        public int img;
        public String subcatid;
        public String catid;
    }
}
