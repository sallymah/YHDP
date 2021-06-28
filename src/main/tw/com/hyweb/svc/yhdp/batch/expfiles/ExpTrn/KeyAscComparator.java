package tw.com.hyweb.svc.yhdp.batch.expfiles.ExpTrn;

import java.util.Comparator;

import tw.com.hyweb.core.cp.batch.framework.expfiles.ExpFileInfo;

class KeyAscComparator implements Comparator {
    public int compare(Object o1, Object o2) {
        String lkey = ((ExpFileInfo) o1).getKey();
        String rkey = ((ExpFileInfo) o2).getKey();
        return lkey.compareTo(rkey);
    }
}
