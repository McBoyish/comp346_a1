public class Logger {
  public static void println(Object message) {
    boolean debug = false;
    if (debug) {
      System.out.println(message.toString());
      return;
    }
    if (message.toString().startsWith("\n DEBUG :"))
      return;
    System.out.println(message);
  }
}
