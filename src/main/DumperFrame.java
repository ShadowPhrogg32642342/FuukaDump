package main;

import java.awt.event.*;
import java.io.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.awt.*;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableCellRenderer;


/**
 * Created by SPh on 2014.11.13..
 */
public class DumperFrame extends JFrame implements ActionListener{
  private JPanel rootPanel;

  private JComboBox modeComboBox;
  private JTextField textFieldThreadId;
  private JTextField textFieldBoardURL;
  private JTextField textFieldBoardName;
  private JTextField textFieldSub;
  private JTextField textFieldName;
  private JPasswordField passwordField;
  private JTextField textFieldMail;
  private JCheckBox spoilerCheckBox;
  private JSpinner spinnerDelay;

  private JTextArea textAreaComment;

  private JLabel labelPreview;

  private JTable tableFiles;

  private JButton buttonAdd;
  private JButton buttonLoad;
  private JButton buttonSave;
  private JButton buttonRemoveSelected;
  private JButton buttonRemoveAll;
  private JButton buttonStartStop;

  private FileData data;
  private DumperThread dumper;
  public  boolean dumpEnabled;

  public DumperFrame(FileData f){
    this.addWindowListener(new WindowAdapter(){
      @Override
      public void windowClosing(WindowEvent e){
        wserialize();
      }

      @Override
      public void windowOpened(WindowEvent e){
        wdeserialize();
      }
    });
    this.setTitle("FókaDömper");
    data = f;
    buttonAdd.addActionListener(this);
    buttonAdd.setActionCommand("add");
    buttonLoad.addActionListener(this);
    buttonLoad.setActionCommand("load");
    buttonSave.addActionListener(this);
    buttonSave.setActionCommand("save");
    buttonRemoveAll.addActionListener(this);
    buttonRemoveAll.setActionCommand("purge");
    buttonRemoveSelected.addActionListener(this);
    buttonRemoveSelected.setActionCommand("delsel");
    buttonStartStop.addActionListener(this);
    buttonStartStop.setActionCommand("start");
    tableFiles.getSelectionModel().addListSelectionListener(e -> updatePreview(e));
    tableFiles.setDefaultRenderer(String.class, this.new FileTableCellRenderer(tableFiles.getDefaultRenderer(String.class)));
    tableFiles.setModel(this.data);
    tableFiles.setFillsViewportHeight(true);
    tableFiles.getTableHeader().setReorderingAllowed(false);
    modeComboBox.addItem("Dump");
    modeComboBox.addItem("SPAM");
    modeComboBox.addItemListener(e -> modeToggle(e));
    setContentPane(rootPanel);
    pack();
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setVisible(true);
  }

  //GETTERS FOR DUMPER THREAD
  public int getDelay(){
    return (Integer) spinnerDelay.getValue()*1000;
  }
  public String getComment(int i){
    return replaceComment(textAreaComment.getText(), i);
  }
  public int getThreadId(){
    return new Integer(textFieldThreadId.getText());
  }
  public URL getBoardURL(){
    String base = textFieldBoardURL.getText();
    if(!base.endsWith("/"))
      base = base+"/";
    String board = textFieldBoardName.getText();
    if(board.startsWith("/"))
      board = board.substring(1, board.length());
    if(board.endsWith("/"))
      board = board.substring(0, board.length()-1);
    try{
      return new URL(base+board+"/submit/");
    }catch(MalformedURLException e){
      System.out.println(base+board+"/submit/");
      return null;
    }
  }
  public String getSubject(){
    return textFieldSub.getText();
  }
  public String getUserName(){
    return textFieldName.getText();
  }
  public String getMailAddress(){
    return textFieldMail.getText();
  }
  public boolean getSpoiler(){
    return spoilerCheckBox.isSelected();
  }
  public String getPassword() {return passwordField.getPassword().toString();}

  //JUnithoz szükséges, máshoz szükségtelen //FIXME
  public void setComment(String comment){
    this.textAreaComment.setText(comment);
  }
  public void setUrl(String url, String board){
    this.textFieldBoardURL.setText(url);
    this.textFieldBoardName.setText(board);
  }

  //MEMBER CLASSES
  public class FileTableCellRenderer implements TableCellRenderer{
    private TableCellRenderer renderer;

    public FileTableCellRenderer(TableCellRenderer defRenderer){
      this.renderer = defRenderer;
    }
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean hasFocus, int row, int col){
      Component component = renderer.getTableCellRendererComponent(table, value, selected, hasFocus, row, col);
      if(selected){
        component.setBackground(new Color(0, 128, 255));
        return component;
      }
      switch(data.getFile(table.convertRowIndexToModel(row)).getStatus()){
        case 0: //not yet processed
          component.setBackground(new Color(238, 238, 238));
          break;
        case 1: //dumped successfully
          component.setBackground(new Color(128, 255, 128));
          break;
        case 2: //excrement made contact with the aerator
          component.setBackground(new Color(255, 128, 128));
          break;
      }
      return component;
    }
  }

