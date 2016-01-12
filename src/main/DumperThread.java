package main;

import sun.net.www.http.HttpClient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by ShadowPhrogg32642342 on 2014.12.03..
 */
public class DumperThread extends Thread{
  private DumperFrame frame;
  private FileData data;

  public DumperThread(DumperFrame f, FileData d){
    this.frame = f;
    this.data = d;
  }
  public void run(){
    while(frame.dumpEnabled){
      int nextindex = data.getFirstUndumped();
      DumpFile de = data.getFile(nextindex);
      if(de != null){
        de.setStatus(dump(de, nextindex));
        data.fireTableDataChanged();
        try{
          Thread.sleep(frame.getDelay());
        }catch(InterruptedException e){
          frame.dumpEnabled = false;
          //interrupted
          return;
        }
      }
      else{
        frame.stopDump();
      }
    }
  }
  public int dump(DumpFile f, int i){
    try{
      URL u = frame.getBoardURL();
      String bnd = "--bump--\n";   //FIXME
      HttpURLConnection h = (HttpURLConnection) u.openConnection();
      h.setDoInput(true);
      h.setRequestMethod("POST");
      h.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + bnd);
      String contenttype = "image/invalid"; //FIXME
      String raw=bnd+"Content-Disposition: form-data; name=\"reply_numero\"\n\n"+frame.getThreadId()+"\n"+
              bnd+"Content-Disposition: form-data; name=\"reply_bokunonome\"\n\n"+frame.getUserName()+"\n"+
              bnd+"Content-Disposition: form-data; name=\"reply_elitterae\"\n\n"+frame.getMailAddress()+"\n"+
              bnd+"Content-Disposition: form-data; name=\"reply_talkingde\"\n\n"+frame.getSubject()+"\n"+
              bnd+"Content-Disposition: form-data; name=\"reply_chennodiscursus\"\n\n"+frame.getComment(i)+"\n"+
              bnd+"Content-Disposition: form-data; name=\"reply_nymphassword\"\n\n"+frame.getPassword()+"\n"+
              bnd+"Content-Disposition: form-data; name=\"reply_postas\"\n\nN\n"+ //post as normal user
              bnd+"Content-Disposition: form-data; name=\"reply_gattai\"\n\n"+!frame.getSpoiler()+"\n"+            //FIXME
              bnd+"Content-Disposition: form-data; name=\"reply_gattai_spoilered\"\n\n"+frame.getSpoiler()+"\n"+

              bnd+"Content-Disposition: form-data; name=\"recaptcha_challenge_field\"\n\n"+"oh damn"+"\n"+ //FIXME DUNNOHOW
              bnd+"Content-Disposition: form-data; name=\"recaptcha_response_field\"\n\n"+"oh damn"+"\n"+ //FIXME DUNNOHOW
              bnd+"Content-Disposition: form-data; name=\"reply_last_limit\"\n\nnull\n"+ //I don't know what that is, I've never seen that
              bnd+"Content-Disposition: form-data; name=\"latest_doc_id\"\n\nundefined\n"+ //noko target
              bnd+"Content-Disposition: form-data; name=\"theme\"\n\nfoolz/foolfuuka-theme-foolfuuka\n"+ //just pretend to be normal

              bnd+"Content-Disposition: form-data; name=\"csrf_token\"\n\ngoddamnit\n"+ //FIXME DUNNOHOW

              bnd+"Content-Disposition: form-data; name=\"file_image\"  filename=\""+data.getFile(i).getName()+"\nContent-Type:"+contenttype+"\n\n"+"image goes here";
      String outstring = URLEncoder.encode(raw, "UTF-8");
      h.setRequestProperty("POSTDATA", outstring);
      h.setRequestProperty("Content-Length", String.valueOf(outstring.length()));

      return 1;
    }
    catch(Exception e){
      return 2;
    }
  }
}
