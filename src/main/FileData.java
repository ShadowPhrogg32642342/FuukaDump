package main;

import javax.swing.table.AbstractTableModel;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileData extends AbstractTableModel{
  private List<DumpFile> filelist;

  public FileData(){
    filelist = Collections.synchronizedList(new ArrayList<DumpFile>());
  }

  //TABLE FUNCTIONS
  @Override
  public int getRowCount(){
    return filelist.size();
  }

  @Override
  public int getColumnCount(){
    return 1;
  }

  public Class getColumnClass(int columnIndex){
    return String.class;
  }

  public String getColumnName(int colIndex){
    switch(colIndex){
      case 0:
        return "Filename";
      default:
        return "";
    }
  }

  public boolean isCellEditable(int rowIndex, int columnIndex){
    return false;
  }

  @Override
  public Object getValueAt(int rowIndex, int colIndex){
    switch(colIndex){
      case 0:
        return filelist.get(rowIndex).getName();
      default:
        return "Dear Sir, there is no problem...";
    }
  }

  //MODEL FUNCTIONS
  public void addFile(File f){
    try{
      filelist.add(new DumpFile(f.getCanonicalPath()));
      this.fireTableDataChanged();
    }catch(IOException e){
      e.printStackTrace();
    }
  }

  public DumpFile getFile(int i){
    return filelist.get(i);
  }

  public void rmFiles(int[] rem){
    for(int i = rem.length - 1; i > -1; i--){
      filelist.remove(rem[i]);
    }
    this.fireTableDataChanged();
  }

  public void purge(){
    filelist.clear();
    this.fireTableDataChanged();
  }

  public List<DumpFile> getList(int[] s){
    List<DumpFile> outfile = new ArrayList<DumpFile>();
    for(int i = 0; i < s.length; i++){
      DumpFile df = filelist.get(s[i]);
      outfile.add(df);
    }
    return outfile;
  }

  public int getFirstUndumped(){
    for(int i = 0; i < filelist.size(); i++){
      if(filelist.get(i).getStatus() == 0)
        return i;
    }
    return -1;
  }
}