//FUNCTIONS OF THE IMPLEMENTED ActionListener INTERFACE
  @Override
  public void actionPerformed(ActionEvent e){
    switch(e.getActionCommand()){
      case "add":
        addData();
        break;
      case "load":
        deserializeData();
        break;
      case "save":
        serializeData();
        break;
      case "delsel":
        data.rmFiles(tableFiles.getSelectedRows());
        break;
      case "purge":
        data.purge();
        break;
      case "start":
        startDump();
        break;
      case "stop":
        stopDump();
        break;
      default:
        break;
    }
  }

//DUMP THREAD&GUI FUNCTIONS
  private void startDump(){
    if(!dumpEnabled){
      dumpEnabled = true;
      dumper = new DumperThread(this, data);
      dumper.start();
      buttonStartStop.setIcon(new ImageIcon("res/stop.gif"));
      buttonStartStop.setText("Stop!");
      buttonStartStop.setActionCommand("stop");
      buttonStartStop.setToolTipText("STOP WHAT YOU'RE DOING AND ASSUME THE PARTY POSITION");
    }
  }
  public void stopDump(){
    dumpEnabled = false;
    dumper.interrupt(); //this kills the dump
    buttonStartStop.setIcon(new ImageIcon("res/start.gif"));
    buttonStartStop.setText("Start!");
    buttonStartStop.setActionCommand("start");
    buttonStartStop.setToolTipText("Start dumping NOW!");
  }

//MODEL MODIFYING FUNCTIONS
  private void addData(){
    JFileChooser fileChooserAdd = new JFileChooser();
    fileChooserAdd.setFileFilter(new FileNameExtensionFilter("Accepted types (.jpg, .gif, .png, .webm...)", "jpg", "jpeg", "jpe", "gif", "png", "webm", "apng"));
    fileChooserAdd.setAcceptAllFileFilterUsed(false);
    fileChooserAdd.setMultiSelectionEnabled(true);
    fileChooserAdd.showOpenDialog(DumperFrame.this);
    File[] files = fileChooserAdd.getSelectedFiles();
    for(int i = 0; i < files.length; i++)
      data.addFile(files[i]);
  }
  private void serializeData(){
    int[] sel = tableFiles.getSelectedRows();
    if(sel.length > 0){
      JFileChooser fileChooserSave = new JFileChooser();
      fileChooserSave.setAcceptAllFileFilterUsed(false);
      fileChooserSave.setFileFilter(new FileNameExtensionFilter("Serialized JAVA Object (.ser)", "ser"));
      fileChooserSave.setSelectedFile(new File("dump.ser"));
      fileChooserSave.showSaveDialog(fileChooserSave);
      try{
        FileOutputStream fos = new FileOutputStream(fileChooserSave.getSelectedFile());
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(data.getList(sel));
        fos.close();
      }catch(Exception ex){
        ex.printStackTrace();
      }
    }
  }
  private void deserializeData(){
    JFileChooser fileChooserLoad = new JFileChooser();
    fileChooserLoad.setAcceptAllFileFilterUsed(false);
    fileChooserLoad.setMultiSelectionEnabled(true);
    fileChooserLoad.setFileFilter(new FileNameExtensionFilter("Serialized JAVA Object (.ser)", "ser"));
    fileChooserLoad.showOpenDialog(DumperFrame.this);
    File[] files = fileChooserLoad.getSelectedFiles();
    for(int i = 0; i < files.length; i++){
      try{
        FileInputStream fis = new FileInputStream(files[i]);
        ObjectInputStream ois = new ObjectInputStream(fis);
        List<DumpFile> dfl = Collections.synchronizedList((List<DumpFile>) ois.readObject());
        for(int j = 0; j < dfl.size(); j++){
          data.addFile(dfl.get(j));
        }
      }catch(Exception e){
        e.printStackTrace();
      }
    }
  }

