package lopez.designpattern.chain;

/**
 * @author ：lopez
 * @date ：Created in 2021/6/17 16:09
 * @description：
 * @modified By：
 */
public class AHandler extends AbstractHandler{

  @Override
  protected boolean doHandle(String event) {

    System.out.println("已处理了AHandler");
    return true;
  }
}
