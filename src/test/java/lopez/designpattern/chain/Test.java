package lopez.designpattern.chain;

/**
 * @author ：lopez
 * @date ：Created in 2021/6/17 18:46
 * @description：
 * @modified By：
 */
public class Test {

  public static void main(String[] args) {
    Handler handler = new AHandler();
    handler.setNext(new BHandler()).setNext(new CHandler());
    handler.handle("处理文字");
  }
}