//GUI FUNCTIONS
  private void updatePreview(ListSelectionEvent e){
    ListSelectionModel lsm = (ListSelectionModel) e.getSource();
    List<Integer> selected = new ArrayList<Integer>();
    if(lsm.isSelectionEmpty()){
      labelPreview.setIcon(null);
      labelPreview.setText("No image selected");
    }else{
      for(int i = lsm.getMinSelectionIndex(); i <= lsm.getMaxSelectionIndex(); i++)
        if(lsm.isSelectedIndex(i))
          selected.add(i);
      if(selected.size() != 1){
        labelPreview.setIcon(null);
        labelPreview.setText("Multiple images selected");
      }else{
        try{
          int wo, ho, wr, hr;
          BufferedImage im = ImageIO.read(data.getFile(selected.get(0)));
          wo = im.getWidth();
          ho = im.getHeight();
          wr = wo > ho ? 400 : ((int) ((double) wo / ho * 400));
          hr = wo > ho ? ((int) ((double) ho / wo * 400)) : 400;
          BufferedImage re = new BufferedImage(wr, hr, im.getType());
          Graphics2D graph2d = (Graphics2D) re.getGraphics();
          graph2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
          graph2d.drawImage(im, 0, 0, wr, hr, null);
          graph2d.dispose();
          labelPreview.setIcon(new ImageIcon(re));
          labelPreview.setText("");
        }catch(Exception ex){
          labelPreview.setIcon(null);
          labelPreview.setText("Error opening image");
        }
      }
    }
  }
  public void modeToggle(ItemEvent e){
    switch((String) e.getItem()){
      default:
        textFieldThreadId.setEnabled(true);
        textFieldThreadId.setToolTipText("ID of the thread you want to dump into.");
        break;
      case "SPAM":
        textFieldThreadId.setEnabled(false);
        textFieldThreadId.setToolTipText("EVERY PICTURE WILL BE DUMPED IN NEW THREADS, YOU CAN EASILY GET BANNED FOR THIS!");
        break;
    }
  }
  private void wdeserialize(){
    File f = new File("config.ser");
    try{
      FileInputStream fis = new FileInputStream(f);
      ObjectInputStream ois = new ObjectInputStream(fis);
      textAreaComment.setText((String) ois.readObject());
      modeComboBox.setSelectedIndex((int) ois.readObject());
      textFieldThreadId.setText((String) ois.readObject());
      textFieldBoardURL.setText((String) ois.readObject());
      textFieldBoardName.setText((String) ois.readObject());
      textFieldSub.setText((String) ois.readObject());
      textFieldName.setText((String) ois.readObject());
      textFieldMail.setText((String) ois.readObject());
      spoilerCheckBox.setSelected((boolean) ois.readObject());
      spinnerDelay.setValue(ois.readObject());

    }catch(IOException|ClassNotFoundException e){
      //no valid configfile found
    }
  }
  private void wserialize(){
    File f = new File("config.ser");
    try{
      if(!f.exists())
        f.createNewFile();
      FileOutputStream fos = new FileOutputStream(f);
      ObjectOutputStream oos = new ObjectOutputStream(fos);
      oos.writeObject(textAreaComment.getText());
      oos.writeObject(modeComboBox.getSelectedIndex());
      oos.writeObject(textFieldThreadId.getText());
      oos.writeObject(textFieldBoardURL.getText());
      oos.writeObject(textFieldBoardName.getText());
      oos.writeObject(textFieldSub.getText());
      oos.writeObject(textFieldName.getText());
      oos.writeObject(textFieldMail.getText());
      oos.writeObject(spoilerCheckBox.isSelected());
      oos.writeObject(spinnerDelay.getValue());
    }catch(IOException e){
      //cannot save configuration
    }
  }
//MISC FUNCTIONS
  private String replaceComment(String text, int i){
    if(text.contains("%t"))                                                                                                 //FIXME TESTINGOLY
      text = text.replace("%t", Integer.toString(data.getRowCount()));                                                      //total
    if(text.contains("%c"))
      text = text.replace("%c", Integer.toString(i));                                                                       //count
    if(text.contains("%n") && data.getRowCount() != 0)
      text = text.replace("%n", data.getFile(i).getName());                                                                 //name
    if(text.contains("%s") && data.getRowCount() != 0)
      text = text.replace("%s", Integer.toString((int) data.getFile(i).length()));                                          //size
    if(text.contains("%h") && data.getRowCount() != 0)
      text = text.replace("%h", humanReadable(data.getFile(i).length()));                                                   //hsize
    if(text.contains("%r") && data.getRowCount() != 0)
      try{
        text = text.replace("%r", ImageIO.read(data.getFile(i)).getHeight()+"x"+ImageIO.read(data.getFile(i)).getWidth());  //res
      }catch(IOException e){text = "ExE";}
    if(text.contains("%p") && data.getRowCount() != 0)
    text = text.replace("%p", i*1.00/data.getRowCount()*100+"%");                                                         //percent
    return text;
  }
  public static String humanReadable(long bytes) { //all rights reserved: BalusC of StackOverflow
    if (bytes < 1024) return bytes + " B";         //http://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
    int exp = (int) (Math.log(bytes) / Math.log(1024));
    String pre = "KMGTPE".charAt(exp-1)+"i";
    return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
  }


  public static void main(String[] args){
    FileData f = new FileData();
    DumperFrame df = new DumperFrame(f);
    df.setContentPane(df.rootPanel);
    df.pack();
    df.setVisible(true);
  }
}
