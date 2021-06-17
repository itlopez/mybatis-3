package lopez.designpattern.chain;

/**
 * @author ：lopez
 * @date ：Created in 2021/6/17 16:10
 * @description：
 * @modified By：
 */
public class CHandler extends AbstractHandler{

  @Override
  protected boolean doHandle(String event) {
    System.out.println("已处理了CHandler");
    return true;
  }
}
