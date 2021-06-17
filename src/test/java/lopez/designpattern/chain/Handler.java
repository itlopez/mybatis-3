package lopez.designpattern.chain;

/**
 * @author ：lopez
 * @date ：Created in 2021/6/17 16:08
 * @description：
 * @modified By：
 */
public interface Handler {

  void handle(String event);

  Handler setNext(Handler handler);

}
