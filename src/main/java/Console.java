public final class Console {
  private Console() {
  }

  public static final String ANSI_RESET = "\u001B[0m";
  public static final String ANSI_BLACK = "\u001B[30m";
  public static final String ANSI_RED = "\u001B[31m";
  public static final String ANSI_GREEN = "\u001B[32m";
  public static final String ANSI_YELLOW = "\u001B[33m";
  public static final String ANSI_BLUE = "\u001B[34m";
  public static final String ANSI_PURPLE = "\u001B[35m";
  public static final String ANSI_CYAN = "\u001B[36m";
  public static final String ANSI_WHITE = "\u001B[37m";

  public static void printGreen(String output) {
    System.out.println(Console.ANSI_GREEN + output + Console.ANSI_RESET);
  }

  public static void printYellow(String output) {
    System.out.println(Console.ANSI_YELLOW + output + Console.ANSI_RESET);
  }

  public static void printRed(String output) {
    System.out.println(Console.ANSI_RED + output + Console.ANSI_RESET);
  }

  public static void printBlue(String output) {
    System.out.println(Console.ANSI_BLUE + output + Console.ANSI_RESET);
  }

  public static void printCyan(String output) {
    System.out.println(Console.ANSI_CYAN + output + Console.ANSI_RESET);
  }
}
