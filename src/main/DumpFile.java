package main;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 * Created by ShadowPhrogg32642342 on 2014.11.22..
 */
public class DumpFile extends File implements Serializable {
    private transient int status;

    public DumpFile(String pathname) {
        super(pathname);
        status = 0;
    }
    public DumpFile(File f) throws IOException {
        super(f.getCanonicalPath());
        status = 0;
    }
    public void setStatus(int i){
        status = i;
    }
    public int getStatus(){
        return status;
    }
}
