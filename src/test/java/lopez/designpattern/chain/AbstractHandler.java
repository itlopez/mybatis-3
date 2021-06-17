package lopez.designpattern.chain;

/**
 * @author ：lopez
 * @date ：Created in 2021/6/17 16:17
 * @description：
 * @modified By：
 */
public abstract class AbstractHandler implements Handler{

  private Handler nextHandler;

  public Handler setNext(Handler handler) {
    this.nextHandler = handler;
    return handler;
  }

  public void handle(String event) {
    boolean result = doHandle(event);
    if (result && hasNext(nextHandler)) {
      nextHandler.handle(event);
    }
  }

  protected boolean hasNext(Handler nextHandler) {
    return nextHandler != null;
  }

  protected abstract boolean doHandle(String event);


}
