package test;

import main.DumperFrame;
import main.FileData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;

import static org.junit.Assert.*;

public class DumperFrameTest{
  DumperFrame df;

  @Before
  public void setUp() throws Exception{
    FileData f = new FileData();
    df = new DumperFrame(f);
    f.addFile(new File("abc.def"));
  }

  @After
  public void tearDown() throws Exception{

  }

  @Test
  public void testGetComment() throws Exception{
    String output;                   //%t(otal) %c(urrent) %n(ame) %s(ize) %h(uman readable size) %r(esolution) %p(ercent, t/c*100)
    df.setComment("");
    output = df.getComment(0);
      assertEquals("", output);
    df.setComment("%t");
      assertEquals("1", output);
      assertNotEquals("%t", output);
  }

  @Test
  public void testGetBoardURL() throws Exception{
    String u = "boards.example.com/";
    String b = "/b/";
    df.setUrl(u, b);
    URL output = df.getBoardURL();
    assertEquals(new URL ("http://boards.example.com/b/submit/"), output);
    assertEquals(new URL ("boards.example.com/b/submit/"), output);
  }

  @Test
  public void testGetPassword() throws Exception{

  }
}